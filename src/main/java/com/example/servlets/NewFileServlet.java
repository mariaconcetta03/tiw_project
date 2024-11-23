package com.example.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.DAOs.*;
import com.example.beans.*;


@WebServlet("/NewFileServlet")
public class NewFileServlet extends HttpServlet {

	DocumentoDao documentoDao = null;
	CartellaDao cartellaDao = null;
	
	// questa funzione viene eseguita solo una volta quando la servlet
	// viene caricata in memoria
	@Override
	public void init(){
		documentoDao = new DocumentoDao();
		cartellaDao = new CartellaDao();
	}
	
	

	// Metodo per generare il codice HTML ricorsivamente dell'albero delle cartelle
	// Inizia a mettere il primo folder
	private void generateHtmlForFolder(PrintWriter out, Folder f, HttpSession session) {

		// Prendiamo la map dei token dalla sessione
		Map<String, Integer> folderTokens = (Map<String, Integer>) session.getAttribute("folderTokens");

		// aggiungiamo il token della nuova cartella
		String token = UUID.randomUUID().toString(); // Un token casuale o identificatore offuscato
		folderTokens.put(token, f.getId());

		// aggiorniamo i token della sessione
		session.setAttribute("folderTokens", folderTokens);

		out.println("<li class=\"folder\"> <a href=" + "\"NewFileServlet?action=getChosenFolder&folder=" + token
				+ "\" class=\"highlight\" type=\"submit\">" + f.getNome() + " </a>");

		if (f.getSottocartelle() != null) { // se ho sottocartelle, allora chiamo la funzione ricorsivamente per tutte le
										// sottocartelle
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

		String user = null;
		String folderName = null;

		HttpSession session = request.getSession(); // false -> check se sessione esiste oppure no (nel caso in cui
													// non esista restituisce null)

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

		// ricevo nome utente (email) dalla sessione
		if (session != null) {
			user = session.getAttribute("email").toString();
		}

		// Connessione al database e recupero delle cartelle (vengono messe in
		// allFolders)
		List<Folder> allFolders = cartellaDao.getAllUserFolder(user);

		// Impostazione della risposta (pagina HTML)
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		out.println(
				"<html lang=\"it\"><head><meta charset=\"UTF-8\"><title>Nuovo file</title><meta charset=\"UTF-8\">\r\n"
						+ "<title>Nuovo file</title>\r\n"
						+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
						+ "<link rel=\"stylesheet\" href=\"FolderStyle.css\">"
						+ "<link rel=\"stylesheet\" href=\"highlightOnClick.css\"></head><body>");

		// Link per fare il logout (rimando alla servlet di logout)
		out.println("<a href=\"LogoutServlet\">Logout</a>");

		// Link per tornare alla pagina precedente
		// nota bene: &nbsp = 1 SPAZIO BIANCO (separa "logout" e "torna alla pagina
		// precedente")
		out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "<form action='BackServlet' method='post' style='display:inline;'>"
				+ "<button type='submit' style='background:none; border:none; color:blue; text-decoration:underline; cursor:pointer;'>"
				+ "Torna alla pagina precedente" + "</button>" + "</form>");

		// Scelta cartella
		out.println("<h2>Seleziona ora la cartella di destinazione: </h2>");
		out.println("<div class=\"tree\">");
		out.println("<ul>");

		// Generazione ricorsiva del codice HTML per albero delle cartelle
		for (Folder folder : allFolders) {
			generateHtmlForFolder(out, folder, session);
		}
		out.println("</ul>");
		out.println("</div>");

		// Messaggio di selezione della cartella
		String action = request.getParameter("action");
		if ("getChosenFolder".equals(action)) { // Gestisci la selezione della cartella
			String folder = request.getParameter("folder"); // folder = il token della cartella di destinazione
			Map<String, Integer> folderTokens = (Map<String, Integer>) session.getAttribute("folderTokens");
			Integer idSopracartella = folderTokens.get(folder);

			// salviamo ID sopracartella nella sessione, poichè ci servirà per la creazione
			// del file
			session.setAttribute("idSopracartella", idSopracartella);

			folderName = cartellaDao.getNomeCartellaById(idSopracartella);

			out.println("Hai selezionato la cartella: " + folderName);
		}

		// Form per inserire il nome del file da creare, il tipo del file e il sommario
		out.println("<h1>Compila i seguenti campi: </h1>" + "<FORM action = \"NewFileServlet\"\r\n"
				+ "method = \"post\" >" + "<P>\r\n" + "<b>Nome file:</b>\r\n" + "    <br><br>\r\n"
				+ "    <INPUT type=\"text\"  name = \"nome\" required>\r\n" + "  </P>\r\n");

		out.println("<FORM action = \"NewFileServlet\"\r\n" + "method = \"post\" size = 60>" + "<P>\r\n"
				+ "<b>Sommario (facoltativo):</b>\r\n" + "    <br><br>\r\n"
				+ "    <INPUT type=\"text\"  name = \"sommario\">\r\n" + "  </P>\r\n");

		out.println("<FORM action = \"NewFileServlet\"\r\n" + "method = \"post\">" + "<P>\r\n" + "<b>Tipo:</b>\r\n"
				+ "    <br><br>\r\n" + "    <INPUT type=\"text\"  name = \"tipo\" required>\r\n" + "  </P>\r\n");

		out.println("<div style=\"color: red;\">\r\n" + "			     <p id=\"errorMessage\"></p>\r\n"
				+ "			    </div>\r\n" + "\r\n" + "			 	<script>\r\n" + "			     \r\n"
				+ "			        const params = new URLSearchParams(window.location.search); "
				+ "			        const errorMessage = params.get('error');" + "			        if (errorMessage) {"
				+ "			            document.getElementById('errorMessage').textContent = errorMessage;"
				+ "			        }\r\n" + "			    </script>");
		// tasto CREA
		out.println("<INPUT type = 'submit' VALUE = CREA>\r\n" + "\r\n" + "</FORM>");
		out.println("</body></html>");

	}

	// metodo che viene chiamato nel momento in cui l'utente ha finito di inserire i
	// dati nel form HTML
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(); // Recupero dei dati dal form
		String user = null;
		String nome = request.getParameter("nome"); // nome del file da creare
		String tipo = request.getParameter("tipo");
		String sommario = request.getParameter("sommario");
		Integer idSopracartella = (Integer) session.getAttribute("idSopracartella");

		// attributi
		if (session != null) {
			user = session.getAttribute("email").toString();
		}

		if (idSopracartella != null) {
			documentoDao.createFile(user, nome, Date.valueOf(LocalDate.now()), sommario, tipo, idSopracartella);
			session.setAttribute("idSopracartella", null); // metto a null il valore di idSopracartella, per far
															// apparire il messaggio
															// di errore in caso di future creazioni di cartella
			response.sendRedirect("HomeServlet");
		} else {
			String errorMessage = "Nessuna cartella di destinazione selezionata!";
			response.sendRedirect("NewFileServlet?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
			return;
		}

	}

	
	
	
	

}
