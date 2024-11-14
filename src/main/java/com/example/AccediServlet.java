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

@WebServlet("/AccediServlet")
public class AccediServlet extends HttpServlet {

	// Classe per rappresentare un file
	class File {
		Integer id;
		String proprietario;
		String nome;
		Date data_creazione;
		String sommario;
		String tipo;
		Integer cartella;

		public File(Integer id, String proprietario, String nome, Date data_creazione, String sommario, String tipo,
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

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false); // false -> check se sessione esiste oppure no (nel caso in cui
															// non esista restituisce null)
		String user = null;
		Map<String, Integer> fileTokens = null;
		File f = null;
		String nomeCartella = null;

		if (session != null) {
			user = session.getAttribute("email").toString();
			fileTokens = (Map<String, Integer>) session.getAttribute("fileTokens");
		}

		// prendo dall'URL il token del file selezionato
		String fileToken = request.getParameter("fileToken");

		Integer fileId = 0;
		if (fileTokens != null && fileTokens.containsKey(fileToken)) { // se il token corrisponde ad uno effettivamente
																		// esistente
			fileId = fileTokens.get(fileToken); // ID della cartella
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

		String sql = "SELECT * FROM documento WHERE id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, fileId);

			// riceviamo il risultato della query SQL
			resultSet = preparedStatement.executeQuery();
			Integer id;
			String proprietario;
			String nome;
			Date data_creazione;
			String sommario;
			String tipo;
			Integer cartella;
			if (resultSet.next()) {
				id = resultSet.getInt("id");
				proprietario = resultSet.getString("proprietario");
				nome = resultSet.getString("nome");
				data_creazione = resultSet.getDate("data_creazione");
				sommario = resultSet.getString("sommario");
				tipo = resultSet.getString("tipo");
				cartella = resultSet.getInt("cartella");
				f = new File(id, proprietario, nome, data_creazione, sommario, tipo, cartella);
			}

			String sql1 = "SELECT nome FROM cartella WHERE id = ?";
			try {
				preparedStatement = connection.prepareStatement(sql1);
				preparedStatement.setInt(1, f.cartella);

				// riceviamo il risultato della query SQL
				resultSet = preparedStatement.executeQuery();

				if (resultSet.next()) {
					nomeCartella = resultSet.getString("nome");
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			// Impostazione della risposta (pagina HTML)
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();

			out.println("<html lang=\"it\"><head>\r\n" + "<meta charset=\"UTF-8\">\r\n"
					+ "<title>Info documento</title>\r\n"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
					+ "</head></head><body>");
			
			
	        out.println("<a href=\"LogoutServlet\">Logout</a>"); // Link per fare il logout (rimando alla servlet di logout)
	        
	        
	        // Link per tornare alla pagina precedente
	        String referer = request.getHeader("Referer");
	        if (referer == null) {
	            referer = "login.html"; // rimando alla pagina di login utente, se il Referer non è disponibile
	        }
	        // nota bene: &nbsp = 1 SPAZIO BIANCO (separa "logout" e "torna alla pagina precedente")
	        out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp <a href='" + referer + "'>Torna alla pagina precedente</a>");
	        
	        
			out.println("<h1> Informazioni del documento selezionato: </h1>");
			out.println("<b>Nome documento:</b> " + f.nome + "<br>");
			out.println("<b>E-mail del proprietario:</b> " + f.proprietario + "<br>");
			out.println("<b>Data di creazione:</b> " + f.data_creazione + "<br>");
			out.println("<b>Sommario:</b> " + f.sommario + "<br>");
			out.println("<b>Tipo:</b> " + f.tipo + "<br>");
			out.println("<b>Cartella:</b> " + nomeCartella + "<br>");

			out.println("<div class=\"tree\">");
			out.println("<ul>");
			out.println("</ul>");
			out.println("</div>");
			out.println("</body></html>");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}