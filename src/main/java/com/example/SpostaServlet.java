package com.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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

import com.example.HomeServlet.Folder;

@WebServlet("/SpostaServlet")
public class SpostaServlet extends HttpServlet {
	
	
	//TODO: bisogna fixare il fatto che se seleziono una cartella esterna poi non mi fa spostare il file in una delle sottocartelle 
	//(anche le sottocartelle devono essere selezionabili!!!)
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(); // false -> check se sessione esiste oppure no (nel caso in cui non esista restituisce null)
		String user = null;
		
		String fileToMoveToken = request.getParameter("fileToken");
		session.setAttribute("originServlet", "SpostaServlet");

		response.sendRedirect("HomeServlet?fileToken=" + fileToMoveToken);	
	}
	
	
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(); // Recupero dei dati dal form
		String user = null;
		String folderToken = request.getParameter("folderToken"); // nome del folder in cui ci spostiamo
		String fileToken = request.getParameter("fileToken"); // nome del file spostato

		Map<String, Integer> folderTokens = (Map<String, Integer>) session.getAttribute("folderTokens");
		Map<String, Integer> fileTokens = (Map<String, Integer>) session.getAttribute("fileTokens");

		Integer fileID = fileTokens.get(fileToken);
		Integer newFolderID = folderTokens.get(folderToken);

		
		// QUERY SQL PER MODIFICARE IL PARAMETRO CARTELLA NELLA TABELLA "FILE"
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
		String sql = "UPDATE documento SET cartella = ? WHERE id = ? ";
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, newFolderID);
			preparedStatement.setInt(2, fileID);

			// riceviamo il risultato della query SQL
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		response.sendRedirect("ContenutiServlet?folderToken=" + folderToken);

	}
	
}
	