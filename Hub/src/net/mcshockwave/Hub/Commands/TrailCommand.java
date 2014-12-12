package net.mcshockwave.Hub.Commands;

import java.util.ArrayList;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TrailCommand implements CommandExecutor {

	public static ArrayList<Player>	using	= new ArrayList<>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (SQLTable.hasRank(p.getName(), Rank.NETHER)) {

				if (!using.contains(p)) {
					using.add(p);
					MCShockwave.send(ChatColor.GREEN, p, "Fire trail %s!", "enabled");
				} else {
					using.remove(p);
					MCShockwave.send(ChatColor.RED, p, "Fire trail %s!", "disabled");
				}

			}
		}
		return false;
	}

}
