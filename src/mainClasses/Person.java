package mainClasses;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import abilities.Sprint;
import effects.Burning;
import effects.Healed;
import mainResourcesPackage.SoundEffect;

public class Person extends RndPhysObj
{
	public int							id;
	public String						name;

	public int							animState;
	public int							animFrame;

	public List<EP>						DNA;
	public List<Ability>				abilities;
	public List<Effect>					effects;
	public List<UIText>					uitexts;

	// STATS
	public int							STRENGTH;
	public int							FITNESS;
	public int							DEXTERITY;
	public int							WITS;
	public int							KNOWLEDGE;
	public int							SOCIAL;

	// SUB-STATS
	public int							maxLife;
	public int							maxMana;
	public int							maxStamina;
	public double						lifeRegen;															// per second. during combat.
	public double						manaRegen;															// ^
	public double						staminaRegen;														// ^
	public double						runSpeed;															// maximum speed in pixel/sec in a single direction while running on dry earth.
	public double						runAccel;
	public int							naturalArmor;
	public double						punchSpeed;
	public double						accuracy;															// from 0 to 1. 0 = 90 degree miss, 1 = 0 degree miss, 0.5 = 45 degree miss.
	public double						missAngle;
	public double						runningStaminaCost;													// per second
	public double						sprintingStaminaCost;												// ^
	public double						evasion;															// chance of an attack missing you
	public double						criticalChance;														// chance of a critical hit
	public double						pushbackResistance;													// pushback immunity

	// Rest of the variables
	public double						life;
	public double						mana;
	public double						stamina;
	public double						charge;
	public boolean						insideWall;
	public boolean						ghostMode;
	public boolean						panic;																// used for panic purposes
	public boolean						prone;																// while ducking or slipping
	public boolean						dead;
	public double						slippedTimeLeft;
	public int							imgW, imgH;															// For drawing purposes only
	public boolean						inCombat;
	public boolean						maintaining;														// whether or not the person is using a maintained ability like Shield or Escalating Scream
	public double						timeSinceLastHit;
	public double						timeBetweenDamageTexts;
	public double						waitingDamage;
	public Point						target;
	public boolean						lastHandUsedIsRight					= false;
	public boolean						punchedSomething					= false;
	public boolean						notMoving							= false;
	public boolean						notAnimating						= false;
	public double						directionOfAttemptedMovement		= 0;
	public double						strengthOfAttemptedMovement			= 0;							// between 0 and 1
	public double						flyDirection						= 0;							// 1 = up, -1 = down.
	public int							abilityTryingToRepetitivelyUse		= -1;
	public int							abilityAiming						= -1;
	public int							abilityMaintaining					= -1;
	public int							commanderID;														// ID of the person's group's leader. If individual, commanderID is the same as id.
	public double						lastSpeed							= 0;							// used for ease of calculation sometimes.
	public boolean						holdingVine							= false;						// true if the person is using a Plant Beam (vine) and grabbling an enemy.
	public double						flySpeed							= -1;

	// for continuous inaccuracy stuff like beams
	public double						inaccuracyAngle						= 0;
	public double						inaccuracyAngleTarget				= 0;
	public double						timeUntilNextInaccuracyAngleChange	= 0;

	// Inventory and stuff?
	public List<Item>					inventory;
	public Armor[]						body;																// head, chest, arms, legs
	public Armor[]						armorParts;															// head, chest, arms, legs

	// Animation
	public List<List<BufferedImage>>	animation;

	// Sounds
	public List<SoundEffect>			sounds								= new ArrayList<SoundEffect>();

	public Person(double x1, double y1)
	{
		super(x1, y1, 0, 0);
		mass = 70; // TODO
		radius = 48;
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
		ghostMode = false;
		insideWall = false;
		timeSinceLastHit = 0;
		timeBetweenDamageTexts = 0;
		waitingDamage = 0;
		panic = false;
		target = new Point(-1, -1);
		initAnimation();
		initSounds();
		imgW = 96;
		imgH = 96;
		initStats();

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
	}

	public void affect(Effect e, boolean add)
	{
		if (!e.stackable)
			for (int i = 0; i < effects.size(); i++)
			{
				Effect e2 = effects.get(i);
				if (e2.name.equals(e.name))
				{
					if (add)
					{
						//set old effect to strength of new effect, and refresh it
						e2.strength = Math.max(e.strength, e2.strength);
						e2.timeLeft = e.duration;
						return;
					} else
					{
						//remove old effect
						e2.unapply(this);
						effects.remove(i);
						i--;
						return;
					}
				}
			}
		if (add)
		{
			e.apply(this);
			effects.add(e);
		}
		else // DELETES OLDEST EFFECT WITH SAME NAME AND STRENGTH
		{
			int oldestEffectIndex = -1;
			for (int i = 0; i < effects.size(); i++)
				if (effects.get(i).name.equals(e.name) && effects.get(i).strength == e.strength)
					if (oldestEffectIndex == -1 || effects.get(i).timeLeft < effects.get(oldestEffectIndex).timeLeft)
						oldestEffectIndex = i;
			if (oldestEffectIndex != -1) // sometimes the program attempts to remove an effect without checking if it already exists. It's ok.
			{
				effects.get(oldestEffectIndex).unapply(this);
				effects.remove(oldestEffectIndex);
			}
		}
	}

