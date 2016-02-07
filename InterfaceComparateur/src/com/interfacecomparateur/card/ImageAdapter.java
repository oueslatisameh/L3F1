package com.interfacecomparateur.card;

import java.util.ArrayList;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * 
 * @author Equipe L3I1
 * 
 * @see BaseAdapter
 * 
 */
public class ImageAdapter extends BaseAdapter {
	
	private Context context;
	private ArrayList<Card> cartes;
	
	public ImageAdapter(Context context) {
       this.context=context;
	   this.cartes = new ArrayList<Card>();
	}
	
	public void addCard(Card card) {
	   cartes.add(card);
	}
	
	public ArrayList<Card> getCartes() {
	   return cartes;
	}
	
	public int getCount() {
	   return cartes.size();
	}
	
	public Object getItem(int position) {
	   return cartes.get(position);
	}
	
	public long getItemId(int position) {
	   return 0;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
	   ImageView imageView = new ImageView(context);
	   imageView.setImageBitmap(cartes.get(position).getImageEnseigne());
	   imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	   imageView.setLayoutParams(new GridView.LayoutParams(150,150));
	   return imageView;
	}

}
