package com.interfacecomparateur.list;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.Calculprix;
import com.interfacecomparateur.classes.GenericAdapter;
import com.interfacecomparateur.classes.Liste;
import com.interfacecomparateur.classes.RequeteHTTP;
import com.interfacecomparateur.classes.SwipeDismissListViewTouchListener;
import com.interfacecomparateur.slidemenu.SlideMenu;

/**
 * <b>Activité d'affichage des listes de courses de l'utilisateur.</b><br /><br />
 * Cette activité est appelée lorsque l'utilisateur clique sur l'item 'Mes listes de courses' dans le SlideMenu.
 * 
 * @author Equipe L3I1
 *
 */
public class ListActivity extends Activity {

	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
    private String identifiant;
    /**
	 * GenericAdapter associé à la ListView permettant d'afficher les noms des listes de courses.
	 * 
	 * @see GenericAdapter
	 */
	private GenericAdapter	adapter = null;
	/**
	 * Liste des listes de courses de l'utilisateur.
	 */
	private List<Liste> listesDeCourses;
	/**
	 * ListView permettant d'afficher les différentes listes de courses de l'utilisateur.
	 * 
	 * @see ListView
	 */
	private ListView listview_listesDeCourses;
	/**
	 * EditText utilisé dans la boîte de dialogue qui s'affiche lors de la création d'une nouvelle liste ou lors
	 * du renommage d'une liste.
	 */
	private EditText dialogEditText;
	
	private Calculprix cp;
	
	private double prix;
	
	private JSONObject jo;
	
	/**
	 * Integer utilisé pour avoir la quantité au sein d'une liste
	 */
	private int quant;
	
	/**
	 * Integer utilisé pour avoir le prix d'un produit
	 */
	private double pt;
	
	/**
	 * Integer utilisé pour l'ensemble des méthodes présentes dans la classe ListProduitActivity
	 */
	private ListProduitActivity lpa;
	
	/**
	 * Liste utilisée pour afficher un message indiquant que l'utilisateur ne possède aucune liste.
	 */
	private Liste listeVide;
	
	/**
	 * Version de l'API Android de l'appareil.
	 */
	private final int sdk = Build.VERSION.SDK_INT;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		setContentView (R.layout.liste_vue);
		
	    Intent intent = getIntent();
		identifiant = intent.getStringExtra("id");
		new SlideMenu(this,identifiant).displaySlideMenu();
		
	    listeVide = new Liste("Pas de liste enregistrée");
	    
