package net.mcshockwave.Hub.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PetCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			p.sendMessage("§c/pets is disabled temperarily");
			return false;

			// if (DefaultListener.isInArena(p)) {
			// p.sendMessage("§c/pets is disabled in the PVP arena");
			// return false;
			// }

			// Inventory i = Bukkit.createInventory(null, 18, "Pets");
			//
			// for (MCSPet pe : MCSPet.values()) {
			// if (pe.r != null && SQLTable.hasRank(p.getName(), pe.r) || pe.l
			// != -1
			// && LevelUtils.getLevelFromXP(LevelUtils.getXP(p)) >= pe.l ||
			// pe.tc != null
			// && ItemsUtils.hasItem(p.getName(), SQLTable.MiscItems, pe.tc)) {
			// i.addItem(ItemMetaUtils.setLore(
			// ItemMetaUtils.setItemName(new ItemStack(Material.MONSTER_EGG),
			// "§r" + pe.name().replace('_', ' ')),
			// "",
			// "Unlocked by "
			// + (pe.r != null ? "having "
			// + WordUtils.capitalizeFully(pe.r.name().replace('_', ' '))
			// : pe.l != -1 ? "reaching level " + pe.l :
			// "purchasing from shop")));
			// }
			// }
			// i.setItem(i.getSize() - 1, ItemMetaUtils.setItemName(new
			// ItemStack(Material.STICK), "§rRemove Pet"));

			// p.openInventory(i);
		}
		return false;
	}

	// public enum MCSPet {
	// // rank unlocked
	// Witch(
	// Rank.ENDER,
	// PetType.WITCH),
	// Blaze(
	// Rank.NETHER,
	// PetType.BLAZE),
	// Enderman(
	// Rank.OBSIDIAN,
	// PetType.ENDERMAN),
	// Creeper(
	// Rank.EMERALD,
	// PetType.CREEPER),
	// // Level unlocked
	// Chicken(
	// 25,
	// PetType.CHICKEN),
	// Pig(
	// 50,
	// PetType.PIG),
	// Cow(
	// 100,
	// PetType.COW),
	// Sheep(
	// 250,
	// PetType.SHEEP),
	// // Shop unlocked
	// Wolf(
	// "Wolf_Pet",
	// PetType.WOLF),
	// Cat(
	// "Cat_Pet",
	// PetType.OCELOT);
	//
	// public Rank r;
	// public int l;
	// public String tc;
	// public PetType t;
	//
	// MCSPet(Rank r, PetType t) {
	// this.r = r;
	// this.l = -1;
	// this.tc = null;
	// this.t = t;
	// }
	//
	// MCSPet(int l, PetType t) {
	// this.r = null;
	// this.l = l;
	// this.tc = null;
	// this.t = t;
	// }
	//
	// MCSPet(String tc, PetType t) {
	// this.r = null;
	// this.l = -1;
	// this.tc = tc;
	// this.t = t;
	// }
	// }

}
