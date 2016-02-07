package com.interfacecomparateur.profile;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import com.interfacecomparateur.classes.Magasin;
import com.interfacecomparateur.classes.RequeteHTTP;
import com.interfacecomparateur.classes.SwipeDismissListViewTouchListener;
import com.interfacecomparateur.map.CarteActivity;

/**
 * <b>Activité d'affichage des informations sur les magasins favoris de l'utilisateur.</b>
 * 
 * @author Equipe L3I1
 *
 */
public class MagasinFavoriActivity extends Activity  {

	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * ListView permettant d'afficher les magasins favoris de l'utilisateur.
	 * 
	 * @see ListView
	 */
	private ListView listviewMagasinsFavoris;
	/**
	 * ArrayList des magasins favoris de l'utilisateur.
	 */
	private ArrayList<Magasin> listMagasinsFavoris;
	/**
	 * GenericAdapter associé à la ListView permettant d'afficher les informations sur les magasins favoris.
	 * 
	 * @see GenericAdapter
	 */
	private GenericAdapter adapter;
	/**
	 * Objet Magasin utilisé pour afficher un message indiquant que l'utilisateur ne possède aucun magasin favori.
	 */
	private Magasin magasinVide;
	/**
	 * Version de l'API Android de l'appareil.
	 */
	public int sdk = Build.VERSION.SDK_INT;
	//private BaseDeDonnees bdd = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.magasins_favoris_vue);
		Intent intent = getIntent();
		identifiant = intent.getStringExtra("id");
		
		listMagasinsFavoris = new ArrayList<Magasin>();
		listviewMagasinsFavoris = (ListView)findViewById(R.id.listview_magasins_favoris);
	    adapter = new GenericAdapter(this,listMagasinsFavoris);
	    magasinVide = new Magasin("Pas de magasin(s) favori(s) enregistrés");
	    
	    listviewMagasinsFavoris.setVisibility(View.GONE);
	    
	    final Handler handler = new Handler() {
			  
		   @Override
		   public void handleMessage(Message msg) {
		      ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
		      progressBar.setVisibility(View.GONE);
		      listviewMagasinsFavoris.setVisibility(View.VISIBLE);
		   }
					   
		};
		
		Thread thread = new Thread(new Runnable() {
	        
		   @Override
		   public void run() {
		      Looper.prepare();
			  getFavoriteShop();
			  handler.sendMessage(handler.obtainMessage());
		   }
		         
		});
	    thread.start();
	       
		ImageButton buttonAdd = (ImageButton)findViewById(R.id.button_add);
		buttonAdd.setOnClickListener(new OnClickListener() {

		   @Override
		   public void onClick(View v) {
		      Intent intent = new Intent(MagasinFavoriActivity.this,RechercheMagasinActivity.class);
		      intent.putExtra("id", identifiant);
			  startActivityForResult(intent,0);
		   }
		   
		});
	    
