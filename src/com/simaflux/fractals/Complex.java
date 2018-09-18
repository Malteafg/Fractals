package com.simaflux.fractals;

import java.io.Serializable;

public class Complex implements Serializable {
	
	private static final long serialVersionUID = -5112929080865487679L;
	private float x, y;
	
	public Complex(float x, float y) {
		this.x = x; 
		this.y = y;
	}
	
	public Complex() {
		this.x = 0.0f; 
		this.y = 0.0f;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
	public float X() {
		return x;
	}
	
	public float Y() {
		return y;
	}
	
	public int IX() {
		return (int) x;
	}
	
	public int IY() {
		return (int) y;
	}
	
	public Complex add(Complex c) {
		return new Complex(x + c.X(), y + c.Y());
	}
	
	public Complex subtract(Complex c) {
		return new Complex(x - c.X(), y - c.Y());
	}
	
	public Complex multiply(Complex c) {
		return new Complex(x * c.X() - y * c.Y(), y * c.X() + x * c.Y());
	}
	
	public Complex power(int p) {
		Complex r = copy();
		while(p > 1) {
			r = r.multiply(this);
			p--;
		}
		return r;
	}
	
	public float length() {
		return (float) Math.sqrt(x * x + y * y);
	}
	
	public void print() {
		System.out.println(x + ", " + y);
	}
	
	public Complex copy() {
		return new Complex(x, y);
	}
	
}
