package com.example.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.LinkedList;
import java.time.LocalDate;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.DAOs.*;

// 1. Cartella nella ROOT (sovracartella = NULL)
// 2. Cartella all'interno di una cartella (sovracartella != NULL)
// 3. file SOLO all'interno di una cartella

@WebServlet("/NewRootFolderServlet")
public class NewRootFolderServlet extends HttpServlet {

	CartellaDao cartellaDao = null;
	
	// questa funzione viene eseguita solo una volta quando la servlet
	// viene caricata in memoria
	@Override
	public void init(){
		cartellaDao = new CartellaDao();
	}
	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

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
		

		
		// Impostazione della risposta (pagina HTML)
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		out.println(
				"<html lang=\"it\"><head><meta charset=\"UTF-8\"><title>Nouva cartella</title><meta charset=\"UTF-8\">\r\n"
						+ "<title>Nuova cartella</title>\r\n"
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
        
        out.println("<h1>Compila il seguente campo: </h1>"+"<FORM action = \"NewRootFolderServlet\"\r\n"
        			+ "method = \"post\" >"+ "<P>\r\n" + "<b>Nome cartella:</b>\r\n"  + "    <br><br>\r\n"
  				    + "    <INPUT type=\"text\"  name = \"nome\" required>\r\n"
				    + "  </P>\r\n"
				    + ""+"<INPUT type = 'submit' VALUE = CREA>\r\n"
				    + "\r\n"
				    + "</FORM>");
        
        
        
		out.println("</body></html>");
	}




	  // metodo che viene chiamato nel momento in cui l'utente ha finitpo di inserire i dati nel form HTML
	  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();        // Recupero dei dati dal form
        String user = null;
        String nome = request.getParameter("nome");
        // ricevo nome utente (email) dalla sessione e metto i foldertokens come attributi
		if (session != null) {
			user = session.getAttribute("email").toString();
			}
        cartellaDao.createRootFolderIntoDB(user, nome, Date.valueOf(LocalDate.now()));
        //si crea il valore della data automaticamente
        
        response.sendRedirect("HomeServlet");
        
     }
	  
	  
	 
	
}