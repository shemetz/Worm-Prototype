import java.awt.Graphics2D;

public class ArcForceField extends Drawable
{
	double	height;
	Person	target;
	double	arc;
	double	minRadius;
	double	maxRadius;
	double	life;
	double	extraLife;	// the extralife is for force fields that are not immediately created with full health, but instead gain the health during the first 1.5 seconds of their creation. Alternatively, for
						// force fields that are created with full health but slowly lose it every second.
	int		elementNum;

	public ArcForceField(Person target1, double angle1, double arc1, double minRadius1, double maxRadius1, double life1, int elementNum1)
	{
		target = target1;
		x = target.x;
		y = target.y;
		z = target.z;
		rotation = angle1;
		arc = arc1;
		minRadius = minRadius1;
		maxRadius = maxRadius1;
		life = 1;
		extraLife = life1;
		elementNum = elementNum1;
		height = 1.5;
		int frame = 0;
		if (life >= 75)
			frame = 0;
		else if (life >= 50)
			frame = 1;
		else if (life >= 15)
			frame = 2;
		else
			frame = 3;
		changeImage(Resources.arcForceFields[elementNum][frame]);
	}

	public void update(double deltaTime)
	{
		if (extraLife > 0)
		{
			life += extraLife * deltaTime / 1.5;
			if (life >= extraLife)
				extraLife = 0;
		}
		if (extraLife < 0)
			life *= 0.8; // -20% every second.
		if (life < 1)
			life = 0;
		x = target.x;
		y = target.y;
		z = target.z;
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
