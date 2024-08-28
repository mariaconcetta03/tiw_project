package com.example;

import java.io.IOException;
import java.lang.*;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/RegistrationServlet")
public class RegistrationServlet extends HttpServlet {

	// private boolean connectionError = false;

	private List<Integer> insertUser(String user, String pass, String em) {
		final String JDBC_URL = "jdbc:mysql://localhost:3306/tiw_project?serverTimezone=UTC";
		final String JDBC_USER = "root";
		final String JDBC_PASSWORD = "iononsonotu";
		List<Integer> value = new ArrayList<>();
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

			String insertSQL = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
			preparedStatement = connection.prepareStatement(insertSQL);
			preparedStatement.setString(1, user);
			preparedStatement.setString(2, pass);
			preparedStatement.setString(3, em);

			preparedStatement.executeUpdate();
			value.add(1); // unique
			value.add(0); // connectionError

		} catch (ClassNotFoundException e) {
			System.err.println("Driver JDBC non trovato: " + e.getMessage());
			e.printStackTrace();
			value.add(0); // unique
			value.add(1); // connectionerror
		} catch (SQLException e) {
			if (e.getSQLState().equals("23000")) { // Codice di errore SQL per violazione di vincolo
				System.out.println("Username o e-mail già esistente.");
				value.add(0); // unique
				value.add(0); // connectionError
			} else {
				e.printStackTrace();
				value.add(0); // unique
				value.add(1); // connectionerror

				// connectionError=true;
				// return false;
			}
		} finally {
			try {
				if (preparedStatement != null)
					preparedStatement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return value;

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// getting the parameters written by the user
		String username = request.getParameter("username");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String confirmPassword = request.getParameter("password_conf");
		/// boolean unique = true;

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		String errorMessage;

		// username ed e-mail sono unici o no?
		List<Integer> value = insertUser(username, password, email);
		boolean unique, connectionError;

		if (value.get(0) == 0) {
			unique = false;
		} else {
			unique = true;
		}

		if (value.get(1) == 0) {
			connectionError = false;
		} else {
			connectionError = true;

		}

		// caso di errore nella comunicazione col server
		if (!unique && connectionError) {
			errorMessage = "C'è stato un errore durante la comunicazione con il server SQL";
			response.sendRedirect("registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
			return;
		}

		if (username.length() > 50) {
			errorMessage = "L'username non può superare la lunghezza di 50 caratteri. Riprova.";
			response.sendRedirect("registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
		} else {
			if (!unique) {
				errorMessage = "Lo username o l'e-mail sono già in uso. Riprova";
				response.sendRedirect("registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
			} else {
				// if confirmation password is different from the password
				if (!password.equals(confirmPassword)) {
					errorMessage = "Le due password non coincidono. Riprova";
					// Reindirizza di nuovo alla pagina HTML con il messaggio di errore nella query
					// string
					response.sendRedirect(
							"registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
				} else {
					String regex = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!£-]).{8,50}";
					if (!password.matches(regex)) {
						// Reindirizza di nuovo alla pagina HTML con il messaggio di errore nella query
						// string
						errorMessage = "La password non soddisfa i requisiti richiesti. Riprova.";
						response.sendRedirect(
								"registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
					} else {
						HttpSession session = request.getSession();
						session.setAttribute("email", email); // Salviamo l'email nella sessione, perchè è quella del
																// PROPRIETARIO delle cartelle'
						// Se non ci sono errori, procediamo in home page
						response.sendRedirect("http://localhost:8080/tiw_project/HomeServlet");
					}
				}
			}
		}
	}
}