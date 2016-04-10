package mainClasses;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A Ball. Flies through the air until it hits an object and shatters.
 * 
 * @author Itamar
 *
 */
public class Ball extends RndPhysObj
{
	public int elementNum;
	final int maxMass = 10; // TODO different per element, maybe depends on radius
	public Person creator;
	public List<Evasion> evasions;
	public boolean critical = false;
	public double timer;
	public double damage, pushback;
	public double size;

	final static public double smokeEffectRate = 0.03;

	/**
	 * Constructor that also has coordinates in it
	 * 
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param elementNumber
	 * @param damage1
	 * @param pushback1
	 * @param angle
	 * @param creator1
	 * @param velocity
	 */
	public Ball(double x1, double y1, double z1, int elementNumber, double damage1, double pushback1, double angle, Person creator1, double velocity)
	{
		this(elementNumber, damage1, pushback1, angle, creator1, velocity);
		x = x1;
		y = y1;
		z = z1;
	}

	/**
	 * Constructor that doesn't have coordinates in it
	 * 
	 * @param elementNumber
	 * @param damage1
	 * @param pushback1
	 * @param angle
	 * @param creator1
	 * @param velocity
	 */
	public Ball(int elementNumber, double damage1, double pushback1, double angle, Person creator1, double velocity)
	{
		super(-1, -1, 1, 1);
		rotation = 0;
		elementNum = elementNumber;
		damage = damage1;
		pushback = pushback1;
		creator = creator1;
		timer = 0;
		changeImage(Resources.balls[elementNum]); // Not all the same size!!!

		angularVelocity = 1.8 * Math.PI;
		height = 1;
		mass = maxMass;
		size = 1;
		xVel = Math.cos(angle) * velocity;
		yVel = Math.sin(angle) * velocity;
		evasions = new ArrayList<Evasion>();

		switch (elementNum)
		{
		case 0: // fire
			radius = 20;
			break;
		case 1: // water
			radius = 30;
			break;
		case 2: // wind
			radius = 35;
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
			radius = 40;
			break;
		case 9: // flesh
			radius = 35;
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

	/**
	 * Makes this evaded by a person
	 * 
	 * @param p
	 */
	public void evadedBy(Person p)
	{
		evasions.add(new Evasion(p.id));
	}

	/**
	 * Sets the size to the new size, and updates mass and radius
	 * 
	 * @param newSize
	 */
	public void setSize(double newSize)
	{
		mass *= newSize / size;
		radius *= newSize / size;
		size = newSize;
	}

	/**
	 * 
	 * @return damage * mass / maxMass
	 */
	public double getDamage()
	{
		return damage * mass / maxMass;
	}

	/**
	 * 
	 * @return pushback * mass / maxMass
	 */
	public double getPushback()
	{
		return pushback * mass / maxMass;
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
			buffer.scale(size, size);
			buffer.translate(-x, -y);

			buffer.rotate(rotation, x, y);
			buffer.drawImage(image, (int) x - image.getWidth() / 2, (int) y - image.getHeight() / 2, null);
			buffer.rotate(-rotation, x, y);

			buffer.translate(x, y);
			buffer.scale(1 / size, 1 / size);
			buffer.scale(1 / (z * MAIN.heightZoomRatio + 1), 1 / (z * MAIN.heightZoomRatio + 1));
			buffer.translate(-x, -y);
		}
	}

}
