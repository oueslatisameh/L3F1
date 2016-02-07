package com.interfacecomparateur.list;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.GenericAdapter;
import com.interfacecomparateur.classes.Produit;
import com.interfacecomparateur.classes.RequeteHTTP;
import com.interfacecomparateur.classes.SwipeDismissListViewTouchListener;
import com.interfacecomparateur.produit.ProduitRechercheActivity;

/**
 * <b>Activité d'affichage des produits dans une liste de courses de l'utilisateur.</b>
 * 
 * @author Equipe L3I1
 *
 */
@SuppressLint("UseValueOf")
public class ListProduitActivity extends Activity implements OnItemClickListener,OnItemLongClickListener {

	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * Nom de la liste de courses
	 */
	private String nomListe;
	/**
	 * Position de la liste de courses dans la ListView affichant les listes de courses de l'utilisateur.
	 */
	private int positionListe;
	/**
	 * GenericAdapter associé à la ListView permettant d'afficher les noms des produits contenus dans
	 * la liste de courses.
	 * 
	 * @see GenericAdapter
	 */
	private GenericAdapter adapter = null;
	/**
	 * ListView permettant d'afficher les différents produits contenus dans la liste de courses de l'utilisateur.
	 * 
	 * @see ListView
	 */
	private ListView listview_produitlist;
	/**
	 * Liste des produits contenus dans la liste de courses de l'utilisateur.
	 */
	private List<Produit> listeProduit;
	/**
	 * Version de l'API Android de l'appareil.
	 */
	private final int sdk = Build.VERSION.SDK_INT; 
	    
	@Override
    public void onCreate (Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.liste_produit_vue);	
	   Intent intent = getIntent();
	   identifiant = intent.getStringExtra("id");
	   positionListe = intent.getIntExtra("position",0);
	   nomListe = intent.getStringExtra("liste");
	   
	   TextView titreMenu = (TextView)findViewById(R.id.nomDeLaListe);
	   titreMenu.setText(nomListe);
	   
