package com.interfacecomparateur.classes;

import java.io.Serializable;
import org.osmdroid.util.GeoPoint;

/**
 * Classe permettant de modéliser l'ensemble des informations utiles sur un magasin.
 * 
 * @author EquipeL3I1
 *
 */
@SuppressWarnings("serial")
public class Magasin implements Serializable {
	
	/**
	 * Id du magasin
	 */
	private String id;
	/**
	 * Nom du magasin
	 */
	private String nom;
	/**
	 * Description du magasin
	 */
	private String description;
	/**
	 * Type du magasin
	 */
	private String type;
	/**
	 * La position du magasin
	 */
	private GeoPoint position;
	/**
	 * La latitude du magasin
	 */
	private double latitude;
	/**
	 * La Longitude du magasin 
	 */
	private double longitude;
	/**
	 * La distance du magasin
	 */
	private double distance;
	/**
	 * Le numero de rue du magasins
	 */
	private String numero;
	/**
	 * Le nom de la rue du magasin
	 */
	private String rue;
	/**
	 * La ville du magasin
	 */
	private String ville;
	/**
	 * Le code postal du magasin
	 */
	private String codePostal;
	
	public Magasin(String id, String nom, String description, String type, double latitude, double longitude, double distance, String numero, String rue, String ville, String codePostal) {
       this.id = id;
	   this.nom = nom;
	   this.description = description;
	   this.type = type;
	   this.latitude = latitude;
	   this.longitude = longitude;
	   this.distance = distance;
	   this.position = new GeoPoint(latitude,longitude);
	   this.numero = numero;
	   this.rue = rue;
	   this.ville = ville;
       this.codePostal = codePostal;
	}

	public Magasin(String id, String nom, String description, String type, double latitude, double longitude, String numero, String rue, String ville, String codePostal) {
	   this(id,nom,description,type,latitude,longitude,-1,numero,rue,ville,codePostal);
	}

	public Magasin(String id, String nom, String description, String type, double latitude, double longitude) {
	   this(id,nom,description,type,latitude,longitude,"","","","");
	}

	public Magasin(String nom) {
       this("",nom,"","",0,0,"","","","");
	}
	
	public Magasin(){
	   this("","","","",0,0,"","","","");
	}
	
	/**
	 * Methode permettante de retourner les information d'un magasin
	 * @see getSubDescription()
	 * @return Les informations d'un magasin
	 * 
	 */

	public String toString() {
	   StringBuffer sb = new StringBuffer();
	   sb.append(nom+"\n");
	   sb.append(getSubDescription());
	   return sb.toString();
	}
	
	
	/**
	 * methode permettante de retourner la description d'un magasin ainsi que son adresse
	 * 
	 * @return Description d un magasin
	 */
	
	public String getSubDescription() {
	   StringBuffer sb = new StringBuffer();
	   if (!description.equals("null") && !description.equals(""))
	      sb.append(description+"\n");
	   if (!type.equals("null") && !type.equals(""))
	      sb.append(type+"\n");
	   if (!numero.equals("null") && !numero.equals(""))
          sb.append(numero+", ");
       if (!rue.equals("null") && !rue.equals(""))
          sb.append(rue+"\n");
       if (!ville.equals("null") && !ville.equals(""))
          sb.append(ville+"\t");
       if (!codePostal.equals("null") && !codePostal.equals(""))
          sb.append(codePostal+"\n");
       return sb.toString();
	}

	public String getId() {
	   return id;
	}

	public String getNom() {
	   return nom;
	}

	public String getDescription() {
	   return description;
	}

	public String getType() {
	   return type;
	}
	
	public double getDistance() {
	   return distance;
	}

	public double getLatitude() {
	   return latitude;
	}

	public double getLongitude() {
	   return longitude;
	}
	
	public String getNumero() {
	   return numero;
	}

	public String getRue(){
	   return rue;
	}
	
	public String getVille(){
       return ville;
	}
	
	public String getCodePostal(){
	   return codePostal;
	}

	public GeoPoint getPosition() {
	   return position;
	}

	public void setId(String id){
	   this.id = id;
	}

	public void setNom(String nom){
	   this.nom = nom;
	}

	public void setDescription(String description){
	   this.description = description;
	}

	public void setType(String type){
	   this.type = type;
	}
	
	public void setNumero(String numero) {
	   this.numero = numero;
	}

	public void setRue(String rue){
	   this.rue = rue;
	}
	
	public void setVille(String ville){
	   this.ville = ville;
	}
	
	public void setCodePostal(String codePostal){
	   this.codePostal = codePostal;
	}

	public void setLatitude(double latitude){
	   this.latitude = latitude;
	}

	public void setLongitude(double longitude){
	   this.longitude = longitude;
	}

	public void setPosition(GeoPoint position) {
	   this.position = position;
	}
	
	public void setDistance(double distance){
		this.numero = this.numero ;
	}
}
