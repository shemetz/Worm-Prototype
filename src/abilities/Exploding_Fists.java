package abilities;

public class Exploding_Fists extends _PunchAbility
{

	public Exploding_Fists(int p)
	{
		super("Exploding Fists", p);

		damage = level * 4;
		pushback = level * 4;
	}
}
