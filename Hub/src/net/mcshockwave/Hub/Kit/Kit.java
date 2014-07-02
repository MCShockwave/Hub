package net.mcshockwave.Hub.Kit;

import net.mcshockwave.Guns.Gun;
import net.mcshockwave.Guns.addons.Addon;
import net.mcshockwave.Guns.descriptors.Category;
import net.mcshockwave.Guns.descriptors.GunType;
import net.mcshockwave.Hub.DefaultListener;
import net.mcshockwave.Hub.HubPlugin;
import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Menu.ItemMenu.Button;
import net.mcshockwave.MCS.Menu.ItemMenu.ButtonRunnable;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public enum Kit {

	Warrior(
		i(Material.LEATHER_HELMET),
		i(Material.CHAINMAIL_CHESTPLATE),
		i(Material.CHAINMAIL_LEGGINGS),
		i(Material.LEATHER_BOOTS),
		i(Material.IRON_SWORD)),
	Archer(
		i(Material.IRON_HELMET),
		i(Material.LEATHER_CHESTPLATE),
		i(Material.LEATHER_LEGGINGS),
		i(Material.IRON_BOOTS),
		i(Material.WOOD_SWORD),
		i(Material.BOW),
		i(Material.ARROW, 1)),
	Tank(
		i(Material.IRON_HELMET),
		i(Material.IRON_CHESTPLATE),
		i(Material.IRON_LEGGINGS),
		i(Material.IRON_BOOTS),
		i(Material.IRON_AXE)),
	Mage(
		i(Material.GOLD_HELMET),
		i(Material.GOLD_CHESTPLATE),
		i(Material.AIR),
		i(Material.AIR),
		i(Material.BLAZE_ROD),
		i(Material.NETHER_STAR)),
	Assassin(
		i(Material.AIR),
		i(Material.AIR),
		i(Material.AIR),
		i(Material.LEATHER_BOOTS),
		i(Material.SHEARS),
		i(Material.GLOWSTONE_DUST, 5)),
	Medic(
		i(Material.LEATHER_HELMET),
		i(Material.LEATHER_CHESTPLATE),
		i(Material.GOLD_LEGGINGS),
		i(Material.GOLD_BOOTS),
		i(Material.STONE_SWORD),
		i(Material.BEACON)),
	Demoman(
		i(Material.LEATHER_HELMET),
		i(Material.LEATHER_CHESTPLATE),
		i(Material.LEATHER_LEGGINGS),
		i(Material.LEATHER_BOOTS),
		i(Material.STONE_AXE),
		i(Material.IRON_BARDING)),
	Engineer(
		i(Material.LEATHER_HELMET),
		i(Material.LEATHER_CHESTPLATE),
		i(Material.IRON_LEGGINGS),
		i(Material.LEATHER_BOOTS),
		i(Material.STONE_SWORD),
		i(Material.DISPENSER)),
	Pyro(
		i(Material.GOLD_HELMET),
		i(Material.CHAINMAIL_CHESTPLATE),
		i(Material.GOLD_LEGGINGS),
		i(Material.GOLD_BOOTS),
		i(Material.GOLD_AXE),
		i(Material.FLINT_AND_STEEL));

	public ItemStack[]		acontents;
	public ItemStack[]		contents;

	public static boolean	gunmode	= false;

	private Kit(ItemStack h, ItemStack c, ItemStack l, ItemStack b, ItemStack... items) {
		acontents = new ItemStack[] { b, l, c, h };
		contents = items;
	}

	public ItemStack getIcon() {
		if (contents.length > 1 && contents[1].getAmount() == 1) {
			return contents[1];
		}
		return contents[0];
	}

	public static ItemMenu getSelectorMenu(Player p) {
		ItemMenu m;
		if (!gunmode) {
			int le = values().length;
			m = new ItemMenu("Kits", ((le + 8) / 9) * 9);

			for (int i = 0; i < le; i++) {
				final Kit k = values()[i];

				Button b = new Button(true, k.getIcon().getType(), 1, k.getIcon().getDurability(), "Kit - �a"
						+ k.name(), "Click to use");
				m.addButton(b, i);
				b.setOnClick(new ButtonRunnable() {
					public void run(Player p, InventoryClickEvent event) {
						// p.teleport(PVPCommand.arena(HubPlugin.dW()));
						p.teleport(getRandomLocation(200, HubPlugin.endWorld()));
						p.sendMessage("�aEntering arena with kit " + k.name());

						k.use(p);
					}
				});
			}
		} else {
			m = new ItemMenu("Gun Mode", 9);

			Button enter = new Button(true, Material.WOOD_HOE, 1, 0, "Gun Mode", "Click to enter");
			m.addButton(enter, 4);
			enter.setOnClick(new ButtonRunnable() {
				public void run(Player p, InventoryClickEvent event) {
					p.teleport(getRandomLocation(200, HubPlugin.endWorld()));
					p.sendMessage("�aEntering arena (Gun mode)");

					giveGunKit(p);
				}
			});
		}

		return m;
	}

	@SuppressWarnings("deprecation")
	public void use(Player p) {
		p.getInventory().clear();
		p.getInventory().setArmorContents(acontents);
		for (ItemStack it : contents) {
			if (it.getType() == Material.BOW) {
				it = ItemMetaUtils.addEnchantment(it, Enchantment.ARROW_DAMAGE, 1);
				it = ItemMetaUtils.addEnchantment(it, Enchantment.ARROW_KNOCKBACK, 1);
				it = ItemMetaUtils.addEnchantment(it, Enchantment.ARROW_INFINITE, 1);
			}
			if (it.getType() == Material.ARROW) {
				p.getInventory().setItem(17, it);
			} else
				p.getInventory().addItem(it);
		}
		p.updateInventory();
		clearPE(p);

		if (this == Tank) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0));
		}
		if (this == Assassin) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
		}
		if (this == Archer) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
		}
	}

	private static Category	gunCategory	= Category.Wasted;

	private static String[]	materials	= { "LEATHER", "CHAINMAIL", "IRON" };

	public static void giveGunKit(Player p) {
		p.getInventory().clear();
		List<Gun> prim = new ArrayList<>();
		List<Gun> seco = new ArrayList<>();
		for (Gun g : gunCategory.getGuns()) {
			if (g.type == GunType.PISTOL) {
				seco.add(g);
			} else {
				prim.add(g);
			}
		}

		ItemStack prWep = prim.get(rand.nextInt(prim.size())).getItem();
		ItemStack seWep = seco.get(rand.nextInt(seco.size())).getItem();

		Addon.Infinite_Ammo.add(prWep);
		Addon.Bottomless_Clip.add(seWep);

		p.getInventory().addItem(prWep);
		p.getInventory().addItem(seWep);

		p.getInventory().setHelmet(
				new ItemStack(Material.valueOf(materials[rand.nextInt(materials.length)] + "_HELMET")));
		p.getInventory().setChestplate(
				new ItemStack(Material.valueOf(materials[rand.nextInt(materials.length)] + "_CHESTPLATE")));
		p.getInventory().setLeggings(
				new ItemStack(Material.valueOf(materials[rand.nextInt(materials.length)] + "_LEGGINGS")));
		p.getInventory()
				.setBoots(new ItemStack(Material.valueOf(materials[rand.nextInt(materials.length)] + "_BOOTS")));
	}

	public static void clearPE(Player p) {
		for (PotionEffect pe : p.getActivePotionEffects()) {
			p.removePotionEffect(pe.getType());
		}
	}

	public static void toggleGunMode() {
		gunmode = !gunmode;

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (DefaultListener.isInArena(p)) {
				p.damage(p.getMaxHealth());
				p.sendMessage("§7[§e§lPVP§e§l] §fToggling gun mode (Now " + gunmode + ")");
			}
		}
	}

	static Random	rand	= new Random();

	public static Location getRandomLocation(int rad, World w) {
		boolean done = false;
		int tries = 1000;
		while (!done && tries > 0) {
			int x = rand.nextInt(rad * 2) - rad;
			int z = rand.nextInt(rad * 2) - rad;
			int y = w.getHighestBlockYAt(x, z);
			Location l = new Location(w, x, y - 1, z);
			if (l.getBlock().getType() != Material.ENDER_STONE || y > 100) {
				tries--;
				continue;
			}

			return l.add(0, 2, 0);
		}
		return w.getSpawnLocation();
	}

	public static ItemStack i(Material m) {
		return new ItemStack(m);
	}

	public static ItemStack i(Material m, int a) {
		return new ItemStack(m, a);
	}

	public static ItemStack i(Material m, int a, int d) {
		return new ItemStack(m, a, (short) d);
	}

}
