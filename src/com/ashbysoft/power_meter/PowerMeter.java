// GUI display of waveform from power metering head
package com.ashbysoft.power_meter;

import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URLDecoder;

import com.ashbysoft.swingland.Graphics;
import com.ashbysoft.swingland.Component;
import com.ashbysoft.swingland.Color;
import com.ashbysoft.swingland.event.KeyListener;
import com.ashbysoft.swingland.event.KeyEvent;

import java.net.Socket;

//import com.ashbysoft.swingland.SwingUtilities;
import com.ashbysoft.swingland.JFrame;

public class PowerMeter extends Component implements Runnable, KeyListener {
	private JFrame frame;
	private boolean dopau = false;
	private boolean doeye = true;
	private boolean shelp = true;
	private File serio;
	private Socket data;
	private int spos = 0;
	private int gain = 100;
	private short samples[] = new short[1000];

	public static void main(String[] args) throws Exception {
		PowerMeter me = new PowerMeter(args);
	}

	PowerMeter(String[] args) throws Exception {
		// Network data or local program?
		this.serio = null;
		this.data = null;
		if (args.length>0) {
			if (args.length>1)
				this.data = new Socket(args[0], Integer.parseInt(args[1]));
			else
				this.data = new Socket("localhost", Integer.parseInt(args[0]));
		} else {
			// Check serio interface program is available
			String p1 = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			String base = URLDecoder.decode(p1, "UTF-8");
			if (base.endsWith(".jar")) {
				base = base.substring(0, base.lastIndexOf('/'));
			}
			this.serio = new File(base + File.separator + "serio");
			if (!serio.exists()) {
				throw new Exception("missing serio program in jar folder");
			}
		}
		// Build GUI
		frame = new JFrame("Power Meter: raw (unscaled) current samples");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(samples.length, 600);
		frame.add(this);
		frame.addKeyListener(this);
		frame.setVisible(true);
		// Read samples..
		Thread thr = new Thread(this);
		thr.setDaemon(true);
		thr.start();
	}

	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0,0,getWidth(), getHeight());
		int hy = getHeight()/2;
		short mn = 32767, mx = -32767;
		long ms = 0;
		// centreline
		g.setColor(Color.RED);
		g.drawLine(0, hy, getWidth(), hy);
		g.setColor(Color.GRAY);
		for (int i=0; i<getHeight(); i++) {
			// graticule bar every 50px, apart from centreline!
			int gy = hy-i;
			if (gy!=0 && gy%50==0) {
				int v = (32767*gy)/(hy*gain);
				g.drawLine(0, i, getWidth(), i);
				g.drawString(""+v, 10, i-2);
			}
		}
		// raw samples
		g.setColor(Color.GREEN);
		int ly = hy;
		for (int i=1; i<getWidth(); i++) {
			int y = i<samples.length ? hy-((int)samples[i]*gain*hy)/32767 : hy;
			g.drawLine(i-1,ly,i,y);
			ly = y;
			if (i<samples.length) {
				mn = (samples[i]<mn) ? samples[i] : mn;
				mx = (samples[i]>mx) ? samples[i] : mx;
				ms += (long)samples[i]*(long)samples[i];
			}
		}
		ms = (long)Math.sqrt(ms/samples.length);
		// eye diagram in top right 1/3 screen
		if (doeye) {
			hy /= 3;
			ly = hy;
			int eo = getWidth()*2/3-1;
			g.setColor(Color.DARK_GRAY);
			g.fillRect(eo, 0, getWidth()-1, hy*2);
			g.setColor(Color.LIGHT_GRAY);
			g.drawRect(eo, 0, getWidth()-1, hy*2);
			int ex = 10;
			int et = 0;
			int sx = (getWidth()/3)/ex;
			g.setColor(Color.GREEN);
			for (int i=1; i<samples.length; i++) {
				int y = hy-((int)samples[i]*gain*hy)/32767;
				// trace running?
				if (ex<9) {
					ex += 1;
					g.drawLine(eo+sx*(ex-1), ly, eo+sx*ex, y);
				} else {
					// zero crossing detect to trigger trace
					if ((ly>=hy && y<hy) || (ly<=hy && y>hy)) {
						ex = 0;
						et += 1;
					}
				}
				ly = y;
			}
		}
		// stats
		g.setColor(Color.RED);
		g.drawString("gain:"+gain+" min:"+mn+" max:"+mx+" rms:"+ms, 10, 10);
		// paused/help text
		g.setColor(Color.YELLOW);
		if (dopau) {
			g.drawString("PAUSED", 10, 25);
		}
		if (shelp) {
			for (int i=0; i<htext.length; i++)
				g.drawString(htext[i], getWidth()/2-100, getHeight()/2-100+15*i);
		}
	}

	private String[] htext = {
		"HELP!",
		" F1   => toggle help :)",
		" PgUp => increase gain",
		" PgDn => decrease gain",
		" E    => toggle eye diagram",
		" P    => toggle pause",
		" Q    => Quit"
	};

	public void keyPressed(KeyEvent k) {
		if (k.getKeyCode()==KeyEvent.VK_F1)
			shelp=!shelp;
		else if (k.getKeyCode()==KeyEvent.VK_PAGE_UP)
			gain+=10;
		else if (k.getKeyCode()==KeyEvent.VK_PAGE_DOWN)
			gain-=10;
		else if (k.getKeyCode()==KeyEvent.VK_P)
			dopau=!dopau;
		else if (k.getKeyCode()==KeyEvent.VK_E)
			doeye=!doeye;
		else if (k.getKeyCode()==KeyEvent.VK_Q)
			frame.dispose();
		if (gain<10)
			gain = 10;
		if (gain>1000)
			gain = 1000;
	}
	public void keyReleased(KeyEvent k) {}
	public void keyTyped(KeyEvent k) {}

	public void run() {
		try {
			BufferedReader bread = null;
			if (serio!=null) {
				// Kick off serio, plot waveform scope style..
				Process pserio = Runtime.getRuntime().exec(serio.toString());
				bread = new BufferedReader(new InputStreamReader(pserio.getInputStream()));
			} else if (data!=null) {
				bread = new BufferedReader(new InputStreamReader(data.getInputStream()));
			}
			String line;
			while (bread!=null && (line=bread.readLine())!=null) {
				if (line.length()>0) {
					try {
						int t = Integer.parseInt(line, 16);
						if (!dopau)
							samples[spos] = (short)t;
						if (++spos>=samples.length) {
							spos=0;
						}
						if (spos%100==0)
							repaint();
					} catch (NumberFormatException nf) {
						System.out.println("unparsable="+line);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
