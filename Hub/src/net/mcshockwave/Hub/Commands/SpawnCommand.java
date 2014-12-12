package net.mcshockwave.Hub.Commands;

import net.mcshockwave.Hub.DefaultListener;
import net.mcshockwave.Hub.HubPlugin;
import net.mcshockwave.Hub.Kit.Paintball.Paintball;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			for (Paintball pg : Paintball.games) {
				if (pg.specs.contains(p.getName())) {
					pg.removeSpectator(p, false);
					return true;
				}
			}
			if (DefaultListener.isInArena(p)) {
				p.sendMessage("§c/spawn is disabled in the PVP arena");
				return false;
			}
			DefaultListener.resetPlayerInv(p);
			p.teleport(HubPlugin.dW().getSpawnLocation());
			p.sendMessage(ChatColor.AQUA + "Teleported to spawn");
			return true;
		}
		return false;
	}

}
