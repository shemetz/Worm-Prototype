package mainClasses;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Drawable
{
	public double x, y, z; //x and y are of the center of the image (and object). z is in "meters". //TODO make z be "centimeters" / "pixels" (multiply or divide lots of things by 100)
	public double rotation = 0;
	public double height; //z is the bottom or the middle of an object; height is the leftover.
	public BufferedImage image, shadow;
	public Portal intersectedPortal = null;
	
	public Drawable(){}
	public void drawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		buffer.setColor(Color.darkGray);
		buffer.fillRect((int)(x-10 + z*shadowX), (int)(y-10+ z*shadowY), 20, 20);
	}
	@SuppressWarnings("unused")
	public void draw(Graphics2D buffer, double cameraZed)
	{
		buffer.setColor(Color.red);
		buffer.fillRect((int)x-10, (int)y-10, 20, 20);
	}
	static int shadowFuzziness = 6;
	static float shadowOpacity = 0.6f; //between 0f (transparent shadow) and 1f (opaque shadow)
	public void changeImage(BufferedImage image1)
	{
		//changes image to image1, and also creates a shadow effect of image1 in "shadow"
		image = image1;
		shadow = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D shadowG = shadow.createGraphics();
		shadowG.drawImage(image, 0, 0, null);
		shadowG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, shadowOpacity));
		shadowG.setColor(Color.black);
		shadowG.fillRect(0, 0, image.getWidth(),  image.getHeight());
		shadowG.dispose();
		shadow = Methods.getGaussianBlurFilter(shadowFuzziness, true).filter(shadow, null);
        shadow = Methods.getGaussianBlurFilter(shadowFuzziness, false).filter(shadow, null);
		shadow = Methods.optimizeImage(shadow);
	}
	public static BufferedImage createShadow (BufferedImage image1)
	{
		//changes image to image1, and also creates a shadow effect of image1 in "shadow"
		BufferedImage shadow = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D shadowG = shadow.createGraphics();
		shadowG.drawImage(image1, 0, 0, null);
		shadowG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, shadowOpacity));
		shadowG.setColor(Color.black);
		shadowG.fillRect(0, 0, image1.getWidth(),  image1.getHeight());
		shadowG.dispose();
		shadow = Methods.getGaussianBlurFilter(shadowFuzziness, true).filter(shadow, null);
        shadow = Methods.getGaussianBlurFilter(shadowFuzziness, false).filter(shadow, null);
		shadow = Methods.optimizeImage(shadow);
		return shadow;
	}
	public double highestPoint()
	{
		return z + height;
	}
}
