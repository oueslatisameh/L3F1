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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.ConnectionActivity;
import com.interfacecomparateur.classes.RequeteHTTP;

/**
 * <b>Activité permettant à l'utilisateur de changer le mot de passe associé à son compte.</b>
 * 
 * @author Equipe L3I1
 *
 */
public class ChangePasswordActivity extends Activity {
	
	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * Bouton sur lequel l'utilisateur doit appuyer pour changer son mot de passe.
	 */
	private Button buttonChange;
	/**
	 * EditText où l'utilisateur doit saisir son nouveau de passe.
	 */
	private EditText newPasswordEditText;
	/**
	 * EditText où l'utilisateur doit saisir son nouveau mot de passe (confirmation).
	 */
	private EditText newPasswordConfirmationEditText;
	/**
	 * TextView où sont affichées les éventuelles erreurs survenues au cours de l'opération.
	 */
	private TextView errorTextview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.change_password_vue); 
	   Intent intent = getIntent();
	   identifiant = intent.getStringExtra("id");
	   
	   newPasswordEditText = (EditText)findViewById(R.id.new_user_password1);
	   newPasswordConfirmationEditText = (EditText)findViewById(R.id.new_user_password2);
	   errorTextview = (TextView)findViewById(R.id.error_textview);
	   
	   ImageButton buttonBack = (ImageButton)findViewById(R.id.button_back);
	   buttonBack.setOnClickListener(new OnClickListener() {
			
	      public void onClick(View v) {
	         finish();
		  }
			
	   });
	   
	   buttonChange = (Button)findViewById(R.id.button_change);
	   buttonChange.setOnClickListener(new OnClickListener() {
			
	      public void onClick(View v) {
	         boolean error = false;
	    	 final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
	    	 StringBuffer sb = new StringBuffer();
	         final Handler handler = new Handler() {
				  
	   	        @Override
	   		    public void handleMessage(Message msg) {
	   	   		   progressBar.setVisibility(View.INVISIBLE);
	   		    }
	   				   
	   		 };
	   	     if(newPasswordEditText.getText().toString().equals("") || newPasswordConfirmationEditText.getText().toString().equals("")) {
		        error=true;
		        sb.append("Les champs 'adresse e-mail' et 'mot de passe' doivent être renseignés");
	         }
	         if (!(newPasswordEditText.getText().toString().equals(newPasswordConfirmationEditText.getText().toString()))) {
	        	if (error)
	        	   sb.append("\n");
	        	else
		           error=true;
		        sb.append("Les deux mots de passe ne sont pas identiques");
	         }
	         if (error) {
		        errorTextview.setVisibility(View.VISIBLE);
		        errorTextview.setText(sb.toString());
		     }
	         else {
	        	progressBar.setVisibility(View.VISIBLE);
	   		    Thread thread = new Thread(new Runnable() {
	           
	   	           @Override
	   	           public void run() {
	   	              Looper.prepare();
	   		          changePassword();
	   		          handler.sendMessage(handler.obtainMessage());
	   	           }
	   	         
	   		    });
	   		    thread.start();
	         }
		  }
			
	   });
    }
	
	/**
	 * Requête POST envoyée au serveur afin de changer le mot de passe de l'utilisateur par le nouveau
	 * mot de passe saisi.
	 * Si l'opération a réussie (si le champ 'registered' a pour valeur 'success' dans le document JSON renvoyé
	 * par le serveur), le texte du bouton change et indique 'Mot de passe changé'.
	 */
	private void changePassword() {
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
       RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/account/",new String[]{"field","value"},new String[]{"password",ConnectionActivity.sha1Hash(newPasswordEditText.getText().toString())},identifiant);
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
	            Toast.makeText(getApplicationContext(),"Erreur lors du changement de votre mot de passe",Toast.LENGTH_LONG).show();
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
		                 errorTextview.setVisibility(View.VISIBLE);
				         StringBuffer sb = new StringBuffer();
				         for (int i=0;i<errors.length();i++)
				            sb.append(errors.get(i)+"\n");
					     errorTextview.setText(sb.toString());
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
	               errorTextview.setVisibility(View.INVISIBLE);
	               buttonChange.setText("Mot de passe modifié");
	               buttonChange.setClickable(false);
	            }
	            
	         });
	      }
	   }
	}

}
