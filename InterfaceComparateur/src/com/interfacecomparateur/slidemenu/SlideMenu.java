package com.interfacecomparateur.slidemenu;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.Accueil;
import com.interfacecomparateur.barcodescanner.ScanActivity;
import com.interfacecomparateur.card.CardActivity;
import com.interfacecomparateur.list.ListActivity;
import com.interfacecomparateur.produit.AjoutProduitScanActivity;
import com.interfacecomparateur.profile.ProfilActivity;
import com.interfacecomparateur.search.RechercheActivity;

public class SlideMenu {
	
	private Activity activity;
	private String identifiant;
	private EditText editTextSearch;
	
	public SlideMenu(Activity activity, String identifiant) {
	   this.activity=activity;
	   this.identifiant=identifiant;
	}
	
	public void displaySlideMenu() {
	   LayoutInflater inflater = LayoutInflater.from(activity);
	   MyHorizontalScrollView scrollView = (MyHorizontalScrollView)inflater.inflate(R.layout.horz_scroll_with_list_menu, null);
	   activity.setContentView(scrollView);
	   String className = activity.getClass().getSimpleName();
	   View activity_layout = null;
	   if (className.equals("ScanActivity"))
	      activity_layout = inflater.inflate(R.layout.scan_vue, null);
	   else if (className.equals("ProduitScanActivity"))
		      activity_layout = inflater.inflate(R.layout.produit_scan_vue, null);
	   else if (className.equals("CardActivity"))
		      activity_layout = inflater.inflate(R.layout.card_gridview_vue, null);
	   else if (className.equals("ProfilActivity"))
		      activity_layout = inflater.inflate(R.layout.profil_vue, null);
	   else if (className.equals("RechercheActivity"))
		      activity_layout = inflater.inflate(R.layout.recherche_vue, null);
	   else if (className.equals("ListActivity"))
		      activity_layout = inflater.inflate(R.layout.liste_vue, null);
	   else if (className.equals("AjoutProduitScanActivity"))
		   	  activity_layout=(inflater.inflate(R.layout.ajout_produit_scan_vue, null));
	   View menu = inflater.inflate(R.layout.horz_scroll_menu, null);
	   ListView listView = (ListView)menu.findViewById(R.id.list);
	   ViewUtils.initListView(activity, listView, 5, android.R.layout.simple_list_item_1);
	   listView.setOnItemClickListener(new ClickListener());
	   ImageButton buttonMenu = (ImageButton)activity_layout.findViewById(R.id.button_menu);
	   buttonMenu.setOnClickListener(new ClickListenerForScrolling(scrollView, menu));

	   final View[] children = new View[] {menu,activity_layout};

	   int scrollToViewIdx = 1;
	   scrollView.initViews(children, scrollToViewIdx, new SizeCallbackForMenu(buttonMenu));
	   
	   editTextSearch = (EditText)((ViewGroup)(((ViewGroup)menu).getChildAt(0))).getChildAt(0);
	   
	   final Button buttonSend = (Button)((ViewGroup)menu).getChildAt(1);
	   buttonSend.setOnClickListener(new OnClickListener() {
		   
		   public void onClick(View v) {
		      Intent intent = new Intent(activity,RechercheActivity.class);
		      intent.putExtra("id", identifiant);
		      intent.putExtra("search", editTextSearch.getText().toString());
		      activity.startActivity(intent);
		      activity.finish();
		   }
	   });
	   
	   editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		   
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		            buttonSend.performClick();
		            return true;
		        }
		        return false;
		    }
		    
		});
	}
	
	class ClickListener implements OnItemClickListener {
		
       @Override
	   public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    	  Intent intent = null;
    	  switch (arg2) {
    	     case(0) : intent = new Intent(activity,ScanActivity.class); break;
    	     case(1) : intent = new Intent(activity,ListActivity.class); break;
    	     case(2) : intent = new Intent(activity,CardActivity.class); break;
    	     case(3) : intent = new Intent(activity,ProfilActivity.class); break;
    	     case(4) : intent = new Intent(activity,Accueil.class); break;
    	     case(5) : intent = new Intent(activity,AjoutProduitScanActivity.class); 
    	               intent.putExtra("logout", true); 
    	               break;
    	  }
    	  intent.putExtra("id", SlideMenu.this.identifiant);
    	  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		  activity.startActivity(intent);
		  activity.finish();
	   } 
		 
	}

}
