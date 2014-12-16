package net.mcshockwave.Hub.Kit;

import net.mcshockwave.Hub.DefaultListener;
import net.mcshockwave.Hub.HubPlugin;
import net.mcshockwave.MCS.Utils.CooldownUtils;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.LocUtils;
import net.mcshockwave.MCS.Utils.SchedulerUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.WordUtils;

public enum RandomEvent implements Listener {

	AIRSTRIKE,
	VOLCANO,
	ENDER_SWARM(
		80),
	ENDER_DRAGON,
	TORNADO;

	static Random				rand			= new Random();

	// public static Biome[] biomes = { Biome.HELL, Biome.SWAMPLAND,
	// Biome.ICE_PLAINS_SPIKES, Biome.SKY };
	// public static String[] biomeList = { "Nether", "Swamp", "Ice Spikes",
	// "End" };

	public static int[]			cou				= { 10, 5, 4, 3, 2, 1 };

	public String				name;
	public Biome				bio;
	public int					sec				= -1;

	public static BukkitTask	task			= null;

	public static RandomEvent	eventRunning	= null;

	RandomEvent() {
		name = WordUtils.capitalizeFully(name().replace('_', ' '));
		this.bio = null;
	}

	RandomEvent(int sec) {
		name = WordUtils.capitalizeFully(name().replace('_', ' '));
		this.bio = null;
		this.sec = sec;
	}

	// RandomEvent(Biome bio) {
	// name = WordUtils.capitalizeFully(name().replace('_', ' '));
	// this.bio = bio;
	// }
	//
	// RandomEvent(Biome bio, int sec) {
	// name = WordUtils.capitalizeFully(name().replace('_', ' '));
	// this.bio = bio;
	// this.sec = sec;
	// }

	public static void startTask() {
		endTask();
		task = new BukkitRunnable() {
			public void run() {
				if (getInArena().size() > 0 && eventRunning == null && rand.nextInt(8) == 0) {
					startRandom();
				}
			}
		}.runTaskTimer(HubPlugin.ins, 600, 600);
	}

	public static void endTask() {
		if (task != null) {
			task.cancel();
		}
	}

