package mainClasses;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

import mainResourcesPackage.SoundEffect;

public class Portal extends Drawable
{
	// Portals are flat and in midair
	public Portal partner;
	// X and Y are for center
	public double length;
	public double angle;
	public Point3D start;
	public Point3D end;
	boolean blackOrWhite;
	Color black = Color.black;
	Color white = Color.white;
	double slope;
	SoundEffect sound;
	Polygon clip1;
	Polygon clip2;

	public Portal(double x_, double y_, double z_, double angle1, double length1)
	{
		x = x_;
		y = y_;
		z = z_;
		height = 1;
		angle = angle1;
		length = length1;
		start = new Point3D((int) (x - Math.cos(angle) * length * 0.5), (int) (y - Math.sin(angle) * length * 0.5), z);
		end = new Point3D((int) (x + Math.cos(angle) * length * 0.5), (int) (y + Math.sin(angle) * length * 0.5), z);
		image = new BufferedImage((int) length, (int) length, BufferedImage.TYPE_INT_ARGB);
		slope = length / 6 / 6;
		this.sound = new SoundEffect("Portal_1.wav"); // is not used, but is needed, temporarily
		setClips();
	}

	public void setClips()
	{
		int k = 1;
		clip1 = new Polygon();
		clip1.addPoint((int) (x - Math.cos(angle) * length * 1), (int) (y - Math.sin(angle) * length * 1));
		clip1.addPoint((int) (x + Math.cos(angle) * length * 1), (int) (y + Math.sin(angle) * length * 1));
		clip1.addPoint((int) (x + Math.cos(angle) * length * 1 + Math.cos(angle + Math.PI / 2) * length * 1 * k),
				(int) (y + Math.sin(angle) * length * 1 + Math.sin(angle + Math.PI / 2) * length * 1 * k));
		clip1.addPoint((int) (x - Math.cos(angle) * length * 1 + Math.cos(angle + Math.PI / 2) * length * 1 * k),
				(int) (y - Math.sin(angle) * length * 1 + Math.sin(angle + Math.PI / 2) * length * 1 * k));
		k = -1;
		clip2 = new Polygon();
		clip2.addPoint((int) (x - Math.cos(angle) * length * 1), (int) (y - Math.sin(angle) * length * 1));
		clip2.addPoint((int) (x + Math.cos(angle) * length * 1), (int) (y + Math.sin(angle) * length * 1));
		clip2.addPoint((int) (x + Math.cos(angle) * length * 1 + Math.cos(angle + Math.PI / 2) * length * 1 * k),
				(int) (y + Math.sin(angle) * length * 1 + Math.sin(angle + Math.PI / 2) * length * 1 * k));
		clip2.addPoint((int) (x - Math.cos(angle) * length * 1 + Math.cos(angle + Math.PI / 2) * length * 1 * k),
				(int) (y - Math.sin(angle) * length * 1 + Math.sin(angle + Math.PI / 2) * length * 1 * k));
	}

	public Polygon getClip(boolean direction)
	{
		return direction ? clip1 : clip2;
	}

	public void playPortalSound()
	{
		if (!sound.active)
			sound.play();
	}

	public Portal(Line2D line)
	{
		// For collision detection purposes
		start = new Point3D((int) line.getX1(), (int) line.getY1(), z);
		end = new Point3D((int) line.getX2(), (int) line.getY2(), z);
	}

	public Line2D Line2D()
	{
		return new Line2D.Double(start.x, start.y, end.x, end.y);
	}

	public Line2D OtherLine2D()
	{
		return new Line2D.Double(end.x, end.y, start.x, start.y);
	}

	public void drawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		if (z > 1)
		{
			buffer.translate(x, y);
			buffer.scale(z * MAIN.heightZoomRatio + 1, z * MAIN.heightZoomRatio + 1);
			buffer.translate(-x, -y);

			buffer.setColor(new Color(0, 0, 0, 128));
			buffer.setStroke(new BasicStroke(2));

			buffer.drawLine((int) (start.x), (int) (start.y), (int) (end.x), (int) (end.y));

			buffer.translate(x, y);
			buffer.scale(1 / (z * MAIN.heightZoomRatio + 1), 1 / (z * MAIN.heightZoomRatio + 1));
			buffer.translate(-x, -y);
		}
	}

	public void draw(Graphics2D buffer, double cameraZed)
	{
		if (z <= cameraZed)
		{
			buffer.translate(x, y);
			buffer.scale(z * MAIN.heightZoomRatio + 1, z * MAIN.heightZoomRatio + 1);
			buffer.translate(-x, -y);

			buffer.setStroke(new BasicStroke(2));

			buffer.setColor((blackOrWhite ? black : white));
			for (int i = 1; i < 6; i++)
			{
				buffer.drawLine((int) (start.x + i * Math.cos(angle + Math.PI / 2) + i * i * slope * Math.cos(angle)),
						(int) (start.y + i * Math.sin(angle + Math.PI / 2) + i * i * slope * Math.sin(angle)), (int) (end.x + i * Math.cos(angle + Math.PI / 2) - i * i * slope * Math.cos(angle)),
						(int) (end.y + i * Math.sin(angle + Math.PI / 2) - i * i * slope * Math.sin(angle)));
			}

			buffer.setStroke(new BasicStroke(1));
			buffer.drawLine((int) (start.x), (int) (start.y), (int) (end.x), (int) (end.y));

			buffer.setStroke(new BasicStroke(2));
			buffer.setColor((blackOrWhite ? white : black));

			for (int i = 1; i < 6; i++)
			{
				buffer.drawLine((int) (start.x - i * Math.cos(angle + Math.PI / 2) + i * i * slope * Math.cos(angle)),
						(int) (start.y - i * Math.sin(angle + Math.PI / 2) + i * i * slope * Math.sin(angle)), (int) (end.x - i * Math.cos(angle + Math.PI / 2) - i * i * slope * Math.cos(angle)),
						(int) (end.y - i * Math.sin(angle + Math.PI / 2) - i * i * slope * Math.sin(angle)));
			}

			buffer.translate(x, y);
			buffer.scale(1 / (z * MAIN.heightZoomRatio + 1), 1 / (z * MAIN.heightZoomRatio + 1));
			buffer.translate(-x, -y);
		}
	}

	public void join(Portal p2)
	{
		this.partner = p2;
		p2.partner = this;
		this.blackOrWhite = false;
		p2.partner.partner.partner.partner.blackOrWhite = true; // :)
		this.sound = new SoundEffect("Portal_1.wav");
		p2.sound = new SoundEffect("Portal_2.wav");
		this.sound.setPosition(x, y);
		p2.sound.setPosition(p2.x, p2.y);
	}
}
