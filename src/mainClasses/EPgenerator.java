package mainClasses;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class EPgenerator
{
	static Random randomizer = new Random();
	static Scanner input = new Scanner(System.in);
	static int sum = 0;

	public static List<EP> generateEPs()
	{
		return convertToEPlist(generateElementList());
	}

	/*
	 * This method generates a list
	 */
	static int[] generateElementList()
	{
		int[] list = new int[10];

		int E1 = randomElement();
		int x = 4 + randomizer.nextInt(2); // 4-6 points for first element
		List<Integer> elements = new ArrayList<Integer>();
		elements.add(E1);
		int i = 0;
		for (; i < x; i++)
			list[i] = E1;
		while (i < 10)
		{
			int nextElement = -1 + randomizer.nextInt(elements.size() + 1); // -1 = new element
			if (nextElement == -1)
			{
				int newElement = randomElement(); // can sometimes not be a new element. Not a problem, I think.
				x = 1 + randomizer.nextInt(2); // 1-3 points for next elements
				if (!elements.contains(newElement)) // don't add the same element twice to the pool (low chance of that ever happening, but if it does happen it would make the next check of adding
													// elements to be more biased towards the repeating element)
					elements.add(newElement);
				for (int j = 0; j < x && i < 10; j++, i++)
					list[i] = newElement;
			} else
			{
				int element = elements.get(randomizer.nextInt(elements.size()));
				x = 1 + randomizer.nextInt(2); // 1-3 points for next element
				for (int j = 0; j < x && i < 10; j++, i++)
					list[i] = element;
			}
		}
		Arrays.sort(list);
		return list;
	}

	static List<EP> convertToEPlist(int[] DNA)
	{
		List<EP> EPs = new ArrayList<EP>();
		int x = 0;
		while (x < 10)
		{
			int e = DNA[x], n = 0;
			for (n = 0; x < 10 && DNA[x] == e; x++, n++)
				;
			EPs.add(new EP(e, n));
		}
		return EPs;
	}

	static List<Integer> FHRElist = new ArrayList<Integer>();

	static void initializeFHRE()
	{
		// create a list where "1" (fire) occurs 4 times, "13" (strong) occurs 10 times, etc.
		// List length is supposed to always be 150, currently
		for (int i = 0; i < elementChances.length; i++)
			for (int j = 0; j < elementChances[i]; j++)
			{
				FHRElist.add(i);
				sum += 1;
			}
	}

	static int randomElement()
	{
		return fastHeavyRandomElement();
	}

	static int fastHeavyRandomElement()
	{
		// This method uses more memory, but is faster
		return FHRElist.get(randomizer.nextInt(sum));
	}

	static int[] elementChances =
	{ 4, 4, 4, 4, 3, 4, 4, 2, 2, 2, 4, 4, 5, 10, 6, 9, 5, 9, 4, 5, 4, 6, 5, 3, 3, 3, 5, 5, 4, 4, 5, 7, 5 };

	/*
	 * Fire 4 Water 4 Wind 4 Electricity 4 Metal 3 Ice 4 Energy 4 Acid 2 Lava 2 Flesh 2 Earth 4 Plant 4 Sense 5 Strong 10 Regenerate 6 Flight 9 Dexterity 5 Armor 9 Movement 4 Teleport 5 Ghost 4 Force
	 * Field 6 Time 5 Loop 3 Power 3 Steal 3 Illusion 5 Summon 5 Explosion 4 Control 4 Reshape 5 Buff 7 Charge 5
	 */
	static void printElementChances()
	{
		for (int i = 0; i < elementChances.length; i++)
			System.out.println(EP.elementList[i] + ", " + elementChances[i]);
	}
}
