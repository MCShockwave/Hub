package net.mcshockwave.Hub.Commands;

import net.mcshockwave.Hub.DefaultListener;
import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class LoungeCommand implements CommandExecutor {

	public static Vector	lounge	= new Vector(-228.5, 120, 198.5);

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (SQLTable.hasRank(p.getName(), Rank.DIAMOND)) {
				if (DefaultListener.isInArena(p)) {
					p.sendMessage("§c/lounge is disabled in the PVP arena");
					return false;
				}
				DefaultListener.resetPlayerInv(p);
				p.teleport(vecToLoc(p.getWorld()));
				p.sendMessage(ChatColor.AQUA + "Teleported to the VIP Lounge");
			} else
				p.sendMessage(ChatColor.RED
						+ "You must be at least Diamond VIP to do that!\nBuy VIP at buy.mcshockwave.net");
		}
		return false;
	}

	public static Location vecToLoc(World w) {
		return new Location(w, lounge.getX(), lounge.getY(), lounge.getZ());
	}

}
