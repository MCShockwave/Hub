package net.mcshockwave.Hub;

import net.minecraft.server.v1_7_R1.EntityCreature;
import net.minecraft.server.v1_7_R1.PathfinderGoal;
import net.minecraft.server.v1_7_R1.Vec3D;

public class PathfinderGoalGoToBlock extends PathfinderGoal {

	private EntityCreature	a;
	private double			b;
	private double			c;
	private double			d;
	private double			e;
	private double			x, y, z;

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
			this.b = vec3d.c;
			this.c = vec3d.d;
			this.d = vec3d.e;
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
