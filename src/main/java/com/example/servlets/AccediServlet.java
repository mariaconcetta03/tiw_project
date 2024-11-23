package com.example.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.DAOs.*;
import com.example.beans.*;

@WebServlet("/AccediServlet")
public class AccediServlet extends HttpServlet {

	DocumentoDao documentoDao = null;
	CartellaDao cartellaDao = null;

	// questa funzione viene eseguita solo una volta quando la servlet
	// viene caricata in memoria
	@Override
	public void init() {
		documentoDao = new DocumentoDao();
		cartellaDao = new CartellaDao();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(); // false -> check se sessione esiste oppure no (nel caso in cui
													// non esista restituisce null)
		Map<String, Integer> fileTokens = null;
		File f = null;
		String nomeCartella = null;

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
			fileTokens = (Map<String, Integer>) session.getAttribute("fileTokens");
		}

		// prendo dall'URL il token del file selezionato
		String fileToken = request.getParameter("fileToken");

		Integer fileId = 0;
		if (fileTokens != null && fileTokens.containsKey(fileToken)) { // se il token corrisponde ad uno effettivamente
																		// esistente
			fileId = fileTokens.get(fileToken); // ID della cartella
		}

		f = documentoDao.findDocumentoByID(fileId);

		nomeCartella = cartellaDao.getNomeCartellaById(f.getCartella());

		// Impostazione della risposta (pagina HTML)
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		out.println(
				"<html lang=\"it\"><head>\r\n" + "<meta charset=\"UTF-8\">\r\n" + "<title>Info documento</title>\r\n"
						+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
						+ "</head></head><body>");

		// Link per fare il logout (rimando alla servlet di logout)
		out.println("<a href=\"LogoutServlet\">Logout</a>");

		// Link per tornare alla pagina precedente
		// nota bene: &nbsp = 1 SPAZIO BIANCO (separa "logout" e "torna alla pagina
		// precedente")
		out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "<form action='BackServlet' method='post' style='display:inline;'>"
				+ "<button type='submit' style='background:none; border:none; color:blue; text-decoration:underline; cursor:pointer;'>"
				+ "Torna alla pagina precedente" + "</button>" + "</form>");

		out.println("<h1> Informazioni del documento selezionato: </h1>");
		out.println("<b>Nome documento:</b> " + f.getNome() + "<br>");
		out.println("<b>E-mail del proprietario:</b> " + f.getProprietario() + "<br>");
		out.println("<b>Data di creazione:</b> " + f.getData_creazione() + "<br>");
		out.println("<b>Sommario:</b> " + f.getSommario() + "<br>");
		out.println("<b>Tipo:</b> " + f.getTipo() + "<br>");
		out.println("<b>Cartella:</b> " + nomeCartella + "<br>");

		out.println("<div class=\"tree\">");
		out.println("<ul>");
		out.println("</ul>");
		out.println("</div>");
		out.println("</body></html>");
	}
}