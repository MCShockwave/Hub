package net.mcshockwave.Hub.Kit.Paintball;

import static net.mcshockwave.Hub.Kit.Paintball.Paintball.GameState.NONE;
import static net.mcshockwave.Hub.Kit.Paintball.Paintball.GameState.QUEUED;
import static net.mcshockwave.Hub.Kit.Paintball.Paintball.GameState.STARTED;
import net.mcshockwave.Guns.Gun;
import net.mcshockwave.Guns.addons.Addon;
import net.mcshockwave.Guns.descriptors.Category;
import net.mcshockwave.Guns.events.GunFireEvent;
import net.mcshockwave.Guns.events.GunHitEvent;
import net.mcshockwave.Hub.DefaultListener;
import net.mcshockwave.Hub.HubPlugin;
import net.mcshockwave.Hub.Kit.TournamentManager;
import net.mcshockwave.Hub.Kit.Paintball.PBObjective.ControlPoint;
import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Menu.ItemMenu.Button;
import net.mcshockwave.MCS.Menu.ItemMenu.ButtonRunnable;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.MiscUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutCollect;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityDestroy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
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
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;

public class Paintball implements Listener {

	public static String				cmdMenu		= "/paint";

	public static ArrayList<Paintball>	games		= new ArrayList<>();

	static Random						rand		= new Random();

	public static int					defaultY	= 101;

	public ArrayList<BukkitTask>		tasks		= new ArrayList<>();

	public Scoreboard					sb			= null;

	public Team							grT			= null, ylT = null;
	public Objective					sidebar		= null;

	private Paintball() {
	}

	public ArrayList<String>	specs	= new ArrayList<>(), players = new ArrayList<>(), green = new ArrayList<>(),
			yellow = new ArrayList<>();

	public static Paintball newGame(Minigame game, int maxPlayers) {
		Paintball pg = new Paintball();
		pg.current = game;
		pg.state = NONE;
		pg.maxPlayers = maxPlayers;
		pg.gameUUID = UUID.randomUUID();
		pg.sb = Bukkit.getScoreboardManager().getNewScoreboard();

		games.add(pg);
		return pg;
	}

	public String					p1, p2;

	public GameState				state		= null;

	public Minigame					current		= null;

	public int						maxPlayers	= 0;

	public boolean					autoStart	= false;

	public UUID						gameUUID	= null;

	public ArrayList<PBObjective>	objs		= new ArrayList<>();

	public BukkitTask				pointTask	= null, waveTask = null;

	public Score					grnWaves	= null, ylwWaves = null;

	public int						grnBroadcastCount	= 20, ylwBroadcastCount = 20;

	public void queue(boolean autoStart, boolean broadcast) {
		if (broadcast) {
			MCShockwave.broadcastAll(MCShockwave.getBroadcastMessage(ChatColor.DARK_GREEN,
					"A new game of %s has been queued!", "paintball"), MCShockwave.getBroadcastMessage(ChatColor.RED,
					"Gamemode: %s   Max Players: %s", current.toString(), maxPlayers), MCShockwave.getBroadcastMessage(
					ChatColor.DARK_AQUA, "Type %s to join!", cmdMenu));
		}
		this.autoStart = autoStart;
		state = QUEUED;
	}

	public void leave(String pl) {
		if (state == QUEUED) {
			players.remove(pl);
			send("§8" + pl + "§7 has left the queue");
		} else if (state == STARTED) {
			players.remove(pl);
			green.remove(pl);
			yellow.remove(pl);
			send("§8" + pl + "§7 has left the game");

			if (Bukkit.getPlayer(pl) != null) {
				Player p = Bukkit.getPlayer(pl);

				p.teleport(HubPlugin.dW().getSpawnLocation());
				DefaultListener.resetPlayerInv(p);
				p.setHealth(p.getMaxHealth());
				p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

				onDeath(p, pl + " left");

				for (Paintball pg : games) {
					if (pg != this) {
						for (Player p2 : pg.getPlayers(true)) {
							p.showPlayer(p2);
							p2.showPlayer(p);
						}
					}
				}
			}
		}
	}

