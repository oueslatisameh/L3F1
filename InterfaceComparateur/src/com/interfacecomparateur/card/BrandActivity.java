package com.interfacecomparateur.card;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.RequeteHTTP;

/**
 * <b>Activité de recherche d'enseignes.</b>
 * 
 * Cette activité est appelée lorsque l'utilisateur souhaite ajouter une carte de fidélité.<br />
 * Il peut alors rechercher une enseigne en entrant son nom.
 * 
 * @author Equipe L3I1
 *
 */
public class BrandActivity extends Activity implements OnItemClickListener {
	
	/**
	 * ListView où sont affichés les résultats de la recherche
	 * 
	 * @see ListView
	 */
	private ListView listView;
	/**
	 * ArrayAdapter pour la ListView servant à afficher les résultats de la recherche
	 * 
	 * @see ArrayAdapter
	 */
	private ArrayAdapter<String> arrayAdapter;
	/**
	 * ArrayList contenant les noms des enseignes résultants de la recherche
	 */
	private ArrayList<String> values;
	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté
	 */
	private String identifiant;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.card_brand_vue);
	   Intent intent = getIntent();
	   identifiant = intent.getStringExtra("id");
	   values = new ArrayList<String>();
	   
	   listView = (ListView)findViewById(R.id.listview_brands);
	   arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
	   listView.setAdapter(arrayAdapter);
	   
	   final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
	   final EditText searchEditText = (EditText)findViewById(R.id.textfield_valeur);
	   final Button buttonSend = (Button)findViewById(R.id.button_send);
	   buttonSend.setOnClickListener(new OnClickListener() {
		  
	      public void onClick(View v) {
	    	 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		     imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
	         final Handler handler = new Handler() {
				  
			    @Override
			    public void handleMessage(Message msg) {
			       progressBar.setVisibility(View.GONE);
				   listView.setVisibility(View.VISIBLE);
				}
								   
			 };
			 progressBar.setVisibility(View.VISIBLE);
			 listView.setVisibility(View.GONE);
			 Thread thread = new Thread(new Runnable() {
				        
			    @Override
			    public void run() {
			       Looper.prepare();
				   getBrandResults(searchEditText.getText().toString());
				   handler.sendMessage(handler.obtainMessage());
				}
					         
			 });
		     thread.start();
	      }
		     
	   });
	   
	   searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		   
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		            buttonSend.performClick();
		            return true;
		        }
		        return false;
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
	 * Envoi d'un requête GET au serveur afin de récupérer les résultats de la recherche effectuée à partir de la chaîne de caractères fournie.
	 * Ces résultats sont ensuite affichés à l'écran pour l'utilisateur.
	 * 
	 * @param search nom d'enseigne entré par l'utilisateur pour la recherche
	 */
	private void getBrandResults(String search) {
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   if (!search.equals("")) {
	      RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/brand",new String[] {"brand"},new String[]{search.replace(" ","_")},identifiant);
	      final JSONObject jsonDocument = requete.sendGetRequest();
		  JSONArray results=null;
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
	               Toast.makeText(getApplicationContext(),"Echec lors de la recherche de magasin",Toast.LENGTH_LONG).show();
			    }
			    
	   	     });
		  }
	   	  else {
		     try {
	            registered = jsonDocument.getString("registered");
	            if (jsonDocument.has("results")) {
	               results=jsonDocument.getJSONArray("results");
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
			    values.clear();
	            try {
	               if (results.length()==0) {
	        	      values.add("Pas de résultat(s)");
	        	      listView.setOnItemClickListener(null);
	               }
	               else {
			          for (int i=0;i<results.length();i++) {
			             values.add(results.getJSONObject(i).getString("brand_name"));
			          }
			          listView.setOnItemClickListener(BrandActivity.this);
	               }
	               runOnUiThread(new Runnable() {
	     		         
	      		      @Override
	      		      public void run() {
	      		    	 errorTextView.setVisibility(View.GONE);
	                     arrayAdapter.notifyDataSetChanged();
	      		      }
	      		      
	               });
	            }
	            catch(JSONException e) {
	               Log.d("Err",e.getMessage());
	            }
	         }
	      }
	   }
	}

	/**
	 * Lorsque l'utilisateur clique sur un item, il passe à l'activité suivante pour scanner le code-barres de la carte de fidélité
	 * qu'il souhaite ajouter.
	 * 
	 * @see CardScanActivity
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String nomEnseigne = ((TextView)view).getText().toString();
		Intent intent = new Intent(BrandActivity.this,CardScanActivity.class);
		intent.putExtra("brand",nomEnseigne);
		intent.putExtra("id",identifiant);
		SharedPreferences preferences = getApplicationContext().getSharedPreferences("name_"+identifiant,0);
		intent.putExtra("name", preferences.getString("name",""));
		startActivity(intent);
		finish();
	}
	    
}
