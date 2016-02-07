package com.interfacecomparateur.basesqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseSQLite extends SQLiteOpenHelper{
	private static final String TABLE_MAGASINS = "table_magasins";
	private static final String COL_ID_MAGASIN = "Id";
	private static final String COL_NOM_MAGASIN = "Nom";
	private static final String COL_DESCRIPTION_MAGASIN = "Description";
	private static final String COL_LATITUDE = "Latitude";
	private static final String COL_LONGITUDE = "Longitude";
	private static final String COL_TYPE_MAGASIN = "Type";
	private static final String COL_DISTANCE_MAGASIN = "Distance";
	private static final String COL_NUMERO_MAGASIN = "Numéro";
	private static final String COL_RUE_MAGASIN = "Rue";
	private static final String COL_VILLE_MAGASIN = "Ville";
	private static final String COL_CODE_POSTAL_MAGASIN = "Code_postal";
	
	private static final String CREATE_BDD_MAGASINS = "CREATE TABLE " + TABLE_MAGASINS + " ("
	+ COL_ID_MAGASIN + " TEXT NOT NULL PRIMARY KEY, " + COL_NOM_MAGASIN + " TEXT NOT NULL, "
	+ COL_DESCRIPTION_MAGASIN + " TEXT NOT NULL, " + COL_LATITUDE + "DOUBLE, "
	+ COL_LONGITUDE + "DOUBLE, " + COL_TYPE_MAGASIN + " TEXT NOT NULL, "
	+ COL_DISTANCE_MAGASIN + "DOUBLE, " + COL_NUMERO_MAGASIN + "TEXT NOT NULL, "
	+ COL_RUE_MAGASIN + "TEXT NOT NULL, " + COL_VILLE_MAGASIN + "TEXT NOT NULL, "
	+ COL_CODE_POSTAL_MAGASIN + "TEXT NOT NULL);";
	
	
	
	private static final String TABLE_PRODUITS = "table_produits";
	private static final String COL_NOM_PRODUIT = "Nom";
	private static final String COL_CODE_BARRE = "Code_barre";
	private static final String COL_NOM_FABRICANT = "Nom_Fabricant";
	private static final String COL_DIR_IMAGE = "Dir_Image";
	private static final String COL_DESCRIPTION_PRODUIT = "Description";
	private static final String COL_QUANTITE = "Quantité";
	
	private static final String CREATE_BDD_PRODUITS = "CREATE TABLE " + TABLE_PRODUITS + " ("
	+ COL_CODE_BARRE + " TEXT NOT NULL PRIMARY KEY, " + COL_NOM_PRODUIT + " TEXT NOT NULL, "
	+ COL_NOM_FABRICANT + " TEXT NOT NULL, " + COL_DIR_IMAGE + "TEXT NOT NULL,"
	+ COL_DESCRIPTION_PRODUIT + " TEXT NOT NULL, "+ COL_QUANTITE + "INTEGER);";
	
	
	
	private static final String TABLE_PRODUITS_DANS_MAGASINS = "table_produits_dans_magasins";
	private static final String COL_NOM_MAG = "Nom";
	private static final String COL_PRODUIT_EAN = "Code_barre";
	private static final String COL_PRIX_PRODUIT = "Prix_produit";
	private static final String COL_PROMOTION = "Prmotion";
	private static final String COL_INSERT_DATE = "InsertDate";
	
	private static final String CREATE_BDD_PRODUITS_DANS_MAGASINS = "CREATE TABLE " + TABLE_PRODUITS_DANS_MAGASINS + " ("
	+ COL_NOM_MAG + " TEXT NOT NULL PRIMARY KEY, " + COL_PRODUIT_EAN + " TEXT NOT NULL, "
	+ COL_PRIX_PRODUIT + " DOUBLE, " + COL_PROMOTION + "BOOLEAN,"
	+ COL_INSERT_DATE + " TIMESTAMP);";



	public BaseSQLite(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_BDD_MAGASINS);
		db.execSQL(CREATE_BDD_PRODUITS);
		db.execSQL(CREATE_BDD_PRODUITS_DANS_MAGASINS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE " + TABLE_MAGASINS + ";");
		db.execSQL("DROP TABLE " + TABLE_PRODUITS + ";");
		db.execSQL("DROP TABLE " + TABLE_PRODUITS_DANS_MAGASINS + ";");
		onCreate(db);
	}

}
