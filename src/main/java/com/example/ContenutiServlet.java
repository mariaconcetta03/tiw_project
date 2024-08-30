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

@WebServlet("/ContenutiServlet")
public class ContenutiServlet extends HttpServlet {

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
	

	// Metodo per recuperare tutte le cartelle in una determinata cartella del database
	private List<Folder> getFoldersFromDB(String user, Integer cartella) {
		List<Folder> foundFolders = new ArrayList<>();

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

		// prepariamo la query SQL per estrarre le CARTELLE
		// prepared statements per evitare SQL-Injection
		String sql = "SELECT * FROM cartella WHERE sopracartella = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, cartella);
			

			// riceviamo il risultato della query SQL
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				Integer id = resultSet.getInt("id");
				String proprietario = resultSet.getString("proprietario");
				String nome = resultSet.getString("nome");
				Date data_creazione = resultSet.getDate("data_creazione");
				Integer sopracartella = resultSet.getInt("sopracartella");
				Folder folderToAdd = new Folder(id, proprietario, nome, data_creazione, sopracartella); 
																										
				folderToAdd.sottocartelle = null; 
				foundFolders.add(folderToAdd);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return foundFolders;
	}
	
	
	
	
	// Metodo per recuperare tutti i documenti in una determinata cartella del database
	private List<File> getDocsFromDB(String user, Integer folder) {
		List<File> foundDocs = new ArrayList<>();

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

		// prepariamo la query SQL per estrarre i DOCUMENTI
		// prepared statements per evitare SQL-Injection
		String sql = "SELECT * FROM documento WHERE cartella = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, folder);
			

			// riceviamo il risultato della query SQL
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				Integer id = resultSet.getInt("id");
				String proprietario = resultSet.getString("proprietario");
				String nome = resultSet.getString("nome");
				Date data_creazione = resultSet.getDate("data_creazione");
				String sommario = resultSet.getString("sommario");
				String tipo = resultSet.getString("tipo");
				Integer cartella = resultSet.getInt("cartella");
				File docsToAdd = new File(id, proprietario, nome, data_creazione, sommario, tipo, cartella); 																									
				foundDocs.add(docsToAdd);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return foundDocs;
	}
	
	
	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession(false); // false -> check se sessione esiste oppure no (nel caso in cui non esista restituisce null)
		String user = null;
		String folderToken = request.getParameter("folderToken");
        Map<String, Integer> folderTokens = (Map<String, Integer>) session.getAttribute("folderTokens");
        Map<String, Integer> fileTokens = new HashMap<>();

        if (session != null) {
        	user = session.getAttribute("email").toString();
        }
        Integer folderId = 0;
        String folderName  = null;
        List<Folder> folders = null;
        List<File> files = null;
        
        if (folderTokens != null && folderTokens.containsKey(folderToken)) { // se il token corrisponde ad uno effettivamente esistente
        	folderId = folderTokens.get(folderToken); // ID della cartella
        	 folders = getFoldersFromDB(user, folderId);
             files = getDocsFromDB(user, folderId);
        } 
        
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
			preparedStatement.setInt(1,folderId);
			
			// riceviamo il risultato della query SQL
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
		        folderName = resultSet.getString("nome");
		    }
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
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
		out.println("<h1> Contenuti della cartella: "+ folderName +"</h1>");
		out.println("<div class=\"tree\">");
		out.println("<ul>");

		// metto tutte le cartelle e tutti i file trovati
		for(Folder f: folders) {
			out.println("<li class=\"folder\">"+ f.nome +"</li>");
		}
		
		for(File f: files) {
		    String token = UUID.randomUUID().toString(); // Un token casuale o identificatore offuscato

		    out.println("<li class=\"file\">" + f.nome 
		    + " <a href=\"AccediServlet?fileToken=" + token + "\">   Accedi</a>" 
		    + " <a href=\"SpostaServlet?fileToken=" + token + "\">   Sposta</a>"
		    + "</li>");		
		    fileTokens.put(token, f.id);
		}
		session.setAttribute("fileTokens", fileTokens);
		

		out.println("</ul>");
		out.println("</div>");
		out.println("</body></html>");
	}
}