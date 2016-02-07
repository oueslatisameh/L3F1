package com.interfacecomparateur.classes;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * Classe contenant des méthodes permettant d'envoyer des requêtes HTTP au serveur et de récupérer les réponses (documents JSON) renvoyés par celui-ci.
 * 
 * @author Equipe L3I1
 * 
 * @see DefaultHttpClient
 * @see HttpPost
 * @see HttpGet
 * @see HttpPut
 * @see HttpDelete
 * @see HttpResponse
 *
 */
public class RequeteHTTP extends Activity {

	/**
	 * Adresse IP et port d'écoute du serveur
	 */
	public static final String SERVER_ADDRESS = "http://172.30.29.9:8080";
	
	/**
	 * Chemin vers le répertoire contenant les images des enseignes qui se trouve sur le serveur
	 */
	public static final String PATH_BRAND_IMG = SERVER_ADDRESS+"/brandImage/";
	
	/**
	 * Chemin vers le répertoire contenant les images des produits qui se trouve sur le serveur
	 */
	public static final String PATH_PRODUCT_IMG = SERVER_ADDRESS+"/productImage/";
    
	/**
	 * Context de l'activité qui fait appel à cette classe
	 */
	private Context context;
	
	/**
	 * Chaîne indiquant la route dans l'URL
	 */
	private String route;
	
	/**
	 * Noms des paramètres transmis dans l'URL
	 */
	private String[] names;
	
	/**
	 * Valeurs des paramètres transmis dans l'URL
	 */
	private String[] values;
	
	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	
	/**
	 * Cookie envoyé par le serveur lors de la requête de connexion et enregistré en mémoire
	 */
	private String cookieNameRetrieved;
	
	/**
	 * Cookie sauvegardé en mémoire et devant être envoyé au serveur pour toutes requêtes
	 */
	private String cookieNameSaved;
	
	/**
	 * Document JSON renvoyé par le serveur (Réponse d'une requête)
	 */
	private JSONObject jsonDocument;
	
	/**
	 * DefaultHttpClient utilisé pour effectuer la requête
	 */
	private DefaultHttpClient client;

	/**
	 * Constructeur de la classe RequeteHTTP
	 * 
	 * @param context contexte de l'activité faisant appel à la classe requeteHTTP
	 * @param route chaîne indiquant la route dans l'URL
	 * @param names noms des paramètres transmis dans l'URL
	 * @param values valeurs des paramètres transmis dans l'URL
	 * @param identifiant identifiant de l'utilisateur
	 */
	public RequeteHTTP(Context context, String route, String[] names, String[] values, String identifiant) {
		this.context=context;
		this.route=route;
		this.names=names;
		this.values=values;
		this.identifiant=identifiant;
		client = new DefaultHttpClient();
		client.getParams().setParameter(CoreProtocolPNames.USER_AGENT,"scanoid/1.0");
		SharedPreferences preferences = context.getSharedPreferences("name_"+identifiant,0);
		cookieNameSaved = preferences.getString("name","");
		if (cookieNameSaved!=null) {
			// Ajout du cookie sauvegardé en mémoire au header de la requête HTTP
			Log.d("envoie","envoie"+cookieNameSaved);
			BasicClientCookie cookie = new BasicClientCookie("name",cookieNameSaved);
			cookie.setVersion(0);
			cookie.setPath("/");
			String inter = ""+SERVER_ADDRESS.subSequence(SERVER_ADDRESS.indexOf(":")+3,SERVER_ADDRESS.length());
			cookie.setDomain(""+inter.subSequence(0,inter.indexOf(":")));
			cookie.setExpiryDate(null);
			client.getCookieStore().addCookie(cookie);
		}
	}

	public RequeteHTTP(Context context, String route, String[] names, String[] values) {
		this(context,route,names,values,"");
	}

	/**
	 * Envoi d'une requête POST au serveur
	 * 
	 * @return le document JSON renvoyé par le serveur
	 */
	public JSONObject sendPostRequest() {
		Thread threadConnection = new Thread() {

			public void run() {
				HttpPost post = new HttpPost(SERVER_ADDRESS+route);
				// Add data
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				for (int i=0;i<names.length;i++) {
					params.add(new BasicNameValuePair(names[i],values[i]));
				}
				JSONObject jsonResponse=null;
				try {
					post.setEntity(new UrlEncodedFormEntity(params));
					// Execute HTTP Post Request
					HttpResponse response = client.execute(post);
					InputStream inputStream = response.getEntity().getContent();
					String result = InputStreamOperations.InputStreamToString(inputStream);
					jsonResponse = new JSONObject(result);
					Cookie cookie = client.getCookieStore().getCookies().get(0);
					if (cookie.getName().equals("name") &&( route.equals("/login") || route.equals("/loginFacebook"))) {
						cookieNameRetrieved=cookie.getValue();
					}
					inputStream.close();
					jsonDocument=jsonResponse;
				}
				catch(Exception e) {
					Log.d("Error HTTP request","Error HTTP request"+e.getMessage());
				}
			}

		};
		threadConnection.start();
		try {
			threadConnection.join();
		} 
		catch (InterruptedException e) {
			Log.d("InterruptedException","Interrupted Exception"+e.getMessage());
		}
		if (cookieNameRetrieved!=null) {
			SharedPreferences preferences = context.getSharedPreferences("name_"+identifiant,0);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("name",cookieNameRetrieved);
			editor.commit();
		}
		return jsonDocument;
	}

