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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

	private boolean wrongParam = true;
	private boolean connectionError = false;
	
	private void checkParameters(String email, String pw) {
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
			connectionError = true;
			e.printStackTrace();
		}

		// prepariamo la query SQL
		// prepared statements per evitare SQL-Injection
		String sql = "SELECT * FROM user WHERE email = ? AND password = ?";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, email);
			preparedStatement.setString(2, pw);

			// riceviamo il risultato della query SQL
			resultSet = preparedStatement.executeQuery();

			// controlliamo che nel risultato della query SQL esista una riga. Infatti
			// sarà solamente una riga corrispondente ad una tupla "e-mail - password".
			// se il risultato è nullo (nessuna riga) significa che email o password sono
			// incorretti
			if (resultSet.next()) {
				wrongParam = false;
			} else {
				wrongParam = true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
            // Chiudere risorse
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
	}



	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// getting the parameters written by the user
		String email = request.getParameter("email");
		String password = request.getParameter("password");

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		String errorMessage;
		
		checkParameters(email, password);

		if(connectionError) {
			errorMessage = "C'è stato un errore durante la comunicazione con il server SQL";
			response.sendRedirect("login.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
			return;
		}
		
		if (!wrongParam) {
			// Se non ci sono errori, procediamo in home page
			response.sendRedirect("home_page.html");
		} else {
			// Reindirizza di nuovo alla pagina HTML con il messaggio di errore nella query
			// string
			errorMessage = "E-mail o password errate. Riprova.";
			response.sendRedirect("login.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
		}

	}

}