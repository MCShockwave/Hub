package net.mcshockwave.Hub;

import net.mcshockwave.Guns.Gun;
import net.mcshockwave.Hub.Commands.LoungeCommand;
import net.mcshockwave.Hub.Commands.PVPCommand;
import net.mcshockwave.Hub.Commands.TrailCommand;
import net.mcshockwave.Hub.Kit.Kit;
import net.mcshockwave.Hub.Kit.TournamentManager;
import net.mcshockwave.Hub.Kit.Paintball.Paintball;
import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.MCS.Challenges.Challenge.ChallengeModifier;
import net.mcshockwave.MCS.Challenges.ChallengeManager;
import net.mcshockwave.MCS.Currency.PointsUtils;
import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Menu.ItemMenu.Button;
import net.mcshockwave.MCS.Menu.ItemMenu.ButtonRunnable;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.LocUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.MCS.Utils.PetMaker;
import net.mcshockwave.MCS.Utils.SchedulerUtils;
import net.minecraft.util.com.google.common.io.ByteArrayDataOutput;
import net.minecraft.util.com.google.common.io.ByteStreams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;

public class DefaultListener implements Listener {

	HubPlugin	plugin;

	public DefaultListener(HubPlugin instance) {
		plugin = instance;
	}

	public static HashMap<UUID, LivingEntity>	pets	= new HashMap<>();

	Random										rand	= new Random();

	ArrayList<Player>							froz	= new ArrayList<Player>();

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		p.teleport(HubPlugin.dW().getSpawnLocation().add(0, 5, 0));
		p.setHealth(20f);
		p.setFoodLevel(20);
		p.setSaturation(2);
		p.setGameMode(GameMode.ADVENTURE);
		resetPlayerInv(p);
		if (MCShockwave.pointmult > 1 || MCShockwave.xpmult > 1) {
			MCShockwave.send(ChatColor.AQUA, p, ChatColor.BOLD + "Network Multipliers are currently %s!",
					"§lactive§r§7");
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		for (Currency c : Currency.values()) {
			if (!c.table.has("Username", p.getName())) {
				c.table.add("Username", p.getName(), c.column, "0");
			}
		}
		if (!SQLTable.MynerimItems.has("Username", p.getName())) {
			SQLTable.MynerimItems.add("Username", p.getName(), "Extra_Shout", "0");
		}

		if (pets.containsKey(p.getUniqueId())) {
			pets.get(p.getUniqueId()).remove();
			pets.remove(p.getUniqueId());
		}

		if (Paintball.getGame(p.getName()) != null) {
			Paintball.getGame(p.getName()).leave(p.getName());
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (event.getPlayer().getPassenger() != null) {
			event.getPlayer().setPassenger(null);
		}
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		Player p = event.getPlayer();

		if (pets.containsKey(p.getUniqueId())) {
			pets.get(p.getUniqueId()).remove();
			pets.remove(p.getUniqueId());
		}

		if (Paintball.getGame(p.getName()) != null) {
			Paintball.getGame(p.getName()).leave(p.getName());
		}
	}

	@EventHandler
	public void onHangingBreak(HangingBreakEvent event) {
		if (event.getCause() != RemoveCause.PHYSICS) {
			event.setCancelled(true);
		}
	}

	public static void giveHelm(Player p) {
		Color c = Color.GRAY;
		if (!SQLTable.Youtubers.has("Username", p.getName())) {
			if (SQLTable.hasRank(p.getName(), Rank.GOLD)) {
				c = Color.YELLOW;
			}
			if (SQLTable.hasRank(p.getName(), Rank.DIAMOND)) {
				c = Color.AQUA;
			}
			if (SQLTable.hasRank(p.getName(), Rank.EMERALD)) {
				c = Color.GREEN;
			}
			if (SQLTable.hasRank(p.getName(), Rank.OBSIDIAN)) {
				c = Color.PURPLE;
			}
			if (SQLTable.hasRank(p.getName(), Rank.NETHER)) {
				c = Color.fromRGB(128, 0, 0);
			}
			if (SQLTable.hasRank(p.getName(), Rank.ENDER)) {
				c = Color.BLACK;
			}
			if (SQLTable.hasRank(p.getName(), Rank.JR_MOD)) {
				c = Color.ORANGE;
			}
			if (SQLTable.hasRank(p.getName(), Rank.SR_MOD)) {
				c = Color.fromRGB(89, 220, 227);
			}
			if (SQLTable.hasRank(p.getName(), Rank.ADMIN)) {
				c = Color.RED;
			}
		} else
			c = Color.fromRGB(200, 0, 0);
		p.getInventory().setHelmet(ItemMetaUtils.setLeatherColor(new ItemStack(Material.LEATHER_HELMET), c));
	}

	HashMap<Player, Long>							coolBROD	= new HashMap<>();
	HashMap<Player, Long>							coolSTAR	= new HashMap<>();
	HashMap<Player, Long>							coolSHEA	= new HashMap<>();
	HashMap<Player, Long>							coolDISA	= new HashMap<>();
	HashMap<Player, Long>							coolSHOT	= new HashMap<>();
	HashMap<Player, Long>							coolDEMO	= new HashMap<>();
	HashMap<Player, Long>							coolDETO	= new HashMap<>();
	HashMap<Player, Long>							coolPYRO	= new HashMap<>();

	public static HashMap<Player, ArrayList<Item>>	demoman		= new HashMap<>();

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player p = event.getPlayer();
		Action a = event.getAction();
		ItemStack it = p.getItemInHand();

		if (a == Action.RIGHT_CLICK_BLOCK) {
			Block b = event.getClickedBlock();
			if (b.getType() == Material.WALL_SIGN) {
				Sign s = (Sign) b.getState();
				if (s.getLine(0).equalsIgnoreCase("You win!") && s.getLine(1).equalsIgnoreCase("Click here to")
						&& s.getLine(2).equalsIgnoreCase("get your reward")) {
					String[] l3 = s.getLine(3).replace("[", "").replace("]", "").split(" ");
					PointsUtils.addPoints(p, Integer.parseInt(l3[0]), "beating the Hub parkour!");
					p.teleport(p.getWorld().getSpawnLocation());
				}

				if (s.getLine(1).equalsIgnoreCase("Click here to") && s.getLine(2).equalsIgnoreCase("hear the music!")) {
					p.setResourcePack("http://mcsw.us/HubMusic.zip");
				}
				if (s.getLine(1).equalsIgnoreCase("Click here to") && s.getLine(2).equalsIgnoreCase("stop the music")) {
					p.setResourcePack("");
				}
				if (ChatColor.stripColor(s.getLine(1)).equalsIgnoreCase("adventure")
						&& ChatColor.stripColor(s.getLine(2)).equalsIgnoreCase("parkour")) {
					p.teleport(new Location(HubPlugin.dW(), 500.5, 202, 500.5));
				}
			}
		}
		if (it.getType() == Material.EYE_OF_ENDER) {
			event.setCancelled(true);

			ItemMenu im = MCShockwave.getServerMenu(p);
			im.open(p);
		}

		if (isInArena(p)) {
			if (it.getType() == Material.BOW
					&& a == Action.LEFT_CLICK_AIR
					&& (coolSHOT.containsKey(p) && coolSHOT.get(p) <= System.currentTimeMillis() || !coolSHOT
							.containsKey(p))) {
				for (int i = 0; i < 5; i++) {
					Arrow ar = p.launchProjectile(Arrow.class);

					float mult = 0.3f;
					Vector v = new Vector(rand.nextGaussian() * mult, rand.nextGaussian() * mult, rand.nextGaussian()
							* mult);
					ar.setVelocity(ar.getVelocity().multiply(1.3).add(v));
				}

				coolSHOT.remove(p);
				coolSHOT.put(p, System.currentTimeMillis() + 2000);
			}

			if (it.getType() == Material.BLAZE_ROD
					&& (coolBROD.containsKey(p) && coolBROD.get(p) <= System.currentTimeMillis() || !coolBROD
							.containsKey(p))) {

				int ticksLive = 15;
				for (int i = 0; i < 4; i++) {
					final SmallFireball fb = p.launchProjectile(SmallFireball.class);
					fb.setVelocity(p.getLocation().getDirection().multiply(1));
					Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
						public void run() {
							fb.remove();
						}
					}, ticksLive);
				}

				coolBROD.remove(p);
				coolBROD.put(p, System.currentTimeMillis() + 400);
			}

