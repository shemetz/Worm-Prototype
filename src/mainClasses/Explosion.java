package mainClasses;

public class Explosion
{
	int x, y;
	double z;
	double timeLeft;
	int type;
	int subExplosionsLeft;

	public Explosion(double x1, double y1, double z1, double timeLeft1, int type1)
	{
		x = (int) x1;
		y = (int) y1;
		z = z1;
		timeLeft = timeLeft1;
		type = type1;
		subExplosionsLeft = 7;
	}

	public void update(Environment env, double deltaTime)
	{
		if (subExplosionsLeft > 0 && Math.random() < 0.5)
		{
			subExplosionsLeft--;
			double dist = Math.random() * 60;
			double angle = Math.random() * 2 * Math.PI;
			int subtype = (int) (Math.random() * 3);
			createSubExplosion(env, (int) (x + dist * Math.cos(angle)), (int) (y + dist * Math.sin(angle)), subtype);
		}
		timeLeft -= deltaTime;
	}

	public void createSubExplosion(Environment env, int x1, int y1, int subtype)
	{
		VisualEffect explosionEffect = new VisualEffect();
		explosionEffect.p1.x = x1;
		explosionEffect.p1.y = y1;
		explosionEffect.z = z;
		explosionEffect.type = VisualEffect.Type.EXPLOSION;
		explosionEffect.subtype = subtype;
		explosionEffect.angle = Math.random() * 2 * Math.PI;
		explosionEffect.timeLeft = Resources.explosions.get(explosionEffect.subtype).size() * 0.02 * 3; // deltaTime = 0.02 right? so 3 in game frames per 1 image frame
		explosionEffect.p2.x = (int) (0.5 * Resources.explosions.get(explosionEffect.subtype).get(0).getWidth());
		explosionEffect.p2.y = (int) (0.5 * Resources.explosions.get(explosionEffect.subtype).get(0).getHeight());
		env.visualEffects.add(explosionEffect);
	}
}
