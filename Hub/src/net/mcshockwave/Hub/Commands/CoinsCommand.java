package net.mcshockwave.Hub.Commands;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoinsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (SQLTable.Coins.has("Username", p.getName()) && args.length == 0) {
				sender.sendMessage(ChatColor.GREEN + "You have " + ChatColor.GOLD
						+ SQLTable.Coins.get("Username", p.getName(), "Amount") + ChatColor.GREEN + " coins");
			}
		}
		if ((sender instanceof Player && SQLTable.hasRank(((Player) sender).getName(), Rank.ADMIN) || !(sender instanceof Player)
				&& sender.isOp())
				&& args.length == 3) {
			if (args[0].equalsIgnoreCase("set")) {
				if (Bukkit.getPlayer(args[1]) != null) {
					SQLTable.Coins.set("Amount", args[2], "Username", args[1]);
					Bukkit.getScoreboardManager().getMainScoreboard().getObjective("Coins")
							.getScore(Bukkit.getPlayer(args[1]))
							.setScore(SQLTable.Coins.getInt("Username", args[1], "Amount"));
				}
			}
//			if (args[0].equalsIgnoreCase("add")) {
//				if (Bukkit.getPlayer(args[1]) != null) {
//					HubPlugin.addPoints(Bukkit.getPlayer(args[1]), Integer.parseInt(args[2]));
//				}
//			}
		}

		return false;
	}

}
