package com.interfacecomparateur.classes;

import java.sql.Timestamp;

/**
 * Classe permettant de modéliser l'ensemble des informations utiles sur un produit dans un magasin.
 * 
 * @author EquipeL3I1
 *
 */
public class ProduitDansMagasin {
	/**
	 * Nom du magasin contenant le produit
	 */
	private String nomMagasin;
	/**
	 * Code barre du produit
	 */
	private String produitEan;
	/**
	 * Prix du produit
	 */
	private double prixProduit;
	/**
	 * 
	 */
	private boolean promotion;
	
	private Timestamp insertDate;
	
	public ProduitDansMagasin(String nomMagasin, String produitEan, double prixProduit, boolean promotion, Timestamp insertDate){
		this.nomMagasin = nomMagasin;
		this.produitEan = produitEan;
		this.prixProduit = prixProduit;
		this.promotion = promotion;
		this.insertDate = insertDate;
	}
	
	public ProduitDansMagasin(){
		this("", "", 0.0, false, null);
	}
	
	public String getNomMagasin(){
		return nomMagasin;
	}
	
	public String getProduitEan(){
		return produitEan;
	}
	
	public double getPrixProduit(){
		return prixProduit;
	}
	
	public boolean isPromotion(){
		return promotion;
	}
	
	public Timestamp getInsertDate(){
		return insertDate;
	}
	
	public void setNomMagasin(String nomMagasin){
		this.nomMagasin = nomMagasin;
	}
	
	public void setProduitEan(String produitEan){
		this.produitEan = produitEan;
	}
	
	public void setPrixProduit(double prixProduit){
		this.prixProduit = prixProduit;
	}
	
	public void setPromotion(boolean promotion){
		this.promotion = promotion;
	}
	
	public void setInsertDate(Timestamp insertDate){
		this.insertDate = insertDate;
	}

}
