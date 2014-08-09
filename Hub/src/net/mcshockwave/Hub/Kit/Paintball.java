package net.mcshockwave.Hub.Kit;

import static net.mcshockwave.Hub.Kit.Paintball.GameState.NONE;
import static net.mcshockwave.Hub.Kit.Paintball.GameState.QUEUED;
import static net.mcshockwave.Hub.Kit.Paintball.GameState.STARTED;
import net.mcshockwave.Guns.Gun;
import net.mcshockwave.Guns.addons.Addon;
import net.mcshockwave.Hub.DefaultListener;
import net.mcshockwave.Hub.HubPlugin;
import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Paintball implements Listener {

	public static String				cmdQueue	= "/queue";

	private static ArrayList<Paintball>	games		= new ArrayList<>();

	static Random						rand		= new Random();

	public static int					defaultY	= 101;

	public ArrayList<BukkitTask>		tasks		= new ArrayList<>();

	public static Paintball getActiveQueue() {
		for (Paintball pg : games) {
			if (pg.state == QUEUED) {
				return pg;
			}
		}
		return null;
	}

	private Paintball() {
	}

	public ArrayList<String>	players	= new ArrayList<>(), green = new ArrayList<>(), yellow = new ArrayList<>();

	public static Paintball newGame(Minigame game, int maxPlayers) {
		Paintball pg = new Paintball();
		pg.current = game;
		pg.state = NONE;
		pg.maxPlayers = maxPlayers;

		games.add(pg);
		return pg;
	}

	public GameState	state		= null;

	public Minigame		current		= null;

	public int			maxPlayers	= 0;

	public boolean		autoStart	= false;

	public void queue(boolean autoStart) {
		MCShockwave
				.broadcastAll(MCShockwave.getBroadcastMessage(ChatColor.DARK_GREEN,
						"A new game of %s has been queued!", "paintball"), MCShockwave.getBroadcastMessage(
						ChatColor.RED, "Gamemode: %s   Max Players: %s", current.toString(), maxPlayers), MCShockwave
						.getBroadcastMessage(ChatColor.DARK_AQUA, "Type %s to join!", cmdQueue));
		this.autoStart = autoStart;
		state = QUEUED;
	}

	public void addToQueue(String pl) {
		if (players.contains(pl)) {
			return;
		}

		send("§8" + pl + " §7has joined the game queue (" + (players.size() + 1) + "/" + maxPlayers + ")");

		players.add(pl);
		if (Bukkit.getPlayer(pl) != null) {
			Player p = Bukkit.getPlayer(pl);

			MCShockwave.send(ChatColor.RED, p, "You have been queued up for paintball (%s)", current.toString());
		}
		if (autoStart && players.size() >= maxPlayers) {
			start();
		}
	}

	public void start() {
		state = STARTED;

		send("§6§lGame started!");

		ArrayList<Player> randomized = getPlayers();
		Collections.shuffle(getPlayers(), rand);
		for (Player p : randomized) {
			if (green.size() > yellow.size()) {
				yellow.add(p.getName());
			} else if (yellow.size() > green.size()) {
				green.add(p.getName());
			} else {
				if (rand.nextBoolean()) {
					yellow.add(p.getName());
				} else {
					green.add(p.getName());
				}
			}
		}

		send("§a§lGreen Team:\n");
		for (Player p : getPlayers(green)) {
			send("§7§o" + p.getName());
		}

		send("§e§lYellow Team:\n");
		for (Player p : getPlayers(yellow)) {
			send("§7§o" + p.getName());
		}

		for (Player p : getPlayers()) {
			respawn(p);
		}

		Bukkit.getPluginManager().registerEvents(this, HubPlugin.ins);

		if (current == Minigame.Capture_the_Flag) {
			tasks.add(new BukkitRunnable() {
				public void run() {
					HubPlugin
							.endWorld()
							.dropItem(
									getGreenSpawn(defaultY),
									ItemMetaUtils.setItemName(new ItemStack(Material.WOOL, 1, (short) 5),
											"§aGreen Wool")).setVelocity(new Vector());
					HubPlugin
							.endWorld()
							.dropItem(
									getGreenSpawn(defaultY),
									ItemMetaUtils.setItemName(new ItemStack(Material.WOOL, 1, (short) 4),
											"§eYellow Wool")).setVelocity(new Vector());
				}
			}.runTaskLater(HubPlugin.ins, 200));

			tasks.add(new BukkitRunnable() {
				public void run() {
					for (Player p : getPlayers(green)) {
						if (p.getInventory().contains(Material.WOOL)) {
							PacketUtils.playBlockDustParticles(Material.WOOL, 5, p.getEyeLocation(), 0.3f, 0.1f);
							if (getGreenSpawn(defaultY).distanceSquared(p.getLocation()) < 3 * 3) {
								MCShockwave.broadcast(ChatColor.GREEN, "%s has won a game of paintball!", "Green");
							}
						}
					}
					for (Player p : getPlayers(yellow)) {
						if (p.getInventory().contains(Material.WOOL)) {
							PacketUtils.playBlockDustParticles(Material.WOOL, 4, p.getEyeLocation(), 0.3f, 0.1f);
							if (getYellowSpawn(defaultY).distanceSquared(p.getLocation()) < 3 * 3) {
								MCShockwave.broadcast(ChatColor.YELLOW, "%s has won a game of paintball!", "Yellow");
							}
						}
					}
				}
			}.runTaskTimer(HubPlugin.ins, 10, 10));
		}

		if (current == Minigame.Grifball) {
			tasks.add(new BukkitRunnable() {
				public void run() {
					HubPlugin
							.endWorld()
							.dropItem(getCenter(),
									ItemMetaUtils.setItemName(new ItemStack(Material.TNT), "§cGrif§6ball"))
							.setVelocity(new Vector());
				}
			}.runTaskLater(HubPlugin.ins, 200));

			tasks.add(new BukkitRunnable() {
				public void run() {
					for (Entity e : HubPlugin.endWorld().getEntities()) {
						if (e instanceof Item) {
							Item i = (Item) e;

							if (i.getItemStack().getType() == Material.TNT) {
								if (getYellowSpawn(defaultY).distanceSquared(i.getLocation()) < 3 * 3) {
									MCShockwave.broadcast(ChatColor.GREEN, "%s has won a game of paintball!", "Green");
									for (Player p : getPlayers()) {
										p.playSound(i.getLocation(), Sound.EXPLODE, 10, 0);
									}
									i.remove();
									end();
									return;
								}

								if (getGreenSpawn(defaultY).distanceSquared(i.getLocation()) < 3 * 3) {
									MCShockwave
											.broadcast(ChatColor.YELLOW, "%s has won a game of paintball!", "Yellow");
									for (Player p : getPlayers()) {
										p.playSound(i.getLocation(), Sound.EXPLODE, 10, 0);
									}
									i.remove();
									end();
									return;
								}
							}
						}
					}

					for (Player p : getPlayers()) {
						if (p.getInventory().contains(Material.TNT)) {
							PacketUtils.playBlockDustParticles(Material.TNT, 0, p.getEyeLocation(), 0.3f, 0.1f);
						}
					}
				}
			}.runTaskTimer(HubPlugin.ins, 10, 10));
		}
	}

	public void end() {
		for (Player p : getPlayers()) {
			p.teleport(HubPlugin.dW().getSpawnLocation());
			DefaultListener.resetPlayerInv(p);
		}
		games.remove(this);

		players.clear();
		green.clear();
		yellow.clear();

		HandlerList.unregisterAll(this);

		for (BukkitTask bt : tasks) {
			bt.cancel();
		}
	}

	public void onDeath(Player p, String msg) {
		send("§8[§c§lPaintball§8] §f" + msg);

		if (!current.allowRespawn) {
			green.remove(p.getName());
			yellow.remove(p.getName());

			if (green.size() == 0) {
				MCShockwave.broadcast(ChatColor.GREEN, "%s has won a game of paintball!", "Green");
				end();
				return;
			}

			if (yellow.size() == 0) {
				MCShockwave.broadcast(ChatColor.YELLOW, "%s has won a game of paintball!", "Yellow");
				end();
				return;
			}
		}

		if (current == Minigame.Grifball) {
			if (p.getInventory().contains(Material.TNT)) {
				p.getWorld().dropItemNaturally(p.getLocation(),
						ItemMetaUtils.setItemName(new ItemStack(Material.TNT), "§cGrif§6ball"));
			}
		}
	}

	public void onRespawn(final Player p, PlayerRespawnEvent event) {
		if (!current.allowRespawn) {
			players.remove(p.getName());
			event.setRespawnLocation(HubPlugin.dW().getSpawnLocation());
			DefaultListener.resetPlayerInv(p);
			return;
		}
		event.setRespawnLocation(getSpawn(p.getName(), 300));

		new BukkitRunnable() {
			public void run() {
				respawn(p);
			}
		}.runTaskLater(HubPlugin.ins, 1);
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity ee = event.getEntity();
		Entity de = event.getDamager();

		if (ee instanceof Player && de instanceof Player) {
			Player p = (Player) ee;
			Player d = (Player) de;

			if (green.contains(p.getName()) && green.contains(d.getName()) || yellow.contains(p.getName())
					&& yellow.contains(d.getName())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player p = event.getPlayer();
		Item i = event.getItem();
		ItemStack it = i.getItemStack();

		if (Paintball.getGame(p.getName()) == this && it.getType() == Material.WOOL) {
			short data = (short) (green.contains(p.getName()) ? 5 : yellow.contains(p.getName()) ? 4 : 0);
			String team = data == 5 ? "§aGreen" : "§eYellow";
			ChatColor sameTeam = data == 5 ? ChatColor.GREEN : ChatColor.YELLOW;
			ChatColor otherTeam = data == 5 ? ChatColor.YELLOW : ChatColor.GREEN;

			if (it.getDurability() == data) {
				event.setCancelled(true);
				if (getSpawn(p.getName()).distanceSquared(i.getLocation()) > 3 * 3) {
					i.teleport(getSpawn(p.getName()));
					send(team + "'s §8wool was recovered by " + sameTeam + p.getName() + "§8!");
				}
			} else {
				send(team + "'s §8wool was stolen by " + otherTeam + p.getName() + "§8!");
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		ItemStack it = event.getItemDrop().getItemStack();

		if (Paintball.getGame(event.getPlayer().getName()) == this && current == Minigame.Grifball
				&& it.getType() == Material.TNT) {
			event.setCancelled(false);
		}
	}

	public void respawn(Player p) {
		if (p.getGameMode() != GameMode.ADVENTURE) {
			p.setGameMode(GameMode.ADVENTURE);
		}

		p.teleport(getSpawn(p.getName(), 300));
		p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 150, 255));
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 150, 10));
		p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));

		giveKit(p);
	}

	public void giveKit(Player p) {
		PlayerInventory pi = p.getInventory();
		pi.clear();

		Color color = green.contains(p.getName()) ? Color.GREEN : yellow.contains(p.getName()) ? Color.YELLOW
				: Color.WHITE;

		pi.setHelmet(ItemMetaUtils.setLeatherColor(new ItemStack(Material.LEATHER_HELMET), color));
		pi.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		pi.setLeggings(ItemMetaUtils.setLeatherColor(new ItemStack(Material.LEATHER_LEGGINGS), color));
		pi.setBoots(ItemMetaUtils.setLeatherColor(new ItemStack(Material.LEATHER_BOOTS), color));

		ItemStack gun = Gun.WL_P30.getItem();
		Addon.Bottomless_Clip.add(gun);
		Addon.Foregrip.add(gun);
		Addon.Laser_Pointer.add(gun);

		pi.addItem(gun);
	}

	public void send(String msg) {
		send(msg, players);
	}

	public void send(String msg, ArrayList<String> names) {
		for (Player p : getPlayers(names)) {
			p.sendMessage(msg);
		}
	}

	public ArrayList<Player> getPlayers() {
		return getPlayers(players);
	}

	public ArrayList<Player> getPlayers(ArrayList<String> names) {
		ArrayList<Player> ret = new ArrayList<>();
		for (String pl : names) {
			if (Bukkit.getPlayer(pl) != null) {
				ret.add(Bukkit.getPlayer(pl));
			}
		}
		return ret;
	}

	public static enum Minigame {
		Elimination(
			false),
		Grifball(
			true),
		Capture_the_Flag(
			true),
		Team_Deathmatch(
			true),
		Search_and_Destroy(
			false);

		public boolean	allowRespawn;

		Minigame(boolean allowRespawn) {
			this.allowRespawn = allowRespawn;
		}

		@Override
		public String toString() {
			return name().replace('_', ' ');
		}

		public static Minigame getFromString(String s) {
			for (Minigame game : values()) {
				if (game.toString().equalsIgnoreCase(s)) {
					return game;
				}
			}
			return null;
		}
	}

	public static enum GameState {
		NONE,
		QUEUED,
		STARTED;
	}

	public static Paintball getGame(String pl) {
		for (Paintball pg : games) {
			if (pg.players.contains(pl)) {
				return pg;
			}
		}
		return null;
	}

	public Location getSpawn(String s) {
		return green.contains(s) ? getGreenSpawn(defaultY) : yellow.contains(s) ? getYellowSpawn(defaultY) : null;
	}

	public Location getSpawn(String s, int y) {
		return green.contains(s) ? getGreenSpawn(y) : yellow.contains(s) ? getYellowSpawn(y) : null;
	}

	public static Location getYellowSpawn(int y) {
		return new Location(HubPlugin.endWorld(), 473.5, y, 467.5);
	}

	public static Location getGreenSpawn(int y) {
		return new Location(HubPlugin.endWorld(), 527.5, y, 553.5, 0, 180);
	}

	public static Location getCenter() {
		return new Location(HubPlugin.endWorld(), 500.5, defaultY, 510.5);
	}

}
