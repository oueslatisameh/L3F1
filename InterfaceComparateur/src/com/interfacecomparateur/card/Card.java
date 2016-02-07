package com.interfacecomparateur.card;

import android.graphics.Bitmap;

/**
 * Classe permettant de modéliser l'ensemble des informations utiles sur une carte de fidélité.
 * 
 * @author EquipeL3I1
 *
 */
public class Card {
	
	/**
	 * Nom de l'enseigne de la carte de fidélité
	 */
	private String nomEnseigne;
	/**
	 * BSIN (Brand Standard Identification Number) de l'enseigne de la carte de fidélité
	 */
	private String bsin;
	/**
	 * Image Bitmap de l'enseigne de la carte de fidélité téléchargée sur le serveur
	 */
	private Bitmap imageEnseigne;
	/**
	 * Code-barres de la carte de fidélité
	 */
	private String code;
	
	public Card(String nomEnseigne, String bsin, Bitmap imageEnseigne, String code) {
	   this.nomEnseigne=nomEnseigne;
	   this.bsin=bsin;
	   this.imageEnseigne=imageEnseigne;
	   this.code=code;
	}

	public String getNomEnseigne() {
	   return nomEnseigne;
	}
	
	public String getBsin() {
       return bsin;
	}

	public Bitmap getImageEnseigne() {
	   return imageEnseigne;
	}
	
	public String getCode() {
	   return code;
	}

}
