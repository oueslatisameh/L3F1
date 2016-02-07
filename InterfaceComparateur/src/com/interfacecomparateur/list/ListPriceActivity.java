package com.interfacecomparateur.list;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.*;
import com.interfacecomparateur.slidemenu.SlideMenu;

public class ListPriceActivity extends Activity {
	
	// Récuperation de l'addresse email de la personne s'il est connecter
	 private String identifiant;
	 
	// Variable pour instancier et utiliser l'ensemble des méthodes
	private Calculprix cp;
	
	// Variable lpa pour récuperer certaines méthodes avec le produit 
	private ListProduitActivity lpa;
	private ListView lv ;
	private ListActivity listname;
	
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		setContentView (R.layout.list_with_price);
		Intent intent = getIntent();
		identifiant = intent.getStringExtra("id");
		listname =new ListActivity();
		lv =(ListView)findViewById(R.id.lV);
		new SlideMenu(this,identifiant).displaySlideMenu();
		final Handler handler = new Handler() {
			  
			   @Override
			   public void handleMessage(Message msg) {
			      ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
			      progressBar.setVisibility(View.GONE);
			   }
								   
			};
			Thread thread = new Thread(new Runnable() {
		        
				   @Override
				   public void run() {
				      Looper.prepare();
					  listname.getLists();
					  lpa.getProductsInList();
					  handler.sendMessage(handler.obtainMessage());
				   }
						         
				});
				thread.start();
				ImageButton buttonAdd = (ImageButton)findViewById(R.id.button_add);
				buttonAdd.setOnClickListener(new OnClickListener() {
					
					 public void onClick(View v) {
						 LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						 lv=(ListView) inflater.inflate(R.layout.profil_dialog_vue,(ViewGroup)findViewById(R.id.dialog_value));
			}
		});
	}
}
