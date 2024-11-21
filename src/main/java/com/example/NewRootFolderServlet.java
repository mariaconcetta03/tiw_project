package com.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.ContenutiServlet.Folder;

// 1. Cartella nella ROOT (sovracartella = NULL)
// 2. Cartella all'interno di una cartella (sovracartella != NULL)
// 3. file SOLO all'interno di una cartella

@WebServlet("/NewRootFolderServlet")
public class NewRootFolderServlet extends HttpServlet {

	// Classe per rappresentare una cartella
	class Folder {
		Integer id;
		String proprietario;
		String nome;
		Date data_creazione;
		Integer sopracartella;
		List<Folder> sottocartelle = new ArrayList<>();

		public Folder(Integer id, String proprietario, String nome, Date data_creazione, Integer sopracartella) {
			this.id = id;
			this.proprietario = proprietario; // mail
			this.nome = nome;
			this.data_creazione = data_creazione;
			this.sopracartella = sopracartella;
			this.sottocartelle = null;
		}
		
	}
	
	
	
	
	
	
	
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
        createFolderIntoDB(user, nome, Date.valueOf(LocalDate.now()));
        //si crea il valore della data automaticamente
        
        response.sendRedirect("HomeServlet");
        
     }
	  
	  
	  
	  	// Metodo per creare cartelle
		private void createFolderIntoDB(String proprietario, String nome, Date data_creazione) {
			final String JDBC_URL = "jdbc:mysql://localhost:3306/tiw_project?serverTimezone=UTC";
			final String JDBC_USER = "root";
			final String JDBC_PASSWORD = "iononsonotu";

			// inizializzazione delle variabili necessarie per la query
			Connection connection = null;
			PreparedStatement preparedStatement = null;

			// connessione al server SQL
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}

			// prepariamo la query SQL per estrarre le CARTELLE
			// prepared statements per evitare SQL-Injection
	
			
			String sql = "INSERT INTO cartella (proprietario, nome, data_creazione, sopracartella) values (?,?,?,?)";
			try {
			
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, proprietario);
				preparedStatement.setString(2, nome);
				preparedStatement.setDate(3, data_creazione);
				preparedStatement.setNull(4, java.sql.Types.INTEGER);
				
				preparedStatement.executeUpdate();


			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		

	
}