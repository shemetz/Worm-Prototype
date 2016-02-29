package mainClasses;

import java.awt.Graphics2D;
import java.awt.Point;

public class Furniture extends Drawable
{

	int w, h; // for collision purposes

	enum Type
	{
		WOOD_CHAIR, PLANT_POT, DESK, DOOR
	};

	Type type;
	int state;
	boolean clickable;
	int life = 100;
	int armor = 3; // TODO maybe uh not

	public Furniture(double x1, double y1, String typeString, double rotation1)
	{
		super();
		x = x1;
		y = y1;
		z = 0;
		rotation = rotation1;
		type = Type.valueOf(typeString.toUpperCase());
		switch (type)
		{
		case PLANT_POT:
			image = Resources.furniture.get("plant_pot");
			clickable = false;
			w = 54;
			h = 54;
			break;
		case WOOD_CHAIR:
			image = Resources.furniture.get("wood_chair");
			clickable = false;
			w = 46;
			h = 46;
			break;
		case DOOR:
			image = Resources.furniture.get("door");
			clickable = true;
			w = 96;
			h = 18;
			state = 0; // 0 = closed, 1 = open, 2 = locked?
			break;
		case DESK:
			state = (int) (Math.random() * 3);
			image = Resources.furniture.get("desk_" + state);
			clickable = false;
			w = 179;
			h = 83;
			break;
		default:
			MAIN.errorMessage("No excuses!");
			break;
		}
	}

	public Point[] getPoints()
	{
		Point[] p = new Point[4];
		double halfDiagonal = 0.5 * Math.sqrt(Math.pow(w, 2) + Math.pow(h, 2));
		// angle between length and width of force field
		double alpha = Math.atan2(h, w);
		p[0] = new Point((int) (x + halfDiagonal * Math.cos(rotation - alpha)), (int) (y + halfDiagonal * Math.sin(rotation - alpha)));
		p[1] = new Point((int) (x + halfDiagonal * Math.cos(rotation + alpha)), (int) (y + halfDiagonal * Math.sin(rotation + alpha)));
		p[2] = new Point((int) (x + halfDiagonal * Math.cos(rotation + Math.PI - alpha)), (int) (y + halfDiagonal * Math.sin(rotation + Math.PI - alpha)));
		p[3] = new Point((int) (x + halfDiagonal * Math.cos(rotation + Math.PI + alpha)), (int) (y + halfDiagonal * Math.sin(rotation + Math.PI + alpha)));
		return p;
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

	public void trueDrawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
	}
}
