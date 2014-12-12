package net.mcshockwave.Hub.Kit.Paintball;

import net.mcshockwave.Hub.HubPlugin;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public abstract class PBObjective {

	public static final DyeColor			GREEN_COLOR	= DyeColor.GREEN, YELLOW_COLOR = DyeColor.YELLOW,
			NEUTRAL_COLOR = DyeColor.WHITE;

	public static ArrayList<PBObjective>	objs		= new ArrayList<>();

	public static ControlPoint cp1(Paintball game) {
		if (getFromIdAndUUID(1, game.gameUUID.toString()) != null) {
			return (ControlPoint) getFromIdAndUUID(1, game.gameUUID.toString());
		}
		ControlPoint cp = new ControlPoint(game, new Vector(-528, 87, -533), new Vector(-520, 82, -542), 1);
		objs.add(cp);
		return cp;
	}

	public static ControlPoint cp2(Paintball game) {
		if (getFromIdAndUUID(2, game.gameUUID.toString()) != null) {
			return (ControlPoint) getFromIdAndUUID(2, game.gameUUID.toString());
		}
		ControlPoint cp = new ControlPoint(game, new Vector(-486, 69, -506), new Vector(-495, 64, -495), 2);
		objs.add(cp);
		return cp;
	}

	public static ControlPoint cp3(Paintball game) {
		if (getFromIdAndUUID(3, game.gameUUID.toString()) != null) {
			return (ControlPoint) getFromIdAndUUID(3, game.gameUUID.toString());
		}
		ControlPoint cp = new ControlPoint(game, new Vector(-460, 87, -460), new Vector(-450, 82, -468), 3);
		objs.add(cp);
		return cp;
	}

	public static PBObjective getFromIdAndUUID(int id, String uuid) {
		for (PBObjective obj : objs) {
			if (obj.game.gameUUID.toString().equals(uuid) && obj.id == id) {
				return obj;
			}
		}
		return null;
	}

	boolean				active;
	int					id;
	public Paintball	game;

	PBObjective(int id, Paintball game) {
		this.id = id;
		this.game = game;
	}

	public static class ControlPoint extends PBObjective {

		public static final int	CP_TIMER_MAX	= 20;

		Location				c1, c2;

		public DyeColor			teamOwn;
		public int				timer			= 0;

		public String			identifier		= "";

		public boolean			isBeingTaken	= false;

		public ControlPoint(Paintball game, Vector c1, Vector c2, int id) {
			super(id, game);
			this.c1 = new Location(HubPlugin.endWorld(), c1.getX(), c1.getY(), c1.getZ());
			this.c2 = new Location(HubPlugin.endWorld(), c2.getX(), c2.getY(), c2.getZ());
			setActive(false);
		}

		public boolean isInPoint(Location l) {
			if (!l.getWorld().getName().equals(c1.getWorld().getName())) {
				return false;
			}

			double mx = Math.min(c1.getX(), c2.getX());
			double my = Math.min(c1.getY(), c2.getY());
			double mz = Math.min(c1.getZ(), c2.getZ());
			double lx = Math.max(c1.getX(), c2.getX());
			double ly = Math.max(c1.getY(), c2.getY());
			double lz = Math.max(c1.getZ(), c2.getZ());

			boolean xb = l.getX() >= mx && l.getX() <= lx;
			boolean yb = l.getY() >= my && l.getY() <= ly;
			boolean zb = l.getZ() >= mz && l.getZ() <= lz;

			return xb && yb && zb;
		}

		// TODO add support for multiple games at once
		public void setActive(boolean active) {
			if (active) {
				getBeaconPoint(id).setType(Material.STAINED_GLASS);
			} else {
				getBeaconPoint(id).setType(Material.DIAMOND_BLOCK);
			}
			this.active = active;
		}

		@SuppressWarnings("deprecation")
		public void setColor(DyeColor color, boolean set) {
			if (active) {
				getBeaconPoint(id).setData(color.getData());
				if (set) {
					teamOwn = color;
					timer = teamOwn == GREEN_COLOR ? 20 : teamOwn == YELLOW_COLOR ? -20 : 0;
					isBeingTaken = false;
				}
			}
		}
	}

	/**
	 * @param p
	 *            objective id (1, 2, 3)
	 * 
	 * @return block to change the color of beacon
	 */
	public static Block getBeaconPoint(int id) {
		Vector v = null;
		switch (id) {
			case 1:
				v = new Vector(-525, 82, -536);
				break;
			case 2:
				v = new Vector(-493, 64, -497);
				break;
			case 3:
				v = new Vector(-454, 82, -465);
				break;
			default:
				return null;
		}
		return HubPlugin.endWorld().getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ());
	}

	public abstract void setActive(boolean active);

	public boolean getActive() {
		return active;
	}

}
