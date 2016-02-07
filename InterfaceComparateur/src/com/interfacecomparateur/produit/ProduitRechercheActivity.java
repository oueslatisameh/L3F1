package com.interfacecomparateur.produit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.Produit;
import com.interfacecomparateur.classes.RequeteHTTP;
import com.interfacecomparateur.map.CarteActivity;

/**
 * <b>Activité d'affichage des informations sur un produit recherché ou sur un produit qui se trouve dans une liste de courses.</b>
 * 
 * @author Equipe L3I1
 *
 */
public class ProduitRechercheActivity  extends Activity {

	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * Produit consulté par l'utilisateur et qui est parmi les résultats de la recherche effectuée.
	 */
	private Produit produit;
	/**
	 * Image du produit. Si aucune image n'est disponible sur le serveur pour ce produit, une image par défaut est affichée.
	 */
	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.produit_recherche_vue);
		Intent intent = getIntent();
		identifiant = intent.getStringExtra("id");
		produit = (Produit)intent.getSerializableExtra("product");
		if (intent.getBooleanExtra("fromList",false)) {
			Button ajoutList = (Button)findViewById(R.id.button_ajout_dans_liste);
			ajoutList.setVisibility(Button.GONE);
		}
		
		final Handler handler = new Handler() {
			  
		   @Override
		   public void handleMessage(Message msg) {
		      ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
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
		  	     bitmap = BitmapFactory.decodeResource(ProduitRechercheActivity.this.getResources(),R.drawable.no_photo);
		  	  runOnUiThread(new Runnable() {
			         
		         @Override
		         public void run() {
		  		    photoProduit.setImageBitmap(bitmap);
		         }
		         
		  	  });
			  handler.sendMessage(handler.obtainMessage());
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

		Button ajoutList = (Button)findViewById(R.id.button_ajout_dans_liste);
		ajoutList.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(ProduitRechercheActivity.this,AjoutListActivity.class);
				intent.putExtra("id",identifiant);
				intent.putExtra("produit",produit.getEan());
				startActivityForResult(intent,0);
			}

		});

		ImageButton buttonList = (ImageButton)findViewById(R.id.button_list);
		buttonList.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(ProduitRechercheActivity.this,ListResultActivity.class);
				intent.putExtra("id",identifiant);
				intent.putExtra("ean",ProduitRechercheActivity.this.produit.getEan());
				startActivity(intent);
			}

		});

		ImageButton buttonMap = (ImageButton)findViewById(R.id.button_map);
		buttonMap.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(ProduitRechercheActivity.this,CarteActivity.class);
				intent.putExtra("id",identifiant);
				intent.putExtra("ean",ProduitRechercheActivity.this.produit.getEan());
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String nomListe = data.getStringExtra("liste");
		if(resultCode==0) {
			Toast.makeText(getApplicationContext(),"Echec lors de l'ajout du produit dans la liste \""+nomListe+"\".\nCe produit se trouve-t-il déjà dans la liste \""+nomListe+"\" ? ",Toast.LENGTH_LONG).show();
		}
		else if (resultCode==1) {
			Toast.makeText(getApplicationContext(),"Produit ajouté dans la liste \""+nomListe+"\"",Toast.LENGTH_LONG).show();
		}
	}

}