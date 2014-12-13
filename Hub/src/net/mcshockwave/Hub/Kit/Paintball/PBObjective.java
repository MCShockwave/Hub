package net.mcshockwave.Hub.Kit.Paintball;

import net.mcshockwave.Hub.HubPlugin;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

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

	public static Cache ca1(Paintball game) {
		if (getFromIdAndUUID(-1, game.gameUUID.toString()) != null) {
			return (Cache) getFromIdAndUUID(-1, game.gameUUID.toString());
		}
		Cache cp = new Cache(-1, game, new Vector(-459, 83, -521));
		objs.add(cp);
		return cp;
	}

	public static Cache ca2(Paintball game) {
		if (getFromIdAndUUID(-2, game.gameUUID.toString()) != null) {
			return (Cache) getFromIdAndUUID(-2, game.gameUUID.toString());
		}
		Cache cp = new Cache(-2, game, new Vector(-526, 83, -464));
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
	int					id, number = -1;
	public Paintball	game;
	public String		identifier	= "";

	public DyeColor		teamOwn;

	public Objective	indicatorObjective;
	public Score		indicatorScore;

	PBObjective(int id, Paintball game) {
		this.id = id;
		this.game = game;

		indicatorObjective = game.sidebar;
		indicatorScore = null;
	}

	public static class ControlPoint extends PBObjective {

		public static final int	CP_TIMER_MAX	= 15;

		Location				c1, c2;

		public int				timer			= 0;

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
			this.active = active;
			if (active) {
				getBeaconPoint(id).setType(Material.STAINED_GLASS);
				if (teamOwn != null) {
					setColor(teamOwn, true);
				}
			} else {
				getBeaconPoint(id).setType(Material.DIAMOND_BLOCK);
				this.timer = teamOwn == GREEN_COLOR ? CP_TIMER_MAX - 1 : teamOwn == YELLOW_COLOR ? -CP_TIMER_MAX + 1 : 0;
			}
			updateObjectiveInfo(active ? teamOwn : null, number);
		}

		@SuppressWarnings("deprecation")
		public void setColor(DyeColor color, boolean set) {
			if (active) {
				if (color != null) {
					getBeaconPoint(id).setData(color.getData());
				}
				if (set) {
					teamOwn = color;
					timer = teamOwn == GREEN_COLOR ? 20 : teamOwn == YELLOW_COLOR ? -20 : 0;
					isBeingTaken = false;
					if (number != -1) {
						updateObjectiveInfo(teamOwn, number);
					}
				}
			}
		}
	}

	public static class Cache extends PBObjective {

		Vector	location;

		public Cache(int id, Paintball game, Vector location) {
			super(id, game);
			this.location = location;
		}

		public Location getLocation() {
			return new Location(HubPlugin.endWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
		}

		@Override
		public void setActive(boolean active) {
			if (!this.active && active) {
				placeCache(getLocation());
			} else if (this.active && !active) {
				for (int x = -1; x <= 1; x++) {
					for (int y = 0; y <= 1; y++) {
						for (int z = -1; z <= 1; z++) {
							getLocation().add(x, y, z).getBlock().setType(Material.AIR);
						}
					}
				}
			}
			updateObjectiveInfo(active ? teamOwn : null, number);
			this.active = active;
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

	// copy-paste from stc
	public void placeCache(Location loc) {
		Random rand = new Random();
		// 1: chest, 2: trap chest, 3: chest / coal block 4: trap chest / coal
		// block 5: chest / chest 6: trap chest / chest 7: chest / trap chest 8:
		// trap chest / trap chest
		int sizeX = 3, sizeZ = 3;
		int[][] layout = new int[3][3];
		for (int i = 0; i < layout.length; i++) {
			for (int j = 0; j < layout[i].length; j++) {
				layout[i][j] = rand.nextInt(8) + 1;
			}
		}

		Location start = loc.clone().add(-1, 0, -1);
		for (int dx = 0; dx < sizeX; dx++) {
			for (int dz = 0; dz < sizeZ; dz++) {
				Block c = start.clone().add(dx, 0, dz).getBlock();
				Block cu = c.getLocation().clone().add(0, 1, 0).getBlock();
				int place = layout[dx][dz];

				switch (place) {
					case 1:
						c.setType(Material.CHEST);
						break;
					case 2:
						c.setType(Material.TRAPPED_CHEST);
						break;
					case 3:
						c.setType(Material.COAL_BLOCK);
						cu.setType(Material.CHEST);
						break;
					case 4:
						c.setType(Material.COAL_BLOCK);
						cu.setType(Material.TRAPPED_CHEST);
						break;
					case 5:
						c.setType(Material.CHEST);
						cu.setType(Material.CHEST);
						break;
					case 6:
						c.setType(Material.CHEST);
						cu.setType(Material.TRAPPED_CHEST);
						break;
					case 7:
						c.setType(Material.TRAPPED_CHEST);
						cu.setType(Material.CHEST);
						break;
					case 8:
						c.setType(Material.TRAPPED_CHEST);
						cu.setType(Material.CHEST);
						break;
					default:
						break;
				}
			}
		}
	}

	public abstract void setActive(boolean active);

	public boolean getActive() {
		return active;
	}

	public static ChatColor getColor(DyeColor color) {
		if (color == GREEN_COLOR) {
			return ChatColor.DARK_GREEN;
		} else if (color == YELLOW_COLOR) {
			return ChatColor.YELLOW;
		} else if (color == NEUTRAL_COLOR) {
			return ChatColor.WHITE;
		} else {
			return ChatColor.GRAY;
		}
	}

	public void updateObjectiveInfo(DyeColor color, int id) {
		if (indicatorObjective == null) {
			return;
		}
		if (indicatorScore != null) {
			indicatorScore.getObjective().getScoreboard().resetScores(indicatorScore.getEntry());
		}
		indicatorScore = indicatorObjective.getScore(getColor(color) + identifier);
		indicatorScore.setScore(-id - 1);
	}

}