		listesDeCourses = new ArrayList<Liste>();
		adapter = new GenericAdapter(this,listesDeCourses);
		listview_listesDeCourses = (ListView)findViewById(R.id.listView);
		
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
			  getLists();
			  handler.sendMessage(handler.obtainMessage());
		   }
				         
		});
		thread.start();
			
		ImageButton buttonAdd = (ImageButton)findViewById(R.id.button_add);
		buttonAdd.setOnClickListener(new OnClickListener() {
			
		   public void onClick(View v) {
		      LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	  dialogEditText = (EditText)inflater.inflate(R.layout.profil_dialog_vue,(ViewGroup)findViewById(R.id.dialog_value));
	    		
	    	  AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
	    	  builder.setCancelable(true)
	    	  .setTitle("Nom de la nouvelle liste : ")
	    	  .setView(dialogEditText)
	    	  .setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
				    			
			     public void onClick(DialogInterface dialog, int id) {
			        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
				    progressBar.setVisibility(View.VISIBLE);
				    listview_listesDeCourses.setVisibility(View.GONE);
				    Thread thread = new Thread(new Runnable() {
				              
				       @Override
				       public void run() {
				          Looper.prepare();
				          addNewList(dialogEditText.getText().toString()); 
				       }
				      	         
				    });
				    thread.start();
				 }
				    			   
		      })
		      .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
				    				
			     public void onClick(DialogInterface dialog, int id) {
				    dialog.dismiss();
				 }
				    			   
		      });
	    	  builder.create().show();
		   }
		});
		
	}
	
	/**
	 * Méthode invoquée lorsque l'utilisateur clique sur l'un des items de la ListView.
	 * Cette action lancera une nouvelle activité où l'utilisateur pourra voir les produits contenus
	 * dans la liste de courses correspondante.
	 * 
	 * @author Equipe L3I1
	 * 
	 * @see OnItemClickListener
	 *
	 */
	class OnListItemClickListener implements OnItemClickListener {

       public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    	  
	      Liste liste=(Liste)adapter.getItem(arg2);
	      //Calculprix cp=(Calculprix)adapter.getItem(arg4);
	      Intent intent = new Intent(ListActivity.this,ListProduitActivity.class);
		  intent.putExtra("id", identifiant);
		  intent.putExtra("liste", liste.getNom());
		  intent.putExtra("position", arg2);
		  startActivity(intent);
	   }
       
	}
	
	/**
	 * Méthode invoquée lorsque l'utilisateur clique et maintient son appui sur l'un des items de la ListView.
	 * Cette action affichera une boîte de dialogue : l'utilisateur pourra choisir de renommer ou de supprimer
	 * la liste de courses correspondante.
	 * 
	 * @author Equipe L3I1
	 * 
	 * @see OnItemLongClickListener
	 *
	 */
	class OnListItemLongClickListener implements OnItemLongClickListener {
		
       public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
    	   
	      // Récupération de l'élément sur lequel on a fait un appui long
	      final Liste liste = (Liste)adapter.getItem(arg2);
	      final int position = arg2;
	      AlertDialog.Builder builderChoices = new AlertDialog.Builder(ListActivity.this);
		  final CharSequence[] items =  {"Renommer la liste","Supprimer la liste","Afficher le prix du panier"};

		  builderChoices.setItems(items, new DialogInterface.OnClickListener() {

		     @Override
	         public void onClick(DialogInterface dialog, int which) {
		        if (which==0) {
			       LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			       dialogEditText = (EditText)inflater.inflate(R.layout.profil_dialog_vue,(ViewGroup)findViewById(R.id.dialog_value));
			    		
			       AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
			       builder.setCancelable(true)
			       // modifier le nom de la liste
			       .setTitle("Entrer le nouveau nom : ")
			       .setView(dialogEditText)
			       .setPositiveButton("Editer", new DialogInterface.OnClickListener() {
			    			
			          public void onClick(DialogInterface dialog, int id) {
			             ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
						 progressBar.setVisibility(View.VISIBLE);
						 listview_listesDeCourses.setVisibility(View.GONE);
						 Thread thread = new Thread(new Runnable() {
						              
						    @Override
						    public void run() {
						       Looper.prepare();
						       editListName(liste,position,dialogEditText.getText().toString());
						    }
						      	         
						 });
						 thread.start(); 
			          }
			    			   
			       })
			       .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
			    				
			          public void onClick(DialogInterface dialog, int id) {
			             dialog.dismiss();
			          }
			    			   
			       });
			       builder.create().show();
			    }
			    else if(which==1) {
			    	
			       // Supprimer une liste de course
			       AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
				   builder.setCancelable(true)
				   .setTitle("Confirmation")
				   .setMessage("Voulez-vous supprimer la liste de produits ? ")
				   .setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
				    			
				      public void onClick(DialogInterface dialog, int id) {
				         ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
						 progressBar.setVisibility(View.VISIBLE);
						 listview_listesDeCourses.setVisibility(View.GONE);
						 Thread thread = new Thread(new Runnable() {
						              
						    @Override
						    public void run() {
						       Looper.prepare();
						       deleteList(liste);
						    }
						      	         
						 });
						 thread.start();
				      }
				    			   
				   })
				   .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
				    				
				      public void onClick(DialogInterface dialog, int id) {
				         dialog.dismiss();
				      }
				    			   
				   });
				   builder.create().show();
		        }
			    else{
					ProgressBar progressbar=(ProgressBar)findViewById(R.id.progressBar);
					progressbar.setVisibility(View.VISIBLE);
					
					lpa = new ListProduitActivity();
					quant = listesDeCourses.size();
					prix = listesDeCourses.size();
					pt = listesDeCourses.size();
					RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/listname",new String[]{},new String[]{},identifiant);
					@SuppressWarnings("unused")
					final JSONObject jsonDocument = requete.sendGetRequest();
					
					cp = new Calculprix(jo,prix,quant,pt);
					
					Thread thread = new Thread(new Runnable() {
			              
						    @Override
						    public void run() {
						       Looper.prepare();
						       afficheList(cp);
						       
						    }
						      	         
						 });
					 thread.start();
			    }
	         }
	      });
	      builderChoices.show();
	      return true;
	   }
	}
	
	/**
	 * Envoi d'une requête GET au serveur afin de récupérer les noms des listes de courses de l'utilisateur.
	 * Ces listes sont ensuite affichées dans la ListView.
	 */
	public void getLists() {
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/listname",new String[]{},new String[]{},identifiant);
	   final JSONObject jsonDocument = requete.sendGetRequest();
	   String registered = null;
	   JSONArray results = null;
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
		        Toast.makeText(getApplicationContext(),"Echec lors de la récupération des listes",Toast.LENGTH_LONG).show();
		     }
		     
	      });
	   }
	   else {
	      try {
		     registered = jsonDocument.getString("registered");
		     if (jsonDocument.has("results")) {
		        results = jsonDocument.getJSONArray("results");
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
	         if (results.length()==0) {
	            listesDeCourses.add(listeVide);
	         }
		     for (int i=0;i<results.length();i++) {
		        try {
		           listesDeCourses.add(new Liste(results.getJSONObject(i).getString("name")));
		        }
		        catch(JSONException e) {
			       Log.d("Err",e.getMessage());
			    }
		     }
		     runOnUiThread(new Runnable() {
		         
			    @Override
			    public void run() {
	               listview_listesDeCourses.setAdapter(adapter);
			    }
			    
		     });
	         if (results.length()!=0) {
	            listview_listesDeCourses.setOnItemClickListener(new OnListItemClickListener());
	            listview_listesDeCourses.setOnItemLongClickListener(new OnListItemLongClickListener());
	            if(sdk>=12) {
	               listview_listesDeCourses.setOnScrollListener(new OnScroll().touchListener.makeScrollListener());
	               listview_listesDeCourses.setOnTouchListener(new OnScroll().touchListener);
	            }
	         }
	      }
	   }
	}
	
	/**
	 * Envoi d'une requête DELETE au serveur afin de supprimer une liste de courses.
	 * 
	 * @param liste  la liste de courses que l'utilisateur souhaite supprimer
	 */
	private void deleteList(final Liste liste) {
       final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
       boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/list/"+liste.getNom().replace(" ","_"),new String[]{},new String[]{},identifiant);
	   final JSONObject jsonDocument = requete.sendDeleteRequest();
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
	            Toast.makeText(getApplicationContext(),"Echec lors de la suppression de la liste",Toast.LENGTH_LONG).show();
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
	         listesDeCourses.remove(liste);
		     if (listesDeCourses.size()==0) {
		        listesDeCourses.add(listeVide);
		        listview_listesDeCourses.setOnItemClickListener(null);
		        listview_listesDeCourses.setOnItemLongClickListener(null);
		        if(sdk>=12){
		           listview_listesDeCourses.setOnScrollListener(null);
	               listview_listesDeCourses.setOnTouchListener(null);
		        }
		     }
		     runOnUiThread(new Runnable() {
				 
		        public void run() {
		           adapter.notifyDataSetChanged();
				   ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
				   progressBar.setVisibility(View.GONE);
				   errorTextView.setVisibility(View.GONE);
				   listview_listesDeCourses.setVisibility(View.VISIBLE);
				   Toast.makeText(getApplicationContext(),"Liste \""+liste.getNom()+"\" supprimée",Toast.LENGTH_LONG).show();
				}
				    
		     });
		  }
	   }
	}
	
	/**
	 * Affiche la liste avec le prix
	 */
	private void afficheList(final Calculprix l){
		final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
		   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
		   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/list/",new String[]{},new String[]{},identifiant);
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
			            Toast.makeText(getApplicationContext(),"Echec lors de la récuperation de la liste",Toast.LENGTH_LONG).show();
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
			   
		   
		   if(registered.equals("success")){
			   
			  
			   if(listesDeCourses.isEmpty())
				    lpa.getProductsInList();
			   		getLists();
			   		cp.getJo(jsonDocument);
			   		pt=cp.getPrix_total();
			   	 listview_listesDeCourses.setOnItemClickListener(new OnListItemClickListener());
				 listview_listesDeCourses.setOnItemLongClickListener(new OnListItemLongClickListener());
		   }
		   
		   }
		   }
		   
		
	
	/** 
	 * Envoi d'une requête PUT au serveur afin de renommer une liste de courses.
	 * 
	 * @param liste la liste de courses que l'utilisateur souhaite renommer
	 * @param position la position de la liste dans la ListView
	 * @param newListName le nouveau nom de la liste donné par l'utilisateur
	 */
	private void editListName(Liste liste,int position,String newListName) {
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/list/"+liste.getNom().replace(" ","_")+"-"+newListName.replace(" ","_"),new String[]{},new String[]{},identifiant);
	   final JSONObject jsonDocument = requete.sendPutRequest();
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
	            Toast.makeText(getApplicationContext(),"Echec lors de la modification du nom de la liste",Toast.LENGTH_LONG).show();
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
		     listesDeCourses.get(position).setNom(newListName);
		     runOnUiThread(new Runnable() {
				 
			    public void run() {
			       adapter.notifyDataSetChanged();
			       ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
				   progressBar.setVisibility(View.GONE);
			       errorTextView.setVisibility(View.GONE);
			       listview_listesDeCourses.setVisibility(View.VISIBLE);
			       Toast.makeText(getApplicationContext(),"Nom de la liste modifié",Toast.LENGTH_LONG).show();
			    }
		     });
	      }
	   }	
    }
	
	/**
	 * Envoi d'une requête POST au serveur afin de créer une nouvelle liste de courses.
	 * 
	 * @param newListName  nom de la nouvelle liste de courses donné par l'utilisateur
	 */
	private void addNewList(final String newListName) {
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/list/"+newListName.replace(" ","_"),new String[]{},new String[]{},identifiant);
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
	            Toast.makeText(getApplicationContext(),"Echec lors de la création de la liste",Toast.LENGTH_LONG).show();
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
			 if (listesDeCourses.contains(listeVide))
			    listesDeCourses.remove(listeVide);
		     listesDeCourses.add(new Liste(newListName));
			 listview_listesDeCourses.setOnItemClickListener(new OnListItemClickListener());
			 listview_listesDeCourses.setOnItemLongClickListener(new OnListItemLongClickListener());
			 if(sdk>=12){
			    listview_listesDeCourses.setOnScrollListener(new OnScroll().touchListener.makeScrollListener());
	            listview_listesDeCourses.setOnTouchListener(new OnScroll().touchListener);
			 }
			 runOnUiThread(new Runnable() {
				 
		        public void run() {
				   adapter.notifyDataSetChanged();
				   ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
				   progressBar.setVisibility(View.GONE);
				   errorTextView.setVisibility(View.GONE);
				   listview_listesDeCourses.setVisibility(View.VISIBLE);
				   Toast.makeText(getApplicationContext(),"Liste \""+newListName+"\" créée",Toast.LENGTH_LONG).show();
				}
				    
		     });
	      }
	   }	
	}
	
    /**
     * Classe permettant d'implémenter une nouvelle façon de supprimer une liste de courses.
     * L'utilisateur peut supprimer une liste en scrollant l'item de la ListView correspondant 
     * à la liste qu'il souhaite supprimer.
     * 
     * @author Equipe L3I1
     * 
     * @since API level 12
     *
     */
	class OnScroll {

       SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
	      listview_listesDeCourses,
		  new SwipeDismissListViewTouchListener.DismissCallbacks() {

		     @Override
			 public boolean canDismiss(int position) {
			    return true;
			 }

			 @Override
			 public void onDismiss(ListView listView, int[] reverseSortedPositions) {
			    for (int position : reverseSortedPositions) {
				   deleteList((Liste) adapter.getItem(position));
				}
				adapter.notifyDataSetChanged();
			 }
	   });
	}
}