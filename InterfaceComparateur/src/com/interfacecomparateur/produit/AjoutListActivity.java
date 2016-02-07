package com.interfacecomparateur.produit;

import java.util.ArrayList;
import java.util.List;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.RequeteHTTP;

/**
 * <b>Activité permettant à l'utilisateur d'ajouter un produit dans une liste de courses.</b>
 * 
 * @author Equipe L3I1
 *
 */
public class AjoutListActivity extends Activity {

	/**
	 * Intent de l'activité
	 */
    private Intent intent;
    /**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * Code-barres EAN-13 du produit que l'utilisateur souhaite ajouter à une liste de courses
	 */
    private String produitEan;
    /**
     * List des noms des listes de courses de l'utilisateur
     */
	private List<String> listes;
	/**
	 * ListView permettant d'afficher les noms des listes de courses de l'utilisateur
	 */
	private ListView listview_listesDeCourses;
	/**
	 * ArrayAdapter servant à l'affichage des noms des listes de courses de l'utilisateur dans la ListView
	 */
	private ArrayAdapter<String> arrayAdapter;

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		setContentView (R.layout.ajout_liste_vue);
		
		intent = getIntent();
		identifiant = intent.getStringExtra("id");
		produitEan = intent.getStringExtra("produit");
	    
		listes = new ArrayList<String>();
		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listes);
		listview_listesDeCourses = (ListView)findViewById(R.id.listView);
		
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
		      getListName();
			  handler.sendMessage(handler.obtainMessage());
		   }
					         
		});
	    thread.start();
		
		ImageButton buttonBack = (ImageButton)findViewById(R.id.button_back);
		buttonBack.setOnClickListener(new OnClickListener() {
				
		   public void onClick(View v) {
			  setResult(-1,intent);
		      finish();
		   }
				
	    });
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	   if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0) {
	      setResult(-1,intent);
	      finish();
	   }
	   return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * Envoi d'une requête GET au serveur afin de récupérer les noms des listes de courses de l'utilisateur.
	 */
	private void getListName() {
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/listname",new String[]{},new String[]{},identifiant);
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
	            Toast.makeText(getApplicationContext(),"Echec lors de la récupération des listes",Toast.LENGTH_LONG).show();
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
	            listes.add("Pas de liste enregistrée");
	         }
		     for (int i=0;i<results.length();i++) {
		        try {
		           listes.add(results.getJSONObject(i).getString("name"));
		        }
		        catch(JSONException e) {
			       Log.d("Err",e.getMessage());
			    }
		     }
		     runOnUiThread(new Runnable() {
		         
		        @Override
				public void run() {
		           errorTextView.setVisibility(View.GONE);
		           listview_listesDeCourses.setAdapter(arrayAdapter);
		        }
		        
		     });
		     if (results.length()!=0) {
		        listview_listesDeCourses.setOnItemClickListener(new OnItemClickListener() {
		        	 
		           public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
		        	   final Handler handler = new Handler() {
		     			  
		        	      @Override
		        		  public void handleMessage(Message msg) {
		        	    	  finish();
		        		   }
		        					   
		        		};
		        		ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
		  			    progressBar.setVisibility(View.VISIBLE);
		  			    listview_listesDeCourses.setVisibility(View.GONE);
		        		Thread thread = new Thread(new Runnable() {
		        	        
		        		   @Override
		        		   public void run() {
		        		      Looper.prepare();
		        		      if (putProductInList(listes.get(arg2)))
		 				         setResult(1,intent);//ajout du produit dans la liste réussi
		 				      else
		 				         setResult(0,intent);//echec de l'ajout du produit dans la liste
		 				      intent.putExtra("liste", listes.get(arg2));
		        			  handler.sendMessage(handler.obtainMessage());
		        		   }
		        		         
		        		});
		        	    thread.start();
				   }
		        });        
		     }
		  }
	   }
	}
	
	/**
	 * Envoi d'une requête POST au serveur afin d'ajouter le produit consulté par l'utilisateur dans la liste de courses sur laquelle
	 * il a cliquée dans la ListView. 
	 * @param nomListe  le nom de la liste de courses dans laquelle l'utilisateur souhaite ajouter un nouveau produit
	 * @return vrai si l'ajout s'est bien déroulé, faux sinon
	 */
	private boolean putProductInList(final String nomListe) {
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/list/"+nomListe.replace(" ","_")+"/product/"+produitEan,new String[]{},new String[]{},identifiant);
	   final JSONObject jsonDocument = requete.sendPostRequest();
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
		         Toast.makeText(getApplicationContext(),"Echec lors de l'ajout du produit dans la liste \""+nomListe+"\"",Toast.LENGTH_LONG).show();
		      }
		      
	       });
		}
		else {
	       try {
		      registered = jsonDocument.getString("registered");
		   } 
		   catch(JSONException e) {
		      Log.d("Err",e.getMessage());
		   }
	       if(registered.equals("success")) {
		      return true;
		   }
		}
		return false;
	}

}