package com.interfacecomparateur.card;

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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.RequeteHTTP;
import com.interfacecomparateur.slidemenu.SlideMenu;

/**
 * <b>Activité qui permet à l'utilisateur de voir les différentes cartes de fidélité associées 
 * à son compte grâce à une GridView</b><br /><br />
 * Cette activité est appelée lorsque l'utilisateur clique sur l'item 'Mes cartes' dans le SlideMenu.
 * 
 * @author Equipe L3I1
 *
 */
public class CardActivity extends Activity {
	
	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * GridView permettant d'afficher les logos des marques des cartes de fidélité
	 * 
	 * @see GridView
	 */
	private GridView gridView;
	/**
	 * ImageAdapter associé à la GridView permettant d'afficher les logos des marques des cartes de fidélité
	 * 
	 * @see ImageAdapter
	 */
	private ImageAdapter imageAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.card_gridview_vue);
	   final Intent intent = getIntent();
	   identifiant = intent.getStringExtra("id");
	   new SlideMenu(this,identifiant).displaySlideMenu();
	   
	   gridView = (GridView)findViewById(R.id.card_grid_view);
       imageAdapter = new ImageAdapter(this);
       
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
		     if (intent.hasExtra("newCard")) {
		        addCard(intent.getStringExtra("brand"),intent.getStringExtra("code"));
		     }
			 getCards();
			 handler.sendMessage(handler.obtainMessage());
	      }
			         
	   });
	   thread.start();
       
	   gridView.setOnItemClickListener(new OnItemClickListener() {
		   
		   public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		      Intent intent = new Intent(CardActivity.this,BarcodeCardActivity.class);
		      intent.putExtra("id", CardActivity.this.identifiant);
		      intent.putExtra("brand", imageAdapter.getCartes().get(position).getNomEnseigne());
		      intent.putExtra("bsin", imageAdapter.getCartes().get(position).getBsin());
		      intent.putExtra("code", imageAdapter.getCartes().get(position).getCode());
		      startActivity(intent);
		      finish();
		   }
		   
	   });
	   
	   ImageButton buttonAdd = (ImageButton)findViewById(R.id.button_add);
	   buttonAdd.setOnClickListener(new OnClickListener() {
		   
		   public void onClick(View v) {
		      Intent intent = new Intent(CardActivity.this,BrandActivity.class);
		      intent.putExtra("id", CardActivity.this.identifiant);
		      startActivity(intent);
		   }
		   
	   });
	   
	}
	
	/**
	 * Envoi d'une requête GET au serveur permettant de récupérer les informations sur les cartes de fidélité 
	 * de l'utilisateur.
	 * Les images des enseignes récupérées seront utilisées pour l'affichage des cartes de fidélité dans
	 * la GridView.
	 */
	private void getCards() {
       final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
       boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/card",new String[]{},new String[]{},identifiant);
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
	            Toast.makeText(getApplicationContext(),"Echec lors de la récupération des cartes",Toast.LENGTH_LONG).show();
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
	      String nomEnseigne=null,code=null,bsin=null;
	      if(registered.equals("success")) {
		     for (int i=0;i<results.length();i++) {
		        try {
		    	   nomEnseigne = results.getJSONObject(i).getString("name");
		    	   bsin = results.getJSONObject(i).getString("bsin");
		    	   code = results.getJSONObject(i).getString("code");
		    	}
		    	catch(JSONException e) {
				   Log.d("Err",e.getMessage());
			    }
		        imageAdapter.addCard(new Card(nomEnseigne,bsin,RequeteHTTP.loadImage(RequeteHTTP.PATH_BRAND_IMG+bsin+".jpg"),code));
		     }
		     runOnUiThread(new Runnable() {
		        	
			    public void run() {
		           errorTextView.setVisibility(View.GONE);
			    }
			    
		     });
		  }
	   }
	   runOnUiThread(new Runnable() {
	         
	      @Override
	      public void run() {
		     gridView.setAdapter(imageAdapter);
	      }
	      
	   });
	}
	
	/**
	 * Envoi d'une requête POST au serveur afin d'ajouter une nouvelle carte de fidélité.
	 * L'enseigne et le code de la nouvelle carte auront été renseignés par l'utilisateur auparavant grâce
	 * aux activités BrandActivity et CardScanActivity.
	 * 
	 * @param nomEnseigne  nom de l'enseigne de la nouvelle carte de fidélité
	 * @param code  valeur du code-barres de la nouvelle carte de fidélité
	 * 
	 * @see BrandActivity
	 * @see CardScanActivity
	 */
	private void addCard(final String nomEnseigne, String code) {
       final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
       boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
       RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/card/"+nomEnseigne.replace(" ","_")+"-"+code,new String[] {},new String[]{},identifiant);
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
	            Toast.makeText(getApplicationContext(),"Echec de l'ajout de la carte de fidélité",Toast.LENGTH_LONG).show();
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
	         runOnUiThread(new Runnable() {
		        	
		        public void run() {
			       errorTextView.setVisibility(View.GONE);
			       Toast.makeText(getApplicationContext(),"Carte \""+nomEnseigne+"\" ajoutée",Toast.LENGTH_LONG).show();
				}
				    
			 });
	      }
	   }
	}

}
