package com.interfacecomparateur.card;

import java.util.EnumMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.interfacecomparateur.classes.RequeteHTTP;

/**
 * <b>Activité d'affichage du code-barres d'une carte de fidélité.</b>
 * 
 * @author Equipe L3I1
 *
 */
public class BarcodeCardActivity extends Activity {
	
	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté
	 */
	private String identifiant;
	/**
	 * Nom de l'enseigne de la carte de fidélité affichée
	 */
	private String nomEnseigne;
	/**
	 * BSIN (Brand Standard Identification Number) de l'enseigne de la carte de fidélité affichée
	 */
	private String bsin;
	/**
	 * Code-barres de la carte de fidélité affichée
	 */
	private String codeCarte;
	private ImageView imageViewCode;
	private TextView textViewCode;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.card_barcode_vue);
	   
	   Intent intent = getIntent();
	   identifiant = intent.getStringExtra("id");
	   nomEnseigne = intent.getStringExtra("brand");
	   bsin = intent.getStringExtra("bsin");
	   codeCarte = intent.getStringExtra("code");
	   
	   TextView nomEnseigneTextView = (TextView)findViewById(R.id.card_brand_name);
	   nomEnseigneTextView.setText(""+nomEnseigne);
	   
	   imageViewCode = (ImageView)findViewById(R.id.card_barcode_imageview);
	   textViewCode = (TextView)findViewById(R.id.card_barcode_textview);
	   
	   generateBarcodeBitmap(codeCarte);
	   
	   ImageButton buttonBack = (ImageButton)findViewById(R.id.button_back);
	   buttonBack.setOnClickListener(new OnClickListener() {
			
	      public void onClick(View v) {
		     Intent intent = new Intent(BarcodeCardActivity.this,CardActivity.class);
			 intent.putExtra("id", identifiant);
			 startActivity(intent);
			 finish();
	      }
			
	   });
	   
	   ImageButton buttonDelete = (ImageButton)findViewById(R.id.button_delete);
	   buttonDelete.setOnClickListener(new OnClickListener() {
		  
		  public void onClick(View v) {
			 imageViewCode.setVisibility(View.GONE);
		     textViewCode.setVisibility(View.GONE);
			 ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
			 progressBar.setVisibility(View.VISIBLE);
		     Thread thread = new Thread(new Runnable() {
					        
			    @Override
				public void run() {
				   Looper.prepare();
				   deleteCard();
				}
						         
			 });
		     thread.start();
		  }
		  
	   });
	   
	   ImageButton buttonEdit = (ImageButton)findViewById(R.id.button_edit);
	   buttonEdit.setOnClickListener(new OnClickListener() {
		  
		  public void onClick(View v) { 
		     Intent intent = new Intent(BarcodeCardActivity.this,CardScanActivity.class);
		     intent.putExtra("editCode",true);
		     startActivityForResult(intent,0);
		  }
		  
	   });
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	   codeCarte = data.getStringExtra("newCode");
	   if (resultCode==1) {
	      final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
	      final Handler handler = new Handler() {
			  
	         @Override
	         public void handleMessage(Message msg) {
		        progressBar.setVisibility(View.GONE);
		     }
							   
	      };
	      imageViewCode.setVisibility(View.GONE);
	      textViewCode.setVisibility(View.GONE);
	      progressBar.setVisibility(View.VISIBLE);
	      Thread thread = new Thread(new Runnable() {
		          
	         @Override
		     public void run() {
		        Looper.prepare();
		        editCardCode(); 
		        handler.sendMessage(handler.obtainMessage());
		     }
				         
	      });
	      thread.start();
	   }
	}
	
	/**
	 * Génère et affiche une image Bitmap du code-barres à l'écran à partir de la valeur fournie 
	 * 
	 * @param code valeur du code-barres
	 */
	private void generateBarcodeBitmap(String code) {
	    // barcode image
	    Bitmap bitmap = null;
	    try {
	       switch (code.length()) {
	          case (13): bitmap = encodeAsBitmap(code, BarcodeFormat.EAN_13, 600, 300); break;
	          case(8) : bitmap = encodeAsBitmap(code, BarcodeFormat.EAN_8, 600, 300); break;
	          default : bitmap = encodeAsBitmap(code, BarcodeFormat.CODE_128, 600, 300); break;
	       }
	       imageViewCode.setImageBitmap(bitmap);
	    } 
	    catch (WriterException e) {
	       e.printStackTrace();
	    }
	    //barcode text
	    textViewCode.setText(code);
	}
	
	private Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width,int img_height) throws WriterException {
	   String contentsToEncode = contents;
	   if (contentsToEncode == null) {
	      return null;
	   }
	   Map<EncodeHintType, Object> hints = null;
	   String encoding = guessAppropriateEncoding(contentsToEncode);
	   if (encoding != null) {
	      hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		  hints.put(EncodeHintType.CHARACTER_SET, encoding);
	   }
	   MultiFormatWriter writer = new MultiFormatWriter();
	   BitMatrix result;
	   try {
	      result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
	   } 
	   catch (IllegalArgumentException iae) {
			// Unsupported format
			return null;
	   }
	   int width = result.getWidth();
	   int height = result.getHeight();
	   int[] pixels = new int[width * height];
	   for (int y = 0; y < height; y++) {
	      int offset = y * width;
		  for (int x = 0; x < width; x++) {
		     pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
		  }
	   }
	   Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	   bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
	   return bitmap;
	}
	
	private static String guessAppropriateEncoding(CharSequence contents) {
	   // Very crude at the moment
	   for (int i = 0; i < contents.length(); i++) {
	      if (contents.charAt(i) > 0xFF) {
		     return "UTF-8";
		  }
	   }
	   return null;
	}
	
	private void deleteCard() {
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/card/"+nomEnseigne.replace(" ","_"),new String[] {},new String[]{},identifiant);
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
		        Toast.makeText(getApplicationContext(),"Echec lors de la suppression de la carte",Toast.LENGTH_LONG).show();
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
	         Intent intent = new Intent(BarcodeCardActivity.this, CardActivity.class);
	         runOnUiThread(new Runnable() {
	        	
		        public void run() {
		           errorTextView.setVisibility(View.GONE);
		           Toast.makeText(getApplicationContext(),"Carte \""+nomEnseigne+"\" supprimée",Toast.LENGTH_LONG).show();
		        }
		     
	         });
		     intent.putExtra("id", BarcodeCardActivity.this.identifiant);
		     startActivity(intent);
		     finish();
	      }
	   }
	}
	
	private void editCardCode() {
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/card/"+bsin+"-"+codeCarte,new String[] {},new String[]{},identifiant);
	   final JSONObject jsonDocument = requete.sendPutRequest();
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
		        Toast.makeText(getApplicationContext(),"Echec lors de la modification du code de la carte \""+nomEnseigne+"\"",Toast.LENGTH_LONG).show();
			 }
			     
		  });
	   }
	   else {
	      String registered = null;
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
		      if (registered.equals("success")) {
		         runOnUiThread(new Runnable() {
		        	
			        public void run() {
			           errorTextView.setVisibility(View.GONE);
			           Toast.makeText(getApplicationContext(),"Code de la carte \""+nomEnseigne+"\" modifié",Toast.LENGTH_LONG).show();
			           generateBarcodeBitmap(codeCarte);
			           imageViewCode.setVisibility(View.VISIBLE);
			    	   textViewCode.setVisibility(View.VISIBLE);
			        }
			     
		         });
		      }
	       }
	    }

}