	public void damage(double damage)
	{
		life -= damage;
		timeSinceLastHit = 0;
		inCombat = true;
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
		abilities.get(0).range = (int) (1.15 * radius);
		abilities.add(Ability.ability("Sprint", 0));
	}

	public void updateSubStats()
	{
		// should always be overridden.
		Main.errorMessage("WHO IS THIS PERSON");
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
		sounds.add(new SoundEffect("scorched.wav")); // 0 - when a beam hits you
		sounds.get(0).endUnlessMaintained = true;
	}

	public void stopAllSounds()
	{
		for (int i = 0; i < sounds.size(); i++)
			sounds.get(i).stop();
	}

	public void initAnimation()
	{
		// randomize look. TODO
		int legs = Main.random.nextInt(2);
		int chest = Main.random.nextInt(2);
		int head = Main.random.nextInt(2);
		int hair = Main.random.nextInt(2);
		List<Integer> n = new ArrayList<Integer>();
		n.add(legs);
		n.add(chest);
		n.add(head);
		n.add(hair);

		animation = new ArrayList<List<BufferedImage>>();

		animation.add(new ArrayList<BufferedImage>()); // stand
		insertFullBodyAnimation(0, 0, n);
		animation.add(new ArrayList<BufferedImage>()); // walk
		insertFullBodyAnimation(1, 1, n);
		insertFullBodyAnimation(1, 0, n);
		insertFullBodyAnimation(1, 1, n);
		insertFullBodyAnimation(1, 2, n);
		insertFullBodyAnimation(1, 3, n);
		insertFullBodyAnimation(1, 2, n);
		animation.add(new ArrayList<BufferedImage>()); // hold shield
		insertFullBodyAnimation(2, 0, n);
		animation.add(new ArrayList<BufferedImage>()); // slip
		insertFullBodyAnimation(3, 0, n);
		animation.add(new ArrayList<BufferedImage>()); // get up from slip
		insertFullBodyAnimation(4, 0, n);
		insertFullBodyAnimation(4, 1, n);
		insertFullBodyAnimation(4, 2, n);
		animation.add(new ArrayList<BufferedImage>()); // punch with right arm
		insertFullBodyAnimation(5, 0, n);
		insertFullBodyAnimation(5, 0, n);
		insertFullBodyAnimation(5, 1, n);
		animation.add(new ArrayList<BufferedImage>()); // punch with left arm
		insertFullBodyAnimation(6, 0, n);
		insertFullBodyAnimation(6, 0, n);
		insertFullBodyAnimation(6, 1, n);
		animation.add(new ArrayList<BufferedImage>()); // fly
		insertFullBodyAnimation(7, 0, n);
		insertFullBodyAnimation(7, 1, n);
		insertFullBodyAnimation(7, 2, n);
		insertFullBodyAnimation(7, 1, n);
		animation.add(new ArrayList<BufferedImage>()); // fly-hover--transition
		insertFullBodyAnimation(8, 0, n);
		insertFullBodyAnimation(8, 0, n);
		animation.add(new ArrayList<BufferedImage>()); // hover
		insertFullBodyAnimation(9, 0, n);
		animation.add(new ArrayList<BufferedImage>()); // flypunch preparation (arms backwards, ready...)
		insertFullBodyAnimation(10, 0, n);
		insertFullBodyAnimation(10, 1, n);
		insertFullBodyAnimation(10, 2, n);
		insertFullBodyAnimation(10, 1, n);
		animation.add(new ArrayList<BufferedImage>()); // flypunch (right arm)
		insertFullBodyAnimation(11, 0, n);
		insertFullBodyAnimation(11, 0, n);
		animation.add(new ArrayList<BufferedImage>()); // flypunch (right arm)
		insertFullBodyAnimation(12, 0, n);
		insertFullBodyAnimation(12, 0, n);
		animation.add(new ArrayList<BufferedImage>()); // dead
		insertFullBodyAnimation(13, 0, n);

		changeImage(animation.get(animState).get(animFrame));
	}

