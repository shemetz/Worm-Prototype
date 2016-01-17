package mainClasses;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class SprayDrop extends RndPhysObj
{
	public int		elementNum;
	public int		points;
	public Person	creator;
	public List<Evasion> evasions;

	public SprayDrop(double x1, double y1, double z1, int elementNumber, int level, double angle, double velocity, Person creator1)
	{
		super(x1, y1, 1, 1);
		z = z1;
		rotation = angle;
		elementNum = elementNumber;
		points = level;
		creator = creator1;
		int type = (int)(Math.random()*3);
		changeImage(Resources.sprayDrops[elementNum][type]); // Not all the same size!!!

		radius = 10;
		height = 1;
		xVel = Math.cos(angle) * velocity;
		yVel = Math.sin(angle) * velocity;
		angularVelocity = 1.8 * Math.PI;
		mass = 0.1;
		evasions = new ArrayList<Evasion>();
	}

	public void evadedBy(Person p)
	{
		evasions.add(new Evasion(p.id));
	}

	public double getDamage()
	{
		return 0.6 * points * Ability.elementalAttackNumbers[elementNum][0] * mass * Ability.elementalAttackNumbers[elementNum][2];
	}

	public double getPushback()
	{
		return 0.6 * points * Ability.elementalAttackNumbers[elementNum][1] * mass * Ability.elementalAttackNumbers[elementNum][2];
	}

	public void drawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		// no shadow
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
