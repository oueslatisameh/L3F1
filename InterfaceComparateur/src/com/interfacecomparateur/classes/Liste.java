package com.interfacecomparateur.classes;

import java.util.ArrayList;

/**
 * Classe permettant de modéliser une liste de courses
 * 
 * @see Produit
 * @author EquipeL3I1
 *
 */

import com.interfacecomparateur.classes.Produit;

public class Liste {
	/**
	 * Le nom de la liste de courses
	 */
	private String nom;
	/**
	 * Les produits de la liste
	 */
	private ArrayList<Produit> produits;
 
	public Liste(String nom) {
	   this.nom = nom ;
	   produits = new ArrayList<Produit>();
	}
	
	public String toString() {
       return nom;
    }
	
	public String getNom() {
	   return nom;
	}
 
	public void setNom(String nom) {
	   this.nom=nom;
	}
 
	/**
	 * Methode permettante d'ajouter un produit a la liste de courses
	 * @param p
	 */
	
	public void ajouterProduit(Produit p) {
	   this.produits.add(p);	
	}
	
	/**
	 * Methode permettante de retourner la liste des produits dans une liste de courses
	 * @return liste de produit
	 */
	
	public ArrayList<Produit> getArrayList() {
	   return this.produits;
	}
	
}