	public void insertFullBodyAnimation(int stateNum, int frameNum, List<Integer> n)
	{
		// following line might be wrong. It should just start as a transparent 96x96 image.
		BufferedImage img = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
		// temporary fix because I haven't drawn all the pictures yet; NOT in the final game! TODO
		if (img != null)
		{
			Graphics2D g2d = img.createGraphics();
			// legs and chest, which change look depending on the frame
			if (stateNum < 4)
			{
				for (int i = 0; i < 2; i++)
					g2d.drawImage(Resources.bodyPart.get(i).get(n.get(i)).get(stateNum).get(frameNum), 0, 0, null); // first get is 0-1 for legs/chest, second get is for the stand state, third get is for the first stand frame
				// head and hair, which do not
				for (int i = 2; i < 4; i++)
					g2d.drawImage(Resources.bodyPart.get(i).get(n.get(i)).get(stateNum).get(0), 0, 0, null);
			} else
				for (int i = 0; i < 4; i++)
					g2d.drawImage(Resources.bodyPart.get(i).get(n.get(i)).get(stateNum).get(frameNum), 0, 0, null);

			g2d.dispose();
		}
		animation.get(stateNum).add(img);
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
			Main.errorMessage("Non-cased animState: " + animState);
			break;
		}
		// remember - if the animation has repeating frames of the same image, they are just added several times to the list.
		if (animFrame > animation.get(animState).size() - 1)
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
				if (!inCombat)
				{
					animState = 0;
					animFrame = 0;
				}
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
			Main.errorMessage("Oh, poop!");
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
			Main.errorMessage("Non-cased animNum: " + newAnimState);
			break;
		}

		changeImage(animation.get(animState).get(animFrame));
	}

	public void randomizeDNA()
	{
		DNA = EPgenerator.generateEPs();
	}

	public void activateDNA()
	{
		abilities = PowerGenerator.generateAbilities(DNA);
	}

	public void trigger()
	{
		// TEMP. In the future triggers will be based on the danger/trauma of the person.
		randomizeDNA();
		activateDNA();
		if (this instanceof NPC)
			rename();
	}

	public void rename()
	{
		name = NameGenerator.generate(this.DNA);
	}

	public void selfFrame(double deltaTime)
	{
		for (SoundEffect s : sounds)
			s.setPosition(x, y);
		// this method is activated 1/deltaTime times per second
		// If it's called with deltaTime == 0, it should only clamp the boundaries of health, mana, etc. and use unactivated passive abilities.
		if (timeSinceLastHit < 60)
			timeSinceLastHit += deltaTime;
		if (timeBetweenDamageTexts < 60)
			timeBetweenDamageTexts += deltaTime;

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
		} else
		{
			timeUntilNextInaccuracyAngleChange = 0.5;
			inaccuracyAngleTarget = (1 - 2 * Math.random()) * missAngle;
		}

		// TODO balance this if needed?
		if (inCombat)
		{
			life += lifeRegen * deltaTime;
			mana += manaRegen * deltaTime;
			stamina += staminaRegen * deltaTime;
		} else
		{
			life += lifeRegen * 3 * deltaTime;
			mana += manaRegen * 1.5 * deltaTime;
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
				if (a.cooldownLeft > 0)
					a.cooldownLeft -= 1 * deltaTime;
				if (a.cooldownLeft < 0)
					a.cooldownLeft = 0;
			} else if (a.cooldownLeft == 0) // check if this passive ability is unactivated
			{
				a.use(null, this, null); // such elegant
				a.cooldownLeft = -1;
			}
		}
		for (int eNum = 0; eNum < effects.size(); eNum++)
		{
			Effect e = effects.get(eNum);
			if (e.duration != -1)
				if (e.timeLeft > 0)
					e.timeLeft -= deltaTime;
				else
				{
					affect(e, false); //will remove the effect
					eNum--;
				}
		}
	}

	public void slip(boolean yes)
	{
		if (yes)
		{
			slippedTimeLeft = 3;
			prone = true;
			evasion = 0.8 * evasion; // reducing evasion
		} else
		{
			slippedTimeLeft = 0;
			prone = false;
			evasion = evasion / 0.8; // Undo the anti-bonus to evasion
		}
	}

	public void drawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		if (z > 0)
		{
			buffer.rotate(rotation - 0.5 * Math.PI, (int) (x + shadowX * z), (int) (y + shadowY * z));
			buffer.drawImage(shadow, (int) (x - image.getWidth() / 2 + shadowX * z), (int) (y - image.getHeight() / 2 + shadowY * z), null);
			buffer.rotate(-rotation + 0.5 * Math.PI, (int) (x + shadowX * z), (int) (y + shadowY * z));
		}
	}

	public void draw(Graphics2D buffer, double cameraZed)
	{
		BufferedImage img = image;
		if (ghostMode)
		{
			if (z <= cameraZed) // when in Ghost Mode, people are drawn as if they are higher on the Z axis, in order to make them be drawn above walls. cameraZed will be 1 lower than actual.
			{
				buffer.translate(x, y);
				buffer.scale(z * Main.heightZoomRatio + 1, z * Main.heightZoomRatio + 1);
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
				buffer.scale(1 / (z * Main.heightZoomRatio + 1), 1 / (z * Main.heightZoomRatio + 1));
				buffer.translate(-x, -y);
			}
		} else if (z <= cameraZed)
		{
			buffer.translate(x, y);
			buffer.scale(z * Main.heightZoomRatio + 1, z * Main.heightZoomRatio + 1);
			buffer.translate(-x, -y);
			buffer.rotate(rotation - 0.5 * Math.PI, (int) (x), (int) (y));
			for (Effect e : effects)
				if (e instanceof Healed)
					drawLargeGreenShadow(buffer, e.strength);
			buffer.drawImage(img, (int) (x - 0.5 * imgW), (int) (y - 0.5 * imgH), null);
			for (Effect e : effects)
				if (e instanceof Burning)
				{
					img = Resources.effects.get(0).get(e.animFrame);
					buffer.drawImage(img, (int) (x - 0.5 * imgW), (int) (y - 0.5 * imgH), null);
				}
			buffer.rotate(-rotation + 0.5 * Math.PI, (int) (x), (int) (y));
			buffer.translate(x, y);
			buffer.scale(1 / (z * Main.heightZoomRatio + 1), 1 / (z * Main.heightZoomRatio + 1));
			buffer.translate(-x, -y);
		}
	}

	public void drawData(Graphics2D buffer, boolean drawLife, boolean drawMana, boolean drawStamina, double cameraRotation)
	{
		buffer.rotate(cameraRotation, x, y);

		// name
		buffer.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
		buffer.setColor(Color.black);
		String s = name;
		if (dead)
		{
			s += " (RIP)";
			buffer.setColor(new Color(50,50,50)); //dark gray
		}
		buffer.drawString(s, (int) (x - s.length() / 2 * 10), (int) (y - radius - 18));

		//Does not draw data if the person is dead
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
			buffer.scale(z * Main.heightZoomRatio + 1, z * Main.heightZoomRatio + 1);
			buffer.translate(-x, -y);
			buffer.rotate(cameraRotation, x, y);
			for (UIText ui : uitexts)
			{
				buffer.setColor(new Color(ui.color.getRed(), ui.color.getGreen(), ui.color.getBlue(), ui.transparency));
				buffer.setFont(new Font("Sans-Serif", Font.BOLD, ui.fontSize));
				buffer.drawString(ui.text, (int) x + ui.x, (int) y + ui.y);
			}
			buffer.rotate(-cameraRotation, x, y);
			buffer.translate(x, y);
			buffer.scale(1 / (z * Main.heightZoomRatio + 1), 1 / (z * Main.heightZoomRatio + 1));
			buffer.translate(-x, -y);
		}

	}

	public void drawWhiteShadow(Graphics2D buffer)
	{
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

	public void drawLargeGreenShadow(Graphics2D buffer, double strength)
	{
		int extra = (int) (strength * 1.5); // extra outline on each side
		if (life < 0)
			life = 0;
		if (mana < 0)
			mana = 0;
		if (stamina < 0)
			stamina = 0;
		BufferedImage img = new BufferedImage(96 + 2 * extra, 96 + 2 * extra, BufferedImage.TYPE_INT_ARGB);
		Graphics2D buffy = img.createGraphics();
		buffy.drawImage(shadow, 0, 0, 96 + 2 * extra, 96 + 2 * extra, null);
		buffy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 0.8f));
		buffy.setColor(new Color(0, 255, 0)); // greener the more life regen you have
		buffy.fillRect(0, 0, 96 + 2 * extra, 96 + 2 * extra);
		buffy.dispose();

		buffer.drawImage(img, (int) (x - imgW / 2 - extra), (int) (y - imgH / 2 - extra), null);
	}

	public void rotate(double rotationAngle, double deltaTime)
	{
		final double lerp_constant = 7;
		this.rotation += (((((rotationAngle - this.rotation) % (Math.PI * 2)) + (Math.PI * 3)) % (Math.PI * 2)) - Math.PI) * lerp_constant * deltaTime;
	}

	public double damageAfterHittingArmor(double damage, int damageType, double percentageOfTheDamage)
	{
		// Only a single randomly selected part of the armor parts gets hit by an attack. For example, a thrown spear, fireball or bullet will only either hit the chest, or the head, or the legs, or the arms of a person.
		int n = -1;
		switch (Main.random.nextInt(20))
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
			Main.errorMessage("Ph'nglui mglw'nafh Cthulhu R'lyeh wgah'nagl fhtagn");
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
			if (Main.random.nextDouble() < 0.1)
				a.reduce(a.maxArmorRating * 0.03 * effectiveness);
			return 0;
		} else // armor reduces damage
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
			Main.errorMessage("HAHAHAHAHAHAHAHA what the fuck?");
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
}
