package net.mcshockwave.Hub.Kit;

import net.mcshockwave.Hub.HubPlugin;
import net.mcshockwave.Hub.Kit.Paintball.Minigame;
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

	public static final String			CHALLONGE_API_KEY	= "KSltuaCt3HcRECfW5JDKh8PgwEa7Jv4sYBBqrKy8";

	public static final String			CHALLONGE_API_URL	= "https://api.challonge.com/v1/%s";

	public static final String			TOURNAMENT_FORMAT	= "PAINTBALL%s";

	public static boolean				running				= false;

	public static int					id					= -1;

	public static String				tournamentURL, tournamentType;

	public static HashMap<Long, String>	participants		= new HashMap<>();

	public static List<JSONObject>		matches				= new ArrayList<>();

	public static HashMap<Long, UUID>	paintball			= new HashMap<>();

	public static Minigame				game				= Minigame.Elimination;

	public static boolean				signups				= false;
	public static List<String>			signedUp			= new ArrayList<>();
	public static final String			SIGNUPS_COMMAND		= "/signup";

	public static final String			TEAM_BASE_COMMAND	= "/team";
	public static final String			TEAM_JOIN			= "join", TEAM_INVITE = "invite", TEAM_CREATE = "create";

	public static void startSignups() {
		signups = true;
		MCShockwave.broadcastAll(MCShockwave.getBroadcastMessage(ChatColor.DARK_GREEN,
				"Signups for the %s tournament have opened!", "paintball"), MCShockwave.getBroadcastMessage(
				ChatColor.DARK_AQUA, "Type %s to sign up!", SIGNUPS_COMMAND));
	}

	public static void signupPlayer(String player) {
		if (signedUp.contains(player)) {
			return;
		}
		signedUp.add(player);
		MCShockwave.broadcast("%s has signed up for the tournament!", player);
	}

	public static void teamCmd(String[] args) {
		String cmd = args[0];
		
		if (cmd.equalsIgnoreCase("")) {
			
		}
	}

	public static void prepareTournament(final String... players) {
		new BukkitRunnable() {
			public void run() {
				id = rand.nextInt(1000000);
				tournamentURL = new BigInteger(65, rand).toString(35).toUpperCase();
				createNewTournament(String.format(TOURNAMENT_FORMAT, id), tournamentURL, tournamentType);
				for (String s : players) {
					addParticipant(s);
				}
				updateParticipants();
			}
		}.runTaskAsynchronously(HubPlugin.ins);
	}

	public static void start() {
		running = true;
		signups = false;
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
				post("tournaments/%s/finalize.format", "", false);
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

	public static void onWin(final String pl, final Paintball game) {
		long id = -1;
		for (Entry<Long, UUID> ent : paintball.entrySet()) {
			if (ent.getValue().equals(game.gameUUID)) {
				id = ent.getKey();
				break;
			}
		}
		if (id != -1) {
			final long idf = id;
			new BukkitRunnable() {
				public void run() {
					String csv = pl.equalsIgnoreCase(game.p1) ? "1-0" : "0-1";
					post("tournaments/%s/matches/" + idf + ".json", "match[scores_csv]=" + csv + "&match[winner_id]="
							+ getParticipantID(pl), true);
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
				long round = -1;
				for (JSONObject obj : matches) {
					if (round == -1 && obj.get("state").equals("open") || (long) obj.get("round") == round) {
						round = (long) obj.get("round");
						long id1 = (long) obj.get("player1_id");
						long id2 = (long) obj.get("player2_id");

						String p1 = participants.get(Long.valueOf(id1));
						String p2 = participants.get(Long.valueOf(id2));

						MCShockwave.broadcast("Match §e" + obj.get("identifier") + "§7: %s VS. %s", p1, p2);

						Paintball pb = Paintball.newGame(game, 2);
						paintball.put((Long) obj.get("id"), pb.gameUUID);
						pb.queue(true, false);

						pb.p1 = p1;
						pb.p2 = p2;

						if (Bukkit.getPlayer(p1) == null) {
							onWin(p2, pb);
							continue;
						}
						if (Bukkit.getPlayer(p2) == null) {
							onWin(p1, pb);
							continue;
						}

						pb.addToQueue(p1);
						pb.addToQueue(p2);
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
