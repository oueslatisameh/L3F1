package com.interfacecomparateur.classes;

import org.json.JSONException;
import org.json.JSONObject;

public class Calculprix {
	private JSONObject jo;
	private double prix;
	private int quantite;
	private double prix_total;
	
	public Calculprix(JSONObject jo,double prix, int quantite,double prix_total){
		jo= new JSONObject();
		this.prix=prix;
		this.quantite=quantite;
		this.prix_total=prix_total;
	}

	public double getPrix(){
		try {
			return prix=jo.getDouble("amout");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return prix;
	}
	public JSONObject getJo(JSONObject o){
		return o;
	}
	public int getQuantit(){
		return quantite;
	}
	public double getPrix_total(){
		for(int i=0;i<jo.length();i++){
			try {
				prix_total=jo.getDouble("i");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return prix_total;
	}
	

}
