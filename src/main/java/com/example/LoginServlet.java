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

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

	//private boolean wrongParam = true;
	//private boolean connectionError = false;
	
	private List<Integer> checkParameters(String email, String pw) {
		final String JDBC_URL = "jdbc:mysql://localhost:3306/tiw_project?serverTimezone=UTC";
		final String JDBC_USER = "root";
		final String JDBC_PASSWORD = "iononsonotu";
			
		List<Integer> value = new ArrayList<>(); // 1 = connectionError    2 = wrongParam
		// inizializzazione delle variabili necessarie per la query
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		// connessione al server SQL
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
		} catch (ClassNotFoundException | SQLException e) {
		//	connectionError = true;
			value.add(1); 
			e.printStackTrace();
		}
		
		if (value.isEmpty()){ // se non è avvenuto l'errore di connessione
			value.add(0);
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
				// wrongParam = false;
				value.add(0);
			} else {
				// wrongParam = true;
				value.add(1);
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
		return value;
	}



	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
				
		HttpSession session = request.getSession(false); // false = se non esiste una sessione, allora non la creo
	    if (session != null) {
	        session.invalidate(); // invalido una possibile sessione precedente
	    }

		// getting the parameters written by the user
		String email = request.getParameter("email");
		String password = request.getParameter("password");

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	
		String errorMessage;
		
			List<Integer> value = checkParameters(email, password);

		if(value.get(0).equals(1)) { // connectionError = 1
			errorMessage = "C'è stato un errore durante la comunicazione con il server SQL";
			response.sendRedirect("login.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
			return;
		}
		
		if (value.get(1).equals(0)) { // wrongParam = 0
			session = request.getSession();
			session.setAttribute("email", email); // Salviamo l'email nella sessione, perchè è quella del PROPRIETARIO delle cartelle
			// Se non ci sono errori, procediamo in home page
			response.sendRedirect("http://localhost:8080/tiw_project/HomeServlet");
		} else { // wrongParam = 1
			// Reindirizza di nuovo alla pagina HTML con il messaggio di errore nella query string
			errorMessage = "E-mail o password errate. Riprova.";
			response.sendRedirect("login.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
		}

	}

}