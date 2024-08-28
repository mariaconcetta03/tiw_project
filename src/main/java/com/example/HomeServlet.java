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

	// Metodo per generare il codice HTML ricorsivamente
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

		HttpSession session = request.getSession(false); // false -> check se sessione esiste oppure no (nel caso in cui
															// non esista restituisce null)
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
		out.println("<h1>Le tue cartelle:</h1>");
		out.println("<div class=\"tree\">");
		out.println("<ul>");

		// Generazione ricorsiva del codice HTML
		for (Folder folder : allFolders) {
			generateHtmlForFolder(out, folder, session);
		}

		out.println("</ul>");
		out.println("</div>");
		out.println("</body></html>");
	}
}