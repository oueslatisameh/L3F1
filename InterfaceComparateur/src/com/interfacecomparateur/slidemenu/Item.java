package com.interfacecomparateur.slidemenu;

public class Item {
	
	private int drawableIcon;
	private String text;
	
	public Item(int drawableIcon, String text) {
	   this.drawableIcon=drawableIcon;
	   this.text=text;
	}
	
	public int getDrawableIcon() {
	   return drawableIcon;
	}
	
	public String getText() {
	   return text;
	}
	
}