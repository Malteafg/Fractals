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
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 7451328825961479810L;

	public static final int UNIT = 120;
	public static final int WIDTH = UNIT * 16;
	public static final int HEIGHT = UNIT * 9;

	private Thread thread;
	private boolean running;

	private ArrayList<Mark> list;
	
	private int x, y, s, p, c, nmax, edit, newNum, mode;
	private float zoom;
	private boolean ui, create, ee, bnw, trace, gradient;
	
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
		c = 100;
		bnw = false;
		trace = true;
		mode = 0;
		list = new ArrayList<>();
	}
	
	private void circleImage() {
		fractal = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D f = (Graphics2D) fractal.getGraphics();
		f.setColor(Color.WHITE);
		f.fillRect(0, 0, WIDTH, HEIGHT);
		
		for(int a = 0; a < WIDTH; a++) {
			for(int b = 0; b < HEIGHT; b++) {
				if(Math.sqrt(Math.pow(atZoomX(a) + 1, 2) + Math.pow(atZoomY(b) * 1.0, 2)) < 0.25) {
					Complex c = new Complex(atZoomX(a), atZoomY(b));
					Complex z = new Complex(0, 0);
					
					for(int i = 0; i < nmax; i++) {
						z = z.power(p).add(c);
					}
					
					f.setColor(Color.BLUE);
					f.fillRect(getScreenPosX(z.X()), getScreenPosY(z.Y()), 1, 1);
				}
			}
		}
	}
	
	private void squareImage() {
		fractal = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D f = (Graphics2D) fractal.getGraphics();
		f.setColor(Color.WHITE);
		f.fillRect(0, 0, WIDTH, HEIGHT);
		
		for(int a = 0; a < WIDTH; a++) {
			for(int b = 0; b < HEIGHT; b++) {
				Complex c = new Complex(atZoomX(a), atZoomY(b));
				Complex z = new Complex(0, 0);
				
				for(int i = 0; i < nmax; i++) {
					z = z.power(p).add(c);
				}
				
				if((int) (a / 30) % 2 == (int) (b / 30) % 2) f.setColor(Color.BLACK);
				else f.setColor(Color.RED);
				f.fillRect(getScreenPosX(z.X()), getScreenPosY(z.Y()), 1, 1);
			}
		}
	}
	
	private void createSet() {
		fractal = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D f = (Graphics2D) fractal.getGraphics();
		
		for(int a = 0; a < WIDTH; a++) {
			for(int b = 0; b < HEIGHT; b++) {
				
				float n = getN(a, b);
				
				if(bnw) {
					int c = (int) (255.0 - 200.0 * n);
					
					f.setColor(new Color(c, c, c));
				} else {
					
					if(n == 1.0f) {
						f.setColor(Color.BLACK);
					} else {
						double rotations = 5.0;
						double startingPoint = Math.PI * c / 100;
						int R = (int) (Math.sin(rotations * 2.0 * Math.PI * n + startingPoint) * 125 + 125);
						int G = (int) (Math.cos(rotations * 2.0 * Math.PI * n + startingPoint) * 125 + 125);
						int B = (int) (255.0 * n);
						f.setColor(new Color(R, G, B));
					}
					
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
		
		float d = gradient ? (float) (1.0f * (n + 1.0 / z.length()) / nmax) : n * 1.0f / nmax;
		return d;
	}
	
	public void trace(float cx, float cy) {
		list = new ArrayList<>();
		Complex c = new Complex(cx, cy);
		Complex z = new Complex(0, 0);
		int n = 0;
		
		while(z.length() < s && n < nmax) {
			z = z.power(p).add(c);
			n++;
			list.add(new Mark(getScreenPosX(z.X()), getScreenPosY(-z.Y())));
		}
	}
	
	public void addNotify() {
		super.addNotify();
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
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
			switch(mode) {
			
			case 1:
				squareImage();
				break;
			case 2:
				circleImage();
				break;
			default:
				createSet();
				break;
			}
			create = false;
		}
	}

	private void gameRender() {
		
		g.drawImage(fractal, 0, 0, WIDTH, HEIGHT, null);
					
		g.setStroke(new BasicStroke(3));
		g.setFont(new Font("Console", Font.BOLD, 20));
		
		int b = getScreenPosY(0);
		for(float q = - 2.0f; q < 2.01f; q += 0.5) {
			int a = getScreenPosX(q);
			g.setColor(q != 0 ? new Color(200, 0, 0, 100) : Color.RED);
			g.drawLine(a, 0, a, HEIGHT);
			g.drawString(Float.toString(q).substring(0, q < 0 ? 4 : 3), a + 15, b - 15);
		}
		
		b = getScreenPosX(0);
		for(float q = - 2.0f; q < 2.01f; q += 0.5) {
			int a = getScreenPosY(q);
			g.setColor(q != 0 ? new Color(200, 0, 0, 100) : Color.RED);
			g.drawLine(0, a, WIDTH, a);
			if(q != 0) g.drawString(Float.toString(-q).substring(0, (-q) < 0 ? 4 : 3) + "i", b + 15, a - 15);
		}
		
		if(trace) {
			for(int i = 0; i < list.size(); i++) {
				if(i + 1 < list.size()) {
					g.setStroke(new BasicStroke(3));
					g.setColor(Color.CYAN);
					g.drawLine(list.get(i).getX(), list.get(i).getY(), list.get(i + 1).getX(), list.get(i + 1).getY());
				}
				list.get(i).draw(g);
			}
		}
		
		g.setColor(new Color(255, 0, 0, 200));
		int xpos = getScreenPosX(-2.0f), ypos = getScreenPosY(-2.0f), size = getScreenPosX(2.0f) - xpos;
		g.drawOval(xpos, ypos, size, size);
		
		if(ui) {
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
			g.drawString("I = " + ((edit == 6 && newNum != 0) ? newNum : nmax), 20, 240);
			g.drawString("Q to toggle UI", 20, 280);
			g.drawString("W and S to edit settings", 20, 320);
			g.drawString("E to render", 20, 360);
			if(!bnw) g.setColor(Color.BLUE);
			g.drawString("R to remove Colors", 20, 400);
			g.setColor(Color.BLACK);
			g.drawString("T to remove trace", 20, 440);
			g.drawString("G to remove gradient", 20, 480);
			g.drawString("Y to change mode", 20, 520);
			
			String s;
			
			switch(mode) {
			
			case 1:
				s = "Square Trace";
				break;
			case 2:
				s = "Circle Trace";
				break;
			default:
				s = "Mandelbrot Set";
				break;
			}
			
			g.drawString("Mode: " + s, 20, 560);
			
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
		
		if(e.getKeyChar() == 'y') {
			mode++;
			if(mode > 2) mode = 0;
			if(mode > 0) nmax = 1;
			else nmax = 500;
		}
		if(e.getKeyChar() == 't') trace = !trace;
		if(e.getKeyChar() == 'g') gradient = !gradient;
		
		if(kc == 81) {
			ui = !ui;
			if(!ui) edit = 0;
		}
		
		if(kc == 87 && ui) {
			edit--;
			newNum = 0;
		}
		
		if(kc == 83 && ui) {
			edit++;
			newNum = 0;
		}
		
		create = (kc == 69);
		if(kc == 82) bnw = !bnw;
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
			if(edit == 6) nmax = newNum;
			newNum = 0;
		}
		
		if(edit > 6) edit = 0;
		if(edit < 0) edit = 6;
		
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
		trace(atZoomX(e.getX()), -atZoomY(e.getY()));
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

}
