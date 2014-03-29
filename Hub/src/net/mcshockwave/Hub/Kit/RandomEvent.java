package net.mcshockwave.Hub.Kit;

import net.mcshockwave.Hub.DefaultListener;
import net.mcshockwave.Hub.HubPlugin;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.LocUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.MCS.Utils.SchedulerUtils;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.WordUtils;

public enum RandomEvent implements Listener {

	// any biome
	AIRSTRIKE,
	WITHER(
		60),
	BIOME_LOCK(
		60),

	// specified biome
	VOLCANO(
		Biome.HELL),
	SWAMP_GAS(
		Biome.SWAMPLAND),
	BLIZZARD(
		Biome.ICE_PLAINS_SPIKES),
	ENDER_SWARM(
		Biome.SKY,
		80);

	static Random			rand		= new Random();

	public static Biome[]	biomes		= { Biome.HELL, Biome.SWAMPLAND, Biome.ICE_PLAINS_SPIKES, Biome.SKY };
	public static String[]	biomeList	= { "Nether", "Swamp", "Ice Spikes", "End" };

	public static int[]		cou			= { 10, 5, 4, 3, 2, 1 };

	public String			name;
	public Biome			bio;
	public int				sec			= -1;

	RandomEvent() {
		name = WordUtils.capitalizeFully(name().replace('_', ' '));
		this.bio = null;
	}

	RandomEvent(int sec) {
		name = WordUtils.capitalizeFully(name().replace('_', ' '));
		this.bio = null;
		this.sec = sec;
	}

	RandomEvent(Biome bio) {
		name = WordUtils.capitalizeFully(name().replace('_', ' '));
		this.bio = bio;
	}

	RandomEvent(Biome bio, int sec) {
		name = WordUtils.capitalizeFully(name().replace('_', ' '));
		this.bio = bio;
		this.sec = sec;
	}

	public void onActivate(Biome b) {
		if (this == AIRSTRIKE) {
			SchedulerUtils u = SchedulerUtils.getNew();

			for (int i = 0; i < 100; i++) {
				final Location m = getRandom(b, 141).getLocation();
				u.add(new Runnable() {
					public void run() {
						final TNTPrimed tnt = (TNTPrimed) m.getWorld().spawnEntity(m, EntityType.PRIMED_TNT);
						tnt.setFuseTicks(80);

						Bukkit.getScheduler().runTaskLater(HubPlugin.ins, new Runnable() {
							public void run() {
								for (Entity e : tnt.getNearbyEntities(5, 5, 5)) {
									if (e instanceof LivingEntity) {
										((LivingEntity) e).damage(10);
									}
								}
							}
						}, 80l);
					}
				});
				u.add(rand.nextInt(5) + 5);
			}

			u.execute();
		}
		if (this == WITHER) {
			SchedulerUtils u = SchedulerUtils.getNew();

			for (int i = 0; i < 40; i++) {
				final Location m = getRandom(b, 141).getLocation();
				u.add(new Runnable() {
					public void run() {
						Skeleton sk = (Skeleton) m.getWorld().spawnEntity(m, EntityType.SKELETON);
						sk.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 10));
						sk.setSkeletonType(SkeletonType.WITHER);

						sk.getEquipment().setItemInHand(
								ItemMetaUtils.addEnchantment(new ItemStack(Material.DIAMOND_SWORD),
										Enchantment.DAMAGE_ALL, 2));
					}
				});
				u.add(rand.nextInt(5) + 10);
			}

