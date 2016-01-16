package mainClasses;

import java.awt.Graphics2D;

public class ArcForceField extends Drawable
{
	public Person	target;
	public double	arc;
	public double	minRadius;
	public double	maxRadius;
	public double	life;
	public double	extraLife;				// the extralife is for force fields that are not immediately created with full health, but instead gain the health during the first 1.5 seconds of their creation. Alternatively, for
	// force fields that are created with full health but slowly lose it every second.
	public double	maxLife;
	public int		elementNum;				// 12+ = not an element but a type of forcefield-matter (Protective Bubble, for example)
	public double	waitingDamage;			// to not spam UITexts
	public double	timeBetweenDamageTexts;
	public String	type;//Shield <Element>, Protective Bubble, Bubble, Mobile Force Field?

	public ArcForceField(Person target1, double angle1, double arc1, double minRadius1, double maxRadius1, double life1, int elementNum1, String type1)
	{
		maxLife = life1;
		target = target1;
		x = target.x;
		y = target.y;
		z = target.z;
		rotation = angle1;
		arc = arc1;
		minRadius = minRadius1;
		maxRadius = maxRadius1;
		elementNum = elementNum1;
		height = 1.5;
		waitingDamage = 0;
		timeBetweenDamageTexts = 0;
		type = type1;
		int frame = 0;
		if (elementNum < 12) // normal elemental shields
		{
			life = 1;
			extraLife = life1;
		} else // forcefield bubbles
		{
			life = maxLife;
			extraLife = 0;
		}
		updateImage();
		changeImage(Resources.arcForceFields[elementNum][frame]);
	}

	public void update(double deltaTime)
	{
		if (timeBetweenDamageTexts < 60)
			timeBetweenDamageTexts += deltaTime;
		if (extraLife > 0)
		{
			life += extraLife * deltaTime / 1.5;
			if (life >= extraLife)
				extraLife = 0;
		}
		if (elementNum < 12) // for elemental Shields
			if (extraLife < 0)
				life *= 0.8; // -20% every second.

		if (life < 1)
			life = 0;
		x = target.x;
		y = target.y;
		z = target.z;
	}

	public void updateImage()
	{
		// Currently force fields change images after lower than (e.g.) 75 health, not 75% health!
		int frame = 0;
		if (elementNum < 12) // number-based
		{
			if (life >= 75)
				frame = 0;
			else if (life >= 50)
				frame = 1;
			else if (life >= 15)
				frame = 2;
			else
				frame = 3;
		} else // percentage-based
		{
			if (life >= 0.75 * maxLife)
				frame = 0;
			else if (life >= 0.50 * maxLife)
				frame = 1;
			else if (life >= 0.25 * maxLife)
				frame = 2;
			else
				frame = 3;
		}
		changeImage(Resources.arcForceFields[elementNum][frame]);
	}

	public int damageType()
	{
		if (elementNum < 12)
			return EP.damageType(elementNum);
		else // force field
			return EP.damageType("Energy");
	}

	public void drawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		if (z > 0)
		{
			buffer.rotate(rotation, (int) (x + shadowX * z), (int) (y + shadowY * z));
			buffer.drawImage(shadow, (int) (x - image.getWidth() / 2 + shadowX * z), (int) (y - image.getHeight() / 2 + shadowY * z), null);
			buffer.rotate(-rotation, (int) (x + shadowX * z), (int) (y + shadowY * z));
		}
	}

	public void draw(Graphics2D buffer, double cameraZed)
	{
		if (z <= cameraZed)
		{
			buffer.translate(x, y);
			buffer.scale(z * Main.heightZoomRatio + 1, z * Main.heightZoomRatio + 1);
			buffer.translate(-x, -y);

			buffer.rotate(rotation, x, y);
			buffer.drawImage(image, (int) x - image.getWidth() / 2, (int) y - image.getHeight() / 2, null);
			buffer.rotate(-rotation, x, y);

			buffer.translate(x, y);
			buffer.scale(1 / (z * Main.heightZoomRatio + 1), 1 / (z * Main.heightZoomRatio + 1));
			buffer.translate(-x, -y);
		}
	}
}
