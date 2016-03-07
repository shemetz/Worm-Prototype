package mainClasses;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import abilities.Charge;
import abilities.Elastic;
import abilities.Elemental_Void;
import abilities.Punch;
import abilities.Sprint;
import effects.Burning;
import effects.E_Resistant;
import effects.Healed;
import effects.Nullified;
import effects.Stunned;
import effects.Tangled;
import mainResourcesPackage.SoundEffect;
import pathfinding.Mover;

public class Person extends RndPhysObj implements Mover
{
	public int id;
	public String name;

	public int animState;
	public int animFrame;

	public List<EP> DNA;
	public List<Ability> abilities;
	public List<Effect> effects;
	public List<UIText> uitexts;

	// STATS
	public int STRENGTH;
	public int FITNESS;
	public int DEXTERITY;
	public int WITS;
	public int KNOWLEDGE;
	public int SOCIAL;

	// SUB-STATS
	public int maxLife;
	public int maxMana;
	public int maxStamina;
	public double lifeRegen; // per second. during combat.
	public double manaRegen; // ^
	public double staminaRegen; // ^
	public double runSpeed; // maximum speed in pixel/sec in a single direction while running on dry earth.
	public double runAccel;
	public int naturalArmor;
	public int flightVisionDistance; // in pixels. is multiplied by height, in meters, while flying
	public double punchSpeed;
	public double accuracy; // from 0 to 1. 0 = 90 degree miss, 1 = 0 degree miss, 0.5 = 45 degree miss.
	public double missAngle;
	public double runningStaminaCost; // per second
	public double sprintingStaminaCost; // ^
	public double evasion; // chance of an attack missing you
	public double criticalChance; // chance of a critical hit
	public double pushbackResistance; // pushback immunity

	// Rest of the variables
	public double life;
	public double mana;
	public double stamina;
	public double charge;
	public boolean insideWall;
	public boolean ghostMode;
	public boolean panic; // used for panic purposes
	public boolean prone; // while ducking or slipping
	public boolean dead;
	public double slippedTimeLeft;
	public int imgW, imgH; // For drawing purposes only
	public boolean inCombat;
	public boolean twitching;
	public boolean maintaining; // whether or not the person is using a maintained ability like Shield or Escalating Scream
	public double timeSinceLastHit;
	public double timeBetweenDamageTexts;
	public double waitingDamage;
	public Point target;
	public boolean lastHandUsedIsRight = false;
	public boolean punchedSomething = false;
	public double notMovingTimer = 0;
	public boolean notAnimating = false;
	public double directionOfAttemptedMovement = 0;
	public double strengthOfAttemptedMovement = 0; // between 0 and 1
	public double flyDirection = 0; // 1 = up, -1 = down.
	public int abilityTryingToRepetitivelyUse = -1;
	public int abilityAiming = -1;
	public int abilityMaintaining = -1;
	public int commanderID; // ID of the person's group's leader. If individual, commanderID is the same as id.
	public double lastSpeed = 0; // used for ease of calculation sometimes.
	public boolean holdingVine = false; // true if the person is using a Plant Beam (vine) and grabbling an enemy.
	public double flySpeed = -1;
	public double timeSincePortal = 0;
	public List<Ability> punchAffectingAbilities;

	// for continuous inaccuracy stuff like beams
	public double inaccuracyAngle = 0;
	public double inaccuracyAngleTarget = 0;
	public double timeUntilNextInaccuracyAngleChange = 0;

	// stuff
	String voiceType; // Male, Female. TODO add more
	public Map<Environment, Area> visibleArea;
	public Map<Environment, Area> rememberArea;
	public Map<Environment, int[][]> seenBefore;

	// Inventory and stuff?
	public List<Item> inventory;
	public Armor[] body; // head, chest, arms, legs
	public Armor[] armorParts; // head, chest, arms, legs

	// Animation
	public List<List<BufferedImage>> animationBottom;
	public List<List<BufferedImage>> animationTop;
	public List<List<BufferedImage>> animationVine;

	// Look
	public int legs, chest, head, hair, nakedLegs, nakedChest;

	// Sounds
	public List<SoundEffect> sounds = new ArrayList<SoundEffect>();

	// Time loop states
	List<PersonCopy> pastCopies;
	double pastCopyTimer;

	// this:
	public int portalToOtherEnvironment = -1;
	public double portalVariableX = 0;
	public double portalVariableY = 0;

	public Person(double x1, double y1)
	{
		super(x1, y1, 0, 0);
		visibleArea = new HashMap<Environment, Area>();
		rememberArea = new HashMap<Environment, Area>();
		seenBefore = new HashMap<Environment, int[][]>();
		mass = 70; // TODO
		radius = 24;
		id = Person.giveID();
		commanderID = id;
		z = 0; // Characters start standing on the ground, I think
		height = 1;
		xVel = 0;
		yVel = 0;
		animState = 0;
		animFrame = 0;
		rotation = 0;
		prone = false;
		dead = false;
		slippedTimeLeft = 0;
		maintaining = false;
		DNA = new ArrayList<EP>();
		abilities = new ArrayList<Ability>();
		effects = new ArrayList<Effect>();
		uitexts = new ArrayList<UIText>();
		punchAffectingAbilities = new ArrayList<Ability>();
		ghostMode = false;
		insideWall = false;
		timeSinceLastHit = 0;
		timeBetweenDamageTexts = 0;
		waitingDamage = 0;
		panic = false;
		target = new Point(-1, -1);
		imgW = 96;
		imgH = 96;
		body = new Armor[4];
		body[0] = (new Armor(1, "Skin"));
		body[1] = (new Armor(1, "Skin"));
		body[2] = (new Armor(1, "Skin"));
		body[3] = (new Armor(1, "Skin"));
		// Default armor is no armor
		armorParts = new Armor[4];
		for (int i = 0; i < body.length; i++)
			armorParts[i] = body[i];

		inventory = new ArrayList<Item>();

		selfFrame(0);
		pastCopies = new ArrayList<PersonCopy>();
		pastCopyTimer = 0;

		// randomize gender
		switch ((int) (Math.random() * 2))
		{
		case 0:
			voiceType = "M";
			break;
		case 1:
			voiceType = "F";
			break;
		default:
			MAIN.errorMessage("Got my A machines on the table got my B machines in the drawer");
		}

		initSounds();
		initStats();

		// randomize look
		legs = 1 + MAIN.random.nextInt(2);
		chest = 1 + MAIN.random.nextInt(2);
		head = MAIN.random.nextInt(2);
		hair = 1 + MAIN.random.nextInt(2);
		nakedLegs = MAIN.random.nextInt(1);
		nakedChest = MAIN.random.nextInt(1);
		initAnimation();
	}