			u.execute();
		}

		if (this == VOLCANO) {
			final Location vol = new Location(HubPlugin.dW(), 25.5, 126, -24.5);

			SchedulerUtils u = SchedulerUtils.getNew();

			for (int i = 0; i < 50; i++) {
				u.add(new Runnable() {
					public void run() {
						for (int i = 0; i < 15; i++) {
							Arrow a = vol.getWorld().spawnArrow(vol,
									new Vector(rand.nextGaussian(), 0.5f, rand.nextGaussian()), 1.3f, 20);
							a.setFireTicks(Integer.MAX_VALUE);
						}
					}
				});
				u.add(rand.nextInt(5) + 5);
			}

			u.execute();
		}
		if (this == SWAMP_GAS) {
			SchedulerUtils u = SchedulerUtils.getNew();

			for (int i = 0; i < 100; i++) {
				u.add(new Runnable() {
					public void run() {
						Block b = getRandom(Biome.SWAMPLAND, rand.nextInt(10) + 112);
						PacketUtils.playParticleEffect(ParticleEffect.SLIME, b.getLocation(), 2.5f, 1, 50);
						for (Player p : Bukkit.getOnlinePlayers()) {
							Location l = p.getLocation();
							if (l.getX() > 0 && l.getX() < 50 && l.getZ() > 0 && l.getZ() < 50 && l.getY() < 130) {
								p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
							}
						}
					}
				});
				u.add(rand.nextInt(5) + 5);
			}

			u.execute();
		}
		if (this == BLIZZARD) {
			SchedulerUtils u = SchedulerUtils.getNew();

			for (int i = 0; i < 100; i++) {
				u.add(new Runnable() {
					public void run() {
						Block b = getRandom(Biome.ICE_PLAINS_SPIKES, rand.nextInt(10) + 112);
						PacketUtils.playParticleEffect(ParticleEffect.INSTANT_SPELL, b.getLocation(), 2.5f, 1, 50);
						for (Player p : Bukkit.getOnlinePlayers()) {
							Location l = p.getLocation();
							if (l.getX() > -50 && l.getX() < 0 && l.getZ() > 0 && l.getZ() < 50 && l.getY() < 130) {
								p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
							}
						}
					}
				});
				u.add(rand.nextInt(5) + 5);
			}

			u.execute();
		}
		if (this == ENDER_SWARM) {
			SchedulerUtils u = SchedulerUtils.getNew();

			for (int i = 0; i < 80; i++) {
				final Location m = getRandom(b, 141).getLocation();
				u.add(new Runnable() {
					public void run() {
						Enderman en = (Enderman) m.getWorld().spawnEntity(m, EntityType.ENDERMAN);
						en.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 10));
					}
				});
				u.add(rand.nextInt(5) + 5);
			}

			u.execute();
		}
		if (this == BIOME_LOCK) {
			lock = b;
		}
	}

	public Biome	lock	= null;

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();

		if ((event.getFrom().getBlock().getBiome() == lock && event.getTo().getBlock().getBiome() != lock || event
				.getFrom().getBlock().getBiome() != lock
				&& event.getTo().getBlock().getBiome() == lock)
				&& DefaultListener.isInArena(p)) {
			event.setTo(event.getFrom());

			p.getWorld().playEffect(LocUtils.addRand(p.getEyeLocation().clone(), 3, 3, 3), Effect.MOBSPAWNER_FLAMES, 0);
		}
	}

	public void onDisable(Biome b) {
		if (this == WITHER) {
			for (Entity e : HubPlugin.dW().getEntities()) {
				if (e instanceof Skeleton && ((Skeleton) e).getSkeletonType() == SkeletonType.WITHER) {
					e.remove();
				}
			}
		}
		if (this == ENDER_SWARM) {
			for (Entity e : HubPlugin.dW().getEntities()) {
				if (e instanceof Enderman) {
					e.remove();
				}
			}
		}
		if (this == BIOME_LOCK) {
			lock = null;
		}
	}

	public static void startRandom() {
		startRandom(false);
	}

	public static void startRandom(boolean superE) {
		if (rand.nextInt(50) == 0 || superE) {
			sendToArena("SUPER EVENT MODE!");
			sendToArena("ALL BIOMES HAVE RANDOM, UNKNOWN EVENTS!");
			for (Biome b : biomes) {
				RandomEvent[] res = getForBiome(b);
				RandomEvent re = res[rand.nextInt(res.length)];

				re.startCountdown(b, true);
			}
			startSuperEvent();
			return;
		}

		final RandomEvent event = values()[rand.nextInt(values().length)];
		final Biome b = (event.bio == null) ? (biomes[rand.nextInt(biomes.length)]) : event.bio;
		sendToArena("A new event has been chosen!");
		sendToArena("Event: " + event.name);
		sendToArena("Biome: " + getStringFromBiome(b));

		event.startCountdown(b);
	}

	public void startCountdown(final Biome b) {
		startCountdown(b, false);
	}

	public void startCountdown(final Biome b, final boolean random) {
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
				onActivate(b);
			}
		});
		if (sec > 0) {
			util.add(sec * 20);
			util.add(new Runnable() {
				public void run() {
					onDisable(b);
				}
			});
		}
		util.execute();
	}

	public static void startSuperEvent() {
		SchedulerUtils util = SchedulerUtils.getNew();

		for (int i = 0; i < cou.length; i++) {
			final int del = cou[i];
			util.add(new Runnable() {
				public void run() {
					sendToArena("Super Event starting in " + del + " seconds!");
				}
			});
			int delay = del - (i < cou.length - 1 ? cou[i + 1] : 0);
			util.add(delay * 20);
		}
		util.add(new Runnable() {
			public void run() {
				sendToArena("Super Event has started!");
			}
		});
		util.execute();
	}

	public static RandomEvent[] getForBiome(Biome b) {
		List<RandomEvent> ret = new ArrayList<>();

		for (RandomEvent re : values()) {
			if (re.bio == null || re.bio == b) {
				ret.add(re);
			}
		}

		return ret.toArray(new RandomEvent[0]);
	}

	public Block getRandom(Biome b, int y) {
		int x = rand.nextInt(50);
		int z = rand.nextInt(50);
		if (y < 0) {
			y = rand.nextInt(40) + 112;
		}

		if (b == Biome.ICE_PLAINS_SPIKES) {
			x = -x;
		}
		if (b == Biome.SKY) {
			x = -x;
			z = -z;
		}
		if (b == Biome.HELL) {
			z = -z;
		}

		return HubPlugin.dW().getBlockAt(x, y, z);
	}

	public Block getRandom(Biome b) {
		return getRandom(b, -1);
	}

	public boolean canHappenIn(Biome b) {
		if (bio == null) {
			return true;
		}
		return b == bio;
	}

	public static void sendToArena(String s) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (DefaultListener.isInArena(p)) {
				p.sendMessage("§7[§e§lPVP§7] [§cEvent§7]§f " + s);
			}
		}
	}

	public static String getStringFromBiome(Biome b) {
		for (int i = 0; i < biomes.length; i++) {
			if (biomes[i] == b) {
				return biomeList[i];
			}
		}
		return "Undefined";
	}

}
