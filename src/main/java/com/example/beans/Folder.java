package com.example.beans;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

//Classe per rappresentare una cartella 
public class Folder {
	private Integer id;
	private String proprietario;
	private String nome;
	private Date data_creazione;
	private Integer sopracartella;
	private List<Folder> sottocartelle = new ArrayList<>();

	
	public Folder(Integer id, String proprietario, String nome, Date data_creazione, Integer sopracartella) {
		this.id = id;
		this.proprietario = proprietario; // mail
		this.nome = nome;
		this.data_creazione = data_creazione;
		this.sopracartella = sopracartella;
		this.sottocartelle = null;
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

	public Integer getSopracartella() {
		return sopracartella;
	}

	public void setSopracartella(Integer sopracartella) {
		this.sopracartella = sopracartella;
	}

	public List<Folder> getSottocartelle() {
		return sottocartelle;
	}

	public void setSottocartelle(List<Folder> sottocartelle) {
		this.sottocartelle = sottocartelle;
	}

	
}