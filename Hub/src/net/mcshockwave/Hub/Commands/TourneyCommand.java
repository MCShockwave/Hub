package net.mcshockwave.Hub.Commands;

import net.mcshockwave.Hub.Kit.TournamentManager;
import net.mcshockwave.Hub.Kit.Paintball.Minigame;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map.Entry;

import org.json.simple.JSONObject;

public class TourneyCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player && sender.isOp()) {
			Player p = (Player) sender;

			if (args[0].equalsIgnoreCase("game")) {
				if (args.length > 1) {
					TournamentManager.game = Minigame.valueOf(args[1]);
				}
				p.sendMessage("§eCurrent tournament game: §6" + TournamentManager.game);
			}

			if (args[0].equalsIgnoreCase("type")) {
				if (args.length > 1) {
					TournamentManager.tournamentType = args[1].replace("_", " ");
				}
				p.sendMessage("§eCurrent tournament type: §6" + TournamentManager.tournamentType);
			}

			if (args[0].equalsIgnoreCase("players")) {
				TournamentManager.getPlayersMenu().open(p);
			}

			if (args[0].equalsIgnoreCase("prepare")) {
				TournamentManager.prepareTournament(TournamentManager.signedUp.toArray(new String[0]));
			}

			if (args[0].equalsIgnoreCase("start")) {
				TournamentManager.start();
			}

			if (args[0].equalsIgnoreCase("matches")) {
				for (JSONObject obj : TournamentManager.matches) {
					p.sendMessage("§eMatch " + obj.get("identifier") + " §8(id §7" + obj.get("id") + "§8, round §7"
							+ obj.get("round") + "§8)§e is " + obj.get("state"));
				}
			}

			if (args[0].equalsIgnoreCase("participants")) {
				for (Entry<Long, String> par : TournamentManager.participants.entrySet()) {
					p.sendMessage("§6Name: " + par.getValue() + " §eID: " + par.getKey());
				}
			}

			if (args[0].equalsIgnoreCase("nextRound")) {
				TournamentManager.nextRound();
			}

			if (args[0].equalsIgnoreCase("rerandomize")) {
				TournamentManager.rerandomize();
			}

			if (args[0].equalsIgnoreCase("end")) {
				TournamentManager.end();
			}

			if (args[0].equalsIgnoreCase("signups")) {
				TournamentManager.startSignups();
			}
		}
		return false;
	}

}
