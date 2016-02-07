package com.interfacecomparateur.classes;

import java.io.Serializable;

/**
 * Classe permettant de modéliser l'ensemble des informations utiles sur une adresse.
 * 
 * @author EquipeL3I1
 *
 */
@SuppressWarnings("serial")
public class Adresse implements Serializable {
	/**
	 *  Numero de rue de l'adresse
	 */
	private String numero;
	/**
	 * Nom de rue de l'adresse
	 */
	private String rue;
	/**
	 * Nom du batiment de l'adresse
	 */
	private String batiment;
	/**
	 * Nom de la residence 
	 */
	private String residence;
	/**
	 * Le quartier de l'adresse
	 */
	private String quartier;
	/**
	 * L'arrondissement de l'adresse
	 */
	private String arrondissement;
	/**
	 * La ville de l'adresse
	 */
	private String ville;
	/**
	 * Le departement de l'adresse
	 */
	private String departement;
	/**
	 * La region de l'adresse
	 */
	private String region;
	/**
	 * Le codePostal de l'adresse
	 */
	private String codePostal;
	/**
	 * La latitude de l'adresse
	 */
	private double latitude;
	/**
	 * la longitude de l'adresse
	 */
	private double longitude;
	
	public Adresse(String numero, String rue, String batiment, String residence, String quartier, String arrondissement, String ville, String departement, String region, String codePostal, double latitude, double longitude) {
	   this.numero=numero;
	   this.rue=rue;
	   this.batiment=batiment;
	   this.residence=residence;
	   this.quartier=quartier;
	   this.arrondissement=arrondissement;
	   this.ville=ville;
	   this.departement=departement;
	   this.region=region;
	   this.codePostal=codePostal;
	   this.latitude=latitude;
	   this.longitude=longitude;
	}
	
	public Adresse(String rue) {
	   this("",rue,"","","","","","","","",0,0);
	}
	/**
	 * Methode permettante de retouner une adresse 
	 * @return L'adresse
	 */
	public String toString() {
	   StringBuffer sb = new StringBuffer();
	   if (!numero.equals("null") && !numero.equals(""))
		   sb.append(numero+", ");
	   if (!rue.equals("null") && !rue.equals(""))
		   sb.append(rue+"\n");
	   if (!batiment.equals("null") && !batiment.equals(""))
		   sb.append(batiment+"\n");
	   if (!residence.equals("null") && !residence.equals(""))
		   sb.append(residence+"\n");
	   if (!quartier.equals("null") && !quartier.equals(""))
		   sb.append(quartier+"\n");
	   if (!ville.equals("null") && !ville.equals(""))
		   sb.append(ville);
	   if (!arrondissement.equals("null") && !arrondissement.equals(""))
		   sb.append(", "+arrondissement);
	   sb.append("\n");
	   if (!codePostal.equals("null") && !codePostal.equals(""))
		   sb.append(codePostal+"\n");
	   if (!departement.equals("null") && !departement.equals(""))
		   sb.append(departement+"\n");
	   if (!region.equals("null") && !region.equals(""))
		   sb.append(region+"\n");
	   return sb.toString();
	}
	
	public String getNumero() {
	   return numero;
	}
	
	public String getRue() {
       return rue;
	}
	
	public String getBatiment() {
	   return batiment;
	}
	
	public String getResidence() {
	   return residence;
	}
	
	public String getQuartier() {
       return quartier;
	}
	
	public String getArrondissement() {
	   return arrondissement;
	}
	
	public String getVille() {
	   return ville;
	}
	
	public String getCodePostal() {
	   return codePostal;
	}
	
	public String getDepartement() {
	   return numero;
	}
	
	public String getRegion() {
	   return numero;
	}
	
	public double getLatitude() {
	   return latitude;
	}
	
	public double getLongitude() {
	   return longitude;
	}

}