	    ImageButton buttonBack = (ImageButton)findViewById(R.id.button_back);
		buttonBack.setOnClickListener(new OnClickListener() {
				
		   public void onClick(View v) {
		      finish();
		   }
				
		});
		
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	   Magasin newMagasinFavori = (Magasin)data.getSerializableExtra("newShop");
	   if(resultCode==0) {
	      Toast.makeText(getApplicationContext(),"Echec lors de l'ajout du magasin en tant que magasin favori",Toast.LENGTH_LONG).show();
	   }
	   else if (resultCode==1) {
	      if (this.listMagasinsFavoris.contains(magasinVide)) {
		     listMagasinsFavoris.remove(magasinVide);
	      }
	      listMagasinsFavoris.add(newMagasinFavori);
		  adapter.notifyDataSetChanged();
		  listviewMagasinsFavoris.setOnItemClickListener(new OnListItemClickListener());
		  listviewMagasinsFavoris.setOnItemLongClickListener(new OnListItemLongClickListener());
		  if(sdk>=12) {
		     listviewMagasinsFavoris.setOnScrollListener(new onScroll().touchListener.makeScrollListener());
	         listviewMagasinsFavoris.setOnTouchListener(new onScroll().touchListener);
		  }
		  Toast.makeText(getApplicationContext(),"Magasin \""+newMagasinFavori.getNom()+"\" ajouté dans la liste des magasins favoris",Toast.LENGTH_LONG).show();
	   }
	}
	
	/**
	 * Méthode invoquée lorsque l'utilisateur clique sur l'un des items de la ListView.<br />
	 * Cette action lancera une nouvelle activité où sera affiché un marqueur indiquant 
	 * l'emplacement du magasin sur une carte.
	 * 
	 * @author Equipe L3I1
	 * 
	 * @see OnItemClickListener
	 * @see CarteActivity
	 *
	 */
	class OnListItemClickListener implements OnItemClickListener {

	   public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
	      Magasin magasin =(Magasin)adapter.getItem(arg2);
		  Intent intent = new Intent(MagasinFavoriActivity.this,CarteActivity.class);
	      intent.putExtra("shop", magasin);
		  startActivity(intent);
	   }
	       
    }
	
	/**
	 * Méthode invoquée lorsque l'utilisateur clique et maintient son appui sur l'un des items de la ListView.
	 * Cette action affichera une boîte de dialogue : l'utilisateur pourra choisir de supprimer le magasin
	 * favori correspondant.
	 * 
	 * @author Equipe L3I1
	 * 
	 * @see OnItemLongClickListener
	 *
	 */
	class OnListItemLongClickListener implements OnItemLongClickListener {

       public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
	      // recuperation de l'element sur lequel on a fait un appui long
	      final Magasin magasin = (Magasin)adapter.getItem(arg2);
		  AlertDialog.Builder builderChoices = new AlertDialog.Builder(MagasinFavoriActivity.this);
		  final CharSequence[] items =  {"Supprimer de la liste"};

		  builderChoices.setItems(items, new DialogInterface.OnClickListener() {

		     @Override
		     public void onClick(DialogInterface dialog, int which) {
		        AlertDialog.Builder builder = new AlertDialog.Builder(MagasinFavoriActivity.this);
			    builder.setCancelable(true)
				.setTitle("Confirmation")
				.setMessage("Voulez-vous supprimer ce magasin de la liste ? ")
				.setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {

				   public void onClick(DialogInterface dialog, int id) {
				      ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
				      progressBar.setVisibility(View.VISIBLE);
				      listviewMagasinsFavoris.setVisibility(View.GONE);
				      Thread thread = new Thread(new Runnable() {
				              
				         @Override
				      	 public void run() {
				      	    Looper.prepare();
				      	    deleteFavoriteShop(magasin);
				      	 }
				      	         
				      });
				      thread.start();
				   }

				})
				.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {

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
    }
	
	/**
	 * Envoi d'une requête GET au serveur afin de récupérer les informations sur les magasins favoris 
	 * de l'utilisateur (Numéro de rue, rue, ville, code postal, ...).
	 * Ces informations sont ensuite affichées dans la ListView.
	 */
	private void getFavoriteShop() {
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
       final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/favorite/shop",new String[]{},new String[]{},identifiant);
	   final JSONObject jsonDocument = requete.sendGetRequest();
	   String registered = null;
	   JSONArray results = null;
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
		        Toast.makeText(getApplicationContext(),"Echec lors de la récupération des magasins favoris",Toast.LENGTH_LONG).show();
		     }
		     
	      });
	   }
	   else {
	      try {
		     registered = jsonDocument.getString("registered");
		     if (jsonDocument.has("results")) {
		        results = jsonDocument.getJSONArray("results");
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
	         if (results.length()==0) {
	            listMagasinsFavoris.add(magasinVide);
	         }
		     for (int i=0;i<results.length();i++) {
		    	JSONObject jsonMagasin;
		        try {
		           jsonMagasin = results.getJSONObject(i);
		           listMagasinsFavoris.add(new Magasin(jsonMagasin.getString("shop_id"),jsonMagasin.getString("shop_name"),
		            		                           jsonMagasin.getString("shop_descriptive"),jsonMagasin.getString("shop_type"),
		            		                           jsonMagasin.getDouble("shop_latitude"),jsonMagasin.getDouble("shop_longitude"),
		            		                           jsonMagasin.getString("shop_house_num"),jsonMagasin.getString("shop_road"),
		          				                       jsonMagasin.getString("shop_town"),jsonMagasin.getString("shop_postcode")));
		        }
		        catch(JSONException e) {
			       Log.d("Err",e.getMessage());
			    }
		     }
		     //bdd.open();
		     //for(int i = 0 ; i<listMagasinsFavoris.size() ; i++)
		    	 //bdd.insertMagasin(listMagasinsFavoris.get(i));
		     //bdd.close();
		     runOnUiThread(new Runnable() {
		         
		        @Override
		        public void run() {
		           errorTextView.setVisibility(View.GONE);
                   listviewMagasinsFavoris.setAdapter(adapter);
		        }
		        
		     });
             if (results.length()!=0) {
                listviewMagasinsFavoris.setOnItemClickListener(new OnListItemClickListener());
        	    listviewMagasinsFavoris.setOnItemLongClickListener(new OnListItemLongClickListener());
        	    if(sdk>=12) {
 			       listviewMagasinsFavoris.setOnScrollListener(new onScroll().touchListener.makeScrollListener());
        	       listviewMagasinsFavoris.setOnTouchListener(new onScroll().touchListener);
        	    }
        	 }
 	      }
 	   }
	}
	
	/**
	 * Envoi d'une requête DELETE au serveur afin de supprimer un magasin favori.
	 * 
	 * @param magasin  le magasin favori que l'utilisateur souhaite supprimer
	 */
	public void deleteFavoriteShop(Magasin magasin) {
       final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
       boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/favorite/shop/"+magasin.getId(),new String[]{},new String[]{},identifiant);
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
	        	
		     public void run() {
	            Toast.makeText(getApplicationContext(),"Echec lors de la suppression de l'adresse favorite",Toast.LENGTH_LONG).show();
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
	         listMagasinsFavoris.remove(magasin);
			 if (listMagasinsFavoris.size()==0) {
			    listMagasinsFavoris.add(magasinVide);
				listviewMagasinsFavoris.setOnItemClickListener(null);
				listviewMagasinsFavoris.setOnItemLongClickListener(null);
				if(sdk >=12) {
			       listviewMagasinsFavoris.setOnTouchListener(null);
			       listviewMagasinsFavoris.setOnScrollListener(null);
				}
		     }
			 runOnUiThread(new Runnable() {
				 
			    public void run() {
			       adapter.notifyDataSetChanged();
			       ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
				   progressBar.setVisibility(View.GONE);
				   errorTextView.setVisibility(View.GONE);
				   listviewMagasinsFavoris.setVisibility(View.VISIBLE);
			       Toast.makeText(getApplicationContext(),"Adresse favorite supprimée",Toast.LENGTH_LONG).show();
			    }
			    
			 });
	      }
	   }
	}
	
	/**
	  * Classe permettant d'implémenter une nouvelle façon de supprimer un magasin favori.
	  * L'utilisateur peut supprimer un magasin favori en scrollant l'item de la ListView correspondant 
	  * au magasin qu'il souhaite supprimer.
	  * 
	  * @author Equipe L3I1
	  * 
	  * @since API level 12
	  *
	  */
	class onScroll {

		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
	       listviewMagasinsFavoris,
		   new SwipeDismissListViewTouchListener.DismissCallbacks() {

		      @Override
			  public boolean canDismiss(int position) {
			     return true;
			  }

			  @Override
			  public void onDismiss(ListView listView, int[] reverseSortedPositions) {
			     for (int position : reverseSortedPositions) {
				    deleteFavoriteShop((Magasin) adapter.getItem(position));
				 }
				 adapter.notifyDataSetChanged();
			  }
		});
	 }
	
}