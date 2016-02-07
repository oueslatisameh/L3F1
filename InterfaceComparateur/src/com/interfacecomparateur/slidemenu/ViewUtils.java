package com.interfacecomparateur.slidemenu;

/**
 * Copyright (C) 2012 Paul Grime
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
  */

import com.example.interfacecomparateur.*;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Utility methods for Views.
 */
public class ViewUtils {
	
    private ViewUtils() {
    }

    public static void setViewWidths(View view, View[] views) {
        int w = view.getWidth();
        int h = view.getHeight();
        for (int i = 0; i < views.length; i++) {
            View v = views[i];
            v.layout((i + 1) * w, 0, (i + 2) * w, h);
        }
    }

    public static void initListView(Context context, ListView listView, int numItems, int layout) {
        final Item[] items = new Item[numItems];
        String[] arr = {"Scanner un produit","Mes listes de courses","Mes cartes","Mon profil","Déconnexion"};
       
        int[] drawableIds = {R.drawable.ic_barcode_scan,R.drawable.ic_shopping_list,R.drawable.ic_loyalty_cards,R.drawable.ic_settings_profile,R.drawable.ic_logout,};
        for (int i=0;i<numItems;i++)
        	items[i] = new Item(drawableIds[i],arr[i]);
        listView.setAdapter(new CustomAdapter(context, R.layout.list_item, items));
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = view.getContext();
              
              
                String msg = "item[" + (position+1) + " ]=" + parent.getItemAtPosition(position);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                System.out.println(msg);
            }
        });
    }
    
}