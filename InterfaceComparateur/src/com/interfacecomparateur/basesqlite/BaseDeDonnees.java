// package com.interfacecomparateur.basesqlite;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import com.interfacecomparateur.classes.Magasin;
//import com.interfacecomparateur.classes.Produit;
//import com.interfacecomparateur.classes.ProduitDansMagasin;
//
//public class BaseDeDonnees {
//	
//	private static final int VERSION_BDD = 1;
//	private static final String NOM_BDD = "baseInterne.db";
// 
//	private static final String TABLE_MAGASINS = "table_magasins";
//	private static final String COL_ID_MAGASIN = "Id";
//	private static final int NUM_COL_ID_MAGASIN = 0;
//	private static final String COL_NOM_MAGASIN = "Nom";
//	private static final int NUM_COL_NOM_MAGASIN = 1;
//	private static final String COL_DESCRIPTION_MAGASIN = "Description";
//	private static final int NUM_COL_DESCRIPTION_MAGASIN = 2;
//	private static final String COL_LATITUDE = "Latitude";
//	private static final int NUM_COL_LATITUDE = 3;
//	private static final String COL_LONGITUDE = "Longitude";
//	private static final int NUM_COL_LONGITUDE = 4;
//	private static final String COL_TYPE_MAGASIN = "Type";
//	private static final int NUM_COL_TYPE_MAGASIN = 5;
//	private static final String COL_DISTANCE_MAGASIN = "Distance";
//	private static final int NUM_COL_DISTANCE_MAGASIN = 6;
//	private static final String COL_NUMERO_MAGASIN = "Numero";
//	private static final int NUM_COL_NUMERO_MAGASIN = 7;
//	private static final String COL_RUE_MAGASIN = "Rue";
//	private static final int NUM_COL_RUE_MAGASIN = 8;
//	private static final String COL_VILLE_MAGASIN = "Ville";
//	private static final int NUM_COL_VILLE_MAGASIN = 9;
//	private static final String COL_CODE_POSTAL_MAGASIN = "Code_postale";
//	private static final int NUM_COL_CODE_POSTAL_MAGASIN = 10;
//	
//	private static final String TABLE_PRODUITS = "table_produits";
//	private static final String COL_NOM_PRODUIT = "Nom";
//	private static final int NUM_COL_NOM_PRODUIT = 0;
//	private static final String COL_CODE_BARRE = "Code_barre";
//	private static final int NUM_COL_CODE_BARRE = 1;
//	private static final String COL_NOM_FABRICANT = "Nom_Fabricant";
//	private static final int NUM_COL_NOM_FABRICANT = 0;
//	private static final String COL_DIR_IMAGE = "Dir_Image";
//	private static final int NUM_COL_DIR_IMAGE = 0;
//	private static final String COL_DESCRIPTION_PRODUIT = "Description";
//	private static final int NUM_COL_DESCRIPTION_PRODUIT = 2;
//	private static final String COL_QUANTITE = "Quantit�";
//	private static final int NUM_COL_QUANTITE = 3;
//	
//	
//	private static final String TABLE_PRODUITS_DANS_MAGASINS = "table_produits_dans_magasins";
//	private static final String COL_NOM_MAG = "Nom";
//	private static final int NUM_COL_NOM_MAG = 0;
//	private static final String COL_PRODUIT_EAN = "Code_barre";
//	private static final int NUM_COL_PRODUIT_EAN = 1;
//	private static final String COL_PRIX_PRODUIT = "Prix_produit";
//	private static final int NUM_COL_PRIX_PRODUIT = 2;
//	private static final String COL_PROMOTION = "Prmotion";
//	private static final int NUM_COL_PROMOTION = 3;
//	private static final String COL_INSERT_DATE = "InsertDate";
//	private static final int NUM_COL_INSERT_DATE = 4;
//
//	
//	
//	private SQLiteDatabase bdd;
//	private BaseSQLite baseSQLite;
//	
//	public BaseDeDonnees(Context context){
//		baseSQLite = new BaseSQLite(context, NOM_BDD, null, VERSION_BDD);
//	}
//	
//	public void open(){
//		bdd = baseSQLite.getWritableDatabase();
//	}
// 
//	public void close(){
//		bdd.close();
//	}
// 
//	public SQLiteDatabase getBDD(){
//		return bdd;
//	}
//	
//	public long insertMagasin(Magasin magasin){
//		//Cr�ation d'un ContentValues (fonctionne comme une HashMap)
//		ContentValues values = new ContentValues();
//		
//		//on lui ajoute une valeur associ� � une cl� (qui est le nom de la colonne dans laquelle on veut mettre la valeur)
//		values.put(COL_NOM_MAGASIN, magasin.getNom());
//		values.put(COL_ID_MAGASIN, magasin.getId());
//		values.put(COL_DESCRIPTION_MAGASIN, magasin.getDescription());
//		values.put(COL_LATITUDE, magasin.getLatitude());
//		values.put(COL_LONGITUDE, magasin.getLongitude());
//		values.put(COL_TYPE_MAGASIN, magasin.getType());
//		values.put(COL_DISTANCE_MAGASIN, magasin.getDistance());
//		values.put(COL_NUMERO_MAGASIN, magasin.getNumero());
//		values.put(COL_RUE_MAGASIN, magasin.getRue());
//		values.put(COL_VILLE_MAGASIN, magasin.getVille());
//		values.put(COL_CODE_POSTAL_MAGASIN, magasin.getCodePostal());
//
//		//on ins�re l'objet dans la BDD via le ContentValues
//		return bdd.insert(TABLE_MAGASINS, null, values);
//	}
//	
//	public int updateMagasin(String nom, Magasin magasin){
//		//La mise � jour d'un magasin dans la BDD fonctionne plus ou moins comme une insertion
//		//il faut simplement pr�ciser quel magasin on doit mettre � jour gr�ce � son nom
//		ContentValues values = new ContentValues();
//		values.put(COL_NOM_MAGASIN, magasin.getNom());
//		values.put(COL_ID_MAGASIN, magasin.getId());
//		return bdd.update(TABLE_MAGASINS, values, COL_NOM_MAGASIN + " = " +nom, null);
//	}
//	
//	public int removeMagasinWithId(String id){
//		//Suppression d'un magasin de la BDD gr�ce � son nom
//		return bdd.delete(TABLE_MAGASINS, COL_ID_MAGASIN + " = " +id, null);
//	}
//	
//	public int removeMagasinWithNom(String nom){
//		//Suppression d'un magasin de la BDD gr�ce � son nom
//		return bdd.delete(TABLE_MAGASINS, COL_NOM_MAGASIN + " = " +nom, null);
//	}
//	
//	public Magasin getMagasinWithNom(String nom){
//		//R�cup�re dans un Cursor les valeur correspondant � un magasin contenu dans la BDD
//		Cursor c = bdd.query(TABLE_MAGASINS, new String[] {COL_ID_MAGASIN, COL_NOM_MAGASIN,
//				COL_DESCRIPTION_MAGASIN, COL_LATITUDE, COL_LONGITUDE, COL_TYPE_MAGASIN, COL_DISTANCE_MAGASIN, 
//				COL_NUMERO_MAGASIN, COL_RUE_MAGASIN, COL_VILLE_MAGASIN, COL_CODE_POSTAL_MAGASIN},
//				COL_NOM_MAGASIN + " LIKE \"" + nom +"\"", null, null, null, null, null);
//		return cursorToMagasin(c);
//	}
//	
//	public Magasin getMagasinWithId(String id){
//		//R�cup�re dans un Cursor les valeur correspondant � un magasin contenu dans la BDD
//		Cursor c = bdd.query(TABLE_MAGASINS, new String[] {COL_ID_MAGASIN, COL_NOM_MAGASIN,
//				COL_DESCRIPTION_MAGASIN, COL_LATITUDE, COL_LONGITUDE, COL_TYPE_MAGASIN, COL_DISTANCE_MAGASIN, 
//				COL_NUMERO_MAGASIN, COL_RUE_MAGASIN, COL_VILLE_MAGASIN, COL_CODE_POSTAL_MAGASIN},
//				COL_ID_MAGASIN + " LIKE \"" + id +"\"", null, null, null, null, null);
//		return cursorToMagasin(c);
//	}
//	
//	//Cette m�thode permet de convertir un cursor en un magasin
//	private Magasin cursorToMagasin(Cursor c){
//		//si aucun �l�ment n'a �t� retourn� dans la requ�te, on renvoie null
//		if (c.getCount() == 0)
//			return null;
//		//Sinon on se place sur le premier �l�ment
//		c.moveToFirst();
//		
//		//On cr�� un magasin
//		Magasin magasin = new Magasin();
//		//on lui affecte toutes les infos gr�ce aux infos contenues dans le Cursor
//		magasin.setId(c.getString(NUM_COL_ID_MAGASIN));
//		magasin.setNom(c.getString(NUM_COL_NOM_MAGASIN));
//		magasin.setDescription(c.getString(NUM_COL_DESCRIPTION_MAGASIN));
//		magasin.setLatitude(c.getDouble(NUM_COL_LATITUDE));
//		magasin.setLongitude(c.getDouble(NUM_COL_LONGITUDE));
//		magasin.setType(c.getString(NUM_COL_TYPE_MAGASIN));
//		magasin.setDistance(c.getDouble(NUM_COL_DISTANCE_MAGASIN));
//		magasin.setNumero(c.getString(NUM_COL_NUMERO_MAGASIN));
//		magasin.setRue(c.getString(NUM_COL_RUE_MAGASIN));
//		magasin.setVille(c.getString(NUM_COL_VILLE_MAGASIN));
//		magasin.setCodePostal(c.getString(NUM_COL_CODE_POSTAL_MAGASIN));
//
//		//On ferme le cursor
//		c.close();
// 
//		return magasin;
//	}
//	
//	public long insertProduit(Produit produit){
//		//Cr�ation d'un ContentValues (fonctionne comme une HashMap)
//		ContentValues values = new ContentValues();
//		
//		//on lui ajoute une valeur associ� � une cl� (qui est le nom de la colonne dans laquelle on veut mettre la valeur)
//		values.put(COL_NOM_PRODUIT, produit.getNom());
//		values.put(COL_CODE_BARRE, produit.getEan());
//		values.put(COL_DESCRIPTION_PRODUIT, produit.getDescriptif());
//		values.put(COL_NOM_FABRICANT, produit.getNomFabricant());
//		values.put(COL_DIR_IMAGE, produit.getDirImage());
//		values.put(COL_QUANTITE, produit.getQuantite());		
//		//on ins�re l'objet dans la BDD via le ContentValues
//		return bdd.insert(TABLE_PRODUITS, null, values);
//	}
//	
//	public int removeProduitWithCodeBarre(String codeBarre){
//		//Suppression d'un produit de la BDD gr�ce � son code barre
//		return bdd.delete(TABLE_PRODUITS, COL_CODE_BARRE + " = " +codeBarre, null);
//	}
//	
//	public int removeProduitWithNom(String nom){
//		//Suppression d'un produit de la BDD gr�ce � son code barre
//		return bdd.delete(TABLE_PRODUITS, COL_NOM_PRODUIT + " = " +nom, null);
//	}
//	
//	
//	public Produit getProduitWithCodeBarre(String codeBarre){
//		//R�cup�re dans un Cursor les valeur correspondant � un produit contenu dans la BDD
//		Cursor c = bdd.query(TABLE_PRODUITS, new String[] {COL_NOM_PRODUIT, COL_CODE_BARRE,
//				COL_NOM_FABRICANT, COL_DIR_IMAGE,COL_DESCRIPTION_PRODUIT, COL_QUANTITE},
//				COL_CODE_BARRE + " LIKE \"" + codeBarre +"\"", null, null, null, null);
//		return cursorToProduit(c);
//	}
//	
//	public Produit getProduitWithNom(String nom){
//		//R�cup�re dans un Cursor les valeur correspondant � un produit contenu dans la BDD
//		Cursor c = bdd.query(TABLE_PRODUITS, new String[] {COL_NOM_PRODUIT, COL_CODE_BARRE,
//				COL_NOM_FABRICANT, COL_DIR_IMAGE,COL_DESCRIPTION_PRODUIT, COL_QUANTITE},
//				COL_NOM_PRODUIT + " LIKE \"" + nom +"\"", null, null, null, null);
//		return cursorToProduit(c);
//	}
//	
//	//Cette m�thode permet de convertir un cursor en un produit
//	private Produit cursorToProduit(Cursor c){
//		//si aucun �l�ment n'a �t� retourn� dans la requ�te, on renvoie null
//		if (c.getCount() == 0)
//			return null;
//		//Sinon on se place sur le premier �l�ment
//		c.moveToFirst();
//		
//		//On cr�� un produit
//		Produit produit = new Produit();
//		//on lui affecte toutes les infos gr�ce aux infos contenues dans le Cursor
//		produit.setNom(c.getString(NUM_COL_NOM_PRODUIT));
//		produit.setEan(c.getString(NUM_COL_CODE_BARRE));
//		produit.setDescriptif(c.getString(NUM_COL_DESCRIPTION_PRODUIT));
//		produit.setQuantite(c.getInt(NUM_COL_QUANTITE));
//		produit.setDirImage(c.getString(NUM_COL_DIR_IMAGE));
//		produit.setNomFabricant(c.getString(NUM_COL_NOM_FABRICANT));
//
//		//On ferme le cursor
//		c.close();
// 
//		return produit;
//	}
//	
//	public long insertProduitDansMagasin(ProduitDansMagasin produitDansMagasin){
//		//Cr�ation d'un ContentValues (fonctionne comme une HashMap)
//		ContentValues values = new ContentValues();
//		
//		//on lui ajoute une valeur associ� � une cl� (qui est le nom de la colonne dans laquelle on veut mettre la valeur)
//		values.put(COL_NOM_MAG, produitDansMagasin.getNomMagasin());
//		values.put(COL_PRODUIT_EAN, produitDansMagasin.getProduitEan());
//		values.put(COL_PRIX_PRODUIT, produitDansMagasin.getPrixProduit());
//		values.put(COL_PROMOTION, produitDansMagasin.isPromotion());
//		//on ins�re l'objet dans la BDD via le ContentValues
//		return bdd.insert(TABLE_PRODUITS_DANS_MAGASINS, null, values);
//	}
//	
//	public int updateProduit(String codeBarre, Produit produit){
//		//La mise � jour d'un produit dans la BDD fonctionne plus ou moins comme une insertion
//		//il faut simple pr�ciser quel produit on doit mettre � jour gr�ce � son code barre
//		ContentValues values = new ContentValues();
//		values.put(COL_NOM_MAGASIN, produit.getNom());
//		values.put(COL_CODE_BARRE, produit.getEan());
//		return bdd.update(TABLE_PRODUITS, values, COL_CODE_BARRE + " = " +codeBarre, null);
//	}
//	
//	public int removeProduitDansMagasinWithNomMag(String nomMag){
//		//Suppression d'un produit de la BDD gr�ce � son code barre
//		return bdd.delete(TABLE_PRODUITS_DANS_MAGASINS, COL_NOM_MAG + " = " +nomMag, null);
//	}
//	
//	public int removeProduitWithProduitEan(String ean){
//		//Suppression d'un produit de la BDD gr�ce � son code barre
//		return bdd.delete(TABLE_PRODUITS_DANS_MAGASINS, COL_PRODUIT_EAN + " = " +ean, null);
//	}
//	
//	
//	public Produit getProduitDansMagasinWithProduitEan(String ean){
//		//R�cup�re dans un Cursor les valeur correspondant � un produit contenu dans la BDD
//		Cursor c = bdd.query(TABLE_PRODUITS_DANS_MAGASINS, new String[] {COL_NOM_MAG, COL_PRODUIT_EAN,
//				COL_PRIX_PRODUIT, COL_PROMOTION,COL_INSERT_DATE},
//				COL_PRODUIT_EAN + " LIKE \"" + ean +"\"", null, null, null, null);
//		return cursorToProduit(c);
//	}
//	
//	public Produit getProduitWithNomMag(String nomMag){
//		//R�cup�re dans un Cursor les valeur correspondant � un produit contenu dans la BDD
//		Cursor c = bdd.query(TABLE_PRODUITS_DANS_MAGASINS, new String[] {COL_NOM_MAG, COL_PRODUIT_EAN,
//				COL_PRIX_PRODUIT, COL_PROMOTION,COL_INSERT_DATE},
//				COL_NOM_MAG + " LIKE \"" + nomMag +"\"", null, null, null, null);
//		return cursorToProduit(c);
//	}
//	
//	//Cette m�thode permet de convertir un cursor en un produit
//	private ProduitDansMagasin cursorToProduitDansMagasin(Cursor c){
//		//si aucun �l�ment n'a �t� retourn� dans la requ�te, on renvoie null
//		if (c.getCount() == 0)
//			return null;
//		//Sinon on se place sur le premier �l�ment
//		c.moveToFirst();
//		
//		//On cr�� un produit
//		ProduitDansMagasin produitDansMagasin = new ProduitDansMagasin();
//		//on lui affecte toutes les infos gr�ce aux infos contenues dans le Cursor
//		produitDansMagasin.setNomMagasin(c.getString(NUM_COL_NOM_MAG));
//		produitDansMagasin.setProduitEan(c.getString(NUM_COL_PRODUIT_EAN));
//		produitDansMagasin.setPrixProduit(c.getDouble(NUM_COL_PRIX_PRODUIT));
//		produitDansMagasin.setPromotion(c.getBoolean(NUM_COL_PROMOTION));
//		produitDansMagasin.setDirImage(c.getString(NUM_COL_DIR_IMAGE));
//		produitDansMagasin.setNomFabricant(c.getString(NUM_COL_NOM_FABRICANT));
//
//		//On ferme le cursor
//		c.close();
// 
//		return produitDansMagasin;
//	}
//}
