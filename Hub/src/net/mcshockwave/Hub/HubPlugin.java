package net.mcshockwave.Hub;

import net.mcshockwave.Hub.Commands.HeadCommand;
import net.mcshockwave.Hub.Commands.HubCommand;
import net.mcshockwave.Hub.Commands.LoungeCommand;
import net.mcshockwave.Hub.Commands.PVPCommand;
import net.mcshockwave.Hub.Commands.PetCommand;
import net.mcshockwave.Hub.Commands.SpawnCommand;
import net.mcshockwave.Hub.Commands.TourneyCommand;
import net.mcshockwave.Hub.Commands.TrailCommand;
import net.mcshockwave.Hub.Kit.RandomEvent;
import net.mcshockwave.Hub.Kit.TournamentManager;
import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Entities.CustomEntityRegistrar;
import net.mcshockwave.MCS.Utils.CommandRegistrar;
import net.mcshockwave.MCS.Utils.MiscUtils;
import net.minecraft.server.v1_7_R4.EntityVillager;
import net.minecraft.server.v1_7_R4.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class HubPlugin extends JavaPlugin {

	public static HubPlugin			ins			= null;

	// public static final Vector[] vLocs = { new Vector(-196.5, 112, 171.5),
	// new Vector(-188.5, 112, 163.5),
	// new Vector(-204.5, 112, 163.5), new Vector(-196.5, 112, 155.5) };
	// public static final String[] vNames = { "Minigames Servers",
	// "Mynerim SG", "Zombiez TD", "Staff Servers" };

	public static final Vector[]	vLocs		= { new Vector(-38.5, 115, 311.5), new Vector(0, 0, 0),
			new Vector(-46.5, 115, 303.5), new Vector(-38.5, 115, 295.5), new Vector(-30.5, 115, 303.5) };
	public static final String[]	vNames		= { "Minigames Servers", "Staff Servers", "Mynerim SG", "Zombiez TD",
			"Adventure Parkour"				};

	public static boolean			saddlemode	= false;
	public static BukkitTask		saddleTask	= null;

	public void onEnable() {
		ins = this;

		Bukkit.getPluginManager().registerEvents(new DefaultListener(this), this);
		Bukkit.getPluginManager().registerEvents(RandomEvent.AIRSTRIKE, this);
		RandomEvent.startTask();

		try {
			CommandRegistrar.registerCommand(ins, TournamentManager.TEAM_BASE_COMMAND.replaceFirst("/", ""));
			for (String s : TournamentManager.cmds) {
				CommandRegistrar.registerCommand(ins, TournamentManager.TEAM_BASE_COMMAND.replaceFirst("/", "") + s);
			}
		} catch (Exception e) {
			MiscUtils.printStackTrace(e);
		}

		getCommand("lounge").setExecutor(new LoungeCommand());
		getCommand("head").setExecutor(new HeadCommand());
		getCommand("pets").setExecutor(new PetCommand());
		getCommand("trail").setExecutor(new TrailCommand());
		getCommand("hc").setExecutor(new HubCommand());
		getCommand("pvp").setExecutor(new PVPCommand());
		getCommand("spawn").setExecutor(new SpawnCommand());
		getCommand("tournament").setExecutor(new TourneyCommand());

		MCShockwave.mesJoin = "";
		MCShockwave.mesKick = "";
		MCShockwave.mesLeave = "";

		for (Player p : Bukkit.getOnlinePlayers()) {
			p.setWalkSpeed(0.2F);
			// setSpeed(p);
		}

		CustomEntityRegistrar.addCustomEntity("Villager", EntityType.VILLAGER, EntityVillager.class,
				ServerSelector.class);

		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				setVils();
			}
		}, 100l);

		advPar().setTime(5000);
		advPar().setGameRuleValue("doDaylightCycle", "false");

		// regSigns();
	}

	// public static void regSigns() {
	// for (Kit k : Kit.values()) {
	// final Kit k2 = k;
	// new CustomSign("§2Kit:", k.name().replace('_', ' '), "§8Click to",
	// "§8Use", "[Kit]", k.name(), null, null)
	// .onClick(new SignRunnable() {
	// public void run(Player p, Sign s, PlayerInteractEvent event) {
	// p.teleport(PVPCommand.arena(dW()));
	// p.sendMessage("§aEntering arena");
	//
	// k2.use(p);
	//
	// petApi.removePet(p, false, false);
	// }
	// });
	// }
	// }

	public static void setVils() {
		for (Entity e : dW().getEntities()) {
			if (e.getType() == EntityType.VILLAGER) {
				e.remove();
			}
		}

		for (int i = 0; i < vLocs.length; i++) {
			Location l = new Location(dW(), vLocs[i].getX(), vLocs[i].getY(), vLocs[i].getZ());
			String n = vNames[i];

			WorldServer ws = ((CraftWorld) l.getWorld()).getHandle();

			ServerSelector ss = new ServerSelector(ws, Profession.BUTCHER, l.getBlockX(), l.getBlockY(), l.getBlockZ());
			ss.setPosition(l.getX(), l.getY(), l.getZ());
			ws.addEntity(ss);

			ss.setCustomName(n);
			ss.setCustomNameVisible(true);
		}
	}

	public static World advPar() {
		World w = Bukkit.getWorld("HubWorldHalloween");
		if (w == null) {
			w = Bukkit.createWorld(new WorldCreator("HubWorldHalloween"));
		}
		return w;
	}

	public static World dW() {
		return Bukkit.getWorld("HubWorld");
	}

	public void onDisable() {
		for (Entity e : dW().getEntities()) {
			if (e.getType() == EntityType.VILLAGER) {
				e.remove();
			}
		}

		for (Block b : DefaultListener.medic.values()) {
			b.setType(Material.AIR);
			b.getRelative(BlockFace.DOWN).setType(Material.AIR);
		}

		for (Block b : DefaultListener.engineer.values()) {
			b.setType(Material.AIR);
			b.getRelative(BlockFace.DOWN).setType(Material.AIR);
		}

		for (ArrayList<Item> is : DefaultListener.demoman.values()) {
			for (Item it : is) {
				it.remove();
			}
		}

		for (LivingEntity le : DefaultListener.pets.values()) {
			le.remove();
		}
	}

	public static World endWorld() {
		return Bukkit.getWorld(dW().getName() + "_the_end");
	}

	// public static void addPoints(Player p, int amount) {
	// SQLTable.Coins.set("Amount", (SQLTable.Coins.getInt("Username",
	// p.getName(), "Amount") + amount) + "",
	// "Username", p.getName());
	// p.sendMessage(ChatColor.GREEN + "+" + amount + " Coins");
	// Bukkit.getScoreboardManager().getMainScoreboard().getObjective("Coins").getScore(p)
	// .setScore(SQLTable.Coins.getInt("Username", p.getName(), "Amount"));
	// }

	// public static UUID uuidSpeed = UUID.randomUUID();
	//
	// public static void setSpeed(LivingEntity le) {
	// EntityInsentient nmsE = (EntityInsentient) ((CraftLivingEntity)
	// le).getHandle();
	//
	// AttributeInstance ai = nmsE.getAttributeInstance(GenericAttributes.d);
	// AttributeModifier modifier = new AttributeModifier(uuidSpeed,
	// "My Modifier", 1.1d, 1);
	// ai.b(modifier);
	// }

}
