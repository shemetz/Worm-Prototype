package mainClasses;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import shouldBeDefault.Point3D;

public class Vine extends Drawable // Vines are plant beams, sort of
{
	final static int	lengthOfVineImg	= 200,
								heightOfVineImg = 40;
	final static int	grabblingRange	= 45;
	Person				creator;
	RndPhysObj				grabbledThing;
	Point3D				start, end;
	int					state;							// -1 = none, 0 = flying in the air, 1 = grabbing onto something, 2 = retracting (like 0 but backwards)
	int					points;
	double				range;							// The maximum range of this vine. Measured from the PERSON to the END.
	double				size;							// currently unused; TODO
	double				height;							// z is in center of beam, like balls
	double				startDistance, endDistance;		// distance from creator to start, and from creator to end
	double				length;							// only relevant, and fixed in size, when vine is grabbling something
	double				deltaLength;					// like a spring! the extra length a vine is pulled/pushed.
	double				rigidity;						// self-explanatory
	double				spinStrength;					// how strong can this be pulled sideways
	double				endPauseTimeLeft;

	public Vine(Person creator, Point3D start, Point3D end, int points, double range)
	{
		creator.holdingVine = true;
		this.creator = creator;
		this.start = start;
		this.end = end;
		this.state = -1;
		this.points = points;
		this.range = range;
		state = 0;
		image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		shadow = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		x = start.x;
		y = start.y;
		z = start.z + 0.5;
		rotation = Math.atan2(end.y - start.y, end.x - start.x);

		startDistance = Math.sqrt(Methods.DistancePow2(creator.x, creator.y, start.x, start.y));
		endDistance = Math.sqrt(Methods.DistancePow2(creator.x, creator.y, end.x, end.y));
		length = endDistance - startDistance;
		deltaLength = 0;
		endPauseTimeLeft = 0;

		size = 1; // temp
		height = size / 2 + 0.1; // temp
		rigidity = 25 * points; // temp
		spinStrength = 100000 * points;
	}

	public void retract()
	{
		range = 0; // pretty bad fix, no? TODO
		grabbledThing = null;
		state = 2; // was 0. or maybe 1
		if (endPauseTimeLeft == 0)
			endPauseTimeLeft = 0.2;
	}

	public void quickRetract()
	{
		retract();
		endPauseTimeLeft = 0;
	}
	
	public double getDamage()
	{
		// 0.6 * points * 0.2 * attackRate * damage
		return 0.6 * points * 0.2 * Ability.elementalAttackNumbers[11][2] * Ability.elementalAttackNumbers[11][0];
	}

	public double getPushback()
	{
		// 0.6 * points * 0.2 * attackRate * pushback.
		return 0.6 * points * 0.2 * Ability.elementalAttackNumbers[11][2] * Ability.elementalAttackNumbers[11][1];
	}

	public void rotate(double angle, double deltaTime)
	{
		// should only be used when not grabbling
		final double lerp_constant = 7;
		endDistance = Math.sqrt(Methods.DistancePow2(creator.x, creator.y, end.x, end.y));
		rotation += (((((angle - this.rotation) % (Math.PI * 2)) + (Math.PI * 3)) % (Math.PI * 2)) - Math.PI) * lerp_constant * deltaTime;
		start.x = (int) (creator.x + startDistance * Math.cos(rotation));
		start.y = (int) (creator.y + startDistance * Math.sin(rotation));
		end.x = (int) (creator.x + endDistance * Math.cos(rotation));
		end.y = (int) (creator.y + endDistance * Math.sin(rotation));
	}

	public void fixPosition()
	{
		start.x = (int) (creator.x + startDistance * Math.cos(rotation));
		start.y = (int) (creator.y + startDistance * Math.sin(rotation));
		if (state == 1) // grabbled
		{
			end.x = (int) (grabbledThing.x);
			end.y = (int) (grabbledThing.y);
			endDistance = Math.sqrt(Methods.DistancePow2(creator.x, creator.y, end.x, end.y));
			deltaLength = length - endDistance + startDistance;
		} else // increase length
		{
			endDistance = Math.sqrt(Methods.DistancePow2(creator.x, creator.y, end.x, end.y));
			length = endDistance - startDistance;
		}
		rotation = Math.atan2(end.y - start.y, end.x - start.x);
	}

	public void die()
	{
		creator.holdingVine = false;
	}

	public void drawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		; // TODO Vine Shadows
	}

	public void draw(Graphics2D buffer, double cameraZed)
	{
		double angle = Math.atan2(end.y - start.y, end.x - start.x);
		if (z <= cameraZed && state != -1)
		{
			buffer.translate(x, y);
			buffer.scale(z * Main.heightZoomRatio + 1, z * Main.heightZoomRatio + 1);
			buffer.translate(-x, -y);
			// middle
			int vineDistance = (int) Math.sqrt(Methods.DistancePow2(start.x, start.y, end.x, end.y));
			int numOfVineImages = vineDistance / (int) (lengthOfVineImg * size);
			int leftoverImageWidth = (int) ((vineDistance % (int) (lengthOfVineImg * size)) / size);
			buffer.rotate(angle, start.x, start.y);
			for (int i = 1; i <= numOfVineImages; i++)
			{
				buffer.drawImage(Resources.beams[11][4], (int) (start.x + vineDistance - i * lengthOfVineImg * size), (int) (start.y - 0.5 * heightOfVineImg * size), (int) (lengthOfVineImg * size),
						(int) (heightOfVineImg * size), null);
			}
			// leftover
			if (leftoverImageWidth > 0)
				buffer.drawImage(Resources.beams[11][4].getSubimage(lengthOfVineImg - leftoverImageWidth - 1, 0, leftoverImageWidth, heightOfVineImg), start.x,
						(int) (start.y - 0.5 * heightOfVineImg * size), (int) (leftoverImageWidth * size), (int) (heightOfVineImg * size), null);
			buffer.rotate(-angle, start.x, start.y);
			// start
			buffer.rotate(angle, start.x, start.y);
			buffer.drawImage(Resources.beams[11][0], start.x - (int) (0.5 * heightOfVineImg * size), start.y - (int) (0.5 * heightOfVineImg * size), (int) (heightOfVineImg * size),
					(int) (heightOfVineImg * size), null);
			buffer.rotate(-angle, start.x, start.y);
			// end
			if (state == 0 || state == 2)
			{
				buffer.rotate(angle, end.x, end.y);
				buffer.drawImage(Resources.beams[11][12], end.x - (int) (0.5 * heightOfVineImg * size), end.y - (int) (0.5 * heightOfVineImg * size), (int) (heightOfVineImg * size),
						(int) (heightOfVineImg * size), null);
				buffer.rotate(-angle, end.x, end.y);
			} else // TODO: Vines wrapping grabbled person
			{
				;
			}

			buffer.translate(x, y);
			buffer.scale(1 / (z * Main.heightZoomRatio + 1), 1 / (z * Main.heightZoomRatio + 1));
			buffer.translate(-x, -y);
		}
	}

	public double getCollisionDamage()
	{
		return 5;
	}

	public double getCollisionPushback()
	{
		return 0.00002 * spinStrength;
	}
}
