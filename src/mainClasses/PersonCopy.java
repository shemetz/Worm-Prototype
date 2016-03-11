package mainClasses;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public boolean possessing;
	public double possessedTimeLeft;
	public int possessingControllerID;
	public int possessingVictimID;
	public double runAccel;
	public double runSpeed;
	public double lifeRegen;
	public double manaRegen;
	public double staminaRegen;

	Map<Ability, Boolean> abilities;
	List<Effect> effects;
	List<Ability> punchAffectingAbilities;

	public PersonCopy(Person other)
	{
		// This method is for the Loop abilities that undo/rewind a person's state and/or position

		// Purposefully not copied:
		// this.mana = other.mana;
		// this.charge = other.charge;
		this.runAccel = other.runAccel;
		this.runSpeed = other.runSpeed;
		this.lifeRegen = other.lifeRegen;
		this.manaRegen = other.manaRegen;
		this.staminaRegen = other.staminaRegen;
		this.punchAffectingAbilities = other.punchAffectingAbilities;
		this.animState = other.animState;
		this.animFrame = other.animFrame;
		this.life = other.life;
		this.stamina = other.stamina;
		this.ghostMode = other.ghostMode;
		this.panic = other.panic;
		this.prone = other.prone;
		this.dead = other.dead;
		this.slippedTimeLeft = other.slippedTimeLeft;
		this.directionOfAttemptedMovement = other.directionOfAttemptedMovement;
		this.strengthOfAttemptedMovement = other.strengthOfAttemptedMovement;
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
		this.rotation = other.rotation;
		this.xVel = other.xVel;
		this.yVel = other.yVel;
		this.zVel = other.zVel;
		this.timeEffect = other.timeEffect;
		this.possessing = other.startStopPossession;
		this.possessedTimeLeft = other.possessedTimeLeft;
		this.possessingControllerID = other.possessingControllerID;
		this.possessingVictimID = other.possessionTargetID;

		abilities = new HashMap<Ability, Boolean>();

		for (Ability a : other.abilities)
			if (a.hasTag("on-off"))
				abilities.put(a, a.on);

		this.effects = new ArrayList<Effect>();
		for (Effect e : other.effects)
		{
			if (e.timeLeft != -1) // DOES NOT COPY PERMANENT EFFECTS
				this.effects.add(e.clone());
		}
	}

	public void draw(Graphics2D buffer)
	{
		int radius = 50;
		buffer.setColor(new Color(0, 0, 0, 128));
		buffer.setStroke(new BasicStroke(5));
		buffer.drawOval((int) x - radius, (int) y - radius, radius * 2, radius * 2);
	}
}
