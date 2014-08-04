package net.mcshockwave.Hub.Commands;

import net.mcshockwave.Hub.HubPlugin;
import net.mcshockwave.Hub.ServerSelector;
import net.mcshockwave.Hub.Kit.Kit;
import net.mcshockwave.Hub.Kit.RandomEvent;
import net.minecraft.server.v1_7_R4.World;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class HubCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player && sender.isOp()) {
			Player p = (Player) sender;

			if (args[0].equalsIgnoreCase("vil")) {
				Location loc = p.getLocation();
				World w = ((CraftWorld) p.getWorld()).getHandle();
				ServerSelector ent = new ServerSelector(w, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
				ent.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
				w.addEntity(ent);
			}

			if (args[0].equalsIgnoreCase("resetVillagers")) {
				for (Entity e : HubPlugin.dW().getEntities()) {
					if (e.getType() == EntityType.VILLAGER) {
						e.remove();
					}
				}

				HubPlugin.setVils();
			}

			if (args[0].equalsIgnoreCase("event")) {
				RandomEvent.startRandom();
			}
			if (args[0].equalsIgnoreCase("sEvent")) {
				RandomEvent.startRandom(true);
			}

			if (args[0].equalsIgnoreCase("spawnpoint")) {
				Location l = p.getLocation();
				p.getWorld().setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
			}
			
			if (args[0].equalsIgnoreCase("gunmode")) {
				Kit.toggleGunMode();
			}
		}
		return false;
	}

}
