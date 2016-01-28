package mainClasses;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import mainResourcesPackage.SoundEffect;

public class ForceField extends Drawable
{
	public int					length, width;
	public double				life;
	public int					maxLife;
	public int					armor;
	public Point[]				p;
	public int					type;
	public List<SoundEffect>	sounds	= new ArrayList<SoundEffect>();

	// TODO type
	public ForceField(double x1, double y1, double z1, int l1, int w1, double rotation1, int life1, int t1)
	{
		x = x1;
		y = y1;
		z = z1;
		rotation = rotation1;
		width = w1;
		length = l1;
		life = life1;
		maxLife = life1;
		type = t1;

		initializePoints();
		initSounds();

		// for the Drawable stuff
		image = new BufferedImage(length, length, BufferedImage.TYPE_INT_ARGB);
		shadow = image;

		// TEMP?
		armor = 0;
		height = 1;
	}

	void initializePoints()
	{
		p = new Point[4];
		double halfFFdiagonal = 0.5 * Math.sqrt(Math.pow(length, 2) + Math.pow(width, 2));
		// angle between length and width of force field
		double alpha = Math.atan2(width, length);
		// four points of rectangle. Maybe they should just be calculated and stored once, in the FF's class?
		p[0] = new Point((int) (x + halfFFdiagonal * Math.cos(rotation - alpha)), (int) (y + halfFFdiagonal * Math.sin(rotation - alpha)));
		p[1] = new Point((int) (x + halfFFdiagonal * Math.cos(rotation + alpha)), (int) (y + halfFFdiagonal * Math.sin(rotation + alpha)));
		p[2] = new Point((int) (x + halfFFdiagonal * Math.cos(rotation + Math.PI - alpha)), (int) (y + halfFFdiagonal * Math.sin(rotation + Math.PI - alpha)));
		p[3] = new Point((int) (x + halfFFdiagonal * Math.cos(rotation + Math.PI + alpha)), (int) (y + halfFFdiagonal * Math.sin(rotation + Math.PI + alpha)));
	}

	void initSounds()
	{
		sounds.add(new SoundEffect("Reflect.wav")); // 0 - when a beam hits the FF and is reflected
		sounds.get(0).endUnlessMaintained = true;
	}

	public void stopAllSounds()
	{
		for (int i = 0; i < sounds.size(); i++)
			sounds.get(i).stop();
	}

	public void drawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		if (z > 1)
		{
			buffer.setColor(new Color(0, 0, 0, 10 + 80 * (int) life / maxLife)); // fill color
			buffer.rotate(rotation, (int) (x + shadowX * z), (int) (y + shadowY * z));
			buffer.fillRect((int) (x - 0.5 * length + shadowX * z), (int) (y - 0.5 * width + shadowY * z), length, width);
			buffer.rotate(-rotation, (int) (x + shadowX * z), (int) (y + shadowY * z));
		}
	}

	public void draw(Graphics2D buffer, double cameraZed)
	{
		if (z < cameraZed)
		{
			buffer.translate(x, y);
			buffer.scale(z * Main.heightZoomRatio + 1, z * Main.heightZoomRatio + 1);
			buffer.translate(-x, -y);
			buffer.rotate(rotation, (int) (x), (int) (y));
			buffer.setColor(new Color(140, 255, 0, 48 + 80 * (int) life / maxLife)); // fill color
			buffer.fillRect((int) (x - 0.5 * length), (int) (y - 0.5 * width), length, width);
			if ((double) life / (double) maxLife < 0.5) // dashed stroke
				buffer.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_MITER, 2.0f, new float[]
				{ 4.0f }, 0.0f));
			else
				buffer.setStroke(new BasicStroke(4));
			if (life < 0)
				buffer.setColor(new Color(0, 236, 0, 64));
			else
				buffer.setColor(new Color(0, 236, 0, 64 + 110 * (int) life / maxLife)); // outline color
			buffer.drawRect((int) (x - 0.5 * length), (int) (y - 0.5 * width), length, width);
			buffer.rotate(-rotation, (int) (x), (int) (y));
			buffer.translate(x, y);
			buffer.scale(1 / (z * Main.heightZoomRatio + 1), 1 / (z * Main.heightZoomRatio + 1));
			buffer.translate(-x, -y);
		}
	}
}
