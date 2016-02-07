package com.interfacecomparateur.classes;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.interfacecomparateur.R;

/**
 * Classe permettant de définir un adapter spécifique pour les ListView utilisées
 * 
 * @see ListView
 * 
 * 
 * @author L3I1
 *
 */

public class GenericAdapter extends BaseAdapter {
	
	List<?>	liste;
	LayoutInflater inflater;

	public GenericAdapter(Context context, List<?> liste) {
	   inflater = LayoutInflater.from(context);
	   this.liste =  liste;
	}

	@Override
	public int getCount () {
	   return liste.size ();
	}

	@Override
	public Object getItem (int position) {
	   return liste.get(position);
	}

	@Override
	public long getItemId (int position) {
	   return position;
	}
	
	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
	   ViewHolder holder;
	   if (convertView == null) {
	      holder = new ViewHolder ();
		  convertView = inflater.inflate(R.layout.listrow_details, null);
		  holder.nom = (TextView)convertView.findViewById(R.id.nomListe);
	   }
	   else {
	      holder=(ViewHolder)convertView.getTag();
	   }
	   holder.nom.setText(((Object)liste.get(position)).toString()); 
	   convertView.setTag(holder);
	   return convertView;
	}
	
    //Classe permettant de sauvegarder l'etat de la liste et de pouvoir recuperer la position.
	public class ViewHolder {
       TextView nom;
	}

}