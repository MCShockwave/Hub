package net.mcshockwave.Hub.Commands;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HeadCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player && sender.isOp()) {
			Player p = (Player) sender;
			ItemStack it = p.getItemInHand();
			
			if (it != null && it.getType() == Material.SKULL_ITEM) {
				ItemMetaUtils.setHeadName(it, args[0]);
				p.setItemInHand(it);
			}
		}
		return false;
	}

}
