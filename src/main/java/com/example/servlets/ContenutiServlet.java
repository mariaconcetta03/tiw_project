package com.example.servlets;

import java.io.IOException;
import java.io.PrintWriter;
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
import com.example.beans.*;
import com.example.DAOs.DocumentoDao;
import com.example.DAOs.CartellaDao;

@WebServlet("/ContenutiServlet")
public class ContenutiServlet extends HttpServlet { 
	//Lista di elemennti in una cartella qualsiasi (di root o sottocartella)
	


	DocumentoDao documentoDao = null;
	CartellaDao cartellaDao = null;
	// questa funzione viene eseguita solo una volta quando la servlet
	// viene caricata in memoria
	@Override
	public void init(){
		documentoDao = new DocumentoDao();
		cartellaDao = new CartellaDao();
	}
	
	
	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession(); // false -> check se sessione esiste oppure no (nel caso in cui non esista restituisce null)
		String user = null;
		String folderToken = request.getParameter("folderToken");
        Map<String, Integer> folderTokens = (Map<String, Integer>) session.getAttribute("folderTokens");
		Map<String, Integer> fileTokens = new HashMap<>();

		
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



        if (session != null) {
        	user = session.getAttribute("email").toString();
        }
        Integer folderId = 0;
        String folderName  = null;
        List<Folder> folders = null;
        List<File> files = null;
        
        if (folderTokens != null && folderTokens.containsKey(folderToken)) { // se il token corrisponde ad uno effettivamente esistente
        	folderId = folderTokens.get(folderToken); // ID della cartella
        	 folders = cartellaDao.getSubfoldersFromDB(user, folderId);
             files = documentoDao.getDocsFromFolder(user, folderId);
        } 	

		folderName = cartellaDao.getNomeCartellaById(folderId);
		
		// Impostazione della risposta (pagina HTML)
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		out.println(
				"<html lang=\"it\"><head>\r\n"
				+ "<meta charset=\"UTF-8\">\r\n"
				+ "<title>Contenuti</title>\r\n"
				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
				+ "<link rel=\"stylesheet\" href=\"ContenutiStyle.css\">\r\n"
				+ "</head></head><body>");
		
		
		// Link per fare il logout (rimando alla servlet di logout)
        out.println("<a href=\"LogoutServlet\">Logout</a>"); 
        
        // Link per tornare alla pagina precedente
	        // nota bene: &nbsp = 1 SPAZIO BIANCO (separa "logout" e "torna alla pagina precedente")
			out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
			    "<form action='BackServlet' method='post' style='display:inline;'>" +
			    "<button type='submit' style='background:none; border:none; color:blue; text-decoration:underline; cursor:pointer;'>" +
			    "Torna alla pagina precedente" +
			    "</button>" +
			    "</form>");	    
			
		out.println("<h1> Contenuti della cartella: "+ folderName +"</h1>");
		out.println("<div class=\"tree\">");
		out.println("<ul>");

		// metto tutte le cartelle e tutti i file trovati
		for(Folder f: folders) { // cartelle
			out.println("<li class=\"folder\">"+ f.getNome() +"</li>");
		}
		
		for(File f: files) { // docs
		    String token = UUID.randomUUID().toString(); // Un token casuale o identificatore offuscato

		    out.println("<li class=\"file\">" + f.getNome()
		    + " <a href=\"AccediServlet?fileToken=" + token + "\">   Accedi</a>" 
		    + " <a href=\"SpostaServlet?fileToken=" + token + "\">   Sposta</a>"
		    + "</li>");		
		    fileTokens.put(token, f.getId());
		}
		session.setAttribute("fileTokens", fileTokens);
		

		out.println("</ul>");
		out.println("</div>");
		out.println("</body></html>");

	}
}