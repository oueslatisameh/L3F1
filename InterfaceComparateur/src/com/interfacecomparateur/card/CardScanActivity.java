package com.interfacecomparateur.card;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.google.zxing.Result;
import com.google.zxing.client.android.CaptureActivity;
import com.interfacecomparateur.barcodescanner.ScanActivity;

/**
 * <b>Activité de scan de code-barres de cartes de fidélité appelé lorsque l'utilisateur souhaite ajouter une nouvelle carte ou éditer le code d'une
 * carte déjà enregistrée.</b>
 * 
 * @author Equipe L3I1
 * 
 * @see ScanActivity
 *
 */
public class CardScanActivity extends CaptureActivity {
	
	/**
	 * Intent de l'activité
	 */
	private Intent intent;
	/**
	 * EditText indiquant la valeur du code-barre retrouvée grâce au scan de celui-ci ou saisie manuellement par l'utilisateur.
	 */
	private EditText codeEditText;
	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * Nom de l'enseigne de la carte de fidélité
	 */
	private String nomEnseigne;
	
	public void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.card_scan_vue);
	   intent = getIntent();
	   identifiant = intent.getStringExtra("id");
	   nomEnseigne = intent.getStringExtra("brand");
	   intent.getStringExtra("bsin");
	   String cookieNameValue = intent.getStringExtra("name");
	   SharedPreferences preferences = getApplicationContext().getSharedPreferences("name_"+identifiant,0);
       SharedPreferences.Editor editor = preferences.edit();
       editor.putString("name",cookieNameValue);
       editor.commit();
	   
	   codeEditText = (EditText)findViewById(R.id.textfield_valeur);
        
       Button buttonSend = (Button)findViewById(R.id.button_send);
       buttonSend.setOnClickListener(new OnClickListener() {
        	   
          public void onClick(View v) {
             if (codeEditText.getText().toString().length()>=8 && !codeEditText.getText().toString().contains(" ")) {
   		        String code = codeEditText.getText().toString();
   		        if (intent.hasExtra("editCode")) {
   			       intent.putExtra("newCode",code);
   		           setResult(1,intent);
   		           finish();
   		        }
   		        else {
   		           Intent intent = new Intent(CardScanActivity.this, CardActivity.class);
  			       intent.putExtra("id", CardScanActivity.this.identifiant);
  			       intent.putExtra("newCard",true);
  			       intent.putExtra("brand",nomEnseigne);
  			       intent.putExtra("code",code);
  			       intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
  			       startActivity(intent);
  			       finish();
   		        }
   			 }
             else {
            	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    			imm.hideSoftInputFromWindow(codeEditText.getWindowToken(), 0);
                Toast.makeText(getApplicationContext(),"Le code-barre doit être de longueur au moins 8 et ne doit pas contenir d'espaces",Toast.LENGTH_LONG).show();
             }
          }
       });
		
	   ImageButton buttonBack = (ImageButton)findViewById(R.id.button_back);
	   buttonBack.setOnClickListener(new OnClickListener() {
				
	      public void onClick(View v) {
	    	 setResult(-1,intent);
		     finish();
		  }
				
	   });
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	   if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0) {
	      setResult(-1,intent);
		  finish();
	   }
	   return super.onKeyDown(keyCode, event);
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

}