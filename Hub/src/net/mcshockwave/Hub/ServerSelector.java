package net.mcshockwave.Hub;

import org.bukkit.entity.Villager.Profession;

import net.minecraft.server.v1_7_R2.EntityInsentient;
import net.minecraft.server.v1_7_R2.EntityVillager;
import net.minecraft.server.v1_7_R2.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_7_R2.World;

public class ServerSelector extends EntityVillager {

	public int	x	= 0, y = 0, z = 0;

	public ServerSelector(World world) {
		this(world, 0, 0, 0);
	}

	public ServerSelector(World world, int x, int y, int z) {
		this(world, Profession.BUTCHER, x, y, z);
	}

	@SuppressWarnings("deprecation")
	public ServerSelector(World world, Profession p, int x, int y, int z) {
		super(world);
		this.setProfession(p.getId());
		this.a(0.6F, 1.8F);
		this.getNavigation().b(true);
		this.getNavigation().a(true);
		// this.goalSelector.a(0, new PathfinderGoalFloat(this));
		// this.goalSelector.a(1, new PathfinderGoalAvoidPlayer(this,
		// EntityZombie.class, 8.0F, 0.6D, 0.6D));
		// this.goalSelector.a(1, new PathfinderGoalTradeWithPlayer(this));
		// this.goalSelector.a(1, new PathfinderGoalLookAtTradingPlayer(this));
		// this.goalSelector.a(2, new PathfinderGoalMoveIndoors(this));
		// this.goalSelector.a(3, new PathfinderGoalRestrictOpenDoor(this));
		// this.goalSelector.a(4, new PathfinderGoalOpenDoor(this, true));
		// this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this,
		// 0.6D));
		// this.goalSelector.a(6, new PathfinderGoalMakeLove(this));
		// this.goalSelector.a(7, new PathfinderGoalTakeFlower(this));
		// this.goalSelector.a(8, new PathfinderGoalPlay(this, 0.32D));
		// this.goalSelector.a(9, new PathfinderGoalInteract(this,
		// EntityHuman.class, 3.0F, 1.0F));
		// this.goalSelector.a(9, new PathfinderGoalInteract(this,
		// EntityVillager.class, 5.0F, 0.02F));
		this.goalSelector.a(0, new PathfinderGoalGoToBlock(this, 0.75f, x, y, z));
		this.goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
	}

}
