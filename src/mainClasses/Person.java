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
import abilities.Clairvoyance;
import abilities.Elastic;
import abilities.Elemental_Void;
import abilities.Punch;
import abilities.Reactive_Explosions;
import abilities.Sprint;
import abilities._FlightAbility;
import effects.Burning;
import effects.E_Resistant;
import effects.Ethereal;
import effects.Healed;
import effects.Nullified;
import effects.Possessed;
import effects.Stunned;
import effects.Tangled;
import mainResourcesPackage.SoundEffect;
import pathfinding.Mover;

/**
 * A Human being that can move, do various actions, have powers, etc. The child classes - Player and NPC - are the ones that are always used.
 * 
 * @author Itamar
 *
 */
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
	// maxCharge is 100. Always.
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
	public double timeUntilPortalConfusionIsOver = 0;
	public List<Ability> punchAffectingAbilities;
	public boolean isAvatar = false;
	public boolean hasChargeAbility = false;
	public boolean isChargingChargeAbility = false;
	public boolean slowRotation = false;

	// for continuous inaccuracy stuff like beams
	public double inaccuracyAngle = 0;
	public double inaccuracyAngleTarget = 0;
	public double timeUntilNextInaccuracyAngleChange = 0;

	// stuff
	String voiceType; // Male, Female. TODO add more
	public Map<Environment, Area> visibleArea;
	public Map<Environment, int[][]> seenBefore;

	// Inventory and stuff?
	public List<Item> inventory;
	public Armor body;
	public Armor armor;

	// Animation
	public List<List<BufferedImage>> animationBottom;
	public List<List<BufferedImage>> animationTop;
	public List<List<BufferedImage>> animationVine;
	public List<List<List<BufferedImage>>> animationArmor;

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
	public boolean startStopPossession;
	public double possessedTimeLeft;
	public int possessingControllerID = -1;
	public int possessionTargetID = -1;
	public boolean possessionVessel = false;
	public boolean onlyNaturalAbilities = false;

	/**
	 * Constructor
	 * 
	 * @param x1
	 *            x position of person
	 * @param y1
	 *            y position of person
	 */
	public Person(double x1, double y1)
	{
		super(x1, y1, 0, 0);
		visibleArea = new HashMap<Environment, Area>();
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
		timeSinceLastHit = 0.01;
		timeBetweenDamageTexts = 0;
		waitingDamage = 0;
		panic = false;
		target = new Point(-1, -1);
		imgW = 96;
		imgH = 96;

		inventory = new ArrayList<Item>();
		armor = new Armor(0, "temp");
		initStats();
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

		// randomize look
		legs = 1 + MAIN.random.nextInt(2);
		chest = 1 + MAIN.random.nextInt(2);
		head = MAIN.random.nextInt(2);
		hair = 1 + MAIN.random.nextInt(2);
		nakedLegs = MAIN.random.nextInt(1);
		nakedChest = MAIN.random.nextInt(1);
		initAnimation();
		body = (new Armor(naturalArmor, "Skin"));
		// Default armor is no armor
		putArmor(body);
	}

	/**
	 * Sets all "state" related fields of this Person to <code>other</code>'s fields. List of fields that are changed: {@link #runAccel}, {@link #runSpeed}, {@link #lifeRegen}, {@link #manaRegen}, {@link #staminaRegen}, {@link #animState},
	 * {@link #animFrame}, {@link #punchAffectingAbilities}, {@link #drawLife(Graphics2D)}, {@link #stamina}, {@link #ghostMode}, {@link #panic}, {@link #prone}, {@link #dead}, {@link #slippedTimeLeft}, {@link #directionOfAttemptedMovement},
	 * {@link #strengthOfAttemptedMovement}, {@link #timeEffect}, {@link #startStopPossession}, {@link #possessedTimeLeft}, {@link #possessionTargetID}, {@link #abilities}'s {@link Ability#on} field and {@link #effects} (creating new instances of
	 * them).
	 * 
	 * <br>
	 * <br>
	 * 
	 * Important fields that are NOT copied: {@link #mana}, {@link #charge}.
	 * 
	 * @param other
	 *            the PersonCopy that represents the other state. Assumed to be a past version of this Person
	 */
	public void copyState(PersonCopy other)
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
		this.animState = other.animState;
		this.animFrame = other.animFrame;
		this.punchAffectingAbilities = other.punchAffectingAbilities;
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
		this.startStopPossession = other.possessing;
		this.possessedTimeLeft = other.possessedTimeLeft;
		this.possessingControllerID = other.possessingControllerID;
		this.possessionTargetID = other.possessingVictimID;

		for (Ability a : this.abilities)
		{
			if (a.hasTag("on-off"))
				a.on = other.abilities.get(a);
			// This might be a BAD IDEA:
			a.disabled = false;
		}

		this.effects = new ArrayList<Effect>();
		for (Effect e : other.effects)
			this.affect(e, true);
	}

	/**
	 * Sets all "position" related fields of this Person to <code>other</code>'s fields. Position, rotation, velocity. List of fields that are changed: x, y, z, xVel, yVel, zVel, rotation.
	 * 
	 * @param other
	 *            the PersonCopy that represents the other state. Assumed to be a past version of this Person
	 */
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

	/**
	 * Adds or removes an {@link Effect} to this Person. Will not add an effect if there is already an existing effect with that name and the effect is not stackable (will remove the weaker one of the two, more or less). Will remove an Effect if it
	 * matches the given effect's name and creatorAbility (or only the name, if the creator ability is null).
	 * 
	 * @param e
	 *            the Effect to be added/removed
	 * @param add
	 *            whether or not this effect is added (true) or removed (false).
	 */
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
						e2.unapply(this);
						e2.strength = Math.max(e.strength, e2.strength);
						e2.timeLeft = e.duration;
						e2.apply(this);
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

	/**
	 * Removes a number of effects (by order of age) from this person.
	 * 
	 * @param amount
	 *            number of effects to be removed
	 */
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

	/**
	 * Deals damage to this person (reduces {@link #life} by an amount). Does not take into account damage type, armor, etc. Will activate any abilities/variables if needed.
	 * 
	 * Also, if this person was damaged while in the air and flying, there is a chance of (damage / maxLife * 10) that they will fall and slip.
	 * 
	 * @param damage
	 */
	public void damage(double damage)
	{
		life -= damage;
		if (damage >= 2.5 || Math.random() < 0.01) // so that beams and wall collisions don't always trigger this
		{
			timeSinceLastHit = 0;
		}
		for (Ability a : abilities)
			if (a.on)
			{
				if (a instanceof Reactive_Explosions)
				{
					Reactive_Explosions aa = (Reactive_Explosions) a;
					if (damage >= aa.minimumDamageNeeded)
						aa.prepareToExplode = true;
				}
			}
		// TODO - this is kinda buggy with the fact that normal wall collisions can damage, and that makes clones stay "in combat" too much
		inCombat = true;
		if (possessedTimeLeft > 0 && life < 0.20 * maxLife && !this.isAvatar) // get out of possession
		{
			possessedTimeLeft = 0;
			startStopPossession = true;
		}

		if (flySpeed != -1)
		{
			double chanceToDrop = damage / maxLife * 10;
			if (Math.random() < chanceToDrop)
			{
				for (Ability a : abilities)
					if (a.on)
						if (a instanceof _FlightAbility)
						{
							a.use(null, this, null);
							slip(true);
						}
			}
		}
	}

	/**
	 * Sets all stats and substats to their default values, and gives the natural abilities: {@link Punch} and {@link Sprint}.
	 * 
	 * Right now the default stat values are 3 per stat.
	 */
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
		charge = 100;

		// Natural abilities
		abilities.add(Ability.ability("Punch", 0));
		abilities.get(0).range = (int) (2.3 * radius);
		abilities.add(Ability.ability("Sprint", 0));
	}

	/**
	 * Sets stats to some values. Will not change a stat if the parameter is -1.
	 * 
	 * @param str
	 *            {@link #STRENGTH}
	 * @param dex
	 *            {@link #DEXTERITY}
	 * @param fit
	 *            {@link #FITNESS}
	 * @param wit
	 *            {@link #WITS}
	 * @param know
	 *            {@link #KNOWLEDGE}
	 * @param soc
	 *            {@link #SOCIAL}
	 */
	public void setStats(int str, int dex, int fit, int wit, int know, int soc)
	{
		if (str != -1)
			STRENGTH = str;
		if (dex != -1)
			DEXTERITY = dex;
		if (fit != -1)
			FITNESS = fit;
		if (wit != -1)
			WITS = wit;
		if (know != -1)
			KNOWLEDGE = know;
		if (soc != -1)
			SOCIAL = soc;
	}

	/**
	 * Multiplies all stats by an amount.
	 * 
	 * @param statMultiplier
	 */
	public void multiplyStats(double statMultiplier)
	{
		STRENGTH = (int) (statMultiplier * STRENGTH);
		DEXTERITY = (int) (statMultiplier * DEXTERITY);
		FITNESS = (int) (statMultiplier * FITNESS);
		WITS = (int) (statMultiplier * WITS);
		KNOWLEDGE = (int) (statMultiplier * KNOWLEDGE);
		SOCIAL = (int) (statMultiplier * SOCIAL);
	}

	/**
	 * Should always be overridden. Is called at the end of the constructor
	 */
	public void updateSubStats()
	{
		// should always be overridden.
	}

	/**
	 * Sets all sub-stats to their default values (depending only on the stats). Should only be called once, when this Person is created, in the constructor.
	 * 
	 * <p>
	 * maxStamina = 4 + 2 * FITNESS. naturalArmor = 0.7*STRENGTH + 0.3*FITNESS. lifeRegen = 1. manaRegen = 0.5. staminaRegen = 0.2 + 0.1*FITNESS. flightVisionDistance = 100*WITS. accuracy = 1 - 0.6 / (DEXTERITY+1). runningStaminaCost = 0.45.
	 * sprintingStaminaCost = 1.8. evasion = 1-0.99431695501 ^ (DEXTERITY*WITS). criticalChance = same calculation as evasion. pushbackResistance = 0. Also, calls updateAccuracy().
	 */
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
		runningStaminaCost = 0.45;
		sprintingStaminaCost = 1.8;
		evasion = 1 - Math.pow(0.99431695501, (DEXTERITY * WITS)); // average is EXACTLY 5%!
		criticalChance = 1 - Math.pow(0.99431695501, (DEXTERITY * WITS)); //
		pushbackResistance = 0;
	}

	/**
	 * updates the {@link #missAngle} field according to {@link #accuracy}.
	 */
	public void updateAccuracy()
	{
		missAngle = (1 - accuracy) * Math.PI / 3; // DEX 0 = 60 degree miss, DEX 3 = 15.
	}

	/**
	 * This kills the person.
	 */
	public void die()
	{
		switchAnimation(13); // TODO death animation
		dead = true;
		prone = false;
		abilityAiming = -1;
		abilityMaintaining = -1;
		abilityTryingToRepetitivelyUse = -1;
		// Abilities will be deactivated in the Main class' frame() method
		// Abilities can't be activated any more (pressAbilityKey() will not work on dead people)
	}

	/**
	 * Returns z + height. If this person is prone, height is multiplied by 0.8. If this person is dead, height is multiplied by 0.1.
	 * 
	 * @return highest point of this person (in the Z dimension)
	 */
	public double highestPoint()
	{
		double functionalHeight = height;
		if (prone)
			functionalHeight *= 0.8;
		if (dead)
			functionalHeight *= 0.1;
		return z + functionalHeight;
	}

	/**
	 * Adds sounds to the {@link #sounds} list, from the files.
	 */
	public void initSounds()
	{
		sounds.add(new SoundEffect("Scorched.wav")); // 0 - when a beam hits you
		sounds.get(0).endUnlessMaintained = true;
		sounds.add(new SoundEffect("Person Fall.wav")); // 1 - fall damage
		for (int i = 1; i <= 5; i++)
			sounds.add(new SoundEffect(voiceType + "_Grunt_" + i + ".wav")); // 2-6 - pain/grunt
	}

	/**
	 * Calls {@link SoundEffect#stop()} on all sounds in {@link #sounds}.
	 */
	public void stopAllSounds()
	{
		for (int i = 0; i < sounds.size(); i++)
			sounds.get(i).stop();
	}

	/**
	 * Sets the {@link #animationBottom}, {@link #animationTop}, {@link #animationVine}, {@link #animationArmor} lists. They depend on the body variables: {@link #legs}, {@link #chest}, {@link #head}, {@link #hair}, {@link #nakedLegs},
	 * {@link #nakedChest}.
	 */
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
		animationArmor = new ArrayList<List<List<BufferedImage>>>();
		for (int i = 0; i < Resources.armor.size(); i++)
			animationArmor.add(new ArrayList<List<BufferedImage>>());

		increaseAnimationListSize(); // stand
		insertFullBodyAnimation(0, 0, n);
		insertFullBodyAnimation(0, 1, n);

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

	/**
	 * Adds a new BufferedImage List to animationBottom, animationTop, animationVine, and all animationArmor lists.
	 */
	void increaseAnimationListSize()
	{
		animationBottom.add(new ArrayList<BufferedImage>());
		animationTop.add(new ArrayList<BufferedImage>());
		animationVine.add(new ArrayList<BufferedImage>());
		for (int i = 0; i < animationArmor.size(); i++)
			animationArmor.get(i).add(new ArrayList<BufferedImage>());
	}

	/**
	 * Inserts the appropriate frame to the appropriate place in the animation lists.
	 * 
	 * @param stateNum
	 *            state (e.g. running, flying, punching, slipping...)
	 * @param frameNum
	 *            frame number (e.g. 0, 1, 2, 3...)
	 * @param bodyParts
	 *            list of body part types.
	 */
	void insertFullBodyAnimation(int stateNum, int frameNum, List<Integer> bodyParts)
	{
		if (stateNum < 4)
		{
			BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = img.createGraphics();
			g2d.drawImage(Resources.bodyPart.get(5).get(bodyParts.get(5)).get(stateNum).get(frameNum), 0, 0, null); // naked legs
			if (bodyParts.get(0) != 0)
				g2d.drawImage(Resources.bodyPart.get(0).get(bodyParts.get(0)).get(stateNum).get(frameNum), 0, 0, null); // legs
			g2d.drawImage(Resources.bodyPart.get(6).get(bodyParts.get(6)).get(stateNum).get(frameNum), 0, 0, null); // naked chest
			if (bodyParts.get(1) != 0)
				g2d.drawImage(Resources.bodyPart.get(1).get(bodyParts.get(1)).get(stateNum).get(frameNum), 0, 0, null); // chest

			animationBottom.get(stateNum).add(img);
			g2d.dispose();
			img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
			g2d = img.createGraphics();
			// head and hair don't change in these frames, hence the .get(0):
			g2d.drawImage(Resources.bodyPart.get(2).get(bodyParts.get(2)).get(stateNum).get(0), 0, 0, null); // head
			if (bodyParts.get(3) != 0)
				g2d.drawImage(Resources.bodyPart.get(3).get(bodyParts.get(3)).get(stateNum).get(0), 0, 0, null); // hair
			animationTop.get(stateNum).add(img);
		}
		else
		{
			BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = img.createGraphics();
			g2d.drawImage(Resources.bodyPart.get(5).get(bodyParts.get(5)).get(stateNum).get(frameNum), 0, 0, null); // naked legs
			if (bodyParts.get(0) != 0)
				g2d.drawImage(Resources.bodyPart.get(0).get(bodyParts.get(0)).get(stateNum).get(frameNum), 0, 0, null); // legs
			g2d.drawImage(Resources.bodyPart.get(6).get(bodyParts.get(6)).get(stateNum).get(frameNum), 0, 0, null); // naked chest
			if (bodyParts.get(1) != 0)
				g2d.drawImage(Resources.bodyPart.get(1).get(bodyParts.get(1)).get(stateNum).get(frameNum), 0, 0, null); // chest
			animationBottom.get(stateNum).add(img);
			g2d.dispose();
			img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
			g2d = img.createGraphics();
			g2d.drawImage(Resources.bodyPart.get(2).get(bodyParts.get(2)).get(stateNum).get(frameNum), 0, 0, null); // head
			if (bodyParts.get(3) != 0)
				g2d.drawImage(Resources.bodyPart.get(3).get(bodyParts.get(3)).get(stateNum).get(frameNum), 0, 0, null); // hair
			animationTop.get(stateNum).add(img);
		}

		BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.drawImage(Resources.bodyPart.get(4).get(0).get(stateNum).get(frameNum), 0, 0, null);
		g2d.dispose();

		animationVine.get(stateNum).add(img);
		for (int i = 0; i < animationArmor.size(); i++)
		{
			img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
			g2d = img.createGraphics();
			g2d.drawImage(Resources.armor.get(i).get(stateNum).get(frameNum), 0, 0, null);
			animationArmor.get(i).get(stateNum).add(img);
			g2d.dispose();
		}
	}

	/**
	 * Increases {@link #animFrame} by 1, if frameNum is divisible by something depending on the animation state, and sets it to 0 if the cycle is done. Is called every frame.
	 * 
	 * @param frameNum
	 *            the variable to be tested. Expected to grow by 1 every frame.
	 */
	public void nextFrame(int frameNum)
	{
		switch (animState)
		{
		/// <number of frames per second> <animation name>
		case 0: // standing
			if (frameNum % 20 == 0)
				animFrame++;
			break;
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

	/**
	 * Switches the animation state to a new one, if the switch is valid.
	 * 
	 * @param newAnimState
	 */
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

	/**
	 * Changes the {@link Drawable#image} variable. Is called whenever this Person's image changes. Also draws additional stuff, like vines or armor, in the image.
	 */
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
		// Armor
		if (armor != body && armor.isElemental())
		{
			int index = EP.toInt(armor.name);
			buffy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (armor.armorRating / armor.maxArmorRating)));
			buffy.drawImage(animationArmor.get(index).get(animState).get(animFrame), 0, 0, null);
			buffy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		}
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

	/**
	 * Randomizes DNA by calling {@link EPgenerator#generateEPs()}.
	 */
	public void randomizeDNA()
	{
		DNA = EPgenerator.generateEPs();
	}

	/**
	 * Activates DNA by calling {@link PowerGenerator#generateAbilities(List)} on {@link #DNA}. <b>Will only ADD all of these abilities.</b>
	 */
	public void activateDNA()
	{
		abilities.addAll(PowerGenerator.generateAbilities(DNA));
	}

	/**
	 * Calls {@link #randomizeDNA()} and {@link #activateDNA()} and, if this is an NPC, {@link #rename()}.
	 */
	public void trigger()
	{
		// TEMP. In the future triggers will be based on the danger/trauma of the person.
		randomizeDNA();
		activateDNA();
		if (this instanceof NPC)
			rename();
	}

	/**
	 * Temporary trigger function; gives a large number of abilities to this player. Should be removed when all abilities are implemented.
	 * <p>
	 * Gives 6 abilities in levels 1-10, from the {@link Ability#implementedAbilities} pool.
	 */
	public void tempTrigger()
	{
		// like trigger(), but only with currently implemented abilities, and also entirely random :/

		// give 6 random abilities, levels 1-7
		Random rand = new Random();
		List<String> possibleAbilities = new ArrayList<String>();
		possibleAbilities.addAll(Ability.implementedAbilities);
		possibleAbilities.add("Ball");
		possibleAbilities.add("Ball");
		possibleAbilities.add("Ball");
		possibleAbilities.add("Beam");
		possibleAbilities.add("Beam");
		possibleAbilities.add("Spray");
		possibleAbilities.add("Spray");
		possibleAbilities.remove("Punch");
		possibleAbilities.remove("Sprint");
		possibleAbilities.remove("Elemental Combat I");
		possibleAbilities.remove("Elemental Combat II");
		for (int i = 0; i < 6;)
		{
			Ability a = null;
			int level = (int) (Math.random() * 9) + 1;
			String str = possibleAbilities.get(rand.nextInt(possibleAbilities.size()));
			if (!Ability.elementalPowers.contains(str))
			{
				a = Ability.ability(str, level);
				abilities.add(a);
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
						a = Ability.ability(str + elementString, level);
						abilities.add(a);
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
			if (a != null)
			{
				for (String s : a.possiblePerks())
					if (Math.random() < 0.3)
						a.addPerk(s);
			}
		}
	}

	/**
	 * Sets {@link #name} to {@link NameGenerator#generate(List)} according to {@link #DNA}.
	 */
	public void rename()
	{
		name = NameGenerator.generate(this.DNA);
	}

	/**
	 * Important function! Is called every frame, and does all the things that should happen every frame, pretty much. This includes setting sound positions, increasing the value of all timers, regenerating life/mana/stamina by the appropriate
	 * values, clamping life/mana/stamina to the possible range of values, dying if life is 0 or less, reducing times for abilities and effects, turning passive abilities on if possible, and saving the current state and position every second in
	 * {@link #pastCopies}.
	 * 
	 * @param deltaTime
	 *            the time delta. <u>Is multiplied by timeEffect!</u>
	 */
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
		if (timeUntilPortalConfusionIsOver > 0)
			timeUntilPortalConfusionIsOver -= deltaTime;
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

		if (armor.armorRating < armor.maxArmorRating * 0.1)
		{
			// LOSE ARMOR
			putArmor(body);
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
		if (life <= 0)
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
				if (e instanceof Possessed && possessionVessel)
					e.timeLeft -= 0;
				else if (e.timeAffecting)
					e.timeLeft -= realDeltaTime;
				else if (e.timeLeft > 0)
					e.timeLeft -= deltaTime;
				if (e.timeLeft <= 0)
				{
					if (e instanceof Ethereal && insideWall)
						continue;
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

	/**
	 * Loops thisk zv person's state and/or position a number of seconds back in time.
	 * 
	 * @param secondsBackwards
	 *            number of seconds to go back
	 * @param state
	 *            whether to loop state or not
	 * @param position
	 *            whether to loop position or not
	 * @see {@link #copyState(PersonCopy)}, {@link #copyPosition(PersonCopy)}
	 */
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

	/**
	 * Makes this person slip, or un-slip. While slipping a person is {@link #prone}, and their evasion is reduced, and usually they can't control their movement anymore. Effects stop when {@link #slippedTimeLeft} reaches 0, after 3 seconds.
	 * 
	 * @param yes
	 *            true = start slipping (unless already slipping), false = stop slipping
	 */
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

	/**
	 * Draws the shadow of this person.
	 */
	public void trueDrawShadow(Graphics2D buffer, double shadowX, double shadowY)
	{
		if (z > 0)
		{
			buffer.rotate(rotation - 0.5 * Math.PI, (int) (x + shadowX * z), (int) (y + shadowY * z));
			buffer.drawImage(shadow, (int) (x - image.getWidth() / 2 + shadowX * z), (int) (y - image.getHeight() / 2 + shadowY * z), null);
			buffer.rotate(-rotation + 0.5 * Math.PI, (int) (x + shadowX * z), (int) (y + shadowY * z));
		}
	}

	/**
	 * Draws this person. Includes special cool effects, yes!
	 */
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
						drawColoredShadow(buffer, a.LEVEL * 10, Color.gray);
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
			// Eyes
			if (possessionVessel)
				buffer.drawImage(Resources.abilities.get("possession_eyes"), (int) (x - 0.5 * imgW), (int) (y - 0.5 * imgH), null);
			for (Ability a : abilities)
				if (a.on)
				{
					if (a instanceof Clairvoyance)
						buffer.drawImage(Resources.abilities.get("clairvoyance_eyes"), (int) (x - 0.5 * imgW), (int) (y - 0.5 * imgH), null);
					if (a instanceof Elastic)
						if (velocityPow2() >= ((Elastic) a).minimumVelocityPow2)
							buffer.drawImage(Resources.abilities.get("elasticcharge_eyes"), (int) (x - 0.5 * imgW), (int) (y - 0.5 * imgH), null);
					if (a instanceof Charge)
						if (velocityPow2() >= ((Charge) a).minimumVelocityPow2)
							buffer.drawImage(Resources.abilities.get("elasticcharge_eyes"), (int) (x - 0.5 * imgW), (int) (y - 0.5 * imgH), null);
				}
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

	/**
	 * 
	 * @return velocity squared of this person.
	 */
	public double velocityPow2()
	{
		return Math.pow(xVel, 2) + Math.pow(yVel, 2);
	}

	/**
	 * Draws this person's name, in the given color. Rotates the Graphics2D object back in order to always draw in the right orientation no matter the camera rotation.
	 * 
	 * @param buffer
	 * @param nameColor
	 * @param cameraRotation
	 */
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
		buffer.drawString(s, (int) (x - s.length() / 2 * 10) + 1, (int) (y - radius - 28) - 1);
		buffer.drawString(s, (int) (x - s.length() / 2 * 10) - 1, (int) (y - radius - 28) + 1);
		buffer.setColor(Color.black);
		buffer.drawString(s, (int) (x - s.length() / 2 * 10), (int) (y - radius - 28));

		buffer.rotate(-cameraRotation, x, y);
	}

	/**
	 * Draws life, mana and stamina, depending on what booleans are given. Rotates the Graphics2D object back in order to always draw in the right orientation no matter the camera rotation. Will not draw if {@link #dead} == true.
	 * 
	 * @param buffer
	 * @param drawLife
	 * @param drawMana
	 * @param drawStamina
	 * @param cameraRotation
	 */
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

	/**
	 * Draws life bar.
	 * 
	 * @param buffer
	 */
	public void drawLife(Graphics2D buffer)
	{
		buffer.setStroke(new BasicStroke(2));
		buffer.setColor(Color.red);
		buffer.fillRect((int) x - 50, (int) y - radius / 2 - 36, (int) (life / maxLife * 100), 8);
		buffer.setColor(Color.black);
		buffer.drawRect((int) x - 50, (int) y - radius / 2 - 36, 100, 8);
	}

	/**
	 * Draws mana bar.
	 * 
	 * @param buffer
	 */
	public void drawMana(Graphics2D buffer)
	{
		buffer.setStroke(new BasicStroke(2));
		buffer.setColor(Color.blue);
		buffer.fillRect((int) x - 50, (int) y - radius / 2 - 28, (int) (mana / maxMana * 100), 8);
		buffer.setColor(Color.black);
		buffer.drawRect((int) x - 50, (int) y - radius / 2 - 28, 100, 8);
	}

	/**
	 * Draws stamina bar.
	 * 
	 * @param buffer
	 */
	public void drawStamina(Graphics2D buffer)
	{
		buffer.setStroke(new BasicStroke(2));
		buffer.setColor(Color.green);
		buffer.fillRect((int) x - 50, (int) y - radius / 2 - 20, (int) (stamina / maxStamina * 100), 8);
		buffer.setColor(Color.black);
		buffer.drawRect((int) x - 50, (int) y - radius / 2 - 20, 100, 8);
	}

	/**
	 * Draws UITexts (like damage ealt to this person recently, or the "EVASION!" text that appears when dodging attacks. Will only draw if z <= cameraZed.
	 * 
	 * @param buffer
	 * @param cameraZed
	 * @param cameraRotation
	 */
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

	/**
	 * I don't know what the fuck this is meant to be. I think it's like...a randomly colored shadow, except instead of being random it's some bizarre calculation with life and mana and stamina?
	 * 
	 * @param buffer
	 */
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

	/**
	 * Draws a colored shadow in the person's position. Shadow size is logarithmically dependent on the size variable given, and is in the chosed color.
	 * 
	 * @param buffer
	 * @param size
	 *            The scale is 0.6 * Math.log(size).
	 * @param color
	 */
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

	/**
	 * 
	 * Rotates this person towards the rotationAngle, using lerping. Rotation will be slower if the {@link #slowRotation} variable is true, and will also depend on timeEffect.
	 * 
	 * @param rotationAngle
	 *            target angle
	 * @param deltaTime
	 *            "amount" of rotation, usually equal to the amount of time since the last frame
	 */
	public void rotate(double rotationAngle, double deltaTime)
	{
		if (Double.isNaN(rotationAngle))
		{
			MAIN.errorMessage("NaN, NaN NaN NaN NaN NaN NaN NaN, NaN, Katamari Damaci");
			return;
		}
		deltaTime *= timeEffect;
		final double lerp_constant = 7;
		double amount = (((((rotationAngle - this.rotation) % (Math.PI * 2)) + (Math.PI * 3)) % (Math.PI * 2)) - Math.PI);
		double maxRotation = slowRotation ? 0.4 : 2;
		if (amount > maxRotation)
			amount = maxRotation;
		if (amount < -maxRotation)
			amount = -maxRotation;
		this.rotation += amount * lerp_constant * deltaTime;
	}

	/**
	 * calls {@link #damageArmorPart(Armor, double, int, double)}, pretty much. Used to do stuff with limbs.
	 * 
	 * @param damage
	 * @param damageType
	 *            (see {@link EP#damageType}
	 * @param percentageOfTheDamage
	 *            (0-1)
	 * @return damage after taking armor into account
	 */
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
		// TODO do something with n
		n = n + 0; // filler
		return damageArmorPart(armor, damage, damageType, percentageOfTheDamage);
	}

	/**
	 * Returns damage after being reduced (or possibly increased) by the armor part. Damage is reduced by the armor's AR times the percentage of damage times the "effectiveness". Effectiveness depends on how the armor's strong/weak against the damage
	 * type.
	 * 
	 * @param a
	 *            the armor that's used
	 * @param damage
	 * @param damageType
	 * @param percentageOfTheDamage
	 * @return damage after the calculations. Never less than 0.
	 * @see Armor#effectiveness(int)
	 */
	public double damageArmorPart(Armor a, double damage, int damageType, double percentageOfTheDamage)
	{
		double effectiveness = a.effectiveness(damageType) * percentageOfTheDamage;
		if (damage < a.armorRating * effectiveness) // armor blocks damage
		{
			// 10% chance of armor degrading
			if (MAIN.random.nextDouble() < 0.1)
				a.reduce(a.maxArmorRating * 0.03);
			return 0;
		}
		else if (effectiveness != 0) // armor reduces damage, or is vulnerable
		{
			damage -= a.armorRating * effectiveness;
			a.reduce(a.maxArmorRating * 0.03);
		}
		// NOTE: if armor is specifically weak to the damage type, damage WILL increase to the person and the armor both.
		return damage;
	}

	private static int lastIDgiven = 0;

	/**
	 * Returns a unique ID number, and increases {@link #lastIDgiven} by 1.
	 * 
	 * @return a unique ID number
	 */
	public static int giveID()
	{
		if (lastIDgiven >= Integer.MAX_VALUE)
		{
			MAIN.errorMessage("HAHAHAHAHAHAHAHA what the fuck?");
			lastIDgiven = Integer.MIN_VALUE;
		}
		return lastIDgiven++;
	}

	/**
	 * Decreases {@link #lastIDgiven} by 1.
	 */
	public static void cancelID()
	{
		lastIDgiven--;
	}

	/**
	 * Sets {@link #lastIDgiven} to 0.
	 */
	public static void resetIDs()
	{
		lastIDgiven = 0;
	}

	/**
	 * Returns a Point object representing this person's location (center of their body) on the XY plane. Since this is a Point object, values are integers. Units are pixels, by the way.
	 */
	public Point Point()
	{
		return new Point((int) x, (int) y);
	}

	/**
	 * 
	 * @return true if DNA is not empty.
	 */
	public boolean isParahuman()
	{
		return !DNA.isEmpty();
	}

	/**
	 * Returns whether or not this person would be sensed by a specific Sense Element element (ignoring distance). Is true if there is any Ability in {@link #abilities} with that element, or if any Effect in {@link #effects} is caused by the
	 * element's damage type (relevant to Burning, Tangled, Frozen, Stunned).
	 * 
	 * @param elementNum
	 * @return
	 */
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

	/**
	 * Returns true only if the other object is a Person with the same ID as this.
	 */
	public boolean equals(Object other)
	{
		if (other instanceof Person)
			return this.id == ((Person) other).id;
		return false;
	}

	/**
	 * Increases {@link #life} by an amount
	 * 
	 * @param healAmount
	 */
	public void heal(double healAmount)
	{
		healAmount = Math.min(healAmount, maxLife - life);
		life += healAmount;
		uitexts.add(new UIText(0, -60, "" + (int) healAmount, 2)); // should probably not be here
	}

	/**
	 * Copies almost all fields from another Person. There are so many fields that are copied, plus, you need to make sure you didn't accidentally forget to add a field here after adding it to Person. So, uh, try to remember this method.
	 * 
	 * @param other
	 */
	public void copy(Person other)
	{
		this.onlyNaturalAbilities = other.onlyNaturalAbilities;
		this.id = other.id;
		this.setStats(other.STRENGTH, other.DEXTERITY, other.FITNESS, other.WITS, other.KNOWLEDGE, other.SOCIAL);
		this.maintaining = other.maintaining;
		this.abilityAiming = other.abilityAiming;
		this.abilityMaintaining = other.abilityMaintaining;
		this.abilityTryingToRepetitivelyUse = -1;
		this.hasChargeAbility = other.hasChargeAbility;
		this.isChargingChargeAbility = other.isChargingChargeAbility;
		this.slowRotation = other.slowRotation;
		this.lifeRegen = other.lifeRegen;
		this.manaRegen = other.manaRegen;
		this.staminaRegen = other.staminaRegen;
		this.punchAffectingAbilities = other.punchAffectingAbilities;
		this.runAccel = other.runAccel;
		this.runSpeed = other.runSpeed;
		this.flyDirection = other.flyDirection;
		this.flySpeed = other.flySpeed;
		this.abilities = other.abilities;
		this.inventory = other.inventory;
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
		this.rotation = other.rotation;
		this.xVel = other.xVel;
		this.yVel = other.yVel;
		this.zVel = other.zVel;
		this.animState = other.animState;
		this.animFrame = other.animFrame;
		this.life = other.life;
		this.stamina = other.stamina;
		this.mana = other.mana;
		this.charge = other.charge;
		this.maxLife = other.maxLife;
		this.maxMana = other.maxMana;
		this.maxStamina = other.maxStamina;
		this.ghostMode = other.ghostMode;
		this.panic = other.panic;
		this.prone = other.prone;
		this.dead = other.dead;
		this.slippedTimeLeft = other.slippedTimeLeft;
		this.directionOfAttemptedMovement = other.directionOfAttemptedMovement;
		this.strengthOfAttemptedMovement = other.strengthOfAttemptedMovement;
		this.timeEffect = other.timeEffect;
		this.timeEffect = other.timeEffect;
		this.startStopPossession = other.startStopPossession;
		this.possessedTimeLeft = other.possessedTimeLeft;
		this.possessingControllerID = other.possessingControllerID;
		this.possessionTargetID = other.possessionTargetID;
		this.possessionVessel = other.possessionVessel;
		this.isAvatar = other.isAvatar;
		this.effects = other.effects;
		this.name = other.name;
		this.hair = other.hair;
		this.head = other.head;
		this.chest = other.chest;
		this.nakedChest = other.nakedChest;
		this.legs = other.legs;
		this.nakedLegs = other.nakedLegs;
		this.initAnimation();
	}

	/**
	 * Unequips armor and equips armor2.
	 * 
	 * @param armor2
	 */
	public void putArmor(Armor armor2)
	{
		armor.equipped = false;
		armor = armor2;
		armor.equipped = true;
	}

	/**
	 * Changes a stat according to an index.
	 * <p>
	 * 0 = STR, 1 = FIT, 2 = DEX, 3 = WIT, 4 = KNOW, 5 = SOC
	 * 
	 * @param statNum
	 * @param change
	 *            amount to be added to the stat
	 */
	public void changeStat(int statNum, int change)
	{
		switch (statNum)
		{
		case 0:
			STRENGTH += change;
			return;
		case 1:
			FITNESS += change;
			return;
		case 2:
			DEXTERITY += change;
			return;
		case 3:
			WITS += change;
			return;
		case 4:
			KNOWLEDGE += change;
			return;
		case 5:
			SOCIAL += change;
			return;
		default:
			MAIN.errorMessage("Hey mister. Somebody told me you wanna check out my scene?");
			return;
		}
	}
}
