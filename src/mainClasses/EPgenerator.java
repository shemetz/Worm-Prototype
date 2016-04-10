package mainClasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Generates a list of EPs with a sum of 10 points, also known as the "DNA" or a parahuman. This list is later used to generate powers.
 * 
 * @author Itamar
 * @see PowerGenerator
 */
public class EPgenerator
{
	static Random randomizer = new Random();
	static int sum = 0;

	/**
	 * 
	 * @return List of a number of EPs with a sum of 10 points, e.g: Fire 3, Movement 6, Charge 1.
	 * @see EPgenerator#generateElementList()
	 */
	public static List<EP> generateEPs()
	{
		return convertToEPlist(generateElementList());
	}

	/**
	 * Generates an element list. How this method works is as follows:
	 * <p>
	 * Pick an element, call it E1. This element will be added 4-5 times to the array. Then, as long as the array isn't full, add 1-2 points of either one of the existing elements or a new element (chance of adding a new element is 1 in X, where X is
	 * the amount of elements already added plus 1.)
	 * 
	 * 
	 * 
	 * @return unsorted array that contains 10 element numbers. e.g: 6, 6, 22, 8, 22, 6, 22, 22, 22, 8
	 */
	static int[] generateElementList()
	{
		int[] list = new int[10];

		int E1 = randomElement();
		int x = 4 + randomizer.nextInt(2); // 4-5 points for first element
		List<Integer> elements = new ArrayList<Integer>();
		elements.add(E1);
		int i = 0;
		for (; i < x; i++)
			list[i] = E1;
		while (i < 10)
		{
			int nextElement = 0 + randomizer.nextInt(elements.size() + 1);
			if (nextElement == 0) // add another element
			{
				int newElement = randomElement();
				x = 1 + randomizer.nextInt(2); // 1-2 points
				if (!elements.contains(newElement)) // don't add the same element twice to the elements pool while adding points to it (low chance of that ever happening, but if it does happen it would make the next check of adding
													// elements to be more biased towards the repeating element)
					elements.add(newElement);
				for (int j = 0; j < x && i < 10; j++, i++)
					list[i] = newElement;
			}
			else // add points to an existing element
			{
				int element = elements.get(randomizer.nextInt(elements.size()));
				x = 1 + randomizer.nextInt(2); // 1-2 points
				for (int j = 0; j < x && i < 10; j++, i++)
					list[i] = element;
			}
		}
		Arrays.sort(list);
		return list;
	}

	/**
	 * Converts an unsorted integer list, e.g. "6,7,5,5,5,6,6,5,7,23" into a sorted EP list, e.g. "Ice 4, Energy 3, Acid 2, Loop 1"
	 * 
	 * @param DNA
	 *            integer list with length of 10
	 * @return list of EPs
	 */
	static List<EP> convertToEPlist(int[] DNA)
	{
		int length = 10;
		List<EP> EPs = new ArrayList<EP>();
		int x = 0;
		while (x < length)
		{
			int e = DNA[x], n = 0;
			for (; x < length && DNA[x] == e; x++, n++)
				;
			EPs.add(new EP(e, n));
		}
		return EPs;
	}

	static List<Integer> elementWeightedList = new ArrayList<Integer>();

	/**
	 * Initializes the {@link #elementWeightedList} variable
	 */
	static void initializeElementWeightedList()
	{
		// create a list where "1" (fire) occurs 4 times, "13" (strong) occurs 10 times, etc.
		// List length is supposed to always be 150, currently
		for (int i = 0; i < elementChances.length; i++)
			for (int j = 0; j < elementChances[i]; j++)
			{
				elementWeightedList.add(i);
				sum += 1;
			}
	}

	/**
	 * Returns a "random" element, from the {@link #elementWeightedList}.
	 * 
	 * @return number of element
	 */
	static int randomElement()
	{
		return elementWeightedList.get(randomizer.nextInt(sum));
	}

	/**
	 * Hardcoded element weights values
	 */
	static int[] elementChances =
	{ 4, 4, 4, 4, 3, 4, 4, 2, 2, 2, 4, 4, 5, 10, 6, 9, 5, 9, 4, 5, 4, 6, 5, 3, 3, 3, 5, 5, 4, 4, 7, 5 };

	/*
	 * Fire 4 Water 4 Wind 4 Electricity 4 Metal 3 Ice 4 Energy 4 Acid 2 Lava 2 Flesh 2 Earth 4 Plant 4 Sense 5 Strong 10 Regenerate 6 Flight 9 Dexterity 5 Armor 9 Movement 4 Teleport 5 Ghost 4 Force Field 6 Time 5 Loop 3 Power 3 Steal 3 Illusion 5
	 * Summon 5 Explosion 4 Control 4 Buff 7 Charge 5
	 */
	/**
	 * Prints the {@link #elementChances} list nicely
	 */
	static void printElementChances()
	{
		for (int i = 0; i < elementChances.length; i++)
			System.out.println(EP.elementList[i] + ", " + elementChances[i]);
	}
}
