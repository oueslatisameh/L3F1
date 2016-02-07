package com.interfacecomparateur.slidemenu;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;

/**
 * SlidingMenuDemo
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

/**
 * Helper for examples with a HSV that should be scrolled by a menu View's width.
 */
public class ClickListenerForScrolling implements OnClickListener {
	
    HorizontalScrollView scrollView;
    View menu;
    /**
     * Menu must NOT be out/shown to start with.
     */
    boolean menuOut = false;

    public ClickListenerForScrolling(HorizontalScrollView scrollView, View menu) {
        super();
        this.scrollView = scrollView;
        this.menu = menu;
    }

    @Override
    public void onClick(View v) {
    	int left,menuWidth = menu.getMeasuredWidth();
        // Ensure menu is visible
        menu.setVisibility(View.VISIBLE);
        if (!menuOut) {
            // Scroll to 0 to reveal menu
            left = 0;
            scrollView.smoothScrollTo(left, 0);
        } 
        else {
            // Scroll to menuWidth so menu isn't on screen.
            left = menuWidth;
            scrollView.smoothScrollTo(left, 0);
        }
        menuOut = !menuOut;
    }
    
}