package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.VisualEffect;

public class ApplyEffect extends Ability{
	public enum targetTypes {SELF, OTHER, TARGETED};
	targetTypes target;
	Effect effect;
	int visualType;
	
	public ApplyEffect(String name, int p, Effect effect1, targetTypes targetType1, int visual){
		super(name, p);
		effect = effect1;
		target = targetType1;
		visualType = visual;
	}
	
	public Person getTarget(Environment env, Person user, double deltaTime){
		Person effectTarget = user;
		switch(target){
		case OTHER:
			double shortestDistancePow2 = range * range;
			for (Person p : env.people)
				if (p != user)
				{
					double distancePow2 = Methods.DistancePow2(user.x, user.y, p.x, p.y);
					if (distancePow2 < shortestDistancePow2)
					{
						shortestDistancePow2 = distancePow2;
						if (targetEffect1 * targetEffect1 >= distancePow2)
							targetEffect1 -= 1 * points * deltaTime * (range + 20 - Math.sqrt(range - targetEffect1) - targetEffect1);
						effectTarget = p;
					}
				}
			break;
		}
			return effectTarget;
	}
	
	public void draw(Environment env, double deltaTime, Person user, Person effectTarget){
		if (effectTarget != user)
		{
			VisualEffect visual = new VisualEffect();
			visual.timeLeft = deltaTime * 2;
			visual.frame = frameNum;
			visual.p1 = new Point((int) user.x, (int) user.y);
			visual.p2 = new Point((int) effectTarget.x, (int) effectTarget.y);
			visual.type = visualType;
			visual.angle = Math.atan2(effectTarget.y - user.y, effectTarget.x - user.x);

			env.effects.add(visual);
		}
	}
	
	public void maintain (Environment env, Person user, Point target, double deltaTime){
		{
			if (costPerSecond * deltaTime <= user.mana)
			{
				if (frameNum % 6 == 0)
					frameNum++;
				if (frameNum >= 4)
					frameNum = 0;

				double shortestDistancePow2 = range * range;
				Person effectTarget = getTarget(env, user, deltaTime);
				effect.apply(effectTarget);
				draw(env, deltaTime, user, effectTarget);
				user.mana -= deltaTime * costPerSecond;
			}
		}
	}
}
