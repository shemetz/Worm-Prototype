package mainClasses;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class VisualEffect
{
	public double timeLeft;
	public double duration;
	public boolean onTop;
	public Point p1, p2;
	public double z;
	public Point p1p2variations;
	public List<Point> points;
	public Color color;
	public int frame;
	public BufferedImage image;

	public enum Type
	{
		BLINK_SUCCESS, BLINK_FAIL, CONNECTING_BEAM, EXPLOSION, NO, TELEPORT, STATE_LOOP, WILD_POWER
	};

	public Type type;

	/*
	 * BLINK_SUCCESS = successful blink. Random blue lines between entry and exit points.
	 * 
	 * BLINK_FAIL = unsuccessful blink. Random red lines between entry and exit.
	 * 
	 * CONNECTING_BEAM = Beam, depends in the image variable.
	 * 
	 * TELEPORT = rotating inwards, fading, image of person at entry point of teleport
	 * 
	 * STATE_LOOP = three arcs that rotate inwards into person
	 * 
	 * EXPLOSION = explosion (subtypes for different drawings)
	 * 
	 * WILD_POWER = four points that move depending on subtype.
	 */
	public int subtype;
	public double angle;
	public double size;

	public VisualEffect()
	{
		points = new ArrayList<Point>();
		p1 = new Point();
		p2 = new Point();
		frame = 0;
		z = 0;
	}

	public void update(int frameNum)
	{
		final int numOfPoints = 7;
		final int variation = 30;
		switch (type)
		{
		case BLINK_SUCCESS: // teleport success
			if (frameNum % 4 == 0)
			{
				points.clear();
				Point pp1 = new Point((int) (p1.x + Math.random() * p1p2variations.x - 0.5 * p1p2variations.x), (int) (p1.y + Math.random() * p1p2variations.y - 0.5 * p1p2variations.y));
				Point pp2 = new Point((int) (p2.x + Math.random() * p1p2variations.x - 0.5 * p1p2variations.x), (int) (p2.y + Math.random() * p1p2variations.y - 0.5 * p1p2variations.y));
				for (int i = 0; i < numOfPoints + 1; i++)
					points.add(new Point((int) (pp1.x + i * (pp2.x - pp1.x) / numOfPoints + Math.random() * (variation * 2 + 1) - variation),
							(int) (pp1.y + i * (pp2.y - pp1.y) / numOfPoints + Math.random() * (variation * 2 + 1) - variation)));
				color = new Color(120, 190 - (int) (Math.random() * 80), 255 - (int) (Math.random() * 100), (int) (255 * timeLeft / duration));
			}
			break;
		case BLINK_FAIL: // teleport fail
			if (frameNum % 4 == 0)
			{
				points.clear();
				Point pp01 = new Point((int) (p1.x + Math.random() * p1p2variations.x - 0.5 * p1p2variations.x), (int) (p1.y + Math.random() * p1p2variations.y - 0.5 * p1p2variations.y));
				Point pp02 = new Point((int) (p2.x + Math.random() * p1p2variations.x - 0.5 * p1p2variations.x), (int) (p2.y + Math.random() * p1p2variations.y - 0.5 * p1p2variations.y));
				for (int i = 0; i < numOfPoints + 1; i++)
					points.add(new Point((int) (pp01.x + i * (pp02.x - pp01.x) / numOfPoints + Math.random() * (variation * 2 + 1) - variation),
							(int) (pp01.y + i * (pp02.y - pp01.y) / numOfPoints + Math.random() * (variation * 2 + 1) - variation)));
				color = new Color(255 - (int) (Math.random() * 100), 70, 60, (int) (255 * timeLeft / duration));
			}
			break;
		case CONNECTING_BEAM:
			break;
		case EXPLOSION: // explosion
			// explosions don't loop - they just stay invisible
			if (frame != -1 && frameNum % 3 == 0)
				frame++;
			if (frame >= Resources.explosions.get(subtype).size())
				frame = -1;
			break;
		case TELEPORT:
		case STATE_LOOP:
		case WILD_POWER:
		case NO:
			break;
		default:
			MAIN.errorMessage("[1] Unused effect type has no update case:  " + type);
			break;
		}
	}

	public void draw(Graphics2D buffer)
	{
		switch (type)
		{
		case WILD_POWER:
			double distance = 0;
			switch (subtype)
			{
			case 0: // ally
				distance = 60;
				buffer.setStroke(new BasicStroke(2));
				for (int i = 0; i < 4; i++)
				{
					buffer.setColor(Color.green);
					buffer.fillOval(-5 + (int) (p1.x + distance * Math.cos(i * Math.PI / 2 + timeLeft)), -5 + (int) (p1.y + distance * Math.sin(i * Math.PI / 2 + timeLeft)), 10, 10);
					buffer.setColor(Color.cyan);
					buffer.drawOval(-5 + (int) (p1.x + distance * Math.cos(i * Math.PI / 2 + timeLeft)), -5 + (int) (p1.y + distance * Math.sin(i * Math.PI / 2 + timeLeft)), 10, 10);
				}
				break;
			case 1: // enemy
				distance = 60;
				buffer.setStroke(new BasicStroke(2));
				for (int i = 0; i < 4; i++)
				{
					buffer.setColor(Color.red);
					buffer.fillOval(-5 + (int) (p1.x + distance * Math.cos(i * Math.PI / 2 - timeLeft)), -5 + (int) (p1.y + distance * Math.sin(i * Math.PI / 2 - timeLeft)), 10, 10);
					buffer.setColor(Color.orange);
					buffer.drawOval(-5 + (int) (p1.x + distance * Math.cos(i * Math.PI / 2 - timeLeft)), -5 + (int) (p1.y + distance * Math.sin(i * Math.PI / 2 - timeLeft)), 10, 10);
				}
				break;
			case 2: // ball
				buffer.setStroke(new BasicStroke(2));
				distance = 110 - timeLeft * timeLeft * 50;
				for (int i = 0; i < 3; i++)
				{
					buffer.setColor(Color.blue);
					buffer.fillOval(-5 + (int) (p1.x + distance * Math.cos(i * Math.PI * 2 / 3)), -5 + (int) (p1.y + distance * Math.sin(i * Math.PI * 2 / 3)), 10, 10);
					buffer.setColor(Color.magenta);
					buffer.drawOval(-5 + (int) (p1.x + distance * Math.cos(i * Math.PI * 2 / 3)), -5 + (int) (p1.y + distance * Math.sin(i * Math.PI * 2 / 3)), 10, 10);
				}
				distance = 10 + timeLeft * timeLeft * 50;
				for (double i = 0.5; i < 3.5; i++)
				{
					buffer.setColor(Color.blue);
					buffer.fillOval(-5 + (int) (p1.x + distance * Math.cos(i * Math.PI * 2 / 3)), -5 + (int) (p1.y + distance * Math.sin(i * Math.PI * 2 / 3)), 10, 10);
					buffer.setColor(Color.magenta);
					buffer.drawOval(-5 + (int) (p1.x + distance * Math.cos(i * Math.PI * 2 / 3)), -5 + (int) (p1.y + distance * Math.sin(i * Math.PI * 2 / 3)), 10, 10);
				}
				break;
			case 3: // wall / pool / floor
				buffer.setStroke(new BasicStroke(2));
				distance = 400 - timeLeft * timeLeft * 400;
				for (int i = 0; i < 4; i++)
				{
					buffer.setColor(Color.yellow);
					buffer.fillOval(-5 + (int) (p1.x + 300 * Math.cos(i * Math.PI / 2 + Math.PI / 4) + distance * Math.cos(i * Math.PI / 2 + Math.PI)),
							-5 + (int) (p1.y + 300 * Math.sin(i * Math.PI / 2 + Math.PI / 4) + distance * Math.sin(i * Math.PI / 2 + Math.PI)), 10, 10);
					buffer.setColor(Color.green);
					buffer.drawOval(-5 + (int) (p1.x + 300 * Math.cos(i * Math.PI / 2 + Math.PI / 4) + distance * Math.cos(i * Math.PI / 2 + Math.PI)),
							-5 + (int) (p1.y + 300 * Math.sin(i * Math.PI / 2 + Math.PI / 4) + distance * Math.sin(i * Math.PI / 2 + Math.PI)), 10, 10);
				}
				break;
			default:
				MAIN.errorMessage("ALERT: CXAXAKLUTHCXAXAKLUTHCXAXAKLUTH");
				break;
			}
			break;
		case STATE_LOOP:
			buffer.setColor(new Color(180, 255, 0));
			int radius = (int) (timeLeft * 120);
			for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / 3)
				buffer.drawArc(p1.x - radius, p1.y - radius, radius * 2, radius * 2, (int) ((i + timeLeft * 7) * 180 / Math.PI), 60);
			Composite original1 = buffer.getComposite();
			buffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) timeLeft * 0.7f));
			buffer.rotate(angle - Math.PI / 2, p1.x, p1.y);
			buffer.drawImage(image, p1.x - image.getWidth() / 2, p1.y - image.getHeight() / 2, null);
			buffer.rotate(-angle + Math.PI / 2, p1.x, p1.y);
			buffer.setComposite(original1);
			break;
		case TELEPORT:
			// draws image, spinning and shrinking
			buffer.rotate(20 * timeLeft + angle, p1.x, p1.y);
			buffer.translate(p1.x, p1.y);
			buffer.scale(timeLeft, timeLeft);
			buffer.translate(-p1.x, -p1.y);

			Composite original = buffer.getComposite();
			buffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) timeLeft));
			buffer.drawImage(image, p1.x - image.getWidth() / 2, p1.y - image.getHeight() / 2, null);
			buffer.setComposite(original);

			buffer.translate(p1.x, p1.y);
			buffer.scale(1 / timeLeft, 1 / timeLeft);
			buffer.translate(-p1.x, -p1.y);
			buffer.rotate(-20 * timeLeft - angle, p1.x, p1.y);
			break;
		case BLINK_SUCCESS:
			buffer.setStroke(new BasicStroke(2));
			buffer.setColor(color);
			for (int i = 0; i < points.size() - 2; i++)
				buffer.drawLine(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
			break;
		case BLINK_FAIL:
			buffer.setStroke(new BasicStroke(2));
			buffer.setColor(color);
			for (int i = 0; i < points.size() - 2; i++)
				buffer.drawLine(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
			break;
		case CONNECTING_BEAM:
			int beamDistance = (int) Math.sqrt(Methods.DistancePow2(p1.x, p1.y, p2.x, p2.y));
			int numOfBeamImages = beamDistance / 100;
			int leftoverImageWidth = beamDistance % 100;
			buffer.rotate(angle, p1.x, p1.y);

			int imageHeight = image.getHeight();
			for (int i = 0; i < numOfBeamImages; i++)
			{
				buffer.drawImage(image.getSubimage(frame, 0, 100 - frame, imageHeight), (int) (p1.x + i * 100), (int) (p1.y - 0.5 * 100), null);
				if (frame != 0)
					buffer.drawImage(image.getSubimage(0, 0, frame, imageHeight), (int) (p1.x + i * 100 + 100 - frame), (int) (p1.y - 0.5 * 100), null);
			}
			if (leftoverImageWidth > 0)
			{
				if (frame + leftoverImageWidth <= 100)
					buffer.drawImage(image.getSubimage(frame, 0, leftoverImageWidth, imageHeight), (int) (p1.x + numOfBeamImages * 100), (int) (p1.y - 0.5 * 100), null);
				else
				{
					buffer.drawImage(image.getSubimage(frame, 0, 100 - frame, imageHeight), (int) (p1.x + numOfBeamImages * 100), (int) (p1.y - 0.5 * 100), null);
					buffer.drawImage(image.getSubimage(0, 0, leftoverImageWidth + frame - 100, imageHeight), (int) (p1.x + numOfBeamImages * 100 + 100 - frame), (int) (p1.y - 0.5 * 100), null);
				}
			}

			buffer.rotate(-angle, p1.x, p1.y);

			break;
		case EXPLOSION: // explosions. Scaled by size.
			if (frame != -1)
			{
				buffer.drawImage(Resources.explosions.get(subtype).get(frame), p1.x - p2.x, p1.y - p2.y, null);
			}
			break;
		case NO:
			break;
		default:
			MAIN.errorMessage("[2] Unused effect type has no draw case:  " + type);
			break;
		}
	}
}
