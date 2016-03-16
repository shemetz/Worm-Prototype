package mainClasses;

import abilities._AFFAbility;
import abilities._BeamAbility;
import abilities._FlightAbility;
import abilities._ForceFieldAbility;
import abilities._ProjectileAbility;
import abilities._SummoningAbility;
import abilities._TeleportAbility;

public class Perk
{
	public String name;

	public Perk(String name_)
	{
		name = name_;
	}

	public void toggle(Ability a, boolean addOrRemove)
	{
		int k = addOrRemove ? 1 : -1;
		switch (name)
		{
		case "Increased damage":
			a.damage *= Math.pow(1.2, k);
			break;
		case "Increased pushback":
			a.pushback *= Math.pow(1.2, k);
			break;
		case "Faster flight":
			((_FlightAbility) a).flySpeed *= Math.pow(1.15, k);
			break;
		case "Longer teleport":
			a.range *= Math.pow(1.25, k);
			break;
		case "Telefrag":
			((_TeleportAbility) a).telefragging = addOrRemove;
			break;
		case "Wider force fields":
			((_ForceFieldAbility) a).length *= Math.pow(1.2, k);
			break;
		case "Thicker force fields":
			((_ForceFieldAbility) a).width *= Math.pow(1.15, k);
			((_ForceFieldAbility) a).life *= Math.pow(1.15, k);
			break;
		case "Stable force fields":
			((_ForceFieldAbility) a).decayRate *= Math.pow(0.7, k);
			break;
		case "Armored force fields":
			((_ForceFieldAbility) a).armor += 2 * k;
			break;
		case "Faster projectile":
			((_ProjectileAbility) a).velocity *= Math.pow(1.2, k);
			break;
		case "Bigger projectile":
			((_ProjectileAbility) a).size *= Math.pow(1.2, k);
			break;
		case "Increased beam range":
			((_BeamAbility) a).range *= Math.pow(1.5, k);
			break;
		case "Increased beam size":
			((_BeamAbility) a).size *= Math.pow(1.2, k);
			break;
		case "Extra durable":
			((_AFFAbility) a).life *= Math.pow(1.25, k);
			break;
		case "Increased explosion size":
			a.radius *= Math.pow(1.15, k);
			break;
		case "Improved armor":
			// TODO
			break;
		case "Reduced cost":
			a.cost *= Math.pow(0.7, k);
			break;
		case "Reduced continuous cost":
			a.costPerSecond *= Math.pow(0.7, k);
			break;
		case "Reduced cooldown":
			a.cooldown *= Math.pow(0.7, k);
			break;
		case "Increased duration":
			a.duration *= Math.pow(1.2, k);
			break;
		case "Increased range":
			a.range *= Math.pow(1.2, k);
			break;
		case "Increased charge rate":
			a.chargeRate *= Math.pow(1.2, k);
			break;
		case "Increased absorption":
			a.steal *= Math.pow(1.2, k);
			break;
		case "Empowered":
			a.level += k;
			break;
		case "Better summons":
			((_SummoningAbility) a).statMultiplier *= Math.pow(1.2, k);
			break;
		case "Durable summons":
			((_SummoningAbility) a).life *= Math.pow(1.25, k);
			break;
		case "More summons":
			((_SummoningAbility) a).maxNumOfClones *= Math.pow(1.5, k); // maybe won't work with some stuff
			break;
		case "Better summon":
			((_SummoningAbility) a).statMultiplier *= Math.pow(1.25, k);
			break;
		default:
			MAIN.errorMessage("no such perk!   " + name);
			break;
		}
	}
}
