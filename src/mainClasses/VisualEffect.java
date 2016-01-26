package mainClasses;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class VisualEffect
{
	public double		timeLeft;
	public double		duration;
	public boolean		onTop;
	public Point		p1, p2;
	public double		z;
	public Point		p1p2variations;
	public List<Point>	points;
	public Color		color;
	public int			frame;
	public int			type, subtype;
	public double		angle;
	public double		size;

	/*
	 * 1 = successful blink. Random blue lines between entry and exit points.
	 * 
	 * 2 = unsuccessful blink. Random red lines between entry and exit.
	 * 
	 * 3 = healing beam. Green.
	 * 
	 * 4 = explosion (subtypes for different drawings)
	 */
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
		case 1: // teleport success
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
		case 2: // teleport fail
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
		case 3: // heal
			break;
		case 4: // explosion
			// explosions don't loop - they just stay invisible
			if (frame != -1 && frameNum % 4 == 0)
				frame++;
			if (frame >= Resources.explosions.get(subtype).size())
				frame = -1;
			break;
		default:
			Main.errorMessage("[1] Unused effect type has no update case:  " + type);
			break;
		}

	}

	public void draw(Graphics2D buffer)
	{
		switch (type)
		{
		case 1:
			buffer.setStroke(new BasicStroke(2));
			buffer.setColor(color);
			for (int i = 0; i < points.size() - 2; i++)
				buffer.drawLine(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
			break;
		case 2:
			buffer.setStroke(new BasicStroke(2));
			buffer.setColor(color);
			for (int i = 0; i < points.size() - 2; i++)
				buffer.drawLine(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
			break;
		case 3:
			int beamDistance = (int) Math.sqrt(Methods.DistancePow2(p1.x, p1.y, p2.x, p2.y));
			int numOfBeamImages = beamDistance / 100;
			int leftoverImageWidth = beamDistance % 100;
			buffer.rotate(angle, p1.x, p1.y);

			int imageHeight = Resources.healingBeam[0].getHeight();
			for (int i = 0; i < numOfBeamImages; i++)
			{
				buffer.drawImage(Resources.healingBeam[0].getSubimage(frame, 0, 100 - frame, imageHeight), (int) (p1.x + i * 100), (int) (p1.y - 0.5 * 100), null);
				if (frame != 0)
					buffer.drawImage(Resources.healingBeam[0].getSubimage(0, 0, frame, imageHeight), (int) (p1.x + i * 100 + 100 - frame), (int) (p1.y - 0.5 * 100), null);
			}
			if (leftoverImageWidth > 0)
			{
				if (frame + leftoverImageWidth <= 100)
					buffer.drawImage(Resources.healingBeam[0].getSubimage(frame, 0, leftoverImageWidth, imageHeight), (int) (p1.x + numOfBeamImages * 100), (int) (p1.y - 0.5 * 100), null);
				else
				{
					buffer.drawImage(Resources.healingBeam[0].getSubimage(frame, 0, 100 - frame, imageHeight), (int) (p1.x + numOfBeamImages * 100), (int) (p1.y - 0.5 * 100), null);
					buffer.drawImage(Resources.healingBeam[0].getSubimage(0, 0, leftoverImageWidth + frame - 100, imageHeight), (int) (p1.x + numOfBeamImages * 100 + 100 - frame),
							(int) (p1.y - 0.5 * 100), null);
				}
			}

			buffer.rotate(-angle, p1.x, p1.y);

			break;
		case 4: // explosions. Scaled by size.
			if (frame != -1)
			{
				buffer.drawImage(Resources.explosions.get(subtype).get(frame), p1.x - p2.x, p1.y - p2.y, null);
			}
			break;
		default:
			Main.errorMessage("[2] Unused effect type has no draw case:  " + type);
			break;
		}
	}
}
