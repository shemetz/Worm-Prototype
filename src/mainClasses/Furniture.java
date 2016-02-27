package mainClasses;

import java.awt.Graphics2D;

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
			w = 179;
			h = 83;
			break;
		case DESK:
			state = (int) (Math.random() * 3);
			image = Resources.furniture.get("desk_" + state);
			clickable = false;
			w = 96;
			h = 18;
			break;
		default:
			MAIN.errorMessage("No excuses!");
			break;
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

	public void trueDrawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
	}
}
