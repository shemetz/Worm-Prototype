package mainClasses;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class PersonCopy
{
	int animState;
	int animFrame;
	double life;
	// double mana;
	double stamina;
	// double charge;
	boolean ghostMode;
	boolean panic;
	boolean prone;
	boolean dead;
	double slippedTimeLeft;
	double directionOfAttemptedMovement = 0;
	double strengthOfAttemptedMovement = 0;
	double x, y, z;
	double rotation;
	double xVel, yVel, zVel;
	double timeEffect;

	List<Ability> abilities;
	List<Effect> effects;

	public PersonCopy(Person other)
	{
		// This method is for the Loop abilities that undo/rewind a person's state and/or position

		// Purposefully not copied:
		// this.mana = other.mana;
		// this.charge = other.charge;
		this.animState = other.animState;
		this.animFrame = other.animFrame;
		this.life = other.life;
		this.stamina = other.stamina;
		this.ghostMode = other.ghostMode;
		this.panic = other.panic;
		this.prone = other.prone;
		this.dead = other.dead;
		this.slippedTimeLeft = other.slippedTimeLeft;
		this.directionOfAttemptedMovement = other.directionOfAttemptedMovement = 0;
		this.strengthOfAttemptedMovement = other.strengthOfAttemptedMovement = 0;
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
		this.rotation = other.rotation;
		this.xVel = other.xVel;
		this.yVel = other.yVel;
		this.zVel = other.zVel;
		this.timeEffect = other.timeEffect;

		// DOES NOT COPY ABILITIES OR THEIR VARIABLES. THIS IS MAYBE IMPORTANT

		this.effects = new ArrayList<Effect>();
		for (Effect e : other.effects)
			this.effects.add(e.clone());
	}

	public void draw(Graphics2D buffer)
	{
		int radius = 50;
		buffer.setColor(new Color(0,0,0,128));
		buffer.setStroke(new BasicStroke(5));
		buffer.drawOval((int) x - radius, (int) y - radius, radius * 2, radius * 2);
	}
}
