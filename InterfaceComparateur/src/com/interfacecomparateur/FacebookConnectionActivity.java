package com.interfacecomparateur;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.interfacecomparateur.classes.RequeteHTTP;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


@SuppressWarnings("deprecation")
public class FacebookConnectionActivity extends Activity {

	public static final String TAG = "FACEBOOK";
	
	private String identifiant = null;
	private Facebook mFacebook;
	public static final String APP_ID = "278126549014314";
	private AsyncFacebookRunner mAsyncRunner;
	private static final String[] PERMS = new String[] { "read_stream","email","user_birthday" };
	private SharedPreferences sharedPrefs;
	private Context mContext;

	protected ProgressBar pb;
	protected HashMap<String,String>infoUser ; 
    protected  TextView errorTextview ;

	public void setConnection() {
		mContext = this;
		mFacebook = new Facebook(APP_ID);
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
	}


	public void getID() {

		if (isSession()) {
			Log.d(TAG, "sessionValid");
			mAsyncRunner.request("me", (RequestListener) new IDRequestListener());
		} else {
			// no logged in, so relogin
			Log.d(TAG, "sessionNOTValid, relogin");
			mFacebook.authorize(this, PERMS, (DialogListener) new LoginDialogListener());
		}
	}

	
	public boolean isSession() {
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		String access_token = sharedPrefs.getString("access_token", "x");
		Long expires = sharedPrefs.getLong("access_expires", -1);
		Log.d(TAG, access_token);

		if (access_token != null && expires != -1) {
			mFacebook.setAccessToken(access_token);
			mFacebook.setAccessExpires(expires);
		}
		return mFacebook.isSessionValid();
	}

	private class LoginDialogListener implements DialogListener {

		@Override
		public void onComplete(Bundle values) {
			Log.d(TAG, "LoginONComplete");
			String token = mFacebook.getAccessToken();
			long token_expires = mFacebook.getAccessExpires();
			Log.d(TAG, "AccessToken: " + token);
			Log.d(TAG, "AccessExpires: " + token_expires);
			sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(mContext);
			sharedPrefs.edit().putLong("access_expires", token_expires)
			.commit();
			sharedPrefs.edit().putString("access_token", token).commit();
			mAsyncRunner.request("me", new IDRequestListener());
		}

		@Override
		public void onFacebookError(FacebookError e) {
			Log.d(TAG, "FacebookError: " + e.getMessage());
		}

		@Override
		public void onError(DialogError e) {
			Log.d(TAG, "Error: " + e.getMessage());
		}

		@Override
		public void onCancel() {
			Log.d(TAG, "OnCancel");
		}
	}


	private class IDRequestListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			try {
				Log.d(TAG, "IDRequestONComplete");
				Log.d(TAG, "Response: " + response.toString());
				JSONObject json = Util.parseJson(response);
                infoUser.put("id",json.getString("id"));
                infoUser.put("firstname", json.getString("first_name"));
                
                infoUser.put("lastname", json.getString("last_name"));
                infoUser.put("email",json.getString("email"));
                infoUser.put("birthdate", json.getString("birthday"));
                Log.d("firstname","ff"+ "ff1"+infoUser.get("firstname"));
				
                FacebookConnectionActivity.this.runOnUiThread(new Runnable() {
					public void run() {
					   Thread thread = new Thread(new Runnable() {
						   
					      public void run() {
			                 Looper.prepare();
						     sendRequest();
					      }
					      
					   });
					   thread.start();
					}
				});
			} catch (JSONException e) {
				Log.d(TAG, "JSONException: " + e.getMessage());
			} catch (FacebookError e) {
				Log.d(TAG, "FacebookError: " + e.getMessage());
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Log.d(TAG, "IOException: " + e.getMessage());
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			Log.d(TAG, "FileNotFoundException: " + e.getMessage());
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			Log.d(TAG, "MalformedURLException: " + e.getMessage());
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Log.d(TAG, "FacebookError: " + e.getMessage());
		}

	}

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mFacebook.authorizeCallback(requestCode, resultCode, data);
	}
    
    public void sendRequest() {
	    String lastName = infoUser.get("firstname");
		String firstName = infoUser.get("lastname");
        String birthdate = infoUser.get("birthdate");
		String email = infoUser.get("email");
        String id = infoUser.get("id");
        
        boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	    RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/loginFacebook",new String[]{"email","id","name","forename","birth"},
			                  new String[]{email,ConnectionActivity.sha1Hash(id),firstName,lastName,birthdate},email);
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
		         Toast.makeText(getApplicationContext(),"Erreur lors de l'envoi de l'e-mail",Toast.LENGTH_LONG).show();
			  }
			     
		   });
		}
		else {
		   try {
		      registered = jsonDocument.getString("registered");
		   }
	       catch(JSONException e) {
		      Log.d("Err",e.getMessage());
		   }
		   if (registered.equals("success")) {
		      identifiant = email;
		   }
           if (identifiant != null){   
		      //Sauvegarde de l'identifiant de connexion
			  SharedPreferences preferences = getSharedPreferences("identifiant",0);
		      SharedPreferences.Editor editor = preferences.edit();
			  editor.putString("id",identifiant);
		      editor.commit();
				    	
		      //Lancement de l'activité par défaut ou de l'activité choisie par l'utilisateur
			  preferences = getSharedPreferences("pref_activity_"+identifiant,0);
			  String choixActivite = preferences.getString("start_activity", "Scanner");
			  Class<?> activity = ConnectionActivity.chooseActivity(choixActivite);
		      Intent intent = new Intent(getApplicationContext(),activity);
		      intent.putExtra("id", FacebookConnectionActivity.this.identifiant);
			  startActivity(intent);
			  finish();
		   }
		}
	}
}