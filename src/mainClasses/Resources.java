package mainClasses;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mainResourcesPackage.ResourceLoader;

public class Resources
{
	static BufferedImage[]							wall;																// type
	static BufferedImage[]							pool;																// type
	static BufferedImage[][]						croppedPool;														// type, 0123 = abcd, 45 = two bridge types
	static BufferedImage[]							floor;																// type
	static BufferedImage[][]						cracks;																// health type, connection style
	static BufferedImage[][][]						wCorner;															// type, connection (up, bridge, bite, full), angle (in quarter-circles)
	static BufferedImage[][][]						pCorner;															// type, connection (up, bridge, bite, full), angle (in quarter-circles)
	static List<List<List<List<BufferedImage>>>>	bodyPart;															// body part, permutation, state, frame
	static Map<String, BufferedImage>				icons;																// ability, frame
	final static int								elementalNum	= 12;
	static BufferedImage[][]						arcForceFields;														// element (12 = Prot. Bubble), frame (healthy / 75% / 50% / 25%)
	final static int								arcFFImageWidth	= 288;
	static BufferedImage[]							balls;																// element
	static BufferedImage[][]						beams;																// element, type (0,1,2,3 = start, 4,5,6,7 = middle, 8,9,10,11 = flat end, 12,13,14,15 regular end)
	static BufferedImage[][]						sprayDrops;															// element, type
	static BufferedImage[][]						debris;																// element, type. 12 = smoke, 13 = Force Field.
	static BufferedImage[][]						debrisShadows;														// element, type. ^
	final static int								debrisWidth		= 40;
	static List<BufferedImage>						clouds;
	static List<BufferedImage>						cloudShadows;
	static List<List<BufferedImage>>				effects;
	static BufferedImage[]							healingBeam;														// frame
	static List<List<BufferedImage>>				explosions;															// type, frame

	static BufferedImage							range_net		= ResourceLoader.getBufferedImage("range_net.png");

