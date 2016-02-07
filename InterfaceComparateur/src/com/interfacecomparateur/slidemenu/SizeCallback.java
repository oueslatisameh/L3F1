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

/**
 * Callback interface to interact with the HSV.
 */
public interface SizeCallback {
	
    /**
     * Used to allow clients to measure Views before re-adding them.
     */
    public void onGlobalLayout();

    /**
     * Used by clients to specify the View dimensions.
     * 
     * @param idx
     *            Index of the View.
     * @param w
     *            Width of the parent View.
     * @param h
     *            Height of the parent View.
     * @param dims
     *            dims[0] should be set to View width. dims[1] should be set to View height.
     */
    public void getViewSize(int idx, int w, int h, int[] dims);
    
}
