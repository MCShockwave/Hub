package net.mcshockwave.Hub;

import net.minecraft.server.v1_7_R4.EntityCreature;
import net.minecraft.server.v1_7_R4.PathfinderGoal;
import net.minecraft.server.v1_7_R4.Vec3D;

public class PathfinderGoalGoToBlock extends PathfinderGoal {

	private EntityCreature	a;
	private double			b;
	private double			c;
	private double			d;
	private double			e;
	public double			x, y, z;

	public PathfinderGoalGoToBlock(EntityCreature entitycreature, double s, double x, double y, double z) {
		this.a = entitycreature;
		this.e = s;
		this.a(1);
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public boolean a() {
		Vec3D vec3d = Vec3D.a(x, y, z);

		if (vec3d == null) {
			return false;
		} else {
			this.b = vec3d.a;
			this.c = vec3d.b;
			this.d = vec3d.c;
			return true;
		}
	}

	public boolean b() {
		return !this.a.getNavigation().g();
	}

	public void c() {
		this.a.getNavigation().a(this.b, this.c, this.d, this.e);
	}
}
