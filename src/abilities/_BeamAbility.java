package abilities;

import java.util.List;

import mainClasses.Ability;
import mainClasses.Evasion;

public class _BeamAbility extends Ability
{
	public double size;
	int beamFrameNum;
	public List<Evasion> evasions;
	public double criticalTimeLeft = 0;

	public _BeamAbility(String name, int p)
	{
		super(name, p);
		size = 1;
	}
}
