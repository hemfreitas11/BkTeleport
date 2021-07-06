package me.bkrmt.bkteleport;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.PagedItem;
import me.bkrmt.bkcore.PagedList;
import me.bkrmt.opengui.Page;
import me.bkrmt.opengui.event.ElementResponse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Home implements PagedItem {
    private final BkPlugin plugin;
    private final String name;
    private final List<String> lore;
    private final String displayName;
    private final ItemStack displayItem;

    public Home(String name, ItemStack displayItem, String displayName, List<String> lore) {
        plugin = BkTeleport.getInstance();
        this.lore = lore;
        this.displayItem = displayItem;
        this.displayName = displayName;
        this.name = name;
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return displayName == null ? plugin.getLangFile().get("homes-menu.default-home-name").replace("{home-name}", name) : displayName;
    }

    @Override
    public List<String> getLore(PagedList list, Page currentPage) {
        return lore;
    }

    @Override
    public ItemStack getDisplayItem(PagedList list, Page currentPage) {
        return displayItem;
    }

    @Override
    public ElementResponse getElementResponse(PagedList list, Page currentPage) {
        return event -> ((Player)event.getWhoClicked()).performCommand(plugin.getLangFile().get("commands.home.command") + " " + name);
    }
}
