package com.interfacecomparateur.classes;

/**
 * Classe permettant de modéliser l'ensemble des informations utiles sur un utilisateur
 * 
 * @author EquipeL3I1
 * 
 * @see InscriptionActivity
 *
 */
public class Utilisateur {
	
	/**
	 * Prénom de l'utilisateur
	 */
	private String forename;
	/**
	 * Nom de l'utilisateur
	 */
	private String name;
	/**
	 * Email de l'utilisateur
	 */
	private String email;
	/**
	 * Adresse (numéro et rue) de l'utilisateur
	 */
	private String address;
	/**
	 * Date de naissance de l'utilisateur
	 */
	private String birthdate;
	/**
	 * Code postal de la ville de l'utilisateur
	 */
	private String postcode;
	/**
	 * Ville de l'utilisateur
	 */
	private String city;

	public Utilisateur(String email, String name, String forename, String birthdate, String address, String postcode, String city) {
		this.forename = forename;
		this.name = name;
		this.email = email;
		this.address = address;
		this.birthdate = birthdate;
		this.postcode = postcode;
		this.city = city;
	}

	public Utilisateur() {
		this("", "", "", "", "", "", "");
	}

	public String getForename() {
		return forename;
	}

	public void setForename(String forename) {
		if (forename.equals("null") || forename.equals("")) {
		   this.forename = "Non renseigné";
		}
		else
		   this.forename = forename;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name.equals("null") || name.equals(""))
		   this.name = "Non renseigné";
		else
		   this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		if (address.equals("null") || address.equals(""))
		   this.address = "Non renseigné";
		else
		   this.address = address;
	}

	public String getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(String birthdate) {
		if (birthdate.equals("null") || birthdate.equals(""))
	       this.birthdate = "Non renseigné";
		else
		   this.birthdate = birthdate;
	}
	
	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		if (postcode.equals("null") || postcode.equals(""))
	       this.postcode = "Non renseigné";
		else
		   this.postcode = postcode;
	}
	
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		if (city.equals("null") || city.equals(""))
	       this.city = "Non renseigné";
		else
		   this.city = city;
	}

}