	public void addToQueue(String pl) {
		if (players.contains(pl)) {
			return;
		}
		if (Paintball.getGame(pl) != null) {
			Paintball.getGame(pl).leave(pl);
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

	public void addSpectator(Player p, boolean playing) {
		for (Paintball pg : games) {
			for (Player p2 : pg.getPlayers(true)) {
				p2.hidePlayer(p);
				if (pg.specs.contains(p2.getName())) {
					p.hidePlayer(p2);
				} else {
					p.showPlayer(p2);
				}
			}
		}
		specs.add(p.getName());

		p.teleport(getCenter());
		p.getInventory().clear();

		MCShockwave.send(ChatColor.DARK_AQUA, p, "Now %s", "spectating");
	}

	public void removeSpectator(Player p, boolean playing) {
		for (Paintball pg : games) {
			for (Player p2 : pg.getPlayers(true)) {
				p2.showPlayer(p);
				p.showPlayer(p2);
			}
		}
		specs.remove(p.getName());

		if (!playing) {
			p.teleport(HubPlugin.dW().getSpawnLocation());
			DefaultListener.resetPlayerInv(p);

			MCShockwave.send(ChatColor.DARK_AQUA, p, "No longer %s", "spectating");
		} else {
			respawn(p);
		}
	}

	public void start() {
		start(true);
	}

	public void start(boolean randomizeTeams) {
		state = STARTED;

		alternateMap = current.altMap;

		for (Paintball pg : games) {
			if (pg != this) {
				for (Player p : getPlayers(true)) {
					for (Player p2 : pg.getPlayers(true)) {
						p.hidePlayer(p2);
						p2.hidePlayer(p);
					}
				}
			}
		}

		tasks.add(new BukkitRunnable() {
			public void run() {
				for (Paintball pg : games) {
					if (pg == Paintball.this)
						continue;
					for (Entity e : HubPlugin.endWorld().getEntities()) {
						if (pg.isItem(e)) {
							PacketPlayOutEntityDestroy dest = new PacketPlayOutEntityDestroy(e.getEntityId());
							sendPacket(dest);
						}
					}
				}
			}
		}.runTaskTimer(HubPlugin.ins, 50, 50));

		tasks.add(new BukkitRunnable() {
			public void run() {
				for (Entity e : HubPlugin.endWorld().getEntities()) {
					if (isItem(e)) {
						Item i = (Item) e;

						if (i.getPickupDelay() > 0) {
							continue;
						}

						for (Entity ne : i.getNearbyEntities(2, 2, 2)) {
							if (ne instanceof Player) {
								Player pickup = (Player) ne;

								if (!players.contains(pickup.getName()) || pickup.isDead() || !pickup.isValid()) {
									continue;
								}

								if (!onPickupItem(pickup, i)) {
									PacketPlayOutCollect col = new PacketPlayOutCollect(i.getEntityId(), pickup
											.getEntityId());
									sendPacket(col);
									pickup.getInventory().addItem(i.getItemStack());
									i.remove();
									break;
								}
							}
						}
					}
				}
			}
		}.runTaskTimer(HubPlugin.ins, 5, 5));

		send("§6§lGame started!");

		if (randomizeTeams) {
			ArrayList<Player> randomized = getPlayers(false);
			Collections.shuffle(randomized, rand);
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
		}

		grT = sb.registerNewTeam("Green");
		grT.setPrefix("§2");
		grT.setSuffix("§r");

		ylT = sb.registerNewTeam("Yellow");
		ylT.setPrefix("§e");
		ylT.setSuffix("§r");

		sidebar = sb.registerNewObjective("Sidebar", "dummy");
		sidebar.setDisplayName("§c" + current.toString());
		sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

		if (current.isWavesMinigame()) {
			grnWaves = sidebar.getScore("§2Green Waves");
			grnWaves.setScore(current.wavesStartGrn);

			ylwWaves = sidebar.getScore("§eYellow Waves");
			ylwWaves.setScore(current.wavesStartYlw);
		}

		tasks.add(new BukkitRunnable() {
			public void run() {
				sidebar.getScore("§2Green").setScore(green.size());
				sidebar.getScore("§eYellow").setScore(yellow.size());
			}
		}.runTaskTimer(HubPlugin.ins, 10, 10));

		send("§2§lGreen Team:\n");
		for (Player p : getPlayersInList(green)) {
			send("§7§o" + p.getName());
			grT.addPlayer(p);
		}

		send("§e§lYellow Team:\n");
		for (Player p : getPlayersInList(yellow)) {
			send("§7§o" + p.getName());
			ylT.addPlayer(p);
		}

		objs.clear();

		if (current == Minigame.Gun_Game) {
			for (Player p : getPlayers(false)) {
				ggtier.put(p.getName(), 0);
			}
		}

		for (Player p : getPlayers(false)) {
			respawn(p);
			p.setHealth(p.getMaxHealth());
			p.setScoreboard(sb);
		}

		Bukkit.getPluginManager().registerEvents(this, HubPlugin.ins);

		if (current == Minigame.Firefight) {
			objs.add(PBObjective.cp1(this));
			objs.add(PBObjective.cp2(this));
			objs.add(PBObjective.cp3(this));

			for (PBObjective obj : objs) {
				obj.setActive(true);
			}

			PBObjective.cp1(this).setColor(PBObjective.GREEN_COLOR, true);
			PBObjective.cp2(this).setColor(PBObjective.NEUTRAL_COLOR, true);
			PBObjective.cp3(this).setColor(PBObjective.YELLOW_COLOR, true);

			PBObjective.cp1(this).identifier = "Alpha";
			PBObjective.cp2(this).identifier = "Bravo";
			PBObjective.cp3(this).identifier = "Charlie";
		}

		if (current == Minigame.Occupy) {
			objs.clear();
			objs.add(PBObjective.cp2(this));

			for (PBObjective obj : objs) {
				obj.setActive(true);
			}

			PBObjective.cp2(this).setColor(PBObjective.NEUTRAL_COLOR, true);

			PBObjective.cp2(this).identifier = "Alpha";
		}

		if (current == Minigame.Siege) {
			Player grn = getPlayersInList(green).get(rand.nextInt(green.size()));
			Player ylw = getPlayersInList(yellow).get(rand.nextInt(yellow.size()));

			grnKing = grn.getName();
			ylwKing = ylw.getName();

			send("§2§l" + grnKing + " is the Green King!");
			send("§e§l" + ylwKing + " is the Yellow King!");

			grn.getInventory().setHelmet(new ItemStack(Material.GOLD_HELMET));
			ylw.getInventory().setHelmet(new ItemStack(Material.GOLD_HELMET));
		}

		if (current == Minigame.Search_and_Destroy) {
			Player bmb = getPlayers(false).get(rand.nextInt(getPlayers(false).size()));
			grnBomb = green.contains(bmb.getName());
			bmb.getInventory().addItem(new ItemStack(Material.TNT));

			send(grnBomb ? "§2§lGreen team has the bomb! §2(" + bmb.getName() + ")"
					: "§e§lYellow team has the bomb! §e(" + bmb.getName() + ")");

			tasks.add(new BukkitRunnable() {
				public void run() {
					if (bombPlanted == null) {
						for (Player p : getPlayersInList(grnBomb ? green : yellow)) {
							if (p.getInventory().contains(Material.TNT)) {
								p.getWorld().playEffect(p.getEyeLocation(), Effect.STEP_SOUND, Material.TNT);
								if (p.getLocation().distanceSquared(getCenter()) < 3 * 3) {
									double maxProg = 5;
									if (++plantProgress >= maxProg) {
										bombPlanted = dropItem(getCenter(), new ItemStack(Material.TNT));
										bombPlanted.setVelocity(new Vector());
										send("§6§lThe Bomb has been planted by " + p.getName());
										defuseProgress = 30;
										plantProgress = 0;
										p.getInventory().clear(p.getInventory().first(Material.TNT));
										break;
									} else {
										playSound(p.getLocation(), Sound.WOOD_CLICK, 10, 0);
										send("§6Planting... (§e" + ((int) ((plantProgress / maxProg) * 100)) + "%§6)");
										break;
									}
								}
							}
						}
					} else {
						double maxProg = 45;
						playParticleEffect(ParticleEffect.FLAME, bombPlanted.getLocation(), 0, 0.05f, 10);
						playSound(bombPlanted.getLocation(), Sound.CLICK, 10,
								((float) plantProgress / (float) maxProg) + 1);

						boolean def = false;
						for (Player p : getPlayersInList(grnBomb ? yellow : green)) {
							if (p.getLocation().distanceSquared(bombPlanted.getLocation()) < 3 * 3) {
								send("§6Defusing... (§e" + ((int) (((30 - --defuseProgress) / 30) * 100)) + "%§6)");
								def = true;
								if (defuseProgress <= 0) {
									MCShockwave.broadcast(grnBomb ? ChatColor.YELLOW : ChatColor.DARK_GREEN,
											"%s has won a game of paintball!", grnBomb ? "Yellow" : "Green");
									end(grnBomb ? "Yellow" : "Green");
									return;
								}
								break;
							}
						}

						if (!def) {
							send("§6Timer: §e" + (int) (maxProg - (plantProgress + 1)));
							if (++plantProgress >= maxProg) {
								playSound(bombPlanted.getLocation(), Sound.EXPLODE, 10, 0);
								MCShockwave.broadcast(grnBomb ? ChatColor.DARK_GREEN : ChatColor.YELLOW,
										"%s has won a game of paintball!", grnBomb ? "Green" : "Yellow");
								end(grnBomb ? "Green" : "Yellow");
								return;
							}
						}
					}
				}
			}.runTaskTimer(HubPlugin.ins, 20, 20));
		}

		if (current == Minigame.Capture_the_Flag) {
			tasks.add(new BukkitRunnable() {
				public void run() {
					dropItem(getGreenSpawn(defaultY),
							ItemMetaUtils.setItemName(new ItemStack(Material.WOOL, 1, (short) 5), "§2Green Wool"))
							.setVelocity(new Vector());
					dropItem(getYellowSpawn(defaultY),
							ItemMetaUtils.setItemName(new ItemStack(Material.WOOL, 1, (short) 4), "§eYellow Wool"))
							.setVelocity(new Vector());
				}
			}.runTaskLater(HubPlugin.ins, 200));

			tasks.add(new BukkitRunnable() {
				public void run() {
					for (Player p : getPlayersInList(green)) {
						if (p.getInventory().contains(Material.WOOL)) {
							p.getWorld().playEffect(p.getEyeLocation(), Effect.STEP_SOUND, Material.WOOL, 4);
							if (getGreenSpawn(defaultY).distanceSquared(p.getLocation()) < 3 * 3 && isFlagAtBase(5)) {
								MCShockwave.broadcast(ChatColor.DARK_GREEN, "%s has won a game of paintball!", "Green");
								end("Green");
								return;
							}
						}
					}
					for (Player p : getPlayersInList(yellow)) {
						if (p.getInventory().contains(Material.WOOL)) {
							p.getWorld().playEffect(p.getEyeLocation(), Effect.STEP_SOUND, Material.WOOL, 5);
							if (getYellowSpawn(defaultY).distanceSquared(p.getLocation()) < 3 * 3 && isFlagAtBase(4)) {
								MCShockwave.broadcast(ChatColor.YELLOW, "%s has won a game of paintball!", "Yellow");
								end("Yellow");
								return;
							}
						}
					}
				}
			}.runTaskTimer(HubPlugin.ins, 5, 5));
		}

		if (current == Minigame.Grifball) {
			tasks.add(new BukkitRunnable() {
				public void run() {
					dropItem(getCenter(), ItemMetaUtils.setItemName(new ItemStack(Material.TNT), "§cGrif§6ball"));
				}
			}.runTaskLater(HubPlugin.ins, 200));

			tasks.add(new BukkitRunnable() {
				public void run() {
					for (Entity e : HubPlugin.endWorld().getEntities()) {
						if (e instanceof Item) {
							Item i = (Item) e;

							if (i.getItemStack().getType() == Material.TNT && isItem(i)) {
								if (getYellowSpawn(defaultY).distanceSquared(i.getLocation()) < 3 * 3) {
									MCShockwave.broadcast(ChatColor.DARK_GREEN, "%s has won a game of paintball!",
											"Green");
									playSound(i.getLocation(), Sound.EXPLODE, 10, 0);
									i.remove();
									end("Green");
									return;
								}

								if (getGreenSpawn(defaultY).distanceSquared(i.getLocation()) < 3 * 3) {
									MCShockwave
											.broadcast(ChatColor.YELLOW, "%s has won a game of paintball!", "Yellow");
									playSound(i.getLocation(), Sound.EXPLODE, 10, 0);
									i.remove();
									end("Yellow");
									return;
								}
							}
						}
					}

					for (Player p : getPlayers(false)) {
						if (p.getInventory().contains(Material.TNT)) {
							p.getWorld().playEffect(p.getEyeLocation(), Effect.STEP_SOUND, Material.TNT);
						}
					}
				}
			}.runTaskTimer(HubPlugin.ins, 10, 10));
		}

		if (objs.size() > 0) {
			pointTask = new BukkitRunnable() {
				public void run() {
					for (PBObjective pbo : objs.toArray(new PBObjective[0])) {
						if (pbo instanceof ControlPoint) {
							ControlPoint cp = (ControlPoint) pbo;

							ArrayList<String> grnPlayers = new ArrayList<>();
							ArrayList<String> ylwPlayers = new ArrayList<>();
							for (Player p : getPlayers(false)) {
								if (!specs.contains(p.getName()) && cp.isInPoint(p.getLocation())) {
									if (green.contains(p.getName())) {
										grnPlayers.add(p.getName());
									}
									if (yellow.contains(p.getName())) {
										ylwPlayers.add(p.getName());
									}
								}
							}

							// if team already has obj captured and more people
							// than other team
							if (ylwPlayers.size() > grnPlayers.size() && cp.teamOwn == PBObjective.YELLOW_COLOR
									&& cp.timer <= -ControlPoint.CP_TIMER_MAX || grnPlayers.size() > ylwPlayers.size()
									&& cp.teamOwn == PBObjective.GREEN_COLOR && cp.timer >= ControlPoint.CP_TIMER_MAX) {
								return;
							}

							if (grnPlayers.size() > ylwPlayers.size()) {
								if (cp.isBeingTaken && cp.teamOwn == PBObjective.GREEN_COLOR) {
									cp.isBeingTaken = false;
								} else if (!cp.isBeingTaken) {
									send("§2§lGreen§7 is attacking §2§l" + cp.identifier + "§7!");
									cp.isBeingTaken = true;
								}
								cp.timer++;
							} else if (ylwPlayers.size() > grnPlayers.size()) {
								if (cp.isBeingTaken && cp.teamOwn == PBObjective.YELLOW_COLOR) {
									cp.isBeingTaken = false;
								} else if (!cp.isBeingTaken) {
									send("§e§lYellow§7 is attacking §2§l" + cp.identifier + "§7!");
									cp.isBeingTaken = true;
								}
								cp.timer--;
							}

							String msgPrefix = "§7Point §3" + cp.identifier + "§f: ", msg = "";
							for (int i = 0; i < ControlPoint.CP_TIMER_MAX; i++) {
								msg += "|";
							}
							if (cp.timer < 0) {
								String pr = msg.substring(0, -cp.timer);
								String su = msg.substring(-cp.timer, msg.length());
								msg = "§e" + pr + "§f" + su;
							}
							if (cp.timer > 0) {
								String pr = msg.substring(0, cp.timer);
								String su = msg.substring(cp.timer, msg.length());
								msg = "§2" + pr + "§f" + su;
							}

							send(msgPrefix + msg, grnPlayers);
							send(msgPrefix + msg, ylwPlayers);

							if (cp.timer == 0) {
								cp.setColor(PBObjective.NEUTRAL_COLOR, true);
							}
							if (cp.timer >= ControlPoint.CP_TIMER_MAX && cp.teamOwn != PBObjective.GREEN_COLOR) {
								PointCaptureEvent ev = new PointCaptureEvent(cp, PBObjective.GREEN_COLOR);
								Bukkit.getPluginManager().callEvent(ev);
								cp.setColor(ev.team, true);
							}
							if (cp.timer <= -ControlPoint.CP_TIMER_MAX && cp.teamOwn != PBObjective.YELLOW_COLOR) {
								PointCaptureEvent ev = new PointCaptureEvent(cp, PBObjective.YELLOW_COLOR);
								Bukkit.getPluginManager().callEvent(ev);
								cp.setColor(ev.team, true);
							}
						}
					}
				}
			}.runTaskTimer(HubPlugin.ins, 20, 20);
		}

		if (current.isWavesMinigame()) {
			waveTask = new BukkitRunnable() {
				public void run() {
					if (grnWaves.getScore() > 0) {
						ArrayList<String> grnDead = new ArrayList<>();
						for (Player p : getPlayersInList(green)) {
							// if dead
							if (specs.contains(p.getName())) {
								grnDead.add(p.getName());
							}
						}

						if (grnDead.size() >= green.size() * 0.3) {
							send("§7Respawning in §2" + grnBroadcastCount--, grnDead);
							if (grnBroadcastCount <= 0) {
								respawnTeam(green);
								grnBroadcastCount = 20;
								if (current != Minigame.Occupy
										|| PBObjective.cp2(Paintball.this).teamOwn != PBObjective.GREEN_COLOR) {
									grnWaves.setScore(grnWaves.getScore() - 1);
								}
							}
						}
					}

					if (ylwWaves.getScore() > 0) {
						ArrayList<String> ylwDead = new ArrayList<>();
						for (Player p : getPlayersInList(yellow)) {
							// if dead
							if (specs.contains(p.getName())) {
								ylwDead.add(p.getName());
							}
						}

						if (ylwDead.size() >= yellow.size() * 0.3) {
							send("§7Respawning in §e" + ylwBroadcastCount--, ylwDead);
							if (ylwBroadcastCount <= 0) {
								respawnTeam(yellow);
								ylwBroadcastCount = 20;
								if (current != Minigame.Occupy
										|| PBObjective.cp2(Paintball.this).teamOwn != PBObjective.YELLOW_COLOR) {
									ylwWaves.setScore(ylwWaves.getScore() - 1);
								}
							}
						}
					}
				}
			}.runTaskTimer(HubPlugin.ins, 20, 20);
		}
	}

	public double					plantProgress	= 0;
	public double					defuseProgress	= 0;
	public boolean					grnBomb			= false;
	public Item						bombPlanted		= null;

	public String					grnKing;
	public String					ylwKing;

	public HashMap<String, Integer>	ggtier			= new HashMap<>();

	public static boolean			alternateMap	= false;

	public boolean isFlagAtBase(int data) {
		for (Entity e : HubPlugin.endWorld().getEntities()) {
			if (e instanceof Item) {
				Item i = (Item) e;

				if (i.getItemStack().getDurability() == data) {
					if (data == 5) {
						return getGreenSpawn(defaultY).distanceSquared(i.getLocation()) < 3 * 3;
					} else {
						return getYellowSpawn(defaultY).distanceSquared(i.getLocation()) < 3 * 3;
					}
				}
			}
		}
		return false;
	}

	public void end(String winner) {
		if (winner != null && TournamentManager.paintball.containsValue(gameUUID)) {
			boolean te = TournamentManager.teams_enabled;
			TournamentManager.onWin(
					"Green".equalsIgnoreCase(winner) ? (te ? p1 : green.get(0))
							: "Yellow".equalsIgnoreCase(winner) ? (te ? p2 : yellow.get(0)) : winner, this);
		}

		for (Player p : getPlayers(false)) {
			try {
				p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
				if (!p.isDead()) {
					p.teleport(HubPlugin.dW().getSpawnLocation());
					DefaultListener.resetPlayerInv(p);
					p.setHealth(p.getMaxHealth());
				}
			} catch (Exception e) {
				MiscUtils.printStackTrace(e);
			}
		}

		for (Player p : getPlayersInList(specs)) {
			removeSpectator(p, false);
		}

		for (Paintball pg : games) {
			if (pg != this) {
				for (Player p : getPlayers(true)) {
					for (Player p2 : pg.getPlayers(true)) {
						p.showPlayer(p2);
						p2.showPlayer(p);
					}
				}
			}
		}

		games.remove(this);

		players.clear();
		green.clear();
		yellow.clear();

		for (PBObjective pbobj : objs) {
			pbobj.setActive(false);
		}
		objs.clear();

		waveTask.cancel();
		pointTask.cancel();

		HandlerList.unregisterAll(this);

		for (BukkitTask bt : tasks) {
			bt.cancel();
		}

		for (Entity e : HubPlugin.endWorld().getEntities()) {
			if (e instanceof Item) {
				Item i = (Item) e;

				if (isItem(i)) {
					i.remove();
				}
			}
		}
	}

	public void onDeath(Player p, String msg) {
		for (Player p2 : getPlayers(true)) {
			Team t = sb.getPlayerTeam(p2);
			if (t == null)
				continue;
			msg = msg.replace(p2.getName(), t.getPrefix() + p2.getName() + t.getSuffix());
		}
		send("§8[§c§lPaintball§8] §f" + msg);

		if (current == Minigame.Search_and_Destroy) {
			if (p.getInventory().contains(Material.TNT)) {
				dropItemNaturally(p.getLocation(), new ItemStack(Material.TNT));
			}
		}

		if (current == Minigame.Gun_Game && p.getKiller() != null) {
			final Player k = p.getKiller();
			int tier = ggtier.get(k.getName());
			ggtier.remove(k.getName());
			ggtier.put(k.getName(), ++tier);

			if (tier >= Category.Wasted.getGuns().length) {
				MCShockwave.broadcast("%s has won a game of paintball!", k.getName());
				end(k.getName());
				return;
			}

			k.getInventory().clear();
			new BukkitRunnable() {
				public void run() {
					giveKit(k);
				}
			}.runTaskLater(HubPlugin.ins, 10);

			send((green.contains(p.getName()) ? "§e" : "§2") + k.getName() + "§8 now has the §c"
					+ Category.Wasted.getGuns()[tier].name);
			k.playSound(p.getLocation(), Sound.ITEM_PICKUP, 10, 0);
		}

		boolean noRes = !current.allowRespawn;

		if (current == Minigame.Siege) {
			if (p.getName().equals(grnKing)) {
				send("§2§lThe Green King has died! Green can no longer respawn!");
				grnKing = null;
			}
			if (p.getName().equals(ylwKing)) {
				send("§e§lThe Yellow King has died! Yellow can no longer respawn!");
				ylwKing = null;
			}

			if (green.contains(p.getName()) && grnKing == null || yellow.contains(p.getName()) && ylwKing == null) {
				noRes = true;
			}
		}

		if (noRes) {
			green.remove(p.getName());
			yellow.remove(p.getName());
			grT.removePlayer(p);
			ylT.removePlayer(p);

			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

			if (current == Minigame.Search_and_Destroy && bombPlanted != null) {
				return;
			}

			if (yellow.size() == 0) {
				MCShockwave.broadcast(ChatColor.DARK_GREEN, "%s has won a game of paintball!", "Green");
				end("Green");
				return;
			}

			if (green.size() == 0) {
				MCShockwave.broadcast(ChatColor.YELLOW, "%s has won a game of paintball!", "Yellow");
				end("Yellow");
				return;
			}
		}

		if (current == Minigame.Grifball) {
			if (p.getInventory().contains(Material.TNT)) {
				dropItemNaturally(p.getLocation(),
						ItemMetaUtils.setItemName(new ItemStack(Material.TNT), "§cGrif§6ball"));
			}
		}

		if (current == Minigame.Capture_the_Flag) {
			if (p.getInventory().contains(Material.WOOL)) {
				boolean grn = green.contains(p.getName());
				dropItemNaturally(p.getLocation(), ItemMetaUtils.setItemName(new ItemStack(Material.WOOL, 1,
						(short) (grn ? 4 : 5)), (grn ? "§eYellow" : "§2Green") + " Wool"));
			}
		}

		if (current.isWavesMinigame()) {
			if (grnWaves.getScore() <= 0) {
				boolean ylwWins = true;
				for (Player pl : getPlayersInList(green)) {
					if (!specs.contains(pl.getName())) {
						ylwWins = false;
						break;
					}
				}
				if (ylwWins) {
					MCShockwave.broadcast(ChatColor.YELLOW, "%s has won a game of paintball!", "Yellow");
					end("Yellow");
				}
			}

			if (ylwWaves.getScore() <= 0) {
				boolean grnWins = true;
				for (Player pl : getPlayersInList(yellow)) {
					if (!specs.contains(pl.getName())) {
						grnWins = false;
						break;
					}
				}
				if (grnWins) {
					MCShockwave.broadcast(ChatColor.DARK_GREEN, "%s has won a game of paintball!", "Green");
					end("Green");
				}
			}
		}
	}

	public void onRespawn(final Player p, PlayerRespawnEvent event) {
		if (!current.allowRespawn) {
			players.remove(p.getName());
			event.setRespawnLocation(HubPlugin.dW().getSpawnLocation());
			DefaultListener.resetPlayerInv(p);
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			return;
		}
		if (current.isWavesMinigame()) {
			new BukkitRunnable() {
				public void run() {
					addSpectator(p, true);
				}
			}.runTaskLater(HubPlugin.ins, 1);
		} else {
			event.setRespawnLocation(getSpawn(p.getName(), 300));

			new BukkitRunnable() {
				public void run() {
					respawn(p);
				}
			}.runTaskLater(HubPlugin.ins, 1);
		}
	}

	@EventHandler
	public void onGunFire(GunFireEvent event) {
		if (event.getEntity() instanceof Player) {
			Player shooter = (Player) event.getEntity();

			if (players.contains(shooter.getName())) {
				event.canSee().clear();
				for (Player p : getPlayers(true)) {
					event.canSee().add(p);
				}
			}
		}
	}

	@EventHandler
	public void onGunHit(GunHitEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player hit = (Player) event.getEntity();
			Player shooter = (Player) event.getDamager();

			if (players.contains(shooter.getName()) && Paintball.getGame(hit.getName()) != this) {
				event.setCancelled(true);
			}

			if (green.contains(hit.getName()) && green.contains(shooter.getName()) || yellow.contains(hit.getName())
					&& yellow.contains(shooter.getName())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity ee = event.getEntity();
		Entity de = event.getDamager();

		if (ee instanceof Player && de instanceof Player) {
			Player p = (Player) ee;
			Player d = (Player) de;

			if (Paintball.getGame(p.getName()) == this && Paintball.getGame(d.getName()) != this) {
				event.setCancelled(true);
				return;
			}

			if (green.contains(p.getName()) && green.contains(d.getName()) || yellow.contains(p.getName())
					&& yellow.contains(d.getName())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (Paintball.getGame(event.getPlayer().getName()) == null && isItem(event.getItem())
				|| Paintball.getGame(event.getPlayer().getName()) == this) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event) {
		if (isItem(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	public boolean onPickupItem(Player p, Item i) {
		boolean cancelled = false;
		ItemStack it = i.getItemStack();

		if (Paintball.getGame(p.getName()) == this && it.getType() == Material.WOOL) {
			short data = (short) (green.contains(p.getName()) ? 5 : yellow.contains(p.getName()) ? 4 : 0);
			String sameName = data == 5 ? "§2Green" : "§eYellow";
			String otherName = data == 5 ? "§eYellow" : "§2Green";
			ChatColor sameTeam = data == 5 ? ChatColor.DARK_GREEN : ChatColor.YELLOW;

			if (it.getDurability() == data) {
				cancelled = true;
				if (getSpawn(p.getName()).distanceSquared(i.getLocation()) > 3 * 3) {
					i.teleport(getSpawn(p.getName()));
					send(sameName + "'s §7wool was recovered by " + sameTeam + p.getName() + "§7!");
				}
			} else {
				send(otherName + "'s §7wool was stolen by " + sameTeam + p.getName() + "§7!");
			}
		}

		if (current == Minigame.Search_and_Destroy) {
			if (Paintball.getGame(p.getName()) == this && it.getType() == Material.TNT) {
				if (grnBomb && yellow.contains(p.getName()) || !grnBomb && green.contains(p.getName())) {
					cancelled = true;
				}
			}

			if (i.equals(bombPlanted)) {
				cancelled = true;
			}
		}

		return cancelled;
	}

	@EventHandler
	public void onPlayerDropItem(final PlayerDropItemEvent event) {
		ItemStack it = event.getItemDrop().getItemStack();

		if (Paintball.getGame(event.getPlayer().getName()) == this
				&& (current == Minigame.Grifball || current == Minigame.Search_and_Destroy)
				&& it.getType() == Material.TNT) {
			Bukkit.getScheduler().runTaskLater(HubPlugin.ins, new Runnable() {
				public void run() {
					setDroppedItem(event.getItemDrop());
				}
			}, 10l);
			event.setCancelled(false);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		Location fr = event.getFrom();
		Location to = event.getTo();

		for (PBObjective obj : objs) {
			if (obj instanceof ControlPoint) {
				ControlPoint cp = (ControlPoint) obj;
				if (cp.isInPoint(to) && !cp.isInPoint(fr)) {
					p.sendMessage("§7Entering point §3§l" + cp.identifier);
				}
				if (!cp.isInPoint(to) && cp.isInPoint(fr)) {
					p.sendMessage("§7Leaving point §3§l" + cp.identifier);
				}
			}
		}
	}

	@EventHandler
	public void onPointCapture(PointCaptureEvent event) {
		ControlPoint cp = event.point;
		DyeColor team = event.team;

		if (current == Minigame.Firefight) {
			respawnTeam(team == PBObjective.GREEN_COLOR ? green : yellow);

			boolean done = true;
			for (PBObjective obj : objs) {
				ControlPoint ocp = (ControlPoint) obj;
				if (ocp.teamOwn != team && !ocp.equals(cp)) {
					done = false;
					break;
				}
			}
			if (done) {
				boolean isGreen = team == PBObjective.GREEN_COLOR;
				MCShockwave.broadcast(isGreen ? ChatColor.DARK_GREEN : ChatColor.YELLOW,
						"%s has won a game of paintball!", isGreen ? "Green" : "Yellow");
				end(isGreen ? "Green" : "Yellow");
			}
		}

		String tnam = team == PBObjective.GREEN_COLOR ? "§2§lGreen" : "§e§lYellow";
		send("§7Team " + tnam + "§7 has captured point §3§l" + cp.identifier);
	}

	public void respawn(Player p) {
		if (p.getGameMode() != GameMode.ADVENTURE) {
			p.setGameMode(GameMode.ADVENTURE);
		}

		p.teleport(getSpawn(p.getName(), 300));
		p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 150, 255));
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 150, 10));
		if (!alternateMap) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
		}

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

		ItemStack gun;
		if (current == Minigame.Gun_Game) {
			gun = Category.Wasted.getGuns()[ggtier.get(p.getName())].getItem();
			Addon.Infinite_Ammo.add(gun);
		} else {
			gun = Gun.WL_P30.getItem();
			Addon.Bottomless_Clip.add(gun);
			Addon.Foregrip.add(gun);
			Addon.Laser_Pointer.add(gun);
		}

		pi.addItem(gun);
	}

	public void send(String msg) {
		send(msg, players);
	}

	public void send(String msg, ArrayList<String> names) {
		for (Player p : getPlayersInList(names)) {
			p.sendMessage(msg);
		}
	}

	public ArrayList<Player> getPlayers(boolean all) {
		if (!all) {
			return getPlayersInList(players);
		} else {
			ArrayList<String> names = new ArrayList<>();
			for (String s : players) {
				names.add(s);
			}
			for (String s : specs) {
				if (!names.contains(s)) {
					names.add(s);
				}
			}
			return getPlayersInList(names);
		}
	}

	public ArrayList<Player> getPlayersInList(ArrayList<String> names) {
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
			false,
			false),
		Grifball(
			true,
			false),
		Capture_the_Flag(
			true,
			false),
		Search_and_Destroy(
			false,
			false),
		Siege(
			true,
			false),
		Gun_Game(
			true,
			false),

		// INS
		Firefight(
			true,
			true,
			0),
		Occupy(
			true,
			true,
			3);

		public boolean	allowRespawn;
		public boolean	altMap;
		public int		wavesStartGrn;
		public int		wavesStartYlw;

		Minigame(boolean allowRespawn, boolean altMap) {
			this.allowRespawn = allowRespawn;
			this.altMap = altMap;
			this.wavesStartGrn = -1;
			this.wavesStartYlw = -1;
		}

		Minigame(boolean allowRespawn, boolean altMap, int wavesStart) {
			this.allowRespawn = allowRespawn;
			this.altMap = altMap;
			this.wavesStartGrn = wavesStart;
			this.wavesStartYlw = wavesStart;
		}

		Minigame(boolean allowRespawn, boolean altMap, int wavesStartGrn, int wavesStartYlw) {
			this.allowRespawn = allowRespawn;
			this.altMap = altMap;
			this.wavesStartGrn = wavesStartGrn;
			this.wavesStartYlw = wavesStartYlw;
		}

		public boolean isWavesMinigame() {
			return wavesStartGrn > -1 && wavesStartYlw > -1;
		}

		@Override
		public String toString() {
			return name().replace('_', ' ');
		}

		public static Minigame getFromString(String s) {
			for (Minigame game : values()) {
				if (game.toString().replace(' ', '_').equalsIgnoreCase(s)) {
					return game;
				}
			}
			return null;
		}
	}

	public static enum GameState {
		NONE(
			ChatColor.WHITE),
		QUEUED(
			ChatColor.LIGHT_PURPLE),
		STARTED(
			ChatColor.DARK_GREEN);

		GameState(ChatColor color) {
			this.color = color;
		}

		ChatColor	color;

		@Override
		public String toString() {
			return color + WordUtils.capitalizeFully(name().replace('_', ' '));
		}
	}

	public void respawnTeam(ArrayList<String> list) {
		for (Player p : getPlayersInList(list)) {
			// if dead
			if (specs.contains(p.getName())) {
				removeSpectator(p, true);
			}
		}
	}

	public void sendPacket(Packet pack) {
		for (Player p : getPlayers(true))
			PacketUtils.sendPacket(p, pack);
	}

	public void playSound(Location l, Sound s, float volume, float pitch) {
		for (Player p : getPlayers(true))
			p.playSound(l, s, volume, pitch);
	}

	// public void playBlockDustParticles(Material m, int data, Location l,
	// float rad, float spd) {
	// for (Player p : getPlayers())
	// PacketUtils.sendPacket(p, PacketUtils.generateBlockDustParticles(m, data,
	// l, rad, spd));
	// }

	public void playParticleEffect(ParticleEffect particle, Location l, float rad, float speed, int amount) {
		for (Player p : getPlayers(true))
			PacketUtils.sendPacket(p, PacketUtils.generateParticles(particle, l, rad, speed, amount));
	}

	public boolean isItem(Entity e) {
		return e.hasMetadata(gameUUID.toString());
	}

	public void setDroppedItem(Item i) {
		for (Paintball pg : games) {
			if (pg != this) {
				for (Player p : pg.getPlayers(true)) {
					PacketPlayOutEntityDestroy dest = new PacketPlayOutEntityDestroy(i.getEntityId());
					PacketUtils.sendPacket(p, dest);
				}
			}
		}

		i.setMetadata(gameUUID.toString(), new FixedMetadataValue(HubPlugin.ins, gameUUID.toString()));
	}

	public Item dropItem(final Location l, ItemStack it) {
		final Item i = l.getWorld().dropItem(l, it);
		i.setVelocity(new Vector());
		Bukkit.getScheduler().runTaskLater(HubPlugin.ins, new Runnable() {
			public void run() {
				i.teleport(l);
			}
		}, 1l);

		setDroppedItem(i);

		return i;
	}

	public Item dropItemNaturally(Location l, ItemStack it) {
		Item i = l.getWorld().dropItemNaturally(l, it);

		setDroppedItem(i);

		return i;
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
		if (alternateMap) {
			return new Location(HubPlugin.endWorld(), -446, 84, -522.5, 0, 0);
		}
		return new Location(HubPlugin.endWorld(), 473.5, y, 467.5, 0, 0);
	}

	public static Location getGreenSpawn(int y) {
		if (alternateMap) {
			return new Location(HubPlugin.endWorld(), -516.5, 84, -455.5, 180, 0);
		}
		return new Location(HubPlugin.endWorld(), 527.5, y, 553.5, 180, 0);
	}

	public static Location getCenter() {
		if (alternateMap) {
			return new Location(HubPlugin.endWorld(), -492.5, 65, -496.5);
		}
		return new Location(HubPlugin.endWorld(), 500.5, 106, 510.5, 0, 0);
	}

	public static ItemMenu getMenu() {
		return getMenu(false);
	}

	public static ItemMenu getMenu(boolean editable) {
		ItemMenu m = new ItemMenu("§3Active Games", games.size() <= 0 ? 1 : games.size());

		int indx = -1;
		for (final Paintball pg : games) {
			indx++;
			List<String> lore = new ArrayList<>();
			lore.addAll(Arrays.asList("§c§o" + pg.current, "§6Max Players: §o" + pg.maxPlayers, pg.state.toString(),
					"", "§7Players:"));
			for (Player p : pg.getPlayers(false)) {
				lore.add((pg.yellow.contains(p.getName()) ? "§e" : pg.green.contains(p.getName()) ? "§2" : "§8") + "§o"
						+ p.getName());
			}
			if (TournamentManager.paintball.containsValue(pg.gameUUID)) {
				lore.add("");
				lore.add("§3§lTournament Game");
			}
			Button b = new Button(false, Material.WOOL, 1, 0, "§eGame #" + (indx + 1), lore.toArray(new String[0]));
			m.addButton(b, indx);

			if (editable) {
				m.addSubMenu(pg.getGameMenu(), b, true);
			} else {
				b.setOnClick(new ButtonRunnable() {
					public void run(Player p, InventoryClickEvent event) {
						if (pg.state == QUEUED) {
							pg.addToQueue(p.getName());
						} else if (pg.state == STARTED) {
							for (Paintball pg : games) {
								if (pg.getPlayers(false).contains(p)) {
									return;
								}
								if (pg.specs.contains(p.getName())) {
									pg.removeSpectator(p, false);
								}
							}
							pg.addSpectator(p, false);
						}
					}
				});
			}
		}

		return m;
	}

	public ItemMenu getGameMenu() {
		ItemMenu m = new ItemMenu("§3Game #" + (games.indexOf(this) + 1), 9);

		Button del = new Button(true, Material.WOOL, 1, 14, "§cDelete Game");
		del.setOnClick(new ButtonRunnable() {
			public void run(Player p, InventoryClickEvent event) {
				if (Paintball.this.state == STARTED) {
					Paintball.this.end(null);
				}
				games.remove(Paintball.this);
			}
		});
		m.addButton(del, 6);

		Button start = new Button(true, Material.WOOL, 1, 5, "§aStart Game");
		start.setOnClick(new ButtonRunnable() {
			public void run(Player p, InventoryClickEvent event) {
				if (Paintball.this.state == QUEUED) {
					Paintball.this.start();
				}
			}
		});
		m.addButton(start, 1);

		return m;
	}

}
