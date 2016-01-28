package mainClasses;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

public class Drawable
{
	public double			x, y, z;					// x and y are of the center of the image (and object). z is in "meters". //TODO make z be "centimeters" / "pixels" (multiply or divide lots of things by 100)
	public double			rotation			= 0;
	public int				radius;
	public double			height;						// z is the bottom or the middle of an object; height is the leftover.
	public BufferedImage	image, shadow;
	public Portal			intersectedPortal	= null;

	public Drawable()
	{
	}

	public void trueDrawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		buffer.setColor(Color.darkGray);
		buffer.fillRect((int) (x - 10 + z * shadowX), (int) (y - 10 + z * shadowY), 20, 20);
	}

	@SuppressWarnings("unused")
	public void trueDraw(Graphics2D buffer, double cameraZed)
	{
		buffer.setColor(Color.red);
		buffer.fillRect((int) x - 10, (int) y - 10, 20, 20);
	}

	static int		shadowFuzziness	= 6;
	static float	shadowOpacity	= 0.6f;	// between 0f (transparent shadow) and 1f (opaque shadow)

	public void changeImage(BufferedImage image1)
	{
		// changes image to image1, and also creates a shadow effect of image1 in "shadow"
		image = image1;
		shadow = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D shadowG = shadow.createGraphics();
		shadowG.drawImage(image, 0, 0, null);
		shadowG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, shadowOpacity));
		shadowG.setColor(Color.black);
		shadowG.fillRect(0, 0, image.getWidth(), image.getHeight());
		shadowG.dispose();
		shadow = Methods.getGaussianBlurFilter(shadowFuzziness, true).filter(shadow, null);
		shadow = Methods.getGaussianBlurFilter(shadowFuzziness, false).filter(shadow, null);
		shadow = Methods.optimizeImage(shadow);
	}

	public static BufferedImage createShadow(BufferedImage image1)
	{
		// changes image to image1, and also creates a shadow effect of image1 in "shadow"
		BufferedImage shadow = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D shadowG = shadow.createGraphics();
		shadowG.drawImage(image1, 0, 0, null);
		shadowG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, shadowOpacity));
		shadowG.setColor(Color.black);
		shadowG.fillRect(0, 0, image1.getWidth(), image1.getHeight());
		shadowG.dispose();
		shadow = Methods.getGaussianBlurFilter(shadowFuzziness, true).filter(shadow, null);
		shadow = Methods.getGaussianBlurFilter(shadowFuzziness, false).filter(shadow, null);
		shadow = Methods.optimizeImage(shadow);
		return shadow;
	}

	public void draw(Graphics2D buffer, double cameraZed)
	{
		// This method should be overridden by any Drawable that does not interact with portals
		if (intersectedPortal == null || intersectedPortal.partner == null)
		{
			trueDraw(buffer, cameraZed);
			return;
		}
		// PORTAL INTERSECTION
		Portal p = intersectedPortal;

		double k = (p.end.x - p.start.x) * (this.y - p.start.y) - (p.end.y - p.start.y) * (this.x - p.start.x);
		// k is >0 if this is below p, <0 if this is above p, or 0 if this is in the middle of p

		Polygon clip = getClipOfPortal(p, k > 0);
		buffer.setClip(clip);
		trueDraw(buffer, cameraZed);
		buffer.setClip(null);
		clip = getClipOfPortal(p.partner, k <= 0);
		buffer.setClip(clip);
		buffer.translate(p.partner.x - p.x, p.partner.y - p.y);
		buffer.rotate(p.partner.angle - p.angle, p.x, p.y);
		trueDraw(buffer, cameraZed);
		buffer.rotate(-p.partner.angle + p.angle, p.x, p.y);
		buffer.translate(-p.partner.x + p.x, -p.partner.y + p.y);
		buffer.setClip(null);

		// If you change this method, change drawShadow too
	}

	public void drawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		// (Copy-paste of the draw() method above. IF it changes, change this)
		// This method should be overridden by any Drawable that does not interact with portals
		if (intersectedPortal == null || intersectedPortal.partner == null)
		{
			trueDrawShadow(buffer, shadowX, shadowY);
			return;
		}

		// PORTAL INTERSECTION
		Portal p = intersectedPortal;

		double k = (p.end.x - p.start.x) * (this.y - p.start.y) - (p.end.y - p.start.y) * (this.x - p.start.x);
		// k is >0 if this is below p, <0 if this is above p, or 0 if this is in the middle of p

		Polygon clip = getClipOfPortal(p, k > 0);
		buffer.setClip(clip);
		trueDrawShadow(buffer, shadowX, shadowY);
		buffer.setClip(null);
		clip = getClipOfPortal(p.partner, k <= 0);
		buffer.setClip(clip);
		buffer.translate(p.partner.x - p.x, p.partner.y - p.y);
		buffer.rotate(p.partner.angle - p.angle, p.x, p.y);
		trueDrawShadow(buffer, shadowX, shadowY);
		buffer.rotate(-p.partner.angle + p.angle, p.x, p.y);
		buffer.translate(-p.partner.x + p.x, -p.partner.y + p.y);
		buffer.setClip(null);
	}

	public Polygon getClipOfPortal(Portal p, boolean direction)
	{

		int k = direction ? 1 : -1;
		Polygon clip = new Polygon();
		clip.addPoint((int) (p.x - Math.cos(p.angle) * p.length * 1), (int) (p.y - Math.sin(p.angle) * p.length * 1));
		clip.addPoint((int) (p.x + Math.cos(p.angle) * p.length * 1), (int) (p.y + Math.sin(p.angle) * p.length * 1));
		clip.addPoint((int) (p.x + Math.cos(p.angle) * p.length * 1 + Math.cos(p.angle + Math.PI / 2) * p.length * 1 * k),
				(int) (p.y + Math.sin(p.angle) * p.length * 1 + Math.sin(p.angle + Math.PI / 2) * p.length * 1 * k));
		clip.addPoint((int) (p.x - Math.cos(p.angle) * p.length * 1 + Math.cos(p.angle + Math.PI / 2) * p.length * 1 * k),
				(int) (p.y - Math.sin(p.angle) * p.length * 1 + Math.sin(p.angle + Math.PI / 2) * p.length * 1 * k));
		return clip;
	}

	public double highestPoint()
	{
		return z + height;
	}
}