	public static ArrayList<Player> getInArena() {
		ArrayList<Player> inArena = new ArrayList<>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (DefaultListener.isInArena(p)) {
				inArena.add(p);
			}
		}
		return inArena;
	}

	public void onActivate() {
		if (sec != -1) {
			eventRunning = this;
		}
		if (this == AIRSTRIKE) {
			SchedulerUtils u = SchedulerUtils.getNew();

			for (int i = 0; i < 200; i++) {
				final Location m = getRandom(100).getLocation();
				u.add(new Runnable() {
					public void run() {
						final TNTPrimed tnt = (TNTPrimed) m.getWorld().spawnEntity(m, EntityType.PRIMED_TNT);
						tnt.setFuseTicks(80);

						Bukkit.getScheduler().runTaskLater(HubPlugin.ins, new Runnable() {
							public void run() {
								for (Entity e : tnt.getNearbyEntities(5, 5, 5)) {
									if (e instanceof LivingEntity) {
										((LivingEntity) e).damage(10f);
									}
								}
							}
						}, 80l);
					}
				});
				u.add(rand.nextInt(2) + 2);
			}

			u.execute();
		}
		// if (this == WITHER) {
		// SchedulerUtils u = SchedulerUtils.getNew();
		//
		// for (int i = 0; i < 40; i++) {
		// final Location m = getRandom(b, 141).getLocation();
		// u.add(new Runnable() {
		// public void run() {
		// Skeleton sk = (Skeleton) m.getWorld().spawnEntity(m,
		// EntityType.SKELETON);
		// sk.addPotionEffect(new
		// PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 10));
		// sk.setSkeletonType(SkeletonType.WITHER);
		//
		// sk.getEquipment().setItemInHand(
		// ItemMetaUtils.addEnchantment(new ItemStack(Material.DIAMOND_SWORD),
		// Enchantment.DAMAGE_ALL, 2));
		// }
		// });
		// u.add(rand.nextInt(5) + 10);
		// }
		//
		// u.execute();
		// }

		if (this == VOLCANO) {
			final Location vol = new Location(HubPlugin.endWorld(), 12.5, 91, 37);

			SchedulerUtils u = SchedulerUtils.getNew();

			for (int i = 0; i < 200; i++) {
				u.add(new Runnable() {
					public void run() {
						for (int i = 0; i < 30; i++) {
							Arrow a = vol.getWorld().spawnArrow(vol,
									new Vector(rand.nextGaussian() * 2, 0.7f, rand.nextGaussian() * 2), 1.3f, 20);
							a.setFireTicks(Integer.MAX_VALUE);
						}
					}
				});
				u.add(rand.nextInt(3) + 2);
			}

			u.execute();
		}
		// if (this == SWAMP_GAS) {
		// SchedulerUtils u = SchedulerUtils.getNew();
		//
		// for (int i = 0; i < 100; i++) {
		// u.add(new Runnable() {
		// public void run() {
		// Block b = getRandom(Biome.SWAMPLAND, rand.nextInt(10) + 112);
		// PacketUtils.playParticleEffect(ParticleEffect.SLIME, b.getLocation(),
		// 2.5f, 1, 50);
		// for (Player p : Bukkit.getOnlinePlayers()) {
		// Location l = p.getLocation();
		// if (l.getX() > 0 && l.getX() < 50 && l.getZ() > 0 && l.getZ() < 50 &&
		// l.getY() < 130) {
		// p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
		// }
		// }
		// }
		// });
		// u.add(rand.nextInt(5) + 5);
		// }
		//
		// u.execute();
		// }
		// if (this == BLIZZARD) {
		// SchedulerUtils u = SchedulerUtils.getNew();
		//
		// for (int i = 0; i < 100; i++) {
		// u.add(new Runnable() {
		// public void run() {
		// Block b = getRandom(Biome.ICE_PLAINS_SPIKES, rand.nextInt(10) + 112);
		// PacketUtils.playParticleEffect(ParticleEffect.INSTANT_SPELL,
		// b.getLocation(), 2.5f, 1, 50);
		// for (Player p : Bukkit.getOnlinePlayers()) {
		// Location l = p.getLocation();
		// if (l.getX() > -50 && l.getX() < 0 && l.getZ() > 0 && l.getZ() < 50
		// && l.getY() < 130) {
		// p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
		// }
		// }
		// }
		// });
		// u.add(rand.nextInt(5) + 5);
		// }
		//
		// u.execute();
		// }
		if (this == ENDER_SWARM) {
			SchedulerUtils u = SchedulerUtils.getNew();

			for (int i = 0; i < 160; i++) {
				final Location m = getRandom(100).getLocation();
				u.add(new Runnable() {
					public void run() {
						Enderman en = (Enderman) m.getWorld().spawnEntity(m, EntityType.ENDERMAN);
						en.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 10));
					}
				});
				u.add(rand.nextInt(2) + 2);
			}

			u.execute();
		}

		if (this == ENDER_DRAGON) {
			eventRunning = this;
			EnderDragon ed = spawnDragon();

			if (getInArena().size() > 0) {
				Player r = getInArena().get(rand.nextInt(getInArena().size()));
				makeDragonRider(r, ed);
			}
		}

		if (this == TORNADO) {
			for (int i = 0; i < 5; i++) {
				Location l = getRandom().getLocation();
				l.setY(l.getWorld().getHighestBlockYAt(l.getBlockX(), l.getBlockZ()));

				Material m = l.getBlock().getType() == Material.AIR ? l.clone().add(0, -1, 0).getBlock().getType() : l
						.getBlock().getType();

				Vector vel = LocUtils.getVelocity(l, new Location(l.getWorld(), 0, 0, 0)).setY(0);

				Tornado.spawnTornado(HubPlugin.ins, l, m, (byte) 0, vel, 0.2f, 200, 1200);
			}
		}
		// if (this == BIOME_LOCK) {
		// lock = b;
		// }
	}

	public static EnderDragon spawnDragon() {
		EnderDragon ed = (EnderDragon) HubPlugin.endWorld().spawnEntity(getRandom().getLocation(),
				EntityType.ENDER_DRAGON);
		ed.setCustomName("Ender Dragon");
		ed.setHealth(100);
		ed.setMaxHealth(100);
		return ed;
	}

	public static void makeDragonRider(Player r, EnderDragon ed) {
		ed.setPassenger(r);
		sendToArena(r.getName() + " is the Dovah-Zoriik (Dragon Rider)!");

		r.getInventory().clear();
		r.getInventory().setArmorContents(new ItemStack[4]);
		r.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.BLAZE_POWDER), "§6Breath Fire"));
		r.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.DRAGON_EGG), "§5Eggrenade"));
	}

	public Biome	lock	= null;

	// @EventHandler
	// public void onPlayerMove(PlayerMoveEvent event) {
	// Player p = event.getPlayer();
	//
	// if ((event.getFrom().getBlock().getBiome() == lock &&
	// event.getTo().getBlock().getBiome() != lock || event
	// .getFrom().getBlock().getBiome() != lock
	// && event.getTo().getBlock().getBiome() == lock)
	// && DefaultListener.isInArena(p)) {
	// event.setTo(event.getFrom());
	//
	// p.getWorld().playEffect(LocUtils.addRand(p.getEyeLocation().clone(), 3,
	// 3, 3), Effect.MOBSPAWNER_FLAMES, 0);
	// }
	// }

	public void onDisable() {
		eventRunning = null;
		// if (this == WITHER) {
		// for (Entity e : HubPlugin.endWorld().getEntities()) {
		// if (e instanceof Skeleton && ((Skeleton) e).getSkeletonType() ==
		// SkeletonType.WITHER) {
		// e.remove();
		// }
		// }
		// }
		if (this == ENDER_SWARM) {
			for (Entity e : HubPlugin.endWorld().getEntities()) {
				if (e instanceof Enderman) {
					e.remove();
				}
			}
		}
		// if (this == BIOME_LOCK) {
		// lock = null;
		// }
	}

	public static void startRandom() {
		startRandom(false);
	}

	public static void startRandom(boolean superE) {
		// if (rand.nextInt(50) == 0 || superE) {
		// sendToArena("SUPER EVENT MODE!");
		// sendToArena("ALL BIOMES HAVE RANDOM, UNKNOWN EVENTS!");
		// for (Biome b : biomes) {
		// RandomEvent[] res = getForBiome(b);
		// RandomEvent re = res[rand.nextInt(res.length)];
		//
		// re.startCountdown(b, true);
		// }
		// startSuperEvent();
		// return;
		// }

		final RandomEvent event = values()[rand.nextInt(values().length)];
		// final Biome b = (event.bio == null) ?
		// (biomes[rand.nextInt(biomes.length)]) : event.bio;
		sendToArena("A new event has been chosen!");
		sendToArena("Event: " + event.name);
		// sendToArena("Biome: " + getStringFromBiome(b));

		event.startCountdown();
	}

	public void startCountdown() {
		startCountdown(false);
	}

	public void startCountdown(final boolean random) {
		SchedulerUtils util = SchedulerUtils.getNew();

		for (int i = 0; i < cou.length; i++) {
			final int del = cou[i];
			if (!random) {
				util.add(new Runnable() {
					public void run() {
						sendToArena(name + " starting in " + del + " seconds!");
					}
				});
			}
			int delay = del - (i < cou.length - 1 ? cou[i + 1] : 0);
			util.add(delay * 20);
		}
		util.add(new Runnable() {
			public void run() {
				if (!random) {
					sendToArena(name + " has started!");
				}
				onActivate();
			}
		});
		if (sec > 0) {
			util.add(sec * 20);
			util.add(new Runnable() {
				public void run() {
					onDisable();
				}
			});
		}
		util.execute();
	}

	// public static void startSuperEvent() {
	// SchedulerUtils util = SchedulerUtils.getNew();
	//
	// for (int i = 0; i < cou.length; i++) {
	// final int del = cou[i];
	// util.add(new Runnable() {
	// public void run() {
	// sendToArena("Super Event starting in " + del + " seconds!");
	// }
	// });
	// int delay = del - (i < cou.length - 1 ? cou[i + 1] : 0);
	// util.add(delay * 20);
	// }
	// util.add(new Runnable() {
	// public void run() {
	// sendToArena("Super Event has started!");
	// }
	// });
	// util.execute();
	// }

	public static RandomEvent[] getForBiome(Biome b) {
		List<RandomEvent> ret = new ArrayList<>();

		for (RandomEvent re : values()) {
			if (re.bio == null || re.bio == b) {
				ret.add(re);
			}
		}

		return ret.toArray(new RandomEvent[0]);
	}

	public static Block getRandom(int y) {
		World w = HubPlugin.endWorld();
		int rad = 100;
		int x = 0;
		int z = 0;
		do {
			x = rand.nextInt(rad * 2) - rad;
			z = rand.nextInt(rad * 2) - rad;
		} while (!w.getHighestBlockAt(x, z).getType().isTransparent());
		if (y < 0) {
			y = rand.nextInt(100) + 50;
		}

		// if (b == Biome.ICE_PLAINS_SPIKES) {
		// x = -x;
		// }
		// if (b == Biome.SKY) {
		// x = -x;
		// z = -z;
		// }
		// if (b == Biome.HELL) {
		// z = -z;
		// }

		return w.getBlockAt(x, y, z);
	}

	public static Block getRandom() {
		return getRandom(-1);
	}

	// public boolean canHappenIn(Biome b) {
	// if (bio == null) {
	// return true;
	// }
	// return b == bio;
	// }

	public static void sendToArena(String s) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (DefaultListener.isInArena(p)) {
				p.sendMessage("§7[§e§lPVP§7] [§cEvent§7]§f " + s);
			}
		}
	}

	// public static String getStringFromBiome(Biome b) {
	// for (int i = 0; i < biomes.length; i++) {
	// if (biomes[i] == b) {
	// return biomeList[i];
	// }
	// }
	// return "Undefined";
	// }

	@EventHandler
	public void onEntityCreatePortal(EntityCreatePortalEvent event) {
		if (event.getEntityType() == EntityType.ENDER_DRAGON) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.ENDER_DRAGON) {
			EnderDragon ed = (EnderDragon) event.getEntity();
			sendToArena(ed.getCustomName() + " was killed"
					+ (ed.getKiller() == null ? "" : " by " + ed.getKiller().getName()));

			if (eventRunning == ENDER_DRAGON) {
				eventRunning = null;
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		ItemStack it = p.getItemInHand();

		if (p.getVehicle() != null && p.getVehicle().getType() == EntityType.ENDER_DRAGON && it != null) {
			EnderDragon ed = (EnderDragon) p.getVehicle();
			if (it.getType() == Material.BLAZE_POWDER) {

				for (int i = 0; i < 5; i++) {
					FallingBlock fb = ed.getWorld().spawnFallingBlock(ed.getLocation(), Material.FIRE, (byte) 0);
					Vector vel = ed.getLocation().getDirection()
							.add(new Vector(rand.nextGaussian() / 3, 0.4f, rand.nextGaussian() / 3));
					vel.setX(-vel.getX());
					vel.setY(-vel.getY());
					vel.setZ(-vel.getZ());
					fb.setVelocity(vel);
					fb.setDropItem(false);
				}

				ed.getWorld().playSound(ed.getLocation(), Sound.GHAST_FIREBALL, 10, 0);
			}

			if (it.getType() == Material.DRAGON_EGG && !CooldownUtils.isOnCooldown("DragonRiderEgg", p.getName())) {
				CooldownUtils.addCooldown("DragonRiderEgg", p.getName(), 100);

				FallingBlock fb = ed.getWorld().spawnFallingBlock(ed.getLocation(), Material.DRAGON_EGG, (byte) 0);
				fb.setVelocity(new Vector(0, -1, 0));
				fb.setDropItem(false);
			}
		}
	}

	@EventHandler
	public void onBlockFormFromSand(EntityChangeBlockEvent event) {
		if (event.getEntityType() == EntityType.FALLING_BLOCK) {
			FallingBlock fb = (FallingBlock) event.getEntity();
			Material m = event.getTo();
			if (m == Material.FIRE) {
				for (Entity e : event.getEntity().getNearbyEntities(3, 3, 3)) {
					e.setFireTicks(100);
				}
			}
			if (m == Material.DRAGON_EGG) {
				fb.getWorld().createExplosion(fb.getLocation(), 12, true);
			}

			if (DefaultListener.isInArena(event.getEntity())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		Entity e = event.getEntity();
		DamageCause dc = event.getCause();

		if (e.getType() == EntityType.ENDER_DRAGON) {
			if (dc.name().contains("EXPLOSION")) {
				event.setCancelled(true);
			}
		}

		if (e.getVehicle() != null && e.getVehicle().getType() == EntityType.ENDER_DRAGON) {
			if (dc == DamageCause.SUFFOCATION || dc.name().contains("EXPLOSION")) {
				event.setCancelled(true);
			}
		}
	}
}
