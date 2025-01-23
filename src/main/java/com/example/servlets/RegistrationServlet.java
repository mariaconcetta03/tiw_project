package com.example.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.DAOs.*;

@WebServlet("/RegistrationServlet")
public class RegistrationServlet extends HttpServlet {

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
	
		// prendiamo i parametri inseriti dall'utente
		String username = request.getParameter("username");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String confirmPassword = request.getParameter("password_conf");

		response.setContentType("text/html");
		// così si scrive direttamente nella risposta HTTP che verrà inviata al client

		String errorMessage;
		
		if (!password.equals(confirmPassword)) { // se non coincidono
			errorMessage = "Le due password non coincidono. Riprova";
			// Reindirizza di nuovo alla pagina HTML con il messaggio di errore nella querystring
			response.sendRedirect(
					"registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
			return; // non provo a mettere nulla nel database
		}
		
		List<Integer> value = userDao.insertUser(username, password, email);
		boolean unique, connectionError; //se username/mail unico, value.get(0)==1 altimenti no lo è, lo stesso per la connessione (se c'è stato un errore o meno)

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

		if (username.length() > 50) { // username troppo lungo
			errorMessage = "L'username non può superare la lunghezza di 50 caratteri. Riprova.";
			response.sendRedirect("registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
		} else {
			if (!unique) { // username o mail già in uso
				errorMessage = "Lo username o l'e-mail sono già in uso. Riprova";
				response.sendRedirect("registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
			} else {
				// se le 2 password NON coincidono
				if (!password.equals(confirmPassword)) {
					errorMessage = "Le due password non coincidono. Riprova";
					// Reindirizza di nuovo alla pagina HTML con il messaggio di errore nella querystring
					response.sendRedirect(
							"registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
				} else {
					String regex = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!£-]).{8,50}";
					if (!password.matches(regex)) {
						// Reindirizza di nuovo alla pagina HTML con il messaggio di errore nella query
						// string
						errorMessage = "La password non soddisfa i requisiti richiesti. Riprova.";
						response.sendRedirect("registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
					} else {
						HttpSession session = request.getSession();
						session.setAttribute("email", email); // Salviamo l'email nella sessione, perchè è quella del PROPRIETARIO delle cartelle
						// Se non ci sono errori, procediamo in home page
						response.sendRedirect("http://localhost:8080/tiw_project/HomeServlet");
					}
				}
			}
		}
	}
}