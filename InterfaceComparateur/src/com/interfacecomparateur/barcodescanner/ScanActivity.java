package com.interfacecomparateur.barcodescanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.google.zxing.Result;
import com.google.zxing.client.android.CaptureActivity;
import com.interfacecomparateur.classes.Produit;
import com.interfacecomparateur.classes.RequeteHTTP;
import com.interfacecomparateur.produit.AjoutProduitScanActivity;
import com.interfacecomparateur.produit.ProduitScanActivity;
import com.interfacecomparateur.slidemenu.SlideMenu;

/**
 * <b>Activité de scan de code-barres. Utilisée lorsque l'utilisateur veut chercher des informations sur un produit à partir de son code-barres.</b><br /><br />
 * Cette activité est appelée lorsque l'utilisateur clique sur l'item 'Scanner un code-barre' dans le SlideMenu.
 * 
 * @see SlideMenu
 * 
 * @author Equipe L3I1
 *
 */
public class ScanActivity extends CaptureActivity {
	
	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * EditText indiquant la valeur du code-barre retrouvée grâce au scan de celui-ci ou saisie manuellement par l'utilisateur.
	 */
	private EditText codeEditText;
	
	final Context context = this;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		identifiant = intent.getStringExtra("id");
		Log.d("identifiant","iden"+ identifiant);
		new SlideMenu(this,identifiant).displaySlideMenu();
		codeEditText = (EditText)findViewById(R.id.textfield_valeur);
		Button buttonSend = (Button)findViewById(R.id.button_send);
		buttonSend.setOnClickListener(new OnClickListener() {
			
	       public void onClick(View v) {
	          InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			  imm.hideSoftInputFromWindow(codeEditText.getWindowToken(), 0);
	    	  Thread thread = new Thread(new Runnable() {
	    	     
	    		 public void run() {
	    	        Looper.prepare();
	    	        searchProductByBarcode();
	    	     }
	    		 
	    	  });
	    	  thread.start();
	       }	
		});
	}
	
	/**
	 * Méthode définie dans la librairie CaptureActivity. Appelée lorsque un barre-code valide a été identifié.
	 * 
	 * @param rawResult Le contenu du code-barre
	 * @param barcode Une image bitmap en niveaux de gris des données de la caméra qui ont été décodées.
	 */
	@Override
	public void handleDecode(Result rawResult, Bitmap barcode) {
		codeEditText.setText(rawResult.getText());
		ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
		toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
	}
	
	private void searchProductByBarcode() {
	   final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(ScanActivity.this);
	   if (codeEditText.getText().toString().length()==13 && !codeEditText.getText().toString().contains(" ") && networkAvailable) {
	      String code = codeEditText.getText().toString();
		  final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
		  runOnUiThread(new Runnable() {
          	   
		     public void run() {
		        progressBar.setVisibility(View.VISIBLE);
		     }
		      
	      });
		  RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/product",new String[] {"product"},new String[]{code},identifiant);
		  final JSONObject jsonDocument = requete.sendGetRequest();
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
		           Toast.makeText(getApplicationContext(),"Echec",Toast.LENGTH_LONG).show();
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
				// Si le produit n'est pas référencé dans la base de données
			    if (result==null) {
			       runOnUiThread(new Runnable() {
			          
				      public void run() {
				    	 progressBar.setVisibility(View.INVISIBLE);
			             //Toast.makeText(getApplicationContext(),"Produit non référencé",Toast.LENGTH_LONG).show();
						   
			             AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			     			alertDialogBuilder.setTitle("Produit non référencé");
			     			alertDialogBuilder
			     				.setMessage("Souhaitez-vous l'ajouter à la base de données ?")
			     				.setCancelable(false)
			     				.setPositiveButton("Oui",new DialogInterface.OnClickListener() {
			     					public void onClick(DialogInterface dialog,int id) {
			     						Intent intent = new Intent(ScanActivity.this, AjoutProduitScanActivity.class);
			     						intent.putExtra("id", identifiant);
									    startActivity(intent);
			     					}
			     				  })
			     				.setNegativeButton("Non",new DialogInterface.OnClickListener() {
			     					public void onClick(DialogInterface dialog,int id) {
			     						dialog.cancel();
			     					}
			     				});
			      
			     				// create alert dialog
			     				AlertDialog alertDialog = alertDialogBuilder.create();
			      
			     				// show it
			     				alertDialog.show();
				      }
				      
			       });
			    }
			    else {
			       Produit produit=null;
			       try {
				      produit = new Produit(result.getString("product_name"),result.getString("product_ean"),result.getString("brand_name"),result.getString("product_img"),result.getString("product_descriptive"));
			       }
			       catch(JSONException e) {
			          Log.d("Err",e.getMessage());
			       }
		           Intent intent = new Intent(ScanActivity.this, ProduitScanActivity.class);
		           intent.putExtra("id",identifiant);
			       intent.putExtra("product", produit);
			       startActivity(intent);
			       finish();				       
			    }
			 }
		  }
	   }
	   else {
	      if (!networkAvailable) {
	         runOnUiThread(new Runnable() {
           	   
		        public void run() {
	               Toast.makeText(getApplicationContext(),"Réseau non disponible",Toast.LENGTH_SHORT).show();
		        }
		        
	         });
	      }
	      else {
	         runOnUiThread(new Runnable() {
           	   
		        public void run() {
	               Toast.makeText(getApplicationContext(),"Le code-barre doit être de longueur 13 et ne doit pas contenir d'espaces",Toast.LENGTH_LONG).show();
		        }
		        
	         });
	      }
	   }
	}

}