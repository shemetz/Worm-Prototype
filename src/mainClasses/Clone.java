package mainClasses;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Clone extends NPC
{
	public Person master;
	public Ability creatorAbility;
	public double timeLeft; // at end of combat or when dead, this will become 1 and then slowly decrease over 1 second while the image is "disintegrated"
	public double disintegrationDuration = 3;

	public Clone(double x1, double y1, Person p, Ability creatorAbility1)
	{
		super(x1, y1, Strategy.AGGRESSIVE); // TODO maybe not aggressive?
		master = p;
		creatorAbility = creatorAbility1;
		legs = p.legs;
		chest = p.chest;
		head = p.head;
		hair = p.hair;
		nakedChest = p.nakedChest;
		nakedLegs = p.nakedLegs;
		initAnimation();
		commanderID = p.commanderID;
		inCombat = true;
		timeLeft = -1;
		name = master.name + "'s Clone";
	}

	@Override
	public void selfFrame(double deltaTime)
	{
		super.selfFrame(deltaTime);
		if (timeLeft == -1)
			if (!inCombat)
			{
				timeLeft = disintegrationDuration;
				// start cooldown on ability that summoned this
				for (Ability a : master.abilities)
					if (a.equals(creatorAbility))
					{
						a.on = false;
						a.cooldownLeft = a.cooldown;
					}
			}
	}

	@Override
	public void trueDraw(Graphics2D buffer, double cameraZed)
	{
		Random random = new Random();
		BufferedImage originalImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics2D g = originalImage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		if (timeLeft != -1)
			for (int i = 0; i < (int) (disintegrationDuration * (1 - timeLeft) * imgH * imgW); i++)
				image.setRGB(random.nextInt(imgW), random.nextInt(imgH), 0x00000000);
		super.trueDraw(buffer, cameraZed);
		image = originalImage;
	}

	@Override
	public void die()
	{
		super.die();
		timeLeft = disintegrationDuration;

		// start cooldown on ability that summoned this
		for (Ability a : master.abilities)
			if (a.equals(creatorAbility))
			{
				a.on = false;
				a.cooldownLeft = a.cooldown;
			}
	}
}
