package mainResourcesPackage;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class SoundEffect
{
	Clip			sound;
	FloatControl	volumeControl;
	double			volume;
	double			length;				// in seconds
	public boolean	active;
	public boolean	justActivated;		// becomes false at the beginning of every frame, if it's false at the end the sound is stopped in certain cases
	public String	type;
	public boolean	paused		= false;
	boolean			loopOrPlay	= false;

	public SoundEffect(String fileName, String type1)
	{
		type = type1;
		sound = ResourceLoader.getClip(fileName);
		volumeControl = (FloatControl) sound.getControl(FloatControl.Type.MASTER_GAIN);
		volume = volumeControl.getValue(); // default is 0, can be between -80 and 6.02 for some reason (dB). Logarithmic. ~-25 is unheardable, and there seems to be no difference between 0 and 6.
		active = false;
		justActivated = false;
		length = (double) sound.getMicrosecondLength() / 1000000; // important that it's a double

		// Only SOME of the .wav files support Balance Control (playing the sound from one speaker louder than the other speaker) or Pan Control.
		// If you want to use that feature, there you go!

		// if (sound.isControlSupported(FloatControl.Type.BALANCE))
		// {
		// FloatControl a = (FloatControl) sound.getControl(FloatControl.Type.BALANCE);
		// a.setValue((float) (Math.random()*2 - 1));
		// }
	}

	public void stopIfEnded()
	{
		if (sound.getFramePosition() == sound.getFrameLength())
		{
			stop();
		}
	}

	public void setVolume(double newVolume)
	{
		// between 0 and 1.
		// actually between -infinity and 2, apparently? TODO ?
		volume = newVolume;
		volumeControl.setValue((float) (Math.log(newVolume) / Math.log(10.0) * 20.0));
	}

	public void loop()
	{
		active = true;
		justActivated = true;
		sound.loop(Clip.LOOP_CONTINUOUSLY);
		loopOrPlay = true;
	}

	public void play()
	{
		active = true;
		justActivated = true;
		sound.start();
		loopOrPlay = false;
	}

	public void stop()
	{
		active = false;
		justActivated = false;
		paused = false;
		sound.stop();
		sound.setFramePosition(0);
	}

	public void pause()
	{
		if (active)
		{
			active = false;
			justActivated = false;
			sound.stop();
			paused = true;
		}
	}

	public void cont()// inue
	{
		if (paused)
		{
			if (loopOrPlay)
				loop();
			else
				play();
			paused = false;
		}
	}
}
