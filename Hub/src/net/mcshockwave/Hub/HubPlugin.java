package net.mcshockwave.Hub;

import net.mcshockwave.Hub.Commands.HeadCommand;
import net.mcshockwave.Hub.Commands.HubCommand;
import net.mcshockwave.Hub.Commands.LoungeCommand;
import net.mcshockwave.Hub.Commands.PVPCommand;
import net.mcshockwave.Hub.Commands.PetCommand;
import net.mcshockwave.Hub.Commands.TrailCommand;
import net.mcshockwave.Hub.Kit.Kit;
import net.mcshockwave.Hub.Kit.RandomEvent;
import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Entities.CustomEntityRegistrar;
import net.mcshockwave.MCS.Utils.CustomSignUtils.CustomSign;
import net.mcshockwave.MCS.Utils.CustomSignUtils.SignRunnable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;

import io.github.dsh105.echopet.api.EchoPetAPI;

public class HubPlugin extends JavaPlugin {
	
	public static HubPlugin ins = null;

	public static EchoPetAPI		petApi	= null;

	public static final Vector[]	vLocs	= { new Vector(-196.5, 112, 171.5), new Vector(-188.5, 112, 163.5),
			new Vector(-204.5, 112, 163.5), new Vector(-196.5, 112, 155.5) };
	public static final String[]	vNames	= { "Minigames Servers", "Mynerim SG", "Zombiez TD", "Staff Servers" };

	public void onEnable() {
		ins = this;
		
		Bukkit.getPluginManager().registerEvents(new DefaultListener(this), this);
		Bukkit.getPluginManager().registerEvents(RandomEvent.BIOME_LOCK, this);

		getCommand("lounge").setExecutor(new LoungeCommand());
		getCommand("head").setExecutor(new HeadCommand());
		getCommand("pets").setExecutor(new PetCommand());
		getCommand("trail").setExecutor(new TrailCommand());
		getCommand("hc").setExecutor(new HubCommand());
		getCommand("pvp").setExecutor(new PVPCommand());

		MCShockwave.mesJoin = "";
		MCShockwave.mesKick = "";
		MCShockwave.mesLeave = "";

		for (Player p : Bukkit.getOnlinePlayers()) {
			p.setWalkSpeed(0.2F);
			// setSpeed(p);
		}

		petApi = EchoPetAPI.getAPI();

		CustomEntityRegistrar.addCustomEntity(ServerSelector.class, "Villager", EntityType.VILLAGER);

		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				setVils();
			}
		}, 10l);

		regSigns();
	}

	public static void regSigns() {
		for (Kit k : Kit.values()) {
			final Kit k2 = k;
			new CustomSign("§2Kit:", k.name().replace('_', ' '), "§8Click to", "§8Use", "[Kit]", k.name(), null, null)
					.onClick(new SignRunnable() {
						public void run(Player p, Sign s, PlayerInteractEvent event) {
							p.teleport(PVPCommand.arena(dW()));
							p.sendMessage("§aEntering arena");

							k2.use(p);
							
							petApi.removePet(p, false, false);
						}
					});
		}
	}

	public static void setVils() {
		for (int i = 0; i < vLocs.length; i++) {
			Location l = new Location(dW(), vLocs[i].getX(), vLocs[i].getY(), vLocs[i].getZ());
			String n = vNames[i];

			net.minecraft.server.v1_7_R1.World w = ((CraftWorld) l.getWorld()).getHandle();
			@SuppressWarnings("deprecation")
			ServerSelector ent = new ServerSelector(w, Profession.BUTCHER.getId(), l.getBlockX(), l.getBlockY(),
					l.getBlockZ());
			ent.setLocation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
			w.addEntity(ent);

			ent.setCustomName(n);
			ent.setCustomNameVisible(true);
		}
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
