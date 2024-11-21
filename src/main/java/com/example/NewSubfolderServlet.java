package com.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.NewSubfolderServlet.Folder;

@WebServlet("/NewSubfolderServlet")
public class NewSubfolderServlet extends HttpServlet {

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

		out.println("<li class=\"folder\"> <a href=" + "\"NewSubfolderServlet?action=getChosenFolder&folder=" + token
				+ "\" class=\"highlight\" type=\"submit\">" + f.nome + " </a>");

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
		List<Folder> allFolders = getFoldersFromDB(user);

		// Impostazione della risposta (pagina HTML)
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		out.println(
				"<html lang=\"it\"><head><meta charset=\"UTF-8\"><title>Nuova cartella</title><meta charset=\"UTF-8\">\r\n"
						+ "<title>Nuova cartella</title>\r\n"
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
			// della cartella "figlia"
			session.setAttribute("idSopracartella", idSopracartella);

			final String JDBC_URL = "jdbc:mysql://localhost:3306/tiw_project?serverTimezone=UTC";
			final String JDBC_USER = "root";
			final String JDBC_PASSWORD = "iononsonotu";
			// Interrogo il database per ottenere il nome della cartella in questione
			Connection connection = null;
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;

			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}

			String sql = "SELECT nome FROM cartella WHERE id = ?";
			try {
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setInt(1, idSopracartella);

				// riceviamo il risultato della query SQL
				resultSet = preparedStatement.executeQuery();
				if (resultSet.next()) {
					folderName = resultSet.getString("nome");
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			out.println("Hai selezionato la cartella: " + folderName);
		}

		// Form per inserire il nome della cartella da creare
		out.println("<h1>Compila il seguente campo: </h1>" + "<FORM action = \"NewSubfolderServlet\"\r\n"
				+ "method = \"post\" >" + "<P>\r\n" + "<b>Nome cartella:</b>\r\n" + "    <br><br>\r\n"
				+ "    <INPUT type=\"text\"  name = \"nome\" required>\r\n" + "  </P>\r\n");

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
		String nome = request.getParameter("nome"); // nome cartella creata
		Integer idSopracartella = (Integer) session.getAttribute("idSopracartella");

		// attributi
		if (session != null) {
			user = session.getAttribute("email").toString();
		}

		if (idSopracartella != null) {
			createFolderIntoDB(user, nome, Date.valueOf(LocalDate.now()), idSopracartella);
			session.setAttribute("idSopracartella", null); // metto a null il valore di idSopracartella, per far
															// apparire il messaggio
															// di errore in caso di future creazioni di cartella
			response.sendRedirect("HomeServlet");
		} else {
			String errorMessage = "Nessuna cartella di destinazione selezionata!";
			response.sendRedirect("NewSubfolderServlet?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));

			return;

		}

	}

	// Metodo per creare cartelle
	private void createFolderIntoDB(String proprietario, String nome, Date data_creazione, Integer sopracartella) {
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
			preparedStatement.setInt(4, sopracartella);

			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
