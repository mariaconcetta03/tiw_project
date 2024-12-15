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

	// COSTRUTTORE
	public File(Integer id, String proprietario, String nome, Date data_creazione, String sommario, String tipo,
			Integer cartella) {
		this.id = id;
		this.proprietario = proprietario; // mail
		this.nome = nome;
		this.data_creazione = data_creazione;
		this.sommario = sommario;
		this.tipo = tipo;
		this.cartella = cartella; // id
	}

	
	
	// METODI GETTER
	public Integer getId() {
		return id;
	}

	public String getProprietario() {
		return proprietario;
	}

	public String getNome() {
		return nome;
	}

	public Date getData_creazione() {
		return data_creazione;
	}

	public String getSommario() {
		return sommario;
	}

	public Integer getCartella() {
		return cartella;
	}

	public String getTipo() {
		return tipo;
	}
	
	
	
	// METODI SETTER
	public void setId(Integer id) {
		this.id = id;
	}

	public void setProprietario(String proprietario) {
		this.proprietario = proprietario;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public void setData_creazione(Date data_creazione) {
		this.data_creazione = data_creazione;
	}

	public void setSommario(String sommario) {
		this.sommario = sommario;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public void setCartella(Integer cartella) {
		this.cartella = cartella;
	}

}
