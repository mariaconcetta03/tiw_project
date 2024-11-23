package com.example.DAOs;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.example.beans.*;

public class CartellaDao {

	Connection connection = null; // this is the actual connection to the DB

	// this method connects to the DB
	private void getConnection() {

		final String JDBC_URL = "jdbc:mysql://localhost:3306/tiw_project?serverTimezone=UTC";
		final String JDBC_USER = "root";
		final String JDBC_PASSWORD = "iononsonotu";

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	// this method closes the connection to the DB
	private void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			System.out.println("Impossibile chiudere la connessione col DB");
		}
	}


	public String getNomeCartellaById(Integer idCartella) {
		String nomeCartella = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		getConnection();
		
		String sql1 = "SELECT nome FROM cartella WHERE id = ?";
		try {
			preparedStatement = connection.prepareStatement(sql1);
			preparedStatement.setInt(1, idCartella);

			// riceviamo il risultato della query SQL
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				nomeCartella = resultSet.getString("nome");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Chiudere risorse
			try {
				if (resultSet != null)
					resultSet.close();
				if (preparedStatement != null)
					preparedStatement.close();
				closeConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return nomeCartella;
	}
	
	
	
	// Metodo per recuperare tutte le cartelle in una determinata cartella del database
	public List<Folder> getSubfoldersFromDB(String user, Integer cartella) {
		List<Folder> foundFolders = new ArrayList<>();

		getConnection();
		// inizializzazione delle variabili necessarie per la query
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		// prepared statements per evitare SQL-Injection
		String sql = "SELECT * FROM cartella WHERE sopracartella = ?";
		
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, cartella);
			

			// riceviamo il risultato della query SQL
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				Integer id = resultSet.getInt("id");
				String proprietario = resultSet.getString("proprietario");
				String nome = resultSet.getString("nome");
				Date data_creazione = resultSet.getDate("data_creazione");
				Integer sopracartella = resultSet.getInt("sopracartella");
				Folder folderToAdd = new Folder(id, proprietario, nome, data_creazione, sopracartella); 
				
				folderToAdd.setSottocartelle(getSubfoldersFromDB (user, id)); 
				foundFolders.add(folderToAdd);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			// Chiudere risorse
			try {
				if (resultSet != null)
					resultSet.close();
				if (preparedStatement != null)
					preparedStatement.close();
				closeConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return foundFolders;
	}
	
	
	
	
	
	// Metodo per recuperare tutte le cartelle dal database
		public List<Folder> getAllUserFolder (String user) {
            getConnection();
            
			List<Folder> allFolders = new ArrayList<>();

			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			
			// prepariamo la query SQL
			// prepared statements per evitare SQL-Injection
			String sql = "SELECT * FROM cartella WHERE sopracartella is NULL and proprietario = ?";
			try {
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, user);
				// prendiamo in
				// considerazione le cartelle più esterne
				// (le quali possono avere sottocartelle)

				// riceviamo il risultato della query SQL
				resultSet = preparedStatement.executeQuery();

				while (resultSet.next()) {
					Integer id = resultSet.getInt("id");
					String proprietario = resultSet.getString("proprietario");
					String nome = resultSet.getString("nome");
					Date data_creazione = resultSet.getDate("data_creazione");
					// se sopracartella è diverso da null, allora metto ID della sopracartella,
					// altrimenti metto NULL
					Integer sopracartella = resultSet.getObject("sopracartella") != null ? resultSet.getInt("sopracartella")
							: null;
					Folder folderToAdd = new Folder(id, proprietario, nome, data_creazione, sopracartella); // aggiungo la
																											// cartella più
																											// esterna
					folderToAdd.setSottocartelle(null); // default
					folderToAdd.setSottocartelle(getSubfoldersFromDB(user, folderToAdd.getId()));
					allFolders.add(folderToAdd);
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}finally {
				// Chiudere risorse
				try {
					if (resultSet != null)
						resultSet.close();
					if (preparedStatement != null)
						preparedStatement.close();
					closeConnection();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			return allFolders;
		}



	  	// Metodo per creare cartelle
		public void createSubfolderIntoDB(String proprietario, String nome, Date data_creazione, Integer sopracartella) {
			getConnection();
		
			// inizializzazione delle variabili necessarie per la query
			PreparedStatement preparedStatement = null;

			// prepared statements per evitare SQL-Injection
			String sql = "INSERT INTO cartella (proprietario, nome, data_creazione, sopracartella) values (?,?,?,?)";
			try {
			
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, proprietario);
				preparedStatement.setString(2, nome);
				preparedStatement.setDate(3, data_creazione);
				preparedStatement.setInt(4, sopracartella);
				
				preparedStatement.executeUpdate();


			} catch (SQLException e) {
				e.printStackTrace();
			}finally {
				// Chiudere risorse
				try {
					if (preparedStatement != null)
						preparedStatement.close();
					closeConnection();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		
		
			// Metodo per creare cartelle
		public void createRootFolderIntoDB(String proprietario, String nome, Date data_creazione) {
			getConnection();
		
			// inizializzazione delle variabili necessarie per la query
			PreparedStatement preparedStatement = null;

			// prepared statements per evitare SQL-Injection
			String sql = "INSERT INTO cartella (proprietario, nome, data_creazione, sopracartella) values (?,?,?,?)";
			try {
			
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, proprietario);
				preparedStatement.setString(2, nome);
				preparedStatement.setDate(3, data_creazione);
				preparedStatement.setNull(4, java.sql.Types.INTEGER);
				
				preparedStatement.executeUpdate();


			} catch (SQLException e) {
				e.printStackTrace();
			}finally {
				// Chiudere risorse
				try {
					if (preparedStatement != null)
						preparedStatement.close();
					closeConnection();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}





}
