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


@WebServlet("/RegistrationServlet")
public class RegistrationServlet extends HttpServlet {
	
     @SuppressWarnings("finally")
	private boolean insertUser(String user, String pass, String em)  {
    	 final String JDBC_URL = "jdbc:mysql://localhost:3306/User's_info";
    	 final String JDBC_USER = "username";
    	 final String JDBC_PASSWORD = "password";
    	 final String JDBC_EMAIL = "email";

    	 Connection connection = null;
         PreparedStatement preparedStatement = null;

         try {
             Class.forName("com.mysql.cj.jdbc.Driver");
             connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_EMAIL);

             String insertSQL = "INSERT INTO User's_info (username, password, email) VALUES (?, ?, ?)";
             preparedStatement = connection.prepareStatement(insertSQL);
             preparedStatement.setString(1, user);
             preparedStatement.setString(2, pass);
             preparedStatement.setString(3, em);

             preparedStatement.executeUpdate();
          
         } catch (ClassNotFoundException e) {
             System.err.println("Driver JDBC non trovato: " + e.getMessage());
       
         } catch (SQLException e) {
        	 if (e.getSQLState().equals("23000")) { // Codice di errore SQL per violazione di vincolo
                 System.out.println("Username già esistente.");
             } else {
                 e.printStackTrace();
             }
		} finally {
             try {
                 if (preparedStatement != null) preparedStatement.close();
                 if (connection != null) connection.close();
             } catch (SQLException e) {
                 e.printStackTrace();
                 return false;
             }
             return true;
         }
     
    	
     }
	
	
	
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // getting the parameters written by the user
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("password_conf");
        boolean unique = true;
       
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
		//TODO fare controllo dei requisiti della pw
        //rquisiti email
        //username dev'essere unico 
        
        String errorMessage;
      
        unique = insertUser(username, password, email);
        
        if(username.length() > 50){
        	errorMessage = "L'username non può superare la lunghezza di 50 caratteri. Riprova.";
            response.sendRedirect("registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
        } else {
	        if (!unique) {
	            errorMessage = "Lo username o l'e-mail sono già in uso. Riprova";
	            response.sendRedirect("registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
	        } else {   
	        	// if confirmation password is different from the password
	        	if(!password.equals(confirmPassword)) {
	        	  errorMessage = "Le due password non coincidono. Riprova";
	            // Reindirizza di nuovo alla pagina HTML con il messaggio di errore nella query string
	            response.sendRedirect("registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
		        } else {
		        	String regex = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!£-]).{8,50}";
		        	if(!password.matches(regex)) {
		        		 // Reindirizza di nuovo alla pagina HTML con il messaggio di errore nella query string
		        		 errorMessage = "La password non soddisfa i requisiti richiesti. Riprova.";
		            response.sendRedirect("registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
		        	} else {
		            // Se non ci sono errori, procediamo in home page
		            response.sendRedirect("home_page.html");
		        	}
		        }
	        }
        }
    }
}