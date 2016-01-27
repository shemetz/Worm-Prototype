package mainClasses;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class Ball extends RndPhysObj
{
	public int		elementNum;
	public int		points;
	final int ballMass = 10;
	public Person creator;
	public List<Evasion> evasions;
	public boolean critical = false;

	public Ball(int en, int p1, double angle, Person creator1)
	{
		super(-1, -1, 1, 1);
		rotation = 0;
		elementNum = en;
		points = p1;
		creator = creator1;
		changeImage(Resources.balls[elementNum]); // Not all the same size!!!

		// TODO make mass, radius and velocity depend on element
		radius = 30;
		height = 1;
		mass = ballMass;
		double velocity = Ball.giveVelocity(p1); // pixels per second. Below 330 and the character would probably accidentally touch them while running
		xVel = Math.cos(angle) * velocity;
		yVel = Math.sin(angle) * velocity;
		angularVelocity = 1.8 * Math.PI;
		evasions = new ArrayList<Evasion>();
	}

	public void evadedBy(Person p)
	{
		evasions.add(new Evasion(p.id));
	}
	
	public static double giveVelocity(int pnts)
	{
		// TODO perhaps give different speeds to different elements.
		return 400 + 20 * pnts;
	}

	public double getDamage()
	{
		return 0.6 * points * Ability.elementalAttackNumbers[elementNum][0] * mass / ballMass;
	}

	public double getPushback()
	{
		return 0.6 * points * Ability.elementalAttackNumbers[elementNum][1] * mass / ballMass + 1; // balls deal an extra 1 pushback
	}

	public void trueDrawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		if (z > 1)
		{
			buffer.rotate(rotation, (int) (x + shadowX * z), (int) (y + shadowY * z));
			buffer.drawImage(shadow, (int) (x - image.getWidth() / 2 + shadowX * z), (int) (y - image.getHeight() / 2 + shadowY * z), null);
			buffer.rotate(-rotation, (int) (x + shadowX * z), (int) (y + shadowY * z));
		}
	}

	public void trueDraw(Graphics2D buffer, double cameraZed)
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