	public static void initialize()
	{
		wall = new BufferedImage[elementalNum];
		pool = new BufferedImage[elementalNum];
		croppedPool = new BufferedImage[elementalNum][14];
		floor = new BufferedImage[1];
		wCorner = new BufferedImage[elementalNum][4][4];
		pCorner = new BufferedImage[elementalNum][4][4];

		bodyPart = new ArrayList<List<List<List<BufferedImage>>>>(); // LEGS, CHEST (+arms), HEAD, HAIR

		icons = new HashMap<String, BufferedImage>();
		arcForceFields = new BufferedImage[elementalNum + 1][4]; // 0-11 = elemental, 12 = Prot. Bubble
		balls = new BufferedImage[elementalNum];
		sprayDrops = new BufferedImage[elementalNum][3];
		debris = new BufferedImage[elementalNum + 2][6];
		debrisShadows = new BufferedImage[debris.length][6];
		cracks = new BufferedImage[3][12]; // 11 is saved for the original wall, not corners
		clouds = new ArrayList<BufferedImage>();
		cloudShadows = new ArrayList<BufferedImage>();
		effects = new ArrayList<List<BufferedImage>>();
		beams = new BufferedImage[elementalNum][16];
		healingBeam = new BufferedImage[4];
		explosions = new ArrayList<List<BufferedImage>>();

		for (int i = 0; i < elementalNum; i++) // all things that occur per element are in this loop
		{
			pool[i] = ResourceLoader.getBufferedImage("environment/pool_" + EP.elementList[i] + ".png");
			wall[i] = ResourceLoader.getBufferedImage("environment/wall_" + EP.elementList[i] + ".png");
			wCorner[i][0][0] = ResourceLoader.getBufferedImage("environment/wCorner_" + EP.elementList[i] + "_up.png");
			wCorner[i][1][0] = ResourceLoader.getBufferedImage("environment/wCorner_" + EP.elementList[i] + "_bridge.png");
			wCorner[i][2][0] = ResourceLoader.getBufferedImage("environment/wCorner_" + EP.elementList[i] + "_bite.png");
			wCorner[i][3][0] = ResourceLoader.getBufferedImage("environment/wCorner_" + EP.elementList[i] + "_full.png");
			for (int j = 0; j < 4; j++) //Rotated corner images
				for (int k = 1; k < 4; k++)
					wCorner[i][j][k] = Methods.rotate(wCorner[i][j][0], 0.5 * Math.PI * k);
			pCorner[i][0][0] = ResourceLoader.getBufferedImage("environment/pCorner_" + EP.elementList[i] + "_up.png");
			pCorner[i][1][0] = ResourceLoader.getBufferedImage("environment/pCorner_" + EP.elementList[i] + "_bridge.png");
			pCorner[i][2][0] = ResourceLoader.getBufferedImage("environment/pCorner_" + EP.elementList[i] + "_bite.png");
			pCorner[i][3][0] = ResourceLoader.getBufferedImage("environment/pCorner_" + EP.elementList[i] + "_full.png");
			for (int j = 0; j < 4; j++) //Rotated corner images
				for (int k = 1; k < 4; k++)
					pCorner[i][j][k] = Methods.rotate(pCorner[i][j][0], 0.5 * Math.PI * k);
			for (int j = 0; j < 4; j++)
				arcForceFields[i][j] = ResourceLoader.getBufferedImage("forcefields/" + EP.elementList[i] + "_" + j + ".png");
			for (int j = 0; j < Ability.elementalAttacks.length; j++)
				icons.put(Ability.elementalAttacks[j] + " <" + EP.elementList[i] + ">",
						ResourceLoader.getBufferedImage("icons/abilities/" + EP.elementList[i] + "_" + Ability.elementalAttacks[j] + ".png"));
			icons.put("Elemental Combat I" + " <" + EP.elementList[i] + ">", ResourceLoader.getBufferedImage("icons/abilities/" + EP.elementList[i] + "_" + "Elemental Combat I" + ".png"));
			icons.put("Elemental Combat II" + " <" + EP.elementList[i] + ">", ResourceLoader.getBufferedImage("icons/abilities/" + EP.elementList[i] + "_" + "Elemental Combat II" + ".png"));
			// TODO icons for Charged Ball and Charge Beam
			balls[i] = ResourceLoader.getBufferedImage("elementalAbilities/" + EP.elementList[i] + "_Ball.png");
			for (int j = 0; j < 3; j++)
				sprayDrops[i][j] = ResourceLoader.getBufferedImage("elementalAbilities/" + EP.elementList[i] + "_Spray_" + j + ".png");
			for (int j = 0; j < 3; j++)
				debris[i][j] = ResourceLoader.getBufferedImage("debris/" + EP.elementList[i] + "_debris_" + j + ".png");
			for (int j = 0; j < 3; j++)
				debris[i][j + 3] = ResourceLoader.getBufferedImage("debris/" + EP.elementList[i] + "_smalldebris_" + j + ".png");
			for (int j = 0; j < debris[0].length; j++)
				if (debris[i][j] != null)
					debrisShadows[i][j] = Drawable.createShadow(debris[i][j]);

			// cropped pools
			if (pool[i] != null)
			{
				for (int j = 0; j < 14; j++)
				{
					croppedPool[i][j] = new BufferedImage(pool[i].getWidth(), pool[i].getHeight(), BufferedImage.TYPE_INT_ARGB); // if you use pool[i].getType then some of the images (Lava, ice) will convert transparent pixels to black pixels. Dunno
																																	// why.
					Graphics2D temp = croppedPool[i][j].createGraphics();
					temp.drawImage(pool[i], 0, 0, null);
					temp.dispose();
				}
				for (int x = 0; x < 48; x++)
					for (int y = 0; y < 48; y++)
						croppedPool[i][0].setRGB(x, y, 0x00000000);
				for (int x = 48; x < 96; x++)
					for (int y = 0; y < 48; y++)
						croppedPool[i][1].setRGB(x, y, 0x00000000);
				for (int x = 48; x < 96; x++)
					for (int y = 48; y < 96; y++)
						croppedPool[i][2].setRGB(x, y, 0x00000000);
				for (int x = 0; x < 48; x++)
					for (int y = 48; y < 96; y++)
						croppedPool[i][3].setRGB(x, y, 0x00000000);

				// twice for these two:
				for (int x = 48; x < 96; x++)
					for (int y = 0; y < 48; y++)
						croppedPool[i][4].setRGB(x, y, 0x00000000);
				for (int x = 0; x < 48; x++)
					for (int y = 48; y < 96; y++)
						croppedPool[i][4].setRGB(x, y, 0x00000000);
				for (int x = 0; x < 48; x++)
					for (int y = 0; y < 48; y++)
						croppedPool[i][5].setRGB(x, y, 0x00000000);
				for (int x = 48; x < 96; x++)
					for (int y = 48; y < 96; y++)
						croppedPool[i][5].setRGB(x, y, 0x00000000);

				for (int x = 48; x < 96; x++)
					for (int y = 0; y < 96; y++)
						croppedPool[i][6].setRGB(x, y, 0x00000000);
				for (int x = 0; x < 96; x++)
					for (int y = 48; y < 96; y++)
						croppedPool[i][7].setRGB(x, y, 0x00000000);
				for (int x = 0; x < 48; x++)
					for (int y = 0; y < 96; y++)
						croppedPool[i][8].setRGB(x, y, 0x00000000);
				for (int x = 0; x < 96; x++)
					for (int y = 0; y < 48; y++)
						croppedPool[i][9].setRGB(x, y, 0x00000000);

				for (int x = 0; x < 96; x++)
					for (int y = 0; y < 96; y++)
						if (x >= 48 || y >= 48)
							croppedPool[i][10].setRGB(x, y, 0x00000000);
				for (int x = 0; x < 96; x++)
					for (int y = 0; y < 96; y++)
						if (x < 48 || y >= 48)
							croppedPool[i][11].setRGB(x, y, 0x00000000);
				for (int x = 0; x < 96; x++)
					for (int y = 0; y < 96; y++)
						if (x < 48 || y < 48)
							croppedPool[i][12].setRGB(x, y, 0x00000000);
				for (int x = 0; x < 96; x++)
					for (int y = 0; y < 96; y++)
						if (x >= 48 || y < 48)
							croppedPool[i][13].setRGB(x, y, 0x00000000);
			}
			if (i != 11) // not for plants
				for (int j = 1; j < 5; j++) // (beams have 4 frames)
				{
					beams[i][j - 1] = ResourceLoader.getBufferedImage("elementalAbilities/" + EP.elementList[i] + "_Beam_start_" + j + ".png");
					beams[i][j + 4 - 1] = ResourceLoader.getBufferedImage("elementalAbilities/" + EP.elementList[i] + "_Beam_" + j + ".png");
					beams[i][j + 8 - 1] = ResourceLoader.getBufferedImage("elementalAbilities/" + EP.elementList[i] + "_Beam_flatend_" + j + ".png");
					beams[i][j + 12 - 1] = ResourceLoader.getBufferedImage("elementalAbilities/" + EP.elementList[i] + "_Beam_end_" + j + ".png");
				}
			else // yes for plants
			{
				beams[i][0] = ResourceLoader.getBufferedImage("elementalAbilities/Plant_Beam_start.png");
				beams[i][4] = ResourceLoader.getBufferedImage("elementalAbilities/Plant_Beam.png");
				beams[i][12] = ResourceLoader.getBufferedImage("elementalAbilities/Plant_Beam_end.png");
				// rest are empty :/
			}
		}

		// Smoke debris
		for (int j = 0; j < 3; j++)
			debris[12][j] = ResourceLoader.getBufferedImage("debris/Smoke_debris_" + j + ".png");
		for (int j = 0; j < 3; j++)
			debris[12][j + 3] = ResourceLoader.getBufferedImage("debris/Smoke_smalldebris_" + j + ".png");
		for (int j = 0; j < debris[0].length; j++)
			debrisShadows[12][j] = Drawable.createShadow(debris[12][j]);
		// FF debris
		for (int j = 0; j < 3; j++)
			debris[13][j] = ResourceLoader.getBufferedImage("debris/FF_debris_" + j + ".png");
		for (int j = 0; j < 3; j++)
			debris[13][j + 3] = ResourceLoader.getBufferedImage("debris/FF_smalldebris_" + j + ".png");
		for (int j = 0; j < debris[0].length; j++)
			debrisShadows[13][j] = Drawable.createShadow(debris[12][j]);

		// Protective bubble
		for (int j = 0; j < 4; j++)
			arcForceFields[12][j] = ResourceLoader.getBufferedImage("forcefields/Protective_Bubble_" + j + ".png");

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 11; j++)
				cracks[i][j] = ResourceLoader.getBufferedImage("environment/cracks/cracks_" + i + "_" + j + ".png");
			cracks[i][11] = ResourceLoader.getBufferedImage("environment/cracks/cracks_" + i + "_wall.png");
		}

		// no need to transform it into BufferedImage.TYPE_INT_RGB
		floor[0] = ResourceLoader.getBufferedImage("environment/floor_Earth.png");

		// TODO additional forcefield types

		bodyPart.add(new ArrayList<List<List<BufferedImage>>>()); // legs
		bodyPart.add(new ArrayList<List<List<BufferedImage>>>()); // chest
		bodyPart.add(new ArrayList<List<List<BufferedImage>>>()); // head
		bodyPart.add(new ArrayList<List<List<BufferedImage>>>()); // hair

		for (int i = 0; i < 4; i++)
		{
			String bodyPartName = "null";
			int numOfPermutations = -1;
			switch (i)
			{
			case 0:
				bodyPartName = "legs";
				numOfPermutations = 2;
				break;
			case 1:
				bodyPartName = "chest";
				numOfPermutations = 2;
				break;
			case 2:
				bodyPartName = "head";
				numOfPermutations = 2;
				break;
			case 3:
				bodyPartName = "hair";
				numOfPermutations = 2;
				break;
			default:
				Main.errorMessage("Unknown body part number: " + i);
				break;
			}
			for (int j = 0; j < numOfPermutations; j++)
			{
				// Comment: Yes. this part is messy. And remember that it needs to be in combination with the Person.initAnimation part.
				bodyPart.get(i).add(new ArrayList<List<BufferedImage>>());
				if (i == 0 || i == 1) // if body part is legs or chest
				{
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // stand = 0
					bodyPart.get(i).get(j).get(0).add(ResourceLoader.getBufferedImage("people/stand-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // walk (run, actually) = 1
					for (int k = 1; k < 5; k++) // TODO make it from 0 to 3 in the file names, not from 1 to 4
						bodyPart.get(i).get(j).get(1).add(ResourceLoader.getBufferedImage("people/run_" + k + "-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // hold shield = 2
					bodyPart.get(i).get(j).get(2).add(ResourceLoader.getBufferedImage("people/hold_shield-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // slip = 3
					bodyPart.get(i).get(j).get(3).add(ResourceLoader.getBufferedImage("people/slip_0-" + bodyPartName + "_" + j + ".png"));
				} else if (i == 2 || i == 3) // heads and hairs have the same image for all frames in most animations, unlike legs and chest
				{
					// reusing images to save on copy-pasting on paint.net
					BufferedImage runImg = ResourceLoader.getBufferedImage("people/run-" + bodyPartName + "_" + j + ".png");
					BufferedImage standImg = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB); // = empty image
					Graphics2D stanly = standImg.createGraphics();
					stanly.drawImage(runImg, 0, -7, null); // 7 pixels above
					stanly.dispose();
					BufferedImage punchImg = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB); // = empty image
					stanly = punchImg.createGraphics();
					stanly.drawImage(runImg, 0, +4, null);
					stanly.dispose();
					BufferedImage slipImg = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB); // = empty image
					stanly = slipImg.createGraphics();
					stanly.drawImage(runImg, 0, -13, null);
					stanly.dispose();
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // 0 stand
					bodyPart.get(i).get(j).get(0).add(standImg);
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // 1 walk (run, actually)
					bodyPart.get(i).get(j).get(1).add(runImg);
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // 2 hold shield
					bodyPart.get(i).get(j).get(2).add(runImg);
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // 3 slip
					bodyPart.get(i).get(j).get(3).add(slipImg);
				}

				bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // 4 get up from slip
				bodyPart.get(i).get(j).get(4).add(ResourceLoader.getBufferedImage("people/slip_1-" + bodyPartName + "_" + j + ".png"));
				bodyPart.get(i).get(j).get(4).add(ResourceLoader.getBufferedImage("people/slip_2-" + bodyPartName + "_" + j + ".png"));
				bodyPart.get(i).get(j).get(4).add(ResourceLoader.getBufferedImage("people/slip_3-" + bodyPartName + "_" + j + ".png"));

				// punches:
				if (i == 1) // only chest+arms move differently during punches
				{
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // 5 right punch
					bodyPart.get(i).get(j).get(5).add(ResourceLoader.getBufferedImage("people/punch_1-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(5).add(ResourceLoader.getBufferedImage("people/punch_3-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // 6 left punch
					bodyPart.get(i).get(j).get(6).add(ResourceLoader.getBufferedImage("people/punch_2-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(6).add(ResourceLoader.getBufferedImage("people/punch_3-" + bodyPartName + "_" + j + ".png"));
				} else if (i == 0 || i == 2 || i == 3)
				{
					BufferedImage runImg = ResourceLoader.getBufferedImage("people/stand-" + bodyPartName + "_" + j + ".png");
					BufferedImage punchImg = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB); // = empty image
					Graphics2D punchy = punchImg.createGraphics();
					punchy.drawImage(runImg, 0, 11, null);
					punchy.dispose();
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // 5 right punch //maybe the opposite?)
					bodyPart.get(i).get(j).get(5).add(punchImg);
					bodyPart.get(i).get(j).get(5).add(punchImg);
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // 6 left punch //I think
					bodyPart.get(i).get(j).get(6).add(punchImg);
					bodyPart.get(i).get(j).get(6).add(punchImg);
				}
				// flight
				if (i < 3) // legs, chest, head
				{
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // 7 fly
					bodyPart.get(i).get(j).get(7).add(ResourceLoader.getBufferedImage("people/fly-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(7).add(ResourceLoader.getBufferedImage("people/fly-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(7).add(ResourceLoader.getBufferedImage("people/fly-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(7).add(ResourceLoader.getBufferedImage("people/fly-" + bodyPartName + "_" + j + ".png"));
				} else
				{
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // 7 fly
					bodyPart.get(i).get(j).get(7).add(ResourceLoader.getBufferedImage("people/fly_1-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(7).add(ResourceLoader.getBufferedImage("people/fly_2-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(7).add(ResourceLoader.getBufferedImage("people/fly_3-" + bodyPartName + "_" + j + ".png"));
				}
				// fly-hover and hover
				bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // 8 fly-hover
				bodyPart.get(i).get(j).get(8).add(ResourceLoader.getBufferedImage("people/fly_hover_transition-" + bodyPartName + "_" + j + ".png"));
				bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // 9 hover
				bodyPart.get(i).get(j).get(9).add(ResourceLoader.getBufferedImage("people/hover_1-" + bodyPartName + "_" + j + ".png"));
				// flight punch w/ arm held back
				if (i == 1) // chest and arms prepare for a strike
				{
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>());
					bodyPart.get(i).get(j).get(10).add(ResourceLoader.getBufferedImage("people/fly_punch_3-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(10).add(ResourceLoader.getBufferedImage("people/fly_punch_3-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(10).add(ResourceLoader.getBufferedImage("people/fly_punch_3-" + bodyPartName + "_" + j + ".png"));
				} else if (i == 3) // hair still has animation
				{
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // fly
					bodyPart.get(i).get(j).get(10).add(ResourceLoader.getBufferedImage("people/fly_1-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(10).add(ResourceLoader.getBufferedImage("people/fly_2-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(10).add(ResourceLoader.getBufferedImage("people/fly_3-" + bodyPartName + "_" + j + ".png"));
				} else
				{
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>());
					bodyPart.get(i).get(j).get(10).add(ResourceLoader.getBufferedImage("people/fly-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(10).add(ResourceLoader.getBufferedImage("people/fly-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(10).add(ResourceLoader.getBufferedImage("people/fly-" + bodyPartName + "_" + j + ".png"));
				}
				// 10 11 12 flight punch w/ arms
				if (i == 1) // chest and arms prepare for a strike
				{
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>());
					bodyPart.get(i).get(j).get(11).add(ResourceLoader.getBufferedImage("people/fly_punch_1-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>());
					bodyPart.get(i).get(j).get(12).add(ResourceLoader.getBufferedImage("people/fly_punch_2-" + bodyPartName + "_" + j + ".png"));
				} else if (i == 3) // hair still has animation
				{
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // fly
					bodyPart.get(i).get(j).get(11).add(ResourceLoader.getBufferedImage("people/fly_1-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(11).add(ResourceLoader.getBufferedImage("people/fly_2-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // fly
					bodyPart.get(i).get(j).get(12).add(ResourceLoader.getBufferedImage("people/fly_1-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(12).add(ResourceLoader.getBufferedImage("people/fly_2-" + bodyPartName + "_" + j + ".png"));
				} else
				{
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>());
					bodyPart.get(i).get(j).get(11).add(ResourceLoader.getBufferedImage("people/fly-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(11).add(ResourceLoader.getBufferedImage("people/fly-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>());
					bodyPart.get(i).get(j).get(12).add(ResourceLoader.getBufferedImage("people/fly-" + bodyPartName + "_" + j + ".png"));
					bodyPart.get(i).get(j).get(12).add(ResourceLoader.getBufferedImage("people/fly-" + bodyPartName + "_" + j + ".png"));
				}

				// dead
				bodyPart.get(i).get(j).add(new ArrayList<BufferedImage>()); // dead = 13
				bodyPart.get(i).get(j).get(13).add(ResourceLoader.getBufferedImage("people/dead_0-" + bodyPartName + "_" + j + ".png"));

				// TODO burning
			}
		}

		// Icons
		for (int i = 0; i < Ability.descriptions.size(); i++)
			switch (Ability.getName(Ability.descriptions.get(i))) // all non-default cases are for abilities with extra elements drawn on them
			{
			case "Sense Element":
				BufferedImage senseElement = ResourceLoader.getBufferedImage("icons/abilities/Sense Element.png");
				for (int j = 0; j < elementalNum; j++)
				{
					BufferedImage senseImage = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
					Graphics2D buffy = senseImage.createGraphics();
					buffy.drawImage(senseElement, 0, 0, null);
					buffy.drawImage(ResourceLoader.getBufferedImage("icons/elements/" + EP.elementList[j] + ".png"), 0, 0, null);
					buffy.dispose();
					icons.put(Ability.getName(Ability.descriptions.get(i)) + " <" + EP.elementList[j] + ">", senseImage);
				}
				break;
			default: // will add unecessary empty icons for stuff that must include an element. That stuff gets its icons somewhere else in the code (above this part I think)
				icons.put(Ability.getName(Ability.descriptions.get(i)), ResourceLoader.getBufferedImage("icons/abilities/" + Ability.getName(Ability.descriptions.get(i)) + ".png"));
				break;
			}

		// Effect icons:
		List<String> effectNames = Arrays.asList("Healed", "Burning"); // all effect names should be written here! TODO make sure it happens
		for (String s : effectNames)
			icons.put(s, ResourceLoader.getBufferedImage("icons/effects/" + s + ".png"));

		// Clouds
		for (int i = 0; i < 5; i++) // the number is the number of cloud images available
		{
			clouds.add(Methods.optimizeImage(ResourceLoader.getBufferedImage("clouds/cloud_" + i + ".png")));
			cloudShadows.add(Cloud.cloudShadow(clouds.get(i)));
		}

		// Effects
		effects.add(new ArrayList<BufferedImage>()); // Burning
		effects.get(0).add(ResourceLoader.getBufferedImage("people/burning_1.png"));
		effects.get(0).add(ResourceLoader.getBufferedImage("people/burning_2.png"));
		effects.get(0).add(ResourceLoader.getBufferedImage("people/burning_3.png"));

		for (int i = 0; i < 4; i++)
			healingBeam[i] = ResourceLoader.getBufferedImage("heal/heal" + "_" + i + ".png");

		explosions.add(new ArrayList<BufferedImage>());
		for (int i = 0; i < 8; i++)
			explosions.get(0).add(ResourceLoader.getBufferedImage("explosions/kexplosion" + "_0_" + i + ".png"));
		explosions.add(new ArrayList<BufferedImage>());
		for (int i = 0; i < 6; i++)
			explosions.get(1).add(ResourceLoader.getBufferedImage("explosions/explosion" + "_0_" + i + ".png"));
		explosions.add(new ArrayList<BufferedImage>());
		for (int i = 0; i < 19; i++)
			explosions.get(2).add(ResourceLoader.getBufferedImage("explosions/kexplosion" + "_1_" + i + ".png"));

		// SOUNDS ARE NOT HERE UNLESS THEY CAN'T BE PLAYED MULTIPLE TIMES SIMULTANEOUSLY
	}

	public static int getHealthImgNum(int health)
	{
		// Currently there are 2 states
		return 1 - (health - 1) / 50;
	}
}