	public void copyState(PersonCopy other)
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
		this.directionOfAttemptedMovement = other.directionOfAttemptedMovement;
		this.strengthOfAttemptedMovement = other.strengthOfAttemptedMovement;
		this.timeEffect = other.timeEffect;

		for (Ability a : this.abilities)
			if (a.hasTag("on-off"))
				a.on = other.abilities.get(a);

		this.effects = new ArrayList<Effect>();
		for (Effect e : other.effects)
			this.affect(e, true);
	}

	public void copyPosition(PersonCopy other)
	{
		// This method is for the Loop abilities that undo/rewind a person's state and/or position

		// position, rotation and velocity
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
		this.rotation = other.rotation;
		this.xVel = other.xVel;
		this.yVel = other.yVel;
		this.zVel = other.zVel;
	}

	public void affect(Effect e, boolean add)
	{

		// Can't get additional effects while dead. TODO look at this again one day and reconsider
		if (dead && add)
			return;
		if (!e.stackable)
			for (int i = 0; i < effects.size(); i++)
			{
				Effect e2 = effects.get(i);
				if (e2.name.equals(e.name))
				{
					if (add)
					{
						// set old effect to strength of new effect, and refresh it
						e2.strength = Math.max(e.strength, e2.strength);
						e2.timeLeft = e.duration;
						return;
					}
					else
					{
						// remove old effect
						e2.unapply(this);
						effects.remove(i);
						i--;
						return;
					}
				}
			}
		if (add)
		{
			// test for immunities:
			for (Effect e2 : effects)
				if (e2 instanceof E_Resistant)
					switch (EP.damageType(((E_Resistant) e2).element))
					{
					case 2:
						if (e instanceof Burning)
							return;
					case 4:
						if (e instanceof Stunned)
							return;
					default:
						break;
					}
			e.apply(this);
			effects.add(e);
		}
		else // DELETES OLDEST EFFECT WITH SAME NAME AND CREATOR ABILITY
		{
			int oldestEffectIndex = -1;
			for (int i = 0; i < effects.size(); i++)
			{
				Effect ee = effects.get(i);
				if (ee.name.equals(e.name) && (ee.creatorAbility == null || e.creatorAbility == null || ee.creatorAbility.equals(e.creatorAbility)))
					if (oldestEffectIndex == -1 || e.timeLeft < effects.get(oldestEffectIndex).timeLeft)
						oldestEffectIndex = i;
			}
			if (oldestEffectIndex != -1) // sometimes the program attempts to remove an effect without checking if it already exists. It's ok.
			{
				effects.get(oldestEffectIndex).unapply(this);
				effects.remove(oldestEffectIndex);
			}
		}
	}

	public void removeEffects(int amount)
	{
		amount = Math.min(amount, effects.size());
		if (amount > 0)
			for (int i = 0, k = 0; i < effects.size() && k < amount; i++, k++)
				if (effects.get(i).removable)
				{
					affect(effects.get(i), false);
					i--;
				}
				else
					k--;

	}

	public void damage(double damage)
	{
		life -= damage;
		if (damage >= 2.5 || Math.random() < 0.01) // so that beams and wall collisions don't always trigger this
		{
			// TODO - this is kinda buggy with the fact that normal wall collisions can damage, and that makes clones stay "in combat" too much
			timeSinceLastHit = 0;
			inCombat = true;
		}
	}

	public void initStats()
	{
		// all of this is TEMP
		STRENGTH = 3;
		DEXTERITY = 3;
		FITNESS = 3;
		WITS = 3;
		KNOWLEDGE = 3;
		SOCIAL = 3;

		maxLife = 100;
		maxMana = 10;

		updateSubStats();

		life = maxLife;
		mana = maxMana;
		stamina = maxStamina;
		charge = 0;

		// Natural abilities
		abilities.add(Ability.ability("Punch", 0));
		abilities.get(0).range = (int) (2.3 * radius);
		abilities.add(Ability.ability("Sprint", 0));
	}

	public void setStats(int str, int dex, int fit, int wit, int know, int soc)
	{
		STRENGTH = str;
		DEXTERITY = dex;
		FITNESS = fit;
		WITS = wit;
		KNOWLEDGE = know;
		SOCIAL = soc;
	}

	public void updateSubStats()
	{
		// should always be overridden.
		MAIN.errorMessage("WHO IS THIS PERSON");
	}

	public void basicUpdateSubStats()
	{
		maxStamina = 4 + 2 * FITNESS;
		naturalArmor = (int) (0.7 * STRENGTH + 0.3 * FITNESS);
		lifeRegen = 1;
		manaRegen = 0.5;
		staminaRegen = 0.2 + 0.1 * FITNESS;
		punchSpeed = 0.55 - Math.min(0.15, 0.02 * FITNESS);
		runAccel = 1000 * FITNESS;
		runSpeed = 100 * FITNESS;
		flightVisionDistance = 100 * WITS;
		accuracy = 1 - 0.6 / ((double) DEXTERITY + 1);
		updateAccuracy();
		runningStaminaCost = 0.6;
		sprintingStaminaCost = 1.8;
		evasion = 1 - Math.pow(0.99431695501, (DEXTERITY * WITS)); // average is EXACTLY 5%!
		criticalChance = 1 - Math.pow(0.99431695501, (DEXTERITY * WITS)); //
		pushbackResistance = 0;
	}

	public void updateAccuracy()
	{
		missAngle = (1 - accuracy) * Math.PI / 3; // DEX 0 = 60 degree miss, DEX 3 = 15.
	}

	public void die()
	{
		switchAnimation(13); // TODO death animation
		dead = true;
		prone = false;
		// Abilities will be deactivated in the Main class' frame() method
		// Abilities can't be activated any more (pressAbilityKey() will not work on dead people)
	}

	public double highestPoint()
	{
		double value = z + height;
		if (prone)
			value -= 0.2;
		if (dead)
			value -= 0.9;
		return value;
	}

	public void initSounds()
	{
		sounds.add(new SoundEffect("Scorched.wav")); // 0 - when a beam hits you
		sounds.get(0).endUnlessMaintained = true;
		sounds.add(new SoundEffect("Person Fall.wav")); // 1 - fall damage
		for (int i = 1; i <= 5; i++)
			sounds.add(new SoundEffect(voiceType + "_Grunt_" + i + ".wav")); // 2-6 - pain/grunt
	}

	public void stopAllSounds()
	{
		for (int i = 0; i < sounds.size(); i++)
			sounds.get(i).stop();
	}

	public void initAnimation()
	{
		List<Integer> n = new ArrayList<Integer>();
		n.add(legs);
		n.add(chest);
		n.add(head);
		n.add(hair);
		n.add(-1); // stand-in for vines
		n.add(nakedLegs);
		n.add(nakedChest);

		animationBottom = new ArrayList<List<BufferedImage>>();
		animationTop = new ArrayList<List<BufferedImage>>();

		animationVine = new ArrayList<List<BufferedImage>>();

		increaseAnimationListSize(); // stand
		insertFullBodyAnimation(0, 0, n);

		increaseAnimationListSize(); // walk
		insertFullBodyAnimation(1, 1, n);
		insertFullBodyAnimation(1, 0, n);
		insertFullBodyAnimation(1, 1, n);
		insertFullBodyAnimation(1, 2, n);
		insertFullBodyAnimation(1, 3, n);
		insertFullBodyAnimation(1, 2, n);

		increaseAnimationListSize(); // hold shield
		insertFullBodyAnimation(2, 0, n);

		increaseAnimationListSize(); // slip
		insertFullBodyAnimation(3, 0, n);

		increaseAnimationListSize(); // get up from slip
		insertFullBodyAnimation(4, 0, n);
		insertFullBodyAnimation(4, 1, n);
		insertFullBodyAnimation(4, 2, n);

		increaseAnimationListSize(); // punch with right arm
		insertFullBodyAnimation(5, 0, n);
		insertFullBodyAnimation(5, 0, n);
		insertFullBodyAnimation(5, 1, n);

		increaseAnimationListSize(); // punch with left arm
		insertFullBodyAnimation(6, 0, n);
		insertFullBodyAnimation(6, 0, n);
		insertFullBodyAnimation(6, 1, n);

		increaseAnimationListSize(); // fly
		insertFullBodyAnimation(7, 0, n);
		insertFullBodyAnimation(7, 1, n);
		insertFullBodyAnimation(7, 2, n);
		insertFullBodyAnimation(7, 1, n);

		increaseAnimationListSize(); // fly-hover transition
		insertFullBodyAnimation(8, 0, n);
		insertFullBodyAnimation(8, 0, n);

		increaseAnimationListSize(); // hover
		insertFullBodyAnimation(9, 0, n);

		increaseAnimationListSize(); // flypunch preparation (arms backwards, ready...)
		insertFullBodyAnimation(10, 0, n);
		insertFullBodyAnimation(10, 1, n);
		insertFullBodyAnimation(10, 2, n);
		insertFullBodyAnimation(10, 1, n);

		increaseAnimationListSize(); // flypunch (left)
		insertFullBodyAnimation(11, 0, n);
		insertFullBodyAnimation(11, 0, n);

		increaseAnimationListSize(); // flypunch (right)
		insertFullBodyAnimation(12, 0, n);
		insertFullBodyAnimation(12, 0, n);

		increaseAnimationListSize(); // dead
		insertFullBodyAnimation(13, 0, n);

		changeImage();
	}

	void increaseAnimationListSize()
	{
		animationBottom.add(new ArrayList<BufferedImage>());
		animationTop.add(new ArrayList<BufferedImage>());
		animationVine.add(new ArrayList<BufferedImage>());
	}

	public void insertFullBodyAnimation(int stateNum, int frameNum, List<Integer> n)
	{
		if (stateNum < 4)
		{
			BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = img.createGraphics();
			g2d.drawImage(Resources.bodyPart.get(5).get(n.get(5)).get(stateNum).get(frameNum), 0, 0, null); // naked legs
			if (n.get(0) != 0)
				g2d.drawImage(Resources.bodyPart.get(0).get(n.get(0)).get(stateNum).get(frameNum), 0, 0, null); // legs
			g2d.drawImage(Resources.bodyPart.get(6).get(n.get(6)).get(stateNum).get(frameNum), 0, 0, null); // naked chest
			if (n.get(1) != 0)
				g2d.drawImage(Resources.bodyPart.get(1).get(n.get(1)).get(stateNum).get(frameNum), 0, 0, null); // chest

			animationBottom.get(stateNum).add(img);
			g2d.dispose();
			img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
			g2d = img.createGraphics();
			// head and hair don't change in these frames, hence the .get(0):
			g2d.drawImage(Resources.bodyPart.get(2).get(n.get(2)).get(stateNum).get(0), 0, 0, null); // head
			if (n.get(3) != 0)
				g2d.drawImage(Resources.bodyPart.get(3).get(n.get(3)).get(stateNum).get(0), 0, 0, null); // hair
			animationTop.get(stateNum).add(img);
		}
		else
		{
			BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = img.createGraphics();
			g2d.drawImage(Resources.bodyPart.get(5).get(n.get(5)).get(stateNum).get(frameNum), 0, 0, null); // naked legs
			if (n.get(0) != 0)
				g2d.drawImage(Resources.bodyPart.get(0).get(n.get(0)).get(stateNum).get(frameNum), 0, 0, null); // legs
			g2d.drawImage(Resources.bodyPart.get(6).get(n.get(6)).get(stateNum).get(frameNum), 0, 0, null); // naked chest
			if (n.get(1) != 0)
				g2d.drawImage(Resources.bodyPart.get(1).get(n.get(1)).get(stateNum).get(frameNum), 0, 0, null); // chest
			animationBottom.get(stateNum).add(img);
			g2d.dispose();
			img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
			g2d = img.createGraphics();
			g2d.drawImage(Resources.bodyPart.get(2).get(n.get(2)).get(stateNum).get(frameNum), 0, 0, null); // head
			if (n.get(3) != 0)
				g2d.drawImage(Resources.bodyPart.get(3).get(n.get(3)).get(stateNum).get(frameNum), 0, 0, null); // hair
			animationTop.get(stateNum).add(img);
		}

		BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.drawImage(Resources.bodyPart.get(4).get(0).get(stateNum).get(frameNum), 0, 0, null);
		g2d.dispose();

		animationVine.get(stateNum).add(img);
	}

	public void nextFrame(int frameNum)
	{
		switch (animState)
		{
		/// <number of frames per second> <animation name>
		case 1: // ~7 running
			for (Ability a : abilities)
				if (a instanceof Sprint && a.on)
				{
					if (frameNum % 4 == 0)
						animFrame++;
					break;
				}
			if (frameNum % 7 == 0)
				animFrame++;
			break;
		case 0: // ~7 standing
		case 2: // ~7 holding shield
		case 3: // ~7 slipping
			if (frameNum % 7 == 0)
				animFrame++;
			break;
		case 4: // 2 getting up from slip
			if (frameNum % 20 == 0 && animFrame < 2)
				animFrame++;
			break;
		case 5: // ~6 punching
		case 6: // ~6 punching
			if (frameNum % 8 == 0)
				animFrame++;
			if (animFrame > 2)
				animFrame = 2;
			break;
		case 7: // fly
			if (frameNum % 15 == 0)
				animFrame++;
			break;
		case 8: // fly-hover
			// only one transition state here
			if (frameNum % 30 == 0)
				animFrame = 1; // transition-ready frame
			break;
		case 9: // hover
			if (frameNum % 15 == 0)
				animFrame++;
			break;
		case 10: // fly punch
			if (frameNum % 15 == 0)
				animFrame++;
			break;
		case 11: // fly punch right
		case 12: // fly punch left
			if (frameNum % 10 == 0)
				animFrame++;
			if (animFrame > 1)
				switchAnimation(10);
			break;
		case 13: // dead
			// Nothing, right? TODO
			break;
		default:
			MAIN.errorMessage("Non-cased animState: " + animState);
			break;
		}
		// remember - if the animation has repeating frames of the same image, they are just added several times to the list.
		if (animFrame > animationBottom.get(animState).size() - 1 && animFrame > animationTop.get(animState).size() - 1)
			animFrame = 0;
	}

	public void switchAnimation(int newAnimState)
	{
		switch (newAnimState) // (hahaha switch pun)
		// depending on what you're trying to DO
		{
		case 0:// standing
			switch (animState)
			// depending on which state you WERE
			{
			case 0:
				break;
			case 5:
			case 6:
				break;
			case 13: // = dead
				break;
			default:
				animState = 0;
				animFrame = 0;
				break;
			}
			break;
		case 1: // running
			switch (animState)
			{
			case 1:
				break;
			case 13: // = dead
				break;
			default:
				animState = 1;
				animFrame = 0;
				break;
			}
			break;
		case 2: // holding shield
			animState = 2;
			animFrame = 0;
			break;
		case 3: // slipping
			switch (animState)
			{
			case 3:
				break;
			case 13: // = dead
				break;
			default:
				animState = 3;
				animFrame = 0;
				break;
			}
			break;
		case 4: // getting up
			switch (animState)
			{
			case 4:
				break;
			case 13: // = dead
				break;
			default:
				animState = 4;
				animFrame = 0;
				break;
			}
			break;
		case 5: // punching with right hand
			if (!lastHandUsedIsRight)
			{
				animState = 5;
				animFrame = 0;
				lastHandUsedIsRight = true;
				break;
			}
			// else: NO BREAK ON PURPOSE!
		case 6: // punching with left hand. Won't be called from outside this method.
			animState = 6;
			animFrame = 0;
			lastHandUsedIsRight = false;
			break;
		case 7:
			switch (animState)
			{
			case 7:
				break;
			case 8:
				if (animFrame == 1)
				{
					animState = 7;
					animFrame = 0;
				}
				break;
			case 9:
				animState = 8;
				animFrame = 0;
				break;
			case 11:
			case 12:
			case 13: // = dead
				break;
			default:
				animState = 7;
				animFrame = 0;
				break;
			}
			break;
		case 8:
			// shouldn't happen
			MAIN.errorMessage("Oh, poop!");
			break;
		case 9:
			switch (animState)
			{
			case 9:
				break;
			case 8:
				if (animFrame == 1)
				{
					animState = 9;
					animFrame = 0;
				}
				break;
			case 7:
				animState = 8;
				animFrame = 0;
				break;
			case 11:
			case 12:
			case 13: // = dead
				break;
			default:
				animState = 9;
				animFrame = 0;
				break;
			}
			break;
		case 10:
			switch (animState)
			{
			case 13: // = dead
				break;
			default:
				animState = 10;
				animFrame = 0;
				break;
			}
			break;
		case 11:
		case 12:
			switch (animState)
			{
			case 13: // = dead
				break;
			default:
				animState = newAnimState;
				animFrame = 0;
				break;
			}
			break;
		case 13:
			// TODO dying animation
			animState = newAnimState;
			animFrame = 0;
			break;
		default:
			MAIN.errorMessage("Non-cased animNum: " + newAnimState);
			break;
		}

		changeImage();
	}

	public void changeImage()
	{
		BufferedImage bf = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D buffy = bf.createGraphics();
		// Bottom
		buffy.drawImage(animationBottom.get(animState).get(animFrame), 0, 0, null);

		// Middle?
		boolean vines = false;
		for (Effect e : effects)
			if (e instanceof Tangled)
				vines = true;
		if (vines)
			buffy.drawImage(animationVine.get(animState).get(animFrame), 0, 0, null);

		// Punch drawings
		if (animState == 5 || animState == 6 || animState == 10 || animState == 11 || animState == 12)
		{
			int index = -1;
			switch (animState)
			{
			case 5:
				if (animFrame >= 2)
					index = 2;
				else
					index = 0;
				break;
			case 6:
				if (animFrame == 2)
					index = 2;
				else
					index = 1;
				break;
			case 10:
				index = 2;
				break;
			case 11:
			case 12:
				index = animState - 11;
				break;
			default:
				MAIN.errorMessage("NNNNNNNNNNNNNNNNOOOOOOO");
				break;
			}
			for (Ability a : punchAffectingAbilities)
				buffy.drawImage(Resources.specialPunches.get(Ability.niceName(a.name)).get(index), 0, 0, null);
		}

		// Top
		buffy.drawImage(animationTop.get(animState).get(animFrame), 0, 0, null);

		changeImage(bf);
	}

	public void randomizeDNA()
	{
		DNA = EPgenerator.generateEPs();
	}

	public void activateDNA()
	{
		abilities.addAll(PowerGenerator.generateAbilities(DNA));
	}

	public void trigger()
	{
		// TEMP. In the future triggers will be based on the danger/trauma of the person.
		randomizeDNA();
		activateDNA();
		if (this instanceof NPC)
			rename();
	}

	public void tempTrigger()
	{
		// like trigger(), but only with currently implemented abilities, and also entirely random :/

		// give 3 random abilities, levels 5
		Random rand = new Random();
		List<String> possibleAbilities = new ArrayList<String>();
		possibleAbilities.addAll(Ability.implementedAbilities);
		possibleAbilities.remove("Punch");
		possibleAbilities.remove("Sprint");
		possibleAbilities.remove("Elemental Combat I");
		for (int i = 0; i < 10;)
		{
			String str = possibleAbilities.get(rand.nextInt(possibleAbilities.size()));
			if (!Ability.elementalPowers.contains(str))
			{
				abilities.add(Ability.ability(str, 5));
				i++;
				possibleAbilities.remove(str);
			}
			else // is an elemental ability
			{
				List<String> elements = new ArrayList<String>();
				for (int j = 0; j < 12; j++)
					elements.add(EP.elementList[j]);
				Collections.shuffle(elements);
				boolean found = false;
				while (!elements.isEmpty())
				{
					String elementString = " <" + elements.get(0) + ">";
					if (Resources.icons.get(str + elementString) != null)
					{
						abilities.add(Ability.ability(str + elementString, 5));
						i++;
						elements.clear();
						found = true;
					}
					else
						elements.remove(0);
				}
				if (!found)
					possibleAbilities.remove(str); // all elements for this ability were used
			}
		}
	}

	public void rename()
	{
		name = NameGenerator.generate(this.DNA);
	}

	public void selfFrame(double deltaTime)
	{
		// this method is activated 1/deltaTime times per second
		// If it's called with deltaTime == 0, it should only clamp the boundaries of health, mana, etc. and use unactivated passive abilities.
		double realDeltaTime = deltaTime;
		deltaTime *= timeEffect;

		for (SoundEffect s : sounds)
			s.setPosition(x, y);
		if (timeSinceLastHit < 20) // 20 seconds of no damage = combat stopped
			timeSinceLastHit += deltaTime;
		else
			inCombat = false;
		if (timeBetweenDamageTexts < 60)
			timeBetweenDamageTexts += deltaTime;
		if (timeSincePortal > 0)
			timeSincePortal -= deltaTime;
		if (notMovingTimer > 0)
			notMovingTimer -= deltaTime;

		if (prone)
		{
			if (slippedTimeLeft > 0)
				slippedTimeLeft -= deltaTime;
			if (slippedTimeLeft > 1)
				switchAnimation(3); // slipping
			else if (slippedTimeLeft > 0 && slippedTimeLeft <= 1)
				switchAnimation(4); // getting up
			if (slippedTimeLeft <= 0)
			{
				slip(false);
			}
		}

		if (timeUntilNextInaccuracyAngleChange > 0)
		{
			timeUntilNextInaccuracyAngleChange -= deltaTime;
			inaccuracyAngle = Methods.lerpAngle(inaccuracyAngle, inaccuracyAngleTarget, 2.15 * deltaTime); // 2.15 because I felt like it
		}
		else
		{
			timeUntilNextInaccuracyAngleChange = 0.5;
			inaccuracyAngleTarget = (1 - 2 * Math.random()) * missAngle;
		}

		// TODO balance this if needed?
		if (inCombat)
		{
			life += lifeRegen * deltaTime;
			mana += manaRegen * realDeltaTime; // mana regen is unaffected by time shenanigans
			stamina += staminaRegen * deltaTime;
		}
		else
		{
			life += lifeRegen * 3 * deltaTime;
			mana += manaRegen * 1.5 * realDeltaTime; // mana regen is unaffected by time shenanigans
			stamina += staminaRegen * 1.5 * deltaTime;
		}

		// boundary checking:
		if (life > maxLife)
			life = maxLife;
		if (mana > maxMana)
			mana = maxMana;
		if (stamina > maxStamina)
			stamina = maxStamina;
		if (charge > 100)
			charge = 100;

		// DIE
		if (life < 0)
		{
			die();
		}

		if (life < 0)
			life = 0;
		if (mana < 0)
			mana = 0;
		if (stamina < 0)
			stamina = 0;
		if (charge < 0)
			charge = 0;

		for (int i = 0; i < abilities.size(); i++) // for (Ability a: abilities) is not possible, because usePassive adds abilities to the run-upon list.
		{
			Ability a = abilities.get(i);
			if (!a.hasTag("passive")) // check if ability isn't passive
			{
				if (a instanceof Punch)
				{
					if (a.cooldownLeft > 0)
						a.cooldownLeft -= deltaTime;// affected by time stretching
					if (a.cooldownLeft < 0)
						a.cooldownLeft = 0;
				}
				else
				{
					if (a.cooldownLeft > 0)
						a.cooldownLeft -= realDeltaTime;// unaffected by time stretching
					if (a.cooldownLeft < 0)
						a.cooldownLeft = 0;
				}
			}
			else if (!a.on) // check if this passive ability is unactivated
				if (!a.disabled)
					a.use(null, this, null); // such elegant
		}
		for (int eNum = 0; eNum < effects.size(); eNum++)
		{
			Effect e = effects.get(eNum);
			if (e.duration != -1)
			{
				if (e.timeAffecting)
					e.timeLeft -= realDeltaTime;
				else if (e.timeLeft > 0)
					e.timeLeft -= deltaTime;
				if (e.timeLeft <= 0)
				{
					affect(e, false); // will remove the effect
					eNum--;
				}
			}
		}

		// time past copies
		pastCopyTimer += realDeltaTime; // unaffected by time stretching
		if (pastCopyTimer >= 1) // every 1 second
		{
			pastCopyTimer -= 1;
			pastCopies.add(new PersonCopy(this));

			// max size is 10 seconds back
			if (pastCopies.size() > 10)
				pastCopies.remove(0); // remove earliest one (from 10 secs ago)
		}
	}

	public void loop(int secondsBackwards, boolean state, boolean position)
	{
		int index = pastCopies.size() - 1 - Math.min(secondsBackwards, pastCopies.size() - 1);
		if (index < 0 || pastCopies.size() <= 0)
			MAIN.errorMessage("nope this is not supposed to happen buddy");
		if (state)
			copyState(pastCopies.get(index));
		if (position)
			copyPosition(pastCopies.get(index));
	}

	public void slip(boolean yes)
	{
		if (yes)
		{
			if (!prone)
			{
				slippedTimeLeft = 3;
				prone = true;
				evasion = 0.8 * evasion; // reducing evasion
			}
		}
		else
		{
			slippedTimeLeft = 0;
			prone = false;
			evasion = evasion / 0.8; // Undo the anti-bonus to evasion
		}
	}

	public void trueDrawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		if (z > 0)
		{
			buffer.rotate(rotation - 0.5 * Math.PI, (int) (x + shadowX * z), (int) (y + shadowY * z));
			buffer.drawImage(shadow, (int) (x - image.getWidth() / 2 + shadowX * z), (int) (y - image.getHeight() / 2 + shadowY * z), null);
			buffer.rotate(-rotation + 0.5 * Math.PI, (int) (x + shadowX * z), (int) (y + shadowY * z));
		}
	}

	public void trueDraw(Graphics2D buffer, double cameraZed)
	{
		BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics2D g = img.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		if (ghostMode)
		{ // TODO merge with real other draw effects
			if (z <= cameraZed) // when in Ghost Mode, people are drawn as if they are higher on the Z axis, in order to make them be drawn above walls. cameraZed will be 1 lower than actual.
			{
				buffer.translate(x, y);
				buffer.scale(z * MAIN.heightZoomRatio + 1, z * MAIN.heightZoomRatio + 1);
				buffer.translate(-x, -y);
				buffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
				buffer.setXORMode(new Color(0, 0, 0, 0));
				buffer.rotate(rotation - 0.5 * Math.PI, (int) (x), (int) (y));
				buffer.drawImage(img, (int) (x - 0.5 * imgW), (int) (y - 0.5 * imgH), null);
				buffer.rotate(-rotation + 0.5 * Math.PI, (int) (x), (int) (y));
				buffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				if (insideWall && panic)
				{
					buffer.setXORMode(new Color(0, 0, 0, 0));
					buffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
					buffer.rotate(rotation - 0.5 * Math.PI, (int) (x), (int) (y));
					buffer.drawImage(img, (int) (x + -50 * (maxLife - life) / maxLife + Math.random() * 100 * (maxLife - life) / maxLife - 0.5 * imgW),
							(int) (y - 50 * (maxLife - life) / maxLife + Math.random() * 100 * (maxLife - life) / maxLife - 0.5 * imgH), null);
					buffer.rotate(-rotation + 0.5 * Math.PI, (int) (x), (int) (y));
					buffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				}
				buffer.translate(x, y);
				buffer.scale(1 / (z * MAIN.heightZoomRatio + 1), 1 / (z * MAIN.heightZoomRatio + 1));
				buffer.translate(-x, -y);
			}
		}
		else if (z <= cameraZed)
		{
			buffer.translate(x, y);
			buffer.scale(z * MAIN.heightZoomRatio + 1, z * MAIN.heightZoomRatio + 1);
			buffer.translate(-x, -y);
			buffer.rotate(rotation - 0.5 * Math.PI, (int) (x), (int) (y));
			// Special shadows
			for (Effect e : effects)
			{
				if (e instanceof Healed)
					drawColoredShadow(buffer, e.strength, Color.green);
				if (e instanceof Nullified)
					drawColoredShadow(buffer, 10, Color.black);
			}
			for (Ability a : abilities)
				if (a.on)
				{
					if (a instanceof Elemental_Void)
						drawColoredShadow(buffer, a.level * 10, Color.gray);
					if (a instanceof Elastic)
					if (velocityPow2() >= ((Elastic)a).minimumVelocityPow2)
						drawColoredShadow(buffer, 10, Color.yellow);
					if (a instanceof Charge)
					if (velocityPow2() >= ((Charge)a).minimumVelocityPow2)
						drawColoredShadow(buffer, 10, Color.yellow);
				}

			if (timeEffect != 1 && timeEffect != 0)
			{
				double intensity = 1;
				if (timeEffect > 1)
					intensity = 1 - 1 / timeEffect; // bigger the faster you are
				if (timeEffect < 1)
					intensity = 1 - timeEffect; // bigger the slower you are
				// Noise and transparency
				for (int i = 0; i < img.getWidth(); i++)
					for (int j = 0; j < img.getHeight(); j++)
						if (img.getRGB(i, j) != 0x00000000) // if not transparent
							if (Math.random() < intensity)
							{
								int RGB = img.getRGB(i, j);
								int R = (RGB >> 16) & 0xff;
								int G = (RGB >> 8) & 0xff;
								int B = (RGB >> 0) & 0xff;
								if (timeEffect < 1) // slow = purple
									img.setRGB(i, j, (new Color((R + 255) / 2, (G / 2), (B + 255) / 2)).getRGB());
								else if (timeEffect > 1) // fast = green, and transparent
									img.setRGB(i, j, (new Color(R, (G + 255) / 2, B / 2, (int) (255 - 200 * intensity))).getRGB());
							}

				// Multiple images
				// buffer.rotate(0.1812, (int) (x), (int) (y));
				// buffer.drawImage(img, (int) (x - 0.5 * imgW), (int) (y - 0.5 * imgH), null);
				// buffer.rotate(-0.1812, (int) (x), (int) (y));
				// buffer.rotate(-0.1812, (int) (x), (int) (y));
				// buffer.drawImage(img, (int) (x - 0.5 * imgW), (int) (y - 0.5 * imgH), null);
				// buffer.rotate(0.1812, (int) (x), (int) (y));
			}
			else if (timeEffect == 0)
			{
				// make grayscale
				for (int i = 0; i < img.getWidth(); i++)
					for (int j = 0; j < img.getHeight(); j++)
					{
						int RGB = img.getRGB(i, j);
						if (RGB != 0x00000000) // if not transparent
						{
							int R = (RGB >> 16) & 0xff;
							int G = (RGB >> 8) & 0xff;
							int B = (RGB >> 0) & 0xff;
							float[] HSB = Color.RGBtoHSB(R, G, B, null);
							HSB[2] += (float) (Math.random() * 0.4 - 0.4 / 2);
							HSB[2] = Math.max(HSB[2], 0f);
							HSB[2] = Math.min(HSB[2], 1f);
							img.setRGB(i, j, Color.HSBtoRGB(HSB[0], 0f, HSB[2]));
						}
					}
			}

			// Player Image
			buffer.drawImage(img, (int) (x - 0.5 * imgW), (int) (y - 0.5 * imgH), null);

			// Special effects
			for (Effect e : effects)
			{
				if (e instanceof Burning)
				{
					BufferedImage flames = Resources.effects.get(0).get(e.animFrame);
					buffer.drawImage(flames, (int) (x - 0.5 * imgW), (int) (y - 0.5 * imgH), null);
				}
			}
			buffer.rotate(-rotation + 0.5 * Math.PI, (int) (x), (int) (y));
			buffer.translate(x, y);
			buffer.scale(1 / (z * MAIN.heightZoomRatio + 1), 1 / (z * MAIN.heightZoomRatio + 1));
			buffer.translate(-x, -y);
		}

	}

	public double velocityPow2()
	{
		return Math.pow(xVel, 2) + Math.pow(yVel, 2);
	}

	public void drawName(Graphics2D buffer, Color nameColor, double cameraRotation)
	{
		buffer.rotate(cameraRotation, x, y);
		// name
		buffer.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
		String s = name;
		if (dead)
		{
			s += " (RIP)";
			buffer.setColor(new Color(50, 50, 50)); // dark gray
		}
		buffer.setColor(nameColor);
		buffer.drawString(s, (int) (x - s.length() / 2 * 10) + 1, (int) (y - radius - 28) + 1);
		buffer.drawString(s, (int) (x - s.length() / 2 * 10) - 1, (int) (y - radius - 28) - 1);
		buffer.setColor(Color.black);
		buffer.drawString(s, (int) (x - s.length() / 2 * 10), (int) (y - radius - 28));

		buffer.rotate(-cameraRotation, x, y);
	}

	public void drawData(Graphics2D buffer, boolean drawLife, boolean drawMana, boolean drawStamina, double cameraRotation)
	{
		buffer.rotate(cameraRotation, x, y);

		// Does not draw data if the person is dead
		if (!dead)
		{
			if (drawLife)
				drawLife(buffer);
			if (drawMana)
				drawMana(buffer);
			if (drawStamina)
				drawStamina(buffer);
		}
		buffer.rotate(-cameraRotation, x, y);
	}

	public void drawLife(Graphics2D buffer)
	{
		buffer.setStroke(new BasicStroke(2));
		buffer.setColor(Color.red);
		buffer.fillRect((int) x - 50, (int) y - radius / 2 - 36, (int) (life / maxLife * 100), 8);
		buffer.setColor(Color.black);
		buffer.drawRect((int) x - 50, (int) y - radius / 2 - 36, 100, 8);
	}

	public void drawMana(Graphics2D buffer)
	{
		buffer.setStroke(new BasicStroke(2));
		buffer.setColor(Color.blue);
		buffer.fillRect((int) x - 50, (int) y - radius / 2 - 28, (int) (mana / maxMana * 100), 8);
		buffer.setColor(Color.black);
		buffer.drawRect((int) x - 50, (int) y - radius / 2 - 28, 100, 8);
	}

	public void drawStamina(Graphics2D buffer)
	{
		buffer.setStroke(new BasicStroke(2));
		buffer.setColor(Color.green);
		buffer.fillRect((int) x - 50, (int) y - radius / 2 - 20, (int) (stamina / maxStamina * 100), 8);
		buffer.setColor(Color.black);
		buffer.drawRect((int) x - 50, (int) y - radius / 2 - 20, 100, 8);
	}

	public void drawUITexts(Graphics2D buffer, double cameraZed, double cameraRotation)
	{
		if (z <= cameraZed)
		{
			buffer.translate(x, y);
			buffer.scale(z * MAIN.heightZoomRatio + 1, z * MAIN.heightZoomRatio + 1);
			buffer.translate(-x, -y);
			buffer.rotate(cameraRotation, x, y);
			for (UIText ui : uitexts)
				ui.draw(buffer, x, y);
			buffer.rotate(-cameraRotation, x, y);
			buffer.translate(x, y);
			buffer.scale(1 / (z * MAIN.heightZoomRatio + 1), 1 / (z * MAIN.heightZoomRatio + 1));
			buffer.translate(-x, -y);
		}

	}

	public void drawWhiteShadow(Graphics2D buffer)
	{
		// TODO figure out what the fuck this is meant to be.
		if (life < 0)
			life = 0;
		if (mana < 0)
			mana = 0;
		if (stamina < 0)
			stamina = 0;
		BufferedImage img = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
		Graphics2D buffy = img.createGraphics();
		buffy.drawImage(shadow, 0, 0, null);
		buffy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, shadowOpacity));
		// Over-complicated purposefully
		buffy.setColor(new Color((int) (255 * life / maxLife * (stamina + mana) / (maxMana + maxStamina)), (int) (255 * stamina / maxStamina * (life + mana) / (maxMana + maxLife)),
				(int) (255 * mana / maxMana * (life + stamina) / (maxLife + maxStamina))));
		buffy.fillRect(0, 0, 96, 96);
		buffy.dispose();
		buffer.rotate(rotation - 0.5 * Math.PI, (int) (x), (int) (y));
		buffer.drawImage(img, (int) (x - imgW / 2), (int) (y - imgH / 2), null);
		buffer.rotate(-rotation + 0.5 * Math.PI, (int) (x), (int) (y));
	}

	public void drawColoredShadow(Graphics2D buffer, double size, Color color)
	{
		BufferedImage img = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
		Graphics2D buffy = img.createGraphics();
		buffy.drawImage(shadow, 0, 0, 96, 96, null);
		buffy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 0.8f));
		buffy.setColor(color);
		buffy.fillRect(0, 0, 96, 96);
		buffy.dispose();

		double factor = 0.6 * Math.log(size);

		buffer.translate(x, y);
		buffer.scale(factor, factor);
		buffer.translate(-x, -y);
		buffer.drawImage(img, (int) (x - imgW / 2), (int) (y - imgH / 2), null);
		buffer.translate(x, y);
		buffer.scale((double) 1 / factor, (double) 1 / factor);
		buffer.translate(-x, -y);
	}

	public void rotate(double rotationAngle, double deltaTime)
	{
		if (rotationAngle == Double.NaN)
		{
			MAIN.errorMessage("NaN, NaN NaN NaN NaN NaN NaN NaN, NaN, Katamari Damaci");
			return;
		}
		deltaTime *= timeEffect;
		final double lerp_constant = 7;
		this.rotation += (((((rotationAngle - this.rotation) % (Math.PI * 2)) + (Math.PI * 3)) % (Math.PI * 2)) - Math.PI) * lerp_constant * deltaTime;
	}

	public double damageAfterHittingArmor(double damage, int damageType, double percentageOfTheDamage)
	{
		// Only a single randomly selected part of the armor parts gets hit by an attack. For example, a thrown spear, fireball or bullet will only either hit the chest, or the head, or the legs, or the arms of a person.
		int n = -1;
		switch (MAIN.random.nextInt(20))
		// quote:
		// "It's arms 25%, legs 25%, head 15%, torso 35%"
		{
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
			n = 2; // arms
			break;
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
			n = 3; // legs
			break;
		case 10:
		case 11:
		case 12:
			n = 0; // head
			break;
		case 13:
		case 14:
		case 15:
		case 16:
		case 17:
		case 18:
		case 19:
			n = 2; // chest
			break;
		default:
			MAIN.errorMessage("Ph'nglui mglw'nafh Cthulhu R'lyeh wgah'nagl fhtagn");
			break;
		}
		Armor a = armorParts[n];
		return damageArmorPart(a, damage, damageType, percentageOfTheDamage);
	}

	public double damageArmorPart(Armor a, double damage, int damageType, double percentageOfTheDamage)
	{
		double effectiveness = a.effectiveness(damageType) * percentageOfTheDamage;
		if (damage < a.armorRating * effectiveness) // armor blocks damage
		{
			// 10% chance of armor degrade
			if (MAIN.random.nextDouble() < 0.1)
				a.reduce(a.maxArmorRating * 0.03 * effectiveness);
			return 0;
		}
		else // armor reduces damage
		{
			damage -= a.armorRating * effectiveness;
			a.reduce(a.maxArmorRating * 0.03 * effectiveness);
		}
		return damage;
	}

	private static int lastIDgiven = 0;

	public static int giveID()
	{
		if (lastIDgiven >= Integer.MAX_VALUE)
		{
			MAIN.errorMessage("HAHAHAHAHAHAHAHA what the fuck?");
			lastIDgiven = Integer.MIN_VALUE;
		}
		return lastIDgiven++;
	}

	public static void resetIDs()
	{
		lastIDgiven = 0;
	}

	public Point Point()
	{
		return new Point((int) x, (int) y);
	}

	public boolean isParahuman()
	{
		return !DNA.isEmpty();
	}

	public boolean elementSensed(int elementNum)
	{
		// check if this person has any powers of that element
		// (TODO reconsider - maybe check DNA instead of abilities, which would not detect given abilities and minor "random" power generation abilities)
		for (Ability a : abilities)
			if (a.elementNum == elementNum)
				return true;

		// check if any effect
		for (Effect e : effects)
			switch (e.name)
			{
			case "Burning":
				if ("Fire".equals(EP.elementList[elementNum]) || "Lava".equals(EP.elementList[elementNum]))
					return true;
				break;
			case "Tangled":
				if ("Plant".equals(EP.elementList[elementNum]))
					return true;
				break;
			case "Stunned": // TODO make sure it's called Stunned and not Stun or something
				if ("Electricity".equals(EP.elementList[elementNum]))
					return true;
				break;
			case "Frozen": // TODO make sure it's called Frozen and not Freeze or something
				if ("Ice".equals(EP.elementList[elementNum]))
					return true;
				break;
			default:
				break;
			}

		return false;
	}

	public boolean equals(Person other)
	{
		return this.id == other.id;
	}

	public void heal(double d)
	{
		// TODO make sure it's alright
		d = Math.min(d, maxLife - life);
		life += d;
		uitexts.add(new UIText(0, -60, "" + (int) d, 2));
	}
}
