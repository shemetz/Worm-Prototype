package mainClasses;

import java.awt.Graphics2D;

public class Debris extends Drawable
{
	double	velocity;
	double	angle;
	int		elementNum;	// 12 = smoke, 13 = force field
	int		type;
	double	timeLeft;

	public Debris(double x1, double y1, double z1, double a1, int e1, double v1)
	{
		x = x1;
		y = y1;
		z = z1; // debris doesn't fall, by the way. I think.
		angle = a1;
		rotation = a1; // yes yes
		elementNum = e1;
		type = MAIN.random.nextInt(6);
		velocity = v1;
		velocity *= MAIN.random.nextDouble() * 1.4 + 0.3;
		image = Resources.debris[elementNum][type];
		shadow = Resources.debrisShadows[elementNum][type];
		radius = image.getWidth() / 2;
	}

	public Debris(double x1, double y1, double z1, double a1, int e1, boolean unMoving, double timeLeft1)
	{
		if (unMoving)
		{
			x = x1;
			y = y1;
			z = z1; // debris doesn't fall, by the way. I think.
			angle = a1;
			rotation = a1; // yes yes
			elementNum = e1;
			type = MAIN.random.nextInt(6);
			if (type >= 3)
				timeLeft1 /= 2;
			velocity = 0;
			image = Resources.debris[elementNum][type];
			shadow = Resources.debrisShadows[elementNum][type];
			radius = image.getWidth() / 2;

			timeLeft = timeLeft1;
		} else
			MAIN.errorMessage("wtf? @ Debris");
	}

	public void update(double deltaTime)
	{
		if (timeLeft > 0)
		{
			timeLeft -= deltaTime;
			if (type < 3 && timeLeft < 0.25)
			{
				type += 3;
				image = Resources.debris[elementNum][type];
				shadow = Resources.debrisShadows[elementNum][type];
			}
		} else
		{
			velocity -= 2.4 * deltaTime * velocity;
			if (type == 1 || type == 4)
				rotation += 4 * Math.PI * deltaTime; // temp ?
			else
				// just to make them not all rotate to same direction.
				rotation -= 4 * Math.PI * deltaTime; // temp ?
		}
	}

	public void addVelocity(double xVel, double yVel)
	{
		xVel += velocity * Math.cos(angle);
		yVel += velocity * Math.sin(angle);
		angle = Math.atan2(yVel, xVel);
		velocity = Math.sqrt(xVel * xVel + yVel * yVel);
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
