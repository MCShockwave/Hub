package net.mcshockwave.Hub.Commands;

import net.mcshockwave.Hub.DefaultListener;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PVPCommand implements CommandExecutor {

	public static Vector	pvp	= new Vector(-0.5, 197, 0.5);
	
	public static Vector	arena	= new Vector(-0.5, 170, -5.5);
	
	public static Vector	arenaPVP = new Vector(-0.5, 112, 0.5);

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (DefaultListener.isInArena(p)) {
				p.sendMessage("§c/pvp is disabled in the PVP arena");
				return false;
			}
			
			p.teleport(vecToLoc(p.getWorld()));
			p.sendMessage(ChatColor.AQUA + "Teleported to the PVP Arena");
			DefaultListener.resetPlayerInv(p);
			p.getInventory().setItem(8,
					ItemMetaUtils.setItemName(new ItemStack(Material.BOOK), "Kit Selector §e(Right click)"));
		}
		return false;
	}

	public static Location vecToLoc(World w) {
		return new Location(w, pvp.getX(), pvp.getY(), pvp.getZ());
	}
	
	public static Location arena(World w) {
		return new Location(w, arena.getX(), arena.getY(), arena.getZ());
	}
	
	public static Location arenaPVP(World w) {
		return new Location(w, arenaPVP.getX(), arenaPVP.getY(), arenaPVP.getZ());
	}
}
