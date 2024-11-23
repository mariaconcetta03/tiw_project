package com.example.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.DAOs.UserDao;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	
	UserDao userDao = null;
	
	// questa funzione viene eseguita solo una volta quando la servlet
	// viene caricata in memoria
	@Override
	public void init(){
		userDao = new UserDao();
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
				
		HttpSession session = request.getSession(); // false = se non esiste una sessione, allora non la creo
	    if (session != null) {
	        session.invalidate(); // invalido una possibile sessione precedente
	    }

		// getting the parameters written by the user
		String email = request.getParameter("email");
		String password = request.getParameter("password");

		response.setContentType("text/html");
	
		String errorMessage;
		
			List<Integer> value = userDao.checkCredentials(email, password);

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