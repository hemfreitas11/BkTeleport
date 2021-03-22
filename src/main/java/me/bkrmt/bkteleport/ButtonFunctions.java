package me.bkrmt.bkteleport;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static me.bkrmt.bkteleport.BkTeleport.plugin;

public class ButtonFunctions implements Listener {
    @EventHandler
    public void onButtonClick(InventoryClickEvent event) {
        String inventoryName = ChatColor.stripColor(event.getView().getTitle());
        if (ChatColor.stripColor(plugin.getLangFile().get("info.home-list-title")).equals(inventoryName) ||
                ChatColor.stripColor(plugin.getLangFile().get("info.warp-list-title")).equals(inventoryName)) {
            event.setCancelled(true);
            if (event.getSlotType().equals(InventoryType.SlotType.CONTAINER)) {
                ItemStack button = event.getCurrentItem();
                if (button != null && !button.getType().equals(Material.AIR)) {
                    if (ChatColor.stripColor(plugin.getLangFile().get("info.home-list-title")).equals(inventoryName)) {
                        UserType type = UserType.User;

                        List<String> lore = button.getItemMeta().getLore();

                        String playerName = "";
                        String spyingString = ChatColor.stripColor(plugin.getLangFile().get("info.spying").replace("{player}", "")).trim();
                        String command = "bkteleport:";
                        String homeCommand = plugin.getLangFile().get("commands.home.command");
                        String delHomeCommand = plugin.getLangFile().get("commands.delhome.command");

                        for (String line : lore) {
                            String cleanLine = ChatColor.stripColor(line).trim();
                            if (cleanLine.contains(spyingString)) {
                                type = UserType.Spy;
                                String[] temp = line.split(":");
                                playerName = temp[1].replaceAll(" ", "");
                                break;
                            }
                            String[] split = cleanLine.replace("/", "").split(" ");
                            if (split.length > 0) {
                                if (split[0].equals(homeCommand)) {
                                    command += homeCommand;
                                } else if (split[0].equals(delHomeCommand)) {
                                    command += delHomeCommand;
                                }
                            }
                        }

                        event.getWhoClicked().closeInventory();
                        String homeName = ChatColor.stripColor(button.getItemMeta().getDisplayName());
                        if (homeName == null) return;

                        if (type.equals(UserType.User)) command += " " + homeName;
                        else command += " " + playerName + ":" + homeName;

                        ((Player) event.getWhoClicked()).performCommand(command);
                    } else {
                        String warpName = ChatColor.stripColor(button.getItemMeta().getDisplayName());
                        if (warpName == null) return;
                        event.getWhoClicked().closeInventory();
                        ((Player) event.getWhoClicked()).performCommand("bkteleport:" + plugin.getLangFile().get("commands.warp.command") + " " + warpName);
                    }
                }
            }
        }
    }
}