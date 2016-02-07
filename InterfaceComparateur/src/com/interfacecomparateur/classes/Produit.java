package com.interfacecomparateur.classes;

import java.io.Serializable;

/**
 * Classe permettant de modéliser l'ensemble des informations utiles sur un produit
 * 
 * @author EquipeL3I1
 *
 */
@SuppressWarnings("serial")
public class Produit implements Serializable {
    /**
     * Nom du produit
     */
	private String nom;
	/**
	 * Code barre du produit
	 */
	private String ean;
	/**
	 * Nom fabricant du produit
	 */
	private String nomFabricant;
	private String dirImage;
	/**
	 * Description d'un produit
	 */
	private String descriptif;
	/**
	 * Quantite du produit
	 */
	private int quantite;

	public Produit(String nom, String ean, String nomFabricant, String dirImage, String descriptif, int quantite) {	
	   this.nom=nom;
	   this.ean=ean;
	   this.nomFabricant=nomFabricant;
	   this.dirImage=dirImage;
	   this.descriptif=descriptif;
	   this.quantite=quantite;
	}

	public Produit(String nom, String ean, String nomFabricant, String dirImage, String descriptif) {	
	   this(nom,ean,nomFabricant,dirImage,descriptif,0);
	}

	public Produit(String nom, String ean, String nomFabricant, String dirImage) {	
	   this(nom,ean,nomFabricant,dirImage,"",0);
	}

	public Produit(String nom, int quantite) {	
	   this(nom,"","","","",quantite);
	}

	public Produit(String nom) {	
	   this(nom,"","","","",1);
	}

	public Produit(){
	   this("","","","","",1);
	}

	public String toString() {
	   return nom;
	}

	public String getNom() {
	   return nom;
	}

	public String getEan() {
	   return ean;
	}

	public String getNomFabricant() {
	   return nomFabricant;
	}

	public String getDirImage() {
	   return dirImage;
	}

	public String getDescriptif() {
	   return descriptif;
	}

	public int getQuantite() {
	   return quantite;
	}
	
	public void setNom(String nom){
	   this.nom = nom;
	}
	
	public void setEan(String ean){
	   this.ean = ean;
	}
	
	public void setNomFabricant(String nomFabricant) {
	   this.nomFabricant = nomFabricant;
	}

	public void setDirImage(String dirImage) {
	   this.dirImage = dirImage;
	}

	public void setDescriptif(String descriptif) {
	   this.descriptif = descriptif;
	}

	public void setQuantite(int quantite) {
	   this.quantite = quantite;
	}

	public void incrementeQuantite() {
	   this.quantite++;
	}

}
