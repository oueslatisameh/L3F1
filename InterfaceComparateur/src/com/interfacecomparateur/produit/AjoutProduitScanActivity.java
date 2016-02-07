package com.interfacecomparateur.produit;

import com.example.interfacecomparateur.R;
import com.interfacecomparateur.profile.RechercheMagasinActivity;
import com.interfacecomparateur.slidemenu.SlideMenu;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * <b>Activité d'ajout d'un produit scanné non référencé dans la base de données</b>
 * 
 * @author Equipe L3F1
 *
 */
public class AjoutProduitScanActivity extends Activity implements OnClickListener {

	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;

	/**
	 * Context de l'activité qui fait appel à cette classe
	 */
	Context context = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ajout_produit_scan_vue);
        
        // Slide menu activable
        new SlideMenu(this,identifiant).displaySlideMenu();
        
		Intent intent = getIntent();
		identifiant = intent.getStringExtra("id");
		
        context = this;
        
        // Bouton pour rechercher magasin
        Button search = (Button) findViewById(R.id.button_search_shop);        
        search.setOnClickListener(this);
    }

    // Lors du clic sur le bouton de recherche
	public void onClick(View v) {
	    final EditText nameProduct = (EditText)findViewById(R.id.name_product);
	    final EditText nameManufacturer = (EditText)findViewById(R.id.name_manufacturer);
	    final EditText description = (EditText)findViewById(R.id.product_description);
	    final EditText price = (EditText)findViewById(R.id.product_price);
	    CheckBox promotionCheckBox = (CheckBox)findViewById(R.id.checkbox_promotion);
		
	    Intent intent = new Intent(AjoutProduitScanActivity.this,RechercheMagasinActivity.class);
	    intent.putExtra("id", identifiant);
		startActivityForResult(intent,0);
	}
}