package mainClasses;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import mainResourcesPackage.SoundEffect;

public class Furniture extends Drawable
{

	public static double clickRangePow2 = 150 * 150;
	public static double standRangePow2 = 400 * 400;
	public int w, h; // for collision purposes

	public enum Type
	{
		WOOD_CHAIR, PLANT_POT, DESK, DOOR
	};

	public Type type;
	public int state;
	boolean clickable;
	public int life = 100;
	public int lifeDamageThingie = 100;
	public int debrisToCreate = 0;
	public int armor = 3; // TODO maybe uh not
	public BufferedImage originalImage;
	public List<SoundEffect> sounds;

	public Furniture(double x1, double y1, String typeString, double rotation1)
	{
		super();
		x = x1;
		y = y1;
		z = 0;
		sounds = new ArrayList<SoundEffect>();
		height = 0.9;
		rotation = rotation1;
		type = Type.valueOf(typeString.toUpperCase());
		switch (type)
		{
		case PLANT_POT:
			image = Resources.furniture.get("plant_pot");
			clickable = false;
			w = 54;
			h = 54;
			break;
		case WOOD_CHAIR:
			image = Resources.furniture.get("wood_chair");
			clickable = false;
			w = 46;
			h = 46;
			break;
		case DOOR:
			image = Resources.furniture.get("door");
			clickable = true;
			w = 96;
			h = 18;
			state = 0; // 0 = closed, 1 = open, 2 = locked?
			break;
		case DESK:
			state = (int) (Math.random() * 3);
			image = Resources.furniture.get("desk_" + state);
			clickable = false;
			w = 179;
			h = 83;
			break;
		default:
			MAIN.errorMessage("No excuses!");
			break;
		}
		sounds.add(new SoundEffect(type.toString() + " smash.wav"));
		sounds.add(new SoundEffect(type.toString() + " hit.wav"));
		for (SoundEffect s : sounds)
			s.setPosition(x, y);
		originalImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		originalImage.createGraphics().drawImage(image, 0, 0, null);
	}

	public void activate()
	{
		if (type == Type.DOOR)
		{
			if (state == 0) // closed
			{
				x += 48 * (Math.cos(rotation) - Math.sin(rotation));
				y += 48 * (Math.sin(rotation) - Math.cos(rotation));
				rotation = rotation + Math.PI / 2;
				state = 1;
			}
			else if (state == 1) // open
			{
				rotation = rotation - Math.PI / 2;
				x -= 48 * (Math.cos(rotation) - Math.sin(rotation));
				y -= 48 * (Math.sin(rotation) - Math.cos(rotation));
				state = 0;
			}
		}
	}

	public Point[] getPoints()
	{
		Point[] p = new Point[4];
		double halfDiagonal = 0.5 * Math.sqrt(Math.pow(w, 2) + Math.pow(h, 2));
		// angle between length and width of force field
		double alpha = Math.atan2(h, w);
		p[0] = new Point((int) (x + halfDiagonal * Math.cos(rotation - alpha)), (int) (y + halfDiagonal * Math.sin(rotation - alpha)));
		p[1] = new Point((int) (x + halfDiagonal * Math.cos(rotation + alpha)), (int) (y + halfDiagonal * Math.sin(rotation + alpha)));
		p[2] = new Point((int) (x + halfDiagonal * Math.cos(rotation + Math.PI - alpha)), (int) (y + halfDiagonal * Math.sin(rotation + Math.PI - alpha)));
		p[3] = new Point((int) (x + halfDiagonal * Math.cos(rotation + Math.PI + alpha)), (int) (y + halfDiagonal * Math.sin(rotation + Math.PI + alpha)));
		return p;
	}

	public void trueDraw(Graphics2D buffer, double cameraZed)
	{
		if (z <= cameraZed)
		{
			buffer.translate(x, y);
			buffer.scale(z * MAIN.heightZoomRatio + 1, z * MAIN.heightZoomRatio + 1);
			buffer.translate(-x, -y);

			buffer.rotate(rotation, x, y);

			buffer.drawImage(image, (int) x - image.getWidth() / 2, (int) y - image.getHeight() / 2, null);

			buffer.rotate(-rotation, x, y);

			buffer.translate(x, y);
			buffer.scale(1 / (z * MAIN.heightZoomRatio + 1), 1 / (z * MAIN.heightZoomRatio + 1));
			buffer.translate(-x, -y);
		}
	}

	public void trueDrawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
	}

	final double percentageOfDamage = 0.01;

	public void damage(int damage)
	{
		life -= damage;
		if (life < 0)
		{
			life = 0;
			debrisToCreate += 2;
			sounds.get(0).play();
		}
		if (damage > 2)
			sounds.get(1).play();
		while (life < lifeDamageThingie)
		{
			lifeDamageThingie -= 10;
			debrisToCreate++;
			int width1 = image.getWidth();
			int height1 = image.getHeight();
			BufferedImage newImage = new BufferedImage(width1, height1, image.getType());
			Graphics2D g = newImage.createGraphics();
			g.drawImage(image, 0, 0, null);
			for (int i = 0; i < width1 - 1; i++)
				for (int j = 0; j < height1 - 1; j++)
					if (newImage.getRGB(i, j) != 0x00000000)
						if (Math.random() < percentageOfDamage)
						{
							int RGB = newImage.getRGB(i, j);
							int R = (RGB >> 16) & 0xff;
							int G = (RGB >> 8) & 0xff;
							int B = (RGB >> 0) & 0xff;
							int white = (int) (Math.random() * 200) + 20;
							for (int ii = i; ii <= i + 1; ii++)
								for (int jj = j; jj <= j + 1; jj++)
									if (newImage.getRGB(ii, jj) != 0x00000000)
										newImage.setRGB(ii, jj, (new Color((R + white) / 2, ((G + white) / 2), (B + white) / 2)).getRGB());
						}
			image = newImage;
			g.dispose();
		}
	}
}
