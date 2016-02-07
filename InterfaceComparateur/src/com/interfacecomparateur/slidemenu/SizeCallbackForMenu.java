package com.interfacecomparateur.slidemenu;

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

import android.view.View;

/**
 * Helper that remembers the width of the 'slide' button, so that the 'slide' button remains in view, even when the menu is
 * showing.
 */
public class SizeCallbackForMenu implements SizeCallback {
	
    int btnWidth;
    View btnSlide;

    public SizeCallbackForMenu(View btnSlide) {
        super();
        this.btnSlide = btnSlide;
    }

    @Override
    public void onGlobalLayout() {
        btnWidth = btnSlide.getMeasuredWidth();
    }

    @Override
    public void getViewSize(int idx, int w, int h, int[] dims) {
        dims[0] = w;
        dims[1] = h;
        final int menuIdx = 0;
        if (idx == menuIdx) {
            dims[0] = w - btnWidth;
        }
    }
    
}
