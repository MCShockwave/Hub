package net.mcshockwave.Hub.Commands;

import net.mcshockwave.Hub.HubPlugin;
import net.mcshockwave.Hub.ServerSelector;
import net.mcshockwave.Hub.Kit.Kit;
import net.mcshockwave.Hub.Kit.RandomEvent;
import net.mcshockwave.Hub.Kit.Paintball.Paintball;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.minecraft.server.v1_7_R4.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_7_R4.World;

import org.bukkit.Bukkit;
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

			if (args[0].equalsIgnoreCase("paintball")) {
				Paintball pg = Paintball.newGame(Paintball.Minigame.getFromString(args[1]),
						args.length > 2 ? Integer.parseInt(args[2]) : 10);
				pg.queue(true, true);
			}

			if (args[0].equalsIgnoreCase("pmenu")) {
				Paintball.getMenu(true).open(p);
			}

			if (args[0].equalsIgnoreCase("endPaintball")) {
				for (Paintball pg : Paintball.games) {
					pg.end(null);
				}
			}

			if (args[0].equalsIgnoreCase("dj")) {
				PacketPlayOutNamedSoundEffect music = new PacketPlayOutNamedSoundEffect(args[1], -33.5, 96, 35.5, 4, 1);
				for (Player p2 : Bukkit.getOnlinePlayers()) {
					PacketUtils.sendPacket(p2, music);
				}
			}
		}
		return false;
	}

}
