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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	private List<Folder> getFoldersFromDB() {
		final String JDBC_URL = "jdbc:mysql://localhost:3306/tiw_project?serverTimezone=UTC";
		final String JDBC_USER = "root";
		final String JDBC_PASSWORD = "iononsonotu";
		List<Folder> allFolders = new ArrayList<>();

		try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
				PreparedStatement statement = connection
						.prepareStatement("SELECT id, proprietario, nome, data_creazione, sopracartella FROM cartella");
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {
				Integer id = resultSet.getInt("id");
				String proprietario = resultSet.getString("proprietario");
				String nome = resultSet.getString("nome");
				Date data_creazione = resultSet.getDate("data");
				// se sopracartella è diverso da null, allora metto ID della sopracartella,
				// altrimenti metto NULL
				Integer sopracartella = resultSet.getObject("sopracartella") != null ? resultSet.getInt("sopracartella")
						: null;
				Folder folderToAdd = new Folder(id, proprietario, nome, data_creazione, sopracartella);
				folderToAdd.sottocartelle = getSubfolders(folderToAdd.id);
				allFolders.add(folderToAdd);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return allFolders;
	}

	// Metodo per recuperare tutte le sottocartelle di una specifica cartella
	private List<Folder> getSubfolders(Integer idToSearch) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		final String JDBC_URL = "jdbc:mysql://localhost:3306/tiw_project?serverTimezone=UTC";
		final String JDBC_USER = "root";
		final String JDBC_PASSWORD = "iononsonotu";
		List<Folder> subFolders = new ArrayList<>();

		// connessione al server SQL
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		String sql = "SELECT * FROM cartella WHERE sopracartella = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, idToSearch);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				Integer id = resultSet.getInt("id");
				String proprietario = resultSet.getString("proprietario");
				String nome = resultSet.getString("nome");
				Date data_creazione = resultSet.getDate("data");
				// se sopracartella è diverso da null, allora metto ID della sopracartella,
				// altrimenti metto NULL
				Integer sopracartella = resultSet.getObject("sopracartella") != null ? resultSet.getInt("sopracartella")
						: null;
				Folder folderToAdd = new Folder(id, proprietario, nome, data_creazione, sopracartella);
				subFolders.add(folderToAdd);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return subFolders;
	}
 


///TODO da rivedere generatehtmlfolders 
	// Metodo per generare il codice HTML ricorsivamente
	// Alla prima chiamata io passo la lista di TUTTI i folders. 
	//Inizia a mettere il primo folder
	private void generateHtmlForFolder(PrintWriter out, List<Folder> folders) {
		List<Folder> allFolders = folders;
		
		for(Folder f: folders) {
			out.println("<li class=\"folder\"> <a href=\"contenuti.html\">" + f.nome + "</a>"); // metto il nodo della lista (cartella esterna)
			if (!f.sottocartelle.isEmpty()) { // se ci sono delle sottocartelle
               	out.println("<ul>"); // inizio il nodo delle sottocartelle
               	for(Folder f1:allFolders){ // cerco la sottocartella in tutte le cartelle (li ho le info salvate)
                       if (f1.id.equals(f.id)) {
                    	   break;
                       }
                       List<Folder> f1sub = new ArrayList<>();
                       f1sub.add(f1);
                       generateHtmlForFolder(out, f1sub); // genero il codice di quella sottocartella
                   }
                   			out.println("</ul>"); // finito il nodo delle sottocartelle

            }
            		out.println("</li>"); // chiudo il nodo della lista esterna

		}
	}
		o



	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Connessione al database e recupero delle cartelle
		List<Folder> folders = getFoldersFromDatabase();

		// Creazione della mappa delle cartelle organizzate per ID
		Map<Integer, Folder> folderMap = new HashMap<>();
		List<Folder> rootFolders = new ArrayList<>();

		for (Folder folder : folders) {
			folderMap.put(folder.id, folder);
			if (folder.sopracartella == null) {
				rootFolders.add(folder);
			} else {
				Folder parentFolder = folderMap.get(folder.parentId);
				if (parentFolder != null) {
					parentFolder.children.add(folder);
				}
			}
		}

		// Impostazione della risposta HTML
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		out.println("<html><head><title>Folder Structure</title></head><body>");
		out.println("<h1>Folder Structure</h1>");
		out.println("<ul>");

		// Generazione ricorsiva del codice HTML
		for (Folder folder : rootFolders) {
			generateHtmlForFolder(out, folder);
		}

		out.println("</ul>");
		out.println("</body></html>");
	}
