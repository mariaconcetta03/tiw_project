package com.example.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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


@WebServlet("/GestioneContenutiServlet")
public class GestioneContenutiServlet extends HttpServlet {


@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String user = null;

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

		
		// Impostazione della risposta (pagina HTML)
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		out.println(
				"<html lang=\"it\"><head><meta charset=\"UTF-8\"><title>Home Page</title><meta charset=\"UTF-8\">\r\n"
						+ "<title>Gestione Contenuti</title>\r\n"
						+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
						+ "<link rel=\"stylesheet\" href=\"FolderStyle.css\"></head><body>");
		 
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
        
        out.println("<h1>Che cosa desideri fare?</h1>");

        // Bottone per azione 1
        out.println("<form action='NewRootFolderServlet' method='get'>");
        out.println("<button type='submit'>Nuova cartella nella ROOT</button>");
        out.println("</form>");
        out.println("<br><br>");

        // Bottone per azione 2
        out.println("<form action='NewSubfolderServlet' method='get'>");
        out.println("<button type='submit'>Nuova cartella interna</button>");
        out.println("</form>");
        out.println("<br><br>");

        // Bottone per azione 3
        out.println("<form action='NewFileServlet' method='get'>");
        out.println("<button type='submit'>Nuovo file in una cartella</button>");
        out.println("</form>");
        
		out.println("</body></html>");
	}

}

