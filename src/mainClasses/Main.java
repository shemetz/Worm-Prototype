package mainClasses;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.Timer;

import mainClasses.abilities.Sense_Powers;
import mainResourcesPackage.SoundEffect;

public class Main extends Frame implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, WindowFocusListener
{
	// Current version of the program
	private static final long	serialVersionUID		= 1;

	// TAU
	final double				TAU						= 2 * Math.PI;

	// CONSTANTS
	final boolean				movementVariation		= true;																											// if true = player is slower when walking sideways and backwards
	int							frameWidth				= 1280,
										frameHeight = 800;
	Timer						frameTimer;																																// Each frame of this timer redraws the frame
	int							frameTimerDelay			= 20;																											// Every 20 milliseconds the frameTimer will do its stuff. =50 FPS
	final static double			heightZoomRatio			= 0.01;
	final int					squareSize				= 96;
	// 1 pixel = 1 centimeter. 1 grid tile = 1 meter. Sadly, that also means that in this world, 1 meter = 96 centimeters. Oh well.
	final double				globalDeltaTime			= (double) frameTimerDelay / 1000;
	final double				gravity					= 9.8;
	final double				someConstant			= 0.03;
	final double				standingFrictionBenefit	= 2.2;
	final double				ghostFrictionMultiplier	= 0.7;
	final double				sqrt2					= Math.sqrt(2);
	final double				sqrt2by2				= sqrt2 / 2;
	final double				cameraSmoothing			= 2.5;
	final BasicStroke			dashedStroke3			= new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]
															{ 10.0f }, 0.0f);
	final String[]				hotkeyStrings			= new String[]
															// Right-Click, Shift, Q, E, R, F, V, C, X, Z
															{ "M-Click", "  Shift  ", "     Q", "     E", "     R", "     F", "     V", "     C", "     X", "     Z" };
	FontRenderContext			frc;
	Font						tooltipFont				= new Font("Serif", Font.PLAIN, 12);
	DateFormat					dateFormat				= new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	static Random				random					= new Random();

	// CAMERA AND MOUSE STUFF
	PointerInfo					pin;																																	// Don't use this
	Point						mousePoint				= new Point();																									// Don't use that
	int							screenmx				= 0;																											// Mouse X coordinate relative to FRAME
	int							screenmy				= 0;																											//
	int							mx						= 0;																											// Mouse X coordinate relative to in-game world
	int							my						= 0;

	Point3D						camera					= new Point3D(0, 0, 25);
	double						zoomLevel				= 1;
	double						UIzoomLevel				= 1;
	double						cameraRotation			= 0;
	double						cameraHeight			= 25;

	// Double Buffering (a.k.a stay away from this)
	int							bufferWidth;
	int							bufferHeight;
	Image						bufferImage;
	Graphics					bufferGraphics;

	// Variables, lists
	Environment					env;
	Player						player;
	int							frameNum				= 0;
	boolean						stopUsingPower			= false;
	double						timeSinceLastScreenshot	= 2;
	Image						lastScreenshot			= null;
	public final static int		numOfElements			= 33;

	// Visual graphical variables
	Point						tooltipPoint			= new Point(-1, -1);
	String						tooltip					= "";
	int							hotkeyHovered			= -1;
	int							hotkeySelected			= -1;

	Line2D						drawLine				= null;																											// temp
	Rectangle2D					drawRect				= null;																											// also temp

	// Pause (tab)
	boolean						tab						= false,
										tab2 = true;
	int							tabHoverAbility			= -1;																											// ability player is hovering above which, with the mouse

	// METHODS

	void maintainAbility(Ability ability, Person user, Point target, double deltaTime)
	{
		double angle = Math.atan2(target.y - user.y, target.x - user.x);
		switch (ability.justName())
		{
		case "Beam":
			break;
		case "Flight I":
		case "Flight II":
			user.stamina -= deltaTime * ability.costPerSecond;
		case "Telekinetic Flight":
			if (ability.cooldownLeft > 0)
				ability.cooldownLeft -= deltaTime;
			break;
		case "Ball":
			if (ability.cooldownLeft == 0)
				if (user.mana >= ability.cost)
				{
					angle = angle + user.missAngle * (2 * random.nextDouble() - 1);
					ability.cooldownLeft = ability.cooldown;
					Ball b = new Ball(ability.getElementNum(), ability.points, angle);
					b.x = user.x + ability.range * Math.cos(angle);
					b.y = user.y + ability.range * Math.sin(angle);
					b.z = user.z + 0.9;
					b.xVel += user.xVel;
					b.yVel += user.yVel;

					// TODO move this to a new method in Environment?
					boolean ballCreationSuccess = true;

					Point ballCenter = new Point((int) b.x, (int) b.y);
					// pow2 to avoid using Math.sqrt(), which is supposedly computationally expensive.
					double ballRadiusPow2 = Math.pow(b.radius, 2);
					// test force field collision
					for (ForceField ff : env.FFs)
						if (ff.x - 0.5 * ff.length <= b.x + b.radius && ff.x + 0.5 * ff.length >= b.x - b.radius && ff.y - 0.5 * ff.length <= b.y + b.radius
								&& ff.y + 0.5 * ff.length >= b.y - b.radius)
							if ((0 <= Methods.realDotProduct(ff.p[0], ballCenter, ff.p[1]) && Methods.realDotProduct(ff.p[0], ballCenter, ff.p[1]) <= ff.width * ff.width
									&& 0 <= Methods.realDotProduct(ff.p[0], ballCenter, ff.p[3]) && Methods.realDotProduct(ff.p[0], ballCenter, ff.p[3]) <= ff.length * ff.length)
									|| Methods.LineToPointDistancePow2(ff.p[0], ff.p[1], ballCenter) < ballRadiusPow2 || Methods.LineToPointDistancePow2(ff.p[2], ff.p[3], ballCenter) < ballRadiusPow2
									|| Methods.LineToPointDistancePow2(ff.p[1], ff.p[2], ballCenter) < ballRadiusPow2 || Methods.LineToPointDistancePow2(ff.p[3], ff.p[0], ballCenter) < ballRadiusPow2)
							{
								ballCreationSuccess = false;
								// damage FF
								double damage = (b.getDamage() + b.getPushback()) * 0.5; // half damage, because the ball "bounces"
								env.damageFF(ff, damage, ballCenter);
							}
					if (ballCreationSuccess)
						env.balls.add(b);
					else
						env.ballDebris(b, "shatter");
					user.mana -= ability.cost;
					user.rotate(angle, deltaTime);
				}
			break;
		case "Pool":
			if (ability.cooldownLeft <= 0 || env.poolHealths[(int) ability.targetEffect1][(int) ability.targetEffect2] <= 0)
				ability.use(env, user, target);
			else
			{
				// effects
				env.otherDebris((ability.targetEffect1 + 0.5) * squareSize, (ability.targetEffect2 + 0.5) * squareSize, ability.getElementNum(), "pool heal", frameNum);
				env.poolHealths[(int) ability.targetEffect1][(int) ability.targetEffect2] += ability.points;
				if (env.poolHealths[(int) ability.targetEffect1][(int) ability.targetEffect2] >= 100)
					env.poolHealths[(int) ability.targetEffect1][(int) ability.targetEffect2] = 100;
				// Might be resource-costly:
				env.updatePools();
				//
				user.mana -= ability.costPerSecond * deltaTime;
			}
			break;
		case "Wall":
			if (ability.cooldownLeft <= 0 || env.wallHealths[(int) ability.targetEffect1][(int) ability.targetEffect2] <= 0)
				ability.use(env, user, target);
			else
			{
				// effects
				env.otherDebris((ability.targetEffect1 + 0.5) * squareSize, (ability.targetEffect2 + 0.5) * squareSize, ability.getElementNum(), "wall heal", frameNum);
				env.wallHealths[(int) ability.targetEffect1][(int) ability.targetEffect2] = (int) Math.max(Math.max((1 - ability.cooldownLeft / ability.cooldown) * 100, 1),
						env.wallHealths[(int) ability.targetEffect1][(int) ability.targetEffect2]); // make sure you aren't decreasing current health
				user.mana -= ability.costPerSecond * deltaTime;
			}
			break;
		case "Ghost Mode I":
			if (ability.timeLeft <= 0)
			{
				if (!user.insideWall)
				{
					user.panic = false;
					ability.use(env, user, target);
				} else
				{
					user.mana -= 1.5 * deltaTime; // punish
					env.hitPerson(user, 15 * deltaTime, 0, 0, 9, deltaTime); // punish
					user.stamina -= 1.5 * deltaTime; // punish
					ability.timeLeft = 0;
					user.panic = true;
				}

			} else
				ability.timeLeft -= deltaTime;
			if (ability.cooldownLeft > 0)
				ability.cooldownLeft -= deltaTime;
			break;
		case "Shield":
			if (user.mana < ability.costPerSecond)
				ability.use(env, user, target);
			else
			{
				user.mana -= ability.costPerSecond * deltaTime;
				for (ArcForceField a : env.arcFFs)
					if (a.target.equals(user))
					{
						a.rotation = user.rotation;
						if (a.extraLife > 0)
							user.mana -= ability.cost * deltaTime;
					}
			}
			break;
		default:
			errorMessage("couldn't find maintain-ability code for " + ability.justName());
			break;
		}
	}

	void frame()
	{
		double deltaTime = globalDeltaTime;
		// Remember: 20 milliseconds between frames, 50 frames per second
		// Resetting the sounds.
		// SOUNDS EFFECTS (1)
		List<SoundEffect> allSounds = new ArrayList<SoundEffect>();
		for (Person p : env.people)
		{
			for (SoundEffect s : p.sounds)
				allSounds.add(s);
			for (Ability a : p.abilities)
				for (SoundEffect s : a.sounds)
					allSounds.add(s);
		}
		// FORCE FIELDS
		for (ForceField ff : env.FFs)
			for (SoundEffect s : ff.sounds)
				allSounds.add(s);
		// TODO make the above lines only happen once, and make allSounds part of Main, and also update it whenever adding abilities/people/forcefields
		// SOUND EFFECTS (2)
		for (SoundEffect s : allSounds)
		{
			s.justActivated = false;
			if (s.active)
				s.stopIfEnded();
		}
		// DEBRIS
		for (int i = 0; i < env.debris.size(); i++)
		{
			Debris d = env.debris.get(i);
			d.update(deltaTime);
			if (d.velocity <= 35)
			{
				env.debris.remove(i);
				i--;
			}
		}
		// CLOUDS
		for (Cloud c : env.clouds)
		{
			// wind pushes clouds
			double speed = c.z;
			c.x += env.windDirection.x * deltaTime * speed * 0.2;
			c.y += env.windDirection.y * deltaTime * speed * 0.2;
		}
		// change wind direction
		if (frameNum % 200 == 0 && random.nextDouble() < 0.3)
			env.windDirection = new Point(Main.random.nextInt(11) - 5, Main.random.nextInt(11) - 5);

		// UI TEXTS
		for (int i = 0; i < env.uitexts.size(); i++)
		{
			UIText ui = env.uitexts.get(i);
			// UItexts rise
			ui.y -= 1;
			// UItexts disappear slowly
			ui.transparency -= 3;
			if (ui.transparency < 0)
			{
				env.uitexts.remove(i);
				i--;
			}
		}
		checkPlayerMovementKeys();
		// VINES
		for (int i = 0; i < env.vines.size(); i++)
		{
			Vine v = env.vines.get(i);
			env.moveVine(v, deltaTime);
			v.fixPosition();
			if (v.length < v.startDistance) // vine retracted and didn't hit anyone
			{
				for (Ability a : v.creator.abilities)
					if (a.name.equals("Beam <Plant>"))
						a.use(env, v.creator, v.creator.target);
			}
		}
		// BEAMS
		for (int i = 0; i < env.beams.size(); i++)
		{
			Beam b = env.beams.get(i);
			env.moveBeam(b, !b.isChild, deltaTime);
			if (frameNum % 15 == 0)
				b.frameNum++;
			if (b.frameNum >= 4)
				b.frameNum = 0;
			b.timeLeft -= deltaTime;

			if (b.timeLeft <= 0)
			{
				env.beams.remove(i);
				i--;
			}
		} // BUG - Reflecting a beam will cause increased pushback due to something related to the order of stuff in a frame. Maybe friction?
			// targeting
		updateTargeting(deltaTime);
		//PEOPLE
		for (Person p : env.people)
		{
			if (p.getClass().equals(NPC.class))
				frameAIupdate((NPC) p, deltaTime);

			// maintaining person abilities
			for (Ability a : p.abilities)
				if (a.on && a.cost != -1)
					a.maintain(env, p, p.target, deltaTime);
			// using abilities the person is trying to repetitively use (e.g. holding down the Punch ability's key)
			if (p.abilityTryingToRepetitivelyUse != -1)
			{
				p.inCombat = true; // TODO
				p.abilities.get(p.abilityTryingToRepetitivelyUse).use(env, p, p.target);
			}

			p.selfFrame(deltaTime);
			for (Effect e : p.effects)
				e.nextFrame(frameNum);
			// movement
			double floorFriction = applyGravityAndFrictionAndReturnFriction(p, deltaTime);
			checkMovementAttempt(p, floorFriction, deltaTime);
			movePerson(p, deltaTime);
			// Animation
			p.nextFrame(frameNum);
			for (int i = 0; i < p.uitexts.size(); i++)
			{
				UIText ui = p.uitexts.get(i);
				ui.y -= 1;
				ui.transparency -= 3;
				if (ui.transparency < 0)
				{
					p.uitexts.remove(i);
					i--;
				}
			}
			// damage because of standing on pools
			if (p.z < 0.1)
			{
				int type = env.poolTypes[(int) ((p.x) / squareSize)][(int) ((p.y) / squareSize)];
				if (type != -1)
				{
					// also damage the pool the person is standing on. Standing on a full-health pool deals it 10 damage per second (out of 100)
					if (frameNum % 5 == 0) // ten times per second, deal 1 damage
						env.poolHealths[(int) ((p.x) / squareSize)][(int) ((p.y) / squareSize)] -= 1;
					switch (type)
					{
					case 1: // water
					case 5: // ice
					case 9: // flesh (blood pool)
						if (frameNum % 50 == 0)
						{
							p.affect(new Effect("Burning", -1), false); // stop burning
							double slipChance = -0.01;
							if (p.xVel * p.xVel + p.yVel * p.yVel > 40000)
								slipChance += 0.2;
							if (p.xVel * p.xVel + p.yVel * p.yVel > 70000)
								slipChance += 0.1;
							if (p.xVel * p.xVel + p.yVel * p.yVel > 150000)
								slipChance += 0.1;
							if (p.xVel * p.xVel + p.yVel * p.yVel > 230000)
								slipChance += 0.1;
							if (random.nextDouble() < slipChance && !p.prone) // slip chance is 30% in water and ice and blood
							{
								p.prone = true;
								p.slippedTimeLeft = 3; // If you change this, change stuff in Person.selfFrame() and Player.nextFrame()
							}
						}
						break;
					case 7: // acid
						env.hitPerson(p, 25, 0, 0, 3, deltaTime); // acid damage
						break;
					case 8: // lava
						env.hitPerson(p, 20, 0, 0, 2, deltaTime); // burn damage
						if (frameNum % 50 == 0 && random.nextDouble() < 0.7) // burn chance is 70% in lava
							p.affect(new Effect("Burning", 5), true);
						break;
					case 10: // earth spikes
						// TEMP spikes deal 30 damage per second
						env.hitPerson(p, 25, 0, 0, 1, deltaTime);
						break;
					default:
						errorMessage("Unknown pool type: " + type);
						break;
					}
				}
			}
			// once per second, damage for burn and test for extinguishing fire
			if (frameNum % 50 == 0)
			{
				for (int i = 0; i < p.effects.size(); i++)
				{
					Effect e = p.effects.get(i);
					if (e.name.equals("Burning"))
					{
						env.hitPerson(p, e.strength, 0, 0, 2);
						if (random.nextDouble() < 0.25) // 25% chance to stop burning, per second
							p.affect(e, false);
					}
				}
			}
		}

		// BALLS
		for (int i = 0; i < env.balls.size(); i++)
		{
			Ball b = env.balls.get(i);
			// gravity
			b.zVel -= 0.001 * gravity * deltaTime;

			b.rotation += b.angularVelocity * deltaTime;
			if (b.xVel == 0 || b.yVel == 0 || b.mass <= 0 || !moveBall(b, deltaTime)) // ball was destroyed, or ball stopped
			{
				env.balls.remove(i);
				i--;
			}
		}
		// to sort for ball-ball collisions:
		for (int i = 0; i < env.balls.size(); i++)
		{
			Ball b = env.balls.get(i);
			if (b.xVel == 0 || b.yVel == 0 || b.mass <= 0)
			{
				env.balls.remove(i);
				i--;
			}
		}
		// ARC FORCE FIELDS
		for (int i = 0; i < env.arcFFs.size(); i++)
		{
			ArcForceField aff = env.arcFFs.get(i);
			aff.update(deltaTime);
			if (frameNum % 10 == 0) // check for next AFF frame 5 times per second, because the check includes drawing an image
			{
				// Currently force fields change images after lower than (e.g.) 75 health, not 75% health!
				int frame = 0;
				if (aff.life >= 75)
					frame = 0;
				else if (aff.life >= 50)
					frame = 1;
				else if (aff.life >= 15)
					frame = 2;
				else
					frame = 3;
				aff.changeImage(Resources.arcForceFields[aff.elementNum][frame]);
			}
			if (aff.life <= 0)
			{
				for (Person p : env.people)
					if (p.equals(aff.target))
						for (Ability a : p.abilities)
							if (a.justName().equals("Shield"))
								a.use(env, p, p.target); // that method will remove the arc force field.
				i--;
			}
		}
		// FORCE FIELDS
		for (int i = 0; i < env.FFs.size(); i++)
		{
			ForceField ff = env.FFs.get(i);
			// Force Shield decay
			updateFF(ff, deltaTime);
			if (ff.life <= 0)
			{
				ff.stopAllSounds();
				env.FFs.remove(i);
				i--;
			}
		}
		// WALLS & POOLS
		for (int x = 0; x < env.width; x++)
			for (int y = 0; y < env.height; y++)
			{
				if (env.wallTypes[x][y] != -1)
					if (env.wallHealths[x][y] <= 0)
						env.remove(x, y);
				if (env.poolTypes[x][y] != -1)
				{
					if (env.poolHealths[x][y] <= 0)
						env.remove(x, y);
					if (frameNum % 25 == 0) // pools decay, 50 seconds = 100 damage
						env.poolHealths[x][y] -= 1;
				}
				// possible TODO: flow from pools to other pools
			}

		// Updating pool transparencies due to damage, and spreading the damage around evenly
		if (frameNum % 10 == 0)
			env.updatePools();

		// camera movement
		int diffX = (int) ((player.x - camera.x) * deltaTime * cameraSmoothing * zoomLevel);
		int diffY = (int) ((player.y - camera.y) * deltaTime * cameraSmoothing * zoomLevel);
		int diffZ = (int) ((player.z + cameraHeight - camera.z) * zoomLevel);
		camera.x += diffX;
		camera.y += diffY;
		camera.z += diffZ;
		updateMousePosition();

		frameNum++;

		for (int i = 0; i < env.effects.size(); i++)
		{
			VisualEffect eff = env.effects.get(i);
			eff.update(frameNum);
			eff.timeLeft -= deltaTime;
			if (eff.timeLeft <= 0)
			{
				env.effects.remove(i);
				i--;
			}
		}

		// Stopping sounds that should stop looping
		for (SoundEffect s : allSounds)
		{
			if (!s.justActivated && s.active)
				switch (s.type)
				{
				case "Beam":
				case "Reflect":
				case "Scorched":
					s.stop();
					break;
				default:
					break;
				}
		}

		if (timeSinceLastScreenshot <= 2)
			timeSinceLastScreenshot += deltaTime;
		else
			lastScreenshot = null;

		keyPressFixingMethod(-1, true);
	}

	void updateFF(ForceField ff, double deltaTime)
	{
		switch (ff.type)
		{
		case 0: // Force Shield. Life decreased by 50% of current life + 1 every second.
			ff.life -= deltaTime * (ff.life / 2 + 1); // 5 seconds for weakest Force Shield, 10 seconds for strongest.
			break;
		case 1: // Strong Force Field
			ff.life -= deltaTime * (ff.life * 0.05 + 1);
			break;
		default:
			errorMessage("They don't think it be like it is, but it do.");
			break;
		}
	}

	void frameAIupdate(NPC p, double deltaTime)
	{
		// choose target
		if (frameNum % 25 == 0) // don't check a lot of times in a short period
		{
			p.targetID = -1;
			switch (p.tactic)
			{
			case "retreat":
			case "circle strafing":
			case "punch chasing":
				// Choose as target the closest enemy.
				double shortestDistanceToTargetPow2 = Double.MAX_VALUE;
				for (Person p2 : env.people)
					if (p.viableTarget(p2))
						if (Methods.DistancePow2(p.x, p.y, p2.x, p2.y) < shortestDistanceToTargetPow2)
						{
							shortestDistanceToTargetPow2 = Methods.DistancePow2(p.x, p.y, p2.x, p2.y);
							p.targetID = p2.id;
						}
				break;
			default:

				break;
			}
		}
		Person target = null;
		if (p.targetID != -1 && p.tactic != "no target")
			for (Person p2 : env.people)
				if (p2.id == p.targetID)
				{
					target = p2;
					break;
				}
		if (target != null)
		{
			double angleToTarget = Math.atan2(target.y - p.y, target.x - p.x);
			double distanceToTargetPow2 = Methods.DistancePow2(p.x, p.y, target.x, target.y);
			// target-type tactics
			switch (p.tactic)
			{
			case "punch chasing":
				// move towards target and punch them.
				p.rotate(angleToTarget, deltaTime);
				p.directionOfAttemptedMovement = angleToTarget;
				p.strengthOfAttemptedMovement = 1;
				p.target = new Point((int) target.x, (int) target.y);
				Ability punch = null;
				int index = -1;
				for (int i = 0; i < p.abilities.size(); i++)
					if (p.abilities.get(i).name.equals("Punch"))
					{
						index = i;
						punch = p.abilities.get(index);
					}
				if (punch != null)
					if (distanceToTargetPow2 < punch.range * punch.range)
						pressAbilityKey(index, true, p);
					else if (punch.cooldownLeft <= 0)
						pressAbilityKey(index, false, p);
				break;
			case "circle strafing":
				// move around target. Also, get close to it or away from it to get into the "circle strafing" range.
				p.rotate(angleToTarget, deltaTime);

				// Fucking fuck shit code is not fucking working and I have no bloody idea what's wrong with it and I rewrote it to something that looks different but does the same and guess what? yes the bug is still happening. gahhhhhhhhhhhhh,
				// hopefully one day I will figure out what's wrong here.

				// double diffAngleFromTarget = Math.asin(7097.42205 * globalDeltaTime * 0.5 / Math.sqrt(distanceToTargetPow2)); // KILL ME
				// double newAngle = -Math.PI + angleToTarget + diffAngleFromTarget * (p.rightOrLeft ? 1 : -1);
				// double newX = target.x + dist * Math.cos(newAngle);
				// double newY = target.y + dist * Math.sin(newAngle);
				// p.directionOfAttemptedMovement = Math.atan2(newY - p.y, newX - p.x);
				// p.strengthOfAttemptedMovement = 1;
				// if (random.nextDouble() < 0.01) // chance of switching direction mid-circle
				// p.rightOrLeft = !p.rightOrLeft;
				double deviationAngle = Math.acos(3062.332465 * deltaTime * 0.5 / Math.sqrt(distanceToTargetPow2)); // Haha I'm just throwing random numbers that make it stop growing
				if (Double.isNaN(deviationAngle))
				{
					// ughhhhhhhhh
					deviationAngle = Math.PI / 1;
				}

				p.directionOfAttemptedMovement = angleToTarget + deviationAngle * (p.rightOrLeft ? 1 : -1); // BUG - distance grows between p and target for some reason!!!! TODO
				p.strengthOfAttemptedMovement = 1;

				if (p.justGotHit || p.justCollided || random.nextDouble() < 0.005) // chance of switching direction mid-circle
					p.rightOrLeft = !p.rightOrLeft;

				// moving away or into range
				// range is ALWAYS between 250 and 500 cm, because....because I said so
				// TODO ....yeah...
				if (distanceToTargetPow2 < 250 * 250)
					p.directionOfAttemptedMovement = Methods.meanAngle(angleToTarget + Math.PI, p.directionOfAttemptedMovement);
				if (distanceToTargetPow2 > 500 * 500)
					p.directionOfAttemptedMovement = Methods.meanAngle(angleToTarget, p.directionOfAttemptedMovement);

				// Attacking
				for (int aIndex = 0; aIndex < p.abilities.size(); aIndex++)
				{
					Ability a = p.abilities.get(aIndex);
					if (a.hasTag("projectile")) // ball
						if (a.justName().equals("Ball"))
						{
							// aim the ball the right direction, taking into account the velocity addition caused by the person moving
							double v = Ball.giveVelocity(a.getElementNum(), a.points);
							double xv = v * Math.cos(angleToTarget);
							double yv = v * Math.sin(angleToTarget);
							xv -= p.xVel;
							yv -= p.yVel;
							p.target = new Point((int) (p.x + xv), (int) (p.y + yv));
							pressAbilityKey(aIndex, true, p);
						}
					if (a.hasTag("beam")) // beam
						if (a.justName().equals("Beam"))
						{
							// aims the beam exactly at the target, so will miss often
							p.target = new Point((int) (p.x), (int) (p.y));
							pressAbilityKey(aIndex, true, p);
						}
				}
				break;
			case "retreat":
				// Back away from any enemy nearby when low on health
				p.strengthOfAttemptedMovement = 0; // stop if there's nobody to retreat from
				double shortestSquaredDistance = 400000; // minimum distance to keep from enemies. About 7 tiles
				if (distanceToTargetPow2 < shortestSquaredDistance)
				{
					p.directionOfAttemptedMovement = angleToTarget + Math.PI; // away from target
					p.rotate(p.directionOfAttemptedMovement, deltaTime);
					p.strengthOfAttemptedMovement = 1;
				}
				break;
			default:
				errorMessage("6j93k, no target-tactic - " + p.tactic);
				break;
			}
		} else // no-target tactics
			switch (p.tactic)
			{
			case "panic":
				// run around aimlessly
				if (frameNum % 40 == 0)
					p.directionOfAttemptedMovement = p.rotation - 0.5 * Math.PI + random.nextDouble() * Math.PI; // random direction in 180 degree arc
				p.rotate(p.directionOfAttemptedMovement, deltaTime);
				p.strengthOfAttemptedMovement = 1;
				break;
			case "circle strafing":
			case "punch chasing":
				// waiting for tactic-switching
				break;
			case "no target":
				// don't move
				p.strengthOfAttemptedMovement = 0;
				// try to switch tactics

				if (p.strategy.equals("aggressive"))
				{
					// Choose as target the closest enemy.
					double shortestDistanceToTargetPow2 = Double.MAX_VALUE;
					for (Person p2 : env.people)
						if (p.viableTarget(p2))
							if (Methods.DistancePow2(p.x, p.y, p2.x, p2.y) < shortestDistanceToTargetPow2)
							{
								shortestDistanceToTargetPow2 = Methods.DistancePow2(p.x, p.y, p2.x, p2.y);
								p.targetID = p2.id; // not necessary?
								target = p2;
							}
					if (target != null)
						if (Methods.DistancePow2(p.x, p.y, target.x, target.y) < 600 * 600) // 600 sounds like an OK number
							if (p.mana > 0.4 * p.maxMana)
								p.tactic = "circle strafing";
							else if (p.stamina > 0.1 * p.maxStamina)
								p.tactic = "punch chasing";
							else
								p.tactic = "retreat";
				}
				break;

			default:
				errorMessage("shpontzilontz. no no-target-tactic - " + p.tactic);
				break;
			}

		// tactic-switching. TODO make sense?
		String prevTactic = "" + p.tactic; // copy
		if (p.panic)
			p.tactic = "panic";
		else if (p.life < 0.15 * p.maxLife)
			p.tactic = "retreat";
		else if (p.tactic.equals("retreat")) // stop retreating when uninjured
			p.tactic = "no target";
		else if (target == null)
			p.tactic = "no target";
		else if (p.strategy.equals("aggressive"))
		{
			if (p.tactic.equals("circle strafing") && p.mana <= 0.1 * p.maxMana)
				p.tactic = "punch chasing";
			else if (p.tactic.equals("punch chasing") && p.mana >= 0.9 * p.maxMana)
				p.tactic = "circle strafing";
		}
		if (!prevTactic.equals(p.tactic))
		{
			if (p.abilityMaintaining != -1)
				pressAbilityKey(p.abilityMaintaining, false, p);
			p.abilityMaintaining = -1;
			p.abilityAiming = -1;
			p.abilityTryingToRepetitivelyUse = -1;
		}
		// resetting check-booleans
		p.justCollided = false;
		p.justGotHit = false;

		if (Double.isNaN(p.directionOfAttemptedMovement))
		{
			p.directionOfAttemptedMovement = 0;
			p.strengthOfAttemptedMovement = 0;
		}

	}

	void tabFrame()
	{
		// TODO
	}

	void updateTargeting(double deltaTime) // for the player
	{
		player.target = new Point(mx, my);
		double angle = Math.atan2(player.target.y - player.y, player.target.x - player.x);
		Ability ability;
		if (player.abilityAiming != -1)
			ability = player.abilities.get(player.abilityAiming);
		else if (player.abilityTryingToRepetitivelyUse != -1)
			ability = player.abilities.get(player.abilityTryingToRepetitivelyUse);
		else if (player.abilityMaintaining != -1 && player.maintaining)
			ability = player.abilities.get(player.abilityMaintaining);
		else
		{
			player.target = new Point(-1, -1);
			player.targetType = "";
			player.successfulTarget = false;
			return;
		}
		// if the area isn't nice
		switch (ability.rangeType)
		{
		case "Create in grid":
			break;
		default:
			player.rangeArea = null;
			break;
		}
		// clamp target to range:
		int comeOnProgramWorkWithMeHere = 0;
		if (player.rangeArea != null && !player.rangeArea.isEmpty())
			while (!player.rangeArea.contains(player.target.x, player.target.y))
			{
				player.target.x = (int) (player.x + Math.cos(angle) * (ability.range - comeOnProgramWorkWithMeHere));
				player.target.y = (int) (player.y + Math.sin(angle) * (ability.range - comeOnProgramWorkWithMeHere));
				comeOnProgramWorkWithMeHere += 10;
			}
		else if (ability.range != -1)
			if (Methods.DistancePow2(player.x, player.y, player.target.x, player.target.y) > ability.range * ability.range)
			{
				player.target.x = (int) (player.x + Math.cos(angle) * ability.range);
				player.target.y = (int) (player.y + Math.sin(angle) * ability.range);
			}

		ability.updatePlayerTargeting(env, player, player.target, deltaTime);
	}

	void drawRange(Graphics2D buffer, Ability ability)
	{

		// else - is written in the case
		switch (ability.rangeType)
		{
		case "Ranged circular area":
			buffer.setStroke(dashedStroke3);
			buffer.setColor(new Color(255, 255, 255, 80));
			// range
			buffer.drawOval((int) (player.x - ability.range), (int) (player.y - ability.range), 2 * ability.range, 2 * ability.range);
			break;
		case "Create in grid":
			buffer.setStroke(dashedStroke3);
			if (player.abilityAiming == -1 || player.abilityMaintaining == player.hotkeys[hotkeySelected])
			{
				if (ability.targetEffect3 == 0)
					buffer.setColor(Color.green);
				else
					buffer.setColor(Color.red);
				buffer.drawRect(player.target.x - squareSize / 2, player.target.y - squareSize / 2, squareSize, squareSize);
			}
			Area rangeArea = new Area();
			for (int i = (int) (player.x - ability.range); i < (int) (player.x + ability.range); i += squareSize)
				for (int j = (int) (player.y - ability.range); j < (int) (player.y + ability.range); j += squareSize)
					if (Math.pow(player.x - i / squareSize * squareSize - 0.5 * squareSize, 2) + Math.pow(player.y - j / squareSize * squareSize - 0.5 * squareSize, 2) <= ability.range
							* ability.range)
						rangeArea.add(new Area(new Rectangle2D.Double(i / squareSize * squareSize, j / squareSize * squareSize, squareSize, squareSize)));
			buffer.setColor(new Color(255, 255, 255, 80)); // stroke is still dashed
			buffer.draw(rangeArea);
			break;
		case "Exact range":
			buffer.setColor(new Color(255, 255, 255, 80)); // transparent white
			buffer.setStroke(dashedStroke3);
			buffer.drawOval((int) (player.x - ability.range), (int) (player.y - ability.range), 2 * ability.range, 2 * ability.range);
			break;
		case "Circle area":
			// "filled" area, not just outlines.
			buffer.setStroke(new BasicStroke(1));
			buffer.setColor(new Color(182, 255, 0));
			Shape thing = new Ellipse2D.Double(player.x - ability.range, player.y - ability.range, 2 * ability.range, 2 * ability.range);
			buffer.setClip(thing);
			for (int x = (int) (player.x - ability.range) / 18 * 18; x < (int) (player.x + ability.range + 18) / 18 * 18; x += 18)
				buffer.drawLine(x, (int) (player.y - ability.range), x, (int) (player.y + ability.range));
			for (int y = (int) (player.y - ability.range) / 18 * 18; y < (int) (player.y + ability.range + 18) / 18 * 18; y += 18)
				buffer.drawLine((int) (player.x - ability.range), y, (int) (player.x + ability.range), y);
			buffer.setClip(null);
			buffer.drawOval((int) (player.x - ability.range), (int) (player.y - ability.range), ability.range * 2, ability.range * 2);

			// more resource-intensive method ahead, that does the exact same thing :)

			// // NOTE: This uses TexturePaint, and will always look slightly or very weird. Worth it though.
			// BufferedImage image = new BufferedImage(2 * ability.range + 18, 2 * ability.range + 18, BufferedImage.TYPE_INT_ARGB);
			// Graphics2D shreodinger = image.createGraphics(); // name is irrelevant
			// shreodinger.setPaint(new TexturePaint(Resources.range_net, new Rectangle(0, 0, 90, 90)));
			// shreodinger.fillOval(0 + (int) (player.x) % 18, 0 + (int) (player.y) % 18, 2 * ability.range, 2 * ability.range);
			// shreodinger.setColor(new Color(182, 255, 0)); // greenish
			// shreodinger.setStroke(new BasicStroke(1));
			// shreodinger.drawOval(0 + (int) (player.x) % 18, 0 + (int) (player.y) % 18, 2 * ability.range, 2 * ability.range);
			// shreodinger.dispose();
			// buffer.drawImage(image, (int) (player.x - ability.range) - (int) (player.x) % 18, (int) (player.y - ability.range) - (int) (player.y) % 18, null);
			// // You must be wondering why I did this wacky hijink instead ofsimply drawing the ovals with buffer. Well, apparently the TexturePaint causes the process to be very slow when the camera is zoomed in (and buffer's scale is very big).

			break;
		case "Look":
		case "":
		default:
			break;
		}
	}

	void drawAim(Graphics2D buffer, Player p)
	{
		Ability ability = player.abilities.get(player.abilityAiming);

		switch (p.targetType)
		{
		case "explosion":
			buffer.setStroke(dashedStroke3);
			buffer.setColor(Color.orange);
			// explosion "plus"
			buffer.drawLine(player.target.x - (int) (0.1 * ability.areaRadius), player.target.y, player.target.x + (int) (0.1 * ability.areaRadius), player.target.y);
			buffer.drawLine(player.target.x, player.target.y - (int) (0.1 * ability.areaRadius), player.target.x, player.target.y + (int) (0.1 * ability.areaRadius));
			// explosion circles
			int circleRadius = (int) (ability.areaRadius);
			while (circleRadius >= 4)
			{
				buffer.setColor(new Color(255, 192, 0, (int) (64 + 191 * circleRadius / ability.areaRadius)));
				buffer.drawOval(player.target.x - (int) (ability.areaRadius - circleRadius), player.target.y - (int) (ability.areaRadius - circleRadius), (int) (ability.areaRadius - circleRadius) * 2,
						(int) (ability.areaRadius - circleRadius) * 2);
				circleRadius /= 2;
			}
			break;
		case "teleport":
			buffer.setStroke(new BasicStroke(3));
			final int radius = 35;
			if (player.successfulTarget)
			{
				buffer.setColor(new Color(53, 230, 240));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect1)), player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect1)),
						player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect1 + Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect1 + Math.PI * 2 / 3)));
				buffer.setColor(new Color(40, 210, 250));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect2)), player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect2)),
						player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect2 + Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect2 + Math.PI * 2 / 3)));
				buffer.setColor(new Color(20, 200, 255));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect3)), player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect3)),
						player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect3 + Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect3 + Math.PI * 2 / 3)));
				buffer.setColor(new Color(53, 230, 240));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect1)), player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect1)),
						player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect1 - Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect1 - Math.PI * 2 / 3)));
				buffer.setColor(new Color(53, 218, 255));
				buffer.drawOval(player.target.x - radius, player.target.y - radius, radius * 2, radius * 2);
				buffer.setColor(new Color(40, 210, 250));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect2)), player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect2)),
						player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect2 - Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect2 - Math.PI * 2 / 3)));
				buffer.setColor(new Color(53, 230, 240));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect1 + Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect1 + Math.PI * 2 / 3)),
						player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect1 - Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect1 - Math.PI * 2 / 3)));
				buffer.setColor(new Color(20, 200, 255));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect3)), player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect3)),
						player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect3 - Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect3 - Math.PI * 2 / 3)));

				buffer.setColor(new Color(40, 210, 250));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect2 + Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect2 + Math.PI * 2 / 3)),
						player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect2 - Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect2 - Math.PI * 2 / 3)));

				buffer.setColor(new Color(20, 200, 255));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect3 + Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect3 + Math.PI * 2 / 3)),
						player.target.x + (int) (radius * 1.3 * Math.cos(ability.targetEffect3 - Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(ability.targetEffect3 - Math.PI * 2 / 3)));

			} else
			{
				buffer.setColor(Color.red);
				buffer.drawOval(player.target.x - 25, player.target.y - 25, 50, 50);
				buffer.drawRect(player.target.x - player.radius / 2, player.target.y - player.radius / 2, player.radius, player.radius);
				buffer.drawOval(player.target.x - radius, player.target.y - radius, radius * 2, radius * 2);
			}
			double angle = Math.atan2(player.target.y - player.y, player.target.x - player.x);
			double distance = Math.sqrt(Math.pow(player.x - player.target.x, 2) + Math.pow(player.y - player.target.y, 2));
			buffer.setStroke(dashedStroke3);
			buffer.drawLine((int) (player.x + 0.1 * distance * Math.cos(angle)), (int) (player.y + 0.1 * distance * Math.sin(angle)), (int) (player.x + 0.9 * distance * Math.cos(angle)),
					(int) (player.y + 0.9 * distance * Math.sin(angle)));

			break;
		case "createFF":
			double angleToFF = Math.atan2(player.y - player.target.y, player.x - player.target.x);
			buffer.setColor(new Color(53, 218, 255));
			buffer.setStroke(dashedStroke3);
			buffer.rotate(angleToFF + Math.PI / 2, player.target.x, player.target.y);
			buffer.drawRect((int) (player.target.x - ability.targetEffect2 / 2), (int) (player.target.y - ability.targetEffect3 / 2), (int) (ability.targetEffect2), (int) (ability.targetEffect3));
			buffer.rotate(-angleToFF - Math.PI / 2, player.target.x, player.target.y);
			break;
		case "":
		default:
			break;
		}
	}

	// Start of the program. Set-up stuff happens here!
	void restart()
	{
		System.setProperty("sun.java2d.opengl", "True");

		EPgenerator.initializeFHRE();
		PowerGenerator.initializeTables();
		Ability.initializeDescriptions();
		Resources.initialize();
		Person.resetIDs();

		// ~~~TEMPORARY TESTING~~~

		env = new Environment(50, 50);
		for (int i = 0; i < 50; i++)
			for (int j = 0; j < 50; j++)
			{
				env.floorTypes[i][j] = 0;
				if (i == 0 || j == 0 || i == 49 || j == 49)
				{
					env.addWall(i, j, -2, true);
				}
			}
		env.shadowX = 1;
		env.shadowY = -0.7;

		player = new Player(96 * 20, 96 * 20);
		player.abilities.add(Ability.ability("Ball <Earth>", 5));
		player.abilities.add(Ability.ability("Heal I", 5));
		player.abilities.add(Ability.ability("Force Shield", 5));
		player.abilities.add(Ability.ability("Flight I", 5));
		player.abilities.add(Ability.ability("Blink", 5));
		player.abilities.add(Ability.ability("Beam <Energy>", 5));
		player.abilities.add(Ability.ability("Ghost Mode I", 5));
		player.abilities.add(Ability.ability("Sense Life", 5));
		player.abilities.add(Ability.ability("Sense Mana and Stamina", 5));
		player.abilities.add(Ability.ability("Elemental Combat II <Earth>", 5));
		player.updateAbilities(); // for the elemental combat
		player.abilities.add(Ability.ability("Beam <Plant>", 5));
		player.abilities.add(Ability.ability("Sense Powers", 4));
		player.abilities.add(Ability.ability("Strong Force Field", 5));
		player.updateAbilities(); // Because we added some abilities and the hotkeys haven't been updated
		env.people.add(player);
		camera = new Point3D((int) player.x, (int) player.y, (int) player.z + 25);

		Person shmulik = new NPC(96 * 22, 96 * 19, "aggressive");
		shmulik.abilities.add(Ability.ability("Beam <Energy>", 6));
		shmulik.abilities.add(Ability.ability("Flight II", 5));
		shmulik.abilities.add(Ability.ability("Force Shield", 3));
		shmulik.abilities.add(Ability.ability("Ball <Earth>", 6));
		shmulik.abilities.add(Ability.ability("Heal I", 3));
		// env.people.add(shmulik);

		Person tzippi = new NPC(96 * 15, 96 * 25, "passive");
		// tzippi.trigger();
		env.people.add(tzippi);
		Person aa = new NPC(96 * 17, 96 * 27, "passive");
		// aa.trigger();
		env.people.add(aa);
		Person cc = new NPC(96 * 10, 96 * 25, "passive");
		// cc.trigger();
		env.people.add(cc);
	}

	void pressAbilityKey(int abilityIndex, boolean press, Person p)
	{
		// n is between 1 and 10; checkHotkey need a number between 0 and 9. So.... n-1.
		if (p.target.getX() == -1 && p.target.getY() == -1)
			p.target = new Point(mx, my); /// WHT ARE YOU DOING ITAMAR WTF ARE YOU EVEN THINKING
		if (stopUsingPower)
		{
			if (!press)
				stopUsingPower = false;
		} else
		{
			if (p.abilities.size() <= abilityIndex || abilityIndex == -1)
			{
				errorMessage("No such ability index for p!: " + abilityIndex);
				return;
			}
			Ability a = p.abilities.get(abilityIndex);
			if (press)
			{
				if (p.abilityTryingToRepetitivelyUse == -1)
				{
					if (a.maintainable)
					{
						if (!p.maintaining)
						{
							p.abilityMaintaining = abilityIndex;
							if (!a.on)
								a.use(env, p, p.target);
						} // Can't start a maintainable ability while maintaining another
					} else
					{
						if (a.instant && !a.hasTag("on-off")) // Instant ability, without aim
						{
							a.use(env, p, p.target);
							p.abilityTryingToRepetitivelyUse = abilityIndex;
						} else
							p.abilityAiming = abilityIndex; // straightforward
					}
				}
				// if trying to use ability while repetitively trying another, doesn't work
			} else if (p.abilityAiming != -1 && p.abilityAiming == abilityIndex) // = activate currently aimed ability
			{
				a.use(env, p, p.target);
				p.abilityAiming = -1;
			} else if (p.maintaining && p.abilityMaintaining == abilityIndex)
			{
				a.use(env, p, p.target); // stop maintaining
				p.abilityMaintaining = -1;
			} else if (!p.maintaining && p.abilityMaintaining == abilityIndex) // player's maintaining was stopped before player released key
				p.abilityMaintaining = -1;
			else if (p.abilityTryingToRepetitivelyUse == abilityIndex)
			{
				p.abilityTryingToRepetitivelyUse = -1;
				p.notMoving = false;
			}

		}
	}

	void playerPressHotkey(int n, boolean press)
	{
		if (!tab || !press)
		{
			keyPressFixingMethod(n, press);
		} else
		{
			if (press && (tabHoverAbility == -1 || player.abilities.get(tabHoverAbility).cost != -1)) // the order of the || is important
				player.hotkeys[n - 1] = tabHoverAbility;
			player.abilityAiming = -1;

			// if keys are released during tab it is a problem? TODO test
		}
	}

	boolean[] presses = new boolean[10];

	void keyPressFixingMethod(int n, boolean press)
	{
		// method is called each frame
		// this entire method is to prevent the bug that happens when the keyboard stops giving "press" signals but haven't yet sent the "release" signal
		if (n != -1)
			presses[n - 1] = press;
		// auto-press all non-released keys
		for (int i = 0; i < presses.length; i++)
			if (presses[i])
			{
				pressAbilityKey(player.hotkeys[i], true, player);
			}
		// releases only when keys actually release
		if (!press)
			pressAbilityKey(player.hotkeys[n - 1], false, player);
	}

	void movePerson(Person p, double deltaTime)
	{
		double velocityLeft = Math.sqrt(p.xVel * p.xVel + p.yVel * p.yVel) * deltaTime;

		p.lastSpeed = velocityLeft / deltaTime;
		double moveQuantumX = p.xVel / velocityLeft * deltaTime;
		double moveQuantumY = p.yVel / velocityLeft * deltaTime; // vector combination of moveQuantumX and moveQuantumY is equal to 1 pixel per frame.
		// This function moves the physics object one pixel towards their direction, until they can't move anymore or they collide with something.
		if (velocityLeft > 0)
			p.insideWall = false; // pretty important
		Rectangle2D personRect = new Rectangle2D.Double((int) p.x - p.radius / 2, (int) p.y - p.radius / 2, p.radius, p.radius); // for FF collisions
		while (velocityLeft > 0)
		{
			if (velocityLeft < 1)
			{ // last part of movement
				moveQuantumX *= velocityLeft;
				moveQuantumY *= velocityLeft;
			} else
				// non-last parts of movement
				velocityLeft -= 1;
			// Move p a fraction
			p.x += moveQuantumX;
			p.y += moveQuantumY;
			// check collisions with walls in the environment, locked to a grid
			if (p.z <= 1)
				for (int i = (int) (p.x - 0.5 * p.radius); velocityLeft > 0 && i / squareSize <= (int) (p.x + 0.5 * p.radius) / squareSize; i += squareSize)
					for (int j = (int) (p.y - 0.5 * p.radius); velocityLeft > 0 && j / squareSize <= (int) (p.y + 0.5 * p.radius) / squareSize; j += squareSize)
					{
						if (env.wallTypes[i / squareSize][j / squareSize] != -1)
						{
							if (!p.ghostMode) // ghosts pass through stuff
							{
								if (p.z > 0.1 && p.z <= 1 && p.zVel < 0) // if falling into a wall
								{
									p.z = 1; // standing on a wall
									for (Ability a : p.abilities) // stop flying when landing this way
										if (a.hasTag("flight"))
										{
											a.use(env, p, p.target);
										}
									p.zVel = 0;
									if (p instanceof NPC)
										((NPC) p).justCollided = true;
								} else if (p.z < 1)
								{
									double prevVelocity = velocityLeft;
									if (collideWithWall(p, i / squareSize, j / squareSize))
									{
										p.x -= moveQuantumX;
										p.y -= moveQuantumY;
										if (p instanceof NPC)
											((NPC) p).justCollided = true;
										if (p.z > 0 && p.zVel < 0)
										{
											p.z -= p.zVel * deltaTime;
											p.zVel = 0;
										}
										velocityLeft *= Math.sqrt(p.xVel * p.xVel + p.yVel * p.yVel) * deltaTime / prevVelocity;
										// "velocity" decreases as the thing moves. If speed is decreased, velocity is multiplied by the ratio of the previous speed and the current one.
										if (velocityLeft != 0)
										{
											moveQuantumX = p.xVel / velocityLeft * deltaTime;
											moveQuantumY = p.yVel / velocityLeft * deltaTime;
											p.x += moveQuantumX;
											p.y += moveQuantumY;
										}
									}
								}
							} else // to avoid ghosts reappearing inside walls
								p.insideWall = true;
						}
					}

			for (Person p2 : env.people)
				if (!p.equals(p2))
				{
					Rectangle2D p1rect = new Rectangle2D.Double(p.x - 0.5 * p.radius, p.y - 0.5 * p.radius, p.radius, p.radius);
					Rectangle2D p2rect = new Rectangle2D.Double(p2.x - 0.5 * p2.radius, p2.y - 0.5 * p2.radius, p2.radius, p2.radius);
					if (p1rect.intersects(p2rect)) // collision check
					{
						if (p2.z + p2.height > p.z && p2.z < p.z + p.height)
						{
							// physics. Assumes the two people are circles.
							// The following code is translated from a StackExchange answer.
							double xVelocity = p2.xVel - p.xVel;
							double yVelocity = p2.yVel - p.yVel;
							double dotProduct = (p2.x - p.x) * xVelocity + (p2.y - p.y) * yVelocity;
							// Neat vector maths, used for checking if the objects moves towards one another.
							if (dotProduct < 0)
							{
								double collisionScale = dotProduct / Methods.DistancePow2(p.x, p.y, p2.x, p2.y);
								double xCollision = (p2.x - p.x) * collisionScale;
								double yCollision = (p2.y - p.y) * collisionScale;
								// The Collision vector is the speed difference projected on the Dist vector,
								// thus it is the component of the speed difference needed for the collision.
								double combinedMass = p.mass + p2.mass;
								double collisionWeightA = 2 * p2.mass / combinedMass;
								double collisionWeightB = 2 * p.mass / combinedMass;
								p.xVel += collisionWeightA * xCollision;
								p.yVel += collisionWeightA * yCollision;
								p2.xVel -= collisionWeightB * xCollision;
								p2.yVel -= collisionWeightB * yCollision;
								p.x -= 2 * moveQuantumX; // good enough for most purposes right now
								p.y -= 2 * moveQuantumY;//
								if (p instanceof NPC)
									((NPC) p).justCollided = true;
								if (p2 instanceof NPC)
									((NPC) p2).justCollided = true;
							}
						}
					}
				}
			for (ForceField ff : env.FFs)
			{
				// checks if (rotated) force field corners are inside person hitbox
				if (ff.z + ff.height > p.z && ff.z < p.z + p.height)
				{
					boolean collidedWithACorner = false;
					for (Point p1 : ff.p)
						if (p1.x > p.x - p.radius / 2 && p1.x < p.x + p.radius / 2 && p1.y > p.y - p.radius / 2 && p1.y < p.y + p.radius / 2)
						{
							collidedWithACorner = true;
							// hitting corners just reverses the person's movement
							p.x -= moveQuantumX;
							p.y -= moveQuantumY;
							p.xVel = -p.xVel;
							p.yVel = -p.yVel;
							p.x += deltaTime * p.xVel;
							p.y += deltaTime * p.yVel;
							env.hitPerson(p, 5 * deltaTime, 0, 0, 4);
							if (p instanceof NPC)
								((NPC) p).justCollided = true;
						}
					if (!collidedWithACorner)
					{
						Line2D l1 = new Line2D.Double(ff.p[0].x, ff.p[0].y, ff.p[3].x, ff.p[3].y);
						Line2D l2 = new Line2D.Double(ff.p[0].x, ff.p[0].y, ff.p[1].x, ff.p[1].y);
						Line2D l3 = new Line2D.Double(ff.p[2].x, ff.p[2].y, ff.p[1].x, ff.p[1].y);
						Line2D l4 = new Line2D.Double(ff.p[2].x, ff.p[2].y, ff.p[3].x, ff.p[3].y);
						boolean collided = false;
						double lineAngle = 0;
						if (personRect.intersectsLine(l1))
						{
							collided = true;
							lineAngle = ff.rotation + 0.5 * Math.PI;
						}
						if (personRect.intersectsLine(l2))
						{
							collided = true;
							lineAngle = ff.rotation;
						}
						if (personRect.intersectsLine(l3))
						{
							collided = true;
							lineAngle = ff.rotation + 0.5 * Math.PI;
						}
						if (personRect.intersectsLine(l4))
						{
							collided = true;
							lineAngle = ff.rotation;
						}
						if (collided)
						{
							// BUGGY
							// SRSLY
							// TODO
							p.x -= moveQuantumX;
							p.y -= moveQuantumY;
							// attempt at physics
							double personAngle = Math.atan2(moveQuantumY, moveQuantumX); // can also use yVel, xVel
							personAngle = 2 * lineAngle - personAngle + Math.PI;
							double velocity = Math.sqrt(p.xVel * p.xVel + p.yVel * p.yVel);
							p.xVel = velocity * Math.cos(personAngle);
							p.yVel = velocity * Math.sin(personAngle);
							moveQuantumX = Math.cos(personAngle) * deltaTime;
							moveQuantumY = Math.sin(personAngle) * deltaTime;
							p.x += 80 * moveQuantumX;
							p.y += 80 * moveQuantumY;
							// zap
							env.hitPerson(p, 5 * deltaTime, 0, 0, 4);
							if (p instanceof NPC)
								((NPC) p).justCollided = true;
						}
					}
				}

			}
			if (velocityLeft < 1) // continue
				velocityLeft = 0;
			personRect = new Rectangle2D.Double((int) p.x - p.radius / 2, (int) p.y - p.radius / 2, p.radius, p.radius);
		}
		// extra check for insideWall, in case you stand still
		if (p.ghostMode && p.z < 1)
			for (int i = (int) (p.x - 0.5 * p.radius); i / squareSize <= (int) (p.x + 0.5 * p.radius) / squareSize; i += squareSize)
				for (int j = (int) (p.y - 0.5 * p.radius); j / squareSize <= (int) (p.y + 0.5 * p.radius) / squareSize; j += squareSize)
					if (env.wallTypes[i / squareSize][j / squareSize] != -1)
						p.insideWall = true;
		// test boundaries
		if (p.x < 0 || p.y < 0 || p.x > env.widthPixels || p.y > env.heightPixels)
		{
			p.x -= p.xVel;
			p.y -= p.yVel;
			p.xVel = 0;
			p.yVel = 0;
		}
	}

	boolean moveBall(Ball b, double deltaTime)
	{
		// return false if ball was destroyed

		double velocityLeft = Math.sqrt(b.xVel * b.xVel + b.yVel * b.yVel) * deltaTime;
		double moveQuantumX = b.xVel / velocityLeft * deltaTime;
		double moveQuantumY = b.yVel / velocityLeft * deltaTime; // vector combination of moveQuantumX and moveQuantumY is equal to 1 pixel per frame.

		// This function moves the physics object one pixel towards their direction, until they can't move anymore or they collide with something.
		while (velocityLeft > 0)
		{
			if (velocityLeft < 1)
			{ // last part of movement
				moveQuantumX *= velocityLeft;
				moveQuantumY *= velocityLeft;
				velocityLeft = 0;
			} else
				// non-last parts of movement
				velocityLeft--;
			// Move p a fraction
			// IMPORTANT NOTE!!!!! moveQuantumX and moveQuantumY don't update every check, so if the velocity or the angle change they should be recalculated immediately afterwards (like when a ball
			// bounces).
			b.x += moveQuantumX;
			b.y += moveQuantumY;

			// if ball exits edge of environment
			if (b.x - b.radius < 0 || b.y - b.radius < 0 || b.x + b.radius > env.heightPixels || b.y + b.radius > env.heightPixels)
				return false;

			// check collisions with walls in the environment, locked to a grid
			if (b.z < 1)
				for (int i = (int) (b.x - b.radius); velocityLeft > 0 && i / squareSize <= (int) (b.x + b.radius) / squareSize; i += squareSize)
					for (int j = (int) (b.y - b.radius); velocityLeft > 0 && j / squareSize <= (int) (b.y + b.radius) / squareSize; j += squareSize)
					{
						if (env.wallTypes[i / squareSize][j / squareSize] != -1)
						{
							Point p = new Point((i / squareSize) * squareSize, (j / squareSize) * squareSize);
							double px = b.x, py = b.y;
							// point on rectangle closest to circle. (snaps the point to the rectangle, pretty much, if the circle center is inside the rectangle there isn't snapping, but this is fine
							// since it will detect a collision as a result)

							if (px > p.x + squareSize)
								px = p.x + squareSize;
							if (px < p.x)
								px = p.x;
							if (py > p.y + squareSize)
								py = p.y + squareSize;
							if (py < p.y)
								py = p.y;

							// distance check:
							if (Math.pow(b.x - px, 2) + Math.pow(b.y - py, 2) < Math.pow(b.radius, 2))
							{
								// collision confirmed.
								// Resolving collision:
								boolean bounce = false;
								// TODO balls bouncing off certain walls?
								if (bounce)
								{
									double prevVelocity = velocityLeft;
									collideWithWall(b, i / squareSize, j / squareSize, (int) px, (int) py);
									b.x -= moveQuantumX;
									b.y -= moveQuantumY;
									velocityLeft *= b.velocity() / prevVelocity * deltaTime; // "velocity" decreases as the thing moves. If speed is decreased, velocity is multiplied by
									// the ratio of the previous speed and the current one.
									if (velocityLeft != 0)
									{
										moveQuantumX = b.xVel / velocityLeft * deltaTime;
										moveQuantumY = b.yVel / velocityLeft * deltaTime;
										b.x += moveQuantumX;
										b.y += moveQuantumY;
									}
								} else
								{
									env.damageWall(i / squareSize, j / squareSize, b.getDamage() + b.getPushback(), EP.damageType(b.elementNum));
									// debris
									env.ballDebris(b, "wall");
									// ball was destroyed
									return false;
								}
							}
						}
					}

			// check collisions with people!
			for (Person p : env.people)
			{
				// TODO testing for evasion, etc.
				if (p.z + p.height > b.z && p.z < b.z + b.height)
					if (!p.ghostMode || EP.damageType(b.elementNum) == 4 || EP.damageType(b.elementNum) == 2) // shock and fire
						// temp collide calculation
						if (Math.sqrt(Math.pow(p.x - b.x, 2) + Math.pow(p.y - b.y, 2)) < p.radius / 2 + b.radius)
						{
							boolean bounce = false;
							// TODO find out which power causes bounce skin
							for (int i = 0; i < p.abilities.size(); i++)
								if (p.abilities.get(i).name.equals("some_bounce_ability"))
									bounce = true;
							if (bounce)
							{
								if (b.x - b.radius < p.x + 0.5 * p.radius || b.x + b.radius > p.x - 0.5 * p.radius)
									b.xVel = -b.xVel;
								if (b.y - b.radius < p.y + 0.5 * p.radius || b.y + b.radius > p.y - 0.5 * p.radius)
									b.yVel = -b.yVel;
							} else
							{
								// damage person
								env.hitPerson(p, b.getDamage(), b.getPushback(), b.angle(), EP.damageType(b.elementNum));
								if (p instanceof NPC)
									((NPC) p).justCollided = true;
								env.ballDebris(b, "shatter");
								// destroy ball
								return false;
							}
						}
			}

			// check collisions with arc force fields
			for (ArcForceField aff : env.arcFFs)
			{
				if (aff.z + aff.height > b.z && aff.z < b.z + b.height)
				{
					double angleToBall = Math.atan2(b.y - aff.target.y, b.x - aff.target.x);
					while (angleToBall < 0)
						angleToBall += 2 * Math.PI;
					double minAngle = (aff.rotation - (aff.arc + 2 * b.radius / aff.maxRadius) / 2);
					double maxAngle = (aff.rotation + (aff.arc + 2 * b.radius / aff.maxRadius) / 2);
					while (minAngle < 0)
						minAngle += 2 * Math.PI;
					while (minAngle >= 2 * Math.PI)
						minAngle -= 2 * Math.PI;
					while (maxAngle < 0)
						maxAngle += 2 * Math.PI;
					while (maxAngle >= 2 * Math.PI)
						maxAngle -= 2 * Math.PI;
					boolean withinAngles = false;
					// Okay so here's a thing: I assume the circle is a point, and increase the aff's dimensions for the calculation, and it's almost precise!
					if (minAngle < maxAngle)
					{
						if (angleToBall > minAngle && angleToBall < maxAngle)
							withinAngles = true;
					} else if (angleToBall < minAngle || angleToBall > maxAngle)
						withinAngles = true;
					if (withinAngles)
					{
						double distance = Math.sqrt(Math.pow(aff.target.y - b.y, 2) + Math.pow(aff.target.x - b.x, 2));
						if (distance > aff.minRadius - b.radius && distance < aff.maxRadius + b.radius)
						// That's totally not a legit collision check, but honestly? it's pretty darn close, according to my intuition.
						{
							if (aff.elementNum == 6 && EP.damageType(b.elementNum) == 4) // electricity and energy balls bounce off of energy
							{
								double damage = (b.getDamage() + b.getPushback()) * 0.5; // half damage, because the ball bounces
								env.damageArcForceField(aff, damage,
										new Point((int) (aff.target.x + aff.maxRadius * Math.cos(angleToBall)), (int) (aff.target.y + aff.maxRadius * Math.sin(angleToBall))),
										EP.damageType(b.elementNum));
								env.hitPerson(aff.target, 0, 0.5 * b.getPushback(), b.angle(), 0);
								// TODO cool sparks
								// PHYSICS
								double angle = 2 * angleToBall - b.angle() + Math.PI;
								// avoiding repeat-bounce immediately afterwards
								moveQuantumX = Math.cos(angle);
								moveQuantumY = Math.sin(angle);
								double velocity = b.velocity();
								b.xVel = velocity * moveQuantumX;
								b.yVel = velocity * moveQuantumY;
								// avoiding it some more
								b.x += moveQuantumX;
								b.y += moveQuantumY;
							} else if (EP.damageType(aff.elementNum) > 1 && EP.damageType(aff.elementNum) != EP.damageType(b.elementNum)) // if damage resistance, and not a "normal" element
							{
								errorMessage("You need to write some code here!");
							} else
							{
								// TODO damage depends on ball speed maybe?a
								// TODO water strong against fire, electricity unblockable by some and entirely blockable by others, , bouncing from metal, etc.
								double damage = b.getDamage() + b.getPushback();
								env.damageArcForceField(aff, damage,
										new Point((int) (aff.target.x + aff.maxRadius * Math.cos(angleToBall)), (int) (aff.target.y + aff.maxRadius * Math.sin(angleToBall))),
										EP.damageType(b.elementNum));
								env.hitPerson(aff.target, 0, 0.5 * b.getPushback(), b.angle(), 0);

								// Special effects! debris!
								env.ballDebris(b, "arc force field", angleToBall);
								return false;
							}
						}
					}
				}
			}

			// Force Fields
			for (ForceField ff : env.FFs)
			{
				if (ff.z + ff.height > b.z && ff.z < b.z + b.height)
					// to avoid needless computation, This line tests basic hitbox collisions first
					if (ff.x - 0.5 * ff.length <= b.x + b.radius && ff.x + 0.5 * ff.length >= b.x - b.radius && ff.y - 0.5 * ff.length <= b.y + b.radius && ff.y + 0.5 * ff.length >= b.y - b.radius)
					{
						boolean bounce = true;
						// TODO move it to once per frame in the FF's code area in frame()
						while (ff.rotation < 0)
							ff.rotation += 2 * Math.PI;
						while (ff.rotation >= 2 * Math.PI)
							ff.rotation -= 2 * Math.PI;
						Point ballCenter = new Point((int) b.x, (int) b.y);
						// pow2 to avoid using Math.sqrt(), which is supposedly computationally expensive.
						double ballRadiusPow2 = Math.pow(b.radius, 2);
						// TODO also test if circle is entirely within the forcefield's rectangle
						/*
						 * four cases because four vertices, and each has its own visual effect In cases 01 and 23, the bounce angle is -Math.PI. but in cases 12 and 30 it's -0. Because rectangle. I can split them to two if-else-ifs because a circle
						 * can't collide with more than 2 of the vertices at once, obviously
						 */
						if (0 <= Methods.realDotProduct(ff.p[0], ballCenter, ff.p[1]) && Methods.realDotProduct(ff.p[0], ballCenter, ff.p[1]) <= ff.width * ff.width
								&& 0 <= Methods.realDotProduct(ff.p[0], ballCenter, ff.p[3]) && Methods.realDotProduct(ff.p[0], ballCenter, ff.p[3]) <= ff.length * ff.length)
						// circle center is within FF. This basically never ever should happen.
						{
							env.damageFF(ff, b.getDamage() + b.getPushback(), ballCenter);

							// FX
							for (int i = 0; i < 7; i++)
								env.debris.add(new Debris(b.x, b.y, b.z, b.angle() + 4 + i * (4) / 6, b.elementNum, 500));
							return false;
						} else
						{
							if (Methods.LineToPointDistancePow2(ff.p[0], ff.p[1], ballCenter) < ballRadiusPow2)
							{
								// TODO cool sparks
								if (bounce)
								{
									// PHYSICS
									double angle = 2 * ff.rotation - b.angle() + Math.PI; // 2*rotation - angle + 180
									// avoiding repeat-bounce immediately afterwards
									moveQuantumX = Math.cos(angle);
									moveQuantumY = Math.sin(angle);
									double velocity = b.velocity();
									b.xVel = velocity * moveQuantumX;
									b.yVel = velocity * moveQuantumY;
									// avoiding it some more
									b.x += moveQuantumX;
									b.y += moveQuantumY;
								}
							} else if (Methods.LineToPointDistancePow2(ff.p[2], ff.p[3], ballCenter) < ballRadiusPow2)
							{
								// TODO cool sparks
								if (bounce)
								{
									// PHYSICS
									double angle = 2 * ff.rotation - b.angle() + Math.PI;// 2*rotation - angle + 180
									// avoiding repeat-bounce immediately afterwards
									moveQuantumX = Math.cos(angle);
									moveQuantumY = Math.sin(angle);
									double velocity = b.velocity();
									b.xVel = velocity * moveQuantumX;
									b.yVel = velocity * moveQuantumY;
									// avoiding it some more
									b.x += moveQuantumX;
									b.y += moveQuantumY;
								}
							}
							if (Methods.LineToPointDistancePow2(ff.p[1], ff.p[2], ballCenter) < ballRadiusPow2)
							{
								// TODO cool sparks
								if (bounce)
								{
									// PHYSICS
									double angle = 2 * ff.rotation - b.angle();// 2*rotation - angle
									// avoiding repeat-bounce immediately afterwards
									moveQuantumX = Math.cos(angle);
									moveQuantumY = Math.sin(angle);
									double velocity = b.velocity();
									b.xVel = velocity * moveQuantumX;
									b.yVel = velocity * moveQuantumY;
									// avoiding it some more
									b.x += moveQuantumX;
									b.y += moveQuantumY;
								}
							} else if (Methods.LineToPointDistancePow2(ff.p[3], ff.p[0], ballCenter) < ballRadiusPow2)
							{
								// TODO cool sparks
								if (bounce)
								{
									// PHYSICS
									double angle = 2 * ff.rotation - b.angle(); // 2*rotation - angle
									// avoiding repeat-bounce immediately afterwards
									moveQuantumX = Math.cos(angle);
									moveQuantumY = Math.sin(angle);
									double velocity = b.velocity();
									b.xVel = velocity * moveQuantumX;
									b.yVel = velocity * moveQuantumY;
									// avoiding it some more
									b.x += moveQuantumX;
									b.y += moveQuantumY;
								}
							}
						}
					}
			}
			for (Ball b2 : env.balls)
			{
				if (b == b2 || b2.mass <= 0 || b2.velocity() <= 0)
					continue;
				// TODO testing for evasion, etc.
				if (b2.z + b2.height > b.z && b2.z < b.z + b.height)
					if (Math.pow(b2.x - b.x, 2) + Math.pow(b2.y - b.y, 2) < Math.pow(b2.radius + b.radius, 2))
					{
						boolean bounce = false;
						if (bounce)
						{
							// TODO balls bounce from each other (shouldn't be hard)
						} else
						{
							if (b.mass > b2.mass)
							{
								env.ballDebris(b2, "shatter");
								// collisions reduce mass from the stronger ball
								b.mass -= b2.mass;
								b2.xVel = 0;
								b2.yVel = 0;
								b2.mass = 0;
							} else if (b2.mass > b.mass)
							{
								env.ballDebris(b, "shatter");
								b2.mass -= b.mass;
								return false;
							} else // equal masses
							{
								env.ballDebris(b2, "shatter");
								env.ballDebris(b, "shatter");
								b2.xVel = 0;
								b2.yVel = 0;
								b2.mass = 0;
								return false;
							}
						}
					}
			}
		}

		// ball gravity
		b.z += b.zVel;
		if (b.z < 0)
		{
			// debris
			env.ballDebris(b, "shatter");
			return false;
		}

		return true;
	}

	boolean collideWithWall(Person p, int x, int y) // x and y in grid
	{
		// returns whether or not this collision changes the person's speed
		// TODO add stuff concerning bounce powers, or breaking through walls, or damaging walls, or damaging characters flung into walls
		final double bounceEfficiency = 0.4;
		// assumes objects have no angle
		Rectangle intersectRect = new Rectangle(x * squareSize, y * squareSize, squareSize, squareSize)
				.intersection(new Rectangle((int) (p.x - 0.5 * p.radius), (int) (p.y - 0.5 * p.radius), (int) (p.radius), (int) (p.radius)));
		if (p.x > intersectRect.x + 0.5 * intersectRect.width)
			p.xVel = Math.abs(p.xVel) * bounceEfficiency;
		if (p.x < intersectRect.x + 0.5 * intersectRect.width)
			p.xVel = -Math.abs(p.xVel) * bounceEfficiency;
		if (p.y > intersectRect.y + 0.5 * intersectRect.height)
			p.yVel = Math.abs(p.yVel) * bounceEfficiency;
		if (p.y < intersectRect.y + 0.5 * intersectRect.height)
			p.yVel = -Math.abs(p.yVel) * bounceEfficiency;

		return true;
	}

	void collideWithWall(RndPhysObj b, int x, int y, int px, int py) // x and y in grid
	{
		final double bounceEfficiency = 1;
		// assumes walls have no angle, of course (they don't)

		if (px == x * squareSize && (b.angle() < Math.PI / 2 || b.angle() >= Math.PI * 3 / 2)) // left side, moving right
			b.xVel = -b.xVel;
		if (px == (x + 1) * squareSize && b.angle() >= Math.PI / 2 && b.angle() < Math.PI * 3 / 2) // right side, moving left
			b.xVel = -b.xVel;
		if (py == y * squareSize && b.angle() < Math.PI) // up side, moving down
			b.yVel = -b.yVel;
		if (py == (y + 1) * squareSize && b.angle() >= Math.PI) // down side, moving up
			b.yVel = -b.yVel;

		b.xVel *= bounceEfficiency;
		b.yVel *= bounceEfficiency;
	}

	double applyGravityAndFrictionAndReturnFriction(Person p, double deltaTime)
	{
		double velocity = Math.sqrt(p.xVel * p.xVel + p.yVel * p.yVel);
		double moveDirectionAngle = Math.atan2(p.yVel, p.xVel);

		if (p.z > 1 && p.z + deltaTime * p.zVel < 0.5)
		{ // TODO WTF?
			// = when framerate causes problems
			p.z = 0.9;
			movePerson(p, deltaTime); // to test for wall-touching
		}

		p.z += deltaTime * p.zVel;

		// gravity
		p.zVel -= 100 * gravity * deltaTime;
		if (p.z < 0)
		{
			p.z = 0;
			for (Ability a : p.abilities) // stop flying when landing this way
				if (a.hasTag("flight") && a.on)
				{
					a.use(env, p, p.target);
				}
		}
		if (p.z < 0.1 || (p.z == 1 && !p.ghostMode && env.wallTypes[(int) (p.x) / squareSize][(int) (p.y) / squareSize] != -1)) // on ground or on a wall
		{
			p.zVel = 0;
			int floorType = env.floorTypes[(int) (p.x) / squareSize][(int) (p.y) / squareSize];
			int poolType = env.poolTypes[(int) (p.x) / squareSize][(int) (p.y) / squareSize];
			int wallType = env.wallTypes[(int) (p.x) / squareSize][(int) (p.y) / squareSize];
			double friction = Environment.floorFriction[floorType];

			if (poolType != -1)
				friction = Environment.poolFriction[poolType];
			if (p.z == 1 && p.zVel == 0 && wallType != -1)
				friction = Environment.wallFriction[wallType];
			if (!p.prone)
				friction *= standingFrictionBenefit;
			if (p.ghostMode)
				friction *= ghostFrictionMultiplier;

			// temp fix for meter-centimeter-pixel mistakes
			friction *= 96;

			if (velocity > friction * gravity * deltaTime)
				velocity -= friction * gravity * deltaTime;
			else
				velocity = 0;

			p.xVel = velocity * Math.cos(moveDirectionAngle);
			p.yVel = velocity * Math.sin(moveDirectionAngle);
			return friction;
		}

		if (p.z > 0)
		{

			// air resistance
			double density = 1.2; // density of air in 20 degree celsius, according to Wikipedia.
			double dragCoefficient = 0.47; // drag coefficient of a sphere. A human body is spherical.
			double area = 0.0004340277 * p.radius * p.radius; // Measured myself, asked around in the WD IRC, this seems like a good-enough value. //TODO add 0.1 to this if the person is wearing special armor or a cape
			double drag = 0.5 * density * velocity * velocity * area * dragCoefficient / p.mass; // acceleration, not force
			velocity -= drag * deltaTime;
			p.xVel = velocity * Math.cos(moveDirectionAngle);
			p.yVel = velocity * Math.sin(moveDirectionAngle);
			if (p.zVel < 0) // falling
			{
				double zDrag = 0.5 * density * p.zVel * p.zVel * area * dragCoefficient / p.mass;
				p.zVel += zDrag * deltaTime;
			}

		}

		return 0;
	}

	void paintBuffer(Graphics g)
	{
		// NOTICE! THE ORDER OF DRAWING OPERATIONS IS ACTUALY IMPORTANT!
		Graphics2D buffer = (Graphics2D) g;
		zoomLevel /= (player.z * heightZoomRatio + 1);
		// Move "camera" to position
		buffer.scale(zoomLevel, zoomLevel);
		buffer.translate(0.5 * frameWidth / zoomLevel, 0.5 * frameHeight / zoomLevel);
		buffer.translate(-camera.x, -camera.y);
		buffer.rotate(-cameraRotation, camera.x, camera.y);

		final int safetyDistance = 50;
		Rectangle bounds = null;
		if (cameraRotation * 180 / Math.PI % 180 == 0)
			bounds = new Rectangle((int) (camera.x - frameWidth / 2 * (player.z - heightZoomRatio + 1) / zoomLevel) - safetyDistance,
					(int) (camera.y - frameHeight / 2 * (player.z - heightZoomRatio + 1) / zoomLevel) - safetyDistance,
					(int) (frameWidth * (player.z - heightZoomRatio + 1) / zoomLevel) + 2 * safetyDistance, (int) (frameHeight * (player.z - heightZoomRatio + 1) / zoomLevel) + 2 * safetyDistance);
		else if ((cameraRotation * 180 / Math.PI + 90) % 180 == 0)
			bounds = new Rectangle((int) (camera.x - frameHeight / 2 * (player.z - heightZoomRatio + 1) / zoomLevel) - safetyDistance,
					(int) (camera.y - frameWidth / 2 * (player.z - heightZoomRatio + 1) / zoomLevel) - safetyDistance,
					(int) (frameHeight * (player.z - heightZoomRatio + 1) / zoomLevel) + 2 * safetyDistance, (int) (frameWidth * (player.z - heightZoomRatio + 1) / zoomLevel) + 2 * safetyDistance);
		else
		{
			// already overshoots, does not need to include safetyDistance
			double halfBoundsDiagonal = Math.sqrt(frameWidth * frameWidth + frameHeight * frameHeight) / 2; // If you don't want a square-root calculation, either make the calculation once and store it in a variable or use frameWidth+frameHeight/2
			// it creates the axis-aligned square that surrounds the circle around the camera, whose radius is half of the diagonal of the screen (in in-game pixels)
			halfBoundsDiagonal = halfBoundsDiagonal * (player.z * heightZoomRatio + 1) / zoomLevel;
			bounds = new Rectangle((int) (camera.x - halfBoundsDiagonal), (int) (camera.y - halfBoundsDiagonal), (int) (halfBoundsDiagonal * 2), (int) (halfBoundsDiagonal * 2));
		}
		env.drawFloor(buffer, this, bounds);
		drawBottomEffects(buffer);
		// environment not including effects and clouds
		env.draw(buffer, this, camera.z, bounds, cameraRotation);
		drawTopEffects(buffer);

		if (hotkeySelected != -1 && player.hotkeys[hotkeySelected] != -1)
			drawRange(buffer, player.abilities.get(player.hotkeys[hotkeySelected]));
		if (tabHoverAbility != -1)
			drawRange(buffer, player.abilities.get(tabHoverAbility));
		if (hotkeyHovered != -1 && player.hotkeys[hotkeyHovered] != -1)
			drawRange(buffer, player.abilities.get(player.hotkeys[hotkeyHovered]));
		if (player.abilityAiming != -1)
			drawAim(buffer, player);

		// temp
		buffer.setColor(Color.red);
		if (drawLine != null)
			buffer.drawLine((int) (drawLine.getX1()), (int) (drawLine.getY1()), (int) (drawLine.getX2()), (int) (drawLine.getY2()));
		if (drawRect != null)
			buffer.drawRect((int) (drawRect.getX()), (int) (drawRect.getY()), (int) (drawRect.getWidth()), (int) (drawRect.getHeight()));

		drawSenseAbilities(buffer);

		// Move camera back
		buffer.rotate(cameraRotation, camera.x, camera.y);
		buffer.translate(camera.x, camera.y);
		buffer.translate(-0.5 * frameWidth / zoomLevel, -0.5 * frameHeight / zoomLevel);
		buffer.scale((double) (1 / zoomLevel), (double) (1 / zoomLevel));

		zoomLevel *= (player.z * heightZoomRatio + 1);
		// User Interface
		if (tab)
			drawTab(buffer);
		drawPlayerStats(buffer);
		drawHotkeysAndEffects(buffer);

		if (timeSinceLastScreenshot < 2)
			drawScreenshot(buffer);
	}

	void drawSenseAbilities(Graphics2D buffer)
	{
		// exists after coordinate shift
		double drawLifeDistancePow2 = 0, drawManaDistancePow2 = 0, drawStaminaDistancePow2 = 0;
		double[] elementSenses = new double[13];
		for (Ability a : player.abilities)
			if (a.on)
				switch (a.justName())
				{
				case "Sense Life":
					drawLifeDistancePow2 = Math.pow(a.range, 2);
					break;
				case "Sense Mana and Stamina":
					drawManaDistancePow2 = Math.pow(a.range, 2);
					drawStaminaDistancePow2 = Math.pow(a.range, 2);
					break;
				case "Sense Element":
					elementSenses[a.getElementNum()] = a.range;
					break;
				case "Sense Powers":
					Sense_Powers spAbility = (Sense_Powers) a;
					// summing up the levels

					// this is supposed to look fantastic.
					buffer.setStroke(new BasicStroke(4));
					double radius = frameHeight / 3;
					int[] elementIndexes = new int[]
					{ 21, 22, 7, 23, 15, 11, 16, 31, 5, 2, 15, 12, 19, 1, 17, 25, 4, 30, 20, 26, 9, 6, 27, 13, 24, 10, 8, 28, 0, 32, 29, 3, 18 };
					String[] colorHexCodes = new String[]
					{ "C6FF7C", "A7C841", "A8A30D", "6D6B08", "156B08", "5DAE00", "00E493", "8FFFC2", "84FFFF", "CDE8FF", "D1CDFF", "91C6FF", "1ECAFF", "0094FF", "0800FF", "404E74", "999999",
							"D5C6CD", "000000", "FFE2EC", "FF75AE", "E751FF", "8131C6", "4F2472", "693F59", "8C2F14", "D32B00", "E57600", "FF6A00", "FF9F00", "FFC97F", "FFD800", "FFF9A8" };
					Point center = new Point((int) (player.x), (int) (player.y));
					buffer.translate(center.x, center.y);
					buffer.rotate(cameraRotation);
					for (int i = 0; i < elementIndexes.length; i++)
					{
						double angle = TAU / elementIndexes.length * i - 0.3 * TAU;
						int elementLevel = 2 * spAbility.details[elementIndexes[i]];
						if (elementLevel <= 0)
							continue;
						Color color = Color.decode("#" + colorHexCodes[i]);
						if (elementIndexes[i] == EP.toInt("Ghost"))
							color = new Color(224, 224, 224, 120);
						buffer.setColor(color);
						buffer.rotate(angle);
						buffer.fillRect(-35, (int) (-radius - elementLevel), 70, elementLevel);
						buffer.rotate(-angle);
					}
					buffer.rotate(-cameraRotation);
					buffer.translate(-center.x, -center.y);
					break;
				default:
					break;
				}
		for (Person p : env.people)
		{
			if (!p.equals(player) && p.z <= camera.z)
			{
				double distancePow2 = Methods.DistancePow2(player.x, player.y, p.x, p.y);

				buffer.translate(p.x, p.y);
				buffer.scale(p.z * Main.heightZoomRatio + 1, p.z * Main.heightZoomRatio + 1);
				buffer.translate(-p.x, -p.y);

				p.drawData(buffer, distancePow2 < drawLifeDistancePow2, distancePow2 < drawManaDistancePow2, distancePow2 < drawStaminaDistancePow2, cameraRotation);

				buffer.translate(p.x, p.y);
				buffer.scale(1 / (p.z * Main.heightZoomRatio + 1), 1 / (p.z * Main.heightZoomRatio + 1));
				buffer.translate(-p.x, -p.y);
			}
		}
	}

	void drawScreenshot(Graphics2D buffer)
	{
		if (lastScreenshot != null)
		{
			buffer.setColor(Color.black);
			buffer.setStroke(new BasicStroke(2));
			double firstpart = 0.5;
			double secondpart = 1.7;
			if (timeSinceLastScreenshot < firstpart)
			{
				buffer.drawImage(lastScreenshot, (int) (timeSinceLastScreenshot * 0.8 / firstpart * frameWidth) - 10, (int) (timeSinceLastScreenshot / firstpart * 0.8 * frameHeight) - 10,
						(int) (frameWidth - timeSinceLastScreenshot / firstpart * 0.8 * frameWidth), (int) (frameHeight - timeSinceLastScreenshot / firstpart * 0.8 * frameHeight), this);
				buffer.drawRect((int) (timeSinceLastScreenshot * 0.8 / firstpart * frameWidth) - 10, (int) (timeSinceLastScreenshot / firstpart * 0.8 * frameHeight) - 10,
						(int) (frameWidth - timeSinceLastScreenshot / firstpart * 0.8 * frameWidth), (int) (frameHeight - timeSinceLastScreenshot / firstpart * 0.8 * frameHeight));
			} else if (timeSinceLastScreenshot < secondpart)
			{
				buffer.drawImage(lastScreenshot, (int) (0.8 * frameWidth) - 10, (int) (0.8 * frameHeight) - 10, (int) (frameWidth - 0.8 * frameWidth), (int) (frameHeight - 0.8 * frameHeight), this);
				buffer.drawRect((int) (0.8 * frameWidth) - 10, (int) (0.8 * frameHeight) - 10, (int) (frameWidth - 0.8 * frameWidth), (int) (frameHeight - 0.8 * frameHeight));
			} else
			{
				buffer.drawImage(lastScreenshot, (int) (timeSinceLastScreenshot / 2 * frameWidth) - 10, (int) (timeSinceLastScreenshot / 2 * frameHeight) - 10,
						(int) (frameWidth - timeSinceLastScreenshot / 2 * frameWidth), (int) (frameHeight - timeSinceLastScreenshot / 2 * frameHeight), this);
				buffer.drawRect((int) (timeSinceLastScreenshot / 2 * frameWidth) - 10, (int) (timeSinceLastScreenshot / 2 * frameHeight) - 10,
						(int) (frameWidth - timeSinceLastScreenshot / 2 * frameWidth), (int) (frameHeight - timeSinceLastScreenshot / 2 * frameHeight));
			}
		} else
		{
			buffer.setStroke(new BasicStroke(1));
			buffer.setColor(Color.red);
			buffer.fillRect(frameWidth - 140, frameHeight - 35, 120, 15);
			buffer.setColor(Color.black);
			buffer.drawRect(frameWidth - 140, frameHeight - 35, 120, 15);
			buffer.drawString("-Screenshot FAILED-", frameWidth - 137, frameHeight - 23);
		}
	}

	void drawBottomEffects(Graphics2D buffer)
	{
		for (VisualEffect eff : env.effects)
			if (!eff.onTop)
			{
				eff.draw(buffer);
			}
	}

	void drawTopEffects(Graphics2D buffer)
	{
		for (VisualEffect eff : env.effects)
			if (eff.onTop)
			{
				eff.draw(buffer);
			}
	}

	void drawPlayerStats(Graphics2D buffer)
	{
		// TEMP. should be fancier in real game, obviously

		// starting beyond window title bar
		buffer.translate(8, 30);
		// Health, Mana, Stamina
		buffer.setStroke(new BasicStroke(1));
		// assuming neither of the following stats is too high (< x10 normal amount)
		buffer.setColor(Color.red);
		buffer.fillRect((int) (20 * UIzoomLevel), (int) (35 * UIzoomLevel), (int) (player.life * 2 * UIzoomLevel), (int) (15 * UIzoomLevel));
		buffer.setColor(Color.blue);
		buffer.fillRect((int) (20 * UIzoomLevel), (int) (60 * UIzoomLevel), (int) (player.mana * 20 * UIzoomLevel), (int) (15 * UIzoomLevel));
		buffer.setColor(Color.green);
		buffer.fillRect((int) (20 * UIzoomLevel), (int) (85 * UIzoomLevel), (int) (player.stamina * 20 * UIzoomLevel), (int) (15 * UIzoomLevel));
		buffer.setColor(Color.red);

		// draw costs of selected (aimed) power
		if (hotkeySelected != -1)
		{
			buffer.setColor(new Color(30, 90, 255));
			buffer.fillRect((int) (20 * UIzoomLevel), (int) (62 * UIzoomLevel), (int) (player.mana * 20 * UIzoomLevel), (int) (12 * UIzoomLevel));
			Ability ability = player.abilities.get(player.hotkeys[hotkeySelected]);
			double cost = -1;
			if (ability.costPerSecond > 0)
				cost = ability.costPerSecond;
			if (ability.cost > 0)
				cost = ability.cost;
			if (ability.on)
				if (ability.costPerSecond > 0) // is more important than initial cost
					cost = ability.costPerSecond;
			if (cost > 0)
				switch (ability.costType)
				{
				case "mana":
					for (int i = 1; i < player.mana / cost; i++)
					{
						// darker rectangle
						if (i % 2 == 0)
							buffer.setColor(new Color(0, 0, 220));
						else
							buffer.setColor(new Color(0, 0, 255));
						buffer.fillRect((int) (20 * UIzoomLevel + player.mana * 20 * UIzoomLevel) - i * (int) (cost * 20 * UIzoomLevel) + 1, (int) (60 * UIzoomLevel) + 2,
								(int) (cost * 20 * UIzoomLevel) - 1, (int) (12 * UIzoomLevel));
						// separating line
						buffer.setColor(new Color(0, 0, 140));
						buffer.fillRect((int) (20 * UIzoomLevel + player.mana * 20 * UIzoomLevel) - i * (int) (cost * 20 * UIzoomLevel), (int) (60 * UIzoomLevel + 1), 2, (int) (13 * UIzoomLevel));
					}
					break;
				case "stamina":
					for (int i = 1; i < player.stamina / cost; i++)
					{
						// darker rectangle
						if (i % 2 == 0)
							buffer.setColor(new Color(0, 220, 0));
						else
							buffer.setColor(new Color(0, 255, 0));
						buffer.fillRect((int) ((int) (20 * UIzoomLevel + player.stamina * 20 * UIzoomLevel) - i * (int) (cost * 20 * UIzoomLevel) + 1 * UIzoomLevel), (int) (87 * UIzoomLevel),
								(int) (cost * 20 * UIzoomLevel - 1 * UIzoomLevel), (int) (12 * UIzoomLevel));
						// separating line
						buffer.setColor(new Color(0, 140, 0));
						buffer.fillRect((int) (20 * UIzoomLevel + player.stamina * 20 * UIzoomLevel) - i * (int) (cost * 20 * UIzoomLevel), (int) (86 * UIzoomLevel), (int) (2 * UIzoomLevel),
								(int) (13 * UIzoomLevel));
					}
					break;
				default:
					errorMessage("ability costs more than 0 but there's no case for its cost type - " + ability.costType);
					break;
				}
		}
		// outlines for bars
		buffer.setStroke(new BasicStroke((float) (3 * UIzoomLevel)));
		buffer.setColor(Color.black);
		buffer.drawRect((int) (20 * UIzoomLevel), (int) (35 * UIzoomLevel), (int) (player.maxLife * 2 * UIzoomLevel), (int) (15 * UIzoomLevel));
		buffer.drawRect((int) (20 * UIzoomLevel), (int) (60 * UIzoomLevel), (int) (player.maxMana * 20 * UIzoomLevel), (int) (15 * UIzoomLevel));
		buffer.drawRect((int) (20 * UIzoomLevel), (int) (85 * UIzoomLevel), (int) (player.maxStamina * 20 * UIzoomLevel), (int) (15 * UIzoomLevel));
		buffer.setStroke(new BasicStroke((float) (1 * UIzoomLevel)));
		buffer.setColor(Color.red);
		buffer.drawRect((int) (20 * UIzoomLevel), (int) (35 * UIzoomLevel), (int) (player.maxLife * 2 * UIzoomLevel), (int) (15 * UIzoomLevel));
		buffer.setColor(Color.blue);
		buffer.drawRect((int) (20 * UIzoomLevel), (int) (60 * UIzoomLevel), (int) (player.maxMana * 20 * UIzoomLevel), (int) (15 * UIzoomLevel));
		buffer.setColor(Color.green);
		buffer.drawRect((int) (20 * UIzoomLevel), (int) (85 * UIzoomLevel), (int) (player.maxStamina * 20 * UIzoomLevel), (int) (15 * UIzoomLevel));

		// Fly-mode height meter
		// not logarithmic!
		if (player.z > 0)
		{
			buffer.setStroke(new BasicStroke(2));
			buffer.setColor(new Color(0, 0, 30));
			buffer.drawLine((int) (frameWidth - 80 * UIzoomLevel), (int) (frameHeight / 2 + 300 * UIzoomLevel), (int) (frameWidth - 80 * UIzoomLevel), (int) (frameHeight / 2 - 300 * UIzoomLevel));
			buffer.drawLine((int) (frameWidth - 80 * UIzoomLevel - 20 * UIzoomLevel), (int) (frameHeight / 2 + 300 * UIzoomLevel), (int) (frameWidth - 60 * UIzoomLevel),
					(int) (frameHeight / 2 + 300 * UIzoomLevel));
			buffer.setFont(new Font("Monospaced", Font.BOLD, (int) (12 * UIzoomLevel)));
			for (int i = 0; i < 1000;)
			{
				buffer.drawLine((int) (frameWidth - 92 * UIzoomLevel), (int) (frameHeight / 2 + 300 * UIzoomLevel - 5 * i * UIzoomLevel), (int) (frameWidth - 68 * UIzoomLevel),
						(int) (frameHeight / 2 + 300 * UIzoomLevel - 5 * i * UIzoomLevel));
				buffer.drawString("" + i, (int) (frameWidth - 48 * UIzoomLevel - 7 * (("" + i).length() - 1) * UIzoomLevel),
						(int) ((int) (frameHeight / 2 + 300 * UIzoomLevel - 5 * i * UIzoomLevel) + 4 * UIzoomLevel));
				if (i < 10)
					i += 2;
				else if (i < 50)
					i += 5;
				else if (i < 100)
					i += 10;
				else if (i < 1000)
					i += 100;
			}
			buffer.setFont(new Font("Monospaced", Font.BOLD, (int) (20 * UIzoomLevel)));
			buffer.drawString("" + (int) (player.z), (int) (frameWidth - 85 * UIzoomLevel - 6 * (("" + (int) (player.z)).length() - 1) * UIzoomLevel),
					(int) (frameHeight / 2 + 300 * UIzoomLevel + 25 * UIzoomLevel));
			buffer.setStroke(new BasicStroke((float) (1 * UIzoomLevel)));
			buffer.setColor(Color.cyan);
			buffer.fillOval((int) (frameWidth - 86 * UIzoomLevel), (int) (frameHeight / 2 + 300 * UIzoomLevel - 5 * player.z * UIzoomLevel - 6 * UIzoomLevel), (int) (12 * UIzoomLevel),
					(int) (12 * UIzoomLevel));
			buffer.setColor(Color.black);
			buffer.drawOval((int) (frameWidth - 86 * UIzoomLevel), (int) (frameHeight / 2 + 300 * UIzoomLevel - 5 * player.z * UIzoomLevel - 6 * UIzoomLevel), (int) (12 * UIzoomLevel),
					(int) (12 * UIzoomLevel));
		}

		//
		buffer.translate(-8, -30);
	}

	void drawHotkeysAndEffects(Graphics2D buffer)
	{
		buffer.setFont(new Font("Sans-Serif", Font.BOLD, (int) (12 * UIzoomLevel)));
		frc = buffer.getFontRenderContext();
		for (int i = 0; i < player.hotkeys.length; i++)
		{
			buffer.setStroke(new BasicStroke((float) (3 * UIzoomLevel)));
			buffer.setColor(Color.black);
			buffer.drawString(hotkeyStrings[i], (int) (42 * UIzoomLevel + i * 80 * UIzoomLevel), (int) (frameHeight - 14 * UIzoomLevel));
			if (player.hotkeys[i] != -1)
			{
				Ability ability = player.abilities.get(player.hotkeys[i]);
				buffer.drawImage(Resources.icons.get(ability.name), (int) (30 * UIzoomLevel + i * 80 * UIzoomLevel), (int) (frameHeight - 90 * UIzoomLevel), this);
				buffer.drawRect((int) (30 * UIzoomLevel + i * 80 * UIzoomLevel), (int) (frameHeight - 90 * UIzoomLevel), (int) (60 * UIzoomLevel), (int) (60 * UIzoomLevel));

				// Cooldown and mana notifications
				if (ability.cooldownLeft != 0)
				{// note that when the cooldown is over it will "jump" from low transparency to full transparency
					buffer.setColor(new Color(0, 0, 0, (int) (130 + 100 * ability.cooldownLeft / ability.cooldown)));
					buffer.fillRect((int) (31 * UIzoomLevel + i * 80 * UIzoomLevel), (int) (frameHeight - 89 * UIzoomLevel), (int) (59 * UIzoomLevel), (int) (59 * UIzoomLevel));
				}
				if (ability.cost > player.mana)
				{
					if (ability.justName().equals("Pool") && ability.cost - 1.5 <= player.mana)
						buffer.setColor(Color.yellow); // can only build low-cost pools next to other stuffs
					else if (ability.justName().equals("Wall") && 0.3 <= player.mana)
						buffer.setColor(Color.yellow); // repairing walls
					else
						buffer.setColor(Color.red);
					buffer.drawRect((int) (27 * UIzoomLevel + i * 80 * UIzoomLevel), (int) (frameHeight - 93 * UIzoomLevel), (int) (66 * UIzoomLevel), (int) (66 * UIzoomLevel));
				}

				// ON/OFF
				if (ability.on)
				{
					buffer.setColor(Color.cyan);
					buffer.setStroke(new BasicStroke(2));
					buffer.drawRect((int) (31 * UIzoomLevel + i * 80 * UIzoomLevel), (int) (frameHeight - 89 * UIzoomLevel), (int) (59 * UIzoomLevel), (int) (59 * UIzoomLevel));
				}

				// current power
				if (player.hotkeys[i] == player.abilityAiming || player.hotkeys[i] == player.abilityMaintaining || player.hotkeys[i] == player.abilityTryingToRepetitivelyUse)
				{
					buffer.setColor(Color.green);
					buffer.setStroke(new BasicStroke(2));
					buffer.drawRect((int) (31 * UIzoomLevel + i * 80 * UIzoomLevel), (int) (frameHeight - 89 * UIzoomLevel), (int) (59 * UIzoomLevel), (int) (59 * UIzoomLevel));
				}

				// selected power for targeting
				if (i == hotkeySelected)
				{
					buffer.setColor(Color.cyan);
					buffer.fillRect((int) (31 * UIzoomLevel + i * 80 * UIzoomLevel), (int) (frameHeight - 28 * UIzoomLevel), (int) (58 * UIzoomLevel), (int) (4 * UIzoomLevel)); // bottom
					buffer.fillRect((int) (31 * UIzoomLevel + i * 80 * UIzoomLevel), (int) (frameHeight - 95 * UIzoomLevel), (int) (58 * UIzoomLevel), (int) (4 * UIzoomLevel)); // top
					buffer.fillRect((int) (25 * UIzoomLevel + i * 80 * UIzoomLevel), (int) (frameHeight - 91 * UIzoomLevel), (int) (4 * UIzoomLevel), (int) (58 * UIzoomLevel)); // left
					buffer.fillRect((int) (92 * UIzoomLevel + i * 80 * UIzoomLevel), (int) (frameHeight - 91 * UIzoomLevel), (int) (4 * UIzoomLevel), (int) (58 * UIzoomLevel)); // right
				}

				// selected power during tab
				if (tab && (player.hotkeys[i] == tabHoverAbility || i == hotkeyHovered))
				{
					buffer.setStroke(new BasicStroke(1));
					buffer.setColor(Color.yellow);
					buffer.drawRect((int) (30 * UIzoomLevel + i * 80 * UIzoomLevel), (int) (frameHeight - 90 * UIzoomLevel), (int) (60 * UIzoomLevel), (int) (60 * UIzoomLevel));
				}
			} else
			// no power in that space
			{
				buffer.setStroke(dashedStroke3);
				buffer.setColor(new Color(0, 0, 0, 90));
				buffer.drawRect((int) (30 * UIzoomLevel + i * 80 * UIzoomLevel), (int) (frameHeight - 90 * UIzoomLevel), (int) (60 * UIzoomLevel), (int) (60 * UIzoomLevel));
			}
			// remember - black rectangle after icon
		}

		buffer.setStroke(new BasicStroke((float) (3 * UIzoomLevel)));
		buffer.setColor(Color.black);
		// effect icons
		for (int i = 0; i < player.effects.size(); i++)
		{
			buffer.drawImage(Resources.icons.get(player.effects.get(i).name), (int) (frameWidth - 90 * UIzoomLevel - i * 80 * UIzoomLevel), (int) (frameHeight - 90 * UIzoomLevel), this);
			buffer.drawRect((int) (frameWidth - 90 * UIzoomLevel - i * 80 * UIzoomLevel), (int) (frameHeight - 90 * UIzoomLevel), (int) (60 * UIzoomLevel), (int) (60 * UIzoomLevel));
		}

		// tooltip
		if (tooltipPoint.x != -1) // can also check y but that's silly
		{
			buffer.setColor(Color.black);
			buffer.setFont(new Font("Serif", Font.PLAIN, (int) (20 * UIzoomLevel)));
			int i = tooltip.indexOf("\n");
			if (i != -1) // if extended tooltip
			{
				buffer.drawString(tooltip.substring(0, i), tooltipPoint.x, tooltipPoint.y);
				buffer.setFont(new Font("Serif", Font.ITALIC, 20));
				buffer.drawString(tooltip.substring(i + 1), tooltipPoint.x - 8, tooltipPoint.y + 25);
			} else
				buffer.drawString(tooltip, tooltipPoint.x, tooltipPoint.y);
		}
	}

	void drawTab(Graphics2D buffer)
	{
		// Should only be called if tab == true

		// Cover screen with dark transparent rectangle
		buffer.setColor(new Color(0, 0, 0, 40));
		buffer.fillRect(0, 0, frameWidth, frameHeight);

		// "PAUSED"
		Font pausedFont = new Font("Serif", Font.PLAIN, (int) (100 * UIzoomLevel));
		buffer.setFont(pausedFont);
		scaleBuffer(buffer, frameWidth / 2, frameHeight / 4, UIzoomLevel);
		buffer.drawString("~PAUSED~", frameWidth / 2 - (int) (230 * UIzoomLevel), frameHeight / 4 + (int) (20 * UIzoomLevel));
		scaleBuffer(buffer, frameWidth / 2, frameHeight / 4, 1 / UIzoomLevel);

		int rectStartX = (int) (frameWidth / 2 - player.abilities.size() * 80 / 2 * UIzoomLevel);
		int rectStartY = (int) (frameHeight * 3 / 4);
		int rectWidth = (int) (player.abilities.size() * 80 * UIzoomLevel);
		int extraUp = screenmx > rectStartX - 50 * UIzoomLevel && screenmx < rectStartX + rectWidth + 130 * UIzoomLevel && screenmy > rectStartY - 70 * UIzoomLevel
				&& screenmy < rectStartY + 100 * UIzoomLevel ? 0 : (int) (-40 * UIzoomLevel);

		// Ability breakdown rectangle
		buffer.setColor(new Color(255, 255, 255, 130));
		buffer.setStroke(new BasicStroke(1));
		buffer.fillRect((int) (rectStartX - 50 * UIzoomLevel), (int) (rectStartY - 70 * UIzoomLevel - extraUp), (int) (rectWidth + 80 * UIzoomLevel), (int) (UIzoomLevel * 170 + extraUp));
		buffer.setColor(new Color(0, 0, 0));
		buffer.drawRect((int) (rectStartX - 50 * UIzoomLevel), (int) (rectStartY - 70 * UIzoomLevel - extraUp), (int) (rectWidth + 80 * UIzoomLevel), (int) (UIzoomLevel * 170 + extraUp));
		// // Waste of time, really:
		// buffer.setStroke(new BasicStroke(8));
		// buffer.drawLine(rectStartX - 50, rectStartY - 70 - extraUp, rectStartX - 20, rectStartY - 70 - extraUp);
		// buffer.drawLine(rectStartX - 50, rectStartY - 70 - extraUp, rectStartX - 50, rectStartY - 40 - extraUp);
		// buffer.drawLine(rectStartX - 50, rectStartY + 100, rectStartX - 50, rectStartY + 70);
		// buffer.drawLine(rectStartX - 50, rectStartY + 100, rectStartX - 20, rectStartY + 100);
		// buffer.drawLine(rectStartX + rectWidth - 50 - 20 + 100, rectStartY + 100, rectStartX + rectWidth - 50 - 20 + 70, rectStartY + 100);
		// buffer.drawLine(rectStartX + rectWidth - 50 - 20 + 100, rectStartY + 100, rectStartX + rectWidth - 50 - 20 + 100, rectStartY + 70);
		// buffer.drawLine(rectStartX + rectWidth - 50 - 20 + 100, rectStartY - 70 - extraUp, rectStartX + rectWidth - 50 - 20 + 70, rectStartY - 70 - extraUp);
		// buffer.drawLine(rectStartX + rectWidth - 50 - 20 + 100, rectStartY - 70 - extraUp, rectStartX + rectWidth - 50 - 20 + 100, rectStartY - 40 - extraUp);

		buffer.setFont(new Font("Sans-Serif", Font.BOLD, (int) (12 * UIzoomLevel)));
		for (int i = 0; i < player.abilities.size(); i++)
		{
			Ability ability = player.abilities.get(i);
			if (ability.cost == -1)
			{
				buffer.setStroke(new BasicStroke((float) (3 * UIzoomLevel), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, (float) (10.0f), new float[]
				{ (float) (10.0f * UIzoomLevel) }, 0.0f));
				buffer.setColor(new Color(0, 0, 0, 90));
			} else
			{
				buffer.setStroke(new BasicStroke((float) (3 * UIzoomLevel)));
				buffer.setColor(Color.black);
			}
			// icons
			scaleBuffer(buffer, (int) (rectStartX + i * 80 * UIzoomLevel + 0 * UIzoomLevel), (int) (rectStartY + 0 * UIzoomLevel), UIzoomLevel);
			buffer.drawImage(Resources.icons.get(ability.name), (int) (rectStartX + i * 80 * UIzoomLevel), (int) (rectStartY), this);
			scaleBuffer(buffer, (int) (rectStartX + i * 80 * UIzoomLevel + 0 * UIzoomLevel), (int) (rectStartY + 0 * UIzoomLevel), 1 / UIzoomLevel);
			buffer.drawRect((int) (rectStartX + i * 80 * UIzoomLevel), rectStartY, (int) (60 * UIzoomLevel), (int) (60 * UIzoomLevel));
			if (i == tabHoverAbility || (hotkeyHovered != -1 && i == player.hotkeys[hotkeyHovered]))
			{
				buffer.setStroke(new BasicStroke(1));
				buffer.setColor(Color.yellow);
				buffer.drawRect((int) (frameWidth / 2 - player.abilities.size() * 80 / 2 * UIzoomLevel + i * 80 * UIzoomLevel), frameHeight * 3 / 4, (int) (60 * UIzoomLevel),
						(int) (60 * UIzoomLevel));
			}
			// Key bound to that ability
			int timesAssigned = 0;
			for (int j = 0; j < player.hotkeys.length; j++)
				if (player.hotkeys[j] == i)
				{
					timesAssigned++;
					buffer.drawString(hotkeyStrings[j], (int) (rectStartX + i * 80 * UIzoomLevel + 12 * UIzoomLevel), (int) (rectStartY + 60 * UIzoomLevel + timesAssigned * 16 * UIzoomLevel));
				}
		}

		// TODO add some funny stuff. Also, menu buttons.
	}

	void checkPlayerMovementKeys()
	{
		double horiAccel = 0, vertAccel = 0;
		if (player.upPressed)
			vertAccel--;
		if (player.leftPressed)
			horiAccel--;
		if (player.downPressed)
			vertAccel++;
		if (player.rightPressed)
			horiAccel++;
		if (horiAccel == 0 && vertAccel == 0)
		{
			player.strengthOfAttemptedMovement = 0;
			player.flyDirection = 0;
		} else
		{
			player.strengthOfAttemptedMovement = 1;
			player.directionOfAttemptedMovement = Math.atan2(vertAccel, horiAccel) + cameraRotation;

			if (player.spacePressed && !player.ctrlPressed)
				player.flyDirection = 1;
			else if (!player.spacePressed && player.ctrlPressed)
				player.flyDirection = -1;
			else
				player.flyDirection = 0;

			if (!(player.abilityTryingToRepetitivelyUse != -1 && player.abilities.get(player.abilityTryingToRepetitivelyUse).justName().equals("Punch")))
			{
				if (!player.notAnimating)
					player.rotate(player.directionOfAttemptedMovement, globalDeltaTime);
			} else
			// fly-punch users can't really stop...or aim themselves...
			if (player.strengthOfAttemptedMovement != 0)
				player.rotate(player.directionOfAttemptedMovement, globalDeltaTime * 0.3);
		}
	}

	void checkMovementAttempt(Person p, double friction, double deltaTime)
	{
		if (Double.isNaN(p.directionOfAttemptedMovement))
		{
			// Not OK
			errorMessage("Person " + p.id + "'s direction of movement is NaN. This is not OK.");
			return;
		}
		// if the person is attempting to stand still
		if (!p.prone && p.strengthOfAttemptedMovement == 0)
		{
			if (p.z == 0 && p.flySpeed == -1)
			{
				if (!p.panic)
				{
					if (!p.notAnimating)
						p.switchAnimation(0);
					if (p.abilityTryingToRepetitivelyUse != -1 && p.abilities.get(p.abilityTryingToRepetitivelyUse).justName().equals("Punch")
							&& p.abilities.get(p.abilityTryingToRepetitivelyUse).cooldownLeft == 0 && p.abilities.get(p.abilityTryingToRepetitivelyUse).cost > p.stamina)
					{
						p.switchAnimation(1);
						p.switchAnimation(0);
					}
					return;
				} else
				{
					// panicked people can't stop running!
					p.strengthOfAttemptedMovement = 1;
					p.directionOfAttemptedMovement = p.rotation; // Only changes it to that value when p isn't trying to move, so it's not bad
				}
			} else if (p.z != 0 && p.flySpeed != -1 && !(p.abilityTryingToRepetitivelyUse != -1 && p.abilities.get(p.abilityTryingToRepetitivelyUse).justName().equals("Punch")))
			{
				p.switchAnimation(9); // slowing down / hover animation
				// glide down slowly
				if (p.xVel * p.xVel + p.yVel * p.yVel < p.flySpeed * p.flySpeed * 0.05)
					p.zVel = -0.3 * p.flySpeed * 5 * deltaTime;
				else
					p.zVel = 0;
			} else if (p.z == 1 && p.flySpeed == -1)
			{
				if (!p.panic)
				{
					if (!p.notAnimating)
						p.switchAnimation(0);
					if (p.abilityTryingToRepetitivelyUse != -1 && p.abilities.get(p.abilityTryingToRepetitivelyUse).justName().equals("Punch")
							&& p.abilities.get(p.abilityTryingToRepetitivelyUse).cooldownLeft == 0 && p.abilities.get(p.abilityTryingToRepetitivelyUse).cost > p.stamina)
					{
						p.switchAnimation(1);
						p.switchAnimation(0);
					}
					return;
				} else
				{
					// panicked people can't stop running!
					p.strengthOfAttemptedMovement = 1;
					p.directionOfAttemptedMovement = p.rotation; // Only changes it to that value when p isn't trying to move, so it's not bad
				}
			}
		}
		// can't move or auto-rotate to movement direction while fisting
		if (p.notMoving) // should never be happening when in the air
			return;
		// A very specific fix for a case. TODO fix this mess one day (See what happens when you punch with minimum amount of stamina, while holding a movement key)
		if (p.abilityTryingToRepetitivelyUse != -1 && p.abilities.get(p.abilityTryingToRepetitivelyUse).justName().equals("Punch")
				&& p.abilities.get(p.abilityTryingToRepetitivelyUse).cooldownLeft == 0 && p.abilities.get(p.abilityTryingToRepetitivelyUse).cost > p.stamina)
		{
			p.switchAnimation(1);
			p.switchAnimation(0);
			return;
		}
		// Okay, get ready
		if (!p.prone)
		{
			if (!(p.abilityTryingToRepetitivelyUse != -1 && p.abilities.get(p.abilityTryingToRepetitivelyUse).justName().equals("Punch")))
			{// if not punching
				if (p.strengthOfAttemptedMovement != 0)
				{
					if (p.flySpeed != -1)
					// flight abilities
					{
						// fly ahead
						if (p.xVel * p.xVel + p.yVel * p.yVel < p.flySpeed * p.flySpeed)
						{
							p.xVel += Math.cos(p.directionOfAttemptedMovement) * deltaTime * 100 * p.strengthOfAttemptedMovement * p.runAccel / 100;
							p.yVel += Math.sin(p.directionOfAttemptedMovement) * deltaTime * 100 * p.strengthOfAttemptedMovement * p.runAccel / 100;
						}
						// ascend
						if (player.spacePressed && !player.ctrlPressed)
							if (p.z < (double) (p.flySpeed * 0.1)) // max limit
								p.zVel = p.flySpeed * 5 * deltaTime;
							else
								p.zVel = 0;
						if (!player.spacePressed && player.ctrlPressed)
							p.zVel = -p.flySpeed * 5 * deltaTime;
						if (player.spacePressed == player.ctrlPressed) // stay floating at max height
							p.zVel = 0;
						if (player.rightMousePressed) // slower ascent/descent
							p.zVel *= 0.25;
						p.switchAnimation(7);
					} else if (p.z == 0 || p.z == 1) // walking on ground or walking on walls
					{
						// making sure dude has enough stamina
						if (p.runningStaminaCost * deltaTime * p.strengthOfAttemptedMovement > p.stamina)
							p.strengthOfAttemptedMovement = p.stamina / (p.runningStaminaCost * deltaTime);
						if (movementVariation)
							p.strengthOfAttemptedMovement *= 1 - 0.6 * Math.abs(Math.sin((p.directionOfAttemptedMovement - p.rotation) / 2)); // backwards = 0.4, forwards = 1, sideways = 0.6 (roughly)
						if (Math.sqrt(Math.abs(p.xVel * p.xVel + p.yVel * p.yVel)) < p.runSpeed * 100 / friction) // TODO change from using math.sqrt to just comparing squared values?
						{
							p.xVel += Math.cos(p.directionOfAttemptedMovement) * deltaTime * p.strengthOfAttemptedMovement * p.runAccel * friction / 100;
							p.yVel += Math.sin(p.directionOfAttemptedMovement) * deltaTime * p.strengthOfAttemptedMovement * p.runAccel * friction / 100;
						}
						// STAMINA COST FOR RUNNING IS IN HERE
						p.stamina -= p.runningStaminaCost * deltaTime * p.strengthOfAttemptedMovement;
						// switch to running animation
						if (!p.notAnimating)
							p.switchAnimation(1);
					} else// freefalling?
					{
						if (Math.sqrt(Math.abs(p.xVel * p.xVel + p.yVel * p.yVel)) < 300) // TODO change from using math.sqrt to just comparing squared values?
						{
							p.xVel += Math.cos(p.directionOfAttemptedMovement) * deltaTime * p.strengthOfAttemptedMovement * 20;
							p.yVel += Math.sin(p.directionOfAttemptedMovement) * deltaTime * p.strengthOfAttemptedMovement * 20;
						}
						// TODO animation for falling that isn't the same as when not falling
					}
				}
			} else // if punching
			{
				p.strengthOfAttemptedMovement = 0.5; // TODO why not 0?
				p.directionOfAttemptedMovement = p.rotation;

				// air-punching (trying to punch while flying)
				if (p.flySpeed != -1)
				{
					if (Math.sqrt(Math.abs(p.xVel * p.xVel + p.yVel * p.yVel)) < p.flySpeed) // TODO change from using math.sqrt to just comparing squared values?
					{
						p.xVel += Math.cos(p.directionOfAttemptedMovement) * deltaTime * 100 * p.strengthOfAttemptedMovement * p.runAccel / 100;
						p.yVel += Math.sin(p.directionOfAttemptedMovement) * deltaTime * 100 * p.strengthOfAttemptedMovement * p.runAccel / 100;
					}

					if (p.z < 1.1 && p.z > 0.6)
					{
						p.zVel = 0;
						p.z = 1.1;
					}
					if (p.z > 5)
						p.zVel = -0.7 * p.flySpeed * 5 * deltaTime; // glide down
					else if (p.z > 1.1) // to avoid weird flickering
						p.zVel = -0.2 * p.flySpeed * 5 * deltaTime; // glide down
					if (p.z < 0.6)
						p.zVel = 0.7 * p.flySpeed * 5 * deltaTime; // glide...up
				}
			}
		}
	}

	void fluffLinesBind(List<List<String>> fluffLines, List<Ability> abilities, int bound)
	{
		fluffLines = new ArrayList<List<String>>();
		for (int i = 0; i < abilities.size(); i++)
		{
			fluffLines.add(new ArrayList<String>());
			String fluff = abilities.get(i).getFluff();
			if (fluff.length() <= bound + 1)
				fluffLines.get(i).add(fluff);
			else
				for (int j = 0; j < fluff.length();)
					if (j >= fluff.length() - bound)
					{
						fluffLines.get(i).add(fluff.substring(j));
						j = fluff.length();
					} else
					{
						fluffLines.get(i).add(fluff.substring(j, fluff.lastIndexOf(" ", bound + j) + 1));
						j = fluff.lastIndexOf(" ", bound + j) + 1;
					}
		}
	}

	void reroll(List<Ability> abilities, List<List<String>> fluffLines)
	{
		List<EP> EPs = EPgenerator.generateEPs();
		abilities = PowerGenerator.generateAbilities(EPs);
		fluffLinesBind(fluffLines, abilities, 68);
	}

	void rerollPowers(List<EP> EPs, List<Ability> abilities, List<List<String>> fluffLines)
	{
		abilities = PowerGenerator.generateAbilities(EPs);
		fluffLines = new ArrayList<List<String>>();
		for (int i = 0; i < abilities.size(); i++)
		{
			fluffLines.add(new ArrayList<String>());
			String fluff = abilities.get(i).getFluff();
			if (fluff.length() <= 73)
				fluffLines.get(i).add(fluff);
			else
				for (int j = 0; j < fluff.length();)
				{
					if (j >= fluff.length() - 72)
					{
						fluffLines.get(i).add(fluff.substring(j));
						j += 72;
					} else
					{
						fluffLines.get(i).add(fluff.substring(j, fluff.lastIndexOf(" ", 72) + 1));
						j += fluff.lastIndexOf(" ", 72 + j) + 1;
					}
				}
		}
	}

	void scaleBuffer(Graphics2D buffy, int xCenter, int yCenter, double amount)
	{
		buffy.translate(xCenter, yCenter);
		buffy.scale(amount, amount);
		buffy.translate(-xCenter, -yCenter);
	}

	public void keyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{ // TODO sort to development-only keys
		case KeyEvent.VK_BACK_SPACE:// Restart
			restart();
			break;
		case KeyEvent.VK_ESCAPE:// Exit
			System.exit(0);
			break;
		case KeyEvent.VK_A:
			player.leftPressed = true;
			break;
		case KeyEvent.VK_D:
			player.rightPressed = true;
			break;
		case KeyEvent.VK_W:
			player.upPressed = true;
			break;
		case KeyEvent.VK_S:
			player.downPressed = true;
			break;
		case KeyEvent.VK_1:
			env.addWall((mx) / 96, (my) / 96, 10, true);
			break;
		case KeyEvent.VK_2:
			env.addWall((mx) / 96, (my) / 96, 5, true);
			break;
		case KeyEvent.VK_3:
			env.addPool((mx) / 96, (my) / 96, 10, true);
			break;
		case KeyEvent.VK_4:
			env.addPool((mx) / 96, (my) / 96, 7, true);
			break;
		case KeyEvent.VK_5:
			env.addPool((mx) / 96, (my) / 96, 8, true);
			break;
		case KeyEvent.VK_6:
			env.addPool((mx) / 96, (my) / 96, 5, true);
			break;
		case KeyEvent.VK_0:
			if (player.ctrlPressed)
				zoomLevel = 1;
			else if (player.rotateButtonPressed)
				cameraRotation = 0;
			else if (player.resizeUIButtonPressed)
				UIzoomLevel = 1;
			else
				env.remove((mx) / 96, (my) / 96);
			break;
		case KeyEvent.VK_K:
			for (Person p : env.people)
				p.initAnimation();
			break;
		case KeyEvent.VK_CONTROL:
			player.ctrlPressed = true;
			break;
		case KeyEvent.VK_ALT:
			player.resizeUIButtonPressed = true;
			break;
		case KeyEvent.VK_SPACE:
			player.spacePressed = true;
			break;
		case KeyEvent.VK_O:
			player.rotateButtonPressed = true;
			break;
		case KeyEvent.VK_TAB:
			tab = true;
			checkDisplayHotkeyPowers();
			break;

		// hotkeys 1, 2, 3....10
		// (Right-Click), Shift, Q, E, R, F, V, C, X, Z
		case KeyEvent.VK_SHIFT:
			playerPressHotkey(2, true);
			break;
		case KeyEvent.VK_Q:
			playerPressHotkey(3, true);
			break;
		case KeyEvent.VK_E:
			playerPressHotkey(4, true);
			break;
		case KeyEvent.VK_R:
			playerPressHotkey(5, true);
			break;
		case KeyEvent.VK_F:
			playerPressHotkey(6, true);
			break;
		case KeyEvent.VK_V:
			playerPressHotkey(7, true);
			break;
		case KeyEvent.VK_C:
			playerPressHotkey(8, true);
			break;
		case KeyEvent.VK_X:
			playerPressHotkey(9, true);
			break;
		case KeyEvent.VK_Z:
			playerPressHotkey(10, true);
			break;

		case KeyEvent.VK_F1:
			env.devMode = !env.devMode;
			break;
		case KeyEvent.VK_F2:
			env.showDamageNumbers = !env.showDamageNumbers;
			break;
		case KeyEvent.VK_F12:
			if (timeSinceLastScreenshot > 0.1)
				try
				{
					Date date = new Date();
					timeSinceLastScreenshot = 0;
					File file = new File("screenshot " + dateFormat.format(date) + ".png");
					ImageIO.write((RenderedImage) bufferImage, "png", file);
					lastScreenshot = ImageIO.read(file);
				} catch (IOException e1)
				{
					// TODO Auto-generated catch block

				}
			break;
		default:
			// errorMessage("Unused key was pressed: " + KeyEvent.getKeyText(e.getKeyCode()));
			break;
		}
	}

	public void keyReleased(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
		case KeyEvent.VK_A:
			player.leftPressed = false;
			break;
		case KeyEvent.VK_D:
			player.rightPressed = false;
			break;
		case KeyEvent.VK_W:
			player.upPressed = false;
			break;
		case KeyEvent.VK_S:
			player.downPressed = false;
			break;
		case KeyEvent.VK_CONTROL:
			player.ctrlPressed = false;
			break;
		case KeyEvent.VK_ALT:
			player.resizeUIButtonPressed = false;
			break;
		case KeyEvent.VK_SPACE:
			player.spacePressed = false;
			break;
		case KeyEvent.VK_O:
			player.rotateButtonPressed = false;
			break;
		case KeyEvent.VK_TAB:
			tab2 = !tab2;
			tab = !tab2;
			tooltip = "";
			if (!tab)
				tabHoverAbility = -1;
			break;

		case KeyEvent.VK_SHIFT:
			playerPressHotkey(2, false);
			break;
		case KeyEvent.VK_Q:
			playerPressHotkey(3, false);
			break;
		case KeyEvent.VK_E:
			playerPressHotkey(4, false);
			break;
		case KeyEvent.VK_R:
			playerPressHotkey(5, false);
			break;
		case KeyEvent.VK_F:
			playerPressHotkey(6, false);
			break;
		case KeyEvent.VK_V:
			playerPressHotkey(7, false);
			break;
		case KeyEvent.VK_C:
			playerPressHotkey(8, false);
			break;
		case KeyEvent.VK_X:
			playerPressHotkey(9, false);
			break;
		case KeyEvent.VK_Z:
			playerPressHotkey(10, false);
			break;
		default:
			// errorMessage("Unused key was released: " + KeyEvent.getKeyText(e.getKeyCode()));
			break;
		}
	}

	public Main()
	{
		frameWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		frameHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

		restart();
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent we)
			{
				System.exit(0);
			}
		});
		this.addMouseWheelListener(this);
		this.setSize(frameWidth, frameHeight);
		this.setVisible(true);
		this.setResizable(true);
		this.setFocusTraversalKeysEnabled(false);
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addComponentListener(this);
		addWindowFocusListener(this);
		frameTimer = new Timer(frameTimerDelay, frameListener);
		frameTimer.setInitialDelay(0);
		frameTimer.start();
	}

	// IGNORE
	ActionListener frameListener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			if (!tab)
				frame(); // extra method is because the actionlistener {{}}; thingie is buggy in Eclipse
			else
				tabFrame();
			// repaint
			repaint();
		}
	};

	// IGNORE
	private void adjustbuffer()
	{
		// getting image size
		bufferWidth = getSize().width;
		bufferHeight = getSize().height;

		// clean buffered image
		if (bufferGraphics != null)
		{
			bufferGraphics.dispose();
			bufferGraphics = null;
		}
		if (bufferImage != null)
		{
			bufferImage.flush();
			bufferImage = null;
		}
		System.gc(); // Garbage cleaner

		// create the new image with the size of the panel
		bufferImage = createImage(bufferWidth, bufferHeight);
		bufferGraphics = bufferImage.getGraphics();
	}

	// IGNORE
	public void update(Graphics g)
	{
		paint(g);
	}

	// IGNORE
	public void paint(Graphics g)
	{
		// Resetting the buffered Image
		if (bufferWidth != getSize().width || bufferHeight != getSize().height || bufferImage == null || bufferGraphics == null)
			adjustbuffer();

		if (bufferGraphics != null)
		{
			// this clears the offscreen image, not the onscreen one
			bufferGraphics.clearRect(0, 0, bufferWidth, bufferHeight);

			// calls the paintbuffer method with buffergraphics
			paintBuffer(bufferGraphics);

			// painting the buffered image on to the visible frame
			g.drawImage(bufferImage, 0, 0, this);
		}
	}

	// IGNORE
	public void keyTyped(KeyEvent e)
	{

	}

	// IGNORE
	public static void main(String[] args)
	{
		@SuppressWarnings("unused")
		Main main = new Main();
	}

	public void componentResized(ComponentEvent e)
	{
		frameWidth = (int) this.getBounds().getWidth();
		frameHeight = (int) this.getBounds().getHeight();
	}

	public void windowGainedFocus(WindowEvent arg0)
	{

	}

	public void windowLostFocus(WindowEvent arg0)
	{
		tab = true;
		tab2 = false;
		tooltip = "";
	}

	// IGNORE
	public void componentHidden(ComponentEvent e)
	{
	}

	// IGNORE
	public void componentMoved(ComponentEvent e)
	{
	}

	// IGNORE
	public void componentShown(ComponentEvent e)
	{
	}

	// IGNORE
	public void mouseWheelMoved(MouseWheelEvent mwe)
	{
		boolean direction = mwe.getWheelRotation() == 1 ? true : false;
		if (player.ctrlPressed)
		{
			// zoom
			if (direction)
				zoomLevel *= 0.9;
			else
				zoomLevel *= 1.1;
		} else if (player.rotateButtonPressed)
		{
			// rotate
			if (direction)
				cameraRotation += 0.04;
			else
				cameraRotation -= 0.04;
		} else if (player.resizeUIButtonPressed)
		{
			// UI-zoom
			if (direction)
				UIzoomLevel *= 0.9;
			else
				UIzoomLevel *= 1.1;
		} else
		{
			// switch currently range-selected ability
			hotkeySelected += direction ? 1 : -1;
			if (hotkeySelected >= player.numOfHotkeys)
				hotkeySelected = -1;
			if (hotkeySelected < -1)
				hotkeySelected = player.numOfHotkeys - 1;
		}
	}

	// IGNORE
	public void mouseDragged(MouseEvent me)
	{
		// Getting mouse info
		pin = MouseInfo.getPointerInfo();
		mousePoint = pin.getLocation();
		screenmx = (int) (mousePoint.getX() - this.getX());
		screenmy = (int) (mousePoint.getY() - this.getY());

		updateMousePosition();

		checkDisplayHotkeyPowers();
	}

	public void mouseMoved(MouseEvent me)
	{
		// Getting mouse info
		pin = MouseInfo.getPointerInfo();
		mousePoint = pin.getLocation();
		screenmx = (int) (mousePoint.getX() - this.getX());
		screenmy = (int) (mousePoint.getY() - this.getY());

		updateMousePosition();

		checkDisplayHotkeyPowers();
	}

	// IGNORE
	public void updateMousePosition()
	{
		double angleToMouse = Math.atan2(screenmy - frameHeight / 2, screenmx - frameWidth / 2);
		double distanceToMouse = Math.sqrt(Math.pow(screenmy - frameHeight / 2, 2) + Math.pow(screenmx - frameWidth / 2, 2));
		angleToMouse += cameraRotation;
		double newScreenMX = frameWidth / 2 + distanceToMouse * Math.cos(angleToMouse);
		double newScreenMY = frameHeight / 2 + distanceToMouse * Math.sin(angleToMouse);
		mx = (int) ((newScreenMX - 0.5 * frameWidth) * ((camera.z - cameraHeight) * heightZoomRatio + 1) / zoomLevel + camera.x);
		my = (int) ((newScreenMY - 0.5 * frameHeight) * ((camera.z - cameraHeight) * heightZoomRatio + 1) / zoomLevel + camera.y);
	}

	// IGNORE
	public void mouseClicked(MouseEvent me)
	{
	}

	// IGNORE
	public void mouseEntered(MouseEvent me)
	{
	}

	// IGNORE
	public void mouseExited(MouseEvent me)
	{
	}

	public void mousePressed(MouseEvent me)
	{
		// TIP: BUTTON1 = left click, BUTTON2 = mid click (scroll wheel click),
		// BUTTON3 = right click
		// TIP: This will only trigger when you press the mouse, and only once.
		// Unlike the keys it won't repeatedly "click" the mouse again
		if (me.getButton() == MouseEvent.BUTTON1) // Left Click
		{
			player.leftMousePressed = true;
		}
		if (me.getButton() == MouseEvent.BUTTON2) // Mid Click
		{
			playerPressHotkey(1, true);
		}
		if (me.getButton() == MouseEvent.BUTTON3) // Right Click
		{
			player.rightMousePressed = true;
			// view extended hotkey tooltips
			checkDisplayHotkeyPowers();

			// disable aimed ability
			if (!tab)
			{
				if (player.abilityMaintaining != -1)
				{
					if (player.abilityAiming == -1) // trying to stop maintained power
					{
						stopUsingPower = true;
						player.abilities.get(player.abilityMaintaining).use(env, player, new Point(mx, my));
						player.abilityMaintaining = -1;
					} else // trying to stop mid-maintain ability
					{
						stopUsingPower = true;
						player.abilityAiming = -1;
					}
				} else if (player.abilityAiming != -1)
				{
					stopUsingPower = true;
					player.abilityAiming = -1;
				}
			}
		}
	}

	public void mouseReleased(MouseEvent me)
	{
		if (me.getButton() == MouseEvent.BUTTON1) // Left Click
		{
			player.leftMousePressed = false;
		}
		if (me.getButton() == MouseEvent.BUTTON2) // Mid Click
		{
			playerPressHotkey(1, false);
		}
		if (me.getButton() == MouseEvent.BUTTON3) // right Click
		{
			player.rightMousePressed = false;
			checkDisplayHotkeyPowers();
		}
	}

	void checkDisplayHotkeyPowers()
	{
		// both hotkey tooltips and effect tooltips, currently.
		boolean foundOne = false;
		for (int i = 0; i < player.hotkeys.length; i++)
		{
			if (player.hotkeys[i] != -1)
			{
				if (screenmx > 30 + i * 80 && screenmy > frameHeight - 90 && screenmx < 30 + i * 80 + 60 && screenmy < frameHeight - 90 + 60)
				{
					foundOne = true;
					hotkeyHovered = i;
					tooltipPoint = new Point(38 + i * 80, frameHeight - 100);
					tooltip = player.abilities.get(player.hotkeys[i]).niceName();
					if (player.rightMousePressed)
					{
						tooltip += " " + player.abilities.get(player.hotkeys[i]).points + "\n" + player.abilities.get(player.hotkeys[i]).getFluff();
						tooltipPoint.y -= 30;
					}
				}
			}
		}
		for (int i = 0; i < player.effects.size(); i++)
		{
			if (screenmx > frameWidth - 30 - i * 80 - 60 && screenmy > frameHeight - 90 && screenmx < frameWidth - 30 - i * 80 && screenmy < frameHeight - 90 + 60)
			{
				hotkeyHovered = -1;
				foundOne = true;
				int textWidth = (int) (tooltipFont.getStringBounds(player.effects.get(i).name, frc).getWidth());
				tooltipPoint = new Point(frameWidth - i * 80 - 50 - textWidth, frameHeight - 100); // TODO fix this
				tooltip = player.effects.get(i).name;
			}
		}
		if (!foundOne)
		{
			hotkeyHovered = -1;
			tooltipPoint = new Point(-1, -1);
			tooltip = "";
		}

		if (tab)
		{
			tabHoverAbility = -1;
			for (int i = 0; i < player.abilities.size(); i++)
				if (screenmx > frameWidth / 2 - player.abilities.size() * 80 / 2 * UIzoomLevel + i * 80 * UIzoomLevel && screenmy > frameHeight * 3 / 4
						&& screenmx < frameWidth / 2 - player.abilities.size() * 80 / 2 * UIzoomLevel + i * 80 * UIzoomLevel + 60 * UIzoomLevel && screenmy < frameHeight * 3 / 4 + 60 * UIzoomLevel)
				{

					// int rectStartX = (int) (frameWidth / 2 - player.abilities.size() * 80 / 2 * UIzoomLevel);
					// int rectStartY = (int) (frameHeight * 3 / 4);
					// buffer.drawRect((int) (rectStartX + i * 80 * UIzoomLevel), rectStartY, (int) (60 * UIzoomLevel), (int) (60 * UIzoomLevel));
					tabHoverAbility = i;
					tooltipPoint = new Point((int) (frameWidth / 2 - player.abilities.size() * 80 / 2 * UIzoomLevel + i * 80 * UIzoomLevel + 8 * UIzoomLevel),
							(int) (frameHeight * 3 / 4 - 10 * UIzoomLevel));
					tooltip = player.abilities.get(i).niceName();
					if (player.rightMousePressed)
					{
						tooltip += " " + player.abilities.get(i).points + "\n" + player.abilities.get(i).getFluff();
						tooltipPoint.y -= 30;
					}
				}
		}
	}

	static void print(Object whatever)
	{
		// Used for temporary debug messages
		System.out.println(whatever);
	}

	public static void errorMessage(Object whatever)
	{
		// Used for error messages
		System.out.println(whatever);
	}

	public static void errorMessage()
	{
		System.out.println();
	}

	public static void print()
	{
		System.out.println();
	}
}
