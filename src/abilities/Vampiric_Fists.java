package abilities;

public class Vampiric_Fists extends _PunchAbility
{

	public Vampiric_Fists(int p)
	{
		super("Vampiric Fists", p);
		
		steal = 0.2*level;
	}
}
