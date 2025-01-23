package com.example.servlets;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.DAOs.*;

@WebServlet("/SpostaServlet")
public class SpostaServlet extends HttpServlet {
	
	DocumentoDao documentoDao = null;
	
	// questa funzione viene eseguita solo una volta quando la servlet
	// viene caricata in memoria
	@Override
	public void init(){
		documentoDao = new DocumentoDao();
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		String user = null;
		
		String fileToMoveToken = request.getParameter("fileToken");
		session.setAttribute("originServlet", "SpostaServlet");

		response.sendRedirect("HomeServlet?fileToken=" + fileToMoveToken);	
	}
	
	
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		String user = null;
		String folderToken = request.getParameter("folderToken"); // nome del folder in cui ci spostiamo
		String fileToken = request.getParameter("fileToken"); // nome del file spostato

		Map<String, Integer> folderTokens = null;
		Map<String, Integer> fileTokens = null;

		if (session != null) {
			user = session.getAttribute("email").toString();
			folderTokens = (Map<String,Integer>)session.getAttribute("folderTokens");
			fileTokens = (Map<String,Integer>)session.getAttribute("fileTokens");
		}
		
		Integer fileID = fileTokens.get(fileToken);
		Integer newFolderID = folderTokens.get(folderToken);
		
		
		documentoDao.updateFilePosition(user, newFolderID, fileID);
		response.sendRedirect("ContenutiServlet?folderToken=" + folderToken);
	}
	
}
	