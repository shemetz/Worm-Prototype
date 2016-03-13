package mainClasses;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class FurnitureDebris extends Debris
{

	public FurnitureDebris(double x1, double y1, double z1, double a1, BufferedImage img, double v1)
	{
		super(x1, y1, z1, a1, -1, v1);

		int w = 10 + (int) (Math.random() * 20);
		int h = 10 + (int) (Math.random() * 20);
		int randX = (int) (Math.random() * (img.getWidth() - w));
		int randY = (int) (Math.random() * (img.getHeight() - h));
		while (img.getRGB(randX, randY) == 0x00000000)
		{
			randX = (int) (Math.random() * (img.getWidth() - w));
			randY = (int) (Math.random() * (img.getHeight() - h));
		}
		image = new BufferedImage(w, h, img.getType());
		Graphics2D g = image.createGraphics();
		g.drawImage(img.getSubimage(randX, randY, w, h), 0, 0, null);
		g.dispose();

		// make it all shardy
		// left up
		int tw = w / 4 + (int) (Math.random() * w / 2);
		int th = h / 4 + (int) (Math.random() * h / 2);
		double ratio = (double) th / (double) tw;
		for (int i = 0; i < tw; i++)
			for (int j = 0; j < th; j++)
				if (j < th - (int) (ratio * i))
					image.setRGB(i, j, 0x00000000); // make transparent
		// down right
		tw = w / 4 + (int) (Math.random() * w / 2);
		th = h / 4 + (int) (Math.random() * h / 2);
		ratio = (double) th / (double) tw;
		for (int i = 0; i < tw; i++)
			for (int j = 0; j < th; j++)
				if (j < th - (int) (ratio * i))
					image.setRGB(w - i - 1, h - j - 1, 0x00000000); // make transparent

		shadow = new BufferedImage(w, h, img.getType());
		for (int i = 0; i < w; i++)
			for (int j = 0; j < h; j++)
				if (image.getRGB(i, j) != 0x00000000)
					shadow.setRGB(i, j, 0x99000000); // just fill it with mostly-transparent black
		radius = (w + h) / 2;
	}

}
