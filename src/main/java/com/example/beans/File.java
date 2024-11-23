package com.example.beans;
import java.sql.Date;


// Classe per rappresentare un file
public class File {
		private Integer id;
		private String proprietario;
		private String nome;
		private Date data_creazione;
		private String sommario;
		private String tipo;
		private Integer cartella;

	
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
		
		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getProprietario() {
			return proprietario;
		}

		public void setProprietario(String proprietario) {
			this.proprietario = proprietario;
		}

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public Date getData_creazione() {
			return data_creazione;
		}

		public void setData_creazione(Date data_creazione) {
			this.data_creazione = data_creazione;
		}

		public String getSommario() {
			return sommario;
		}

		public void setSommario(String sommario) {
			this.sommario = sommario;
		}

		public String getTipo() {
			return tipo;
		}

		public void setTipo(String tipo) {
			this.tipo = tipo;
		}

		public Integer getCartella() {
			return cartella;
		}

		public void setCartella(Integer cartella) {
			this.cartella = cartella;
		}

		
}
	

