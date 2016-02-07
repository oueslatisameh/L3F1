package com.interfacecomparateur.search;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.Produit;
import com.interfacecomparateur.classes.RequeteHTTP;
import com.interfacecomparateur.produit.ProduitRechercheActivity;
import com.interfacecomparateur.slidemenu.SlideMenu;

/**
 * <b>Activité de recherche de produit(s) par nom.</b>
 * 
 * @author Equipe L3I1
 *
 */
public class RechercheActivity extends Activity implements OnItemClickListener {
	
	private static final int NB_MAX_PRODUIT = 50;
	private static int nbProduitBdd = 0;
	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * Nom du produit recherché.
	 */
	private String recherche;
	/**
	 * JSONArray contenant les informations renvoyées par le serveur sur les produits issus de la recherche.
	 */
	private JSONArray results;
	/**
	 * ArrayList des noms des produits issus de la recherche.
	 */
	private ArrayList<String> values;
	/**
	 * ListView où les noms des produits sont affichés.
	 */
	private ListView resultatsListView;
	/**
	 * ArrayAdapter servant à l'affichage des noms des produits dans la ListView.
	 */
	private ArrayAdapter<String> arrayAdapter;
	/**
	 * Vrai si l'appareil a accès au réseau, faux sinon.
	 */
	private boolean connexionInternet;
	/**
	 * 
	 */
	private Produit produitTrouve = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recherche_vue);

		Intent intent = getIntent();
		identifiant = intent.getStringExtra("id");
		recherche = intent.getStringExtra("search");
		new SlideMenu(this,identifiant).displaySlideMenu();

		connexionInternet = RequeteHTTP.isNetworkConnected(this);

		TextView searchTextview = (TextView)findViewById(R.id.textview_search);
		searchTextview.setText(recherche);

		values = new ArrayList<String>();
		resultatsListView  = (ListView)findViewById(R.id.listview_resultats);
		arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,values) ;
		
		final Handler handler = new Handler() {
			  
		   @Override
		   public void handleMessage(Message msg) {
		      ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
		      progressBar.setVisibility(View.GONE);
		   }
					   
		};
			
	    Thread thread = new Thread(new Runnable() {
	        
		   @Override
		   public void run() {
		      Looper.prepare();
			  getResultsSearchProduct();
			  handler.sendMessage(handler.obtainMessage());
		   }
		         
		});
	    thread.start();
	}

	/**
	 * Méthode invoquée lorsque l'utilisateur clique sur l'un des items de la ListView.<br />
	 * Cette action lancera une nouvelle activité où seront affichées les informations sur le produit correspondant
	 * à l'item sur lequel l'utilisateur a cliqué.
	 * 
	 * @author Equipe L3I1
	 * 
	 * @see OnItemClickListener
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Produit produitChoisi=null;
		try {
			if(connexionInternet){
				JSONObject jsonProduit = results.getJSONObject(position);
				produitChoisi = new Produit(jsonProduit.getString("name"),jsonProduit.getString("ean"),jsonProduit.getString("brand_name"),jsonProduit.getString("img"),jsonProduit.getString("product_descriptive"));
			}
			else
				produitChoisi = produitTrouve;
			if(nbProduitBdd < NB_MAX_PRODUIT) {
				//bdd.open();
				//bdd.insertProduit(produitChoisi);
				//bdd.close();
			}
		}
		catch (JSONException e) {
			Log.d("Err",e.getMessage());
		}
		Intent intent = new Intent(RechercheActivity.this,ProduitRechercheActivity.class);
		intent.putExtra("id",identifiant);
		intent.putExtra("product",produitChoisi);
		startActivity(intent);
	}
	
	/**
	 * Envoi d'une requête GET au serveur afin de récupérer les produits correspondants à la recherche effectuée par l'utilisateur.
	 */
	private void getResultsSearchProduct() {
		final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
		if(connexionInternet){
	       RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/product",new String[] {"product"},new String[]{recherche.replace(" ","_")},identifiant);
		   final JSONObject jsonDocument = requete.sendGetRequest();
		   String registered = null;
		   if (jsonDocument==null) {
		      runOnUiThread(new Runnable() {
	            	   
	             public void run() {
				    Toast.makeText(getApplicationContext(),"Echec de la recherche de produits",Toast.LENGTH_LONG).show();
	             }
	              
		      });
		   }
		   else {
		      try {
			     registered = jsonDocument.getString("registered");
				 if (jsonDocument.has("results")) {
				    results=jsonDocument.getJSONArray("results");
				 }
				 if (jsonDocument.has("err")) {
				    runOnUiThread(new Runnable() {
					        	
			 	   	   public void run() {
			 	          try {
			 	   		     JSONArray errors = jsonDocument.getJSONArray("err");
			 	   			 errorTextView.setVisibility(View.VISIBLE);
			 	   		     StringBuffer sb = new StringBuffer();
			 	   		     for (int i=0;i<errors.length();i++)
			 	   		        sb.append(errors.get(i)+"\n");
			 	   			 errorTextView.setText(sb.toString());
			 	   		  }
			 	   		  catch(JSONException e) {
			 	   		     Log.d("Err",e.getMessage());
			 	   	      }
			 	   	   }
			 	   					           
			 	    });
			     }
			  }
			  catch(JSONException e) {
			     Log.d("Err",e.getMessage());
			  }
			  if(registered.equals("success")) {
			     try {
				    if (results.length()==0) {
					   values.add("Pas de résultat(s)");
					}
					for (int i=0;i<results.length();i++) {
				       values.add(results.getJSONObject(i).getString("name"));
					}
				 }
				 catch(JSONException e) {
				    Log.d("Err",e.getMessage());
				 }
		      }
			  runOnUiThread(new Runnable() {
			         
		         @Override
		         public void run() {
		        	errorTextView.setVisibility(View.GONE);
				    resultatsListView.setAdapter(arrayAdapter);
		         }
		            
		      });
			  if (results.length()!=0)
			     resultatsListView.setOnItemClickListener(this);
		   }
		}
		else{
	       runOnUiThread(new Runnable() {
		        	
		      public void run() {
			     Toast.makeText(getApplicationContext(),"Réseau non disponible",Toast.LENGTH_SHORT).show();
			  }
			 			     
		   });
		   //bdd.open();
		   //produitTrouve = bdd.getProduitWithNom(recherche);
		   //bdd.close();
		}
	}

}