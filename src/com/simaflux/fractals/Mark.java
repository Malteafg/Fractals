package com.simaflux.fractals;

import java.awt.Color;
import java.awt.Graphics2D;

public class Mark {

	private int x, y;
	
	public Mark(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void draw(Graphics2D g) {
		g.setColor(Color.BLUE);
		g.fillOval(x - 4, y - 4, 8, 8);
	}
	
}
