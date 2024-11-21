package com.example;

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

@WebServlet("/HomeServlet")
public class HomeServlet extends HttpServlet {

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
	
		
	
	// Classe per rappresentare un file
		class File {
			Integer id;
			String proprietario;
			String nome;
			Date data_creazione;
			String sommario;
			String tipo;
			Integer cartella;
			
			public File (Integer id,
			String proprietario,
			String nome,
			Date data_creazione,
			String sommario,
			String tipo,
			Integer cartella) {
				this.id = id;
				this.proprietario = proprietario; // mail
				this.nome = nome;
				this.data_creazione = data_creazione;
				this.sommario = sommario;
				this.tipo = tipo;
				this.cartella = cartella;
	    	}
		}


		



	// Metodo per recuperare tutte le cartelle dal database
	private List<Folder> getFoldersFromDB(String user) {
		List<Folder> allFolders = new ArrayList<>();

		final String JDBC_URL = "jdbc:mysql://localhost:3306/tiw_project?serverTimezone=UTC";
		final String JDBC_USER = "root";
		final String JDBC_PASSWORD = "iononsonotu";

		// inizializzazione delle variabili necessarie per la query
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
    	// connessione al server SQL
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		// prepariamo la query SQL
		// prepared statements per evitare SQL-Injection
		String sql = "SELECT * FROM cartella WHERE sopracartella is NULL and proprietario = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, user);
			// preparedStatement.setNull(1, java.sql.Types.INTEGER); // prendiamo in
			// considerazione le cartelle più esterne
			// (le quali possono avere sottocartelle)

			// riceviamo il risultato della query SQL
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				Integer id = resultSet.getInt("id");
				String proprietario = resultSet.getString("proprietario");
				String nome = resultSet.getString("nome");
				Date data_creazione = resultSet.getDate("data_creazione");
				// se sopracartella è diverso da null, allora metto ID della sopracartella,
				// altrimenti metto NULL
				Integer sopracartella = resultSet.getObject("sopracartella") != null ? resultSet.getInt("sopracartella")
						: null;
				Folder folderToAdd = new Folder(id, proprietario, nome, data_creazione, sopracartella); // aggiungo la
																										// cartella più
																										// esterna
				folderToAdd.sottocartelle = null; // default
				folderToAdd.sottocartelle = getSubfolders(folderToAdd.id, user);
				allFolders.add(folderToAdd);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allFolders;
	}

	// Metodo per recuperare tutte le sottocartelle di una specifica cartella
	private List<Folder> getSubfolders(Integer idToSearch, String user) {
		final String JDBC_URL = "jdbc:mysql://localhost:3306/tiw_project?serverTimezone=UTC";
		final String JDBC_USER = "root";
		final String JDBC_PASSWORD = "iononsonotu";
		List<Folder> subFolders = new ArrayList<>();

		// inizializzazione delle variabili necessarie per la query
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		// connessione al server SQL
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		// prepariamo la query SQL
		// prepared statements per evitare SQL-Injection
		String sql = "SELECT * FROM cartella WHERE sopracartella = ? and proprietario = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, idToSearch); // prendiamo in considerazione le cartelle più esterne (le quali
														// possono avere sottocartelle)
														// per evitare sql injection (un utente malevolo saprebbe i
														// valori da utilizare) quindi si settano man mano i valori
			preparedStatement.setString(2, user);

			// riceviamo il risultato della query SQL
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				Integer id = resultSet.getInt("id");
				String proprietario = resultSet.getString("proprietario");
				String nome = resultSet.getString("nome");
				Date data_creazione = resultSet.getDate("data_creazione");
				// se sopracartella è diverso da null, allora metto ID della sopracartella,
				// altrimenti metto NULL
				Integer sopracartella = resultSet.getObject("sopracartella") != null ? resultSet.getInt("sopracartella")
						: null;
				Folder folderToAdd = new Folder(id, proprietario, nome, data_creazione, sopracartella); // aggiungo la
																										// cartella più
																										// esterna
				folderToAdd.sottocartelle = null; // default
				folderToAdd.sottocartelle = getSubfolders(folderToAdd.id, user);
				subFolders.add(folderToAdd);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return subFolders;
	}

