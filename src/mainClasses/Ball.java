package mainClasses;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class Ball extends RndPhysObj
{
	public int elementNum;
	final int ballMass = 10; // TODO different per element, maybe depends on radius
	public Person creator;
	public List<Evasion> evasions;
	public boolean critical = false;
	public double timer;
	public double damage, pushback;

	final static public double smokeEffectRate = 0.03;

	public Ball(double x1, double y1, double z1, int en, double damage1, double pushback1, double angle, Person creator1)
	{
		this(en, damage1, pushback1, angle, creator1);
		x = x1;
		y = y1;
		z = z1;
	}

	public Ball(int en, double damage1, double pushback1, double angle, Person creator1)
	{
		super(-1, -1, 1, 1);
		rotation = 0;
		elementNum = en;
		damage = damage1;
		pushback = pushback1;
		creator = creator1;
		timer = 0;
		changeImage(Resources.balls[elementNum]); // Not all the same size!!!

		angularVelocity = 1.8 * Math.PI;
		height = 1;
		mass = ballMass;
		double velocity = Ball.giveVelocity(); // pixels per second. Below 330 and the character would probably accidentally touch them while running
		xVel = Math.cos(angle) * velocity;
		yVel = Math.sin(angle) * velocity;
		evasions = new ArrayList<Evasion>();
		
		switch (elementNum)
		{
		case 0: // fire
			radius = 20;
			break;
		case 1: // water
			break;
		case 2: // wind
			radius = 30;
			angularVelocity = 20;
			break;
		case 3: // electricity
			radius = 10;
			break;
		case 4: // metal
			radius = 30;
			break;
		case 5: // ice
			radius = 30;
			break;
		case 6: // energy
			radius = 15;
			break;
		case 7: // acid
			radius = 45;
			break;
		case 8: // lava
			radius = 45;
			break;
		case 9: // flesh
			radius = 30;
			angularVelocity = 4;
			break;
		case 10: // earth
			radius = 30;
			break;
		case 11: // plant
			radius = 30;
			break;
		default:
			MAIN.errorMessage("Oh why did I force myself to have default cases for every switch case ;-;");
		}
	}

	public void evadedBy(Person p)
	{
		evasions.add(new Evasion(p.id));
	}

	public static double giveVelocity()
	{
		// TODO perhaps give different speeds to different elements.
		return 500;
	}

	public double getDamage()
	{
		return damage * mass / ballMass;
	}

	public double getPushback()
	{
		return pushback * mass / ballMass; // balls deal an extra 1 pushback
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
			buffer.scale(z * MAIN.heightZoomRatio + 1, z * MAIN.heightZoomRatio + 1);
			buffer.translate(-x, -y);

			buffer.rotate(rotation, x, y);
			buffer.drawImage(image, (int) x - image.getWidth() / 2, (int) y - image.getHeight() / 2, null);
			buffer.rotate(-rotation, x, y);

			buffer.translate(x, y);
			buffer.scale(1 / (z * MAIN.heightZoomRatio + 1), 1 / (z * MAIN.heightZoomRatio + 1));
			buffer.translate(-x, -y);
		}
	}

}
