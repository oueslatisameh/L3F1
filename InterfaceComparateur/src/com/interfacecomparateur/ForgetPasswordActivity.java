package com.interfacecomparateur;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.RequeteHTTP;

/**
 * <b>Activité qui permet à un utilisateur de demander l'envoi d'un e-mail contenant un nouveau mot de passe et un lien pour l'activer</b><br /><br />
 * 
 * @author Equipe L3I1
 *
 */
public class ForgetPasswordActivity extends Activity {
	
	/**
	 * Bouton déclenchant l'envoi d'une requête au serveur pour l'envoi de l'e-mail à l'adresse e-mail indiquée
	 */
	private Button buttonSend;
	/**
	 * TextView dans lequel l'utilisateur saisi l'identifiant (adresse e-mail) du compte dont il a oublié le mot de passe
	 */
	private TextView emailTextView;
	/**
	 * Adresse e-mail saisie par l'utilisateur
	 */
    private String email;
    /**
     * Mot de passe de l'utilisateur
     */
    private String motDePasse = null;
	/**
	 * Variables pour l'envoie de mail
	 */
	private String rec, subject, textMessage;
	private Session session = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.forget_password_vue); 
	   emailTextView = (TextView)findViewById(R.id.email_sending_new_password);
	   
	   ImageButton buttonBack = (ImageButton)findViewById(R.id.button_back);
	   buttonBack.setOnClickListener(new OnClickListener() {
			
	      public void onClick(View v) {
	         finish();
		  }
			
	   });
	   
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   buttonSend = (Button)findViewById(R.id.button_send);
	   buttonSend.setOnClickListener(new OnClickListener() {
			
	      public void onClick(View v) {
	    	 final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
	         final Handler handler = new Handler() {
				  
	   	        @SuppressWarnings("unused")
				public void handleMessage(Message msg) {
	   	   		   progressBar.setVisibility(View.INVISIBLE);
	   		    }
	   				   
	   		};
	   		if(emailTextView.getText().toString().equals("")) {
		        errorTextView.setText("L'adresse e-mail du compte dont vous avez oublié le mot de passe doit être saisie");
	   		    errorTextView.setVisibility(View.VISIBLE);
	   		}
	   		else {
	   	       errorTextView.setVisibility(View.INVISIBLE);
	   		   progressBar.setVisibility(View.VISIBLE);
	   		   Thread thread = new Thread(new Runnable() {
	           
	   	          @Override
	   	          public void run() {
	   	             Looper.prepare();
	   		         sendEmailForNewPassword();
	   		         handler.sendMessage(handler.obtainMessage());
	   	          }
	   	         
	   		   });
	   		   thread.start();
		     }
	      }
			
	   });
	   
	}
	
	/**
	 * Envoi d'une requête POST au serveur.
	 * Si le champ 'registered' a pour valeur 'success' dans le document JSON renvoyé par le serveur, l'envoi de l'e-mail a réussi et l'interface est
	 * changée en conséquence.
	 */
	private void sendEmailForNewPassword() {
       final TextView errorTextview = (TextView)findViewById(R.id.error_textview);
       boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/recovery",new String[]{"email"},new String[]{emailTextView.getText().toString().replace(" ","_")},"");
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
	            Toast.makeText(getApplicationContext(),"Erreur lors de l'envoi de l'e-mail",Toast.LENGTH_LONG).show();
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
	        		   email = emailTextView.getText().toString();
	        		   
	    	           rec = emailTextView.getText().toString();
	    	           subject = "Identifiants de votre compte ScanOID";
	    	           textMessage = ""
	    	           		+ "<html>"
	    	           			+ "<body>"
	    	   		        		+ "<p>Bonjour, <br/><br/>"
	    	   		        		+ "Les identifiants de votre compte ScanOID sont les suivantes :<br/>"
	    	   		        		+ "Nom de compte : " + email +"<br/>"
	    	    	   		        + "Mot de passe : " + motDePasse + "<br/><br/>"
	    	   		        		+ "Cordialement,<br/>"
	    	   		        		+ "L'équipe ScanOID.<br/><br/>"
	    	   		        		+ "----------------------- ScanOID Application -----------------------<br/><br/>"
	    	   		        		+ "Ceci est un message automatique, veuillez ne pas y répondre.<br/>"
	    	   		        		+ "Pour toute informations complémentaires, n'hésitez pas à nous contacter à l'adresse suivante :<br/>"
	    	   		        		+ "scanoid.application@gmail.com</p>"
	    	           			+ "</body>"
	    	           		+ "</html>";
	    	           
	    	           emailTextView.setText("");
	    	           
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
			Toast.makeText(getApplicationContext(), "Votre demande a bien été prise en compte. Un mail a été envoyé à l'adresse " + email +".", Toast.LENGTH_LONG).show();
		}
	}
}
