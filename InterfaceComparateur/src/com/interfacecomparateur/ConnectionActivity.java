package com.interfacecomparateur;

import java.security.MessageDigest;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.facebook.widget.LoginButton;
import com.interfacecomparateur.barcodescanner.ScanActivity;
import com.interfacecomparateur.card.CardActivity;
import com.interfacecomparateur.classes.RequeteHTTP;
import com.interfacecomparateur.list.ListActivity;
import com.interfacecomparateur.profile.ProfilActivity;

/**
 * <b>Activité de connexion à l'application.</b><br /><br />
 * Cette activité n'est plus utilisée si l'utilisateur est déjà connecté à l'application.
 * L'utilisateur peut se connecter à l'aide du compte qu'il aura préalablement créé dans l'activité InscriptionActivity ou
 * à l'aide de son compte Facebook.
 * 
 * @see InscriptionActivity
 * 
 * @author Equipe L3I1
 *
 */
public class ConnectionActivity extends FacebookConnectionActivity {
	
	/**
	 * Identifiant saisi par l'utilisateur
	 */
	private String identifiant = null;
	/**
	 * Mot de passe saisi par l'utilisateur
	 */
	private String mdp = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connection_vue);
		errorTextview = (TextView)findViewById(R.id.error_textview);

		LoginButton authButton = (LoginButton)findViewById(R.id.authButton);
        infoUser = new HashMap<String, String>();
     	authButton.setOnClickListener(new OnClickListener() {
				
		   @Override
		   public void onClick(View v) {
		      pb = (ProgressBar)findViewById(R.id.progressBar);
		      pb.setVisibility(View.VISIBLE);
			  setConnection();
			  getID();
		   }
		   
		});
		
		final EditText identifiantEditText = (EditText)findViewById(R.id.identifiant_edittext);
		final EditText passEditText = (EditText)findViewById(R.id.mdp_edittext);
		Button connectionButton = (Button)findViewById(R.id.button_connecter);
		connectionButton.setOnClickListener(new OnClickListener() {

		   public void onClick(View v) {
			  if (RequeteHTTP.isNetworkConnected(ConnectionActivity.this)) {
			     final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
		         identifiant = identifiantEditText.getText().toString();
			     mdp = passEditText.getText().toString();
			     final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
			     if(identifiant.equals("") || mdp.equals("")) {
				    errorTextview.setVisibility(View.VISIBLE);
				    errorTextview.setText("Identifiant et/ou mot de passe non renseigné(s)");
			     }
			     else {
			    	errorTextview.setVisibility(View.GONE);
				    progressBar.setVisibility(View.VISIBLE);
				    Thread thread = new Thread(new Runnable() {
				    	
	                   public void run() {
	                	  Looper.prepare();
	                      RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/login",new String[]{"email","password"},new String[]{identifiant,sha1Hash(mdp)},identifiant);
	   				      final JSONObject jsonDocument = requete.sendPostRequest();
	   			   	      String registered = null;
	   			   	      if (jsonDocument==null) {
	   			   	         runOnUiThread(new Runnable() {
	 		            	   
	 		                    public void run() {
	   	 		                   Toast.makeText(getApplicationContext(),"Echec de la connexion",Toast.LENGTH_LONG).show();
	   	 		                   progressBar.setVisibility(View.GONE);
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
	   			 	   				        progressBar.setVisibility(View.GONE);
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
	   				            //Sauvegarde de l'identifiant de connexion
	   				            SharedPreferences preferences = getSharedPreferences("identifiant",0);
	   				            SharedPreferences.Editor editor = preferences.edit();
	   				            editor.putString("id",identifiant);
	   				            editor.commit();
	   				    	
	   				            //Lancement de l'activité par défaut ou de l'activité choisie par l'utilisateur
	   			                preferences = getSharedPreferences("pref_activity_"+identifiant,0);
	   					        String choixActivite = preferences.getString("start_activity", "Scanner");
	   					        Class<?> activity = chooseActivity(choixActivite);
	   				            Intent intent = new Intent(ConnectionActivity.this, activity);
	   					        intent.putExtra("id", ConnectionActivity.this.identifiant);
	   					        startActivity(intent);
	   					        finish();
	   				         }
	   			   	      }
	   			   	   }
	                   
				    });
				    thread.start();
	   			 }
			  }
	   		  else {
	   		     Toast.makeText(getApplicationContext(),"Réseau non disponible",Toast.LENGTH_SHORT).show();
	   		  }
	       }
		   
		});
		
		Button forgetPasswordButton = (Button)findViewById(R.id.button_mdp_oublie);
		forgetPasswordButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
			   Intent intent = new Intent(ConnectionActivity.this,ForgetPasswordActivity.class);
			   startActivity(intent);
			}
			
		});
		
		ImageButton buttonBack = (ImageButton)findViewById(R.id.button_back);
		buttonBack.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
			   finish();
			}
		});
	}
	
	/**
	 * Hache la chaîne fournit en paramètre avec la fonction de hachage SHA1
	 * 
	 * @param string la chaîne de caractères à hacher
	 * @return la chaîne hachée
	 */
	public static String sha1Hash(String string) {
	   try{
	      MessageDigest digest = MessageDigest.getInstance("SHA-1");
	      byte[] hash = digest.digest(string.getBytes("UTF-8"));
	      StringBuffer hexString = new StringBuffer();
	         for (int i = 0; i < hash.length; i++) {
	            String hex = Integer.toHexString(0xff & hash[i]);
	            if(hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	         }
          return hexString.toString();
	    } 
	    catch(Exception ex) {
	       throw new RuntimeException(ex);
	    }
	}
	
	/**
	 * Renvoie la classe à lancer lorsque l'utilisateur accède à l'interface principale de l'application
	 * 
	 * @param choixActivite chaine de caractères récupérée grâce au mécanisme des SharedPreferences qui indique l'activité à lancer si l'utilisateur
	 *                      l'a spécifiée dans 'Mon Profil'
	 * @return la classe à lancer
	 */
	public static Class<?> chooseActivity(String choixActivite) {
	   if(choixActivite.equals("Scanner"))
	      return ScanActivity.class;
	   else if (choixActivite.equals("List"))
		  return ListActivity.class;
	   else if (choixActivite.equals("Card"))
		  return CardActivity.class;
	   else
		  return ProfilActivity.class;
	}
	
}