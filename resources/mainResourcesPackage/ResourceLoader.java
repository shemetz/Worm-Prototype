package mainResourcesPackage;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
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
			// Make sure to update the fileName parameter to make it work with
			// your directory setup
		} catch (Exception e)
		{
			 //e.printStackTrace(System.err); //TODO
		}
		return b;
	}

	public static Clip getClip(String fileName)
	{
		// Create the BufferedImage that we wish to return
		try
		{
			Clip c = AudioSystem.getClip();
			c.open(AudioSystem.getAudioInputStream(rl.getClass().getResource("sounds/" + fileName)));
			return c;
			// Make sure to update the fileName parameter to make it work with
			// your directory setup
		} catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
		return null;
	}
}
