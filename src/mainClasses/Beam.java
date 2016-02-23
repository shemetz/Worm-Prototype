package mainClasses;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import abilities.Beam_E;

public class Beam extends Drawable
{
	public Person			creator;
	public boolean			isChild;
	public Point3D			start, end;
	public int				elementNum;
	public int				endType;						// 0 = regular end, 1 = flat, -1 = not tested yet.
	public double			endAngle;
	public int				frameNum;						// 0, 1, 2 or 3
	public double				strength;
	public double			timeLeft;
	public double			damaging;
	public final static int	lengthOfBeamImg	= 200,
									heightOfBeamImg = 40;
	public double			range;							// The maximum range of this beam. Subsequent reflection-beams will have shorter range.
	public double			size;
	public Beam_E			theAbility;
	public boolean			critical		= false;

	public Beam(Person creator, Beam_E theAbility, Point3D start, Point3D end, int elementNum, double strength1, double range)
	{
		this.theAbility = theAbility;
		this.creator = creator;
		this.start = start;
		this.end = end;
		this.elementNum = elementNum;
		this.endType = -1;
		this.endAngle = -1;
		this.strength = strength1;
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
		// 0.6 * strength * 0.25 * attackRate * damage
		return 0.6 * strength * 0.25 * Ability.elementalAttackNumbers[elementNum][2] * Ability.elementalAttackNumbers[elementNum][0];
	}

	public double getPushback()
	{
		// 0.6 * strength * 0.25 * attackRate * pushback.
		return 0.6 * strength * 0.25 * Ability.elementalAttackNumbers[elementNum][2] * Ability.elementalAttackNumbers[elementNum][1];
	}

	// unused
	public void rotate(double angle, double deltaTime)
	{
		final double lerp_constant = 7;
		rotation += (((((angle - this.rotation) % (Math.PI * 2)) + (Math.PI * 3)) % (Math.PI * 2)) - Math.PI) * lerp_constant * deltaTime;
		start.x = (int) (creator.x + 80 * Math.cos(rotation));
		start.y = (int) (creator.y + 80 * Math.sin(rotation));
		end.x = (int) (creator.x + 10000 * Math.cos(rotation));
		end.y = (int) (creator.y + 10000 * Math.sin(rotation));
	}

	public double angle()
	{
		return Math.atan2(end.y - start.y, end.x - start.x);
	}

	public void drawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		; // on purpose. Beams have no shadow.
	}

	public void draw(Graphics2D buffer, double cameraZed)
	{
		if (intersectedPortal == null || intersectedPortal.partner == null)
		{
			trueDraw(buffer, cameraZed);
			return;
		}
		// PORTAL INTERSECTION
		Portal p = intersectedPortal;

		Point point = (Methods.LineToPointDistancePow2(p.start, p.end, new Point(start.x, start.y)) > Methods.LineToPointDistancePow2(p.start, p.end, new Point(end.x, end.y))) ? new Point(5*end.x/6 + start.x / 6, 5*end.y/6 + start.y / 6) : new Point(end.x/6 + 5*start.x / 6, end.y/6 + 5*start.y / 6);
		double k = (p.end.x - p.start.x) * (point.y - p.start.y) - (p.end.y - p.start.y) * (point.x - p.start.x);
		// k is >0 if this is below p, <0 if this is above p, or 0 if this is in the middle of p

		Shape originalClip = buffer.getClip();
		Polygon clip = getClipOfPortal(p, k > 0);
		buffer.clip(clip);
		trueDraw(buffer, cameraZed);
		buffer.setClip(originalClip);
		clip = getClipOfPortal(p.partner, k <= 0);
		buffer.clip(clip);
		buffer.translate(p.partner.x - p.x, p.partner.y - p.y);
		buffer.rotate(p.partner.angle - p.angle, p.x, p.y);
		trueDraw(buffer, cameraZed);
		buffer.rotate(-p.partner.angle + p.angle, p.x, p.y);
		buffer.translate(-p.partner.x + p.x, -p.partner.y + p.y);
		buffer.setClip(originalClip);
	}

	public Polygon getClipOfPortal(Portal p, boolean direction)
	{

		int k = direction ? 1 : -1;
		double clipLength = this.range;
		Polygon clip = new Polygon();
		clip.addPoint((int) (p.x - Math.cos(p.angle) * clipLength * 1), (int) (p.y - Math.sin(p.angle) * clipLength * 1));
		clip.addPoint((int) (p.x + Math.cos(p.angle) * clipLength * 1), (int) (p.y + Math.sin(p.angle) * clipLength * 1));
		clip.addPoint((int) (p.x + Math.cos(p.angle) * clipLength * 1 + Math.cos(p.angle + Math.PI / 2) * clipLength * 1 * k),
				(int) (p.y + Math.sin(p.angle) * clipLength * 1 + Math.sin(p.angle + Math.PI / 2) * clipLength * 1 * k));
		clip.addPoint((int) (p.x - Math.cos(p.angle) * clipLength * 1 + Math.cos(p.angle + Math.PI / 2) * clipLength * 1 * k),
				(int) (p.y - Math.sin(p.angle) * clipLength * 1 + Math.sin(p.angle + Math.PI / 2) * clipLength * 1 * k));
		return clip;
	}

	public void trueDraw(Graphics2D buffer, double cameraZed)
	{
		double angle = Math.atan2(end.y - start.y, end.x - start.x);
		if (z <= cameraZed && endType != -1)
		{
			buffer.translate(x, y);
			buffer.scale(z * MAIN.heightZoomRatio + 1, z * MAIN.heightZoomRatio + 1);
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
			buffer.scale(1 / (z * MAIN.heightZoomRatio + 1), 1 / (z * MAIN.heightZoomRatio + 1));
			buffer.translate(-x, -y);

			// end is in the next method
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
