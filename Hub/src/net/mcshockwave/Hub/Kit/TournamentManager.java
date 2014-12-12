package net.mcshockwave.Hub.Kit;

import net.mcshockwave.Hub.HubPlugin;
import net.mcshockwave.Hub.Kit.Paintball.Paintball;
import net.mcshockwave.Hub.Kit.Paintball.Paintball.Minigame;
import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Menu.ItemMenu.Button;
import net.mcshockwave.MCS.Menu.ItemMenu.ButtonRunnable;
import net.minecraft.util.org.apache.commons.codec.binary.Base64;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TournamentManager {

	public static final String								CHALLONGE_API_KEY	= "KSltuaCt3HcRECfW5JDKh8PgwEa7Jv4sYBBqrKy8";

	public static final String								CHALLONGE_API_URL	= "https://api.challonge.com/v1/%s";

	public static final String								TOURNAMENT_FORMAT	= "PAINTBALL%s";

	public static boolean									running				= false;

	public static int										id					= -1;

	public static String									tournamentURL, tournamentType;

	public static HashMap<Long, String>						participants		= new HashMap<>();

	public static List<JSONObject>							matches				= new ArrayList<>();

	public static HashMap<Long, UUID>						paintball			= new HashMap<>();

	public static Minigame									game				= Minigame.Elimination;

	public static boolean									signups				= false;
	public static List<String>								signedUp			= new ArrayList<>();
	public static final String								SIGNUPS_COMMAND		= "/signup";

	public static boolean									teams_enabled		= false;
	public static final HashMap<UUID, ArrayList<String>>	teams				= new HashMap<>();
	public static final String								TEAM_BASE_COMMAND	= "/team";
	public static final String								TEAM_JOIN			= "join", TEAM_INVITE = "invite",
			TEAM_CREATE = "create", TEAM_LIST = "list", TEAM_LIST_ALL = "listall", TEAM_KICK = "kick",
			TEAM_LEAVE = "leave";
	public static int										team_limit			= 3;

	public static final String[]							cmds				= { TEAM_CREATE, TEAM_INVITE,
			TEAM_JOIN, TEAM_KICK, TEAM_LEAVE, TEAM_LIST, TEAM_LIST_ALL			};

	public static final HashMap<String, ArrayList<String>>	invites				= new HashMap<>();

	public static void startSignups() {
		signups = true;
		MCShockwave.broadcastAll(MCShockwave.getBroadcastMessage(ChatColor.DARK_GREEN,
				"Signups for the %s tournament have opened!", "paintball"), MCShockwave.getBroadcastMessage(
				ChatColor.DARK_AQUA, "Type %s to sign up!", SIGNUPS_COMMAND));
	}

	public static void signupTeam(UUID team) {
		if (signedUp.contains(team.toString())) {
			return;
		}
		signedUp.add(team.toString());
		String owner = teams.get(team).get(0);
		MCShockwave.broadcast(ChatColor.DARK_AQUA, "%s" + (owner.endsWith("s") ? "'" : "'s")
				+ " team has signed up for the tournament!", owner);
	}

	public static void signupPlayer(String player) {
		if (signedUp.contains(player)) {
			return;
		}
		signedUp.add(player);
		MCShockwave.broadcast("%s has signed up for the tournament!", player);
	}

	public static UUID getTeamFromParticipantName(String name) {
		for (Entry<UUID, ArrayList<String>> ent : teams.entrySet()) {
			if (getTeamParticipantName(ent.getKey()).equals(name)) {
				return ent.getKey();
			}
		}
		return null;
	}

	public static UUID getTeam(String pl) {
		for (Entry<UUID, ArrayList<String>> ent : teams.entrySet()) {
			if (ent.getValue().contains(pl)) {
				return ent.getKey();
			}
		}
		return null;
	}

	public static List<Player> getPlayersForTeam(UUID team) {
		List<Player> ret = new ArrayList<>();
		for (String s : teams.get(team)) {
			if (Bukkit.getPlayer(s) != null) {
				ret.add(Bukkit.getPlayer(s));
			}
		}
		return ret;
	}

	public static void messageTeam(UUID team, String msg) {
		for (Player p : getPlayersForTeam(team)) {
			p.sendMessage(msg);
		}
	}

	public static String getTeamParticipantName(UUID team) {
		String ret = "";
		for (String s : teams.get(team)) {
			ret += s + ", ";
		}
		if (ret.length() > 2) {
			ret = ret.substring(0, ret.length() - 2);
		}
		return ret;
	}

	public static void prepareTournament(final String... players) {
		signups = false;
		new BukkitRunnable() {
			public void run() {
				id = rand.nextInt(1000000);
				tournamentURL = new BigInteger(65, rand).toString(35).toUpperCase();
				createNewTournament(String.format(TOURNAMENT_FORMAT, id), tournamentURL, tournamentType);
				if (teams_enabled) {
					for (UUID team : teams.keySet()) {
						addParticipant(getTeamParticipantName(team).replace(' ', '+'));
					}
				} else {
					for (String s : players) {
						addParticipant(s);
					}
				}
				updateParticipants();
			}
		}.runTaskAsynchronously(HubPlugin.ins);
	}

	public static void start() {
		running = true;
		signedUp.clear();
		new BukkitRunnable() {
			public void run() {
				post("tournaments/%s/start.json", "", false);
				updateMatches();
			}
		}.runTaskAsynchronously(HubPlugin.ins);
	}

	public static void end() {
		new BukkitRunnable() {
			public void run() {
				post("tournaments/%s/finalize.json", "", false);
			}
		}.runTaskAsynchronously(HubPlugin.ins);
		running = false;
		id = -1;
		tournamentURL = null;
		participants.clear();
		matches.clear();
		paintball.clear();
	}

	public static Long getParticipantID(String name) {
		long id = -1;
		for (Entry<Long, String> ent : participants.entrySet()) {
			if (ent.getValue().equalsIgnoreCase(name)) {
				id = ent.getKey();
				break;
			}
		}
		return id;
	}

	public static void onWin(String pl, final Paintball game) {
		if (teams_enabled && Bukkit.getPlayer(pl) != null) {
			pl = getTeam(pl).toString();
		}
		long id = -1;
		for (Entry<Long, UUID> ent : paintball.entrySet()) {
			if (ent.getValue().equals(game.gameUUID)) {
				id = ent.getKey();
				break;
			}
		}
		if (id != -1) {
			final long idf = id;
			final String plf2 = pl;
			if (teams_enabled) {
				pl = getTeamParticipantName(UUID.fromString(pl));
			}
			final String plf = pl;
			new BukkitRunnable() {
				public void run() {
					String csv = plf2.equalsIgnoreCase(game.p1) ? "1-0" : "0-1";
					post("tournaments/%s/matches/" + idf + ".json", "match[scores_csv]=" + csv + "&match[winner_id]="
							+ getParticipantID(plf), true);
					updateMatches();
				}
			}.runTaskAsynchronously(HubPlugin.ins);
			MCShockwave.broadcast(ChatColor.DARK_GREEN, "%s has won a %s tournament match!", pl, "paintball");
		}
	}

	public static void nextRound() {
		new BukkitRunnable() {
			public void run() {
				updateParticipants();
				updateMatches();
			}
		}.runTaskAsynchronously(HubPlugin.ins);
		new BukkitRunnable() {
			public void run() {
				MCShockwave.broadcast(ChatColor.DARK_GREEN, "The next round of the %s tournament has started!",
						"paintball");
				long round = Long.MIN_VALUE;
				for (JSONObject obj : matches) {
					if ((round == Long.MIN_VALUE || (long) obj.get("round") == round)
							&& obj.get("state").equals("open")) {
						round = (long) obj.get("round");
						long id1 = (long) obj.get("player1_id");
						long id2 = (long) obj.get("player2_id");

						String p1 = participants.get(Long.valueOf(id1));
						String p2 = participants.get(Long.valueOf(id2));

						MCShockwave.broadcast("Match §e" + obj.get("identifier") + "§7: %s VS. %s", p1, p2);

						if (teams_enabled) {
							Paintball pb = Paintball.newGame(game, teams.get(getTeamFromParticipantName(p1)).size()
									+ teams.get(getTeamFromParticipantName(p2)).size());
							paintball.put((Long) obj.get("id"), pb.gameUUID);
							pb.queue(false, false);

							pb.p1 = getTeamFromParticipantName(p1).toString();
							pb.p2 = getTeamFromParticipantName(p2).toString();

							if (getPlayersForTeam(UUID.fromString(pb.p1)).size() < 1) {
								onWin(pb.p2, pb);
								pb.end(null);
								continue;
							}
							if (getPlayersForTeam(UUID.fromString(pb.p2)).size() < 1) {
								onWin(pb.p1, pb);
								pb.end(null);
								continue;
							}

							for (Player p : getPlayersForTeam(UUID.fromString(pb.p1))) {
								pb.addToQueue(p.getName());
								pb.green.add(p.getName());
							}
							for (Player p : getPlayersForTeam(UUID.fromString(pb.p2))) {
								pb.addToQueue(p.getName());
								pb.yellow.add(p.getName());
							}

							pb.start(false);
						} else {
							Paintball pb = Paintball.newGame(game, 2);
							paintball.put((Long) obj.get("id"), pb.gameUUID);
							pb.queue(true, false);

							pb.p1 = p1;
							pb.p2 = p2;

							if (Bukkit.getPlayer(p1) == null) {
								onWin(p2, pb);
								pb.end(null);
								continue;
							}
							if (Bukkit.getPlayer(p2) == null) {
								onWin(p1, pb);
								pb.end(null);
								continue;
							}

							pb.addToQueue(p1);
							pb.addToQueue(p2);
						}
					}
				}
			}
		}.runTaskLater(HubPlugin.ins, 100);
	}

	static Random	rand	= new Random();

	public static void createNewTournament(String name, String url, String tournamentType) {
		post("tournaments.json", "tournament[name]=" + name + "&tournament[url]=" + url
				+ "&tournament[tournament_type]=" + tournamentType, false);
	}

	public static void addParticipant(String name) {
		post("tournaments/%s/participants.json", "participant[name]=" + name, false);
	}

	public static void updateMatches() {
		matches.clear();
		JSONArray arr = get("tournaments/%s/matches.json");
		for (int i = 0; i < arr.size(); i++) {
			matches.add((JSONObject) ((JSONObject) arr.get(i)).get("match"));
		}
	}

	public static void updateParticipants() {
		participants.clear();
		JSONArray arr = get("tournaments/%s/participants.json");
		for (int i = 0; i < arr.size(); i++) {
			JSONObject obj = (JSONObject) ((JSONObject) arr.get(i)).get("participant");
			participants.put(Long.parseLong(obj.get("id") + ""), obj.get("name") + "");
		}
	}

	public static void rerandomize() {
		post("tournaments/%s/participants/randomize.format", "", false);
	}

	public static void teamCmd(Player p, String[] args) {
		String cmd = args[0];
		String subcmd = cmd.replaceFirst(TEAM_BASE_COMMAND, "");

		if (subcmd.equalsIgnoreCase(TEAM_LIST)) {
			if (getTeam(p.getName()) == null) {
				p.sendMessage("§cYou are not in a team!");
				return;
			}

			p.sendMessage("§e§oTeam ID: " + getTeam(p.getName()));
			for (String s : teams.get(getTeam(p.getName()))) {
				p.sendMessage((Bukkit.getPlayer(s) != null ? "§a" : "§c") + "§o" + s);
			}
		}

		if (subcmd.equalsIgnoreCase(TEAM_LIST_ALL)) {
			for (Entry<UUID, ArrayList<String>> tems : teams.entrySet()) {
				p.sendMessage("§e§oTeam ID: " + tems.getKey());
				for (String s : tems.getValue()) {
					p.sendMessage((Bukkit.getPlayer(s) != null ? "§a" : "§c") + "§o" + s);
				}
			}
		}

		if (!signups || !teams_enabled) {
			p.sendMessage("§cTeams are closed");
			return;
		}

		if (cmd.equalsIgnoreCase(TEAM_BASE_COMMAND)) {
			p.sendMessage("§eCommand list:");
			for (String s : cmds) {
				p.sendMessage("§6" + TEAM_BASE_COMMAND + s);
			}
		}

		if (subcmd.equalsIgnoreCase(TEAM_CREATE)) {
			if (getTeam(p.getName()) != null) {
				teams.remove(getTeam(p.getName()));
			}

			UUID team = UUID.randomUUID();
			teams.put(team, new ArrayList<>(Arrays.asList(p.getName())));

			p.sendMessage("§6Successfully created a team with UUID §o" + team);
			p.sendMessage("§eInvite people with " + TEAM_BASE_COMMAND + TEAM_INVITE + " [player]");
		}

		if (subcmd.equalsIgnoreCase(TEAM_INVITE) && args.length > 1) {
			String invite = args[1];

			if (getTeam(p.getName()) == null || teams.get(getTeam(p.getName())).indexOf(p.getName()) != 0) {
				p.sendMessage("§cYou are not the leader of the team or you are not in a team");
				return;
			}
			if (teams.get(getTeam(p.getName())).size() >= team_limit) {
				p.sendMessage("§cTeam is too full!");
				return;
			}
			if (Bukkit.getPlayer(invite) != null) {
				Player p2 = Bukkit.getPlayer(invite);
				p2.sendMessage("§3" + p.getName() + "§b has invited you to their team, type §9" + TEAM_BASE_COMMAND
						+ TEAM_JOIN + " " + p.getName() + "§b to join.");

				if (invites.containsKey(p.getName())) {
					invites.get(p.getName()).add(p2.getName());
				} else {
					invites.put(p.getName(), new ArrayList<>(Arrays.asList(p2.getName())));
				}

				messageTeam(getTeam(p.getName()), "§3" + p.getName() + "§b has invited §3" + invite + "§b to the team");
			}
		}

		if (subcmd.equalsIgnoreCase(TEAM_JOIN) && args.length > 1) {
			String join = args[1];

			if (invites.containsKey(join) && invites.get(join).contains(p.getName())) {
				if (getTeam(p.getName()) != null) {
					messageTeam(getTeam(p.getName()), "§3" + p.getName() + "§b has left the team to join another");
					if (teams.get(getTeam(p.getName())).indexOf(p.getName()) == 0) {
						teams.remove(getTeam(p.getName()));
					} else
						teams.get(getTeam(p.getName())).remove(p.getName());
				}

				invites.get(join).remove(p.getName());
				teams.get(getTeam(join)).add(p.getName());

				messageTeam(getTeam(join), "§3" + p.getName() + "§b has joined the team");
			} else {
				p.sendMessage("§3" + join + "§b has no team or has not invited you");
			}
		}

		if (subcmd.equalsIgnoreCase(TEAM_LEAVE)) {
			if (getTeam(p.getName()) != null) {
				messageTeam(getTeam(p.getName()), "§3" + p.getName() + "§b has left the team");
				if (teams.get(getTeam(p.getName())).indexOf(p.getName()) == 0) {
					teams.remove(getTeam(p.getName()));
				} else
					teams.get(getTeam(p.getName())).remove(p.getName());
			}
		}

		if (subcmd.equalsIgnoreCase(TEAM_KICK) && args.length > 1) {
			String kick = args[1];

			if (getTeam(p.getName()) == null || teams.get(getTeam(p.getName())).indexOf(p.getName()) != 0) {
				p.sendMessage("§cYou are not the leader of the team or you are not in a team");
				return;
			}
			messageTeam(getTeam(p.getName()), "§3" + p.getName() + "§b kicked §3" + kick + "§b from the team");
			teams.get(getTeam(p.getName())).remove(kick);
		}
	}

	public static ItemMenu getPlayersMenu() {
		ItemMenu m = new ItemMenu("§2Tournament §8- §3Players", Bukkit.getOnlinePlayers().size());

		int indx = -1;
		for (final Player p : Bukkit.getOnlinePlayers()) {
			Button b = new Button(false, Material.SKULL_ITEM, 1, signedUp.contains(p.getName()) ? 3 : 0, p.getName(),
					"Click to toggle");
			b.setOnClick(new ButtonRunnable() {
				public void run(Player p2, InventoryClickEvent event) {
					ItemStack it = event.getCurrentItem();

					if (it.getDurability() == 0) {
						it.setDurability((short) 3);
						signedUp.add(p.getName());
					} else {
						it.setDurability((short) 0);
						signedUp.remove(p.getName());
					}

					event.setCurrentItem(it);
				}
			});
			m.addButton(b, ++indx);
		}

		return m;
	}

	public static void post(String urlToPost, String args, boolean put) {
		try {
			// String encoded = URLEncoder.encode(args, "UTF-8");
			String encoded = args;
			String base64Auth = new String(Base64.encodeBase64(("MCS_Paintball:" + CHALLONGE_API_KEY).getBytes()));
			URL url = new URL(String.format(CHALLONGE_API_URL, String.format(urlToPost, tournamentURL)));
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod(put ? "PUT" : "POST");
			con.setRequestProperty("Authorization", "Basic " + base64Auth);
			con.setRequestProperty("Content-Length", encoded.length() + "");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			OutputStream os = con.getOutputStream();
			os.write(encoded.getBytes());
			os.flush();
			os.close();
			con.disconnect();

			System.out.println((put ? "PUT" : "POST") + ": " + args + " (" + con.getResponseMessage() + ")");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static JSONArray get(String args) {
		try {
			String base64Auth = new String(Base64.encodeBase64(("MCS_Paintball:" + CHALLONGE_API_KEY).getBytes()));
			URL url = new URL(String.format(CHALLONGE_API_URL, String.format(args, tournamentURL)));
			HttpsURLConnection http = (HttpsURLConnection) url.openConnection();
			http.setRequestMethod("GET");
			http.setRequestProperty("Authorization", "Basic " + base64Auth);
			InputStreamReader isrea = new InputStreamReader(http.getInputStream());
			BufferedReader rea = new BufferedReader(isrea);

			System.out.println("GET: " + args + " (" + http.getResponseMessage() + ")");

			return (JSONArray) new JSONParser().parse(rea);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