	// Metodo per generare il codice HTML ricorsivamente dell'albero delle cartelle
	// Inizia a mettere il primo folder
	private void generateHtmlForFolder(PrintWriter out, Folder f, HttpSession session) {
		
		// Prendiamo la map dei token dalla sessione
		Map<String, Integer> folderTokens = (Map<String, Integer>) session.getAttribute("folderTokens");
		
   		// aggiungiamo il token della nuova cartella
        String token = UUID.randomUUID().toString(); // Un token casuale o identificatore offuscato
        folderTokens.put(token, f.id);

    	
        // aggiorniamo i token della sessione
    	session.setAttribute("folderTokens", folderTokens);    
    
    
		out.println("<li class=\"folder\"> <a href='ContenutiServlet?folderToken=" + token + "'>" + f.nome + "</a><br>"); // creo la cartella più
											// esterna
		
		if (f.sottocartelle != null) { // se ho sottocartelle, allora chiamo la funzione ricorsivamente per tutte le
										// sottocartelle
			out.println("<ul>"); // inizia la lista non ordinata
			for (Folder sub : f.sottocartelle) {
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
		File file = null;
		Folder folder = null;
		String  nomeFile = null;
		String nomeCartella = null;
		HttpSession session = request.getSession(); // false -> check se sessione esiste oppure no (nel caso in cui
															// non esista restituisce null)
		String origin = (String) session.getAttribute("originServlet");
		
		Map<String,Integer> fileTokens = null;
		
		// CASO SPOSTA
		if (origin != null && origin.equals("SpostaServlet")){
			Folder sopracartella = null;
			//tutte le robe che deve fare dopo sposta 
			session.setAttribute("originServlet", null);
			// ricevo nome utente (email) dalla sessione e metto i foldertokens come attributi
		if (session != null) {
			user = session.getAttribute("email").toString();
			fileTokens = (Map<String,Integer>)session.getAttribute("fileTokens");
		}

		// Connessione al database e recupero delle cartelle (vengono messe in
		// allFolders)
		allFolders = getFoldersFromDB(user);
		String fileToMoveToken = request.getParameter("fileToken");
		Integer idFileToMove = fileTokens.get(fileToMoveToken); //ottengo in questo modo id della cartella associata 
	
		final String JDBC_URL = "jdbc:mysql://localhost:3306/tiw_project?serverTimezone=UTC";
		final String JDBC_USER = "root";
		final String JDBC_PASSWORD = "iononsonotu";

		// inizializzazione delle variabili necessarie per la query
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		Integer IDcartella = null;
		
		// connessione al server SQL
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		// prepariamo la query SQL
		// prepared statements per evitare SQL-Injection
		String sql = "SELECT nome, cartella FROM documento WHERE id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, idFileToMove); // prendiamo in considerazione le cartelle più esterne (le quali
														// possono avere sottocartelle)
														// per evitare sql injection (un utente malevolo saprebbe i
														// valori da utilizare) quindi si settano man mano i valori
			
			// riceviamo il risultato della query SQL
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				
				nomeFile = resultSet.getString("nome");
				// se sopracartella è diverso da null, allora metto ID della sopracartella,
				// altrimenti metto NULL
				IDcartella = resultSet.getInt("cartella");
			}
		
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		
		sql = "SELECT nome FROM cartella WHERE id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, IDcartella); // prendiamo in considerazione le cartelle più esterne (le quali
														// possono avere sottocartelle)
														// per evitare sql injection (un utente malevolo saprebbe i
														// valori da utilizare) quindi si settano man mano i valori
			
			// riceviamo il risultato della query SQL
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				
				nomeCartella = resultSet.getString("nome");
				// se sopracartella è diverso da null, allora metto ID della sopracartella,
				// altrimenti metto NULL
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
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
			generateHtmlForMovingFolder(out, folder1, session, IDcartella, fileToMoveToken);
		}
		
		//BLOCCATI

		out.println("</ul>");
		out.println("</div>");
		out.println("</body></html>");





		// CASO HOME PAGE NORMALE
		} else {
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
		allFolders = getFoldersFromDB(user);

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




private void generateHtmlForMovingFolder(PrintWriter out, Folder f, HttpSession session, Integer originFolderID, String fileToken) {
	if (!f.id.equals(originFolderID)) {
	
	// Prendiamo la map dei token dalla sessione
	Map<String, Integer> folderTokens = (Map<String, Integer>) session.getAttribute("folderTokens");
	
		// aggiungiamo il token della nuova cartella
    String token = UUID.randomUUID().toString(); // Un token casuale o identificatore offuscato
    folderTokens.put(token, f.id);

	
    // aggiorniamo i token della sessione
	session.setAttribute("folderTokens", folderTokens); 
	   
	// VIRTUALIZZAZIONE DEL TASTO "SPOSTA" (ogni singola cartella è come un bottone SPOSTA)
	out.println("<li class=\"folder\">");
	out.println("<form action='SpostaServlet' method='POST' style='display:inline;'>");
	out.println("<input type='hidden' name='folderToken' value='" + token + "'>");
		out.println("<input type='hidden' name='fileToken' value='" + fileToken + "'>");

	out.println("<button type='submit' style='background:none; border:none; color:blue; text-decoration:underline; cursor:pointer;'>");
	out.println(f.nome);
	out.println("</button>");
	out.println("</form>");
	out.println("</li>");										// esterna
	
	
	if (f.sottocartelle != null) { // se ho sottocartelle, allora chiamo la funzione ricorsivamente per tutte le
									// sottocartelle
		out.println("<ul>"); // inizia la lista non ordinata
		for (Folder sub : f.sottocartelle) {
			generateHtmlForMovingFolder(out, sub, session, originFolderID, fileToken); // chiamata ricorsiva
		}
		out.println("</ul>"); // fine della lista non ordinata --- per sottocartelle uso le "ul"
	}
	out.println("</li>"); // fine della cartella più esterna -- list item
 }  else {
	 	out.println("<li class=\"folder\" style='background:none; border:none; color:red;'>");
		out.println(f.nome);
		
		// se ho delle sottocartelle, allora non posso saltarle. Infatti è possibile spostare un file da una cartella
		// principale ad una sua sottocartella
		if (f.sottocartelle != null) { // se ho sottocartelle, allora chiamo la funzione ricorsivamente per tutte le
			// sottocartelle
			out.println("<ul>"); // inizia la lista non ordinata
			for (Folder sub : f.sottocartelle) {
				// tolgo la formattazione precedente, poichè i figli non devono essere evidenziati in giallo
		        out.println("<li class=\"folder\" style='background:none; border:none; color:black;'>");
		        generateHtmlForMovingFolder(out, sub, session, originFolderID, fileToken); // chiamata ricorsiva
		        out.println("</li>");			}
			out.println("</ul>"); // fine della lista non ordinata --- per sottocartelle uso le "ul"
		}
			out.println("</li>"); // fine della cartella più esterna -- list item
		
		
}
}

}

