package com.data.backup.loan;


import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

@Entity
//@Table(name="loan")
public class Loan {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
	private Long id;
	private String collection = "";
	private String dbName = "";
	
	public Loan() {
		super();
	}
	
	public Loan(Long id, String collection, String dbName) {
		super();
		this.collection = collection;
		this.dbName = dbName;
		this.id = id;
	}
	
	public Long getid() {
		return id;
	}
	
	public void setid(Long id) {
		this.id = id;
	}

	public String getCollection() {
		return collection;
	}
	
	public void setCollection(String collection) {
		this.collection = collection;
	}
	
	public String getDbName() {
		return dbName;
	}
	
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
	
	
}
