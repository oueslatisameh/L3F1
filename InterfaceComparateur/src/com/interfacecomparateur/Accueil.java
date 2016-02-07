package com.interfacecomparateur;

import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.RequeteHTTP;
import android.app.AlertDialog;
import android.view.KeyEvent;
import android.widget.ImageButton;

/**
 * <b>Activité d'accueil de l'application.</b>
 * 
 * @author Equipe L3I1
 *
 */
@SuppressLint("InflateParams")
public class Accueil extends Activity {

	/** 
	 * Bouton "Créer un compte"
	 */
	private Button creer;
	/**
	 * Bouton "Se connecter" ou "Accès à Scanoid" si l'utilisateur est déjà connecté
	 */
	private Button connection;
	/**
	 * Identifiant de l'utilisateur utilisé pour se connecter
	 */
	private String identifiant;
	/**
	 * AlertDialog pour quitter l'application
	 */
    private AlertDialog dialog;
    private AlertDialog.Builder adb;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accueil_vue);
		
		LayoutInflater factory = LayoutInflater.from(this);
        final View alertDialogView = factory.inflate(R.layout.alert_dialog_quit_vue, null);

        adb = new AlertDialog.Builder(this);
        adb.setView(alertDialogView);
        adb.setCancelable(false);

        final ImageButton valider = (ImageButton)alertDialogView.findViewById(R.id.imageButton);
        final ImageButton annuler = (ImageButton)alertDialogView.findViewById(R.id.imageButton2);

        // Bouton "Valider" au AlertDialog, pour quitter l'application
        valider.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        // Bouton "Annuler" au AlertDialog, pour rester sur l'application
        annuler.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog = adb.create();
        
		Intent intent = getIntent();
		identifiant = intent.getStringExtra("id");
		creer = (Button)findViewById(R.id.button_creation);
		connection=(Button)findViewById(R.id.button_connection);
		
		// L'utilisateur a appuyé sur "Déconnexion"
		if (intent.getBooleanExtra("logout",false)) {
	       final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
	       final LinearLayout boutons = (LinearLayout)findViewById(R.id.accueil_boutons);
		   final Handler handler = new Handler() {
				  
		      @Override
			  public void handleMessage(Message msg) {
			     progressBar.setVisibility(View.GONE);
			     determineAccess();
			     boutons.setVisibility(View.VISIBLE);
			  }
			
		   };
		   Thread thread = new Thread(new Runnable() {
		   
		      @Override
		      public void run() {
		    	 Looper.prepare();
				 progressBar.setVisibility(View.VISIBLE);
				 boutons.setVisibility(View.GONE);
				 if (logoutProcedure())
			        handler.sendMessage(handler.obtainMessage());
			  }
					         
		   });
		   thread.start();
		   
		}
		else
		   determineAccess();
		
		creer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Accueil.this, InscriptionActivity.class);
				startActivity(intent);
			}
			
		});

	}
	
	@Override
	protected void onResume() {
	   super.onResume();
	   determineAccess();
	}
	
	/**
	 * Détermine si un utilisateur est déjà connecté en vérifiant si un identifiant et le cookie de session associé sont enregistrés dans la mémoire de l'appareil.<br />
	 * Si tel est le cas, une requête est envoyée au serveur pour vérifier si l'utilisateur est toujours connecté sur le serveur.
	 * Si le serveur renvoie 'success' l'utilisateur peut accéder directement à l'application, sinon il doit se connecter.
	 * L'interface est donc changée en conséquence.
	 * 
	 * @see ConnectionActivity
	 */
	private void determineAccess() {
	   final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)connection.getLayoutParams();
	   SharedPreferences preferences = getSharedPreferences("identifiant",0);
	   final String identifiant = preferences.getString("id","");
	   preferences = getSharedPreferences("name_"+identifiant,0);
	   final String cookieNameRetrieved = preferences.getString("name","");
	   final boolean networkAvailable = RequeteHTTP.isNetworkConnected(Accueil.this);
	   if (!cookieNameRetrieved.equals("")) {
	      connection.setText("Accès à ScanOID ("+identifiant+")");
	      creer.setVisibility(View.GONE);
	      params.setMargins(0,110,0,0);
	      connection.setLayoutParams(params);
   		  connection.setOnClickListener(new OnClickListener() {	   
   		   
	   	     @Override
	   		 public void onClick(View v) {
	   	    	if (!networkAvailable) {
	   		   	   Toast.makeText(getApplicationContext(),"Réseau non disponible",Toast.LENGTH_SHORT).show();
	   			}
	   	    	else {
	   	    	   final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
		   		   final LinearLayout boutons = (LinearLayout)findViewById(R.id.accueil_boutons);
	   	    	   final Handler handler = new Handler() {
					  
	   		          @Override
	   		          public void handleMessage(Message msg) {
	   		             SharedPreferences preferences = getSharedPreferences("pref_activity_"+identifiant,0);
		   			     String choixActivite = preferences.getString("start_activity", "Scanner");
		   		         Class<?> activity = ConnectionActivity.chooseActivity(choixActivite);
		   		         Intent intent = new Intent(Accueil.this, activity);
		   		         intent.putExtra("id", identifiant);
		   		         progressBar.setVisibility(View.GONE);
					     boutons.setVisibility(View.VISIBLE);
		   			     startActivity(intent);
	   		          }
	   		        
	   	    	   };
	   	    	   progressBar.setVisibility(View.VISIBLE);
			       boutons.setVisibility(View.GONE);
	   		       Thread thread = new Thread(new Runnable() {
	   				
	   		          @Override
	   		          public void run() {
	   		    	     Looper.prepare();
	   		             if (checkIfConnectedOnServer(identifiant)) {
	   		                handler.sendMessage(handler.obtainMessage());
	   		             }
	   		             else {
	   		                runOnUiThread(new Runnable() {
	   			        	
	   			               @Override 
	   			               public void run() {
	   			                  Toast.makeText(Accueil.this,"Vous n'êtes plus connecté sur le serveur",Toast.LENGTH_LONG).show();
	   			                  SharedPreferences preferences = getSharedPreferences("name_"+identifiant,0);
	   				              SharedPreferences.Editor editor = preferences.edit();
	   				              editor.remove("name");
	   				              editor.commit();
	   				              preferences = getSharedPreferences("identifiant",0);
	   				              editor.remove("id");
	   				              editor.commit();
	   			                  connection.setText("Se connecter");
	   			                  params.setMargins(0,40,0,0);
	   			  	              connection.setLayoutParams(params);
	   			                  progressBar.setVisibility(View.GONE);
	   			                  creer.setVisibility(View.VISIBLE);
	  					          boutons.setVisibility(View.VISIBLE);
	   			               }
	   			           
	   		        	    });
	   		   	            connection.setOnClickListener(new OnClickListener(){

	   		   		           @Override
	   		   			       public void onClick(View v) {
	   		   			          Intent intent = new Intent(Accueil.this, ConnectionActivity.class);
	   		   				      startActivity(intent);
	   		   			       }
	   		   		      
	   		   		        });
	   		             }
	   		          }
	   			
	   		       });
	   		       thread.start();
	   	    	}
	   	     }
	   	      
   		  });
	   }
	   else {
   	      connection.setText("Se connecter");
   	      params.setMargins(0,40,0,0);
          connection.setLayoutParams(params);
   	      creer.setVisibility(View.VISIBLE);
   	      connection.setOnClickListener(new OnClickListener(){

   		     @Override
   			 public void onClick(View v) {
   			    Intent intent = new Intent(Accueil.this, ConnectionActivity.class);
   				startActivity(intent);
   			 }
   		      
   		  });
   	   }
	}
	
	/**
	 * Envoi d'une requête GET au serveur pour lui signifier la tentative de déconnexion de l'utilisateur.<br />
	 * La déconnexion est effective si le champ 'registered' a pour valeur 'success' dans le document JSON renvoyé par le serveur.<br />
	 * Si tel est le cas, la valeur du cookie de session est supprimée de la mémoire du téléphone, ainsi que l'identifiant de l'utilisateur.<br />
	 * Dans le cas contraire, un message d'erreur indique à l'utilisateur que la déconnexion a échouée. 
	 */
	private boolean logoutProcedure() {
       RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/logout",new String[]{},new String[]{},identifiant);
	   JSONObject jsonDocument = requete.sendGetRequest();
	   String registered = null;
	   if (jsonDocument==null) {
	      Toast.makeText(getApplicationContext(),"Echec de la déconnexion",Toast.LENGTH_LONG).show();
	      return false;
	   }
	   else {
	      try {
		     registered = jsonDocument.getString("registered");
		  }
	      catch(JSONException e) {
		     Log.d("Err",e.getMessage());
		  }
		  if(registered.equals("success")) {
		     SharedPreferences preferences = getSharedPreferences("name_"+identifiant,0);
	         SharedPreferences.Editor editor = preferences.edit();
	         editor.remove("name");
	         editor.commit();
	         preferences = getSharedPreferences("identifiant",0);
	         editor.remove("id");
	         editor.commit();
	         runOnUiThread(new Runnable() {
	        	
	        	@Override 
	            public void run() {
	               Toast.makeText(Accueil.this,"Déconnexion réussie",Toast.LENGTH_LONG).show();
	        	}
	         
	         });
	         return true;
		  }
		  return false;
	   }
	}

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            dialog.show();
        }
        return super.onKeyDown(keyCode, event);
    }
	
	/**
	 * Envoi d'une requête GET au serveur afin de vérifier si l'utilisateur est toujours connecté sur le serveur, i.e. si le cookie de session
	 * associé à son identifiant est toujours enregistré sur celui-ci.
	 * 
	 * @param identifiant identifiant de l'utilisateur connecté
	 * @return vrai si l'utilisateur est connecté sur le serveur, faux sinon
	 */
	private boolean checkIfConnectedOnServer(String identifiant) {
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/connected",new String[]{},new String[]{},identifiant);
	   JSONObject jsonDocument = requete.sendGetRequest();
	   String registered = null;
	   if (jsonDocument==null) {
	      Toast.makeText(getApplicationContext(),"Echec de la vérification de la connexion",Toast.LENGTH_LONG).show();
	      return false;
	   }
	   else {
	      try {
		     registered = jsonDocument.getString("registered");
		  }
		  catch(JSONException e) {
		     Log.d("Err",e.getMessage());
		  }
		  return (registered.equals("success"));
	   }
	}

}