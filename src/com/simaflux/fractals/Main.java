package com.simaflux.fractals;

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

	private int x, y, s, p, nmax, edit, newNum;
	private float zoom;
	private boolean ui, create, ee;
	
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
		s = 10;
		p = 2;
		nmax = 500;
		edit = 0;
		newNum = 0;
		ui = true;
		create = true;
		ee = false;
	}
	
	private void createSet() {
		fractal = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D f = (Graphics2D) fractal.getGraphics();
		
		for(int a = 0; a < WIDTH; a++) {
			for(int b = 0; b < HEIGHT; b++) {
				
				float n = getN(a, b);
				//f.setColor(new Color((int) (255.0 * n / nmax), 255, n < nmax ? 255 : 0));
				
				double rotations = 10.0;
				int R = (int) (Math.sin(rotations * 2.0 * Math.PI * n + Math.PI) * 125 + 125);
				int G = (int) (Math.cos(rotations * 2.0 * Math.PI * n) * 125 + 125);
				int B = (int) (int) (255.0 * n);
				
				f.setColor(new Color(R, G, B));
				f.fillRect(a, b, 1, 1);
			}
		}
		
	}

	public float getN(int a, int b) {
		Complex c = new Complex(
				(a - WIDTH / 2 - x / 100.0f) / zoom / zoom + x / 100.0f, 
				(b - HEIGHT / 2 - y / 100.0f) / zoom / zoom + y / 100.0f);
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
			if(edit > 0) {
				g.setColor(new Color(100, 100, 100, 200));
				g.fillRect(10, (edit - 1) * 40 + 10, 280, 40);
			}
			g.setColor(Color.WHITE);
			g.setFont(new Font("Console", Font.BOLD, 30));
			g.drawString("X = " + ((edit == 1 && newNum != 0) ? newNum : x), 20, 40);
			g.drawString("Y = " + ((edit == 2 && newNum != 0) ? newNum : (-y)), 20, 80);
			g.drawString("Z = " + ((edit == 3 && newNum != 0) ? newNum :zoom), 20, 120);
			g.drawString("P = " + ((edit == 4 && newNum != 0) ? newNum : p), 20, 160);
			g.drawString("F1 to toggle UI", 20, 200);
			g.drawString("F2 to edit settings", 20, 240);
			g.drawString("F3 to render", 20, 280);
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
		
		if(kc == 118) ee = !ee;
		
		if(edit > 0 && kc >= 48 && kc <= 57) newNum = newNum * 10 + kc - 48;
		if(kc == 8 && newNum != 0) newNum = (newNum - newNum % 10) / 10;
		if(kc == 45 && newNum != 0) newNum *= -1;
		
		if(kc == 10 && edit > 0 && newNum != 0) {
			if(edit == 1) x = newNum;
			if(edit == 2) y = -newNum;
			if(edit == 3) zoom = newNum;
			if(edit == 4) p = newNum;
			newNum = 0;
		}
		
		if(edit > 4) edit = 0;
		
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
