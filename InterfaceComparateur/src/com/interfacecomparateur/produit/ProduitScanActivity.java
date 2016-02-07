package com.interfacecomparateur.produit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.Localisation;
import com.interfacecomparateur.classes.Magasin;
import com.interfacecomparateur.classes.Produit;
import com.interfacecomparateur.classes.RequeteHTTP;
import com.interfacecomparateur.map.CarteActivity;
import com.interfacecomparateur.profile.RechercheMagasinActivity;
import com.interfacecomparateur.slidemenu.SlideMenu;

/**
 * <b>Activité d'affichage des informations sur un produit scanné.</b>
 * 
 * @author Equipe L3I1
 *
 */
public class ProduitScanActivity extends Activity {
	
	/**
	 * Produit trouvé lorsque l'utilisateur a scanné son code-barres.
	 */
	private Produit produit;
	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * Bouton où sont affichées les informations sur le magasin le plus proche de la position de l'utilisateur.
	 * Si l'utilisateur clique sur ce bouton, il se retrouve dans une nouvelle activité où il peut chercher un autre magasin
	 * à proximité de sa position actuelle. Le magasin qu'il sélectionnera sera alors pris comme magasin de référence lors de l'ajout d'un prix
	 * pour le produit scanné.
	 * 
	 * @see RechercheMagasinActivity
	 */
	private Button magasinProposeButton;
	/**
	 * EditText où l'utilisateur peut entrer le prix qu'il souhaite proposer pour ce produit dans ce magasin.
	 */
	private EditText priceEditText;
	/**
	 * Magasin où l'utilisateur dit se trouver.
	 */
	private Magasin magasin;
	/**
	 * Image du produit. Si aucune image n'est disponible sur le serveur pour ce produit, une image par défaut est affichée.
	 */
	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.produit_scan_vue);
	   Intent intent = getIntent();
	   identifiant = intent.getStringExtra("id");
	   produit = (Produit)intent.getSerializableExtra("product");
	   new SlideMenu(this,identifiant).displaySlideMenu();
	   
	   final Handler handlerImage = new Handler() {
			  
	      @Override
		  public void handleMessage(Message msg) {
		     ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar1);
		   	 progressBar.setVisibility(View.GONE);
		  }
					   
	   };
	   final ImageView photoProduit = (ImageView)findViewById(R.id.photo_produit_imageview);
	   Thread thread = new Thread(new Runnable() {
	        
	      @Override
		  public void run() {
		     Looper.prepare();
		     bitmap = RequeteHTTP.loadImage(RequeteHTTP.PATH_PRODUCT_IMG+produit.getDirImage()+"/"+produit.getEan()+".jpg");
		  	 if (bitmap==null)
		  	    bitmap = BitmapFactory.decodeResource(ProduitScanActivity.this.getResources(),R.drawable.no_photo);
		  	 runOnUiThread(new Runnable() {
			         
		        @Override
		        public void run() {
		  		   photoProduit.setImageBitmap(bitmap);
		        }
		         
		  	 });
			 handlerImage.sendMessage(handlerImage.obtainMessage());
		  }
		         
	   });
	   thread.start();
	   
	   TextView nomProduit = (TextView)findViewById(R.id.nom_produit_textview);
	   nomProduit.setText(produit.getNom());
	   TextView nomFabriquant = (TextView)findViewById(R.id.nom_fabriquant_textview);
	   nomFabriquant.setText(produit.getNomFabricant());
	   TextView descriptif = (TextView)findViewById(R.id.description_produit_textview);
	   if (produit.getDescriptif().equals(""))
		  descriptif.setText("Aucune");
	   else
	      descriptif.setText(produit.getDescriptif());
	   priceEditText = (EditText)findViewById(R.id.edittext_prix_propose);
	   
	   Button ajoutList = (Button)findViewById(R.id.button_ajout_dans_liste);
	   ajoutList.setOnClickListener(new OnClickListener() {
		   
		   public void onClick(View v) {
		      Intent intent = new Intent(ProduitScanActivity.this,AjoutListActivity.class);
		      intent.putExtra("id",identifiant);
		      intent.putExtra("produit",produit.getEan());
		      startActivityForResult(intent,0);
		   }
		   
	   });
	   
	   ImageButton buttonList = (ImageButton)findViewById(R.id.button_list);
	   buttonList.setOnClickListener(new OnClickListener() {
			
	      public void onClick(View v) {
		     Intent intent = new Intent(ProduitScanActivity.this,ListResultActivity.class);
		     intent.putExtra("id",identifiant);
		     intent.putExtra("ean",ProduitScanActivity.this.produit.getEan());
		     startActivity(intent);
		  }
			
	   });
	   
	   ImageButton buttonMap = (ImageButton)findViewById(R.id.button_map);
	   buttonMap.setOnClickListener(new OnClickListener() {
			
	      public void onClick(View v) {
		     Intent intent = new Intent(ProduitScanActivity.this,CarteActivity.class);
		     intent.putExtra("id",identifiant);
		     intent.putExtra("ean",ProduitScanActivity.this.produit.getEan());
		     startActivity(intent);
		  }
			
	   });
	   
	   magasinProposeButton = (Button)findViewById(R.id.button_magasin_propose);
	   final Handler handlerShopButton = new Handler() {
			  
	      @Override
		  public void handleMessage(Message msg) {
		     ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar2);
			 progressBar.setVisibility(View.GONE);
			 magasinProposeButton.setVisibility(View.VISIBLE);
	      }
						   
	   };
	   thread = new Thread(new Runnable() {
	        
	      @Override
	      public void run() {
	         Looper.prepare();
	         /*
	  	      * Recuperation de la position de l'utilisateur pour l'envoyer au serveur afin qu'il puisse nous renvoyer les informations
	  	      * sur le magasin le plus proche de cette position. On affiche ensuit le nom de ce magasin dans le bouton.
	  	      */
	  	     Location l = Localisation.getLocation(ProduitScanActivity.this);
	         fillShopButton(new GeoPoint(l.getLatitude(),l.getLongitude()));
	         handlerShopButton.sendMessage(handlerShopButton.obtainMessage());
		  }
	      
	   });
	   thread.start();
	   
	   magasinProposeButton.setOnClickListener(new OnClickListener() {
		   
		   public void onClick(View v) {
		      Intent intent = new Intent(ProduitScanActivity.this,RechercheMagasinActivity.class);
		      intent.putExtra("id",identifiant);
		      intent.putExtra("fromScan",true);
		      startActivityForResult(intent,0);
		   }
		   
	   });
	   
	   Button proposerPrix = (Button)findViewById(R.id.button_ajouter_prix);
	   proposerPrix.setOnClickListener(new OnClickListener() {
		   
		   public void onClick(View v) {
			  if (!magasinProposeButton.getText().toString().equals("") && !priceEditText.getText().toString().equals("") && !priceEditText.getText().toString().contains(" ")) {
				 CheckBox promotionCheckBox = (CheckBox)findViewById(R.id.checkbox_promotion);
				 boolean isPromotion = promotionCheckBox.isChecked();
		         addPriceForAShop(isPromotion);
			  }
		   }
		   
	   });
	   
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	   String nomListe = data.getStringExtra("liste");
	   Magasin magasinChoisi = (Magasin)data.getSerializableExtra("shop");
	   if(resultCode==0) {
	      Toast.makeText(getApplicationContext(),"Echec lors de l'ajout du produit dans la liste \""+nomListe+"\".\nCe produit se trouve-t-il déjà dans la liste \""+nomListe+"\" ? ",Toast.LENGTH_LONG).show();
	   }
	   else if (resultCode==1) {
		  Toast.makeText(getApplicationContext(),"Produit ajouté dans la liste \""+nomListe+"\"",Toast.LENGTH_LONG).show();
	   }
	   else if(resultCode==3) {
		  Toast.makeText(getApplicationContext(),"Echec lors de la sélection du magasin",Toast.LENGTH_LONG).show();
	   }
	   else if (resultCode==4) {
		  magasin = magasinChoisi;
	      magasinProposeButton.setText(magasinChoisi.toString());
	   }
	   
	}
	
	/** 
	 * Envoi d'une requête GET au serveur afin de récupérer les informations sur le magasin le plus proche de la position de l'utilisateur.
	 * Ces informations sont ensuite affichées dans le bouton 'magasinProposeButton'.
	 * 
	 * @param userLocation  position actuelle de l'utilisateur
	 */
	private void fillShopButton(GeoPoint userLocation) {
       final TextView errorTextview = (TextView)findViewById(R.id.error_textview);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/shop/near",new String[] {"lat","lon"},new String[]{""+userLocation.getLatitude(),""+userLocation.getLongitude()},identifiant);
	   JSONObject jsonDocument = requete.sendGetRequest();
	   JSONArray errors = null,results=null;
	   String registered = null;
	   if (jsonDocument==null) {
	      Toast.makeText(getApplicationContext(),"Echec lors de la recherche du magasin le plus proche de votre position",Toast.LENGTH_LONG).show();
	   }
	   else {
	      try {
	         registered = jsonDocument.getString("registered");
	         if (jsonDocument.has("results")) {
	            results=jsonDocument.getJSONArray("results");
	         }
	         if (jsonDocument.has("err")) {
	            errors = jsonDocument.getJSONArray("err");
	        	errorTextview.setVisibility(View.VISIBLE);
			    StringBuffer sb = new StringBuffer();
			    for (int i=0;i<errors.length();i++)
			       sb.append(errors.get(i)+"\n");
			    errorTextview.setText(sb.toString());
	         }
		  }
		  catch(JSONException e) {
	         Log.d("Err",e.getMessage());
		  }
		  if(registered.equals("success")) {
			 Magasin magasinTrouve=null;
		     try {
		    	JSONObject result = results.getJSONObject(0);
			    magasinTrouve = new Magasin(result.getString("id"),result.getString("name"),
			    		                    result.getString("descriptive"),result.getString("type"),
			    		                    result.getDouble("latitude"),result.getDouble("longitude"),
			    		                    result.getString("numero"),result.getString("rue"),
			    		                    result.getString("ville"),result.getString("code_postal"));
		     }
		     catch(JSONException e) {
		        Log.d("Err",e.getMessage());
			 }
		     magasin = magasinTrouve;
		     runOnUiThread(new Runnable() {
		         
			    @Override
			    public void run() {
		           magasinProposeButton.setText(magasin.toString());
		           errorTextview.setVisibility(View.GONE);
			    }
			    
		     });
	      }
	   }
	}
	
	/**
	 * Envoi d'une requête POST au serveur afin de transmettre le prix proposé par l'utilisateur pour ce produit dans le magasin proposé (ou choisi).
	 * 
	 * @param isPromotion  vrai si le produit a été indiqué comme étant en promotion, faux sinon.
	 */
	private void addPriceForAShop(boolean isPromotion) {
	   TextView errorTextview = (TextView)findViewById(R.id.error_textview);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/product/"+produit.getEan()+"/shop/"+magasin.getId()+"/price/"+priceEditText.getText().toString()+"-"+isPromotion,new String[] {},new String[]{},identifiant);
	   JSONObject jsonDocument = requete.sendPostRequest();
	   JSONArray errors = null;
	   String registered = null;
	   if (jsonDocument==null) {
	      Toast.makeText(getApplicationContext(),"Echec lors de l'ajout du prix",Toast.LENGTH_LONG).show();
	   }
	   else {
	      try {
		     registered = jsonDocument.getString("registered");
		     if (jsonDocument.has("err")) {
		        errors = jsonDocument.getJSONArray("err");
		        errorTextview.setVisibility(View.VISIBLE);
				StringBuffer sb = new StringBuffer();
				for (int i=0;i<errors.length();i++)
				   sb.append(errors.get(i)+"\n");
				errorTextview.setText(sb.toString());
		     }
		  }
		  catch(JSONException e) {
		     Log.d("Err",e.getMessage());
	      }
		  if(registered.equals("success")) {
		     Toast.makeText(getApplicationContext(),"Votre prix pour \""+produit.getNom()+"\" dans le magasin \""+magasin.getNom()+"\" a bien été enregistré",Toast.LENGTH_LONG).show();
		     errorTextview.setVisibility(View.GONE);
		  }
	   }
	}
	
}