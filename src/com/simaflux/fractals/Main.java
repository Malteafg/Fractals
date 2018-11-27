package com.simaflux.fractals;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main extends JPanel implements Runnable, KeyListener, MouseListener {

	private static final long serialVersionUID = 7451328825961479810L;

	public static final int UNIT = 120;
	public static final int WIDTH = UNIT * 16;
	public static final int HEIGHT = UNIT * 9;

	private Thread thread;
	private boolean running;

	private int x, y, s, p, c, nmax, edit, newNum;
	private float zoom;
	private boolean ui, create, ee, bnw;
	
	private BufferedImage image, fractal;
	private Graphics2D g;

	private int FPS = 30;

	public static void main(String[] args) {

		JFrame window = new JFrame("Game Engine");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setContentPane(new Main());
		window.setUndecorated(true);

		window.pack();
		window.setVisible(true);

	}

	public Main() {
		super();
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setFocusable(true);
		requestFocus();
	}

	private void init() {
		x = 0;
		y = 0;
		zoom = 20.0f;
		s = 3;
		p = 2;
		nmax = 500;
		edit = 0;
		newNum = 0;
		ui = true;
		create = true;
		ee = false;
		c = 1;
		bnw = false;
	}
	
	private void createSet() {
		fractal = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D f = (Graphics2D) fractal.getGraphics();
		
		for(int a = 0; a < WIDTH; a++) {
			for(int b = 0; b < HEIGHT; b++) {
				
				float n = getN(a, b);
				//f.setColor(new Color((int) (255.0 * n / nmax), 255, n < nmax ? 255 : 0));
				
				if(bnw) {
					int c = (int) (255.0 - 200.0 * n);
					
					f.setColor(new Color(c, c, c));
				} else {
					double rotations = 5.0;
					double startingPoint = Math.PI * c / 100;
					int R = (int) (Math.sin(rotations * 2.0 * Math.PI * n + startingPoint) * 125 + 125);
					int G = (int) (Math.cos(rotations * 2.0 * Math.PI * n + startingPoint) * 125 + 125);
					int B = (int) (255.0 * n);
					
					f.setColor(new Color(R, G, B));
				}
				f.fillRect(a, b, 1, 1);
			}
		}
		
	}

	public float atZoomX(int v) {
		return (v - WIDTH / 2 - x / 100.0f) / zoom / zoom + x / 100.0f;
	}
	
	public float atZoomY(int v) {
		return (v - HEIGHT / 2 - y / 100.0f) / zoom / zoom + y / 100.0f;
	}
	
	public int getScreenPosX(float m) {
		return (int) ((m - x / 100.0f) * zoom * zoom + x / 100.0f + WIDTH / 2);
	}
	
	public int getScreenPosY(float m) {
		return (int) ((m - y / 100.0f) * zoom * zoom + y / 100.0f + HEIGHT / 2);
	}
	
	public float getN(int a, int b) {
		Complex c = new Complex(atZoomX(a), atZoomY(b));
		Complex z = new Complex(0, 0);
		int n = 0;
		
		while(z.length() < s && n < nmax) {
			z = z.power(p).add(c);
			n++;
		}
		
		if(n == nmax) return 1.0f;
		
		float d = (float) (1.0f * (n + 1.0 / z.length()) / nmax);
		return d;
	}
	
	public void addNotify() {
		super.addNotify();
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
		addKeyListener(this);
		addMouseListener(this);
	}

	public void run() {

		running = true;

		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) image.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		init();

		long startTime;
		long waitTime;

		long targetTime = 1000 / FPS;

		// GAME LOOP
		while (running) {

			startTime = System.nanoTime();

			gameUpdate();
			gameRender();
			gameDraw();

			waitTime = targetTime - ((System.nanoTime() - startTime) / 1000000);

			try {
				Thread.sleep(waitTime);
			} catch (Exception e) {}

		}

	}

	private void gameUpdate() {
		if(create) {
			createSet();
			create = false;
		}
	}

	private void gameRender() {
		
		g.drawImage(fractal, 0, 0, WIDTH, HEIGHT, null);
		
		if(ui) {
			
			g.setStroke(new BasicStroke(3));
			g.setFont(new Font("Console", Font.BOLD, 20));
			
			for(float q = - 2.0f; q < 2.01f; q += 0.5) {
				int a = getScreenPosX(q);
				g.setColor(q != 0 ? new Color(0, 0, 0, 100) : Color.BLACK);
				g.drawLine(a, 0, a, HEIGHT);
				g.drawString(Float.toString(q).substring(0, q < 0 ? 4 : 3), a + 15, HEIGHT / 2 - 15);
			}
			
			for(float q = - 2.0f; q < 2.01f; q += 0.5) {
				int a = getScreenPosY(q);
				g.setColor(q != 0 ? new Color(0, 0, 0, 100) : Color.BLACK);
				g.drawLine(0, a, WIDTH, a);
				if(q != 0) g.drawString(Float.toString(q).substring(0, q < 0 ? 4 : 3) + "i", WIDTH / 2 + 15, a - 15);
			}
			
			g.setColor(new Color(255, 0, 0, 200));
			int xpos = getScreenPosX(-2.0f), ypos = getScreenPosY(-2.0f), size = getScreenPosX(2.0f) - xpos;
			g.drawOval(xpos, ypos, size, size);
			
			if(edit > 0) {
				g.setColor(new Color(100, 100, 100, 200));
				g.fillRect(10, (edit - 1) * 40 + 10, 280, 40);
			}
			
			g.setColor(Color.BLACK);
			g.drawString("X = " + ((edit == 1 && newNum != 0) ? newNum : x), 20, 40);
			g.drawString("Y = " + ((edit == 2 && newNum != 0) ? newNum : (-y)), 20, 80);
			g.drawString("Z = " + ((edit == 3 && newNum != 0) ? newNum :zoom), 20, 120);
			g.drawString("P = " + ((edit == 4 && newNum != 0) ? newNum : p), 20, 160);
			g.drawString("C = " + ((edit == 5 && newNum != 0) ? newNum : c), 20, 200);
			g.drawString("F1 to toggle UI", 20, 240);
			g.drawString("F2 to edit settings", 20, 280);
			g.drawString("F3 to render", 20, 320);
			if(!bnw) g.setColor(Color.BLUE);
			g.drawString("F4 to remove Colors", 20, 360);
		}
		
		if(ee) {
			g.drawString("Made by Simon Brun Olsen", 1520, 1060);
		}
		
	}

	private void gameDraw() {
		Graphics g2 = this.getGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int kc = e.getKeyCode();
		if (kc == 27)	System.exit(0);
		
		if(kc == 112) {
			ui = !ui;
			if(!ui) edit = 0;
		}
		
		if(kc == 113 && ui) {
			edit++;
			newNum = 0;
		}
		
		create = (kc == 114);
		if(kc == 115) bnw = !bnw;
		if(kc == 118) ee = !ee;
		
		if(edit > 0 && kc >= 48 && kc <= 57) newNum = newNum * 10 + kc - 48;
		if(kc == 8 && newNum != 0) newNum = (newNum - newNum % 10) / 10;
		if(kc == 45 && newNum != 0) newNum *= -1;
		
		if(kc == 10 && edit > 0 && newNum != 0) {
			if(edit == 1) x = newNum;
			if(edit == 2) y = -newNum;
			if(edit == 3) zoom = newNum;
			if(edit == 4) p = newNum;
			if(edit == 5) c = newNum;
			newNum = 0;
		}
		
		if(edit > 5) edit = 0;
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override

	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

}
