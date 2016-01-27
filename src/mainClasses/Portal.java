package mainClasses;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Portal extends Drawable
{
	// Portals are flat and in midair
	public Portal	partner;
	// X and Y are for center
	public double	length;
	public double	angle;
	public Point3D	start;
	public Point3D	end;

	public Portal(double x_, double y_, double z_, double angle1, double length1)
	{
		x = x_;
		y = y_;
		z = z_;
		angle = angle1;
		length = length1;
		start = new Point3D((int) (x - Math.cos(angle) * length * 0.5), (int) (y - Math.sin(angle) * length * 0.5), z);
		end = new Point3D((int) (x + Math.cos(angle) * length * 0.5), (int) (y + Math.sin(angle) * length * 0.5), z);
		image = new BufferedImage((int) length, (int) length, BufferedImage.TYPE_INT_ARGB);
	}

	public void drawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{

	}

	public void draw(Graphics2D buffer, double cameraZed)
	{
		if (z <= cameraZed)
		{
			buffer.setStroke(new BasicStroke(8));
			if (partner == null)
				buffer.setColor(Color.black);
			else
				buffer.setColor(Color.white);
			buffer.drawLine(start.x, start.y, end.x, end.y);
			buffer.setStroke(new BasicStroke(4));
			buffer.setColor(Color.gray);
			buffer.drawLine(start.x, start.y, end.x, end.y);
		}
	}

	public void join(Portal p2)
	{
		this.partner = p2;
		p2.partner = this;
	}
}
