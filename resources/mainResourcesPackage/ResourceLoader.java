package mainResourcesPackage;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class ResourceLoader
{
	static ResourceLoader rl = new ResourceLoader();

	public static BufferedImage getBufferedImage(String fileName)
	{
		BufferedImage b = null;
		// Create the BufferedImage that we wish to return
		try
		{
			b = ImageIO.read(rl.getClass().getResource("images/" + fileName));
		} catch (Exception e)
		{
			 //e.printStackTrace(System.err); //TODO remove when all is done
		}
		return b;
	}

	public static Clip getClip(String fileName)
	{
		// Create the BufferedImage that we wish to return
		try
		{
			Clip c = AudioSystem.getClip();
			AudioInputStream input = AudioSystem.getAudioInputStream(rl.getClass().getResource("sounds/" + fileName));
			c.open(input);
			return c;
		} catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
		return null;
	}
}