	/** 
	 * Envoi d'une requête GET au serveur
	 * 
	 * @return le document JSON renvoyé par le serveur
	 */
	public JSONObject sendGetRequest() {
		Thread threadConnection = new Thread() {

			public void run() {
				StringBuffer sb = new StringBuffer();
				sb.append("?");
				for (int i=0;i<names.length;i++) {
					sb.append(names[i]+"="+values[i]);
					if (i!=names.length-1)
						sb.append("&");
				}
				HttpGet get = new HttpGet(SERVER_ADDRESS+route+sb.toString());
				try {
					HttpResponse response = client.execute(get);
					JSONObject jsonResponse=null;
					InputStream inputStream = response.getEntity().getContent();
					String result = InputStreamOperations.InputStreamToString(inputStream);
					jsonResponse = new JSONObject(result);
					inputStream.close();
					jsonDocument=jsonResponse;
				}
				catch(Exception e) {
					Log.d("Error HTTP request","Error HTTP request"+e.getMessage());
				}
			}

		};
		threadConnection.start();
		try {
			threadConnection.join();
		} 
		catch (InterruptedException e) {
			Log.d("InterruptedException","Interrupted Exception"+e.getMessage());
		}
		return jsonDocument;
	}

	/**
	 * Envoi d'une requête DELETE au serveur
	 * 
	 * @return le document JSON renvoyé par le serveur
	 */
	public JSONObject sendDeleteRequest() {
		Thread threadConnection = new Thread() {

			public void run() {
				StringBuffer sb = new StringBuffer();
				sb.append("?");
				for (int i=0;i<names.length;i++) {
					sb.append(names[i]+"="+values[i]);
					if (i!=names.length-1)
						sb.append("&");
				}
				HttpDelete del = new HttpDelete(SERVER_ADDRESS+route+sb.toString());
				try {
					HttpResponse response = client.execute(del);
					JSONObject jsonResponse=null;
					InputStream inputStream = response.getEntity().getContent();
					String result = InputStreamOperations.InputStreamToString(inputStream);
					jsonResponse = new JSONObject(result);
					inputStream.close();
					jsonDocument=jsonResponse;
				}
				catch(Exception e) {
					Log.d("Error HTTP request","Error HTTP request"+e.getMessage());
				}
			}
		};
		threadConnection.start();
		try {
			threadConnection.join();
		} 
		catch (InterruptedException e) {
			Log.d("InterruptedException","Interrupted Exception"+e.getMessage());
		}
		return jsonDocument;
	}

	/**
	 * Envoi d'une requête PUT au serveur
	 * 
	 * @return le document JSON renvoyé par le serveur
	 */
	public JSONObject sendPutRequest() {
		Thread threadConnection = new Thread() {

			public void run() {
				StringBuffer sb = new StringBuffer();
				sb.append("?");
				for (int i=0;i<names.length;i++) {
					sb.append(names[i]+"="+values[i]);
					if (i!=names.length-1)
						sb.append("&");
				}
				HttpPut put = new HttpPut(SERVER_ADDRESS+route+sb.toString());
				try {
					HttpResponse response = client.execute(put);
					JSONObject jsonResponse=null;
					InputStream inputStream = response.getEntity().getContent();
					String result = InputStreamOperations.InputStreamToString(inputStream);
					jsonResponse = new JSONObject(result);
					inputStream.close();
					jsonDocument=jsonResponse;
				}
				catch(Exception e) {
					Log.d("Error HTTP request","Error HTTP request"+e.getMessage());
				}
			}
		};
		threadConnection.start();
		try {
			threadConnection.join();
		} 
		catch (InterruptedException e) {
			Log.d("InterruptedException","Interrupted Exception"+e.getMessage());
		}
		return jsonDocument;
	}

	/**
	 * Récupération d'une image se trouvant sur le serveur
	 * 
	 * @param url URL permettant d'accéder à l'image souhaitée sur le serveur
	 * 
	 * @return le Bitmap de l'image
	 */
	public static Bitmap loadImage(String url) {
		try {
			URLConnection connection = new URL(url).openConnection();
			connection.connect();
			return BitmapFactory.decodeStream(connection.getInputStream());
		}
		catch(Exception e) {
			Log.d("Error loading image","Error loading image : "+e.getMessage());
		}
		return null;
	}

	/**
	 * Vérification de la connexion au réseau de l'appareil
	 * 
	 * @param context Context de l'activité appelant cette méthode
	 * 
	 * @return vrai si l'utilisateur est connecté, faux sinon
	 */
	public static boolean isNetworkConnected(Context context) {
       ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
       boolean has3G = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
       boolean hasWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
       return (hasWifi||has3G);
	}

}