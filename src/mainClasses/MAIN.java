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
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.Timer;

import abilities.Chronobiology;
import abilities.Elastic;
import abilities.Portals;
import abilities.Protective_Bubble_I;
import abilities.Protective_Bubble_II;
import abilities.Punch;
import abilities.Sense_Powers;
import abilities.Shield_E;
import abilities.Sprint;
import abilities.Wild_Power;
import abilities._AFFAbility;
import abilities._BeamAbility;
import abilities._FlightAbility;
import abilities._ForceFieldAbility;
import abilities._GridTargetingAbility;
import abilities._LoopAbility;
import abilities._ProjectileAbility;
import abilities._SummoningAbility;
import abilities._TeleportAbility;
import effects.Burning;
import effects.Ethereal;
import effects.Tangled;
import mainClasses.NPC.Strategy;
import mainResourcesPackage.SoundEffect;

public class MAIN extends JFrame implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, WindowFocusListener
{
	// Current version of the program
	private static final long serialVersionUID = 1;

	// TAU
	final double TAU = 2 * Math.PI;

	// CONSTANTS
	boolean playerRememberPreviouslySeenPlaces = false;
	final boolean movementVariation = true; // if true = player is slower when walking sideways and backwards
	boolean portalCameraRotation = false;
	int frameWidth = 1280, frameHeight = 800;
	Timer frameTimer; // Each frame of this timer redraws the frame
	int frameTimerDelay = 20; // Every 20 milliseconds the frameTimer will do its stuff. =50 FPS
	final static double heightZoomRatio = 0.01;
	final int squareSize = 96;
	// 1 pixel = 1 centimeter. 1 grid tile = 1 meter. Sadly, that also means that in this world, 1 meter = 96 centimeters. Oh well.
	final double globalDeltaTime = (double) frameTimerDelay / 1000;
	final double gravity = 9.8;
	final double someConstant = 0.03;
	final double standingFrictionBenefit = 2.2;
	final double ghostFrictionMultiplier = 0.7;
	final double sqrt2 = Math.sqrt(2);
	final double sqrt2by2 = sqrt2 / 2;
	final double cameraSmoothing = 2.5;
	final BasicStroke dashedStroke3 = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]
	{ 10.0f }, 0.0f);
	final String[] hotkeyStrings = new String[]
	// Right-Click, Shift, Q, E, R, F, V, C, X, Z
	{ "M-Click", "  Shift  ", "     Q", "     E", "     R", "     F", "     V", "     C", "     X", "     Z" };
	FontRenderContext frc;
	Font tooltipFont = new Font("Serif", Font.PLAIN, 12);
	Font FPSFont = new Font("Sans-Serif", Font.PLAIN, 20);
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	static Random random = new Random();
	Point[] niceHotKeys;

	// CAMERA AND MOUSE STUFF
	PointerInfo pin; // Don't use this
	Point mousePoint = new Point(); // Don't use that
	int screenmx = 0; // Mouse X coordinate relative to FRAME
	int screenmy = 0; //
	int mx = 0; // Mouse X coordinate relative to in-game world
	int my = 0;

	Point3D camera = new Point3D(0, 0, 25);
	double zoomLevel = 1;
	double UIzoomLevel = 1;
	double cameraRotation = 0;
	double cameraHeight = 25;

	// Double Buffering (a.k.a stay away from this)
	int bufferWidth;
	int bufferHeight;
	Image bufferImage;
	Graphics bufferGraphics;

	// Variables, lists
	List<Environment> environmentList;
	Environment[][] world;
	int worldCoordsX, worldCoordsY;
	Environment env;
	Player player;
	int frameNum = 0;
	boolean stopUsingPower = false;
	double timeSinceLastScreenshot = 2;
	Image lastScreenshot = null;
	public final static int numOfElements = 32;

	// Visual graphical variables
	Point tooltipPoint = new Point(-1, -1);
	String tooltip = "";
	int hotkeyHovered = -1;
	int hotkeySelected = -1;
	int abilityExamined = -1;

	Line2D drawLine = null; // temp
	Rectangle2D drawRect = null; // also temp

	// Pause
	boolean paused = false, extraPauseBoolean = true;
	int pauseHoverAbility = -1; // ability player is hovering above which, with the mouse

	// FPS checks
	long lastLoopTime = System.nanoTime();
	boolean showFPS = false;
	int FPS = -1;

	// Pause menus
	enum Menu
	{
		NO, TAB, ESC, CHEATS, ABILITIES
	};

	Menu menu = Menu.NO;
	List<MenuElement> menuStuff;
	String cheatedAbilityName = null;
	String cheatedAbilityElement = null;
	int cheatedAbilityLevel = 1;

	// METHODS
	void frame()
	{

		// TODO move this!!!
		/////////////
		Environment newEnv = env;
		if (player.portalToOtherEnvironment == -1)
		{
			if (env.parent == null) // which means it's a World environment
			{
				if (player.x >= env.widthPixels - 1)
					if (worldCoordsX < world.length - 1)
					{
						worldCoordsX++;
					}
				if (player.x <= 0)
					if (worldCoordsX > 0)
					{
						worldCoordsX--;
					}
				if (player.y >= env.heightPixels - 1)
					if (worldCoordsY < world[worldCoordsX].length - 1)
					{
						worldCoordsY++;
					}
				if (player.y <= 0)
					if (worldCoordsY > 0)
					{
						worldCoordsY--;
					}
				newEnv = world[worldCoordsX][worldCoordsY];
			}
			if (env.parent != null) // which means it's an inner environment. Checks if exited
			{
				if (player.x >= env.widthPixels - 1)
				{
					newEnv = env.parent;
				}
				if (player.x <= 0)
				{
					newEnv = env.parent;
				}
				if (player.y >= env.heightPixels - 1)
				{
					newEnv = env.parent;
				}
				if (player.y <= 0)
				{
					newEnv = env.parent;
				}
			}
			for (Environment innerEnv : env.subEnvironments)
			{
				if (player.x >= innerEnv.globalX - env.globalX)
					if (player.x <= innerEnv.globalX - env.globalX + innerEnv.widthPixels - 1)
						if (player.y >= innerEnv.globalY - env.globalY)
							if (player.y <= innerEnv.globalY - env.globalY + innerEnv.heightPixels - 1)
								newEnv = innerEnv;
			}
			if (newEnv == null || newEnv.id != env.id)
			{
				if (newEnv == null)
				{
					world[worldCoordsX][worldCoordsY] = new Environment(worldCoordsX * 48 * squareSize, worldCoordsY * 48 * squareSize, 48, 48);
					environmentList.add(world[worldCoordsX][worldCoordsY]);
					newEnv = world[worldCoordsX][worldCoordsY];
					newEnv.tempBuild();
				}

				double xChange = 0, yChange = 0;

				xChange += env.globalX;
				yChange += env.globalY;

				doSomethingToAllSounds("pause");
				env.people.remove(player);
				env = newEnv;
				doSomethingToAllSounds("unpause");
				env.people.add(player);

				xChange -= env.globalX;
				yChange -= env.globalY;

				player.x += xChange;
				player.y += yChange;
				camera.x += xChange;
				camera.y += yChange;
				env.removeAroundPerson(player);

				// avoiding a bug/exploit in a patchworky way because I'm lazy
				for (PersonCopy pc : player.pastCopies)
				{
					pc.x = player.x;
					pc.y = player.y;
				}
			}
		}

		/////////////

		double deltaTime = globalDeltaTime;
		// Remember: 20 milliseconds between frames, 50 frames per second

		// FPS stuff
		long delta = System.nanoTime() - lastLoopTime;
		lastLoopTime = System.nanoTime();
		if (frameNum % 25 == 0)
			FPS = (int) (1000000000 / delta);

		// Resetting the sounds.
		// SOUNDS EFFECTS (1)
		List<SoundEffect> allSounds = new ArrayList<SoundEffect>();
		for (Person p : env.people)
		{
			allSounds.addAll(p.sounds);
			for (Ability a : p.abilities)
			{
				a.setSounds(p.Point());
				allSounds.addAll(a.sounds);
			}
		}
		// FF SOUNDS
		for (ForceField ff : env.FFs)
			allSounds.addAll(ff.sounds);
		// FURNITURE SOUNDS
		for (Furniture f : env.furniture)
			allSounds.addAll(f.sounds);
		// PORTAL SOUNDS
		for (Portal p : env.portals)
		{
			if (p.sound != null)
				allSounds.add(p.sound);
		}
		allSounds.addAll(env.ongoingSounds);
		// TODO make the above lines only happen once, and make allSounds part of Main, and also update it whenever adding abilities/people/forcefields
		// SOUND EFFECTS (2)
		for (SoundEffect s : allSounds)
		{
			s.justActivated = false;
			if (s.active)
			{
				s.updateVolume(player.x - 1000, player.y - 1000, player.x + 1000, player.y + 1000); // bounds in which sounds won't be muted
				s.stopIfEnded();
			}

		}
		// DEBRIS
		for (int i = 0; i < env.debris.size(); i++)
		{
			Debris d = env.debris.get(i);
			d.update(deltaTime);
			env.moveDebris(d, deltaTime);
			if (d.velocity <= 35 && d.timeLeft <= 0)
			{
				env.debris.remove(i);
				i--;
			}
		}
		// EXPLOSIONS
		for (int i = 0; i < env.explosions.size(); i++)
		{
			Explosion e = env.explosions.get(i);
			e.update(env, deltaTime);
			if (e.timeLeft <= 0)
			{
				env.explosions.remove(i);
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
			env.windDirection = new Point(MAIN.random.nextInt(11) - 5, MAIN.random.nextInt(11) - 5);

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
		// VINES
		for (int i = 0; i < env.vines.size(); i++)
		{
			Vine v = env.vines.get(i);
			for (int j = 0; j < v.evasions.size(); j++)
				if (v.evasions.get(j).timeLeft > 0)
					v.evasions.get(j).timeLeft -= deltaTime;
				else
				{
					v.evasions.remove(j);
					j--;
				}
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
			// PEOPLE
		peopleLoop: for (int k = 0; k < env.people.size(); k++)
		{
			Person p = env.people.get(k);
			for (Ability a : p.abilities) // TODO make sure this is resistant to ConcurrentModificationException and doesn't bug out when dying with extra ability giving abilities
			{
				if (a.prepareToDisable)
				{
					a.prepareToDisable = false;
					a.disable(env, p);
				}
				if (a.prepareToEnable)
				{
					a.prepareToEnable = false;
					a.disabled = false;
				}
			}
			if (p instanceof Clone)
			{
				Clone clone = ((Clone) p);
				// disintegrate
				if (clone.timeLeft > 0)
					clone.timeLeft -= deltaTime;
				else if (clone.timeLeft != -1)
				{
					env.people.remove(k);
					k--;
					continue peopleLoop;
				}
			}
			double floorFriction = applyGravityAndFrictionAndReturnFriction(p, deltaTime);
			if (p.dead)
			{
				// Deactivate all abilities
				int numOfAbilities = p.abilities.size();
				for (int i = 0; i < numOfAbilities; i++)
				{
					p.abilities.get(i).disable(env, p);
					while (numOfAbilities != p.abilities.size())
					{
						numOfAbilities--;
						i--;
						if (p instanceof Player)
							updateNiceHotkeys();
					}
				}
				// Remove all effects
				for (int i = 0; i < p.effects.size(); i++)
					if (p.effects.get(i).removeOnDeath)
					{
						p.effects.get(i).unapply(p);
						p.effects.remove(i);
						i--;
					}
					else
						p.effects.get(i).timeLeft = -1;

				if (p instanceof Player)
				{
					hotkeySelected = -1;
					hotkeyHovered = -1;
					abilityExamined = -1;
					pauseHoverAbility = -1;
				}
			}
			else
			{
				if (p instanceof NPC)
				{
					NPC npc = (NPC) p;
					if (!p.twitching)
						npc.frameAIupdate(deltaTime, env, this);
				}
				else if (p instanceof Player)
				{
					checkPlayerMovementKeys();
					updatePlayerTargeting();
				}

				// maintaining person abilities
				if (p.timeEffect != 0)
					for (int i = 0; i < p.abilities.size(); i++)
					{
						Ability a = p.abilities.get(i);
						if (a.on)
							if (!a.disabled)
								a.maintain(env, p, p.target, deltaTime); // not affected by timeEffect. TODO double check this! (try using abilities like Beam with it)
						p.isChargingChargeAbility = false;
						if (a.hasTag("charge"))
						{
							p.hasChargeAbility = true;
							if (a.checkCharge(env, p, deltaTime))
							{
								p.isChargingChargeAbility = true;
								p.charge += a.chargeRate * deltaTime; // 5 Charge per Second, usually
							}
						}
					}
				// If trying to maintain after slipping
				if (p.maintaining && p.prone)
					p.abilities.get(p.abilityMaintaining).use(env, p, p.target);
				// using abilities the person is trying to repetitively use (e.g. holding down the Punch ability's key)
				if (p.abilityTryingToRepetitivelyUse != -1)
				{
					Ability a = p.abilities.get(p.abilityTryingToRepetitivelyUse);
					if (!a.disabled)
						a.use(env, p, p.target);
				}
				p.selfFrame(deltaTime);
				if (p instanceof NPC)
					for (Effect e : p.effects)
						e.nextFrame(frameNum);
				// movement
				if (!p.twitching)
					checkMovementAttempt(p, floorFriction, deltaTime);
			}
			env.movePerson(p, deltaTime);
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
				int gridX = Math.min(Math.max((int) (p.x) / squareSize, 0), env.width - 1);
				int gridY = Math.min(Math.max((int) (p.y) / squareSize, 0), env.height - 1);
				int type = env.poolTypes[gridX][gridY];
				if (type != -1)
				{
					// also damage the pool the person is standing on. Standing on a full-health pool deals it 10 damage per second (out of 100)
					if (frameNum % 5 == 0) // ten times per second, deal 1 damage
						env.poolHealths[gridX][gridY] -= 1;
					switch (type)
					{
					case 1: // water
					case 5: // ice
					case 9: // flesh (blood pool)
						if (frameNum % 50 == 0)
						{
							p.affect(new Burning(0, null), false); // stop burning
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
								p.slip(true);
							}
						}
						break;
					case 7: // acid
						env.hitPerson(p, 25, 0, 0, 7, deltaTime);
						break;
					case 8: // lava
						env.hitPerson(p, 20, 0, 0, 8, deltaTime);
						if (frameNum % 50 == 0 && random.nextDouble() < 0.4) // burn chance is +40% in lava
							p.affect(new Burning(0, null), true);
						break;
					case 10: // earth spikes
						env.hitPerson(p, 25, 0, 0, 10, deltaTime);
						break;
					case 11: // plant vines/spikes
						env.hitPerson(p, 5, 0, 0, 11, deltaTime);
						if (frameNum % 50 == 0 && random.nextDouble() < 0.2) // tangle chance is +20% in vines
							p.affect(new Tangled(0, null), true);
						break;
					default:
						errorMessage("Unknown pool type: " + type);
						break;
					}
				}
			}
			if (frameNum % 50 == 0)
			{
				int tangleDamage = 0;
				for (int i = 0; i < p.effects.size(); i++)
				{
					Effect e = p.effects.get(i);
					// once per second, damage for burn and test for extinguishing fire
					if (e instanceof Burning)
					{
						env.hitPerson(p, e.strength, 0, 0, 0);
						if (random.nextDouble() < 0.25) // 25% chance to stop burning, per second
							p.affect(e, false);
					}
					// damage from Tangled
					if (e instanceof Tangled)
						tangleDamage += ((Tangled) e).damage;
				}

				env.hitPerson(p, tangleDamage, 0, 0, -1); // not 11!!!! don't!

			}

			// Portals to other environments
			if (p.portalToOtherEnvironment != -1)
			{
				newEnv = environmentList.get(p.portalToOtherEnvironment);

				if (p instanceof Player)
				{
					doSomethingToAllSounds("pause");
					env.people.remove(player);
					env = newEnv;
					doSomethingToAllSounds("unpause");
					env.people.add(player);
					camera.x += p.portalVariableX - p.x;
					camera.y += p.portalVariableY - p.y;
					worldCoordsX = env.globalX / env.widthPixels;
					worldCoordsY = env.globalY / env.heightPixels;
				}
				else
				{
					env.people.remove(p);
					newEnv.people.add(p);
				}

				p.x = p.portalVariableX;
				p.y = p.portalVariableY;

				p.portalToOtherEnvironment = -1;
				p.portalVariableX = 0;
				p.portalVariableY = 0;
			}
			// Possessions:
			if (p.startStopPossession && p.possessionTargetID != -1)
			{
				// BTW, there's no chain-possessions. Too icky and I can't get them to work good.
				p.startStopPossession = false;
				boolean unpossessing = p.possessionTargetID == p.possessingControllerID;
				if (unpossessing)
				{
					p.possessedTimeLeft = 0;
					p.possessingControllerID = -1;
				}
				Person victim = null;
				check: for (int i = 0; i < env.people.size(); i++)
					if (env.people.get(i).id == p.possessionTargetID)
					{
						victim = env.people.get(i);
						break check;
					}
				p.possessionTargetID = -1;

				Person temp = new Person(0, 0);
				Person.cancelID();
				temp.copy(victim);
				victim.copy(p);
				p.copy(temp);
				if (p instanceof Player)
				{
					if (!unpossessing)
					{
						player.oldHotkeys = new int[10];
						for (int i = 0; i < 10; i++)
							player.oldHotkeys[i] = player.hotkeys[i];
						player.defaultHotkeys();
					}
					else
					{
						player.hotkeys = new int[10];
						for (int i = 0; i < 10; i++)
							player.hotkeys[i] = player.oldHotkeys[i];
					}
					abilityExamined = -1;
					updateNiceHotkeys();
				}
				// make victim possessed
				if (victim instanceof NPC)
					if (unpossessing)
					{
						((NPC) victim).strategy = NPC.Strategy.AGGRESSIVE;
						victim.possessionVessel = false;
					}
					else
					{
						victim.possessionVessel = true;
						((NPC) victim).strategy = NPC.Strategy.POSSESSED;
					}
				p.possessionVessel = false;
			}
		}

		// SPRAY DROPS
		for (int i = 0; i < env.sprayDrops.size(); i++)
		{
			SprayDrop sd = env.sprayDrops.get(i);
			for (int j = 0; j < sd.evasions.size(); j++)
				if (sd.evasions.get(j).timeLeft > 0)
					sd.evasions.get(j).timeLeft -= deltaTime;
				else
				{
					sd.evasions.remove(j);
					j--;
				}
			// gravity
			sd.zVel -= 0.003 * gravity * deltaTime * sd.timeEffect;
			if (random.nextInt(100) <= 1)
				env.sprayDropDebris(sd);
			if (sd.xVel == 0 || sd.yVel == 0 || sd.mass <= 0 || !env.moveSprayDrop(sd, deltaTime)) // sd was destroyed, or sd stopped. Also, moves the sd
			{
				env.sprayDrops.remove(i);
				i--;
			}
		}
		// BALLS
		for (int i = 0; i < env.balls.size(); i++)
		{
			Ball b = env.balls.get(i);
			for (int j = 0; j < b.evasions.size(); j++)
				if (b.evasions.get(j).timeLeft > 0)
					b.evasions.get(j).timeLeft -= deltaTime;
				else
				{
					b.evasions.remove(j);
					j--;
				}
			if (b.elementNum == EP.toInt("Fire"))
			{
				b.timer += deltaTime;
				if (b.timer >= Ball.smokeEffectRate)
				{
					env.debris.add(new Debris(b.x, b.y, b.z, b.angle() + Math.PI * 2 / 3 + Math.PI * 2 / 3 * Math.random(), Math.random() < 0.5 ? 0 : 12, 400));
					b.timer = 0;
				}
			}
			// gravity
			b.zVel -= 0.001 * gravity * deltaTime * b.timeEffect;
			b.rotation += b.angularVelocity * deltaTime;
			if (b.xVel == 0 || b.yVel == 0 || b.mass <= 0 || !env.moveBall(b, deltaTime)) // ball was destroyed, or ball stopped. Also, moves the ball
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
		affloop: for (int i = 0; i < env.AFFs.size(); i++)
		{
			ArcForceField aff = env.AFFs.get(i);
			aff.update(deltaTime);
			if (frameNum % 10 == 0) // check for next AFF frame 5 times per second, because the check includes drawing an image
			{
				aff.updateImage();
			}
			// push people within
			for (Person p : env.people)
				if (env.personAFFCollision(p, aff))
				{
					double angleToPerson = Math.atan2(p.y - aff.y, p.x - aff.x);
					double pushStrength = 10000;
					double distFromCenterPow2 = Methods.DistancePow2(aff.x, aff.y, p.x, p.y);
					// if inside the AFF
					if (distFromCenterPow2 < Math.pow(aff.maxRadius / 2 + aff.minRadius / 2, 2))
					{
						// if it's not a bubble
						if (aff.arc < TAU)
							continue;
						// if not touching the edges
						if (distFromCenterPow2 < Math.pow(aff.minRadius - p.radius, 2))
							continue;
						else
						{
							if (aff.type == ArcForceField.Type.MOBILE_BUBBLE)
								aff.life = 0;
							else
								pushStrength *= -3; // pull people inwards if they're inside
						}

					}
					double xMax = 0.03 * pushStrength * Math.cos(angleToPerson);
					double yMax = 0.03 * pushStrength * Math.sin(angleToPerson);
					if ((xMax > 0 && p.xVel < xMax) || (xMax < 0 && p.xVel > xMax))
						p.xVel += deltaTime * pushStrength * Math.cos(angleToPerson);
					if ((yMax > 0 && p.yVel < yMax) || (yMax < 0 && p.yVel > yMax))
						p.yVel += deltaTime * pushStrength * Math.sin(angleToPerson);
					if (p instanceof NPC)
						((NPC) p).justCollided = true;
				}
			// push away from walls, if bubble
			// TODO fix this
			if (aff.arc >= 2 * Math.PI)
				for (int x = (int) ((aff.x - aff.maxRadius) / squareSize); x < (int) ((aff.x + aff.maxRadius) / squareSize) + 1; x++)
					for (int y = (int) ((aff.y - aff.maxRadius) / squareSize); y < (int) ((aff.y + aff.maxRadius) / squareSize) + 1; y++)
						if (x >= 0 && y >= 0 && x < env.width && y < env.height)
							if (env.wallTypes[x][y] != -1)
								for (int x2 = x; x2 < x + 2; x2++)
									for (int y2 = y; y2 < y + 2; y2++)
									{
										double distanceToWallPow2 = Math.pow(squareSize * (y2) - aff.y, 2) + Math.pow(squareSize * (x2) - aff.x, 2);
										if (distanceToWallPow2 < aff.maxRadius * aff.maxRadius)
										{
											double angleToWall = Math.atan2(squareSize * (y2) - aff.y, squareSize * (x2) - aff.x);
											double pushStrength = 10000;
											double xMax = 10.03 * pushStrength * Math.cos(angleToWall);
											double yMax = 10.03 * pushStrength * Math.sin(angleToWall);
											Person p = aff.target;
											if ((xMax > 0 && p.xVel < xMax) || (xMax < 0 && p.xVel > xMax))
												p.xVel -= deltaTime * pushStrength * Math.cos(angleToWall);
											if ((yMax > 0 && p.yVel < yMax) || (yMax < 0 && p.yVel > yMax))
												p.yVel -= deltaTime * pushStrength * Math.sin(angleToWall);
											if (p instanceof NPC)
												((NPC) p).justCollided = true;
										}
									}
			Person p = null;
			for (Person p2 : env.people)
				if (p2.equals(aff.target))
					p = p2;
			// If person inside is gone
			if (p == null)
			{
				env.shieldDebris(aff, "bubble");
				env.AFFs.remove(i);
				i--;
				continue affloop;
			}
			if (aff.life <= 0)
			{
				for (Ability a : p.abilities)
				{
					if (a instanceof Shield_E)
					{
						Shield_E ability = (Shield_E) a;
						if (aff.equals(ability.shield))
						{
							ability.use(env, p, p.target); // that method will remove the arc force field.
							i--;
							continue affloop;
						}
					}
					if (a instanceof Protective_Bubble_I)
					{
						Protective_Bubble_I ability = (Protective_Bubble_I) a;
						if (aff.equals(ability.bubble))
						{
							ability.on = false;
							ability.sounds.get(1).play();
							ability.cooldownLeft = ability.cooldown;
							env.shieldDebris(aff, "bubble");
							env.AFFs.remove(i);
							i--;
							continue affloop;
						}
					}
					if (a instanceof Protective_Bubble_II)
					{
						Protective_Bubble_II ability = (Protective_Bubble_II) a;
						if (aff.equals(ability.bubble))
						{
							ability.on = false;
							ability.sounds.get(1).play();
							ability.cooldownLeft = ability.cooldown;
							env.shieldDebris(aff, "bubble");
							env.AFFs.remove(i);
							i--;
							continue affloop;
						}
					}
				}
				// was a bubble created by someone else
				env.shieldDebris(aff, "bubble");
				env.AFFs.remove(i);
				i--;
				continue affloop;

			}
		}

		// FORCE FIELDS
		for (int i = 0; i < env.FFs.size(); i++)
		{
			ForceField ff = env.FFs.get(i);
			// Force Shield decay
			ff.decay(deltaTime);
			if (ff.life <= 0)
			{
				ff.stopAllSounds();
				env.FFs.remove(i);
				i--;
			}
		}

		// FURNITURE
		for (int i = 0; i < env.furniture.size(); i++)
		{
			Furniture f = env.furniture.get(i);
			if (f.life <= 0)
			{
				env.furniture.remove(i);
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

		// PORTALS
		for (int i = 0; i < env.portals.size(); i++)
			if (env.portals.get(i).destroyThis)
			{
				env.portals.remove(i);
				i--;
			}
		// PORTALS
		List<Drawable> stuff = new ArrayList<Drawable>();
		stuff.addAll(env.people);
		stuff.addAll(env.balls);
		stuff.addAll(env.debris);
		stuff.addAll(env.sprayDrops);
		for (Drawable d : stuff)
		{
			Rectangle2D dRect = new Rectangle2D.Double(d.x - d.image.getWidth() / 2, d.y - d.image.getHeight() / 2, d.image.getWidth(), d.image.getHeight());

			// if portal was canceled but still exists in person object:
			if (d.intersectedPortal != null && !env.portals.contains(d.intersectedPortal))
				d.intersectedPortal = null;

			for (Portal p : env.portals)
			{
				Line2D pLine = new Line2D.Double(p.start.x, p.start.y, p.end.x, p.end.y);
				boolean intersects = false;
				if (d.z < p.highestPoint() && p.z < d.highestPoint())
					if (dRect.intersectsLine(pLine))
					{
						// check if within portal
						Point2D closestPointOnLine = Methods.getClosestPointOnLine(p.start.x, p.start.y, p.end.x, p.end.y, d.x, d.y);
						Point2D closestPointOnSegment = Methods.getClosestPointOnSegment(p.start.x, p.start.y, p.end.x, p.end.y, d.x, d.y);
						if (closestPointOnLine.equals(closestPointOnSegment))
						{
							if (Methods.DistancePow2(p.start, closestPointOnSegment) > d.radius * d.radius && Methods.DistancePow2(p.end, closestPointOnSegment) > d.radius * d.radius)
								intersects = true;
						}
					}
				if (intersects)
				{
					if (d.intersectedPortal == null || d.intersectedPortal.equals(p))
						d.intersectedPortal = p;
					else // intersecting two portals at once is a big no-no!
					{
						d.intersectedPortal = null;
						if (d instanceof Person)
							env.hitPerson(((Person) d), 10, 0, 0, 9, deltaTime); // flesh damage. like with ghost modes
					}
				}
				else if (d.intersectedPortal != null && d.intersectedPortal.equals(p)) // if it used to be but no longer is
					d.intersectedPortal = null;
			}
		}
		for (Beam b : env.beams)
		{
			double minDist = b.size * 20 * 1.414;
			for (Portal p : env.portals)
			{
				boolean intersects = false;
				Point2D closestPointOnLine = Methods.getClosestPointOnLine(p.start.x, p.start.y, p.end.x, p.end.y, b.start.x, b.start.y);
				Point2D closestPointOnSegment = Methods.getClosestPointOnSegment(p.start.x, p.start.y, p.end.x, p.end.y, b.start.x, b.start.y);
				if (closestPointOnLine.equals(closestPointOnSegment) && Methods.DistancePow2(b.start, closestPointOnLine) < minDist * minDist)
					intersects = true;
				closestPointOnLine = Methods.getClosestPointOnLine(p.start.x, p.start.y, p.end.x, p.end.y, b.end.x, b.end.y);
				closestPointOnSegment = Methods.getClosestPointOnSegment(p.start.x, p.start.y, p.end.x, p.end.y, b.end.x, b.end.y);
				if (closestPointOnLine.equals(closestPointOnSegment))
					intersects = true;
				if (intersects)
					b.intersectedPortal = p;
				else if (b.intersectedPortal != null && b.intersectedPortal.equals(p)) // if it used to be but no longer is
					b.intersectedPortal = null;
			}
		}

		// Updating pool transparencies due to damage, and spreading the damage around evenly
		if (frameNum % 10 == 0)
			env.updatePools();

		// camera movement
		int diffX = (int) ((player.x - camera.x) * deltaTime * cameraSmoothing * zoomLevel);
		int diffY = (int) ((player.y - camera.y) * deltaTime * cameraSmoothing * zoomLevel);
		int diffZ = (int) ((player.z + cameraHeight - camera.z) * zoomLevel);
		if (portalCameraRotation && player.portalCameraRotation != 0)
		{
			cameraRotation += player.portalCameraRotation;
			player.portalCameraRotation = 0;
		}
		if (player.portalVariableX != 0 || player.portalVariableY != 0)
		{
			camera.x += player.x - player.portalVariableX;
			camera.y += player.y - player.portalVariableY;
			player.portalVariableX = 0;
			player.portalVariableY = 0;
		}
		camera.x += diffX;
		camera.y += diffY;
		camera.z += diffZ;
		updateMousePosition();

		frameNum++;

		for (int i = 0; i < env.visualEffects.size(); i++)
		{
			VisualEffect eff = env.visualEffects.get(i);
			eff.update(frameNum);
			eff.timeLeft -= deltaTime;
			if (eff.timeLeft <= 0)
			{
				env.visualEffects.remove(i);
				i--;
			}
		}

		// Stopping sounds that should stop looping
		for (SoundEffect s : allSounds)
			if (!s.justActivated && s.active && s.endUnlessMaintained)
				s.stop();

		if (timeSinceLastScreenshot <= 2)
			timeSinceLastScreenshot += deltaTime;
		else
			lastScreenshot = null;

		keyPressFixingMethod(-1, true);
	}

	void pauseFrame()
	{
		// TODO
	}

	void updatePlayerTargeting()
	{
		player.target = new Point(mx, my);
		Ability ability;
		if (player.abilityAiming != -1)
			ability = player.abilities.get(player.abilityAiming);
		else if (player.abilityTryingToRepetitivelyUse != -1)
			ability = player.abilities.get(player.abilityTryingToRepetitivelyUse);
		else if (player.abilityMaintaining != -1 && player.maintaining)
			ability = player.abilities.get(player.abilityMaintaining);
		else
		{
			player.aimType = Player.AimType.NONE;
			player.successfulTarget = false;
			return;
		}
		updateTargeting(player, ability);
		ability.updatePlayerTargeting(env, player, player.target, 0);
	}

	void updateTargeting(Person p, Ability ability)
	{
		double angle = Math.atan2(p.target.y - p.y, p.target.x - p.x);

		if (ability instanceof Portals)
			if (((Portals) ability).holdTarget != null)
				return;
		// if the area isn't nice
		if (!ability.rangeType.equals(Ability.RangeType.CREATE_IN_GRID))
			if (ability.hasTag("range"))
				if (Methods.DistancePow2(p.x, p.y, p.target.x, p.target.y) > ability.range * ability.range) // clamp target to range:
				{
					p.target.x = (int) (p.x + Math.cos(angle) * ability.range);
					p.target.y = (int) (p.y + Math.sin(angle) * ability.range);
				}
	}

	void drawRange(Graphics2D buffer, Ability ability)
	{
		switch (ability.rangeType)
		{
		case EXPLOSION:
			buffer.setStroke(dashedStroke3);
			buffer.setColor(Color.orange);
			player.target = player.Point(); // I hope this isn't a mistake
			// explosion "plus"
			buffer.drawLine(player.target.x - (int) (0.1 * ability.radius), player.target.y, player.target.x + (int) (0.1 * ability.radius), player.target.y);
			buffer.drawLine(player.target.x, player.target.y - (int) (0.1 * ability.radius), player.target.x, player.target.y + (int) (0.1 * ability.radius));
			// explosion circles
			int circleRadius = (int) (ability.radius);
			while (circleRadius >= 4)
			{
				buffer.setColor(new Color(255, 192, 0, (int) (64 + 191 * circleRadius / ability.radius)));
				buffer.drawOval(player.target.x - (int) (ability.radius - circleRadius), player.target.y - (int) (ability.radius - circleRadius), (int) (ability.radius - circleRadius) * 2,
						(int) (ability.radius - circleRadius) * 2);
				circleRadius /= 2;
			}
			break;
		case CREATE_IN_GRID:
			buffer.setStroke(dashedStroke3);
			_GridTargetingAbility gAbility = (_GridTargetingAbility) ability;
			gAbility.UPT(env, player);
			if (player.abilityAiming == -1 || ability.on)
			{
				if (gAbility.canBuildInTarget)
					buffer.setColor(Color.green);
				else
					buffer.setColor(Color.red);
				buffer.drawRect(player.target.x - squareSize / 2, player.target.y - squareSize / 2, squareSize, squareSize);
			}
			buffer.setColor(new Color(255, 255, 255, 80)); // stroke is still dashed
			buffer.draw(gAbility.rangeArea);
			break;
		case EXACT_RANGE:
			buffer.setColor(new Color(255, 255, 255, 80)); // transparent white
			buffer.setStroke(dashedStroke3);
			buffer.drawOval((int) (player.x - ability.range), (int) (player.y - ability.range), (int) (2 * ability.range), (int) (2 * ability.range));
			break;
		case CIRCLE_AREA:
			// "filled" area, not just outlines.
			buffer.setStroke(new BasicStroke(1));
			buffer.setColor(new Color(182, 255, 0));
			Shape thing = new Ellipse2D.Double(player.x - ability.range, player.y - ability.range, 2 * ability.range, 2 * ability.range);
			Shape originalClip = buffer.getClip();
			buffer.clip(thing);
			for (int x = (int) (player.x - ability.range) / 18 * 18; x < (int) (player.x + ability.range + 18) / 18 * 18; x += 18)
				buffer.drawLine(x, (int) (player.y - ability.range), x, (int) (player.y + ability.range));
			for (int y = (int) (player.y - ability.range) / 18 * 18; y < (int) (player.y + ability.range + 18) / 18 * 18; y += 18)
				buffer.drawLine((int) (player.x - ability.range), y, (int) (player.x + ability.range), y);
			buffer.setClip(originalClip);
			buffer.drawOval((int) (player.x - ability.range), (int) (player.y - ability.range), (int) (ability.range * 2), (int) (ability.range * 2));

			// more resource-intensive method ahead, that does the exact same thing :)

			// // NOTE: This uses TexturePaint, and will always look slightly or very weird. Worth it though.
			// BufferedImage image = new BufferedImage(2 * ability.range + 18, 2 * ability.range + 18, BufferedImage.TYPE_INT_ARGB);
			// Graphics2D shreodinger = image.createGraphics(); // name is irrelevant
			// shreodinger.setPaint(new TexturePaint(Resources.range_net, new Rectangle(0, 0, 90, 90))); //range_net was an image, very simple one
			// shreodinger.fillOval(0 + (int) (player.x) % 18, 0 + (int) (player.y) % 18, 2 * ability.range, 2 * ability.range);
			// shreodinger.setColor(new Color(182, 255, 0)); // greenish
			// shreodinger.setStroke(new BasicStroke(1));
			// shreodinger.drawOval(0 + (int) (player.x) % 18, 0 + (int) (player.y) % 18, 2 * ability.range, 2 * ability.range);
			// shreodinger.dispose();
			// buffer.drawImage(image, (int) (player.x - ability.range) - (int) (player.x) % 18, (int) (player.y - ability.range) - (int) (player.y) % 18, null);
			// // You must be wondering why I did this wacky hijink instead ofsimply drawing the ovals with buffer. Well, apparently the TexturePaint causes the process to be very slow when the camera is zoomed in (and buffer's scale is very big).

			break;
		case CONE:
			buffer.setStroke(dashedStroke3);
			buffer.setColor(new Color(255, 255, 255, 80)); // transparent white
			buffer.drawLine((int) (player.x + 50 * Math.cos(player.rotation + ability.arc / 2)), (int) (player.y + 50 * Math.sin(player.rotation + ability.arc / 2)),
					(int) (player.x + ability.range * Math.cos(player.rotation + ability.arc / 2)), (int) (player.y + ability.range * Math.sin(player.rotation + ability.arc / 2)));
			buffer.drawLine((int) (player.x + 50 * Math.cos(player.rotation - ability.arc / 2)), (int) (player.y + 50 * Math.sin(player.rotation - ability.arc / 2)),
					(int) (player.x + ability.range * Math.cos(player.rotation - ability.arc / 2)), (int) (player.y + ability.range * Math.sin(player.rotation - ability.arc / 2)));
			buffer.drawArc((int) (player.x - ability.range), (int) (player.y - ability.range), (int) (ability.range * 2), (int) (ability.range * 2),
					(int) ((-player.rotation - ability.arc / 2) / Math.PI * 180), (int) (ability.arc / Math.PI * 180));
			buffer.drawArc((int) (player.x - 50), (int) (player.y - 50), 50 * 2, 50 * 2, (int) ((-player.rotation - ability.arc / 2) / Math.PI * 180), (int) (ability.arc / Math.PI * 180));
			break;
		case NONE:
			break;
		default:
			errorMessage("No such range type, sir!   " + ability.rangeType);
			break;
		}
	}

	void drawAim(Graphics2D buffer)
	{
		Ability ability = player.abilities.get(player.abilityAiming);

		switch (player.aimType)
		{
		case AIMLESS:
			// draws star around player
			double starRadiusSmall = 50;
			double starRadiusLarge = 70;

			buffer.setStroke(new BasicStroke(5));
			buffer.setColor(Color.black);
			for (int i = 0; i < 5; i++)
			{
				double angle = TAU / 12 + TAU / 5 * i;
				buffer.drawLine((int) (player.x + starRadiusSmall * Math.cos(angle)), (int) (player.y + starRadiusSmall * Math.sin(angle)),
						(int) (player.x + starRadiusLarge * Math.cos(angle + TAU / 10)), (int) (player.y + starRadiusLarge * Math.sin(angle + TAU / 10)));
				buffer.drawLine((int) (player.x + starRadiusSmall * Math.cos(angle + TAU / 5)), (int) (player.y + starRadiusSmall * Math.sin(angle + TAU / 5)),
						(int) (player.x + starRadiusLarge * Math.cos(angle + TAU / 10)), (int) (player.y + starRadiusLarge * Math.sin(angle + TAU / 10)));
			}
			buffer.setStroke(new BasicStroke(3));
			buffer.setColor(Color.orange);
			for (int i = 0; i < 5; i++)
			{
				double angle = TAU / 12 + TAU / 5 * i;
				buffer.drawLine((int) (player.x + starRadiusSmall * Math.cos(angle)), (int) (player.y + starRadiusSmall * Math.sin(angle)),
						(int) (player.x + starRadiusLarge * Math.cos(angle + TAU / 10)), (int) (player.y + starRadiusLarge * Math.sin(angle + TAU / 10)));
				buffer.drawLine((int) (player.x + starRadiusSmall * Math.cos(angle + TAU / 5)), (int) (player.y + starRadiusSmall * Math.sin(angle + TAU / 5)),
						(int) (player.x + starRadiusLarge * Math.cos(angle + TAU / 10)), (int) (player.y + starRadiusLarge * Math.sin(angle + TAU / 10)));
			}
			break;
		case CLONE:
			double angle1 = Math.atan2(player.target.y - player.y, player.target.x - player.x);
			buffer.setStroke(new BasicStroke(2));
			buffer.setColor(Color.magenta);
			for (int i = 0; i < 6; i++)
				buffer.drawLine((int) (player.x + ability.range * Math.cos(angle1) + 60 * Math.cos(i * Math.PI / 3)),
						(int) (player.y + ability.range * Math.sin(angle1) + 60 * Math.sin(i * Math.PI / 3)), (int) (player.x + ability.range * Math.cos(angle1)),
						(int) (player.y + ability.range * Math.sin(angle1)));
			break;
		case WILD_POWER:
			Object target1 = ((Wild_Power) ability).getTarget(env, player.target);
			if (target1 instanceof Person)
			{
				Point targetPerson = ((Person) target1).Point();
				buffer.setStroke(new BasicStroke(3));
				if (((Person) target1).commanderID == player.commanderID)
					buffer.setColor(Color.green);
				else
					buffer.setColor(Color.red);
				int haloRadius = 60;
				buffer.drawOval(targetPerson.x - haloRadius, targetPerson.y - haloRadius, haloRadius * 2, haloRadius * 2);
			}
			else if (target1 instanceof Ball)
			{
				Point targetBall = ((Ball) target1).Point();
				buffer.setStroke(new BasicStroke(3));
				buffer.setColor(Color.blue);
				int haloRadius = 40;
				buffer.drawOval(targetBall.x - haloRadius, targetBall.y - haloRadius, haloRadius * 2, haloRadius * 2);
			}
			else if (target1 instanceof Point)
			{
				Point point = (Point) target1;
				buffer.setStroke(new BasicStroke(3));
				buffer.setColor(Color.yellow);
				buffer.drawRect(point.x * squareSize - ((int) (ability.LEVEL / 4)) * squareSize, point.y * squareSize - ((int) (ability.LEVEL / 4)) * squareSize,
						((int) (ability.LEVEL / 4)) * 2 * squareSize + squareSize, ((int) (ability.LEVEL / 4)) * 2 * squareSize + squareSize);
			}
			break;
		case PORTALS:
			Portals p = (Portals) ability;
			if (p.holdTarget == null)
				break;
			if (p.p1 == null) // first portal - variable length
			{
				double portalAngle = Math.atan2(player.target.y - p.holdTarget.y, player.target.x - p.holdTarget.x);
				if (p.alignPortals) // snap to cardinal directions
				{
					double length = Math.min(p.maxPortalLength, Math.sqrt(Methods.DistancePow2(p.holdTarget.x, p.holdTarget.y, player.target.x, player.target.y)));
					length = Math.max(p.minPortalLength, length);
					portalAngle += Math.PI; // angle is between 0 and TAU
					portalAngle = (int) ((portalAngle / Math.PI * 180 + 45) / 90) * 90 + 180;
					portalAngle = portalAngle / 180 * Math.PI;
				}
				double length;
				length = Math.min(p.maxPortalLength, Math.sqrt(Methods.DistancePow2(p.holdTarget.x, p.holdTarget.y, player.target.x, player.target.y)));
				length = Math.max(p.minPortalLength, length);
				Line2D newPortal = new Line2D.Double(p.holdTarget.x, p.holdTarget.y, p.holdTarget.x + length * Math.cos(portalAngle), p.holdTarget.y + length * Math.sin(portalAngle));
				Portal p1 = new Portal(newPortal, player.z);
				if (env.checkPortal(p1))
					buffer.setColor(Color.orange);
				else
				{
					checkDrawPortalProblem(buffer, p1);
					buffer.setColor(Color.red);
				}
				buffer.setStroke(dashedStroke3);
				buffer.drawLine((int) (newPortal.getX1()), (int) (newPortal.getY1()), (int) (newPortal.getX2()), (int) (newPortal.getY2()));
			}
			else if (p.p2 == null)
			{
				double length = p.p1.length;
				double portalAngle = Math.atan2(player.target.y - p.holdTarget.y, player.target.x - p.holdTarget.x);
				Line2D newPortal = new Line2D.Double(p.holdTarget.x, p.holdTarget.y, p.holdTarget.x + length * Math.cos(portalAngle), p.holdTarget.y + length * Math.sin(portalAngle));
				if (p.alignPortals) // parallel portals
				{
					double htx = (int) (player.target.x - p.p1.length / 2 * Math.cos(p.p1.angle));
					double hty = (int) (player.target.y - p.p1.length / 2 * Math.sin(p.p1.angle));
					portalAngle = p.p1.angle;
					newPortal = new Line2D.Double(htx, hty, htx + length * Math.cos(portalAngle), hty + length * Math.sin(portalAngle));
				}
				Portal p2 = new Portal(newPortal, player.z);
				if (!env.checkPortal(p2))
				{
					checkDrawPortalProblem(buffer, p2);
					buffer.setColor(Color.red);
				}
				else
					buffer.setColor(Color.orange);
				buffer.setStroke(dashedStroke3);
				buffer.drawLine((int) (newPortal.getX1()), (int) (newPortal.getY1()), (int) (newPortal.getX2()), (int) (newPortal.getY2()));
			}
			else // Deleting portals
			{
				buffer.setStroke(new BasicStroke(3));
				buffer.setColor(Color.orange);
				int XArmLength = 50;
				buffer.drawLine(player.target.x - XArmLength, player.target.y - XArmLength, player.target.x + XArmLength, player.target.y + XArmLength);
				buffer.drawLine(player.target.x - XArmLength, player.target.y + XArmLength, player.target.x + XArmLength, player.target.y - XArmLength);
				buffer.setColor(Color.red);
				buffer.drawLine(p.p1.start.x, p.p1.start.y, p.p1.end.x, p.p1.end.y);
				buffer.drawLine(p.p2.start.x, p.p2.start.y, p.p2.end.x, p.p2.end.y);
			}
			break;
		case EXPLOSION:
			buffer.setStroke(dashedStroke3);
			buffer.setColor(Color.orange);
			// explosion "plus"
			buffer.drawLine(player.target.x - (int) (0.1 * ability.radius), player.target.y, player.target.x + (int) (0.1 * ability.radius), player.target.y);
			buffer.drawLine(player.target.x, player.target.y - (int) (0.1 * ability.radius), player.target.x, player.target.y + (int) (0.1 * ability.radius));
			// explosion circles
			int circleRadius = (int) (ability.radius);
			while (circleRadius >= 4)
			{
				buffer.setColor(new Color(255, 192, 0, (int) (64 + 191 * circleRadius / ability.radius)));
				buffer.drawOval(player.target.x - (int) (ability.radius - circleRadius), player.target.y - (int) (ability.radius - circleRadius), (int) (ability.radius - circleRadius) * 2,
						(int) (ability.radius - circleRadius) * 2);
				circleRadius /= 2;
			}
			break;
		case TARGET_IN_RANGE:
			Person targetPerson = ability.getTarget(env, player, player.target);
			if (targetPerson != null)
			{
				buffer.setStroke(new BasicStroke(3));
				buffer.setColor(Color.green);
				int haloRadius = 60;
				buffer.drawOval((int) targetPerson.x - haloRadius, (int) targetPerson.y - haloRadius, haloRadius * 2, haloRadius * 2);
			}
			else
			{
				buffer.setStroke(dashedStroke3);
				buffer.setColor(Color.orange);
				int haloRadius = 100;
				buffer.drawOval(player.target.x - haloRadius, player.target.y - haloRadius, haloRadius * 2, haloRadius * 2);
			}
			break;
		case TELEPORT:
			final int radius = 35;
			_TeleportAbility teleportAbility = (_TeleportAbility) ability;
			if (player.successfulTarget)
			{
				Color red = Color.red;
				boolean telefrag = false;
				for (Person p1 : env.people)
					if (!p1.equals(player))
						if (p1.z + p1.height > player.z && p1.z < player.z + player.height)
							if (Methods.DistancePow2(player.target.x, player.target.y, p1.x, p1.y) < Math.pow((player.radius + p1.radius), 2))
							{
								telefrag = true;
								break;
							}

				buffer.setStroke(new BasicStroke(3));
				buffer.setColor(new Color(53, 230, 240));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle1)), player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle1)),
						player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle1 + Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle1 + Math.PI * 2 / 3)));
				if (telefrag)
				{
					buffer.setStroke(new BasicStroke(1));
					buffer.setColor(red);
					buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle1)), player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle1)),
							player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle1 + Math.PI * 2 / 3)),
							player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle1 + Math.PI * 2 / 3)));
				}
				buffer.setStroke(new BasicStroke(3));
				buffer.setColor(new Color(40, 210, 250));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle2)), player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle2)),
						player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle2 + Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle2 + Math.PI * 2 / 3)));
				if (telefrag)
				{
					buffer.setStroke(new BasicStroke(1));
					buffer.setColor(red);
					buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle2)), player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle2)),
							player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle2 + Math.PI * 2 / 3)),
							player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle2 + Math.PI * 2 / 3)));
				}
				buffer.setStroke(new BasicStroke(3));
				buffer.setColor(new Color(20, 200, 255));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle3)), player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle3)),
						player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle3 + Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle3 + Math.PI * 2 / 3)));
				if (telefrag)
				{
					buffer.setStroke(new BasicStroke(1));
					buffer.setColor(red);
					buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle3)), player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle3)),
							player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle3 + Math.PI * 2 / 3)),
							player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle3 + Math.PI * 2 / 3)));
				}
				buffer.setStroke(new BasicStroke(3));
				buffer.setColor(new Color(53, 230, 240));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle1)), player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle1)),
						player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle1 - Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle1 - Math.PI * 2 / 3)));
				if (telefrag)
				{
					buffer.setStroke(new BasicStroke(1));
					buffer.setColor(red);
					buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle1)), player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle1)),
							player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle1 - Math.PI * 2 / 3)),
							player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle1 - Math.PI * 2 / 3)));
				}
				buffer.setStroke(new BasicStroke(3));
				buffer.setColor(new Color(53, 218, 255));
				buffer.drawOval(player.target.x - radius, player.target.y - radius, radius * 2, radius * 2);
				if (telefrag)
				{
					buffer.setStroke(new BasicStroke(1));
					buffer.setColor(red);
					buffer.drawOval(player.target.x - radius, player.target.y - radius, radius * 2, radius * 2);
				}
				buffer.setStroke(new BasicStroke(3));
				buffer.setColor(new Color(40, 210, 250));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle2)), player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle2)),
						player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle2 - Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle2 - Math.PI * 2 / 3)));
				if (telefrag)
				{
					buffer.setStroke(new BasicStroke(1));
					buffer.setColor(red);
					buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle2)), player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle2)),
							player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle2 - Math.PI * 2 / 3)),
							player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle2 - Math.PI * 2 / 3)));
				}
				buffer.setStroke(new BasicStroke(3));
				buffer.setColor(new Color(53, 230, 240));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle1 + Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle1 + Math.PI * 2 / 3)),
						player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle1 - Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle1 - Math.PI * 2 / 3)));
				if (telefrag)
				{
					buffer.setStroke(new BasicStroke(1));
					buffer.setColor(red);
					buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle1 + Math.PI * 2 / 3)),
							player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle1 + Math.PI * 2 / 3)),
							player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle1 - Math.PI * 2 / 3)),
							player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle1 - Math.PI * 2 / 3)));
				}
				buffer.setStroke(new BasicStroke(3));
				buffer.setColor(new Color(20, 200, 255));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle3)), player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle3)),
						player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle3 - Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle3 - Math.PI * 2 / 3)));
				if (telefrag)
				{
					buffer.setStroke(new BasicStroke(1));
					buffer.setColor(red);
					buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle3)), player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle3)),
							player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle3 - Math.PI * 2 / 3)),
							player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle3 - Math.PI * 2 / 3)));
				}
				buffer.setStroke(new BasicStroke(3));
				buffer.setColor(new Color(40, 210, 250));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle2 + Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle2 + Math.PI * 2 / 3)),
						player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle2 - Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle2 - Math.PI * 2 / 3)));
				if (telefrag)
				{
					buffer.setStroke(new BasicStroke(1));
					buffer.setColor(red);
					buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle2 + Math.PI * 2 / 3)),
							player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle2 + Math.PI * 2 / 3)),
							player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle2 - Math.PI * 2 / 3)),
							player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle2 - Math.PI * 2 / 3)));
				}
				buffer.setStroke(new BasicStroke(3));
				buffer.setColor(new Color(20, 200, 255));
				buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle3 + Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle3 + Math.PI * 2 / 3)),
						player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle3 - Math.PI * 2 / 3)),
						player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle3 - Math.PI * 2 / 3)));
				if (telefrag)
				{
					buffer.setStroke(new BasicStroke(1));
					buffer.setColor(red);
					buffer.drawLine(player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle3 + Math.PI * 2 / 3)),
							player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle3 + Math.PI * 2 / 3)),
							player.target.x + (int) (radius * 1.3 * Math.cos(teleportAbility.triangle3 - Math.PI * 2 / 3)),
							player.target.y + (int) (radius * 1.3 * Math.sin(teleportAbility.triangle3 - Math.PI * 2 / 3)));
				}
			}
			else
			{
				buffer.setColor(Color.red);
				buffer.drawOval(player.target.x - 25, player.target.y - 25, 50, 50);
				buffer.drawRect(player.target.x - player.radius / 2, player.target.y - player.radius / 2, player.radius, player.radius);
				buffer.drawOval(player.target.x - radius, player.target.y - radius, radius * 2, radius * 2);
			}

			if (teleportAbility.type != _TeleportAbility.Type.TARGET_POINT)
			{
				double angle = Math.atan2(player.target.y - player.y, player.target.x - player.x);
				double distance = Math.sqrt(Math.pow(player.x - player.target.x, 2) + Math.pow(player.y - player.target.y, 2));
				buffer.setStroke(dashedStroke3);
				buffer.drawLine((int) (player.x + 0.1 * distance * Math.cos(angle)), (int) (player.y + 0.1 * distance * Math.sin(angle)), (int) (player.x + 0.9 * distance * Math.cos(angle)),
						(int) (player.y + 0.9 * distance * Math.sin(angle)));
			}

			break;
		case CREATE_FF:
			_ForceFieldAbility ffAbility = (_ForceFieldAbility) ability;
			double angleToFF = Math.atan2(player.y - player.target.y, player.x - player.target.x);
			buffer.setColor(new Color(53, 218, 255));
			buffer.setStroke(dashedStroke3);
			buffer.rotate(angleToFF + Math.PI / 2, player.target.x, player.target.y);
			buffer.drawRect((int) (player.target.x - ffAbility.length / 2), (int) (player.target.y - ffAbility.width / 2), (int) (ffAbility.length), (int) (ffAbility.width));
			buffer.rotate(-angleToFF - Math.PI / 2, player.target.x, player.target.y);
			break;
		case LOOP:
			_LoopAbility loopAbility = (_LoopAbility) ability;

			List<Person> targets = loopAbility.getTargets(env, player, player.target);
			if (loopAbility.targeting != _LoopAbility.Targeting.SELF)
			{
				// draw range around mouse point
				buffer.setStroke(dashedStroke3);
				buffer.setColor(Color.orange);
				int haloRadius = (int) loopAbility.maxDistFromTargetedPoint;
				buffer.drawOval(player.target.x - haloRadius, player.target.y - haloRadius, haloRadius * 2, haloRadius * 2);
			}
			if (!targets.isEmpty())
			{
				int haloRadius = 60;
				for (Person targetedPerson : targets)
				{
					buffer.setStroke(new BasicStroke(3));
					buffer.setColor(Color.orange);
					buffer.drawOval((int) targetedPerson.x - haloRadius, (int) targetedPerson.y - haloRadius, haloRadius * 2, haloRadius * 2);
					if (loopAbility.position)
					{
						buffer.setColor(new Color(182, 255, 0, 128));
						int last = targetedPerson.pastCopies.size() - 1;
						for (int i = 0; i < last && i < loopAbility.duration; i++)
							buffer.drawLine((int) targetedPerson.pastCopies.get(last - i).x, (int) targetedPerson.pastCopies.get(last - i).y, (int) targetedPerson.pastCopies.get(last - i - 1).x,
									(int) targetedPerson.pastCopies.get(last - i - 1).y);
						buffer.drawLine((int) targetedPerson.x, (int) targetedPerson.y, (int) targetedPerson.pastCopies.get(last).x, (int) targetedPerson.pastCopies.get(last).y);
						targetedPerson.pastCopies.get((int) Math.max(last - loopAbility.duration, 0)).draw(buffer);
					}

				}
			}
			else
			{
				buffer.setStroke(dashedStroke3);
				buffer.setColor(Color.red);
				int haloRadius = (int) loopAbility.maxDistFromTargetedPoint - 2;
				buffer.drawOval(player.target.x - haloRadius, player.target.y - haloRadius, haloRadius * 2, haloRadius * 2);
			}
			break;
		case NONE:
			break;
		default:
			errorMessage("No such target type");
			break;
		}
	}

	void checkDrawPortalProblem(Graphics2D buffer, Portal p1)
	{
		for (Portal p2 : env.portals)
			if (p1.z <= p2.highestPoint() && p2.z <= p1.highestPoint())
			{
				if (p1.Line2D().intersectsLine(p2.Line2D()))
					drawPortalProblem(buffer, p2);
				else if (Methods.SegmentToPointDistancePow2(p2.start.x, p2.start.y, p2.end.x, p2.end.y, p1.start.x, p1.start.y) < Portals.minimumDistanceBetweenPortalsPow2)
					drawPortalProblem(buffer, p2);
				else if (Methods.SegmentToPointDistancePow2(p2.start.x, p2.start.y, p2.end.x, p2.end.y, p1.end.x, p1.end.y) < Portals.minimumDistanceBetweenPortalsPow2)
					drawPortalProblem(buffer, p2);
				else if (Methods.SegmentToPointDistancePow2(p1.start.x, p1.start.y, p1.end.x, p1.end.y, p2.start.x, p2.start.y) < Portals.minimumDistanceBetweenPortalsPow2)
					drawPortalProblem(buffer, p2);
				else if (Methods.SegmentToPointDistancePow2(p1.start.x, p1.start.y, p1.end.x, p1.end.y, p2.end.x, p2.end.y) < Portals.minimumDistanceBetweenPortalsPow2)
					drawPortalProblem(buffer, p2);
			}
	}

	void drawPortalProblem(Graphics2D buffer, Portal p1)
	{
		double minDist = Math.sqrt(Portals.minimumDistanceBetweenPortalsPow2);
		// rectangle
		Polygon polygon = new Polygon();
		polygon.addPoint((int) (p1.start.x + minDist * Math.cos(p1.angle + Math.PI / 2)), (int) (p1.start.y + minDist * Math.sin(p1.angle + Math.PI / 2)));
		polygon.addPoint((int) (p1.start.x - minDist * Math.cos(p1.angle + Math.PI / 2)), (int) (p1.start.y - minDist * Math.sin(p1.angle + Math.PI / 2)));
		polygon.addPoint((int) (p1.end.x - minDist * Math.cos(p1.angle + Math.PI / 2)), (int) (p1.end.y - minDist * Math.sin(p1.angle + Math.PI / 2)));
		polygon.addPoint((int) (p1.end.x + minDist * Math.cos(p1.angle + Math.PI / 2)), (int) (p1.end.y + minDist * Math.sin(p1.angle + Math.PI / 2)));
		Area a = new Area(polygon);
		// two circles
		Area e1 = new Area(new Ellipse2D.Double(p1.start.x - minDist, p1.start.y - minDist, minDist * 2, minDist * 2));
		Area e2 = new Area(new Ellipse2D.Double(p1.end.x - minDist, p1.end.y - minDist, minDist * 2, minDist * 2));
		a.add(e1);
		a.add(e2);
		buffer.setColor(new Color(255, 0, 0, 78));
		buffer.fill(a);
		buffer.setColor(Color.red);
		buffer.setStroke(new BasicStroke(2));
		buffer.draw(a);
	}

	// Start of the program. Set-up stuff happens here!
	void restart()
	{
		frameTimer.stop();
		System.setProperty("sun.java2d.opengl", "True");

		EPgenerator.initializeElementWeightedList();
		PowerGenerator.initializeTables();
		Ability.initializeDescriptions();
		Resources.initialize();
		NameGenerator.initialize();
		Person.resetIDs();
		Environment.resetIDs();

		niceHotKeys = new Point[10];
		updateNiceHotkeys();

		menuStuff = new ArrayList<MenuElement>();

		// ~~~TEMPORARY TESTING~~~

		environmentList = new ArrayList<Environment>();
		world = new Environment[5][5];
		worldCoordsX = 2;
		worldCoordsY = 2;
		world[worldCoordsX][worldCoordsY] = new Environment(worldCoordsX * squareSize * 48, worldCoordsY * squareSize * 48, 48, 48);
		environmentList.add(world[worldCoordsX][worldCoordsY]);
		env = world[worldCoordsX][worldCoordsY];
		env.tempBuild();
		// env.subEnvironments.add(new Environment(env.globalX + squareSize * 3, env.globalY + squareSize * 3, 10, 10));
		// Environment sub = env.subEnvironments.get(0);
		// environmentList.add(sub);
		// sub.tempBuild();
		// for (int i = 3; i < 3 + sub.width; i++)
		// for (int j = 3; j < 3 + sub.height; j++)
		// env.addWall(i, j, -2, true);
		// sub.parent = env;

		player = new Player(96 * 15, 96 * 15);
		player.tempTrigger();
		// player.abilities.add(Ability.ability("Force Shield", 5));
		// player.abilities.add(Ability.ability("Beam <Energy>", 5));
		player.defaultHotkeys();
		updateNiceHotkeys();
		env.people.add(player);
		camera = new Point3D((int) player.x, (int) player.y, (int) player.z + 25);
		player.rename();

		Person shmulik = new NPC(96 * 12, 96 * 18, Strategy.AGGRESSIVE);
		shmulik.abilities.add(Ability.ability("Beam <Energy>", 6));
		shmulik.abilities.add(Ability.ability("Flight II", 5));
		shmulik.abilities.add(Ability.ability("Force Shield", 3));
		shmulik.abilities.add(Ability.ability("Ball <Earth>", 6));
		shmulik.abilities.add(Ability.ability("Heal I", 3));
		shmulik.name = "Shmulik";
		// env.people.add(shmulik);

		// three unpowered thugs against you
		for (int i = 0; i < 3; i++)
		{
			Person person = new NPC((int) (100 + Math.random() * (env.widthPixels - 200)), (int) (100 + Math.random() * (env.heightPixels - 200)), Strategy.AGGRESSIVE);
			person.commanderID = 2;
			env.people.add(person);
		} // four individual powered people, passive until damaged
		for (int i = 0; i < 4; i++)
		{
			Person person = new NPC((int) (100 + Math.random() * (env.widthPixels - 200)), (int) (100 + Math.random() * (env.heightPixels - 200)), Strategy.HALF_PASSIVE);
			if (i == 0)
			{
				person.abilities.add(Ability.ability("Explosive Fists", 5));
				person.DNA = Arrays.asList(new EP(28, 10));
			}
			if (i == 1)
			{
				person.abilities.add(Ability.ability("Toughness II", 2));
				person.abilities.add(Ability.ability("Strength II", 2));
				person.abilities.add(Ability.ability("Leg Muscles", 1));
				person.DNA = Arrays.asList(new EP(17, 5), new EP(13, 5));
			}
			if (i == 2)
			{
				person.abilities.add(Ability.ability("Protective Bubble I", 3));
				person.abilities.add(Ability.ability("Beam <Energy>", 3));
				person.DNA = Arrays.asList(new EP(21, 4), new EP(6, 6));
			}
			if (i == 3)
			{
				person.abilities.add(Ability.ability("Leg Muscles", 3));
				person.abilities.add(Ability.ability("Trail <Acid>", 2));
				pressAbilityKey(person.abilities.get(3), true, person); // start the trail
				pressAbilityKey(person.abilities.get(3), false, person); // start the trail
				person.abilities.add(Ability.ability("Elemental Resistance <Acid>", 5));
				person.DNA = Arrays.asList(new EP(18, 3), new EP(7, 7));
			}
			person.rotation = TAU * Math.random();
			person.rename();
			env.people.add(person);
		}

		// Randomize stats a bit
		for (Person p : env.people)
		{
			p.changeStat((int) (Math.random() * 6), (int) (Math.random() * 3 - 1)); // -1 or 0 or +1
			p.changeStat((int) (Math.random() * 6), (int) (Math.random() * 3 - 1)); // -1 or 0 or +1
			p.changeStat((int) (Math.random() * 6), (int) (Math.random() * 3 - 1)); // -1 or 0 or +1
		}

		// Fix walls spawning on people
		for (Person p : env.people)
			env.removeAroundPerson(p);

		frameTimer.start();
	}

	void pressAbilityKey(Ability ability, boolean press, Person p)
	{
		for (int i = 0; i < p.abilities.size(); i++)
			if (p.abilities.get(i).equals(ability))
			{
				pressAbilityKey(i, press, p);
				return;
			}
		MAIN.errorMessage("Keep Beach City Weird");
	}

	void pressAbilityKey(int abilityIndex, boolean press, Person p)
	{
		if (p.dead)
			return; // TODO is there a neater way of resolving this?
		// n is between 1 and 10; checkHotkey need a number between 0 and 9. So.... n-1.
		if (stopUsingPower)
		{
			if (!press)
				stopUsingPower = false;
		}
		else
		{
			if (p.abilities.size() <= abilityIndex || abilityIndex == -1)
			{
				errorMessage("No such ability index for p!: " + abilityIndex);
				return;
			}
			Ability a = p.abilities.get(abilityIndex);
			if (press)
			{
				updateTargeting(p, a);
				if (p.abilityTryingToRepetitivelyUse == -1)
				{
					if (a.maintainable)
					{
						if (!p.maintaining)
						{
							p.abilityMaintaining = abilityIndex;
							if (!a.on)
							{
								if (!a.disabled)
									if (!p.onlyNaturalAbilities || a.natural)
										a.use(env, p, p.target);
							}
						} // Can't start a maintainable ability while maintaining another (or the same one)
					}
					else
					{
						if (a.instant && !a.hasTag("on-off")) // Instant ability, without aim
						{ // TODO does this make sense? where's the on-off stuff?
							if (!a.disabled)
								if (!p.onlyNaturalAbilities || a.natural)
									a.use(env, p, p.target);
							p.abilityTryingToRepetitivelyUse = abilityIndex;
						}
						else
						{
							p.abilityAiming = abilityIndex; // straightforward
							if (a instanceof Portals) // TODO make this for any ability that does stuff while aiming
								a.updatePlayerTargeting(env, player, p.target, 0);
						}
					}
				}
				// if trying to use ability while repetitively trying another, doesn't work
			}
			else if (p.abilityAiming != -1 && p.abilityAiming == abilityIndex) // Activate currently aimed ability
			{
				if (!a.disabled || a instanceof Chronobiology) // can toggle chronobiology while it's disabled
					if (!p.onlyNaturalAbilities || a.natural)
						a.use(env, p, p.target);
				p.abilityAiming = -1;
			}
			else if (p.maintaining && p.abilityMaintaining == abilityIndex) // Stop maintaining currently maintained ability
			{
				if (!a.disabled)
					a.use(env, p, p.target); // stop maintaining
				p.abilityMaintaining = -1;
			}
			else if (!p.maintaining && p.abilityMaintaining == abilityIndex) // player's maintaining was stopped before player released key
				p.abilityMaintaining = -1;
			else if (p.abilityTryingToRepetitivelyUse == abilityIndex)
			{
				p.abilityTryingToRepetitivelyUse = -1;
			}

		}
	}

	void playerPressHotkey(int n, boolean press)
	{
		if (!paused || !press)
		{
			keyPressFixingMethod(n, press);
		}
		else
		{
			// Unbinding hotkeys
			if (press && (pauseHoverAbility == -1 || !player.abilities.get(pauseHoverAbility).hasTag("passive"))) // the order of the || is important
			{
				if (pauseHoverAbility == -1 && hotkeySelected == n - 1)
					hotkeySelected = -1;
				player.hotkeys[n - 1] = pauseHoverAbility;
				updateNiceHotkeys();
			}
			player.abilityAiming = -1;

			// if keys are released during pause it is a problem? TODO test
		}
	}

	boolean[] presses = new boolean[10];

	void keyPressFixingMethod(int n, boolean press)
	{
		// method is called each frame
		// this entire method is to prevent the bug that happens when the keyboard stops giving "press" signals but haven't yet sent the "release" signal
		if (n != -1) // n should be 1-10
			presses[n - 1] = press;
		else
			return;
		if (player.hotkeys[n - 1] == -1)
			return;
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

	double applyGravityAndFrictionAndReturnFriction(Person p, double deltaTime)
	{
		deltaTime *= p.timeEffect;
		double velocity = Math.sqrt(p.xVel * p.xVel + p.yVel * p.yVel);
		double moveDirectionAngle = Math.atan2(p.yVel, p.xVel);

		if (p.z > 1 && p.z + deltaTime * p.zVel < 0.5)
		{ // TODO WTF?
			// = when framerate causes problems
			p.z = 0.9;
			env.movePerson(p, deltaTime); // to test for wall-touching
		}

		p.z += deltaTime * p.zVel;

		// gravity
		p.zVel -= 100 * gravity * deltaTime;
		if (p.z < 0)
		{
			p.z = 0;
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
		// fly-punching
		if (p.abilityTryingToRepetitivelyUse != -1 && p.flySpeed != -1 && p.abilities.get(p.abilityTryingToRepetitivelyUse).name.equals("Punch"))
		{
			return 0;
		}
		// landing
		if (p.z <= 1 && p.z >= 0 && p.zVel < -200 * gravity * deltaTime)
		{
			boolean safeLanding = false;
			for (Ability a : p.abilities) // stop flying when landing this way
				if (a.hasTag("flight") && a.on)
				{
					// if landed on a wall
					if (!p.ghostMode && env.wallTypes[(int) (p.x) / squareSize][(int) (p.y) / squareSize] != -1)
					{
						p.z = 1;
						a.use(env, p, p.target);
						safeLanding = true;
						p.zVel = 0;
					}
					else if (p.z <= 0.1)
					{
						p.z = 0;
						a.use(env, p, p.target);
						safeLanding = true;
						p.zVel = 0;
					}
					else
						return 0;
				}
			if (!safeLanding)
			{
				// fall damage
				double damage = p.zVel * p.zVel * 0.0002 * p.timeEffect;
				env.hitPerson(p, damage, 0, 0, -1); // blunt
				if (p.zVel * p.zVel * p.mass * 0.0001 > 15)
					p.sounds.get(2).play(); // fall hit
			}
		}

		// Friction
		int gridX = Math.min(Math.max((int) (p.x) / squareSize, 0), env.width - 1);
		int gridY = Math.min(Math.max((int) (p.y) / squareSize, 0), env.height - 1);
		if (p.z < 0.1 || (p.z == 1 && !p.ghostMode && env.wallTypes[gridX][gridY] != -1)) // on ground or on a wall
		{
			p.zVel = 0;
			int floorType = env.floorTypes[gridX][gridY];
			int poolType = env.poolTypes[gridX][gridY];
			int wallType = env.wallTypes[gridX][gridY];
			double friction = Environment.floorFriction[floorType];

			if (poolType != -1)
				friction = Environment.poolFriction[poolType];
			if (p.z == 1 && p.zVel == 0 && wallType != -1)
				friction = Environment.wallFriction[wallType];
			if (!p.prone)
				friction *= standingFrictionBenefit;
			if (p.ghostMode)
				friction *= ghostFrictionMultiplier;
			for (Ability a : p.abilities)
				if (a.on)
					if (a instanceof Elastic)
						friction *= 0.10; // 10%

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

		return 0;
	}

	void paintBuffer(Graphics g)
	{
		// NOTICE! THE ORDER OF DRAWING OPERATIONS IS ACTUALLY IMPORTANT!
		Graphics2D buffer = (Graphics2D) g;

		// Fill everything with black
		buffer.setColor(Color.black);
		buffer.fillRect(0, 0, frameWidth, frameHeight);

		zoomLevel /= (player.z * heightZoomRatio + 1);

		// Move "camera" to position
		buffer.scale(zoomLevel, zoomLevel);
		buffer.translate(0.5 * frameWidth / zoomLevel, 0.5 * frameHeight / zoomLevel);
		buffer.translate(-camera.x, -camera.y);
		buffer.rotate(-cameraRotation, camera.x, camera.y);

		final int safetyDistance = 50;
		Rectangle bounds = null;
		if (cameraRotation * 180 / Math.PI % 180 == 0)
			bounds = new Rectangle((int) (camera.x - frameWidth / 2 * (player.z * heightZoomRatio + 1) / zoomLevel) - safetyDistance,
					(int) (camera.y - frameHeight / 2 * (player.z * heightZoomRatio + 1) / zoomLevel) - safetyDistance,
					(int) (frameWidth * (player.z * heightZoomRatio + 1) / zoomLevel) + 2 * safetyDistance, (int) (frameHeight * (player.z * heightZoomRatio + 1) / zoomLevel) + 2 * safetyDistance);
		else if ((cameraRotation * 180 / Math.PI + 90) % 180 == 0)
			bounds = new Rectangle((int) (camera.x - frameHeight / 2 * (player.z * heightZoomRatio + 1) / zoomLevel) - safetyDistance,
					(int) (camera.y - frameWidth / 2 * (player.z * heightZoomRatio + 1) / zoomLevel) - safetyDistance,
					(int) (frameHeight * (player.z * heightZoomRatio + 1) / zoomLevel) + 2 * safetyDistance, (int) (frameWidth * (player.z * heightZoomRatio + 1) / zoomLevel) + 2 * safetyDistance);
		else
		{
			// already overshoots, does not need to include safetyDistance
			double halfBoundsDiagonal = Math.sqrt(frameWidth * frameWidth + frameHeight * frameHeight) / 2; // If you don't want a square-root calculation, either make the calculation once and store it in a variable or use frameWidth+frameHeight/2
			// it creates the axis-aligned square that surrounds the circle around the camera, whose radius is half of the diagonal of the screen (in in-game pixels)
			halfBoundsDiagonal = halfBoundsDiagonal * (player.z * heightZoomRatio + 1) / zoomLevel;
			bounds = new Rectangle((int) (camera.x - halfBoundsDiagonal), (int) (camera.y - halfBoundsDiagonal), (int) (halfBoundsDiagonal * 2), (int) (halfBoundsDiagonal * 2));
		}

		double viewRangeDistance = Math.max(player.flightVisionDistance * player.z, 1350);
		List<Environment> drawnEnvironments = new ArrayList<Environment>();
		if (env.parent != null)
			drawnEnvironments.add(env.parent);
		else
		// world
		{
			double viewRangeDistancePow2 = viewRangeDistance * viewRangeDistance;
			for (int i = 0; i < world.length; i++)
				for (int j = 0; j < world[0].length; j++)
					if (world[i][j] != null && world[i][j].id != env.id)
					{
						double x = world[i][j].globalX - env.globalX;
						double y = world[i][j].globalY - env.globalY;
						double w = world[i][j].widthPixels;
						double h = world[i][j].widthPixels;
						if (player.x < x && x - player.x > viewRangeDistance)
							continue;
						if (player.y < y && y - player.y > viewRangeDistance)
							continue;
						if (player.x > x && player.x - x > w + viewRangeDistance)
							continue;
						if (player.y > y && player.y - y > h + viewRangeDistance)
							continue;
						boolean visible = true;
						if ((player.x < x || player.x >= x + w) && (player.y < y || player.y >= y + h))
							visible = false;
						if (Methods.DistancePow2(player.x, player.y, x, y) < viewRangeDistancePow2)
							visible = true;
						else if (Methods.DistancePow2(player.x, player.y, x + w, y) < viewRangeDistancePow2)
							visible = true;
						else if (Methods.DistancePow2(player.x, player.y, x, y + h) < viewRangeDistancePow2)
							visible = true;
						else if (Methods.DistancePow2(player.x, player.y, x + w, y + h) < viewRangeDistancePow2)
							visible = true;
						if (visible)
							drawnEnvironments.add(world[i][j]);
					}
		}
		drawnEnvironments.add(env);
		drawnEnvironments.addAll(env.subEnvironments); // but not THEIR subEnvironments!
		for (Environment e : drawnEnvironments)
		{
			buffer.translate(e.globalX - env.globalX, e.globalY - env.globalY);
			player.x -= e.globalX - env.globalX;
			player.y -= e.globalY - env.globalY;
			bounds.x -= e.globalX - env.globalX;
			bounds.y -= e.globalY - env.globalY;
			if (player.limitedVisibility)
			{
				drawExtraEnvironmentInfo(buffer, e);

				// visibility
				if (!player.seenBefore.containsKey(e))
				{
					player.seenBefore.put(e, new int[e.width][e.height]);
				}
				if (player.visibleArea.get(e) == null || frameNum % 2 == 0)
				{
					// ~7 ms
					Area visibleArea = e.updateVisibility(player, player.seenBefore.get(e));
					Ellipse2D viewRange = new Ellipse2D.Double(player.x - viewRangeDistance, player.y - viewRangeDistance, 2 * viewRangeDistance, 2 * viewRangeDistance);
					visibleArea.intersect(new Area(viewRange));
					player.visibleArea.put(e, visibleArea);
					player.visibleRememberArea.put(e, visibleArea);
				}
				// Draws everything within the player's view range that is inside the rememberArea.
				if (playerRememberPreviouslySeenPlaces)
					buffer.setClip(player.visibleRememberArea.get(e));
				else
					buffer.setClip(player.visibleArea.get(e));
				e.drawFloor(buffer, bounds);
				drawBottomEffects(buffer, e);
				e.draw(buffer, (int) camera.z, bounds);
				drawTopEffects(buffer, e);
				buffer.setClip(null);
				// Draw this environment's boundaries
				buffer.setColor(new Color(128, 255, 255, 60));
				buffer.setStroke(new BasicStroke(6));
				buffer.drawRect(0, 0, e.widthPixels, e.heightPixels);
				buffer.setStroke(new BasicStroke(3));
				buffer.drawRect(0, 0, e.widthPixels, e.heightPixels);
				buffer.setStroke(new BasicStroke(2));
				buffer.drawRect(0, 0, e.widthPixels, e.heightPixels);
			}
			else
			{
				e.drawFloor(buffer, bounds);
				drawBottomEffects(buffer, e);
				e.draw(buffer, (int) camera.z, bounds); //TODO is camera.z *supposed* to be an INTEGER? CHECK THIS!
				drawTopEffects(buffer, e);
			}
			buffer.translate(-(e.globalX - env.globalX), -(e.globalY - env.globalY));
			player.x += e.globalX - env.globalX;
			player.y += e.globalY - env.globalY;
			bounds.x += e.globalX - env.globalX;
			bounds.y += e.globalY - env.globalY;
		}

		if (hotkeySelected != -1 && player.hotkeys[hotkeySelected] != -1)
			drawRange(buffer, player.abilities.get(player.hotkeys[hotkeySelected]));
		if (pauseHoverAbility != -1)
			drawRange(buffer, player.abilities.get(pauseHoverAbility));
		if (hotkeyHovered != -1 && player.hotkeys[hotkeyHovered] != -1)
			drawRange(buffer, player.abilities.get(player.hotkeys[hotkeyHovered]));
		if (player.abilityAiming != -1)
			drawAim(buffer);

		// temp
		buffer.setColor(Color.red);
		if (drawLine != null)
			buffer.drawLine((int) (drawLine.getX1()), (int) (drawLine.getY1()), (int) (drawLine.getX2()), (int) (drawLine.getY2()));
		if (drawRect != null)
			buffer.drawRect((int) (drawRect.getX()), (int) (drawRect.getY()), (int) (drawRect.getWidth()), (int) (drawRect.getHeight()));

		for (Environment e : drawnEnvironments)
		{
			buffer.translate(e.globalX - env.globalX, e.globalY - env.globalY);
			player.x -= e.globalX - env.globalX;
			player.y -= e.globalY - env.globalY;
			drawExtraPeopleInfo(buffer, e);
			buffer.translate(-(e.globalX - env.globalX), -(e.globalY - env.globalY));
			player.x += e.globalX - env.globalX;
			player.y += e.globalY - env.globalY;
			
			if (env.showDamageNumbers)
			for (Person p : env.people)
				p.drawUITexts(buffer, camera.z, cameraRotation);
			for (UIText ui : env.uitexts) // TODO add height to environment UITexts
				ui.draw(buffer, 0, 0);
		}

		// Move camera back
		buffer.rotate(cameraRotation, camera.x, camera.y);
		buffer.translate(camera.x, camera.y);
		buffer.translate(-0.5 * frameWidth / zoomLevel, -0.5 * frameHeight / zoomLevel);
		buffer.scale((double) (1 / zoomLevel), (double) (1 / zoomLevel));

		zoomLevel *= (player.z * heightZoomRatio + 1);
		// User Interface
		drawPlayerStats(buffer);
		drawHotkeysAndEffects(buffer);
		if (paused)
			drawPause(buffer);
		// Tooltip
		if (tooltipPoint.x != -1) // can also check y but that's silly
		{
			Point[] p = new Point[]
			{ new Point(-1, 0), new Point(1, 0), new Point(0, 1), new Point(0, -1), new Point(0, 0) };
			for (int j = 0; j < 5; j++)
			{
				if (j < 4)
					buffer.setColor(Color.black);
				else
					buffer.setColor(Color.white);
				buffer.setFont(new Font("Serif", Font.PLAIN, (int) (20 * UIzoomLevel)));
				int i = tooltip.indexOf("\n");
				if (i != -1) // if extended tooltip
				{
					buffer.drawString(tooltip.substring(0, i), tooltipPoint.x + p[j].x, tooltipPoint.y + p[j].y);
					buffer.setFont(new Font("Serif", Font.ITALIC, 20));
					buffer.drawString(tooltip.substring(i + 1), tooltipPoint.x + p[j].x - 8, tooltipPoint.y + p[j].y + 25);
				}
				else
					buffer.drawString(tooltip, tooltipPoint.x + p[j].x, tooltipPoint.y + p[j].y);
			}
		}

		if (timeSinceLastScreenshot < 2)
			drawScreenshot(buffer);

		// FPS
		if (showFPS)
		{
			buffer.setFont(FPSFont);
			buffer.setColor(Color.white);
			buffer.drawString("" + FPS, frameWidth - 50 - 1, 50 - 1);
			buffer.drawString("" + FPS, frameWidth - 50 + 1, 50 - 1);
			buffer.drawString("" + FPS, frameWidth - 50 - 1, 50 + 1);
			buffer.drawString("" + FPS, frameWidth - 50 + 1, 50 + 1);
			buffer.setColor(Color.black);
			buffer.drawString("" + FPS, frameWidth - 50, 50);
		}
	}

	void drawExtraEnvironmentInfo(Graphics2D buffer, Environment e)
	{
		// like drawExtraPeopleInfo but for walls and stuff
		double[] elementSenses = new double[12];
		for (int i = 0; i < elementSenses.length; i++)
			elementSenses[i] = 0;
		double drawStructureDistancePow2 = 0;
		for (Ability a : player.abilities)
			if (a.on)
				switch (a.justName())
				{
				case "Clairvoyance":
				case "Sense Structure":
					drawStructureDistancePow2 = Math.pow(a.range, 2);
					break;
				case "Sense Element":
					elementSenses[a.elementNum] = Math.pow(a.range, 2);
					break;
				default:
					break;
				}
		for (int x = 0; x < e.widthPixels; x += squareSize)
			for (int y = 0; y < e.heightPixels; y += squareSize)
			{
				int gx = (int) (x / squareSize), gy = (int) (y / squareSize);
				double distPow2 = Methods.DistancePow2(gx * squareSize + squareSize / 2, gy * squareSize + squareSize / 2, player.x, player.y);
				// Sense Structure
				if (distPow2 <= drawStructureDistancePow2)
					if (e.wallTypes[gx][gy] != -1)
					{
						buffer.setStroke(new BasicStroke(3));
						buffer.setColor(new Color(40, 40, 40));
						buffer.drawRect(gx * squareSize + 3, gy * squareSize + 3, squareSize - 6, squareSize - 6);
					}
				// Sense Element - Walls
				if (e.wallTypes[gx][gy] >= 0 && e.wallTypes[gx][gy] < 12)
					if (distPow2 <= elementSenses[e.wallTypes[gx][gy]])
					{
						buffer.setStroke(new BasicStroke(3));
						buffer.setColor(EP.elementColors[e.wallTypes[gx][gy]]);
						buffer.drawRect(gx * squareSize + 3, gy * squareSize + 3, squareSize - 6, squareSize - 6);
					}
				// Sense Element - Pools
				if (e.poolTypes[gx][gy] >= 0 && e.poolTypes[gx][gy] < 12)
				{
					if (distPow2 <= elementSenses[e.poolTypes[gx][gy]])
					{
						buffer.setStroke(new BasicStroke(3));
						buffer.setColor(EP.elementColors[e.poolTypes[gx][gy]]);
						buffer.drawOval(gx * squareSize + 3, gy * squareSize + 3, squareSize - 6, squareSize - 6);
					}
				}

			}
	}

	void drawExtraPeopleInfo(Graphics2D buffer, Environment e)
	{
		// exists after coordinate shift
		double drawLifeDistancePow2 = 0, drawMovementDistancePow2 = 0, drawManaDistancePow2 = 0, drawStaminaDistancePow2 = 0, drawParahumansDistancePow2 = 0;
		double[] elementSenses = new double[12];
		for (Ability a : player.abilities)
			if (a.on)
				switch (a.justName())
				{
				case "Sense Parahumans":
					drawParahumansDistancePow2 = Math.pow(a.range, 2);
					break;
				case "Sense Life":
					drawLifeDistancePow2 = Math.pow(a.range, 2);
					break;
				case "Sense Movement":
					drawMovementDistancePow2 = Math.pow(a.range, 2);
					break;
				case "Sense Mana and Stamina":
					drawManaDistancePow2 = Math.pow(a.range, 2);
					drawStaminaDistancePow2 = Math.pow(a.range, 2);
					break;
				case "Sense Element":
					elementSenses[a.elementNum] = Math.pow(a.range, 2);
					break;
				case "Clairvoyance":
					drawParahumansDistancePow2 = Math.pow(a.range, 2);
					drawMovementDistancePow2 = Math.pow(a.range, 2);
					drawLifeDistancePow2 = Math.pow(a.range, 2);
					drawManaDistancePow2 = Math.pow(a.range, 2);
					drawStaminaDistancePow2 = Math.pow(a.range, 2);
				case "Sense Powers":
					Sense_Powers spAbility = (Sense_Powers) a;
					// summing up the levels

					// this is supposed to look fantastic.
					buffer.setStroke(new BasicStroke(4));
					double radius = frameHeight / 3;
					int[] elementIndexes = new int[]
					{ 21, 22, 7, 23, 15, 11, 16, 30, 5, 2, 15, 12, 19, 1, 17, 25, 4, 20, 26, 9, 6, 27, 13, 24, 10, 8, 28, 0, 31, 29, 3, 18 };
					Point center = new Point((int) (player.x), (int) (player.y));
					buffer.translate(center.x, center.y);
					buffer.rotate(cameraRotation);
					buffer.rotate(spAbility.angle);
					for (int i = 0; i < elementIndexes.length; i++)
					{
						double angle = TAU / elementIndexes.length * i - 0.3 * TAU;
						int elementLevel = 2 * (int) spAbility.details[elementIndexes[i]];
						if (elementLevel <= 0)
							continue;
						Color color = EP.elementColors[elementIndexes[i]];
						buffer.setColor(color);
						buffer.rotate(angle);
						buffer.fillRect(-35, (int) (-radius - elementLevel), 70, elementLevel);
						buffer.rotate(-angle);
					}
					buffer.rotate(-spAbility.angle);
					buffer.rotate(-cameraRotation);
					buffer.translate(-center.x, -center.y);
					break;
				default:
					break;
				}

		for (Person p : e.people)
		{
			// does not draw info for the player herself/himself
			if (!p.equals(player) && p.z <= camera.z)
			{
				double distancePow2 = Methods.DistancePow2(player.x, player.y, p.x, p.y);

				buffer.translate(p.x, p.y);
				buffer.scale(p.z * MAIN.heightZoomRatio + 1, p.z * MAIN.heightZoomRatio + 1);
				buffer.translate(-p.x, -p.y);

				// Sense Parahumans
				if (p.isParahuman())
					if (distancePow2 < drawParahumansDistancePow2)
					{
						buffer.setStroke(new BasicStroke(3));
						// if within sight, color is less noticeable
						if (!player.limitedVisibility || player.visibleRememberArea.get(e).contains(p.x, p.y))
							buffer.setColor(new Color(200, 210, 255, 91));
						else
							buffer.setColor(new Color(200, 210, 255));
						int radius = 20;
						// draws a circle with an X on it
						buffer.drawOval((int) (p.x - radius), (int) (p.y - radius), radius * 2, radius * 2);
						buffer.drawLine((int) (p.x + radius * sqrt2by2), (int) (p.y + radius * sqrt2by2), (int) (p.x - radius * sqrt2by2), (int) (p.y - radius * sqrt2by2));
						buffer.drawLine((int) (p.x - radius * sqrt2by2), (int) (p.y + radius * sqrt2by2), (int) (p.x + radius * sqrt2by2), (int) (p.y - radius * sqrt2by2));
					}

				buffer.setStroke(new BasicStroke(3));
				// Sense Element
				for (int elementNum = 0; elementNum < elementSenses.length; elementNum++)
					if (distancePow2 < elementSenses[elementNum])
					{
						Color color = EP.elementColors[elementNum];
						if (!player.limitedVisibility || player.visibleRememberArea.get(e).contains(p.x, p.y))
							buffer.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 91)); // more transparent
						else
							buffer.setColor(color);
						if (p.elementSensed(elementNum))
						{
							// draws a circle with an X on it
							int radius = 10 + elementNum; // so that elements don't fully intersect each other
							buffer.drawOval((int) (p.x - radius), (int) (p.y - radius), radius * 2, radius * 2);
							buffer.drawLine((int) (p.x + radius * sqrt2by2), (int) (p.y + radius * sqrt2by2), (int) (p.x - radius * sqrt2by2), (int) (p.y - radius * sqrt2by2));
							buffer.drawLine((int) (p.x - radius * sqrt2by2), (int) (p.y + radius * sqrt2by2), (int) (p.x + radius * sqrt2by2), (int) (p.y - radius * sqrt2by2));
						}
					}
				// Sense Movement
				if (distancePow2 < drawMovementDistancePow2 && p.velocityPow2() > 400) // 400 - because I felt like it
				{
					// draws a red arrow, length equal to velocity
					double lengthOfArrow = 0.1 * p.velocity(), angleOfArrow = p.angle(), arrowSidewaysThingieLength = 20;
					buffer.setColor(Color.red);
					Point endOfArrow = new Point((int) (p.x + lengthOfArrow * Math.cos(angleOfArrow)), (int) (p.y + lengthOfArrow * Math.sin(angleOfArrow)));
					buffer.setStroke(new BasicStroke(3));
					buffer.drawLine((int) p.x, (int) p.y, endOfArrow.x, endOfArrow.y);
					buffer.drawLine(endOfArrow.x, endOfArrow.y, (int) (endOfArrow.x + arrowSidewaysThingieLength * Math.cos(angleOfArrow + Math.PI - Math.PI / 5)),
							(int) (endOfArrow.y + arrowSidewaysThingieLength * Math.sin(angleOfArrow + Math.PI - Math.PI / 5)));
					buffer.drawLine(endOfArrow.x, endOfArrow.y, (int) (endOfArrow.x + arrowSidewaysThingieLength * Math.cos(angleOfArrow + Math.PI + Math.PI / 5)),
							(int) (endOfArrow.y + arrowSidewaysThingieLength * Math.sin(angleOfArrow + Math.PI + Math.PI / 5)));
				}
				// Name (DOES NOT draw name out of visibility)
				if (!player.limitedVisibility || player.visibleRememberArea.get(e).contains(p.x, p.y))
				{
					Color nameColor = Color.white; // neutral
					if (p.commanderID == player.commanderID) // friendly
						nameColor = Color.green;
					if (p.commanderID != player.commanderID) // enemy. TODO make neutrals also possible
						nameColor = Color.red;
					p.drawName(buffer, nameColor, cameraRotation);
				}
				// life/stamina/mana
				p.drawData(buffer, distancePow2 < drawLifeDistancePow2, distancePow2 < drawManaDistancePow2, distancePow2 < drawStaminaDistancePow2, cameraRotation);

				buffer.translate(p.x, p.y);
				buffer.scale(1 / (p.z * MAIN.heightZoomRatio + 1), 1 / (p.z * MAIN.heightZoomRatio + 1));
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
			}
			else if (timeSinceLastScreenshot < secondpart)
			{
				buffer.drawImage(lastScreenshot, (int) (0.8 * frameWidth) - 10, (int) (0.8 * frameHeight) - 10, (int) (frameWidth - 0.8 * frameWidth), (int) (frameHeight - 0.8 * frameHeight), this);
				buffer.drawRect((int) (0.8 * frameWidth) - 10, (int) (0.8 * frameHeight) - 10, (int) (frameWidth - 0.8 * frameWidth), (int) (frameHeight - 0.8 * frameHeight));
			}
			else
			{
				buffer.drawImage(lastScreenshot, (int) (timeSinceLastScreenshot / 2 * frameWidth) - 10, (int) (timeSinceLastScreenshot / 2 * frameHeight) - 10,
						(int) (frameWidth - timeSinceLastScreenshot / 2 * frameWidth), (int) (frameHeight - timeSinceLastScreenshot / 2 * frameHeight), this);
				buffer.drawRect((int) (timeSinceLastScreenshot / 2 * frameWidth) - 10, (int) (timeSinceLastScreenshot / 2 * frameHeight) - 10,
						(int) (frameWidth - timeSinceLastScreenshot / 2 * frameWidth), (int) (frameHeight - timeSinceLastScreenshot / 2 * frameHeight));
			}
		}
		else
		{
			buffer.setStroke(new BasicStroke(1));
			buffer.setColor(Color.red);
			buffer.fillRect(frameWidth - 140, frameHeight - 35, 120, 15);
			buffer.setColor(Color.black);
			buffer.drawRect(frameWidth - 140, frameHeight - 35, 120, 15);
			buffer.drawString("-Screenshot FAILED-", frameWidth - 137, frameHeight - 23);
		}
	}

	void drawBottomEffects(Graphics2D buffer, Environment e)
	{
		for (VisualEffect eff : e.visualEffects)
			if (!eff.onTop)
				eff.draw(buffer);
	}

	void drawTopEffects(Graphics2D buffer, Environment e)
	{
		for (VisualEffect eff : e.visualEffects)
			if (eff.onTop)
				eff.draw(buffer);
	}

	void drawPlayerStats(Graphics2D buffer)
	{
		// TEMP. should be fancier in real game, obviously

		// starting beyond window title bar
		buffer.translate(8, 30);
		// Name
		buffer.setFont(new Font("Monospaced", Font.BOLD, (int) (20 * UIzoomLevel)));
		buffer.setColor(Color.white);
		buffer.drawString(player.name, (int) (20 * UIzoomLevel) - 1, 25 - 1);
		buffer.drawString(player.name, (int) (20 * UIzoomLevel) - 1, 25 + 1);
		buffer.drawString(player.name, (int) (20 * UIzoomLevel) + 1, 25 - 1);
		buffer.drawString(player.name, (int) (20 * UIzoomLevel) + 1, 25 + 1);
		buffer.setColor(Color.black);
		buffer.drawString(player.name, (int) (20 * UIzoomLevel), 25);
		// Health, Mana, Stamina, Charge?
		buffer.setStroke(new BasicStroke(1));
		// assuming neither of the following stats is too high (< x10 normal amount)
		buffer.setColor(Color.red);
		buffer.fillRect((int) (20 * UIzoomLevel), (int) (35 * UIzoomLevel), (int) (player.life * 2 * UIzoomLevel), (int) (15 * UIzoomLevel));
		buffer.setColor(Color.blue);
		buffer.fillRect((int) (20 * UIzoomLevel), (int) (60 * UIzoomLevel), (int) (player.mana * 20 * UIzoomLevel), (int) (15 * UIzoomLevel));
		buffer.setColor(Color.green);
		buffer.fillRect((int) (20 * UIzoomLevel), (int) (85 * UIzoomLevel), (int) (player.stamina * 20 * UIzoomLevel), (int) (15 * UIzoomLevel));
		if (player.hasChargeAbility)
		{
			buffer.setColor(Color.orange);
			buffer.fillRect((int) (20 * UIzoomLevel), (int) (110 * UIzoomLevel), (int) (player.charge * 2 * UIzoomLevel), (int) (15 * UIzoomLevel));
		}

		// draw costs of selected (aimed) power
		if (hotkeySelected != -1 && player.hotkeys[hotkeySelected] != -1)
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
				case MANA:
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
				case STAMINA:
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
				case CHARGE:
					for (int i = 1; i < player.charge / cost; i++)
					{
						// darker rectangle
						if (i % 2 == 0)
							buffer.setColor(new Color(220, 122, 0));
						else
							buffer.setColor(new Color(240, 115, 0));
						buffer.fillRect((int) ((int) (20 * UIzoomLevel + player.charge * 2 * UIzoomLevel) - i * (int) (cost * 2 * UIzoomLevel) + 1 * UIzoomLevel), (int) (112 * UIzoomLevel),
								(int) (cost * 2 * UIzoomLevel - 1 * UIzoomLevel), (int) (12 * UIzoomLevel));
						// separating line
						buffer.setColor(new Color(170, 85, 0));
						buffer.fillRect((int) (20 * UIzoomLevel + player.charge * 2 * UIzoomLevel) - i * (int) (cost * 2 * UIzoomLevel), (int) (111 * UIzoomLevel), (int) (2 * UIzoomLevel),
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
		if (player.hasChargeAbility)
		{
			if (player.isChargingChargeAbility)
				buffer.setStroke(new BasicStroke((float) (5 * UIzoomLevel)));
			buffer.drawRect((int) (20 * UIzoomLevel), (int) (110 * UIzoomLevel), (int) (100 * 2 * UIzoomLevel), (int) (15 * UIzoomLevel));
		}
		buffer.setStroke(new BasicStroke((float) (1 * UIzoomLevel)));
		buffer.setColor(Color.red);
		buffer.drawRect((int) (20 * UIzoomLevel), (int) (35 * UIzoomLevel), (int) (player.maxLife * 2 * UIzoomLevel), (int) (15 * UIzoomLevel));
		buffer.setColor(Color.blue);
		buffer.drawRect((int) (20 * UIzoomLevel), (int) (60 * UIzoomLevel), (int) (player.maxMana * 20 * UIzoomLevel), (int) (15 * UIzoomLevel));
		buffer.setColor(Color.green);
		buffer.drawRect((int) (20 * UIzoomLevel), (int) (85 * UIzoomLevel), (int) (player.maxStamina * 20 * UIzoomLevel), (int) (15 * UIzoomLevel));
		if (player.hasChargeAbility)
		{
			if (player.isChargingChargeAbility)
			{
				buffer.setColor(Color.yellow);
				buffer.setStroke(new BasicStroke((float) (3 * UIzoomLevel)));
			}
			else
				buffer.setColor(Color.orange);
			buffer.drawRect((int) (20 * UIzoomLevel), (int) (110 * UIzoomLevel), (int) (100 * 2 * UIzoomLevel), (int) (15 * UIzoomLevel));
		}
		// Fly-mode height meter
		// not logarithmic!
		if (player.z > 0)
		{
			buffer.setStroke(new BasicStroke((float) (2 * UIzoomLevel)));
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
		for (int i = 0; i < niceHotKeys.length; i++)
			if (niceHotKeys[i] != null)
			{
				int x = niceHotKeys[i].x;
				int y = niceHotKeys[i].y;
				buffer.setStroke(new BasicStroke((float) (3 * UIzoomLevel)));
				buffer.setColor(Color.black);
				if (player.hotkeys[i] != -1)
				{
					// key
					buffer.setColor(Color.black);
					for (int a = -1; a <= 1; a += 2)
						for (int b = -1; b <= 1; b += 2)
							buffer.drawString(hotkeyStrings[i], x + a + (int) (12 * UIzoomLevel), y + b + (int) (76 * UIzoomLevel));
					buffer.setColor(Color.white);
					buffer.drawString(hotkeyStrings[i], x + (int) (12 * UIzoomLevel), y + (int) (76 * UIzoomLevel));

					// rectangle outline
					buffer.setStroke(new BasicStroke((float) (3 * UIzoomLevel)));
					buffer.setColor(Color.black);
					buffer.drawRect(x, y, (int) (60 * UIzoomLevel), (int) (60 * UIzoomLevel));
					// rectangle fill
					buffer.setColor(new Color(255, 255, 255, 89));
					buffer.fillRect(x, y, (int) (61 * UIzoomLevel), (int) (61 * UIzoomLevel));
					// ability icon
					Ability ability = player.abilities.get(player.hotkeys[i]);
					scaleBuffer(buffer, x, y, UIzoomLevel);
					buffer.drawImage(Resources.icons.get(ability.name), x + 1, y + 1, this);
					scaleBuffer(buffer, x, y, 1 / UIzoomLevel);

					// Cooldown and mana notifications
					if (ability.cooldownLeft != 0)
					{// note that when the cooldown is over it will "jump" from low transparency to full transparency
						buffer.setColor(new Color(0, 0, 0, (int) (130 + 100 * ability.cooldownLeft / ability.cooldown)));
						buffer.fillRect(x + (int) (UIzoomLevel), y + (int) (UIzoomLevel), (int) (60 * UIzoomLevel), (int) (60 * UIzoomLevel));
					}
					if (ability.cost > player.mana)
					{
						if (ability.justName().equals("Pool") && ability.cost - 1.5 <= player.mana)
							buffer.setColor(Color.yellow); // can only build low-cost pools next to other stuffs
						else if (ability.justName().equals("Wall") && 0.3 <= player.mana)
							buffer.setColor(Color.yellow); // repairing walls
						else
							buffer.setColor(Color.red);
						buffer.drawRect(x + (int) (-3 * UIzoomLevel), y + (int) (-3 * UIzoomLevel), (int) (66 * UIzoomLevel), (int) (66 * UIzoomLevel));
					}

					// ON/OFF
					if (ability.on)
					{
						if (ability instanceof Chronobiology)
						{
							if (((Chronobiology) ability).state)
							{
								buffer.setColor(Color.green);
								buffer.setStroke(new BasicStroke(2));
								buffer.drawLine(x + (int) (1 * UIzoomLevel), y + (int) (1 * UIzoomLevel), x + (int) (60 * UIzoomLevel), y + (int) (1 * UIzoomLevel));
								buffer.drawLine(x + (int) (1 * UIzoomLevel), y + (int) (1 * UIzoomLevel), x + (int) (1 * UIzoomLevel), y + (int) (60 * UIzoomLevel));
							}
							else
							{
								buffer.setColor(Color.magenta);
								buffer.setStroke(new BasicStroke(2));
								buffer.drawLine(x + (int) (60 * UIzoomLevel), y + (int) (60 * UIzoomLevel), x + (int) (60 * UIzoomLevel), y + (int) (1 * UIzoomLevel));
								buffer.drawLine(x + (int) (60 * UIzoomLevel), y + (int) (60 * UIzoomLevel), x + (int) (1 * UIzoomLevel), y + (int) (60 * UIzoomLevel));

							}
						}
						else
						{
							buffer.setColor(Color.cyan);
							buffer.setStroke(new BasicStroke(2));
							buffer.drawRect(x + (int) (1 * UIzoomLevel), y + (int) (1 * UIzoomLevel), (int) (59 * UIzoomLevel), (int) (59 * UIzoomLevel));
						}
					}
					else if (ability instanceof Portals) // if portals ability
						if (((Portals) ability).p1 != null)
						{
							// draw half of the "on" sign
							buffer.setColor(Color.cyan);
							buffer.setStroke(new BasicStroke(2));
							buffer.drawLine(x + (int) (1 * UIzoomLevel), y + (int) (1 * UIzoomLevel), x + (int) (1 * UIzoomLevel + 59 * UIzoomLevel), y + (int) (1 * UIzoomLevel));
							buffer.drawLine(x + (int) (1 * UIzoomLevel), y + (int) (1 * UIzoomLevel), x + (int) (1 * UIzoomLevel), y + (int) (1 * UIzoomLevel + 59 * UIzoomLevel));
						}

					// current power
					if (player.hotkeys[i] == player.abilityAiming || player.hotkeys[i] == player.abilityMaintaining || player.hotkeys[i] == player.abilityTryingToRepetitivelyUse)
					{
						buffer.setColor(Color.green);
						buffer.setStroke(new BasicStroke(2));
						buffer.drawRect(x + (int) (1 * UIzoomLevel), y + (int) (1 * UIzoomLevel), (int) (59 * UIzoomLevel), (int) (59 * UIzoomLevel));
					}

					// disabled?

					if (ability.disabled || (!ability.natural && player.onlyNaturalAbilities))
					{
						buffer.drawImage(Resources.disabled, x, y, null);
					}

					// selected power for targeting
					if (i == hotkeySelected)
					{
						int uiz = (int) UIzoomLevel;
						buffer.setColor(Color.cyan);
						buffer.fillRect(x + uiz, y + (int) (62 * UIzoomLevel), (int) (58 * UIzoomLevel), (int) (4 * UIzoomLevel)); // bottom
						buffer.fillRect(x + uiz, y + (int) (-5 * UIzoomLevel), (int) (58 * UIzoomLevel), (int) (4 * UIzoomLevel)); // top
						buffer.fillRect(x + (int) (-5 * UIzoomLevel), y + uiz, (int) (4 * UIzoomLevel), (int) (58 * UIzoomLevel)); // left
						buffer.fillRect(x + (int) (62 * UIzoomLevel), y + uiz, (int) (4 * UIzoomLevel), (int) (58 * UIzoomLevel)); // right
					}

					// selected power during tab
					if (paused && (player.hotkeys[i] == pauseHoverAbility || i == hotkeyHovered))
					{
						buffer.setStroke(new BasicStroke(1));
						buffer.setColor(Color.yellow);
						buffer.drawRect(x, y, (int) (60 * UIzoomLevel), (int) (60 * UIzoomLevel));
					}
				}
				else
				// no power in that space
				{
					// buffer.setStroke(dashedStroke3);
					// buffer.setColor(new Color(0, 0, 0, 90));
					// buffer.drawRect(x, y, (int) (60 * UIzoomLevel), (int) (60 * UIzoomLevel));
				}
				// remember - black rectangle after icon
			}

		buffer.setStroke(new BasicStroke((float) (3 * UIzoomLevel)));
		buffer.setColor(Color.black);
		// effects
		for (int i = 0; i < player.effects.size(); i++)
		{
			Effect e = player.effects.get(i);
			// Icon
			Rectangle place = new Rectangle((int) (frameWidth - 90 * UIzoomLevel - i * 80 * UIzoomLevel), (int) (frameHeight - 90 * UIzoomLevel), (int) (60 * UIzoomLevel), (int) (60 * UIzoomLevel));
			buffer.drawImage(Resources.icons.get(e.name), place.x, place.y, this);
			buffer.drawRect(place.x, place.y, place.width, place.height);
			// Cooldown
			if (e.duration != -1)
			{
				buffer.setColor(new Color(0, 0, 0, 89));
				Shape prevClip = buffer.getClip();
				buffer.setClip(new Rectangle2D.Double(place.x, place.y, place.width, place.height));
				buffer.fillArc(place.x - place.width / 2, place.y - place.height / 2, place.width * 2, place.height * 2, +90, (int) (360 * e.timeLeft / e.duration));
				buffer.setClip(prevClip);
				// for ghost mode panic
				if (e instanceof Ethereal && player.insideWall)
				{
					buffer.setStroke(new BasicStroke(1));
					buffer.setColor(Color.red);
					buffer.drawRect(place.x, place.y, place.width, place.height);
				}
			}
		}
	}

	void drawPause(Graphics2D buffer)
	{
		// Should only be called if paused == true

		// Cover screen with dark transparent rectangle
		buffer.setColor(new Color(0, 0, 0, 40));
		buffer.fillRect(0, 0, frameWidth, frameHeight);

		// "PAUSED"
		Font pausedFont = new Font("Serif", Font.PLAIN, (int) (100 * UIzoomLevel));
		buffer.setFont(pausedFont);
		scaleBuffer(buffer, frameWidth / 2, frameHeight / 4, UIzoomLevel);
		buffer.drawString("~PAUSED~", frameWidth / 2 - (int) (230 * UIzoomLevel), frameHeight / 4 + (int) (20 * UIzoomLevel));
		scaleBuffer(buffer, frameWidth / 2, frameHeight / 4, 1 / UIzoomLevel);

		// buttons and stuff:
		for (MenuElement m : menuStuff)
			m.draw(buffer);

		if (menu == Menu.TAB)
			drawPauseAbilities(buffer);
		// TODO make everything scale with uizoom or buffer scale
		if (menu == Menu.CHEATS)
			drawPauseAbilities(buffer);
		if (menu == Menu.ABILITIES)
		{
			drawPauseAbilities(buffer);
			drawAbilitiesMenu(buffer);
		}
	}

	void drawPauseAbilities(Graphics2D buffer)
	{
		int rectStartX = (int) (frameWidth / 2 - player.abilities.size() * 80 / 2 * UIzoomLevel);
		int rectStartY = (int) (frameHeight * 3 / 4);
		int rectWidth = (int) (player.abilities.size() * 80 * UIzoomLevel);
		int extraUp = screenmx > rectStartX - 50 * UIzoomLevel && screenmx < rectStartX + rectWidth + 130 * UIzoomLevel && screenmy > rectStartY - 70 * UIzoomLevel
				&& screenmy < rectStartY + 100 * UIzoomLevel ? 0 : (int) (-40 * UIzoomLevel);

		// Ability breakdown rectangle
		buffer.setColor(new Color(255, 255, 255, 130));
		buffer.setStroke(new BasicStroke(1));
		buffer.fillRect(0, (int) (rectStartY - 70 * UIzoomLevel - extraUp), frameWidth, (int) (UIzoomLevel * 170 + extraUp));
		buffer.setColor(new Color(0, 0, 0));
		buffer.drawRect(0, (int) (rectStartY - 70 * UIzoomLevel - extraUp), frameWidth, (int) (UIzoomLevel * 170 + extraUp));

		buffer.setFont(new Font("Sans-Serif", Font.BOLD, (int) (12 * UIzoomLevel)));
		for (int i = 0; i < player.abilities.size(); i++)
		{
			Ability ability = player.abilities.get(i);
			if (ability.hasTag("passive"))
			{
				buffer.setStroke(new BasicStroke((float) (3 * UIzoomLevel), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, (float) (10.0f), new float[]
				{ (float) (10.0f * UIzoomLevel) }, 0.0f));
				buffer.setColor(new Color(0, 0, 0, 90));
			}
			else
			{
				buffer.setStroke(new BasicStroke((float) (3 * UIzoomLevel)));
				buffer.setColor(Color.black);
			}
			// icons
			scaleBuffer(buffer, (int) (rectStartX + i * 80 * UIzoomLevel + 0 * UIzoomLevel), (int) (rectStartY + 0 * UIzoomLevel), UIzoomLevel);
			buffer.drawImage(Resources.icons.get(ability.name), (int) (rectStartX + i * 80 * UIzoomLevel), (int) (rectStartY), this);
			scaleBuffer(buffer, (int) (rectStartX + i * 80 * UIzoomLevel + 0 * UIzoomLevel), (int) (rectStartY + 0 * UIzoomLevel), 1 / UIzoomLevel);
			buffer.drawRect((int) (rectStartX + i * 80 * UIzoomLevel), rectStartY, (int) (60 * UIzoomLevel), (int) (60 * UIzoomLevel));
			if (i == pauseHoverAbility || (hotkeyHovered != -1 && i == player.hotkeys[hotkeyHovered]))
			{
				buffer.setStroke(new BasicStroke(1));
				buffer.setColor(Color.yellow);
				buffer.drawRect((int) (frameWidth / 2 - player.abilities.size() * 80 / 2 * UIzoomLevel + i * 80 * UIzoomLevel), frameHeight * 3 / 4, (int) (60 * UIzoomLevel),
						(int) (60 * UIzoomLevel));
			}
			// Key bound to that ability
			int timesAssigned = 0;
			buffer.setColor(Color.black);
			for (int j = 0; j < player.hotkeys.length; j++)
				if (player.hotkeys[j] == i)
				{
					timesAssigned++;
					buffer.drawString(hotkeyStrings[j], (int) (rectStartX + i * 80 * UIzoomLevel + 12 * UIzoomLevel), (int) (rectStartY + 60 * UIzoomLevel + timesAssigned * 16 * UIzoomLevel));
				}
		}
	}

	void drawAbilitiesMenu(Graphics2D buffer)
	{
		if (abilityExamined == -1)
		{
			buffer.setFont(new Font("Serif", Font.PLAIN, 60));
			buffer.setColor(Color.white);
			buffer.drawString("CHOOSE AN ABILITY TO EXAMINE", frameWidth / 2 - 500 - 1, frameHeight / 2 - 1);
			buffer.setColor(Color.white);
			buffer.drawString("CHOOSE AN ABILITY TO EXAMINE", frameWidth / 2 - 500 - 1, frameHeight / 2 + 1);
			buffer.setColor(Color.white);
			buffer.drawString("CHOOSE AN ABILITY TO EXAMINE", frameWidth / 2 - 500 + 1, frameHeight / 2 - 1);
			buffer.setColor(Color.white);
			buffer.drawString("CHOOSE AN ABILITY TO EXAMINE", frameWidth / 2 - 500 + 1, frameHeight / 2 + 1);
			buffer.setColor(Color.black);
			buffer.drawString("CHOOSE AN ABILITY TO EXAMINE", frameWidth / 2 - 500, frameHeight / 2);
			return;
		}
		int cx = frameWidth / 2, cy = frameHeight * 2 / 5, rectWidth = 1200, rectHeight = 500;
		buffer.setColor(new Color(0, 0, 0, 90));
		buffer.fillRect(cx - rectWidth / 2, cy - rectHeight / 2, rectWidth, rectHeight);

		// ~~left column~~
		// ability icon
		int iconCenterX = (int) (cx - rectWidth * 0.32);
		int iconCenterY = (int) (cy + rectHeight * 0.05);
		Ability ability = player.abilities.get(abilityExamined);
		buffer.translate(iconCenterX, iconCenterY);
		buffer.scale(4, 4);
		buffer.translate(-iconCenterX, -iconCenterY);
		buffer.setColor(new Color(255, 255, 255, 63));
		buffer.fillRect(iconCenterX - 30, iconCenterY - 30, 60, 60);
		buffer.setColor(Color.black);
		buffer.setStroke(new BasicStroke(3));
		buffer.drawRect(iconCenterX - 30, iconCenterY - 30, 60, 60);
		buffer.drawImage(Resources.icons.get(ability.name), iconCenterX - 30, iconCenterY - 30, null); // 30 = icon width / 2
		buffer.translate(iconCenterX, iconCenterY);
		buffer.scale((double) 1 / 4, (double) 1 / 4);
		buffer.translate(-iconCenterX, -iconCenterY);

		// ability basic details
		buffer.setColor(Color.white);
		Font nameFont = new Font("Serif", Font.PLAIN, 40);
		buffer.setFont(nameFont);
		frc = buffer.getFontRenderContext();
		int nameTextWidth = (int) (nameFont.getStringBounds(Ability.niceName(ability.name), frc).getWidth());
		buffer.drawString(Ability.niceName(ability.name), iconCenterX - nameTextWidth / 2, iconCenterY + 170);
		buffer.setFont(new Font("Sans-Serif", Font.BOLD, 50));
		buffer.drawString("Level " + ability.LEVEL, iconCenterX - 80, iconCenterY - 160);
		buffer.drawLine(iconCenterX - nameTextWidth / 2, iconCenterY + 180, iconCenterX + nameTextWidth / 2, iconCenterY + 180);
		buffer.setFont(new Font("Serif", Font.PLAIN, 20));
		buffer.drawString(ability.getFluff(), iconCenterX - 140, iconCenterY + 215);

		// ~~right column~~
		// ability stats
		buffer.setFont(new Font("Serif", Font.PLAIN, 40));
		final int lineGap = 50, linesUpperY = cy - rectHeight / 2 + 50, stringX = cx - 150, valueX = stringX + 300;
		int lines = 0;
		if (ability.range != -1)
		{
			buffer.drawString("Range:", stringX, linesUpperY + lines * lineGap);
			buffer.drawString("" + niceNumber(ability.range * 0.01) + " m", valueX, linesUpperY + lines * lineGap);
			lines++;
		}
		if (ability.cooldown != -1)
		{
			buffer.drawString("Cooldown:", stringX, linesUpperY + lines * lineGap);
			buffer.drawString("" + niceNumber(ability.cooldown) + " s", valueX, linesUpperY + lines * lineGap);
			lines++;
		}
		if (ability.cost != -1)
		{
			buffer.drawString("Cost:", stringX, linesUpperY + lines * lineGap);
			buffer.drawString("" + niceNumber(ability.cost) + " " + ability.costType.toString().toLowerCase(), valueX, linesUpperY + lines * lineGap);
			lines++;
		}
		if (ability.costPerSecond != -1)
		{
			buffer.drawString("Cost Per Second:", stringX, linesUpperY + lines * lineGap);
			buffer.drawString("" + niceNumber(ability.costPerSecond) + " " + ability.costType.toString().toLowerCase(), valueX, linesUpperY + lines * lineGap);
			lines++;
		}
		if (ability.radius != -1)
		{
			buffer.drawString("Radius:", stringX, linesUpperY + lines * lineGap);
			buffer.drawString("" + niceNumber(ability.radius * 0.01) + " m", valueX, linesUpperY + lines * lineGap);
			lines++;
		}
		if (ability.damage != -1)
		{
			buffer.drawString("Damage:", stringX, linesUpperY + lines * lineGap);
			buffer.drawString("" + niceNumber(ability.damage), valueX, linesUpperY + lines * lineGap);
			lines++;
		}
		if (ability.pushback != -1)
		{
			buffer.drawString("Pushback:", stringX, linesUpperY + lines * lineGap);
			buffer.drawString("" + niceNumber(ability.pushback), valueX, linesUpperY + lines * lineGap);
			lines++;
		}
		if (ability.steal != -1)
		{
			buffer.drawString("Steal:", stringX, linesUpperY + lines * lineGap);
			buffer.drawString("" + niceNumber(ability.steal * 100) + "%", valueX, linesUpperY + lines * lineGap);
			lines++;
		}
		if (ability.duration != -1)
		{
			buffer.drawString("Duration:", stringX, linesUpperY + lines * lineGap);
			buffer.drawString("" + niceNumber(ability.duration) + " s", valueX, linesUpperY + lines * lineGap);
			lines++;
		}
		if (ability.chance != -1)
		{
			buffer.drawString("Chance:", stringX, linesUpperY + lines * lineGap);
			buffer.drawString("" + niceNumber(ability.chance * 100) + "%", valueX, linesUpperY + lines * lineGap);
			lines++;
		}
		if (ability.amount != -1)
		{
			buffer.drawString("Amount:", stringX, linesUpperY + lines * lineGap);
			buffer.drawString("" + niceNumber(ability.amount), valueX, linesUpperY + lines * lineGap);
			lines++;
		}
		if (ability.chargeRate != -1)
		{
			buffer.drawString("Charge Rate:", stringX, linesUpperY + lines * lineGap);
			buffer.drawString("" + niceNumber(ability.chargeRate) + " %/s", valueX, linesUpperY + lines * lineGap);
			lines++;
		}
		if (ability instanceof _AFFAbility)
		{
			_AFFAbility a = (_AFFAbility) ability;
			if (a.life != 0)
			{
				buffer.drawString("Durability:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("" + niceNumber(a.life) + " HP", valueX, linesUpperY + lines * lineGap);
				lines++;
			}
			if (a.decayRate != 0)
			{
				buffer.drawString("Decay Rate:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("" + niceNumber(a.decayRate * 100) + " %/s", valueX, linesUpperY + lines * lineGap);
				lines++;
			}
			if (a.armor != 0)
			{
				buffer.drawString("Armor:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("" + niceNumber(a.armor) + "", valueX, linesUpperY + lines * lineGap);
				lines++;
			}
		}
		if (ability instanceof _BeamAbility)
		{
			_BeamAbility a = (_BeamAbility) ability;
			if (a.size != 1)
			{
				buffer.drawString("Size:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("" + niceNumber(a.size * 100) + "%", valueX, linesUpperY + lines * lineGap);
				lines++;
			}
		}
		if (ability instanceof _FlightAbility)
		{
			_FlightAbility a = (_FlightAbility) ability;
			if (a.flySpeed != -1)
			{
				buffer.drawString("Speed:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("" + niceNumber(a.flySpeed) + " m/s", valueX, linesUpperY + lines * lineGap);
				lines++;
			}
		}
		if (ability instanceof _ForceFieldAbility)
		{
			_ForceFieldAbility a = (_ForceFieldAbility) ability;
			if (a.length != 0)
			{
				buffer.drawString("Length:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("" + niceNumber(a.length) + " m", valueX, linesUpperY + lines * lineGap);
				lines++;
			}
			if (a.width != 0)
			{
				buffer.drawString("Width:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("" + niceNumber(a.width) + " m", valueX, linesUpperY + lines * lineGap);
				lines++;
			}
			if (a.life != 0)
			{
				buffer.drawString("Life:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("" + niceNumber(a.life) + " HP", valueX, linesUpperY + lines * lineGap);
				lines++;
			}
			if (a.armor != 0)
			{
				buffer.drawString("Armor:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("" + niceNumber(a.armor), valueX, linesUpperY + lines * lineGap);
				lines++;
			}
			if (a.decayRate != 0)
			{
				buffer.drawString("Decay Rate:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("" + niceNumber(a.decayRate * 100) + " %/s", valueX, linesUpperY + lines * lineGap);
				lines++;
			}
		}
		if (ability instanceof _ProjectileAbility)
		{
			_ProjectileAbility a = (_ProjectileAbility) ability;
			if (a.velocity != 0)
			{
				buffer.drawString("Velocity:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("" + niceNumber(a.velocity) + " m/s", valueX, linesUpperY + lines * lineGap);
				lines++;
			}
			if (a.size != 1)
			{
				buffer.drawString("Size:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("" + niceNumber(a.size * 100) + "%", valueX, linesUpperY + lines * lineGap);
				lines++;
			}
		}
		if (ability instanceof _SummoningAbility)
		{
			_SummoningAbility a = (_SummoningAbility) ability;
			if (a.maxNumOfClones != 1)
			{
				buffer.drawString("Maximum:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("" + niceNumber(a.maxNumOfClones), valueX, linesUpperY + lines * lineGap);
				lines++;
			}
			if (a.statMultiplier != 1)
			{
				buffer.drawString("Stats:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("x" + niceNumber(a.statMultiplier * 100) + "%", valueX, linesUpperY + lines * lineGap);
				lines++;
			}
			if (a.life != 0)
			{
				buffer.drawString("Life:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("" + niceNumber(a.life) + " HP", valueX, linesUpperY + lines * lineGap);
				lines++;
			}
		}
		if (ability instanceof _TeleportAbility)
		{
			_TeleportAbility a = (_TeleportAbility) ability;
			if (a.telefragging)
			{
				buffer.drawString("Telefrags:", stringX, linesUpperY + lines * lineGap);
				buffer.drawString("YEAH", valueX, linesUpperY + lines * lineGap);
				lines++;
			}
		}

		int perkX = cx + 450;
		Font perkFont = new Font("Sans Serif", Font.PLAIN, 20);
		buffer.setFont(perkFont);
		frc = buffer.getFontRenderContext();
		lines = 0;
		for (Perk p : ability.perks)
		{
			int perkTextWidth = (int) (perkFont.getStringBounds(p.name, frc).getWidth());
			buffer.drawImage(Resources.icons.get(p.name), perkX - 30, linesUpperY + lines * lineGap * 2 - 30, null);
			buffer.drawString(p.name, perkX - perkTextWidth / 2, linesUpperY + lines * lineGap * 2 + 50);
			lines++;
		}
	}

	String niceNumber(double number)
	{
		// leaves only 2 sig figs, deletes trailing zeros
		String string = "" + number;
		while (string.contains(".") && (string.charAt(string.length() - 1) == '.' || string.charAt(string.length() - 1) == '0'))
			string = string.substring(0, string.length() - 1);
		int indexOfPoint = string.indexOf(".");
		if (indexOfPoint == -1 || indexOfPoint + 3 >= string.length())
			return string;
		string = string.substring(0, indexOfPoint + 3);
		return string;
	}

	void checkPlayerMovementKeys()
	{
		Person probablyPlayer = player;
		double horiAccel = 0, vertAccel = 0;
		if (player.upPressed)
			vertAccel--;
		if (player.leftPressed)
			horiAccel--;
		if (player.downPressed)
			vertAccel++;
		if (player.rightPressed)
			horiAccel++;
		if (player.timeUntilPortalConfusionIsOver > 0.05)
		{
			player.wasdPortalArray[0] = player.upPressed;
			player.wasdPortalArray[1] = player.leftPressed;
			player.wasdPortalArray[2] = player.downPressed;
			player.wasdPortalArray[3] = player.rightPressed;
		}
		else if (player.portalMovementRotation != 0)
		{
			if ((player.wasdPortalArray[0] != player.upPressed) || (player.wasdPortalArray[1] != player.leftPressed) || (player.wasdPortalArray[2] != player.downPressed)
					|| (player.wasdPortalArray[3] != player.rightPressed))
				player.portalMovementRotation = 0;
		}
		if (horiAccel == 0 && vertAccel == 0)
		{
			probablyPlayer.strengthOfAttemptedMovement = 0;
			probablyPlayer.flyDirection = 0;
			player.portalMovementRotation = 0; // If you stop, portal sickness cancels
			if (player.leftMousePressed && !probablyPlayer.maintaining && !probablyPlayer.dead)
				if (probablyPlayer.abilityTryingToRepetitivelyUse == -1 || !(probablyPlayer.abilities.get(probablyPlayer.abilityTryingToRepetitivelyUse) instanceof Punch)
						|| probablyPlayer.abilities.get(probablyPlayer.abilityTryingToRepetitivelyUse).cooldownLeft <= 0)
				{
					// rotate to where mouse point is
					double angle = Math.atan2(my - probablyPlayer.y, mx - probablyPlayer.x);
					probablyPlayer.rotate(angle, globalDeltaTime);
				}
		}
		else
		{
			probablyPlayer.strengthOfAttemptedMovement = 1;
			if (portalCameraRotation)
				probablyPlayer.directionOfAttemptedMovement = Math.atan2(vertAccel, horiAccel) + cameraRotation;
			else
				probablyPlayer.directionOfAttemptedMovement = Math.atan2(vertAccel, horiAccel) + cameraRotation + player.portalMovementRotation;

			// Reduce effect of portal axis change. Commented out because "keep moving until you release keys" feels better.
			// if (player.movementAxisRotation > 0)
			// player.movementAxisRotation += (((((0 - player.movementAxisRotation) % (Math.PI * 2)) + (Math.PI * 3)) % (Math.PI * 2)) - Math.PI) * 3 * 0.02 * (0.5 - player.timeSincePortal);

			if (player.spacePressed && !player.ctrlPressed)
				probablyPlayer.flyDirection = 1;
			else if (!player.spacePressed && player.ctrlPressed)
				probablyPlayer.flyDirection = -1;
			else
				player.flyDirection = 0;

			if (probablyPlayer.abilityTryingToRepetitivelyUse == -1 || !probablyPlayer.abilities.get(probablyPlayer.abilityTryingToRepetitivelyUse).justName().equals("Punch")) // if not punching
			{
				if (!probablyPlayer.notAnimating)
					probablyPlayer.rotate(probablyPlayer.directionOfAttemptedMovement, globalDeltaTime);
			}
			else if (probablyPlayer.flySpeed != -1 && probablyPlayer.strengthOfAttemptedMovement != 0)
			{
				// rotation of fly-punchers
				probablyPlayer.rotate(probablyPlayer.directionOfAttemptedMovement, globalDeltaTime * 1); // this 1 used to be 0.3
			}
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
		if (p.timeEffect == 0) // time stopped
			return;
		// if the person is attempting to stand still
		if (!p.prone && p.strengthOfAttemptedMovement == 0)
		{
			if (p.z == 0 && p.flySpeed == -1)
			{
				if (!p.panic)
				{
					if (!p.notAnimating)
						p.switchAnimation(0);
					return;
				}
				else
				{
					// panicked people can't stop running!
					p.strengthOfAttemptedMovement = 1;
					p.directionOfAttemptedMovement = p.rotation; // Only changes it to that value when p isn't trying to move, so it's not bad
				}
			}
			// fly-punching
			else if (p.flySpeed != -1 && !(p.abilityTryingToRepetitivelyUse != -1 && p.abilities.get(p.abilityTryingToRepetitivelyUse).justName().equals("Punch")))
			{
				p.switchAnimation(9); // slowing down / hover animation
				// glide down slowly
				if (p.xVel * p.xVel + p.yVel * p.yVel < 300 * 300) // 500 - min speed for keeping height
					p.zVel = -0.3 * p.flySpeed * 5 * deltaTime;
				else
					p.zVel = 0;
			}
			else if (p.z == 1 && p.flySpeed == -1)
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
				}
				else
				{
					// panicked people can't stop running!
					p.strengthOfAttemptedMovement = 1;
					p.directionOfAttemptedMovement = p.rotation; // Only changes it to that value when p isn't trying to move, so it's not bad
				}
			}
		}
		// can't move or auto-rotate to movement direction while fisting
		if (p.notMovingTimer > 0) // should never be happening when in the air
		{
			return;
		}
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
					}
					else if (p.z == 0 || p.z == 1) // walking on ground or walking on walls
					{
						double staminaMultiplier = 1;
						double runMultiplier = 1;
						for (Ability a : p.abilities)
							if (a instanceof Sprint && a.on)
							{
								staminaMultiplier *= a.costPerSecond;
								runMultiplier *= 2;
							}
						// Tangled
						for (Effect e : p.effects)
							if (e instanceof Tangled)
								runMultiplier *= 0.66; // Speed decreased by 33% per vine (stacking multiplicatively)

						// Time shenanigans
						staminaMultiplier *= p.timeEffect;
						runMultiplier *= p.timeEffect;

						// making sure dude/dudette has enough stamina
						double timesStaminaFitsIntoStaminaCost = p.stamina / (p.runningStaminaCost * deltaTime * staminaMultiplier);
						if (timesStaminaFitsIntoStaminaCost < 1)
						{
							runMultiplier *= timesStaminaFitsIntoStaminaCost; // Because of the 3:2 ratio, if you try to sprint while lacking stamina you will run slower
							staminaMultiplier *= timesStaminaFitsIntoStaminaCost;
						}

						p.stamina -= staminaMultiplier * p.runningStaminaCost * deltaTime * p.strengthOfAttemptedMovement;

						if (movementVariation)
							p.strengthOfAttemptedMovement *= 1 - 0.6 * Math.abs(Math.sin((p.directionOfAttemptedMovement - p.rotation) / 2)); // backwards = 0.4, forwards = 1, sideways = 0.6 (roughly)

						// A person with 0 stamina will be unable to move anymore, so to fix it:
						if (runMultiplier < 0.2)
							runMultiplier = 0.2;

						if (p.xVel * p.xVel + p.yVel * p.yVel < Math.pow(p.runSpeed * runMultiplier * 100 / friction, 2))
						{
							p.xVel += Math.cos(p.directionOfAttemptedMovement) * deltaTime * p.strengthOfAttemptedMovement * p.runAccel * friction / 100 * runMultiplier;
							p.yVel += Math.sin(p.directionOfAttemptedMovement) * deltaTime * p.strengthOfAttemptedMovement * p.runAccel * friction / 100 * runMultiplier;
						}

						// switch to running animation
						if (!p.notAnimating)
							p.switchAnimation(1);
					}
					else// freefalling?
					{
						if (p.xVel * p.xVel + p.yVel * p.yVel < Math.pow(300, 2))
						{
							p.xVel += Math.cos(p.directionOfAttemptedMovement) * deltaTime * p.strengthOfAttemptedMovement * 20;
							p.yVel += Math.sin(p.directionOfAttemptedMovement) * deltaTime * p.strengthOfAttemptedMovement * 20;
						}
						// TODO animation for falling that isn't the same as when not falling
					}
				}
			}
			else // if punching
			{
				p.strengthOfAttemptedMovement = 0.5; // TODO why not 0?
				p.directionOfAttemptedMovement = p.rotation;

				// fly-punching (trying to punch while flying)
				if (p.flySpeed != -1)
				{
					if (p.xVel * p.xVel + p.yVel * p.yVel < p.flySpeed * p.flySpeed)
					{
						p.xVel += Math.cos(p.directionOfAttemptedMovement) * deltaTime * 100 * p.strengthOfAttemptedMovement * p.runAccel / 100;
						p.yVel += Math.sin(p.directionOfAttemptedMovement) * deltaTime * 100 * p.strengthOfAttemptedMovement * p.runAccel / 100;
					}

					if (p.z <= 1.1 && p.z > 0.6)
					{
						p.zVel = 0;
						p.z = 1.1;
					}
					if (p.z > 5)
						p.zVel = -0.7 * p.flySpeed * 5 * deltaTime; // glide down
					else if (p.z > 1.1) // to avoid weird flickering
						p.zVel = -0.2 * p.flySpeed * 5 * deltaTime; // glide down
					if (p.z <= 0.6)
					{
						p.zVel = 1; // glide...up
						if (p.z == 0)
							p.z = 0.2;
					}
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
					}
					else
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
					}
					else
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

	void doSomethingToAllSounds(String command)
	{
		List<SoundEffect> sounds = new ArrayList<SoundEffect>();
		sounds.addAll(env.ongoingSounds);
		for (Person p : env.people)
		{
			for (Ability a : p.abilities)
				sounds.addAll(a.sounds);
			sounds.addAll(p.sounds);
		}
		for (ForceField ff : env.FFs)
			sounds.addAll(ff.sounds);
		for (Portal p : env.portals)
			if (p != null)
				sounds.add(p.sound);
		for (Furniture f : env.furniture)
			for (SoundEffect s : f.sounds)
				sounds.add(s);

		for (SoundEffect s : sounds)
			if (s == null)
				errorMessage("ERROR - a sound is null. I dunno which sound. It's null.");
			else
				switch (command)
				{
				case "stop":
					s.stop();
					break;
				case "pause":
					s.pause();
					break;
				case "unpause":
					s.cont(); // inue
					break;
				default:
					MAIN.errorMessage("sibyl system one two three");
					break;
				}
	}

	void pause(Menu target, boolean bool)
	{
		if (target != null && !bool && target != menu && menu != Menu.NO) // switch between pause menus
		{
			menu = target;
			updatePauseMenu();
			return;
		}
		if (bool) // pause key pressed
		{
			if (target == Menu.ESC && target != menu && menu != Menu.NO)
			{
				extraPauseBoolean = false;
				paused = false;
				menu = Menu.NO;
				tooltip = "";
				pauseHoverAbility = -1;
				doSomethingToAllSounds("unpause");
				return;
			}
			menu = target;
			if (!paused)
				doSomethingToAllSounds("pause");
			paused = true;
		}
		else // pause key released
		{
			extraPauseBoolean = !extraPauseBoolean;
			paused = !extraPauseBoolean;
			if (!paused)
				menu = Menu.NO;
			tooltip = "";
			if (!paused)
			{
				pauseHoverAbility = -1;
				doSomethingToAllSounds("unpause");
			}
		}
		updatePauseMenu();
		mousePositionHoverChecks();
	}

	void updatePauseMenu()
	{
		menuStuff.clear(); // remove all current stuff
		switch (menu)
		{
		case TAB:
			menuStuff.add(new MenuText(frameWidth / 2 - 78, frameHeight / 2 - 30, 156, 60, "RESUME"));
			menuStuff.add(new MenuText(frameWidth / 2 - 78, frameHeight / 2 + 40, 156, 60, "ABILITIES"));
			break;
		case ABILITIES:
			menuStuff.add(new MenuText(frameWidth / 2 - 78, 50, 156, 60, "Resume"));
			break;
		case ESC:
			menuStuff.add(new MenuText(frameWidth / 2 - 78, frameHeight / 2 - 100, 156, 60, "RESUME"));
			menuStuff.add(new MenuText(frameWidth / 2 - 137, frameHeight / 2 + 40, 274, 60, "EXIT_GAME"));
			menuStuff.add(new MenuText(frameWidth / 2 - 78, frameHeight / 2 - 30, 156, 60, "ABILITIES"));
			// TODO OPTIONS
			menuStuff.add(new MenuText(frameWidth / 2 - 73, frameHeight / 2 + 110, 146, 60, "CHEATS"));
			break;
		case CHEATS:
			menuStuff.add(new MenuText(frameWidth / 2 - 78, 50, 156, 60, "Resume"));
			List<String> abilities = new ArrayList<String>();
			abilities.addAll(Ability.implementedAbilities);
			int x = (int) 300;
			int y = (int) 200;
			int columnNumber = 18;
			// abilities
			for (int row = 0; row <= (abilities.size() + columnNumber - 1) / columnNumber; row++)
				for (int column = 0; row * columnNumber + column < abilities.size() && column < columnNumber; column++)
				{
					String abilityName = abilities.get(row * columnNumber + column);
					menuStuff.add(new MenuThingie(x + column * 80, y + row * 80, "CHEATS_ABILITY", abilityName));
				}
			// elements
			for (int i = 0; i < 6; i++)
			{
				menuStuff.add(new MenuThingie(x + 40 + i * 80, y - 160, "CHEATS_ELEMENT", EP.elementList[i]));
				menuStuff.add(new MenuThingie(x + 40 + i * 80, y - 80, "CHEATS_ELEMENT", EP.elementList[i + 6]));
			}
			break;
		case NO:
			break;
		default:
			errorMessage("What's on the menu?");
			break;
		}
	}

	void pressMenuButton(MenuElement m)
	{
		switch (m.type)
		{
		case RESUME:
			pause(null, false);
			break;
		case EXIT_GAME:
			System.exit(0);
			break;
		case CHEATS:
			menu = Menu.CHEATS;
			updatePauseMenu();
			break;
		case ABILITIES:
			menu = Menu.ABILITIES;
			updatePauseMenu();
			break;
		case CHEATS_ABILITY:
		case CHEATS_ELEMENT:
			for (MenuElement m2 : menuStuff)
				if (m2.type == m.type && !m.equals(m2))
					((MenuThingie) m2).on = false;
			((MenuThingie) m).on = true;
			if (m.type == MenuElement.Type.CHEATS_ABILITY)
			{
				cheatedAbilityName = Ability.justName(m.text);
				if (!Ability.elementalPowers.contains(cheatedAbilityName))
				{
					for (MenuElement m3 : menuStuff)
						if (m3.type == MenuElement.Type.CHEATS_ELEMENT)
						{
							((MenuThingie) m3).available = false;
							// if element was selected, avoid bug
							if (((MenuThingie) m3).on)
								cheatedAbilityElement = null;
						}
				}
				else // is elemental
					for (MenuElement m3 : menuStuff)
						if (m3.type == MenuElement.Type.CHEATS_ELEMENT)
						{
							// checks if such elemental ability exists
							((MenuThingie) m3).available = Resources.icons.get(cheatedAbilityName + " <" + m3.text + ">") != null;
							// if not and element was selected, avoid bug
							if (((MenuThingie) m3).on)
								if (!((MenuThingie) m3).available)
									cheatedAbilityElement = null;
								else
									cheatedAbilityElement = m3.text;
						}
			}
			else if (m.type == MenuElement.Type.CHEATS_ELEMENT)
			{
				cheatedAbilityElement = m.text;
				for (MenuElement m3 : menuStuff)
					if (m3.type == MenuElement.Type.CHEATS_ABILITY)
						if (((MenuThingie) m3).on)
							if (Resources.icons.get(Ability.justName(m3.text) + " <" + m.text + ">") == null)
							{
								cheatedAbilityElement = null;
								((MenuThingie) m).available = false;
								((MenuThingie) m).on = false;
							}
				if (cheatedAbilityElement != null)
					for (MenuElement m3 : menuStuff)
						if (m3.type == MenuElement.Type.CHEATS_ABILITY)
							if (Ability.elementalPowers.contains(Ability.justName(m3.text)))
							{
								BufferedImage img = Resources.icons.get(Ability.justName(m3.text) + " <" + cheatedAbilityElement + ">");
								if (img != null)
									((MenuThingie) m3).image = img;
								else
									((MenuThingie) m3).image = Resources.icons.get(Ability.justName(m3.text));
							}

			}
			if (cheatedAbilityElement == null)
				for (MenuElement m3 : menuStuff)
					if (m3.type == MenuElement.Type.CHEATS_ABILITY)
						if (Ability.elementalPowers.contains(Ability.justName(m3.text)))
							((MenuThingie) m3).image = Resources.icons.get(Ability.justName(m3.text));

			// update relevant text
			updateCheatAddAbilityButton();
			break;
		case CHEATS_RESULT_ABILITY:
			String abilityName = "";
			if (Ability.elementalPowers.contains(cheatedAbilityName))
				if (cheatedAbilityElement != null)
					abilityName = cheatedAbilityName + " <" + cheatedAbilityElement + ">";
				else
					return;
			else // normal non-elemental ability
				abilityName = cheatedAbilityName;
			if ("Punch".equals(abilityName) || "Sprint".equals(abilityName))
				return;
			// remove existing ability if exists
			int removedIndex = -1;
			for (int i = 0; i < player.abilities.size(); i++)
				if (player.abilities.get(i).name.equals(abilityName) && player.abilities.get(i).LEVEL == cheatedAbilityLevel)
				{
					if (player.abilities.get(i).on)
						player.abilities.get(i).disable(env, player);
					removedIndex = i;
				}
			if (removedIndex != -1)
			{
				player.abilities.remove(removedIndex);
				for (int i = 0; i < player.hotkeys.length; i++)
				{
					if (player.hotkeys[i] == removedIndex)
						player.hotkeys[i] = -1;
					if (player.hotkeys[i] > removedIndex)
						player.hotkeys[i]--;
				}
				if (abilityExamined == removedIndex)
					abilityExamined = -1;
				updateNiceHotkeys();
				break;
			}
			// otherwise, add new ability
			Ability ability = Ability.ability(abilityName, cheatedAbilityLevel);
			player.abilities.add(ability);
			if (!ability.hasTag("passive"))
				for (int i = 0; i < player.hotkeys.length; i++)
				{
					if (player.hotkeys[i] == -1)
					{
						player.hotkeys[i] = player.abilities.size() - 1;
						updateNiceHotkeys();
						break;
					}
				}
			break;
		case ICON:
			break;
		default:
			errorMessage("Been there done that messed around, I'm having fun, don't put me down    (" + m.type + ")");
			break;
		}
	}

	void updateCheatAddAbilityButton()
	{
		String abilityName = "";
		if (cheatedAbilityName != null)
		{
			if (cheatedAbilityElement != null)
				abilityName = cheatedAbilityName + " <" + cheatedAbilityElement + ">";
			else if (Ability.elementalPowers.contains(cheatedAbilityName))
				abilityName = null;
			else // normal non-elemental ability
				abilityName = cheatedAbilityName;
			// remove previous ability result
			for (int i = 0; i < menuStuff.size(); i++)
				if (menuStuff.get(i).type == MenuElement.Type.CHEATS_RESULT_ABILITY || menuStuff.get(i).type == MenuElement.Type.ICON)
				{
					menuStuff.remove(i);
					i--;
				}
			if (abilityName != null)
			{
				// add new ability name and description texts
				MenuText abilityMenuText = new MenuText(300 + 800, 200 - 160, 650, 150, Ability.niceName(abilityName) + ", level " + cheatedAbilityLevel);
				abilityMenuText.clickable = true;
				abilityMenuText.type = MenuElement.Type.CHEATS_RESULT_ABILITY;
				abilityMenuText.text += "\n" + Ability.getFluff(abilityName);
				menuStuff.add(abilityMenuText);
				// icon
				MenuThingie abilityIcon = new MenuThingie(300 + 800 + 500, 200 - 160 + 10, "ICON", abilityName);
				abilityIcon.clickable = false;
				menuStuff.add(abilityIcon);
			}
		}
	}

	public void keyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{ // TODO sort to development-only keys
		case KeyEvent.VK_BACK_SPACE:// Restart
			doSomethingToAllSounds("stop");
			restart();
			break;
		case KeyEvent.VK_ESCAPE:// Pause menu
			pause(Menu.ESC, true);
			break;
		case KeyEvent.VK_TAB:
			pause(Menu.TAB, true);
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
		case KeyEvent.VK_9:
			env.people.add(new NPC(mx, my, NPC.Strategy.AGGRESSIVE));
			break;
		case KeyEvent.VK_0:
			env.remove((mx) / 96, (my) / 96);
			break;
		case KeyEvent.VK_K:
			for (Person p : env.people)
			{
				p.initAnimation();
				p.rename();
			}
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
		case KeyEvent.VK_F3:
			updateNiceHotkeys();
			break;
		case KeyEvent.VK_F4:
			portalCameraRotation = !portalCameraRotation;
			break;
		case KeyEvent.VK_F5:
			showFPS = !showFPS;
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
				}
				catch (IOException e1)
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
		case KeyEvent.VK_ESCAPE:
			pause(Menu.ESC, false);
			break;
		case KeyEvent.VK_TAB:
			pause(Menu.TAB, false);
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

	public MAIN()
	{

		frameTimer = new Timer(frameTimerDelay, frameListener);
		frameTimer.setInitialDelay(0);
		restart();
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent we)
			{
				System.exit(0);
			}
		});
		this.addMouseWheelListener(this);
		this.setSize(640, 640);
		this.setResizable(true);
		this.setFocusTraversalKeysEnabled(false);
		this.setExtendedState(Frame.MAXIMIZED_BOTH);

		// System.setProperty("sun.java2d.ddforcevram","True"); // not doing anything

		frameWidth = (int) this.getBounds().getWidth();
		frameHeight = (int) this.getBounds().getHeight();
		updateFrame();

		this.setVisible(true);
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addComponentListener(this);
		addWindowFocusListener(this);
	}

	// IGNORE
	ActionListener frameListener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			if (!paused)
				frame(); // extra method is because the actionlistener {{}}; thingie is buggy in Eclipse
			else
				pauseFrame();
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
			// bufferImage.flush();
			bufferImage = null;
		}
		System.gc(); // Garbage cleaner. useless line?

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
		MAIN main = new MAIN();
		main.toFront();
	}

	public void componentResized(ComponentEvent e)
	{
		frameWidth = (int) this.getBounds().getWidth();
		frameHeight = (int) this.getBounds().getHeight();
		updateFrame();
	}

	public void windowGainedFocus(WindowEvent arg0)
	{

	}

	public void windowLostFocus(WindowEvent arg0)
	{
		pause(Menu.ESC, true);
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
		}
		else if (player.rotateButtonPressed)
		{
			// rotate
			if (direction)
				cameraRotation += 0.04;
			else
				cameraRotation -= 0.04;
		}
		else if (player.resizeUIButtonPressed)
		{
			// UI-zoom
			if (direction)
				UIzoomLevel *= 0.9;
			else
				UIzoomLevel *= 1.1;
			updateNiceHotkeys();
		}
		else if (menu == Menu.CHEATS)
		{
			cheatedAbilityLevel += -mwe.getWheelRotation();
			cheatedAbilityLevel = Math.min(cheatedAbilityLevel, 10);
			cheatedAbilityLevel = Math.max(1, cheatedAbilityLevel);
			updateCheatAddAbilityButton();
			updateMousePosition();
		}
		else
		{
			// switch currently range-selected ability
			boolean thereAreHotkeys = false;
			for (int i = 0; i < player.hotkeys.length; i++)
				if (player.hotkeys[i] != -1)
					thereAreHotkeys = true;
			if (!thereAreHotkeys)
				return;
			if (hotkeySelected == -1 && !direction)
				hotkeySelected = player.hotkeys.length;
			if (hotkeySelected == player.hotkeys.length - 1 && direction)
				hotkeySelected = -2;
			do
				hotkeySelected += direction ? 1 : -1;
			while (hotkeySelected > -1 && hotkeySelected < player.hotkeys.length && player.hotkeys[hotkeySelected] == -1);
			if (hotkeySelected >= player.hotkeys.length)
				hotkeySelected = -1;
			if (hotkeySelected < -1)
				hotkeySelected = player.hotkeys.length - 1;
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

		mousePositionHoverChecks();
	}

	public void mouseMoved(MouseEvent me)
	{
		// Getting mouse info
		pin = MouseInfo.getPointerInfo();
		mousePoint = pin.getLocation();
		screenmx = (int) (mousePoint.getX() - this.getX());
		screenmy = (int) (mousePoint.getY() - this.getY());

		updateMousePosition();

		mousePositionHoverChecks();
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

			for (Furniture f : env.furniture)
				if (f.clickable)
					if (Methods.DistancePow2(f.x, f.y, mx, my) < Furniture.clickRangePow2) // distance to mouse is less than ~150
						if (Methods.DistancePow2(f.x, f.y, player.x, player.y) < Furniture.standRangePow2) // distance to player is less than ~400
							f.activate();
			if (player.abilityAiming != -1 && player.abilities.get(player.abilityAiming).toggleable)
				player.abilities.get(player.abilityAiming).toggle();
			else if (hotkeySelected != -1 && player.abilities.get(player.hotkeys[hotkeySelected]).toggleable)
				player.abilities.get(player.hotkeys[hotkeySelected]).toggle();

			MenuElement pressedThing = null;
			for (MenuElement m : menuStuff)
				if (m.selected) // cursor on it will make it selected
					if (m.clickable)
						pressedThing = m;
			if (pressedThing != null)
				pressMenuButton(pressedThing);

			if (menu == Menu.CHEATS && pauseHoverAbility != -1)
			{
				// set current cheat-selected ability to the clicked ability
				cheatedAbilityName = player.abilities.get(pauseHoverAbility).justName();
				String element = null;
				if (cheatedAbilityName.length() < player.abilities.get(pauseHoverAbility).name.length())
					element = player.abilities.get(pauseHoverAbility).getElement();
				else
					element = null;
				cheatedAbilityLevel = player.abilities.get(pauseHoverAbility).LEVEL;
				for (int i = 0; i < menuStuff.size(); i++)
					if (menuStuff.get(i) instanceof MenuThingie)
					{
						MenuThingie mm = (MenuThingie) menuStuff.get(i);
						if (Ability.justName(mm.text).equals(cheatedAbilityName))
							pressMenuButton(mm);
					}
				for (int i = 0; i < menuStuff.size(); i++)
					if (menuStuff.get(i) instanceof MenuThingie)
					{
						MenuThingie mm = (MenuThingie) menuStuff.get(i);
						if (mm.text.equals(element))
							pressMenuButton(mm);
					}
			}
			if (menu == Menu.ABILITIES)
			{
				if (hotkeyHovered != -1)
					abilityExamined = player.hotkeys[hotkeyHovered];
				else if (pauseHoverAbility != -1)
					abilityExamined = pauseHoverAbility;
				else
				{
					return;
				}
			}
		}
		if (me.getButton() == MouseEvent.BUTTON2) // Mid Click
		{
			if (player.ctrlPressed)
				zoomLevel = 1;
			else if (player.rotateButtonPressed)
				cameraRotation = 0;
			else if (player.resizeUIButtonPressed)
			{
				UIzoomLevel = 1;
				updateNiceHotkeys();
			}
			else
				playerPressHotkey(1, true);
		}
		if (me.getButton() == MouseEvent.BUTTON3) // Right Click
		{
			player.rightMousePressed = true;
			// view extended hotkey tooltips
			mousePositionHoverChecks();

			// disable aimed ability
			if (!paused)
			{
				if (player.abilityMaintaining != -1)
				{
					if (player.abilityAiming == -1) // trying to stop maintained power
					{
						stopUsingPower = true;
						player.abilities.get(player.abilityMaintaining).use(env, player, new Point(mx, my));
						player.abilityMaintaining = -1;
					}
					else // trying to stop mid-maintain ability
					{
						stopUsingPower = true;
						player.abilityAiming = -1;
					}
				}
				else if (player.abilityAiming != -1)
				{
					stopUsingPower = true;
					// some abilities need to know that you stopped aiming them. specifically, the Portals ability.
					player.abilities.get(player.abilityAiming).updatePlayerTargeting(env, player, player.target, 0);
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
			mousePositionHoverChecks();
		}
	}

	void mousePositionHoverChecks()
	{
		// both hotkey tooltips and effect tooltips, currently.
		boolean foundOne = false;
		for (int i = 0; i < player.hotkeys.length; i++)
		{
			// hotkey bar
			if (player.hotkeys[i] != -1 && niceHotKeys[i] != null) // the null check is sadly needed
			{
				if (screenmx > niceHotKeys[i].x && screenmy > niceHotKeys[i].y && screenmx < niceHotKeys[i].x + 60 && screenmy < niceHotKeys[i].y + 60)
				{
					foundOne = true;
					hotkeyHovered = i;
					tooltipPoint = new Point(niceHotKeys[i].x + 8, niceHotKeys[i].y - 10);
					tooltip = Ability.niceName(player.abilities.get(player.hotkeys[i]).name);
					if (player.rightMousePressed)
					{
						tooltip += " " + player.abilities.get(player.hotkeys[i]).LEVEL + "\n" + player.abilities.get(player.hotkeys[i]).getFluff();
						tooltipPoint.y -= 30;
					}
				}
			}
		}
		// effects
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

		if (paused)
			if (menu == Menu.TAB || menu == Menu.CHEATS || menu == Menu.ABILITIES)
			{
				pauseHoverAbility = -1;
				for (int i = 0; i < player.abilities.size(); i++)
					if (screenmx > frameWidth / 2 - player.abilities.size() * 80 / 2 * UIzoomLevel + i * 80 * UIzoomLevel && screenmy > frameHeight * 3 / 4
							&& screenmx < frameWidth / 2 - player.abilities.size() * 80 / 2 * UIzoomLevel + i * 80 * UIzoomLevel + 60 * UIzoomLevel
							&& screenmy < frameHeight * 3 / 4 + 60 * UIzoomLevel)
					{

						// int rectStartX = (int) (frameWidth / 2 - player.abilities.size() * 80 / 2 * UIzoomLevel);
						// int rectStartY = (int) (frameHeight * 3 / 4);
						// buffer.drawRect((int) (rectStartX + i * 80 * UIzoomLevel), rectStartY, (int) (60 * UIzoomLevel), (int) (60 * UIzoomLevel));
						pauseHoverAbility = i;
						tooltipPoint = new Point((int) (frameWidth / 2 - player.abilities.size() * 80 / 2 * UIzoomLevel + i * 80 * UIzoomLevel + 8 * UIzoomLevel),
								(int) (frameHeight * 3 / 4 - 10 * UIzoomLevel));
						tooltip = Ability.niceName(player.abilities.get(i).name);
						if (player.rightMousePressed)
						{
							tooltip += " " + player.abilities.get(i).LEVEL + "\n" + player.abilities.get(i).getFluff();
							tooltipPoint.y -= 30;
						}
					}
			}

		for (MenuElement m : menuStuff)
			m.selected = m.contains(screenmx, screenmy);
	}

	Point[] niceHotkeyDefault = new Point[]
	{ new Point(1200, 970), new Point(640, 970), new Point(840, 770), new Point(920, 770), new Point(1000, 770), new Point(1000, 850), new Point(1000, 930), new Point(920, 930), new Point(840, 930),
			new Point(840, 850) };

	void updateNiceHotkeys()
	{
		if (player == null) // what
			return;

		int numOfActiveHotkeys = 0;
		for (int i = 0; i < player.hotkeys.length; i++)
			if (player.hotkeys[i] != -1)
				numOfActiveHotkeys++;
		int k = 0;
		for (int i = 0; i < niceHotKeys.length; i++)
			if (player.hotkeys[i] != -1)
			{
				k++;
				niceHotKeys[i] = new Point();
				niceHotKeys[i].x = (int) (frameWidth / 2 - numOfActiveHotkeys * 40 * UIzoomLevel + k * 80 * UIzoomLevel);
				niceHotKeys[i].y = (int) (frameHeight - 100 * UIzoomLevel);
			}
	}

	void updateFrame()
	{
		updateNiceHotkeys();
	}

	public static void print(Object whatever)
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
