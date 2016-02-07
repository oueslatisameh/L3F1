package com.interfacecomparateur.slidemenu;

import com.example.interfacecomparateur.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<Item> {
	
	private Context context;
	private int layoutResourceId;
	private Item[] items;
	
	public CustomAdapter(Context context, int layoutResourceId, Item[] items) {
	   super(context,layoutResourceId,items);
	   this.context=context;
	   this.layoutResourceId=layoutResourceId;
	   this.items=items;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
       View row = convertView;
       ItemHolder holder = null;
       if(row == null) {
          LayoutInflater inflater = ((Activity)context).getLayoutInflater();
          row = inflater.inflate(layoutResourceId, parent, false); 
          holder = new ItemHolder();
          holder.imageIcon = (ImageView)row.findViewById(R.id.icon_list_item);
          holder.textView = (TextView)row.findViewById(R.id.text_list_item);
          row.setTag(holder);
       }
       else {
          holder = (ItemHolder)row.getTag();
       }
       Item item = items[position];
       holder.textView.setText(item.getText());
       holder.imageIcon.setImageResource(item.getDrawableIcon());   
       return row;
    }
		
	static class ItemHolder {
		ImageView imageIcon;
		TextView textView;
	}
	
}
