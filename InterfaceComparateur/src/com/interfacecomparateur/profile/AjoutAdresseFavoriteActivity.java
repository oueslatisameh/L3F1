package com.interfacecomparateur.profile;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.Adresse;
import com.interfacecomparateur.classes.RequeteHTTP;

/**
 * <b>Activité permettant à l'utilisateur d'ajouter une nouvelle adresse favorite.</b>
 * 
 * @author Equipe L3I1
 *
 */
public class AjoutAdresseFavoriteActivity extends Activity {
	
	/**
	 * Intent de l'activité
	 */
	private Intent intent;
	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * EditText où l'utilisateur doit rentrer le numéro et la rue de l'adresse
	 */
	private EditText editTextAdresse;
	/**
	 * EditText où l'utilisateur doit rentrer la ville de l'adresse
	 */
	private EditText editTextVille;
	/**
	 * EditText où l'utilisateur doit rentrer le code postal de l'adresse
	 */
	private EditText editTextCodePostal;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.ajout_adresse_favorite_vue);
	   intent = getIntent();
	   identifiant = intent.getStringExtra("id");
	   
	   editTextAdresse = (EditText)findViewById(R.id.EditTextAdresse) ;
	   editTextVille= (EditText)findViewById(R.id.EditTextVille);
	   editTextCodePostal = (EditText)findViewById(R.id.EditTextCP);
	   
	   final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
	   
	   Button buttonAdd = (Button)findViewById(R.id.button_add);
	   buttonAdd.setOnClickListener(new OnClickListener() {

	      @Override
		  public void onClick(View v) {
	         if (!editTextAdresse.getText().toString().equals("") && !editTextVille.getText().toString().equals("") && !editTextCodePostal.getText().toString().equals("") && !editTextCodePostal.getText().toString().contains(" ")&& editTextCodePostal.getText().toString().length()==5) {
	            final Handler handler = new Handler() {
	   			  
	      	       @Override
	      		   public void handleMessage(Message msg) {
	      	   	      finish();
	      		   }
	      				   
	      		};
	      		progressBar.setVisibility(View.VISIBLE);
	      		Thread thread = new Thread(new Runnable() {
	              
	      	       @Override
	      	       public void run() {
	      	          Looper.prepare();
	      	          addFavoriteAddress();
	      		      handler.sendMessage(handler.obtainMessage());
	      	       }
	      	         
	      		});
	      		thread.start();
			 }
	         else {
	            Toast.makeText(getApplicationContext(),"Tous les champs doivent être remplis",Toast.LENGTH_LONG).show();
	         }
		  }
		   
	   });
	   
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
	 * Envoi d'une requête POST au serveur afin d'ajouter une nouvelle adresse favorite.
	 * Les informations saisies par l'utilisateur (adresse, ville et code postal) sont envoyées au serveur
	 * afin que celui-ci puisse localiser cette adresse grâce à une requête envoyée à Nominatim.
	 * Le serveur renvoie alors dans le document JSON des informations complémentaires sur l'adresse
	 * (latitude, longitude, ...) si celle-ci a été trouvée.<br />
	 * La nouvelle adresse favorite est ajoutée dans la ListView affichant les adresses favorites.
	 */
	public void addFavoriteAddress() {
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/favorite/address",new String[]{"address","town","postcode"},new String[]{editTextAdresse.getText().toString(),editTextVille.getText().toString(),editTextCodePostal.getText().toString()},identifiant);
	   final JSONObject jsonDocument = requete.sendPostRequest();
	   JSONObject result=null;
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
	            Toast.makeText(getApplicationContext(),"Echec lors de la création de la liste",Toast.LENGTH_LONG).show();
	         }
	         
	      });
	   }
	   else {
	      try {
	         registered = jsonDocument.getString("registered");
	         if (jsonDocument.has("results")) {
	        	result = jsonDocument.getJSONObject("results");
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
			 Adresse adresse=null;
			 try {
			    adresse = new Adresse(result.getString("house_number"),result.getString("road"),result.getString("building"),
			    		              result.getString("residential"),result.getString("suburb"),result.getString("city_district"),
			    		              result.getString("town"),result.getString("county"),result.getString("state"),result.getString("postcode"),
                                      Double.parseDouble(result.getString("lat")),Double.parseDouble(result.getString("lon")));
			 }
			 catch(JSONException e) {
			     Log.d("Err",e.getMessage());
			 }
			 intent.putExtra("newAddress",adresse);
		     setResult(1,intent);
		  }
		  else if (registered.equals("failure")) {
			  setResult(0,intent);
		  }
	   }
	}
}