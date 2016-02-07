package com.interfacecomparateur.profile;

import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.RequeteHTTP;
import com.interfacecomparateur.classes.Utilisateur;
import com.interfacecomparateur.slidemenu.SlideMenu;

/**
 * <b>Activité où l'utilisateur peut consulter et modifier certaines informations de son profil. Il peut également choisir l'activité
 * de démarrage de l'application lorsqu'il se connecte.</b><br /><br />
 * Cette activité est appelée lorsque l'utilisateur clique sur l'item 'Mon profil' dans le SlideMenu.
 * 
 * @author Equipe L3I1
 *
 */
public class ProfilActivity extends Activity {
	
	public static final String[] choixActivite = {"Scanner","List","Card","Profile"};
	public static final String[] tab = {"Scanner un produit","Mes listes de courses","Mes cartes","Mon profil"};

	/**
	 * Spinner servant à afficher les différentes activités de démarrage possibles à l'utilisateur.
	 */
	private Spinner spinnerChoixActivite = null;
	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * Indice du choix de l'activité de démarrage fait par l'utilisateur.
	 */
	private int indiceChoixActivite;
	/**
	 * Objet utilisateur contenant l'ensemble des informations connues sur l'utilisateur connecté.
	 */
	private Utilisateur utilisateur = null;
	/**
	 * TextView où est affiché l'identifiant (adresse e-mail) de l'utilisateur.
	 */
	private TextView emailTextView = null;
	/**
	 * TextView où est affiché le nom de l'utilisateur.
	 */
	private TextView nomTextView = null;
	/**
	 * TextView où est affiché le prénom de l'utilisateur.
	 */
	private TextView prenomTextView = null;
	/**
	 * Bouton où est affichée la date de naissance de l'utilisateur. L'utilisateur peut modifier cette information en cliquant sur le bouton.
	 */
	private Button dateNaissanceEditText = null;
	/**
	 * Bouton où est affichée l'adresse de l'utilisateur. L'utilisateur peut modifier cette information en cliquant sur le bouton.
	 */
	private Button adresseEditText = null;
	/**
	 * Bouton où est affiché le code postal de la ville de l'utilisateur. L'utilisateur peut modifier cette information en cliquant sur le bouton.
	 */
	private Button codePostalEditText = null;
	/**
	 * Bouton où est affichée la ville de l'utilisateur. L'utilisateur peut modifier cette information en cliquant sur le bouton.
	 */
	private Button villeEditText = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profil_vue);

		Intent intent = getIntent();
        identifiant = intent.getStringExtra("id");
     	new SlideMenu(this,identifiant).displaySlideMenu();
		
		spinnerChoixActivite = (Spinner)findViewById(R.id.list_ecran_demarrage);
		emailTextView = (TextView)findViewById(R.id.emailTextView);
		nomTextView = (TextView)findViewById(R.id.nomTextView);
		prenomTextView = (TextView)findViewById(R.id.prenomTextView);
		dateNaissanceEditText = (Button)findViewById(R.id.dateNaissanceButton);
		adresseEditText = (Button)findViewById(R.id.adresseButton);
		codePostalEditText = (Button)findViewById(R.id.codePostalButton);
		villeEditText = (Button)findViewById(R.id.villeButton);

		ArrayAdapter<String> ar = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,tab);
		ar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerChoixActivite.setAdapter(ar);
		
		spinnerChoixActivite.setOnItemSelectedListener(new OnItemSelectedListener() {

		   @Override
		   public void onItemSelected(AdapterView<?> arg0, View view,int position, long id){
		      indiceChoixActivite = position;
		   }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
			
		});
		
		SharedPreferences preferences = getSharedPreferences("pref_activity_"+identifiant,0);
	    String choix = preferences.getString("start_activity", "Scanner");
	    spinnerChoixActivite.setSelection(Arrays.asList(choixActivite).indexOf(choix));
	    
	    LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.profil_dialog_vue,(ViewGroup)findViewById(R.id.dialog_value));
		//Création du builder pour la boîte de dialogue
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true)
		.setView(view)
		.setPositiveButton("Modifier", new DialogInterface.OnClickListener() {
			
		   public void onClick(DialogInterface dialog, int id) {}
		   
		 })
		.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
			
		   public void onClick(DialogInterface dialog, int id) {
		      dialog.dismiss();
		   }
		   
		});
		
		final AlertDialog alertDialog = builder.create();
	    
	    final Handler handler = new Handler() {
			  
		   @Override
		   public void handleMessage(Message msg) {
		      LinearLayout layout = (LinearLayout)findViewById(R.id.profile_information);
			  layout.setVisibility(View.VISIBLE);
		      ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
		      progressBar.setVisibility(View.GONE);
			  dateNaissanceEditText.setOnClickListener(new EditTextListener(alertDialog,(EditText)view,utilisateur.getBirthdate(),"Date de naissance","birthdate"));
			  adresseEditText.setOnClickListener(new EditTextListener(alertDialog,(EditText)view,utilisateur.getAddress(),"Adresse","address"));
			  codePostalEditText.setOnClickListener(new EditTextListener(alertDialog,(EditText)view,utilisateur.getPostcode(),"Code postal","postcode"));
			  villeEditText.setOnClickListener(new EditTextListener(alertDialog,(EditText)view,utilisateur.getCity(),"Ville","town"));
		   }
					   
		};
			
		Thread thread = new Thread(new Runnable() {
	        
		   @Override
		   public void run() {
		      Looper.prepare();
		      retrieveProfileInformation();
			  handler.sendMessage(handler.obtainMessage());
		   }
		         
		});
	    thread.start();
		
		Button buttonSave = (Button)findViewById(R.id.button_save);
	    buttonSave.setOnClickListener(new OnClickListener() {
		
		   public void onClick(View v) {
		      //Sauvegarde du choix de l'activité de démarrage
		      SharedPreferences preferences = getSharedPreferences("pref_activity_"+identifiant,0);
		      SharedPreferences.Editor editor = preferences.edit();
		      editor.putString("start_activity",choixActivite[indiceChoixActivite]);
		      editor.commit();
		      Toast.makeText(getApplicationContext(), "Choix sauvegardé", Toast.LENGTH_LONG).show();
		   }
		
	    });
	    
	    ImageButton buttonChangePassword = (ImageButton)findViewById(R.id.button_change_password);
	    buttonChangePassword.setOnClickListener(new OnClickListener() {
	    	
	    	public void onClick(View v) {
	    		Intent intent = new Intent(ProfilActivity.this,ChangePasswordActivity.class);
	    		intent.putExtra("id", identifiant);
	    		startActivity(intent);
	    	}
	    	
	    });
	    
	    Button buttonFavoriteAddress = (Button)findViewById(R.id.button_favorite_address);
	    buttonFavoriteAddress.setOnClickListener(new OnClickListener() {
	    	
	    	public void onClick(View v) {
	    		Intent intent = new Intent(ProfilActivity.this,AdresseFavoriteActivity.class);
	    		intent.putExtra("id", identifiant);
	    		startActivity(intent);
	    	}
	    	
	    });
	    
	    Button buttonFavoriteShops = (Button)findViewById(R.id.button_favorite_shop);
	    buttonFavoriteShops.setOnClickListener(new OnClickListener() {
	    	
	    	public void onClick(View v) {
	    		Intent intent = new Intent(ProfilActivity.this,MagasinFavoriActivity.class);
	    		intent.putExtra("id", identifiant);
	    		startActivity(intent);
	    	}
	    	
	    });
	
     }
	
	 /**
	  * Envoi d'une requête GET au serveur afin de récupérer les informations connues sur l'utilisateur connecté.
	  */
	 private void retrieveProfileInformation() {
		boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
		final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	    RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/account",new String[]{},new String[]{},identifiant);
		final JSONObject jsonDocument = requete.sendGetRequest();
		String registered = null;
		JSONObject account = null;
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
		         Toast.makeText(getApplicationContext(),"Echec lors de la récupération des informations du profil",Toast.LENGTH_LONG).show();
		      }
		      
	       });
		}
		else {
	       try {
		      registered = jsonDocument.getString("registered");
		      if (jsonDocument.has("account")) {
		         account = jsonDocument.getJSONObject("account");
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
		   Log.d("registered account","registered account : "+jsonDocument.toString());
		   if(registered.equals("success")) {
		      this.utilisateur = new Utilisateur();
		      try {
		         utilisateur.setEmail(account.getString("email"));
			     utilisateur.setName(account.getString("name"));
			     utilisateur.setForename(account.getString("forename"));
			     utilisateur.setAddress(account.getString("address"));
			     String birthDate = account.getString("birthdate");
			     if (!account.getString("birthdate").equals("null") && !account.getString("birthdate").equals("")) {
			        birthDate = birthDate.substring(0,birthDate.indexOf("T"));
			     }
			     utilisateur.setBirthdate(birthDate);
			     utilisateur.setPostcode(account.getString("postcode"));
		         utilisateur.setCity(account.getString("town"));
		      }
		      catch(JSONException e) {
		         Log.d("Err",e.getMessage());
		      }
		   }
		   runOnUiThread(new Runnable() {
		         
	          @Override
	          public void run() {
	        	 errorTextView.setVisibility(View.GONE);
		         fillEditText();
	          }
	          
		   });
		}
	 }
	 
	 /**
	  * Permet de remplir les différents TextView et Button avec les informations récupérées.
	  */
	 private void fillEditText() {
	    emailTextView.setText(utilisateur.getEmail());
	    nomTextView.setText(utilisateur.getName());
        prenomTextView.setText(utilisateur.getForename());
	    dateNaissanceEditText.setText(utilisateur.getBirthdate());
	    adresseEditText.setText(utilisateur.getAddress());
	    codePostalEditText.setText(utilisateur.getPostcode());
        villeEditText.setText(utilisateur.getCity());
	 }
	
	 class EditTextListener implements OnClickListener {
		 
		 private AlertDialog alertDialog;
		 private String title;
		 private String field;
		 private String value;
		 private EditText valueEditText;
		 
		 public EditTextListener(AlertDialog alertDialog,EditText valueEditText, String value, String title, String field) {
		    this.alertDialog=alertDialog;
		    this.title=title;
		    this.field=field;
		    this.value=value;
		    this.valueEditText=valueEditText;
		 }
		 
		 public void onClick(View v) {
			 alertDialog.setTitle(title);
		     alertDialog.show();
		     if (field.equals("birthdate"))
		    	valueEditText.setText(utilisateur.getBirthdate());
		     else if (field.equals("address"))
		    	valueEditText.setText(utilisateur.getAddress());
		     else if (field.equals("postcode"))
		    	valueEditText.setText(utilisateur.getPostcode());
		     else if (field.equals("town"))
		    	valueEditText.setText(utilisateur.getCity());
		     if (value.equals("Non renseigné"))
		    	valueEditText.setText("");
		     
		     Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		     positiveButton.setOnClickListener(new OnClickListener() {
		    	 
		    	public void onClick(View v) {
		    	   if (!valueEditText.getText().toString().equals(value)) {
		    		   Thread thread = new Thread(new Runnable() {
		    		        
		    		      @Override
		    			  public void run() {
		    			     Looper.prepare();
		    		         boolean networkAvailable = RequeteHTTP.isNetworkConnected(ProfilActivity.this);
		                     RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/account/",new String[]{"field","value"},new String[]{field,valueEditText.getText().toString().replace(" ","_")},identifiant);
				             JSONObject jsonDocument = requete.sendPostRequest();
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
					                  Toast.makeText(getApplicationContext(),"Echec lors de la modicification du champ "+title,Toast.LENGTH_LONG).show();
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
			                       alertDialog.cancel();
			                       EditTextListener.this.value = valueEditText.getText().toString();
			 		               if (field.equals("birthdate"))
			 		    	          utilisateur.setBirthdate(EditTextListener.this.value);
			 		               else if (field.equals("address"))
			 		                  utilisateur.setAddress(EditTextListener.this.value);
			 		               else if (field.equals("postcode"))
			 		    	          utilisateur.setPostcode(EditTextListener.this.value);
			 		               else if (field.equals("town"))
			 		    	          utilisateur.setCity(EditTextListener.this.value);
			 		               runOnUiThread(new Runnable() {
			 		            	   
			 		                  public void run() {
			 		                	 fillEditText();
				                         Toast.makeText(getApplicationContext(),title+" modifié(é)", Toast.LENGTH_LONG).show();
			 		                  }
			 		                  
			 		               });
			                    }
					         }
				          }
		    		      
		    		   });
		    		   thread.start();
		           }
		    	}
		    	
		     });
	       }
	    }
	 
}