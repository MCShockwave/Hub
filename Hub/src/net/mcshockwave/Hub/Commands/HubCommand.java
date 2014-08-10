package net.mcshockwave.Hub.Commands;

import net.mcshockwave.Hub.HubPlugin;
import net.mcshockwave.Hub.ServerSelector;
import net.mcshockwave.Hub.Kit.Kit;
import net.mcshockwave.Hub.Kit.Paintball;
import net.mcshockwave.Hub.Kit.RandomEvent;
import net.mcshockwave.Hub.Kit.TournamentManager;
import net.mcshockwave.Hub.Kit.Paintball.Minigame;
import net.minecraft.server.v1_7_R4.World;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Map.Entry;

import org.json.simple.JSONObject;

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

			if (args[0].equalsIgnoreCase("tgame")) {
				if (args.length == 1) {
					p.sendMessage("§eCurrent tournament game: §6" + TournamentManager.game);
				} else {
					TournamentManager.game = Minigame.valueOf(args[1]);
				}
			}

			if (args[0].equalsIgnoreCase("tcreate")) {
				TournamentManager.getPrepMenu().open(p);
			}

			if (args[0].equalsIgnoreCase("tstart")) {
				TournamentManager.start();
			}

			if (args[0].equalsIgnoreCase("tmatches")) {
				for (JSONObject obj : TournamentManager.matches) {
					p.sendMessage("§eMatch " + obj.get("identifier") + " §8(id §7" + obj.get("id") + "§8, round §7"
							+ obj.get("round") + "§8)§e is " + obj.get("state"));
				}
			}

			if (args[0].equalsIgnoreCase("tparticipants")) {
				for (Entry<Long, String> par : TournamentManager.participants.entrySet()) {
					p.sendMessage("§6Name: " + par.getValue() + " §eID: " + par.getKey());
				}
			}

			if (args[0].equalsIgnoreCase("tnextRound")) {
				TournamentManager.nextRound();
			}

			if (args[0].equalsIgnoreCase("trerandomize")) {
				TournamentManager.rerandomize();
			}
		}
		return false;
	}

}
