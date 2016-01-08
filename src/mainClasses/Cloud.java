package mainClasses;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Cloud extends Drawable
{
	public Cloud(int x1, int y1, int z1, int cloudImageNum)
	{
		x = x1;
		y = y1;
		z = z1;
		image = Resources.clouds.get(cloudImageNum);
		shadow = Resources.cloudShadows.get(cloudImageNum);
	}

	public static BufferedImage cloudShadow(BufferedImage image)
	{
		BufferedImage shadow = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D shadowG = shadow.createGraphics();
		shadowG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
		shadowG.setColor(new Color(0, 0, 0, 0));
		shadowG.drawImage(image, 0, 0, null); // takes a lot of calculation time if the picture is large
		shadowG.setXORMode(Color.white);
		shadowG.fillRect(0, 0, image.getWidth(), image.getHeight());
		shadowG.dispose();
		return Methods.optimizeImage(shadow); // takes a lot of calculation time if the picture is large
	}

	public void drawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		buffer.rotate(rotation - 0.5 * Math.PI, (int) (x + shadowX * z), (int) (y + shadowY * z));
		buffer.drawImage(shadow, (int) (x - image.getWidth() / 2 + shadowX * z), (int) (y - image.getHeight() / 2 + shadowY * z), null);
		buffer.rotate(-rotation + 0.5 * Math.PI, (int) (x + shadowX * z), (int) (y + shadowY * z));
	}

	public void draw(Graphics2D buffer, double cameraZed)
	{
		if (z <= cameraZed)
		{
			buffer.translate(x, y);
			buffer.scale(z * Main.heightZoomRatio + 1, z * Main.heightZoomRatio + 1);
			buffer.translate(-x, -y);

			buffer.drawImage(image, (int) x - image.getWidth() / 2, (int) y - image.getHeight() / 2, null);

			buffer.translate(x, y);
			buffer.scale(1 / (z * Main.heightZoomRatio + 1), 1 / (z * Main.heightZoomRatio + 1));
			buffer.translate(-x, -y);
		}
	}
}
