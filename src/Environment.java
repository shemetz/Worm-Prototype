import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class Environment
{
	final int				numOfClouds			= 0;
	final int				minCloudHeight		= 60,
									maxCloudHeight = 400;
	final static double[]	floorFriction		= new double[]
													{ 0.6 };													// depending on floor type
	final static double[]	poolFriction		= new double[]
													{ -1, 0.3, -1, -1, 0.8, 0.2, -1, 0.6, 0.7, 0.3, 0.8, 0.6 };	// depending on pool type
	final static double[]	wallFriction		= new double[]
													{ -1, 0.3, -1, -1, 0.8, 0.2, -1, 0.6, 0.7, 0.3, 0.8, 0.6 };	// depending on wall type
	boolean					devMode				= false;
	boolean					showDamageNumbers	= true;
	Point					windDirection;
	double					shadowX, shadowY;

	final static int		squareWidth			= 96;
	final int				elementalNum;
	// All of these shouldn't be ints, they range from -1 to 12. :/
	int[][]					wallHealths;																		// 2D array of wall healths. -1 = no wall. 100 = full health wall.
	int[][]					wallTypes;																			// 2D array of wall types. Types are equal to the wall's element. -1 = no wall.
	int[][]					poolHealths;
	int[][]					poolTypes;
	BufferedImage[][]		poolImages;																			// cropped images, to join the pool corners
	int[][]					cornerCracks;
	int[][][]				wCornerStyles;																		// x, y, element; corners on the *UP-LEFT* corner of the corresponding square. +1 = +90 degrees clockwise
	int[][][]				pCornerStyles;																		// x, y, element; the int means both the shape and its rotation
	int[][][]				pCornerTransparencies;
	int[][]					floorTypes;																			// -1 = no floor. 0 = ground.
	int						width, height, widthPixels, heightPixels;											// used for camera-blocking purposes
	// 0 1 2 3 4 5 6 7 8 9 10 11
	// "Fire", "Water", "Wind", "Electricity", "Metal", "Ice", "Energy", "Acid", "Lava", "Flesh", "Earth", "Plant"

	List<VisualEffect>		effects;
	List<ArcForceField>		arcFFs;
	List<Person>			people;
	List<Ball>				balls;
	List<Debris>			debris;
	List<UIText>			uitexts;
	List<ForceField>		FFs;
	List<Cloud>				clouds;
	List<Beam>				beams;
	List<Vine>				vines;

	// Sounds
	List<SoundEffect>		ongoingSounds		= new ArrayList<SoundEffect>();

	public Environment(int width1, int height1)
	{
		this.width = width1;
		this.height = height1;
		elementalNum = Resources.elementalNum;
		wallHealths = new int[width][height];
		wallTypes = new int[width][height];
		poolHealths = new int[width][height];
		poolTypes = new int[width][height];
		poolImages = new BufferedImage[width][height];
		floorTypes = new int[width][height];
		cornerCracks = new int[width][height];
		wCornerStyles = new int[width][height][elementalNum];
		pCornerStyles = new int[width][height][elementalNum];
		pCornerTransparencies = new int[width][height][elementalNum];
		this.widthPixels = width * squareWidth;
		this.heightPixels = height * squareWidth;
		// default shadow position is directly below.
		shadowX = 0;
		shadowY = 0;
		effects = new ArrayList<VisualEffect>();
		people = new ArrayList<Person>();
		arcFFs = new ArrayList<ArcForceField>();
		balls = new ArrayList<Ball>();
		debris = new ArrayList<Debris>();
		uitexts = new ArrayList<UIText>();
		FFs = new ArrayList<ForceField>();
		clouds = new ArrayList<Cloud>();
		beams = new ArrayList<Beam>();
		vines = new ArrayList<Vine>();

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			{
				wallHealths[x][y] = -1;
				wallTypes[x][y] = -1;
				poolHealths[x][y] = -1;
				poolTypes[x][y] = -1;
				poolImages[x][y] = null;
				floorTypes[x][y] = -1;
				cornerCracks[x][y] = -1;
				for (int i = 0; i < elementalNum; i++)
				{
					wCornerStyles[x][y][i] = -1;
					pCornerStyles[x][y][i] = -1;
					pCornerTransparencies[x][y][i] = 100;
				}
			}
		// random cloud generation
		for (int i = 0; i < numOfClouds; i++)
		{
			int x = Main.random.nextInt(widthPixels * 3) - widthPixels;
			int y = Main.random.nextInt(heightPixels * 3) - heightPixels;
			int z = minCloudHeight + Main.random.nextInt(maxCloudHeight - minCloudHeight);
			int type = Main.random.nextInt(Resources.clouds.size());
			clouds.add(new Cloud(x, y, z, type));
		}
		windDirection = new Point(Main.random.nextInt(11) - 5, Main.random.nextInt(11) - 5);
	}

	private int			healthSum		= 0,
								poolNum = 0;
	private boolean[][]	checkedSquares	= new boolean[width][height];

	public void updatePools()
	{
		checkedSquares = new boolean[width][height];
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
			{
				int element = poolTypes[i][j];
				if (element != -1)
				{
					// spreading health, updating corner transparencies
					healthSum = 0;
					poolNum = 0;
					recursivePoolCheck(i, j, element);
					recursivePoolCheck(i - 1, j, element);
					recursivePoolCheck(i, j - 1, element);
					recursivePoolCheck(i + 1, j, element);
					recursivePoolCheck(i, j + 1, element);
					recursivePoolCheck(i - 1, j - 1, element);
					recursivePoolCheck(i - 1, j + 1, element);
					recursivePoolCheck(i + 1, j - 1, element);
					recursivePoolCheck(i + 1, j + 1, element);
					poolHealths[i][j] = healthSum / poolNum;
					recursivePoolUpdate(i, j, element, healthSum / poolNum);
					recursivePoolUpdate(i - 1, j - 1, element, healthSum / poolNum);
					recursivePoolUpdate(i - 1, j + 1, element, healthSum / poolNum);
					recursivePoolUpdate(i + 1, j - 1, element, healthSum / poolNum);
					recursivePoolUpdate(i + 1, j + 1, element, healthSum / poolNum);

					// updating image, depending on the connections to pool corners
					boolean a = pCornerStyles[i][j][element] != -1; // top left
					boolean b = pCornerStyles[i + 1][j][element] != -1; // top right
					boolean c = pCornerStyles[i + 1][j + 1][element] != -1; // bottom right
					boolean d = pCornerStyles[i][j + 1][element] != -1; // bottom left

					if (!a && !b && !c && !d) // lone pool
						poolImages[i][j] = Resources.pool[element];

					else if (a && !b && !c && !d) // LU
						poolImages[i][j] = Resources.croppedPool[element][0];
					else if (!a && b && !c && !d) // RU
						poolImages[i][j] = Resources.croppedPool[element][1];
					else if (!a && !b && c && !d) // RB
						poolImages[i][j] = Resources.croppedPool[element][2];
					else if (!a && !b && !c && d) // LB
						poolImages[i][j] = Resources.croppedPool[element][3];

					else if (!a && b && !c && d) // RU + LB
						poolImages[i][j] = Resources.croppedPool[element][4];
					else if (a && !b && c && !d) // LU + RB
						poolImages[i][j] = Resources.croppedPool[element][5];

					else if (!a && b && c && !d) // R
						poolImages[i][j] = Resources.croppedPool[element][6];
					else if (!a && !b && c && d) // B
						poolImages[i][j] = Resources.croppedPool[element][7];
					else if (a && !b && !c && d) // L
						poolImages[i][j] = Resources.croppedPool[element][8];
					else if (a && b && !c && !d) // U
						poolImages[i][j] = Resources.croppedPool[element][9];

					else if (!a && b && c && d) // not LU
						poolImages[i][j] = Resources.croppedPool[element][10];
					else if (a && !b && c && d) // not RU
						poolImages[i][j] = Resources.croppedPool[element][11];
					else if (a && b && !c && d) // not RB
						poolImages[i][j] = Resources.croppedPool[element][12];
					else if (a && b && c && !d) // not LB
						poolImages[i][j] = Resources.croppedPool[element][13];

					else if (a && b && c && d) // all corners
						poolImages[i][j] = null;

				}
			}
	}

	private void recursivePoolCheck(int x, int y, int elementNum)
	{
		if (poolTypes[x][y] != elementNum || checkedSquares[x][y])
			return;
		healthSum += poolHealths[x][y];
		poolNum++;
		checkedSquares[x][y] = true;
		recursivePoolCheck(x - 1, y, elementNum);
		recursivePoolCheck(x, y - 1, elementNum);
		recursivePoolCheck(x + 1, y, elementNum);
		recursivePoolCheck(x, y + 1, elementNum);
		recursivePoolCheck(x - 1, y - 1, elementNum);
		recursivePoolCheck(x + 1, y - 1, elementNum);
		recursivePoolCheck(x + 1, y + 1, elementNum);
		recursivePoolCheck(x - 1, y + 1, elementNum);
	}

	private void recursivePoolUpdate(int x, int y, int elementNum, int newHealth)
	{
		if (poolTypes[x][y] != elementNum || !checkedSquares[x][y])
			return;
		poolHealths[x][y] = newHealth;
		if (pCornerStyles[x][y][elementNum] != -1)
			pCornerTransparencies[x][y][elementNum] = newHealth;
		if (pCornerStyles[x + 1][y + 1][elementNum] != -1)
			pCornerTransparencies[x + 1][y + 1][elementNum] = newHealth;
		if (pCornerStyles[x + 1][y][elementNum] != -1)
			pCornerTransparencies[x + 1][y][elementNum] = newHealth;
		if (pCornerStyles[x][y + 1][elementNum] != -1)
			pCornerTransparencies[x][y + 1][elementNum] = newHealth;
		checkedSquares[x][y] = false;
		recursivePoolUpdate(x - 1, y, elementNum, newHealth);
		recursivePoolUpdate(x, y - 1, elementNum, newHealth);
		recursivePoolUpdate(x + 1, y, elementNum, newHealth);
		recursivePoolUpdate(x, y + 1, elementNum, newHealth);
		recursivePoolUpdate(x - 1, y - 1, elementNum, newHealth);
		recursivePoolUpdate(x + 1, y - 1, elementNum, newHealth);
		recursivePoolUpdate(x + 1, y + 1, elementNum, newHealth);
		recursivePoolUpdate(x - 1, y + 1, elementNum, newHealth);
	}

	public void playSound(String s)
	{
		SoundEffect sound = null;
		switch (s)
		{
		case "Rock Smash":
			sound = (new SoundEffect("rock-smash.wav", s));
			break;
		default:
			Main.errorMessage("What's that? I can't hear you!");
			return;
		}
		sound.play();
	}

	public void drawFloor(Graphics2D buffer, Frame that, final Rectangle bounds)
	{
		// Pools (and floors)
		for (int x = 0; x < width; x++)
			if (x * squareWidth > bounds.getMinX() - squareWidth && x * squareWidth < bounds.getMaxX())
				for (int y = 0; y < height; y++)
					if (y * squareWidth > bounds.getMinY() - squareWidth && y * squareWidth < bounds.getMaxY())
					{
						// floor
						if (floorTypes[x][y] != -1)
							buffer.drawImage(Resources.floor[floorTypes[x][y]], x * squareWidth, y * squareWidth, that);
						// pools
						if (poolTypes[x][y] != -1)
						{
							buffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f + 0.009f * poolHealths[x][y]));
							buffer.drawImage(poolImages[x][y], x * squareWidth, y * squareWidth, that);
							buffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
						}
						// pool corners
						for (int i = elementalNum - 1; i >= 0; i--)
							// decreasing order because I want earth walls to be bottomest and earth is one of the last elements
							if (pCornerStyles[x][y][i] != -1)
							{
								BufferedImage cornerImg = Resources.pCorner[i][getStyle(pCornerStyles[x][y][i])];
								cornerImg = Methods.rotate(cornerImg, 0.5 * Math.PI * Environment.getAngle(pCornerStyles[x][y][i]), that);
								buffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f + 0.009f * pCornerTransparencies[x][y][i]));
								buffer.drawImage(cornerImg, x * squareWidth - squareWidth / 2, y * squareWidth - squareWidth / 2, that);
								buffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
							}
					}
	}

	public void draw(Graphics2D buffer, Frame that, int cameraZed, final Rectangle bounds, double cameraRotation)
	{
		List<Drawable> drawableThings = new ArrayList<Drawable>();
		drawableThings.addAll(people);
		drawableThings.addAll(clouds);
		drawableThings.addAll(balls);
		drawableThings.addAll(FFs);
		drawableThings.addAll(debris);
		drawableThings.addAll(arcFFs);
		drawableThings.addAll(beams);
		drawableThings.addAll(vines);
		Predicate<Drawable> outOfScreen = new Predicate<Drawable>()
		{
			public boolean test(Drawable arg0)
			{
				// debugging
				if (arg0 == null || arg0.image == null)
				{
					Main.errorMessage("ummm what? " + arg0);
					return true;
				}
				if (arg0.image.getWidth() == 1 && arg0.image.getHeight() == 1) // for stuff like Beam images
					return false;
				if (arg0.x + arg0.image.getWidth() / 2 < bounds.getMinX() || arg0.x - arg0.image.getWidth() / 2 > bounds.getMaxX() || arg0.y + arg0.image.getHeight() / 2 < bounds.getMinY()
						|| arg0.y - arg0.image.getHeight() / 2 > bounds.getMaxY())
					return true;
				return false;
			}
		};
		Comparator<Drawable> sortByHeight = new Comparator<Drawable>()
		{
			public int compare(Drawable d1, Drawable d2)
			{
				Integer i1 = new Integer((int) d1.z);
				Integer i2 = new Integer((int) d2.z);
				return i1.compareTo(i2);
			}
		};
		drawableThings.removeIf(outOfScreen);
		Collections.sort(drawableThings, sortByHeight);
		// Clouds, people, balls, force fields, debris, arc force fields, beams, vines
		drawDrawables(buffer, cameraZed, cameraRotation, drawableThings, -1, 1);

		// Walls and wall corners
		drawWalls(buffer, bounds, that);

		// Clouds, people, balls, force fields, debris, arc force fields, beams, vines
		drawDrawables(buffer, cameraZed, cameraRotation, drawableThings, 1, Integer.MAX_VALUE);

		// Combat UI
		if (showDamageNumbers)
			for (UIText ui : uitexts)
			{
				buffer.setColor(new Color(ui.color.getRed(), ui.color.getGreen(), ui.color.getBlue(), ui.transparency));
				buffer.setFont(new Font("Sans-Serif", Font.BOLD, ui.fontSize));
				buffer.drawString(ui.text, ui.x, ui.y);
			}
	}

	public void drawWalls(Graphics2D buffer, Rectangle bounds, Frame that)
	{
		for (int x = 0; x < width; x++)
			if (x * squareWidth > bounds.getMinX() - squareWidth && x * squareWidth < bounds.getMaxX())
				for (int y = 0; y < height; y++)
					if (y * squareWidth > bounds.getMinY() - squareWidth && y * squareWidth < bounds.getMaxY())
					{
						// walls
						if (wallTypes[x][y] == -2)
						{
							buffer.setColor(new Color(165, 165, 165));
							buffer.fillRect(x * squareWidth, y * squareWidth, squareWidth, squareWidth);
						} else if (wallTypes[x][y] != -1)
						{
							buffer.drawImage(Resources.wall[wallTypes[x][y]], x * squareWidth, y * squareWidth, null);
							if (wallHealths[x][y] <= 25)
								buffer.drawImage(Resources.cracks[2][11], x * squareWidth, y * squareWidth, null);
							else if (wallHealths[x][y] <= 50)
								buffer.drawImage(Resources.cracks[1][11], x * squareWidth, y * squareWidth, null);
							else if (wallHealths[x][y] <= 75)
								buffer.drawImage(Resources.cracks[0][11], x * squareWidth, y * squareWidth, null);
						}
						// wall corners
						for (int i = elementalNum - 1; i >= 0; i--)
							if (wCornerStyles[x][y][i] != -1)
							{
								BufferedImage cornerImg = Resources.wCorner[i][getStyle(wCornerStyles[x][y][i])];
								cornerImg = Methods.rotate(cornerImg, 0.5 * Math.PI * Environment.getAngle(wCornerStyles[x][y][i]), that);
								buffer.drawImage(cornerImg, x * squareWidth - squareWidth / 2, y * squareWidth - squareWidth / 2, null);

								// cracks
								if (cornerCracks[x][y] != -1) // 0 = <75, 1 = <50, 2 = <25
								{
									cornerImg = Resources.cracks[cornerCracks[x][y]][wCornerStyles[x][y][i]];
									// no rotation
									buffer.drawImage(cornerImg, x * squareWidth - squareWidth / 2, y * squareWidth - squareWidth / 2, null);
								}
							}
					}
	}

	public void drawDrawables(Graphics2D buffer, int cameraZed, double cameraRotation, List<Drawable> drawableThings, double minZ, double maxZ)
	{
		for (Drawable d : drawableThings)
		{
			if (d.getClass().equals(Player.class) && ((Player) d).ghostMode) // very specific change, for ghost-players
			{
				if (d.z + 1 >= minZ && d.z + 1 < maxZ)
				{
					// Ghosts are drawn as if they were 1 z-unit above other things, so that the player can see the phasing effect
					d.draw(buffer, cameraZed);

					// copypasta code from down below... :(
					//
					Person p = (Person) d;
					if (showDamageNumbers)
						p.drawUITexts(buffer, cameraZed, cameraRotation);
					if (devMode) // draws helpful things in the x,y of the object. (NOT Z AXIS)
					{
						// center
						buffer.setColor(Color.green);
						buffer.drawRect((int) (p.x - 1), (int) (p.y - 1), 3, 3);
						// hitbox
						buffer.drawRect((int) (p.x - 0.5 * p.radius), (int) (p.y - 0.5 * p.radius), p.radius, p.radius);
						// target
						buffer.setColor(Color.red);
						buffer.drawOval(p.target.x - 10, p.target.y - 10, 20, 20);
						// id
						buffer.setColor(Color.red);
						buffer.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
						buffer.drawString("" + p.id, (int) p.x, (int) p.y + p.imgH / 2);
						// movement line
						buffer.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
						buffer.setColor(Color.red);
						buffer.drawLine((int) (p.x), (int) (p.y), (int) (p.x + p.strengthOfAttemptedMovement * 100 * Math.cos(p.directionOfAttemptedMovement)),
								(int) (p.y + p.strengthOfAttemptedMovement * 100 * Math.sin(p.directionOfAttemptedMovement)));
					}
					//
				}
			} else if (d.z >= minZ && d.z < maxZ) // what will basically almost always happen for everything
			{
				// important drawings:
				d.drawShadow(buffer, shadowX, shadowY);
				d.draw(buffer, cameraZed);
				if (d.getClass().equals(Person.class) || d.getClass().getSuperclass().equals(Person.class))
				{
					Person p = (Person) d;
					if (showDamageNumbers)
						p.drawUITexts(buffer, cameraZed, cameraRotation);
				}
				if (d.getClass().equals(Beam.class))
				{
					Beam b = (Beam) d;
					b.drawTopEffects(buffer, cameraZed); // TODO why not include this in draw()?
				}
				// dev-mode debugging unimportant drawings:
				if (devMode) // draws helpful things in the x,y of the object. (NOT Z AXIS)
				{
					if (d instanceof Vine)
					{
						Vine v = (Vine) d;
						buffer.setStroke(new BasicStroke(2));
						buffer.setColor(Color.red);
						buffer.drawOval(v.start.x - 3, v.start.y - 3, 7, 7);
						buffer.drawOval(v.end.x - 3, v.end.y - 3, 7, 7);
						buffer.drawLine(v.start.x, v.start.y, v.end.x, v.end.y);
						if (v.state == 0)
						{
							buffer.setColor(Color.white);
							buffer.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
							buffer.drawOval(v.end.x - Vine.grabblingRange, v.end.y - Vine.grabblingRange, 2 * Vine.grabblingRange, 2 * Vine.grabblingRange);
						}
						if (v.state == 1)
						{
							buffer.setColor(Color.blue);
							buffer.setStroke(new BasicStroke(2));
							buffer.drawLine((int) (v.end.x + v.deltaLength * Math.cos(v.rotation)), (int) (v.end.y + v.deltaLength * Math.sin(v.rotation)), v.end.x, v.end.y);
						}
					}
					if (d instanceof Beam)
					{
						Beam b = (Beam) d;
						buffer.setColor(Color.red);
						buffer.drawOval(b.start.x - 3, b.start.y - 3, 7, 7);
						buffer.drawOval(b.end.x - 3, b.end.y - 3, 7, 7);
						buffer.drawLine(b.start.x, b.start.y, b.end.x, b.end.y);
					}
					if (d instanceof Ball)
					{
						Ball b = (Ball) d;
						buffer.setColor(Color.red);
						buffer.drawRect((int) (b.x - 1), (int) (b.y - 1), 3, 3);
						buffer.setColor(Color.green);
						buffer.drawOval((int) (b.x - b.radius), (int) (b.y - b.radius), (int) (2 * b.radius), (int) (2 * b.radius));
					}
					if (d instanceof Player)
					{
						Person p = (Person) d;
						// center
						buffer.setColor(Color.green);
						buffer.drawRect((int) (p.x - 1), (int) (p.y - 1), 3, 3);
						// hitbox
						buffer.drawRect((int) (p.x - 0.5 * p.radius), (int) (p.y - 0.5 * p.radius), p.radius, p.radius);
						// target
						buffer.setColor(Color.red);
						buffer.drawOval(p.target.x - 10, p.target.y - 10, 20, 20);
						// id
						buffer.setColor(Color.red);
						buffer.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
						buffer.drawString("" + p.id, (int) p.x, (int) p.y + p.imgH / 2);
						// movement line
						buffer.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
						buffer.setColor(Color.red);
						buffer.drawLine((int) (p.x), (int) (p.y), (int) (p.x + p.strengthOfAttemptedMovement * 100 * Math.cos(p.directionOfAttemptedMovement)),
								(int) (p.y + p.strengthOfAttemptedMovement * 100 * Math.sin(p.directionOfAttemptedMovement)));
					}
					if (d instanceof NPC)
					{
						Person p = (Person) d;
						// center
						buffer.setColor(new Color(160, 255, 160));
						buffer.drawRect((int) (p.x - 1), (int) (p.y - 1), 3, 3);
						// hitbox
						buffer.drawRect((int) (p.x - 0.5 * p.radius), (int) (p.y - 0.5 * p.radius), p.radius, p.radius);
						// target
						buffer.setColor(new Color(255, 160, 160));
						buffer.drawOval(p.target.x - 10, p.target.y - 10, 20, 20);
						// id
						buffer.setColor(Color.red);
						buffer.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
						buffer.drawString("" + p.id, (int) p.x, (int) p.y + p.imgH / 2);
						// movement line
						buffer.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
						buffer.setColor(Color.red);
						buffer.drawLine((int) (p.x), (int) (p.y), (int) (p.x + p.strengthOfAttemptedMovement * 100 * Math.cos(p.directionOfAttemptedMovement)),
								(int) (p.y + p.strengthOfAttemptedMovement * 100 * Math.sin(p.directionOfAttemptedMovement)));
					}
					if (d instanceof ForceField)
					{
						ForceField ff = (ForceField) d;
						buffer.setColor(Color.red);
						int i = -1;
						for (Point p : ff.p)
						{
							i++;
							buffer.drawRect(p.x - 1, p.y - 1, 3, 3);
							buffer.drawString("" + i, p.x, p.y - 6);
						}
					}
					if (d instanceof ArcForceField)
					{
						ArcForceField aff = (ArcForceField) d;
						buffer.setColor(Color.green);
						buffer.drawArc((int) (aff.target.x - aff.maxRadius), (int) (aff.target.y - aff.maxRadius), (int) (aff.maxRadius * 2), (int) (aff.maxRadius * 2),
								(int) (-aff.rotation / Math.PI * 180 - aff.arc / Math.PI * 90), (int) (aff.arc / Math.PI * 180));
						buffer.drawArc((int) (aff.target.x - aff.minRadius), (int) (aff.target.y - aff.minRadius), (int) (aff.minRadius * 2), (int) (aff.minRadius * 2),
								(int) (-aff.rotation / Math.PI * 180 - aff.arc / Math.PI * 90), (int) (aff.arc / Math.PI * 180));
						buffer.drawLine((int) (aff.target.x + aff.minRadius * Math.cos(aff.rotation - 0.5 * aff.arc)), (int) (aff.target.y + aff.minRadius * Math.sin(aff.rotation - 0.5 * aff.arc)),
								(int) (aff.target.x + aff.maxRadius * Math.cos(aff.rotation - 0.5 * aff.arc)), (int) (aff.target.y + aff.maxRadius * Math.sin(aff.rotation - 0.5 * aff.arc)));
						buffer.drawLine((int) (aff.target.x + aff.minRadius * Math.cos(aff.rotation + 0.5 * aff.arc)), (int) (aff.target.y + aff.minRadius * Math.sin(aff.rotation + 0.5 * aff.arc)),
								(int) (aff.target.x + aff.maxRadius * Math.cos(aff.rotation + 0.5 * aff.arc)), (int) (aff.target.y + aff.maxRadius * Math.sin(aff.rotation + 0.5 * aff.arc)));

						buffer.setColor(Color.red);
						buffer.drawArc((int) (aff.target.x - (aff.maxRadius + 20)), (int) (aff.target.y - (aff.maxRadius + 20)), (int) ((aff.maxRadius + 20) * 2), (int) ((aff.maxRadius + 20) * 2),
								(int) (-aff.rotation / Math.PI * 180 - (aff.arc + 2 * 20 / aff.maxRadius) / Math.PI * 90), (int) ((aff.arc + 2 * 20 / aff.maxRadius) / Math.PI * 180));
						buffer.drawArc((int) (aff.target.x - (aff.minRadius - 20)), (int) (aff.target.y - (aff.minRadius - 20)), (int) ((aff.minRadius - 20) * 2), (int) ((aff.minRadius - 20) * 2),
								(int) (-aff.rotation / Math.PI * 180 - (aff.arc + 2 * 20 / aff.maxRadius) / Math.PI * 90), (int) ((aff.arc + 2 * 20 / aff.maxRadius) / Math.PI * 180));
						buffer.drawLine((int) (aff.target.x + (aff.minRadius - 20) * Math.cos(aff.rotation - 0.5 * (aff.arc + 2 * 20 / aff.maxRadius))),
								(int) (aff.target.y + (aff.minRadius - 20) * Math.sin(aff.rotation - 0.5 * (aff.arc + 2 * 20 / aff.maxRadius))),
								(int) (aff.target.x + (aff.maxRadius + 20) * Math.cos(aff.rotation - 0.5 * (aff.arc + 2 * 20 / aff.maxRadius))),
								(int) (aff.target.y + (aff.maxRadius + 20) * Math.sin(aff.rotation - 0.5 * (aff.arc + 2 * 20 / aff.maxRadius))));
						buffer.drawLine((int) (aff.target.x + (aff.minRadius - 20) * Math.cos(aff.rotation + 0.5 * (aff.arc + 2 * 20 / aff.maxRadius))),
								(int) (aff.target.y + (aff.minRadius - 20) * Math.sin(aff.rotation + 0.5 * (aff.arc + 2 * 20 / aff.maxRadius))),
								(int) (aff.target.x + (aff.maxRadius + 20) * Math.cos(aff.rotation + 0.5 * (aff.arc + 2 * 20 / aff.maxRadius))),
								(int) (aff.target.y + (aff.maxRadius + 20) * Math.sin(aff.rotation + 0.5 * (aff.arc + 2 * 20 / aff.maxRadius))));
					}
				}
			}
		}
	}

	public void addWall(int x, int y, int elementalType, boolean fullHealth)
	{
		if (poolTypes[x][y] != -1 || (wallTypes[x][y] != -1 && fullHealth))
			remove(x, y);
		wallTypes[x][y] = elementalType;
		if (fullHealth)
			wallHealths[x][y] = 100;
		checkWCorner(elementalType, x, y);
		checkWCorner(elementalType, x + 1, y);
		checkWCorner(elementalType, x, y + 1);
		checkWCorner(elementalType, x + 1, y + 1);
	}

	public void addPool(int x, int y, int elementalType, boolean fullHealth)
	{
		if (wallTypes[x][y] != -1 || (poolTypes[x][y] != -1 && fullHealth))
			remove(x, y);
		poolTypes[x][y] = elementalType;
		if (fullHealth)
			poolHealths[x][y] = 100;
		checkPCorner(elementalType, x, y);
		checkPCorner(elementalType, x + 1, y);
		checkPCorner(elementalType, x, y + 1);
		checkPCorner(elementalType, x + 1, y + 1);
		updatePools();
	}

	public void remove(int x, int y)
	{
		int element = -1;
		if (wallTypes[x][y] != -1)
		{
			element = wallTypes[x][y];
			wallTypes[x][y] = -1;
			wallHealths[x][y] = -1;
			checkWCorner(element, x, y);
			checkWCorner(element, x + 1, y);
			checkWCorner(element, x, y + 1);
			checkWCorner(element, x + 1, y + 1);
		}
		if (poolTypes[x][y] != -1)
		{
			element = poolTypes[x][y];
			poolTypes[x][y] = -1;
			poolHealths[x][y] = -1;
			checkPCorner(element, x, y);
			checkPCorner(element, x + 1, y);
			checkPCorner(element, x, y + 1);
			checkPCorner(element, x + 1, y + 1);
			updatePools();
		}
	}

	public void destroyWall(int x, int y)
	{
		int elementNum = wallTypes[x][y];
		for (int i = 0; i < 5; i++)
			debris.add(new Debris(x * squareWidth + 0.5 * squareWidth, y * squareWidth + 0.5 * squareWidth, 0, Math.PI * 2 / 5 * i, elementNum, 200));
		remove(x, y);
	}

	public void connectPool(int x, int y)
	{
		int element = -1;
		if (poolTypes[x][y] != -1)
		{
			element = poolTypes[x][y];
			checkPCorner(element, x, y);
			checkPCorner(element, x + 1, y);
			checkPCorner(element, x, y + 1);
			checkPCorner(element, x + 1, y + 1);
			updatePools();
		}
	}

	public void connectWall(int x, int y)
	{
		int element = -1;
		if (wallTypes[x][y] != -1)
		{
			element = wallTypes[x][y];
			checkWCorner(element, x, y);
			checkWCorner(element, x + 1, y);
			checkWCorner(element, x, y + 1);
			checkWCorner(element, x + 1, y + 1);
		}
	}

	void checkWCorner(int e, int x, int y)
	{
		if (x < width - 1 && y < height - 1 && x > 1 && y > 1)
		{
			boolean a = wallTypes[x - 1][y - 1] == e; // LU
			boolean b = wallTypes[x][y - 1] == e; // RU
			boolean c = wallTypes[x - 1][y] == e; // LD
			boolean d = wallTypes[x][y] == e; // RD
			int numOfTakens = (a ? 1 : 0) + (b ? 1 : 0) + (c ? 1 : 0) + (d ? 1 : 0);
			if (numOfTakens < 2)
			{
				wCornerStyles[x][y][e] = -1;
				cornerCracks[x][y] = -1;
			} else
			{
				// damage crack images
				int damageNumbers = 0;
				if (a)
					damageNumbers += wallHealths[x - 1][y - 1];
				if (b)
					damageNumbers += wallHealths[x][y - 1];
				if (c)
					damageNumbers += wallHealths[x - 1][y];
				if (d)
					damageNumbers += wallHealths[x][y];
				damageNumbers /= numOfTakens;
				damageNumbers /= 25;
				if (damageNumbers >= 3)
					damageNumbers = -1;
				else
					damageNumbers = 2 - damageNumbers;
				cornerCracks[x][y] = damageNumbers;

				if (a && b && !c && !d)
					wCornerStyles[x][y][e] = 0; // up
				else if (!a && b && !c && d)
					wCornerStyles[x][y][e] = 1; // right
				else if (!a && !b && c && d)
					wCornerStyles[x][y][e] = 2; // down
				else if (a && !b && c && !d)
					wCornerStyles[x][y][e] = 3; // left

				else if (a && !b && !c && d)
					wCornerStyles[x][y][e] = 4; // bridge 1
				else if (!a && b && c && !d)
					wCornerStyles[x][y][e] = 5; // bridge 2

				else if (a && b && c && !d)
					wCornerStyles[x][y][e] = 6; // all except RD
				else if (a && b && !c && d)
					wCornerStyles[x][y][e] = 7; // all except LD
				else if (!a && b && c && d)
					wCornerStyles[x][y][e] = 8; // all except LU
				else if (a && !b && c && d)
					wCornerStyles[x][y][e] = 9; // all except RU

				else if (a && b && c && d)
					wCornerStyles[x][y][e] = 10;
			}
		}
		// else, I don't have time for this shit
	}

	void checkPCorner(int e, int x, int y)
	{
		if (x < width - 1 && y < height - 1 && x > 0 && y > 0)
		{
			boolean a = poolTypes[x - 1][y - 1] == e;
			boolean b = poolTypes[x][y - 1] == e;
			boolean c = poolTypes[x - 1][y] == e;
			boolean d = poolTypes[x][y] == e;
			int numOfTakens = (a ? 1 : 0) + (b ? 1 : 0) + (c ? 1 : 0) + (d ? 1 : 0);
			if (numOfTakens < 2)
				pCornerStyles[x][y][e] = -1;
			else if (a && b && !c && !d)
				pCornerStyles[x][y][e] = 0;
			else if (!a && b && !c && d)
				pCornerStyles[x][y][e] = 1;
			else if (!a && !b && c && d)
				pCornerStyles[x][y][e] = 2;
			else if (a && !b && c && !d)
				pCornerStyles[x][y][e] = 3;

			else if (a && !b && !c && d)
				pCornerStyles[x][y][e] = 4;
			else if (!a && b && c && !d)
				pCornerStyles[x][y][e] = 5;

			else if (a && b && c && !d)
				pCornerStyles[x][y][e] = 6;
			else if (a && b && !c && d)
				pCornerStyles[x][y][e] = 7;
			else if (!a && b && c && d)
				pCornerStyles[x][y][e] = 8;
			else if (a && !b && c && d)
				pCornerStyles[x][y][e] = 9;

			else if (a && b && c && d)
				pCornerStyles[x][y][e] = 10;
		}
		// else, I don't have time for this shit
	}

	static int getAngle(int x)
	{
		int ans = 0;
		if (x < 4)
			ans = x;
		if (x >= 4)
			ans = x - 4;
		if (x >= 6)
			ans = x - 6;
		return ans;
	}

	static int getStyle(int x)
	{
		if (x < 4)
			return 0;
		if (x < 6)
			return 1;
		if (x < 10)
			return 2;
		else
			return 3;
	}
}
