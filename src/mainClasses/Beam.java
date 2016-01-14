package mainClasses;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Beam extends Drawable
{
	public Person				creator;
	public boolean				isChild;
	public Point3D				start, end;
	public int					elementNum;
	public int					endType;									// 0 = regular end, 1 = flat, -1 = not tested yet.
	public double				endAngle;
	public int					frameNum;									// 0, 1, 2 or 3
	public int					points;
	public double				timeLeft;
	public double				damaging;
	public final static int	lengthOfBeamImg	= 200, heightOfBeamImg = 40;
	public double				range;										// The maximum range of this beam. Subsequent reflection-beams will have shorter range.
	public double				size;

	public Beam(Person creator, Point3D start, Point3D end, int elementNum, int points, double range)
	{
		this.creator = creator;
		this.start = start;
		this.end = end;
		this.elementNum = elementNum;
		this.endType = -1;
		this.endAngle = -1;
		this.points = points;
		this.range = range;

		isChild = false;

		image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		shadow = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		x = start.x;
		y = start.y;
		z = start.z;
		rotation = Math.atan2(end.y - start.y, end.x - start.x);

		timeLeft = 0.02; // 0.02 = no trail, 0.04 = one trail, 0.06 = two trails....

		size = 1; // temp
		height = size / 2 + 0.1; // temp
	}

	public double getDamage()
	{
		// 0.6 * points * 0.2 * attackRate * damage
		return 0.6 * points * 0.2 * Ability.elementalAttackNumbers[elementNum][2] * Ability.elementalAttackNumbers[elementNum][0];
	}

	public double getPushback()
	{
		// 0.6 * points * 0.2 * attackRate * pushback.
		return 0.6 * points * 0.2 * Ability.elementalAttackNumbers[elementNum][2] * Ability.elementalAttackNumbers[elementNum][1];
	}

	//unused
	public void rotate(double angle, double deltaTime)
	{
		final double lerp_constant = 7;
		rotation += (((((angle - this.rotation) % (Math.PI * 2)) + (Math.PI * 3)) % (Math.PI * 2)) - Math.PI) * lerp_constant * deltaTime;
		start.x = (int) (creator.x + 80 * Math.cos(rotation));
		start.y = (int) (creator.y + 80 * Math.sin(rotation));
		end.x = (int) (creator.x + 10000 * Math.cos(rotation));
		end.y = (int) (creator.y + 10000 * Math.sin(rotation));
	}

	public void drawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		; // on purpose. Beams have no shadow.
	}

	public void draw(Graphics2D buffer, double cameraZed)
	{
		double angle = Math.atan2(end.y - start.y, end.x - start.x);
		if (z <= cameraZed && endType != -1)
		{
			buffer.translate(x, y);
			buffer.scale(z * Main.heightZoomRatio + 1, z * Main.heightZoomRatio + 1);
			buffer.translate(-x, -y);

			// middle
			int beamDistance = (int) Math.sqrt(Methods.DistancePow2(start.x, start.y, end.x, end.y));
			int numOfBeamImages = beamDistance / (int) (lengthOfBeamImg * size);
			int leftoverImageWidth = (int) ((beamDistance % (int) (lengthOfBeamImg * size)) / size);
			buffer.rotate(angle, start.x, start.y);
			for (int i = 0; i < numOfBeamImages; i++)
			{
				buffer.drawImage(Resources.beams[elementNum][frameNum + 4], (int) (start.x + i * lengthOfBeamImg * size), (int) (start.y - 0.5 * heightOfBeamImg * size),
						(int) (lengthOfBeamImg * size), (int) (heightOfBeamImg * size), null);
			}
			// leftover
			if (leftoverImageWidth > 0)
				buffer.drawImage(Resources.beams[elementNum][frameNum + 4].getSubimage(0, 0, leftoverImageWidth, heightOfBeamImg), (int) (start.x + numOfBeamImages * lengthOfBeamImg * size),
						(int) (start.y - 0.5 * heightOfBeamImg * size), (int) (leftoverImageWidth * size), (int) (heightOfBeamImg * size), null);
			buffer.rotate(-angle, start.x, start.y);
			// start
			buffer.rotate(angle, start.x, start.y);
			buffer.drawImage(Resources.beams[elementNum][frameNum], start.x - (int) (48 * size), start.y - (int) (48 * size), (int) (96 * size), (int) (96 * size), null); // note the 48 instead of that image dot getWidth, because of laziness
			buffer.rotate(-angle, start.x, start.y);

			buffer.translate(x, y);
			buffer.scale(1 / (z * Main.heightZoomRatio + 1), 1 / (z * Main.heightZoomRatio + 1));
			buffer.translate(-x, -y);
		}
	}

	public void drawTopEffects(Graphics2D buffer, int cameraZed)
	{

		double angle = Math.atan2(end.y - start.y, end.x - start.x);
		if (z <= cameraZed && endType != -1)
		{
			// end 
			if (endType == 0)
			{
				buffer.rotate(angle, end.x, end.y);
				buffer.drawImage(Resources.beams[elementNum][frameNum + 12], end.x - (int) (48 * size), end.y - (int) (48 * size), (int) (96 * size), (int) (96 * size), null);
				buffer.rotate(-angle, end.x, end.y);
			} else
			{
				buffer.rotate(endAngle, end.x, end.y);
				buffer.drawImage(Resources.beams[elementNum][frameNum + 8], end.x - (int) (48 * size), end.y - (int) (48 * size), (int) (96 * size), (int) (96 * size), null);
				buffer.rotate(-endAngle, end.x, end.y);
			}
		}
	}
}