	   listview_produitlist = (ListView)findViewById(R.id.listViewProduitlist);
	   listeProduit = new ArrayList<Produit>();
	   adapter = new GenericAdapter(this,listeProduit);
	   
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
		      getProductsInList();
		      handler.sendMessage(handler.obtainMessage());
	       }
	         
		});
		thread.start();
	   
	    ImageButton buttonBack = (ImageButton)findViewById(R.id.button_back);
	    buttonBack.setOnClickListener(new OnClickListener() {
			
	       public void onClick(View v) {
		      finish();
		   }
			
	    });
	   
	  
	 }
	
	/**
	 * Méthode invoquée lorsque l'utilisateur clique et maintient son appui sur l'un des items de la ListView.
	 * Cette action affichera une boîte de dialogue : l'utilisateur pourra choisir de supprimer le produit correspondant
	 * de la liste de courses.
	 * 
	 * @author Equipe L3I1
	 * 
	 * @see OnItemLongClickListener
	 *
	 */
	 public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
       // Je  recupere l'element sur lequel on a fait un appui long
	   final Produit produit=(Produit)adapter.getItem(arg2);
	   AlertDialog.Builder builderChoices = new AlertDialog.Builder(ListProduitActivity.this);
	   final CharSequence[] items =  {"Supprimer le produit de la liste"} ;

       builderChoices.setItems(items, new DialogInterface.OnClickListener() {

	      @Override
	      public void onClick(DialogInterface dialog, int which) {
	         AlertDialog.Builder builder = new AlertDialog.Builder(ListProduitActivity.this);
	         builder.setCancelable(true)
	         .setTitle("Confirmation")
		     .setMessage("Voulez-vous supprimer ce produit de la liste \""+ListProduitActivity.this.nomListe+"\" ?")
		     .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
			  			
		        public void onClick(DialogInterface dialog, int id) {
		           ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
				   progressBar.setVisibility(View.VISIBLE);
				   listview_produitlist.setVisibility(View.GONE);
				   Thread thread = new Thread(new Runnable() {
					              
				      @Override
					  public void run() {
					     Looper.prepare();
					     deleteProductFromList(produit);
					  }
					      	         
				   });
				   thread.start();
			    }
			  			   
	         })
	         .setNegativeButton("Non", new DialogInterface.OnClickListener() {
			  				
		        public void onClick(DialogInterface dialog, int id) {
			       dialog.dismiss();
			    }
			  			   
	         });
	         builder.create().show();
	      }
			       
	   });
	   builderChoices.show();
	   return true;
	}
	
	 /**
	  * Méthode invoquée lorsque l'utilisateur clique sur l'un des items de la ListView.
	  * Cette action lancera une nouvelle activité où seront affichées les informations sur le produit et
	  * où l'utilisateur pourra voir les prix enregistrés pour ce produit.
	  * 
	  * @author Equipe L3I1
	  * 
	  * @see OnItemClickListener
	  * @see ProduitRechercheActivity
	  *
   	  */ 
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	   Intent intent = new Intent(ListProduitActivity.this,ProduitRechercheActivity.class);
	   intent.putExtra("id", identifiant);
	   intent.putExtra("product",listeProduit.get(position));
	   intent.putExtra("fromList",true);
	   startActivity(intent);
	}
	 
    /**
     * Envoi d'une requête GET au serveur afin de récupérer les informations sur les produits contenus
     * dans la liste de courses (nom du produit, nom du fabriquant, code EAN-13 du produit, ...).
     * Les noms des produits sont affichés dans la ListView.
     */
	void getProductsInList() {
       final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
       boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/list",new String[]{},new String[]{},identifiant);
	   final JSONObject jsonDocument = requete.sendGetRequest();
	   String registered = null;
	   JSONArray lists=null;
	   if (!networkAvailable) {
	      runOnUiThread(new Runnable() {
	 		        	
	 	     public void run() {
	 	        Toast.makeText(getApplicationContext(),"Réseau non disponible",Toast.LENGTH_SHORT).show();
	 	     }
	 			     
	 	  });
	   }
	   else if (jsonDocument==null) {
	      runOnUiThread(new Runnable() {
	        	
		     public void run() {
		        Toast.makeText(getApplicationContext(),"Echec lors de la récupération des produits",Toast.LENGTH_LONG).show();
		     }
		      
	      });
	   }
	   else {
		  try {
	         registered = jsonDocument.getString("registered");
			 if (jsonDocument.has("results")) {
			    lists = jsonDocument.getJSONObject("results").getJSONArray("lists");
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
		        if (lists.getJSONObject(positionListe).getJSONArray("products").length()==0) {
			       listeProduit.add(new Produit("Liste vide"));
		        }
		        for (int i=0;i<lists.getJSONObject(positionListe).getJSONArray("products").length();i++) {
		           JSONObject jsonProduit = lists.getJSONObject(positionListe).getJSONArray("products").getJSONObject(i);
		           Produit produit =  new Produit(jsonProduit.getString("name"),jsonProduit.getString("ean"),
		           		                          jsonProduit.getString("bname"),jsonProduit.getString("img"),
			        		                      jsonProduit.getString("descriptive"),jsonProduit.getInt("amount"));
			       listeProduit.add(produit);
			    }
			    runOnUiThread(new Runnable() {
			         
			       @Override
			       public void run() {
			          errorTextView.setVisibility(View.GONE);
			          listview_produitlist.setAdapter(adapter);
			       }
			        
			    });
			    if (lists.getJSONObject(positionListe).getJSONArray("products").length()!=0) {
				   listview_produitlist.setOnItemClickListener(this);
				   listview_produitlist.setOnItemLongClickListener(this);
				   if(sdk>=12) {
				      listview_produitlist.setOnScrollListener(new OnScroll().touchListener.makeScrollListener());
				      listview_produitlist.setOnTouchListener(new OnScroll().touchListener);
				   }
				}
		     }
			 catch(JSONException e) {
			    Log.d("Err",e.getMessage());  	
			 }
		  }
	   }
	}
	 
	/**
	 * Envoi d'une requête DELETE au serveur afin de supprimer le produit que l'utilisateur souhaite
	 * supprimer de la liste de courses.
	 * 
	 * @param produit  le produit que l'utilisateur souhaite supprimer de la liste de courses
	 */
    private void deleteProductFromList(Produit produit) {
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/list/"+nomListe.replace(" ","_")+"/product/"+produit.getEan(),new String[]{},new String[]{},identifiant);
	   final JSONObject jsonDocument = requete.sendDeleteRequest();
	   String registered = null;
	   if (!networkAvailable) {
	      runOnUiThread(new Runnable() {
	 		        	
	         public void run() {
	 	        Toast.makeText(getApplicationContext(),"Réseau non disponible",Toast.LENGTH_SHORT).show();
	 	     }
	 			     
	      });
	   }
	   else if (jsonDocument==null) {
	      runOnUiThread(new Runnable() {
		         
		     @Override
		     public void run() {
		        Toast.makeText(getApplicationContext(),"Echec lors de la suppression du produit de la liste",Toast.LENGTH_LONG).show();
		     }
		        
		  });
	   }
	   else {
	      try {
		     registered = jsonDocument.getString("registered");
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
		     listeProduit.remove(produit);
			 if (listeProduit.size()==0) {
		        listeProduit.add(new Produit("Liste vide"));
		        listview_produitlist.setOnItemClickListener(null);
		        listview_produitlist.setOnItemLongClickListener(null);
		        if(sdk>=12){
		           listview_produitlist.setOnScrollListener(null);
				   listview_produitlist.setOnTouchListener(null);
		        }
			 }
			 runOnUiThread(new Runnable() {
				 
			    public void run() {
			       adapter.notifyDataSetChanged();
				   ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
				   progressBar.setVisibility(View.GONE);
				   errorTextView.setVisibility(View.GONE);
				   listview_produitlist.setVisibility(View.VISIBLE);
				   Toast.makeText(getApplicationContext(),"Produit supprimé de la liste \""+nomListe+"\"",Toast.LENGTH_LONG).show();
			    }
					    
			 });
	      }	
	   }
	}
	 
	 /**
	  * Classe permettant d'implémenter une nouvelle façon de supprimer un produit d'une liste de courses.
	  * L'utilisateur peut supprimer un produit en scrollant l'item de la ListView correspondant 
	  * au produit qu'il souhaite supprimer.
	  * 
	  * @author Equipe L3I1
	  * 
	  * @since API level 12
	  *
	  */
	class OnScroll {

		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
				listview_produitlist,
				new SwipeDismissListViewTouchListener.DismissCallbacks() {

					@Override
					public boolean canDismiss(int position) {
						return true;
					}

					@Override
					public void onDismiss(ListView listView,
							int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							deleteProductFromList((Produit) adapter
									.getItem(position));
						}
						adapter.notifyDataSetChanged();
					}

				});
	}
}