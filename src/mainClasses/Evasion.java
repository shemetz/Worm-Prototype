package mainClasses;

public class Evasion
{
	public int		id;			// ID of person who evaded
	public double	timeLeft;	// time left until evasion is not automatic anymore

	public Evasion(int id1)
	{
		id = id1;
		timeLeft = 0.5; // TODO maybe not
	}
}
