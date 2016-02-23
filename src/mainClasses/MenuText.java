package mainClasses;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class MenuText extends MenuElement
{

	public MenuText(int x1, int y1, int w1, int h1, String string)
	{
		try
		{
			// Button
			type = Type.valueOf(string.toUpperCase());
			switch (type)
			{
			case EXIT_GAME:
				text = "Quit to desktop";
				break;
			case RESUME:
				text = "Resume";
				break;
			case TEXT:
				text = "";
				break;
			case CHEATS:
				text = "Cheats";
				break;
			default:
				MAIN.errorMessage("problematic is " + type);
				break;
			}
			clickable = true;
		}
		catch (IllegalArgumentException e)
		{
			// Text
			text = string;
			type = Type.TEXT;
			clickable = false;
		}
		x = x1;
		y = y1;
		width = w1;
		height = h1;
	}

	public void draw(Graphics2D buffer)
	{
		buffer.setColor(new Color(0, 0, 0, 100));
		buffer.fillRect(x, y, width, height);
		buffer.setColor(Color.gray);
		buffer.setStroke(new BasicStroke(5));
		buffer.drawRect(x - 1, y - 1, width, height);
		buffer.drawRect(x - 1, y + 1, width, height);
		buffer.drawRect(x + 1, y - 1, width, height);
		buffer.drawRect(x + 1, y + 1, width, height);
		buffer.setColor(Color.black);
		buffer.drawRect(x, y, width, height);
		if (type == MenuElement.Type.CHEATS_RESULT_ABILITY)
		{
			buffer.setFont(new Font("Serif", Font.PLAIN, 20));
			int[] a =
			{ -1, -1, +1, +1, 0 };
			int[] b =
			{ -1, +1, -1, +1, 0 };
			for (int i = 0; i < 5; i++)
			{
				if (i == 4)
				{
					if (selected)
					{
						buffer.setColor(Color.orange);
						buffer.setStroke(new BasicStroke(3));
						buffer.drawRect(x + a[i], y + b[i], width, height);
					}
					else
						buffer.setColor(Color.white);
				}
				else
					buffer.setColor(Color.black);

				// NAME
				buffer.drawString(text.substring(0, text.indexOf('\n')), x + a[i] + 5, y + b[i] + 20);
				// DESCRIPTION
				String string = text.substring(text.indexOf('\n') + 1);
				if (string.length() < 80)
					buffer.drawString(string, x + a[i] + 5, y + b[i] + 100);
				else // possibly+b[i] spannning two lines
				{
					int lastSpaceIndex = string.lastIndexOf(" ", 80);
					buffer.drawString(string.substring(0, lastSpaceIndex), x + a[i] + 5, y + b[i] + 100);
					buffer.drawString(string.substring(lastSpaceIndex), x + a[i] + 5, y + b[i] + 130);
				}
			}
		}
		else
		{
			buffer.setFont(new Font("Sans-Serif", Font.PLAIN, 40));
			buffer.setColor(Color.gray);
			buffer.drawString(text, x - 1 + 5, y - 1 + height / 2 + 13);
			buffer.drawString(text, x - 1 + 5, y + 1 + height / 2 + 13);
			buffer.drawString(text, x + 1 + 5, y - 1 + height / 2 + 13);
			buffer.drawString(text, x + 1 + 5, y + 1 + height / 2 + 13);
			if (selected)
			{
				buffer.setColor(Color.orange);
				buffer.setStroke(new BasicStroke(3));
				buffer.drawRect(x, y, width, height);
			}
			else
				buffer.setColor(Color.black);
			// color depends
			buffer.drawString(text, x + 5, y + height / 2 + 13);
		}
	}
}
