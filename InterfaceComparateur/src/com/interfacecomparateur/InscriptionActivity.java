package com.interfacecomparateur;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.RequeteHTTP;

/**
 * <b>Activité d'inscription (création d'un nouveau compte utilisateur) à l'application.</b>
 * 
 * @author Equipe L3I1
 *
 */
public class InscriptionActivity extends Activity  {
	/**
	 * Bouton déclenchant l'envoi d'une requête au serveur
	 */
	private Button boutonCreer;
	/**
	 * EditText de l'adresse e-mail saisie par l'utilisateur
	 */
	private EditText emailEditText;
	/**
	 * EditText du mot de passe saisi par l'utilisateur
	 */
	private EditText passEditText;
	/**
	 * EditText du mot de passe (confirmation) saisi par l'utilisateur
	 */
	private EditText confirmPassEditText;
	/**
	 * EditText du nom saisi par l'utilisateur
	 */
	private EditText nomEditText;
	/**
	 * EditText du prénom saisi par l'utilisateur
	 */
	private EditText prenomEditText;
	/**
	 * EditText de la date de naissance saisie par l'utilisateur
	 */
	private EditText dateNaissanceEditText;
	/**
	 * EditText de l'adresse saisie par l'utilisateur
	 */
	private EditText adresseEditText;
	/**
	 * EditText du code postal saisi par l'utilisateur
	 */
	private EditText codePostalEditText;
	/**
	 * EditText de la ville saisie par l'utilisateur
	 */
	private EditText villeEditText;
	/**
	 * TextView affichant à l'écran les éventuelles erreurs survenues
	 */
	private TextView errorTextView;
	/**
	 * Adresse e-mail saisie par l'utilisateur
	 */
    private String email;
    /**
	 * Mot de passe saisi par l'utilisateur
	 */
    private String pass1;
    /**
	 * Mot de passe (confirmation) saisi par l'utilisateur
	 */
    private String pass2;
    /**
	 * Nom saisi par l'utilisateur
	 */
    private String nom;
    /**
	 * Prénom saisi par l'utilisateur
	 */
	private String prenom;
	/**
	 * Date de naissance saisie par l'utilisateur
	 */
	private String dateNaissance;
	/**
	 * Adresse saisie par l'utilisateur
	 */
	private String adresse;
	/**
	 * Code postal saisi par l'utilisateur
	 */
	private String codePostal;
	/**
	 * Ville saisie par l'utilisateur
	 */
	private String ville;
	/**
	 * Variables pour l'envoie de mail
	 */
	private String rec, subject, textMessage;
	private Session session = null;
	ProgressDialog pdialog = null;
	Context context = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.inscription_vue);
	   this.emailEditText = (EditText)findViewById(R.id.user_email);
	   this.passEditText = (EditText)findViewById(R.id.user_password1);
	   this.confirmPassEditText = (EditText)findViewById(R.id.user_password2);
	   this.nomEditText = (EditText)findViewById(R.id.user_nom);
	   this.prenomEditText = (EditText)findViewById(R.id.user_prenom);
	   this.dateNaissanceEditText = (EditText)findViewById(R.id.user_dateDeNaissance);
	   this.adresseEditText = (EditText)findViewById(R.id.user_adresse);
	   this.codePostalEditText = (EditText)findViewById(R.id.user_codePostal);
	   this.villeEditText = (EditText)findViewById(R.id.user_ville);
		
	   boutonCreer = (Button)findViewById(R.id.boutonCreer);
       boutonCreer.setOnClickListener(new OnClickListener() {
	
	      @Override
     	  public void onClick(View v) {
	         final Handler handler = new Handler() {
					  
		   	    @SuppressWarnings("unused")
				public void handleMessage(Message msg) {
		   	       ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
		   	       progressBar.setVisibility(View.INVISIBLE);
		   		}
		   				   
		     };
	   		 Thread thread = new Thread(new Runnable() {
	           
	   	        @Override
	   	        public void run() {
	   	           Looper.prepare();
	   		       createAccount();
	   		       handler.sendMessage(handler.obtainMessage());
	   	        }
	   	         
	   		 });
	   		 thread.start();
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
	 * Envoi d'une requête POST au serveur.
	 * Si le champ 'registered' a pour valeur 'success' dans le document JSON renvoyé par le serveur, l'inscription a réussi et un e-mail
	 * contenant un lien pour activer le compte a été envoyée à l'adresse e-mail indiquée lors de l'inscription.
	 * L'interface est changée en conséquence.
	 */
	 private void createAccount() {
        email = emailEditText.getText().toString();
 	    pass1 = passEditText.getText().toString();
 	    pass2 = confirmPassEditText.getText().toString();
 	    nom = nomEditText.getText().toString();
 	    prenom = prenomEditText.getText().toString();
 	    dateNaissance = dateNaissanceEditText.getText().toString();
 	    adresse = adresseEditText.getText().toString();
 	    codePostal = codePostalEditText.getText().toString();
 	    ville = villeEditText.getText().toString();
  	    errorTextView = (TextView)findViewById(R.id.error_textview);
  	    boolean error=false;
  	    final StringBuffer sb = new StringBuffer();
	    if(email.equals("") || pass1.equals("") || pass2.equals("")) {
	       error=true;
		   sb.append("Les champs 'adresse e-mail' et 'mot de passe' doivent être renseignes");
	    }
	    if (!(pass1.equals(pass2))) {
	       if(error)
	          sb.append("\n");
	       else
		      error=true;
		   sb.append("Les deux mots de passe ne sont pas identiques");
	    }
	    if (error) {
		    runOnUiThread(new Runnable() {
	        	
	          public void run() {
	             errorTextView.setText(sb.toString());
	         	 errorTextView.setVisibility(View.VISIBLE);
	          }
	         
		   });
	    }
	    else {
		   runOnUiThread(new Runnable() {
	        	
	          public void run() {
	        	 errorTextView.setVisibility(View.GONE);
	             ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
		         progressBar.setVisibility(View.VISIBLE);
	          }
	         
		   });
		   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	       RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/registration",new String[]{"email","password","cpassword","name","forename","birth","address","postcode","town"},
	    			                             new String[]{email,ConnectionActivity.sha1Hash(pass1),ConnectionActivity.sha1Hash(pass2),nom,prenom,dateNaissance,adresse,codePostal,ville});
	       final JSONObject jsonDocument = requete.sendPostRequest();
	       String registered = null;
	       if (!networkAvailable) {
	 	      runOnUiThread(new Runnable() {
	 		        	
	 		     public void run() {
	 			    Toast.makeText(getApplicationContext(),"Reseau non disponible",Toast.LENGTH_SHORT).show();
	 			 }
	 			     
	 		  });
	 	   }
	  	   else if (jsonDocument==null) {
	 	      runOnUiThread(new Runnable() {
	 	        	
	 		     public void run() {
	 	            Toast.makeText(getApplicationContext(),"Echec de la création du compte",Toast.LENGTH_LONG).show();
	 		     }
	 		     
	 	      });
	 	   }
	  	   else {
	          try {
		         registered = jsonDocument.getString("registered");
		         registered = "success";
			     if (jsonDocument.has("err")) {
		            runOnUiThread(new Runnable() {
			        	
			           public void run() {
			              try {
			                 JSONArray errors = jsonDocument.getJSONArray("err");
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
	          if (registered.equals("success")) {
	             runOnUiThread(new Runnable() {
		        	
			        public void run() {	    	           
	    	           rec = emailEditText.getText().toString();
	    	           subject = "Activation de votre compte ScanOID";
	    	           textMessage = ""
	    	           		+ "<html>"
	    	           			+ "<body>"
	    	   		        		+ "<p>Bonjour, <br/><br/>"
	    	   		        		+ "Votre compte a bien été créé. Vous pouvez maintenant vous connecter à l'application avec vos identifiants.<br/>"
	    	   		        		+ "Nom de compte : " + emailEditText.getText().toString() +"<br/>"
	    	    	   		        + "Mot de passe : " + passEditText.getText().toString() +"<br/><br/>"
	    	   		        		+ "Cordialement,<br/>"
	    	   		        		+ "L'équipe ScanOID.<br/><br/>"
	    	   		        		+ "----------------------- ScanOID Application -----------------------<br/><br/>"
	    	   		        		+ "Ceci est un message automatique, veuillez ne pas y répondre.<br/>"
	    	   		        		+ "Pour toute informations complémentaires, n'hésitez pas à nous contacter à l'adresse suivante :<br/>"
	    	   		        		+ "scanoid.application@gmail.com</p>"
	    	           			+ "</body>"
	    	           		+ "</html>";
	    	           
	                   emailEditText.setText("");
	    	           passEditText.setText("");
	    	           confirmPassEditText.setText("");
	    	           nomEditText.setText("");
	    	           prenomEditText.setText("");
	    	           dateNaissanceEditText.setText("");
	    	           adresseEditText.setText("");
	    	           codePostalEditText.setText("");
	    	           villeEditText.setText("");
	    	           
	    	   	  	  Properties props = new Properties();
	    	   		  props.put("mail.smtp.host", "smtp.gmail.com");
	    	   		  props.put("mail.smtp.socketFactory.port", "465");
	    	   		  props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	    	   		  props.put("mail.smtp.auth", "true");
	    	   		  props.put("mail.smtp.port", "465");
	    	   		
	    	   		  session = Session.getDefaultInstance(props, new Authenticator() {
	    	   			 protected PasswordAuthentication getPasswordAuthentication() {
	    	   				return new PasswordAuthentication("noreply.scanoid@gmail.com", "scanoidl3f1");
	    	   			}
	    	   		  });
	    	   		  RetreiveFeedTask task = new RetreiveFeedTask();
	    	   		  task.execute();
			          }
	             });
	             
	          }
	       }
	    }
	}
	 
	 class RetreiveFeedTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			
			try{
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress("noreply.scanoid@gmail.com"));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(rec));
				message.setSubject(subject);
				message.setContent(textMessage, "text/html; charset=utf-8");
				Transport.send(message);
			} catch(MessagingException e) {
				e.printStackTrace();
			} catch(Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {

            ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
	         progressBar.setVisibility(View.INVISIBLE);
			Toast.makeText(getApplicationContext(), "Votre compte a bien été créé. Un mail de confirmation a été envoyé à l'adresse "+email+".", Toast.LENGTH_LONG).show();
		}
	}
}
