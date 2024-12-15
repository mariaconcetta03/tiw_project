package com.example.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.DAOs.CartellaDao;
import com.example.DAOs.DocumentoDao;
import com.example.beans.*;


@WebServlet("/HomeServlet")
public class HomeServlet extends HttpServlet {

	DocumentoDao documentoDao = null;
	CartellaDao cartellaDao = null;
	
	// questa funzione viene eseguita solo una volta quando la servlet
	// viene caricata in memoria
	@Override
	public void init() {
		documentoDao = new DocumentoDao();
		cartellaDao = new CartellaDao();
	}

	
	// Metodo per generare il codice HTML ricorsivamente dell'albero delle cartelle
	private void generateHtmlForFolder(PrintWriter out, Folder f, HttpSession session) {
		
		// Prendiamo la map dei token dalla sessione
		Map<String, Integer> folderTokens = (Map<String, Integer>) session.getAttribute("folderTokens");
		
   		// aggiungiamo il token della nuova cartella
        String token = UUID.randomUUID().toString(); // Un token casuale o identificatore offuscato
        folderTokens.put(token, f.getId());

        // aggiorniamo i token della sessione
    	session.setAttribute("folderTokens", folderTokens);    
    
		out.println("<li class=\"folder\"> <a href='ContenutiServlet?folderToken=" + token + "'>" + f.getNome() + "</a><br>"); // creo la cartella più esterna
		
		if (f.getSottocartelle() != null) { // se ho sottocartelle, allora chiamo la funzione ricorsivamente per tutte le sottocartelle
			out.println("<ul>"); // inizia la lista non ordinata
			for (Folder sub : f.getSottocartelle()) {
				generateHtmlForFolder(out, sub, session); // chiamata ricorsiva
			}
			out.println("</ul>"); // fine della lista non ordinata --- per sottocartelle uso le "ul"
		}
		out.println("</li>"); // fine della cartella più esterna -- list item
	}

	

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Map<String, Integer> folderTokens = new HashMap<>();
		List<Folder> allFolders = new ArrayList<>();
		String user = null;
		String  nomeFile = null;
		String nomeCartella = null;
		HttpSession session = request.getSession(); 

		String origin = (String) session.getAttribute("originServlet"); //servlet d'origine (homepage) che diventa poi servlet per fare sposta (home page con contenuti selezionabili)
		Integer IDCartella = null;
		Map<String,Integer> fileTokens = null;
		
