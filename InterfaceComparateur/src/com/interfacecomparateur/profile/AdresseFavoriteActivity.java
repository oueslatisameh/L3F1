package com.interfacecomparateur.profile;

import java.util.ArrayList;
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
import com.interfacecomparateur.classes.Adresse;
import com.interfacecomparateur.classes.GenericAdapter;
import com.interfacecomparateur.classes.RequeteHTTP;
import com.interfacecomparateur.classes.SwipeDismissListViewTouchListener;
import com.interfacecomparateur.map.CarteActivity;

/**
 * <b>Activité d'affichage des informations sur les adresses favorites de l'utilisateur.</b>
 * 
 * @author Equipe L3I1
 *
 */
@SuppressLint("UseValueOf")
public class AdresseFavoriteActivity extends Activity {

	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * ArrayList des adresses favorites de l'utilisateur.
	 */
	private ArrayList<Adresse> listAdressesFavorites;
	/**
	 * ListView permettant d'afficher les adresses favorites de l'utilisateur.
	 * 
	 * @see ListView
	 */
	private ListView listviewAdressesFavorites;
	/**
	 * GenericAdapter associé à la ListView permettant d'afficher les informations sur les adresses favorites.
	 * 
	 * @see GenericAdapter
	 */
	private GenericAdapter adapter;
	/**
	 * Objet Adresse utilisée pour afficher un message indiquant que l'utilisateur ne possède aucune adresse favorite.
	 */
	private Adresse adresseVide;
	/**
	 * Version de l'API Android de l'appareil.
	 */
	private final int sdk = Build.VERSION.SDK_INT;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.adresses_favorites_vue);
		Intent intent = getIntent();
		identifiant = intent.getStringExtra("id");
		
		listAdressesFavorites = new ArrayList<Adresse>();
        adapter = new GenericAdapter(AdresseFavoriteActivity.this,listAdressesFavorites);
        listviewAdressesFavorites = (ListView)findViewById(R.id.listview_adresses_favorites);
		adresseVide = new Adresse("Pas d'adresse(s) favorite(s) enregistrées");
		
		listviewAdressesFavorites.setVisibility(View.GONE);
		
		final Handler handler = new Handler() {
			  
	       @Override
		   public void handleMessage(Message msg) {
	          ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
	   		  progressBar.setVisibility(View.GONE);
	   		  listviewAdressesFavorites.setVisibility(View.VISIBLE);
		   }
				   
		};
		
		Thread thread = new Thread(new Runnable() {
        
	       @Override
	       public void run() {
	          Looper.prepare();
		      getFavoriteAddress();
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
		
		ImageButton buttonAdd = (ImageButton)findViewById(R.id.button_add);
		buttonAdd.setOnClickListener(new OnClickListener() {

		   @Override
		   public void onClick(View v) {
		      Intent intent = new Intent(AdresseFavoriteActivity.this,AjoutAdresseFavoriteActivity.class);
		      intent.putExtra("id", identifiant);
			  startActivityForResult(intent,0);
		   }
		   
		});
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	   Adresse newFavoriteAddress = (Adresse)data.getSerializableExtra("newAddress");
	   if(resultCode==0) {
	      Toast.makeText(getApplicationContext(),"Echec lors de l'ajout de l'adresse favorite dans la liste",Toast.LENGTH_LONG).show();
	   }
	   else if (resultCode==1) {
		  if (listAdressesFavorites.contains(adresseVide)) {
		     listAdressesFavorites.remove(adresseVide);
		  }
          listAdressesFavorites.add(newFavoriteAddress);
		  adapter.notifyDataSetChanged();
		  listviewAdressesFavorites.setOnItemClickListener(new OnListItemClickListener());
		  listviewAdressesFavorites.setOnItemLongClickListener(new OnListItemLongClickListener());
		  if(sdk>=12) {
		     listviewAdressesFavorites.setOnScrollListener(new OnScroll().touchListener.makeScrollListener());
		     listviewAdressesFavorites.setOnTouchListener(new OnScroll().touchListener);
		  }
		  Toast.makeText(getApplicationContext(),"Adresse favorite ajoutée",Toast.LENGTH_LONG).show();
	   }	
	}
	
	/**
	 * Méthode invoquée lorsque l'utilisateur clique sur l'un des items de la ListView.<br />
	 * Cette action lancera une nouvelle activité où sera affiché un marqueur indiquant 
	 * l'emplacement de l'adresse sur une carte.
	 * 
	 * @author Equipe L3I1
	 * 
	 * @see OnItemClickListener
	 * @see CarteActivity
	 *
	 */
	class OnListItemClickListener implements OnItemClickListener {

	   public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
	      Adresse adresse = (Adresse)adapter.getItem(arg2);
		  Intent intent = new Intent(AdresseFavoriteActivity.this,CarteActivity.class);
		  intent.putExtra("id", identifiant);
	      intent.putExtra("address",adresse);
	      startActivity(intent);
	   }
	       
    }

	/**
	 * Méthode invoquée lorsque l'utilisateur clique et maintient son appui sur l'un des items de la ListView.<br />
	 * Cette action affichera une boîte de dialogue : l'utilisateur pourra choisir de supprimer l'adresse favorite
	 * correspondante.
	 * 
	 * @author Equipe L3I1
	 * 
	 * @see OnItemLongClickListener
	 *
	 */
	class OnListItemLongClickListener implements OnItemLongClickListener {

	   public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
	      // recuperation de l'element sur lequel on a fait un appui long
		  final Adresse adresse = (Adresse)adapter.getItem(arg2);
		  AlertDialog.Builder builderChoices = new AlertDialog.Builder(AdresseFavoriteActivity.this);
		  final CharSequence[] items =  {"Supprimer de la liste"};

		  builderChoices.setItems(items, new DialogInterface.OnClickListener() {

		     @Override
			 public void onClick(DialogInterface dialog, int which) {
			    AlertDialog.Builder builder = new AlertDialog.Builder(AdresseFavoriteActivity.this);
				builder.setCancelable(true)
				.setTitle("Confirmation")
				.setMessage("Voulez-vous supprimer cettre adresse de la liste ? ")
				.setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {

			       public void onClick(DialogInterface dialog, int id) {
			    	  ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
			          progressBar.setVisibility(View.VISIBLE);
			          listviewAdressesFavorites.setVisibility(View.GONE);
			      	  Thread thread = new Thread(new Runnable() {
			              
			      	     @Override
			      	     public void run() {
			      	        Looper.prepare();
			      	        deleteFavoriteAdresse(adresse);
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
	 * Envoi d'une requête GET au serveur afin de récupérer les informations sur les adresses favorites 
	 * de l'utilisateur (Numéro de rue, rue, ville, code postal, ...).<br />
	 * Ces informations sont ensuite affichées dans la ListView.
	 */
	private void getFavoriteAddress() {	
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
       RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/favorite/address",new String[]{},new String[]{},identifiant);
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
	            Toast.makeText(getApplicationContext(),"Echec lors de la récupération des adresses favorites",Toast.LENGTH_LONG).show();
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
                listAdressesFavorites.add(adresseVide);
             }
	         for (int i=0;i<results.length();i++) {
	            try {
	        	   JSONObject addressJson = results.getJSONObject(i);
	               listAdressesFavorites.add(new Adresse(addressJson.getString("house_num"),addressJson.getString("road"),
	            		                                 addressJson.getString("building"),addressJson.getString("residential"),
	            		                                 addressJson.getString("suburb"),addressJson.getString("town_district"),
	            		                                 addressJson.getString("town"),addressJson.getString("county"),
	            		                                 addressJson.getString("state"),addressJson.getString("postcode"),
                                                         Double.parseDouble(addressJson.getString("latitude")),
                                                         Double.parseDouble(addressJson.getString("longitude"))));
	            }
	            catch(JSONException e) {
		           Log.d("Err",e.getMessage());
		        }
	         }
	         runOnUiThread(new Runnable() {
	         
	            @Override
	            public void run() {
	               errorTextView.setVisibility(View.GONE);
	               listviewAdressesFavorites.setAdapter(adapter);
                }
	            
	         });
             if (results.length()!=0) {
    	        listviewAdressesFavorites.setOnItemClickListener(new OnListItemClickListener());
    	        listviewAdressesFavorites.setOnItemLongClickListener(new OnListItemLongClickListener());
    	        if(sdk>=12) {
    	           listviewAdressesFavorites.setOnScrollListener(new OnScroll().touchListener.makeScrollListener());
    		       listviewAdressesFavorites.setOnTouchListener(new OnScroll().touchListener);
    	        }
	         }
          }
	   }
	}

	/**
	 * Envoi d'une requête DELETE au serveur afin de supprimer une adresse favorite.
	 * 
	 * @param adresse  l'adresse favorite que l'utilisateur souhaite supprimer
	 */
	public void deleteFavoriteAdresse(Adresse adresse) {
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/favorite/address/"+adresse.getRue().replace(" ","_").replace("-","+")+"-"+adresse.getVille().replace(" ","_").replace("-","+")+"-"+adresse.getCodePostal().replace(" ","_").replace("-","+"),new String[]{},new String[]{},identifiant);
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
		     listAdressesFavorites.remove(adresse);
			 if (listAdressesFavorites.size()==0) {
		        listAdressesFavorites.add(adresseVide);
			    listviewAdressesFavorites.setOnItemClickListener(null);
			    listviewAdressesFavorites.setOnItemLongClickListener(null);
			    if(sdk>=12) {
			       listviewAdressesFavorites.setOnScrollListener(null);
			       listviewAdressesFavorites.setOnTouchListener(null);
			    }
			 }
			 runOnUiThread(new Runnable() {
				 
			    public void run() {
				   adapter.notifyDataSetChanged();
				   ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
				   progressBar.setVisibility(View.GONE);
				   errorTextView.setVisibility(View.GONE);
				   listviewAdressesFavorites.setVisibility(View.VISIBLE);
				   Toast.makeText(getApplicationContext(),"Adresse favorite supprimée",Toast.LENGTH_LONG).show();
				}
				        
		     });
		  }
	   }
	}

	/**
	  * Classe permettant d'implémenter une nouvelle façon de supprimer une adresse favorite.
	  * L'utilisateur peut supprimer une adresse en scrollant l'item de la ListView correspondant 
	  * à l'adresse qu'il souhaite supprimer.
	  * 
	  * @author Equipe L3I1
	  * 
	  * @since API level 12
	  *
	  */
    class OnScroll {

	   SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
          listviewAdressesFavorites,
	      new SwipeDismissListViewTouchListener.DismissCallbacks() {

	         @Override
		     public boolean canDismiss(int position) {
		        return true;
		     }

		     @Override
		     public void onDismiss(ListView listView, int[] reverseSortedPositions) {
		        for (int position : reverseSortedPositions) {
			       deleteFavoriteAdresse((Adresse)adapter.getItem(position));
			    }
                adapter.notifyDataSetChanged();
		     }
	   });
    }
    
}