			if (it.getType() == Material.NETHER_STAR
					&& (coolSTAR.containsKey(p) && coolSTAR.get(p) <= System.currentTimeMillis() || !coolSTAR
							.containsKey(p))) {
				final Item i = p.getWorld().dropItem(p.getEyeLocation(), new ItemStack(Material.NETHER_STAR));
				i.setPickupDelay(Integer.MAX_VALUE);
				Vector vel = p.getLocation().getDirection();
				vel.setY(vel.getY() + 0.3f);
				i.setVelocity(vel);

				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						Location l = i.getLocation();
						i.remove();

						l.getWorld().createExplosion(l, 5f);
						for (Entity e : i.getNearbyEntities(5, 5, 5)) {
							if (e instanceof LivingEntity && e != p) {
								LivingEntity le = (LivingEntity) e;

								le.damage(rand.nextInt(8) + 10f, p);
							}
						}
					}
				}, 40l);

				coolSTAR.remove(p);
				coolSTAR.put(p, System.currentTimeMillis() + 5000);
			}

			if (it.getType() == Material.SHEARS
					&& a.name().contains("RIGHT_CLICK")
					&& p.hasPotionEffect(PotionEffectType.INVISIBILITY)
					&& (coolSHEA.containsKey(p) && coolSHEA.get(p) <= System.currentTimeMillis() || !coolSHEA
							.containsKey(p))) {
				p.removePotionEffect(PotionEffectType.INVISIBILITY);
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 110, 1));
				p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 110, 0));
				p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 110, -10));

				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						if (isInArena(p)) {
							p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
						}
					}
				}, 100);

				coolSHEA.remove(p);
				coolSHEA.put(p, System.currentTimeMillis() + 20000);
			}

			if (it.getType() == Material.GLOWSTONE_DUST
					&& (coolDISA.containsKey(p) && coolDISA.get(p) <= System.currentTimeMillis() || !coolDISA
							.containsKey(p))) {
				for (int i = 0; i < 5; i++) {
					PacketUtils
							.playBlockParticles(Material.GLOWSTONE, 0, LocUtils.addRand(p.getEyeLocation(), 5, 5, 5));
				}

				for (Entity e : p.getNearbyEntities(5, 5, 5)) {
					if (e instanceof Player) {
						final Player np = (Player) e;

						if (p.getItemInHand() != null && p.getItemInHand().getType() != Material.AIR) {
							final int sl = np.getInventory().getHeldItemSlot();
							final ItemStack iih = np.getItemInHand().clone();
							np.setItemInHand(null);

							Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
								public void run() {
									np.getInventory().setItem(sl, iih);
								}
							}, 40);
						}
					}
				}

				int am = it.getAmount() - 1;
				if (am > 0) {
					it.setAmount(am);
					p.setItemInHand(it);
				} else {
					p.setItemInHand(null);
				}

				coolDISA.remove(p);
				coolDISA.put(p, System.currentTimeMillis() + 5000);
			}

			if (it.getType() == Material.IRON_BARDING || it.getType() == Material.GOLD_BARDING) {
				if (a.name().contains("LEFT_CLICK")
						&& demoman.containsKey(p)
						&& (coolDETO.containsKey(p) && coolDETO.get(p) <= System.currentTimeMillis() || !coolDETO
								.containsKey(p))) {

					SchedulerUtils ut = SchedulerUtils.getNew();
					ut.add(p);

					final ArrayList<Item> deto = demoman.get(p);
					for (Item i2 : demoman.get(p).toArray(new Item[0])) {
						if (i2.getFireTicks() > 0) {
							continue;
						}
						deto.remove(i2);

						final Item i = i2;
						ut.add(new Runnable() {
							public void run() {
								i.getWorld().createExplosion(i.getLocation(), 3f);

								for (Entity e : i.getNearbyEntities(3, 3, 3)) {
									if (e instanceof LivingEntity) {
										((LivingEntity) e).damage(rand.nextInt(10) + 5f, p);
									}
								}

								i.remove();
							}
						});
						ut.add(1);
					}
					ut.add(new Runnable() {
						public void run() {
							demoman.remove(p);
							demoman.put(p, deto);
						}
					});

					ut.execute();
				}

				if (a.name().contains("RIGHT_CLICK")
						&& (coolDEMO.containsKey(p) && coolDEMO.get(p) <= System.currentTimeMillis() || !coolDEMO
								.containsKey(p))) {

					Item i = p.getWorld().dropItem(p.getEyeLocation(), new ItemStack(Material.TNT));
					i.setPickupDelay(Integer.MAX_VALUE);
					i.setFireTicks(10);
					Vector vel = p.getLocation().getDirection().multiply(1.5);
					vel.setY(vel.getY() + 0.1f);
					i.setVelocity(vel);

					if (!demoman.containsKey(p)) {
						demoman.put(p, new ArrayList<Item>());
					}
					demoman.get(p).add(i);

					if (it.getType() == Material.IRON_BARDING) {
						coolDEMO.remove(p);
						coolDEMO.put(p, System.currentTimeMillis() + 3000);
					}
				}
			}

			if (it.getType() == Material.FLINT_AND_STEEL
					&& (coolPYRO.containsKey(p) && coolPYRO.get(p) <= System.currentTimeMillis() || !coolPYRO
							.containsKey(p))) {
				for (Entity e : p.getNearbyEntities(5, 5, 5)) {
					e.setFireTicks(e.getFireTicks() + 100);
					PacketUtils.playParticleEffect(ParticleEffect.FLAME, e.getLocation(), 0, 0.3f, 15);
					PacketUtils.playParticleEffect(ParticleEffect.LAVA, e.getLocation(), 0, 0.3f, 15);
				}

				PacketUtils.playParticleEffect(ParticleEffect.FLAME, p.getLocation(), 0, 0.3f, 15);
				PacketUtils.playParticleEffect(ParticleEffect.LAVA, p.getLocation(), 0, 0.3f, 15);

				coolPYRO.remove(p);
				coolPYRO.put(p, System.currentTimeMillis() + 3000);
			}
		} else {
			if (it.getType() == Material.BOOK) {
				Kit.getSelectorMenu(p).open(p);
			}
		}

		final Material[] ms = { Material.COOKED_CHICKEN, Material.BOW, Material.BEACON, Material.DISPENSER,
				Material.ENDER_PEARL };

		if (p.getGameMode() != GameMode.CREATIVE && !(Arrays.asList(ms).contains(it.getType()))
				&& event.getAction() != Action.PHYSICAL) {
			event.setCancelled(true);
		} else
			event.setCancelled(false);
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		String msg = event.getMessage();
		String[] args = msg.split(" ");
		String cmd = args[0].toLowerCase();

		if (cmd.equalsIgnoreCase(Paintball.cmdMenu)) {
			Paintball.getMenu().open(p);
			event.setCancelled(true);
		}

		if (cmd.equalsIgnoreCase(TournamentManager.SIGNUPS_COMMAND)) {
			if (TournamentManager.signups) {
				if (TournamentManager.teams_enabled) {
					if (TournamentManager.getTeam(p.getName()) == null
							|| TournamentManager.teams.get(TournamentManager.getTeam(p.getName())).indexOf(p.getName()) != 0) {
						p.sendMessage("§cYou are not the leader of the team or you are not in a team");
						return;
					}
					TournamentManager.signupTeam(TournamentManager.getTeam(p.getName()));
				} else
					TournamentManager.signupPlayer(p.getName());
			} else {
				p.sendMessage("§cSignups are not open!");
			}
			event.setCancelled(true);
		}

		if (msg.toLowerCase().startsWith(TournamentManager.TEAM_BASE_COMMAND.toLowerCase())) {
			TournamentManager.teamCmd(p, args);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		event.getEntity().remove();
	}

	public static HashMap<Player, Block>			medic		= new HashMap<>();
	public static HashMap<Player, SchedulerUtils>	medicTask	= new HashMap<>();

	public static HashMap<Player, Block>			engineer	= new HashMap<>();
	public static HashMap<Player, SchedulerUtils>	turretTask	= new HashMap<>();

	public boolean isInTube(Location l) {
		return false;
		// return l.getX() < 3 && l.getZ() < 3 && l.getX() > -3 && l.getZ() >
		// -3;
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		final Player p = event.getPlayer();
		final Block b = event.getBlock();

		if (p.getGameMode() != GameMode.CREATIVE
				&& p.getWorld().getSpawnLocation().distanceSquared(p.getLocation()) < 200 * 200) {
			if (b.getType() == Material.BEACON) {
				event.setCancelled(true);

				if (medic.containsKey(p)) {
					return;
				}

				// if (b.getType() != Material.AIR && b.getType() !=
				// Material.BEACON
				// || b.getRelative(BlockFace.UP).getType() != Material.AIR ||
				// !isInArena(p)
				// ||
				// b.getLocation().distanceSquared(PVPCommand.arenaPVP(b.getWorld()))
				// < 8 * 8
				// || isInTube(b.getLocation())) {
				// p.sendMessage("§cInvalid healer location");
				// return;
				// }

				p.getInventory().remove(Material.BEACON);

				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						b.getRelative(BlockFace.UP).setType(Material.BEACON);
						b.setType(Material.FENCE);
					}
				}, 2l);

				medic.put(p, b.getRelative(BlockFace.UP));

				final Location bl = b.getLocation().add(0.5, 1.5, 0.5);

				final int totalTicks = 600;
				final int tickUpdate = 5;

				final int minDis = 8;

				SchedulerUtils sc = SchedulerUtils.getNew();

				sc.add(p);
				sc.add("§aPlaced healer");
				for (int i = 0; i < totalTicks; i += tickUpdate) {
					sc.add(new Runnable() {
						public void run() {
							double disSq = p.getLocation().distanceSquared(bl);
							if (disSq < minDis * minDis) {
								Location pl = p.getEyeLocation().add(0, -1, 0);

								Vector pv = new Vector(pl.getX(), pl.getY(), pl.getZ());
								Vector bv = new Vector(bl.getX(), bl.getY(), bl.getZ());

								Vector fv = bv.subtract(pv).multiply(0.1);

								pl.add(fv);
								for (int i = 0; i < 100; i++) {
									pl.add(fv);
									PacketUtils.playParticleEffect(ParticleEffect.HEART, pl, 0.05f, 1, 1);
									if (pl.distanceSquared(bl) < 1) {
										break;
									}
								}

								p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 3));
							}
						}
					});
					sc.add(tickUpdate);
				}
				sc.add(new Runnable() {
					public void run() {
						medic.remove(p);
						bl.getBlock().setType(Material.AIR);
						bl.getBlock().getRelative(BlockFace.DOWN).setType(Material.AIR);
					}
				});
				sc.add(200);
				sc.add(new Runnable() {
					public void run() {
						p.getInventory().addItem(new ItemStack(Material.BEACON));
						medicTask.remove(p);
					}
				});

				sc.execute();

				medicTask.put(p, sc);
			}

			if (b.getType() == Material.DISPENSER) {
				event.setCancelled(true);

				if (engineer.containsKey(p)) {
					return;
				}

				// if (b.getType() != Material.AIR && b.getType() !=
				// Material.DISPENSER
				// || b.getRelative(BlockFace.UP).getType() != Material.AIR ||
				// !isInArena(p)
				// ||
				// b.getLocation().distanceSquared(PVPCommand.arenaPVP(b.getWorld()))
				// < 8 * 8
				// || isInTube(b.getLocation())) {
				// p.sendMessage("§cInvalid turret location");
				// return;
				// }

				p.getInventory().remove(Material.DISPENSER);

				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						b.getRelative(BlockFace.UP).setType(Material.DISPENSER);
						b.setType(Material.FENCE);
					}
				}, 2l);

				engineer.put(p, b.getRelative(BlockFace.UP));

				final Location bl = b.getLocation().add(0.5, 1.5, 0.5);

				final Item ne = bl.getWorld().dropItem(bl, new ItemStack(Material.ARROW));
				ne.setPickupDelay(Integer.MAX_VALUE);
				ne.remove();

				final int totalTicks = 600;
				final int tickUpdate = 20;

				final int minDis = 8;

				SchedulerUtils sc = SchedulerUtils.getNew();

				sc.add(p);
				sc.add("§aPlaced turret");
				for (int i = 0; i < totalTicks; i += tickUpdate) {
					sc.add(new Runnable() {
						public void run() {
							ne.teleport(bl);

							for (Entity e : ne.getNearbyEntities(minDis, minDis, minDis)) {
								if (e instanceof Player && e != p) {
									Player p2 = (Player) e;

									p2.getWorld().playSound(bl, Sound.SHOOT_ARROW, 1, 1);

									Location pl = p2.getEyeLocation();

									Vector pv = new Vector(pl.getX(), pl.getY(), pl.getZ());
									Vector bv = new Vector(bl.getX(), bl.getY(), bl.getZ());

									Vector fv = pv.subtract(bv);

									Arrow a = p2.getWorld().spawnArrow(bl.clone().add(fv.clone().multiply(0.3)),
											fv.clone().add(new Vector(0, 0.1, 0)), 2f, 20f);
									a.setShooter(p);
									a.setMetadata("TurretArrow", new FixedMetadataValue(plugin, true));

									break;
								}
							}
						}
					});
					sc.add(tickUpdate);
				}
				sc.add(new Runnable() {
					public void run() {
						engineer.remove(p);
						bl.getBlock().setType(Material.AIR);
						bl.getBlock().getRelative(BlockFace.DOWN).setType(Material.AIR);
					}
				});
				sc.add(300);
				sc.add(new Runnable() {
					public void run() {
						if (p.getWorld().getEnvironment() == Environment.THE_END) {
							p.getInventory().addItem(new ItemStack(Material.DISPENSER));
						}
						turretTask.remove(p);
					}
				});

				sc.execute();

				turretTask.put(p, sc);
			}
		}
	}

	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if (pets.containsValue(event.getEntity())) {
			event.setTarget(null);
		}
	}

	@EventHandler
	public void noFireballFire(BlockIgniteEvent event) {
		if (event.getCause().equals(IgniteCause.FIREBALL))
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
	}

	public static enum Currency {
		Points(
			SQLTable.Points,
			"Points"),
		Credits(
			SQLTable.Zombiez,
			"Credits"),
		XP(
			SQLTable.Level,
			"XP");

		public String	column;
		public SQLTable	table;

		Currency(SQLTable table, String column) {
			this.column = column;
			this.table = table;
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory i = event.getInventory();
		HumanEntity he = event.getWhoClicked();
		ItemStack cu = event.getCurrentItem();
		if (he instanceof Player) {
			final Player p = (Player) he;

			if (p.getGameMode() != GameMode.CREATIVE) {
				event.setCancelled(true);
			}

			if (i.getName().equalsIgnoreCase("Pets")) {
				EntityType petType = EntityType.fromId(cu.getDurability());

				if (pets.containsKey(p.getUniqueId())) {
					pets.get(p.getUniqueId()).remove();
					pets.remove(p.getUniqueId());
				}

				if (petType != null) {
					LivingEntity pet = (LivingEntity) p.getWorld().spawnEntity(p.getLocation(), petType);
					pet.setCustomNameVisible(true);
					pet.setCustomName(p.getName()
							+ (p.getName().endsWith("s") ? "'" : "'s")
							+ " "
							+ (petType == EntityType.OCELOT ? "Cat" : WordUtils.capitalizeFully(petType.name().replace(
									'_', ' '))));
					PetMaker.makePet(pet, p.getUniqueId());

					if (pet instanceof Ocelot) {
						((Ocelot) pet).setCatType(Type.BLACK_CAT);
					}

					pets.put(p.getUniqueId(), pet);
				}

				event.setCancelled(true);
				p.closeInventory();
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity().getType() == EntityType.VILLAGER && event.getCause() != DamageCause.MAGIC) {
			event.setCancelled(true);
		}

		if (event.getCause() == DamageCause.PROJECTILE) {
			event.setCancelled(false);
			return;
		}

		if (event.getEntityType() == EntityType.PLAYER) {
			Player p = (Player) event.getEntity();
			for (Paintball pg : Paintball.games) {
				if (pg.specs.contains(p.getName())) {
					event.setCancelled(true);
					return;
				}
			}
			if ((!isInArena(p) || event.getCause().name().contains("EXPLOSION"))
					&& event.getCause() != DamageCause.ENTITY_ATTACK) {
				event.setCancelled(true);
			} else {
				resetDurability(p);
			}
		}

		if (pets.containsValue(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		final Entity ee = event.getEntity();
		Entity de = event.getDamager();

		if (ee instanceof Player && de instanceof Player) {
			Player p = (Player) ee;
			Player d = (Player) de;

			if (d.getGameMode() == GameMode.CREATIVE) {
				event.setCancelled(false);
				return;
			}

			if (isInArena(p) && isInArena(d)) {
				resetDurability(p);

				ItemStack it = d.getItemInHand();

				if (it.getType() == Material.SHEARS) {
					event.setDamage(event.getDamage() + 7);
				}

				if (it.getType() == Material.GOLD_AXE) {
					event.setDamage(event.getDamage() + (p.getFireTicks() > 0 ? 2 : 0));
				}
			}
		}

		if (de instanceof SmallFireball) {
			event.setDamage(3f);
			if (ee.getFireTicks() < 10) {
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						ee.setFireTicks(100);
					}
				}, 1l);
			}
		}

		if (de.hasMetadata("TurretArrow")) {
			event.setDamage(event.getDamage() - 1);
		}
	}

	public static boolean isInArena(Entity e) {
		if (!PVPCommand.isEnabled()) {
			return false;
		}

		return e.getWorld().getEnvironment() == Environment.THE_END;

		// int rad = 50;
		// return
		// e.getLocation().distanceSquared(PVPCommand.arenaPVP(e.getWorld())) <
		// rad * rad;
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		event.setDroppedExp(0);

		if (event.getEntityType() == EntityType.ENDERMAN) {
			event.getDrops().add(new ItemStack(Material.ENDER_PEARL));
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();

		// if (event.getTo().getY() < 108 && p.getGameMode() !=
		// GameMode.CREATIVE) {
		// event.setTo(p.getWorld().getSpawnLocation());
		// }

		if (froz.contains(event.getPlayer()) && !sameCoords(event)) {
			event.setCancelled(true);
		}

		event.getPlayer().setFoodLevel(20);

		Block b = event.getTo().getBlock().getRelative(BlockFace.DOWN);
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE && b.getType() == Material.SANDSTONE
				&& b.getData() == 2) {
			event.setTo(LoungeCommand.vecToLoc(event.getTo().getWorld()));
		}

		if (TrailCommand.using.contains(p)) {
			PacketUtils.playParticleEffect(ParticleEffect.FLAME, p.getLocation(), 0.3f, 0.05f, 3);
		}

		// if (rand.nextInt(10000) == 0 && isInArena(p)) {
		// RandomEvent.startRandom();
		// }

		if (event.getTo().getY() <= 0) {
			p.teleport(p.getWorld().getSpawnLocation().add(0, 5, 0));
		}
	}

	public static boolean sameCoords(PlayerMoveEvent event) {
		Location t = event.getTo();
		Location f = event.getFrom();
		if (f.getX() == t.getX() && f.getY() == t.getY() && f.getZ() == t.getZ()) {
			return true;
		}
		return false;
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player p = event.getPlayer();
		Item i = event.getItem();
		ItemStack it = i.getItemStack();
		if (ItemMetaUtils.hasCustomName(it) && ItemMetaUtils.getItemName(it).equalsIgnoreCase("BoomItem")) {
			i.remove();
			p.getWorld().playSound(p.getLocation(), Sound.EXPLODE, 3, 1);
			p.setVelocity(p.getVelocity().add(new Vector(0, 3, 0)));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		event.blockList().clear();
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();
		if (Paintball.getGame(p.getName()) != null) {
			Paintball.getGame(p.getName()).onDeath(p, event.getDeathMessage());
		} else {
			if (isInArena(p)) {
				for (Player p2 : Bukkit.getOnlinePlayers()) {
					if (isInArena(p2)) {
						p2.sendMessage("§7[§e§lPVP§7]§f " + event.getDeathMessage());
					}
				}
				if (p.getKiller() != null) {
					ChallengeManager
							.incrChallenge(ChallengeModifier.Hub_Kills, null, p.getKiller().getName(), 1, false);
				}
			}
		}

		if (isInArena(p)) {
			try {
				if (p.getKiller() != null) {
					String display = "§o" + p.getName();
					PacketUtils.playTitle(p.getKiller(), 0, 2, 13, "", "§7Killed §6" + display);
					PacketUtils.playTitle(p, 3, 10, 10, "§6" + p.getKiller().getName(), "§7§oKilled You");
				} else {
					PacketUtils.playTitle(p, 3, 10, 10, "§6Nobody", "§7§oKilled You");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		event.setDeathMessage("");

		if (medic.containsKey(p)) {
			medic.get(p).setType(Material.AIR);
			medic.get(p).getRelative(BlockFace.DOWN).setType(Material.AIR);
			medic.remove(p);

			if (medicTask.containsKey(p)) {
				medicTask.get(p).terminate();
				medicTask.remove(p);
			}
		}

		if (engineer.containsKey(p)) {
			engineer.get(p).setType(Material.AIR);
			engineer.get(p).getRelative(BlockFace.DOWN).setType(Material.AIR);
			engineer.remove(p);

			if (turretTask.containsKey(p)) {
				turretTask.get(p).terminate();
				turretTask.remove(p);
			}
		}

		if (demoman.containsKey(p)) {
			ArrayList<Item> is = demoman.get(p);
			for (Item i : is) {
				i.remove();
			}
			demoman.remove(p);
		}

		PlayerRespawnEvent ev = new PlayerRespawnEvent(p, HubPlugin.dW().getSpawnLocation(), false);
		Bukkit.getPluginManager().callEvent(ev);
		p.setHealth(p.getMaxHealth());
		p.teleport(ev.getRespawnLocation());
		p.setFireTicks(0);
		for (PotionEffect pe : p.getActivePotionEffects()) {
			p.removePotionEffect(pe.getType());
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		final Player p = event.getPlayer();
		if (Paintball.getGame(p.getName()) != null) {
			Paintball.getGame(p.getName()).onRespawn(p, event);
		} else {
			if (isInArena(p)) {
				event.setRespawnLocation(HubPlugin.dW().getSpawnLocation());
				resetPlayerInv(p);
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						Kit.clearPE(p);
					}
				}, 10l);

				p.getInventory().setItem(8,
						ItemMetaUtils.setItemName(new ItemStack(Material.BOOK), "Kit Selector §e(Right click)"));
			}
		}
	}

	public static void resetDurability(Player p) {
		PlayerInventory pi = p.getInventory();

		for (ItemStack it : pi.getContents()) {
			if (it != null && it.getType() != Material.AIR && Gun.fromItem(it) == null) {
				if (it.getDurability() > 0 && it.getType().getMaxDurability() > 16) {
					it.setDurability((short) 0);
				}
			}
		}
		for (ItemStack it : pi.getArmorContents()) {
			if (it != null && it.getType() != Material.AIR) {
				if (it.getDurability() > 0) {
					it.setDurability((short) 0);
				}
			}
		}

		p.updateInventory();
	}

	public static void resetPlayerInv(Player p) {
		p.getInventory().setArmorContents(null);
		if (SQLTable.hasRank(p.getName(), Rank.IRON) || SQLTable.hasRank(p.getName(), Rank.YOUTUBE)) {
			giveHelm(p);
		}
		p.getInventory().clear();
		int o = 0;
		if (SQLTable.MiscItems.has("Username", p.getName())) {
			o = SQLTable.MiscItems.getInt("Username", p.getName(), "Red_Boots");
		} else {
			SQLTable.MiscItems.add("Username", p.getName());
		}
		if (o == 1) {
			ItemStack it = new ItemStack(Material.LEATHER_BOOTS);
			ItemMetaUtils.setLeatherColor(it, Color.RED);
			ItemMetaUtils.setItemName(it, ChatColor.RED + "Red Boots!!");
			p.getInventory().setBoots(it);
			p.getInventory().setBoots(it);
		}
		p.getInventory().addItem(
				ItemMetaUtils.setItemName(new ItemStack(Material.EYE_OF_ENDER), ChatColor.BLUE
						+ "MCShockwave Servers (Right click)"));
		Kit.clearPE(p);
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Player p = event.getPlayer();
		Entity rc = event.getRightClicked();

		if (rc.getType() == EntityType.VILLAGER) {
			Villager ss = (Villager) rc;
			event.setCancelled(true);

			if (ss.getCustomName() != null) {
				String n = ss.getCustomName();

				if (n.equalsIgnoreCase("Minigames Servers")) {
					getMenuMG().open(p);
				}
				if (n.equalsIgnoreCase("Mynerim SG")) {
					getMenuMSG().open(p);
				}
				if (n.equalsIgnoreCase("Zombiez TD")) {
					getMenuZTD().open(p);
				}
				if (n.equalsIgnoreCase("Staff Servers")) {
					getMenuStaff().open(p);
				}
				if (n.equalsIgnoreCase("Adventure Parkour")) {
					getMenuAdvPar().open(p);
				}
				// if (n.equalsIgnoreCase("Battle Bane")) {
				// getMenuBB().open(p);
				// }
			}
		}
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		final Player p = e.getPlayer();
		if (SQLTable.hasRank(p.getName(), Rank.ADMIN)) {
			return;
		}
		new BukkitRunnable() {
			public void run() {
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("IP");
				p.sendPluginMessage(MCShockwave.instance, "BungeeCord", out.toByteArray());
			}
		}.runTaskLater(MCShockwave.instance, 3L);
	}

	public ItemMenu getMenuAdvPar() {
		ItemMenu adv = new ItemMenu("Adventure Parkour", 9);

		Button tp = new Button(true, Material.EYE_OF_ENDER, 1, 0, "Adventure Parkour", "Click to TP");
		adv.addButton(tp, 4);
		tp.setOnClick(new ButtonRunnable() {
			public void run(Player p, InventoryClickEvent event) {
				p.teleport(new Location(HubPlugin.advPar(), 500, 200, 500));
			}
		});

		return adv;
	}

	public ItemMenu getMenuMG() {
		ItemMenu mg = new ItemMenu("MCMinigames Servers", 9);

		Button mg1 = new Button(true, Material.DIAMOND_SWORD, 1, 0, "MCMinigames Server 1", "Click to join server", "",
				"Players: " + MCShockwave.getPlayerCount("MG1") + " / 30");
		Button mg2 = new Button(true, Material.DIAMOND_SWORD, 2, 0, "MCMinigames Server 2", "Click to join server", "",
				"Players: " + MCShockwave.getPlayerCount("MG2") + " / 30");
		Button mg3 = new Button(true, Material.DIAMOND_SWORD, 3, 0, "MCMinigames Server 3", "Click to join server", "",
				"Players: " + MCShockwave.getPlayerCount("MG3") + " / 30");

		mg1.setOnClick(new ButtonRunnable() {
			public void run(Player p, InventoryClickEvent e) {
				MCShockwave.connectToServer(p, "mg1", "MCMinigames Server 1");
			}
		});
		mg2.setOnClick(new ButtonRunnable() {
			public void run(Player p, InventoryClickEvent e) {
				MCShockwave.connectToServer(p, "mg2", "MCMinigames Server 2");
			}
		});
		mg3.setOnClick(new ButtonRunnable() {
			public void run(Player p, InventoryClickEvent e) {
				MCShockwave.connectToServer(p, "mg3", "MCMinigames Server 3");
			}
		});

		mg.addButton(mg1, 2);
		mg.addButton(mg2, 4);
		mg.addButton(mg3, 6);

		return mg;
	}

	public ItemMenu getMenuStaff() {
		ItemMenu st = new ItemMenu("Staff Servers", 9);

		Button build = new Button(true, Material.GRASS, 1, 0, "Building Server", "Click to join server",
				"§cBUILDERS ONLY", "", "Players: " + MCShockwave.getPlayerCount("build") + " / -");
		Button test = new Button(true, Material.DIAMOND, 1, 0, "Testing Server", "Click to join server",
				"§cBUILDERS ONLY", "", "Players: " + MCShockwave.getPlayerCount("test") + " / -");
		Button event = new Button(true, Material.GOLDEN_APPLE, 1, 1, "Event Server", "Click to join server",
				"Note: only open when we", "are doing an event!", "", "Players: " + MCShockwave.getPlayerCount("event")
						+ " / -");

		build.setOnClick(new ButtonRunnable() {
			public void run(Player p, InventoryClickEvent e) {
				MCShockwave.connectToServer(p, "build", "Building Server");
			}
		});
		test.setOnClick(new ButtonRunnable() {
			public void run(Player p, InventoryClickEvent e) {
				MCShockwave.connectToServer(p, "test", "Testing Server");
			}
		});
		event.setOnClick(new ButtonRunnable() {
			public void run(Player p, InventoryClickEvent e) {
				MCShockwave.connectToServer(p, "event", "Event Server");
			}
		});

		st.addButton(build, 2);
		st.addButton(test, 4);
		st.addButton(event, 6);

		return st;
	}

	public ItemMenu getMenuMSG() {
		ItemMenu sg = new ItemMenu("Mynerim SG", 9);

		Button sg1 = new Button(true, Material.DRAGON_EGG, 1, 0, "Mynerim SG", "Click to join server", "", "Players: "
				+ MCShockwave.getPlayerCount("mynerim") + " / 32");

		sg1.setOnClick(new ButtonRunnable() {
			public void run(Player p, InventoryClickEvent event) {
				MCShockwave.connectToServer(p, "mynerim", "Mynerim SG");
			}
		});

		sg.addButton(sg1, 4);

		return sg;
	}

	public ItemMenu getMenuZTD() {
		ItemMenu ztd = new ItemMenu("Zombiez TD", 9);

		Button ztd1 = new Button(true, Material.ROTTEN_FLESH, 1, 0, "Zombiez TD", "Click to join server", "",
				"Players: " + MCShockwave.getPlayerCount("ZTD") + " / 50");

		ztd1.setOnClick(new ButtonRunnable() {
			public void run(Player p, InventoryClickEvent event) {
				MCShockwave.connectToServer(p, "ZTD", "Zombiez TD");
			}
		});

		ztd.addButton(ztd1, 4);

		return ztd;

		/*
		 * public ItemMenu getMenuBB() { ItemMenu bb = new
		 * ItemMenu("Battle Bane", 9);
		 * 
		 * Button bb1 = new Button(true, Material.NETHER_STAR, 1, 0,
		 * "Battle Bane", "Click to join server", "", "Players: " +
		 * MCShockwave.getPlayerCount("bane") + " / 80");
		 * 
		 * bb1.setOnClick(new ButtonRunnable() { public void run(Player p,
		 * InventoryClickEvent event) { MCShockwave.connectToServer(p, "bane",
		 * "Battle Bane"); } });
		 * 
		 * bb.addButton(bb1, 4);
		 * 
		 * return bb; }
		 */
	}
}