		// CASO SPOSTA
		if (origin != null && origin.equals("SpostaServlet")){
					//tutte le robe che deve fare dopo sposta 
					session.setAttribute("originServlet", null);
					// ricevo nome utente (email) dalla sessione e metto i foldertokens come attributi
				if (session != null) {
					user = session.getAttribute("email").toString();
					fileTokens = (Map<String,Integer>)session.getAttribute("fileTokens");
				}
		
				// Connessione al database e recupero delle cartelle (vengono messe in
				// allFolders)
				allFolders = cartellaDao.getAllUserFolder(user);
				String fileToMoveToken = request.getParameter("fileToken");
				Integer idFileToMove = fileTokens.get(fileToMoveToken); // ottengo in questo modo id della cartella associata 
			
				File f = documentoDao.findDocumentoByID (idFileToMove); // file da spostare
				nomeFile = f.getNome(); // nome file da spostare
				IDCartella = f.getCartella(); // vecchia cartella del file
				
				nomeCartella = cartellaDao.getNomeCartellaById(IDCartella); // nome vecchia cartella del file
				
				// Impostazione della risposta (pagina HTML)
				response.setContentType("text/html");
				PrintWriter out = response.getWriter();
		
				out.println(
						"<html lang=\"it\"><head><meta charset=\"UTF-8\"><title>Home Page</title><meta charset=\"UTF-8\">\r\n"
								+ "<title>Home Page</title>\r\n"
								+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
								+ "<link rel=\"stylesheet\" href=\"FolderStyle.css\"></head><body>");
				 
				// Link per fare il logout (rimando alla servlet di logout)
		        out.println("<a href=\"LogoutServlet\">Logout</a>");
		     	out.println ("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp"); // spaziatura
		        
				out.println("<h1>Stai spostando il documento \"" + nomeFile + "\" dalla cartella \"" + nomeCartella + "\".</h1>");
				out.println("<h3>Scegli la cartella di destinazione.</h3>");
				out.println("<div class=\"tree\">");
				out.println("<ul>");
		
				// Generazione ricorsiva del codice HTML
				for (Folder folder1 : allFolders) {
					generateHtmlForMovingFolder(out, folder1, session, IDCartella, fileToMoveToken);
				}
				
				out.println("</ul>");
				out.println("</div>");
				out.println("</body></html>");
		
		} else {
			// CASO HOME PAGE NORMALE
			
				 // CODICE PER GESTIONE PAGINE PRECEDENTI -----------------------------
				 // Ottieni la parte principale dell'URL
					String currentPage = request.getRequestURL().toString();
					
					// Aggiungi la query string, se esiste
					String queryString = request.getQueryString();
					if (queryString != null) {
					    currentPage += "?" + queryString;
					    }
		
		        // Recupera o inizializza la cronologia nella sessione
		        LinkedList<String> history = (LinkedList<String>) session.getAttribute("pageHistory");
		        if (history == null) {
		            history = new LinkedList<>();
		        }
		
		        // Aggiungi la pagina corrente alla cronologia, evitando duplicati consecutivi
		        if (history.isEmpty() || !history.getLast().equals(currentPage)) {
		            history.add(currentPage);
		        }
		        
		        // Salva la cronologia nella sessione
		        session.setAttribute("pageHistory", history);
				// -------------------------------------------------------------------
				
				
				// ricevo nome utente (email) dalla sessione e metto i foldertokens come attributi
				if (session != null) {
					user = session.getAttribute("email").toString();
					session.setAttribute("folderTokens", folderTokens);
				}
		
				// Connessione al database e recupero delle cartelle (vengono messe in
				// allFolders)
				allFolders = cartellaDao.getAllUserFolder(user);
		
				// Impostazione della risposta (pagina HTML)
				response.setContentType("text/html");
				PrintWriter out = response.getWriter();
		
				out.println(
						"<html lang=\"it\"><head><meta charset=\"UTF-8\"><title>Home Page</title><meta charset=\"UTF-8\">\r\n"
								+ "<title>Home Page</title>\r\n"
								+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
								+ "<link rel=\"stylesheet\" href=\"FolderStyle.css\"></head><body>");
				 
				// Link per fare il logout (rimando alla servlet di logout)
		        out.println("<a href=\"LogoutServlet\">Logout</a>");
		     	out.println ("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp"); // spaziatura
		        out.println("<a href=\"GestioneContenutiServlet\">Gestione contenuti</a>");
		        
				out.println("<h1>Le tue cartelle:</h1>");
				out.println("<div class=\"tree\">");
				out.println("<ul>");
		
				// Generazione ricorsiva del codice HTML
				for (Folder folder1 : allFolders) {
					generateHtmlForFolder(out, folder1, session);
				}
		
				out.println("</ul>");
				out.println("</div>");
				out.println("</body></html>");
		}
		
	}




	// Questo metodo genera l'HTML dell'albero per muovere un file
	private void generateHtmlForMovingFolder(PrintWriter out, Folder f, HttpSession session, Integer originFolderID, String fileToken) {
		if (!f.getId().equals(originFolderID)) { // se la cartella è una cartella in cui io posso spostare
		
		// Prendiamo la map dei token dalla sessione
		Map<String, Integer> folderTokens = (Map<String, Integer>) session.getAttribute("folderTokens");
		
		// aggiungiamo il token della nuova cartella
	    String token = UUID.randomUUID().toString(); // Un token casuale
	    folderTokens.put(token, f.getId());
		
	    // aggiorniamo i token della sessione
		session.setAttribute("folderTokens", folderTokens); 
		   
		// VIRTUALIZZAZIONE DEL TASTO "SPOSTA" (ogni singola cartella è come un bottone SPOSTA)
		out.println("<li class=\"folder\">");
		out.println("<form action='SpostaServlet' method='POST' style='display:inline;'>");
		out.println("<input type='hidden' name='folderToken' value='" + token + "'>");
			out.println("<input type='hidden' name='fileToken' value='" + fileToken + "'>");
		out.println("<button type='submit' style='background:none; border:none; color:blue; text-decoration:underline; cursor:pointer;'>");
		out.println(f.getNome());
		
		out.println("</button>");
		out.println("</form>");
		out.println("</li>");
		
		
		if (f.getSottocartelle() != null) { // se ho sottocartelle, allora chiamo la funzione ricorsivamente per tutte le sottocartelle
			out.println("<ul>"); // inizia la lista non ordinata
			for (Folder sub : f.getSottocartelle()) {
				generateHtmlForMovingFolder(out, sub, session, originFolderID, fileToken); // chiamata ricorsiva
			}
			out.println("</ul>"); // fine della lista non ordinata --- per sottocartelle uso le "ul"
		}
		out.println("</li>"); // fine della cartella più esterna -- list item
		
		
	 }  else { // la cartella è la stessa, quindi non posso spostare il documento 
		 	out.println("<li class=\"folder\" style='background:none; border:none; color:red;'>"); // in rosso
			out.println(f.getNome());
			
			// se ho delle sottocartelle, allora non posso saltarle. Infatti è possibile spostare un file da una cartella
			// principale ad una sua sottocartella
			if (f.getSottocartelle() != null) { // se ho sottocartelle, allora chiamo la funzione ricorsivamente per tutte le
				// sottocartelle
				out.println("<ul>"); // inizia la lista non ordinata
				for (Folder sub : f.getSottocartelle()) {
					// tolgo la formattazione precedente, poichè i figli non devono essere evidenziati in giallo
			        out.println("<li class=\"folder\" style='background:none; border:none; color:black;'>"); // in nero
			        generateHtmlForMovingFolder(out, sub, session, originFolderID, fileToken); // chiamata ricorsiva
			        out.println("</li>");			}
				out.println("</ul>"); // fine della lista non ordinata --- per sottocartelle uso le "ul"
			}
				out.println("</li>"); // fine della cartella più esterna -- list item
	}
}

}

