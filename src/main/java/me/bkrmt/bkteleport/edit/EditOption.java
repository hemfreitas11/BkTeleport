package me.bkrmt.bkteleport.edit;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.PagedItem;
import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.bkgui.event.ElementResponse;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkteleport.BkTeleport;
import me.bkrmt.bkteleport.teleportable.Teleportable;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class EditOption implements PagedItem {
    private final Object customObject;
    private long id = -1;
    private final BkPlugin plugin;
    private final Player player;
    private String[] permissions;
    private int slot;
    private int page;

    public EditOption(Object customObject, Player player, String[] permissions) {
        this.player = player;
        plugin = BkTeleport.getInstance();
        this.customObject = customObject;
        this.permissions = permissions;
        slot = -1;
        page = -1;
    }

    public Object getCustomObject() {
        return customObject;
    }

    public Teleportable getTeleportable() {
        Object teleportableObject = getCustomObject();
        return teleportableObject != null ? (Teleportable) teleportableObject : null;
    }

    public BkPlugin getPlugin() {
        return plugin;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack translateItem(ItemStack item) {
        if (hasPermission())
            return item;
        else
            return XMaterial.BARRIER.parseItem();
    }

    public String translateName(String displayName) {
        if (hasPermission())
            return displayName;
        else
            return ChatColor.COLOR_CHAR + "8" + ChatColor.COLOR_CHAR + "l" + ChatColor.stripColor(displayName);
    }

    public List<String> translateLore(List<String> lore) {
        if (hasPermission()) {
            return lore;
        } else {
            List<String> returnValue = new ArrayList<>();
            for (String line : lore) {
                returnValue.add(ChatColor.COLOR_CHAR + "8" + ChatColor.stripColor(line));
            }
            if (!returnValue.isEmpty() && !returnValue.get(returnValue.size()-1).equalsIgnoreCase(" "))
                returnValue.add(" ");
            returnValue.add(plugin.getLangFile().get("error.option-permission"));
            return returnValue;
        }
    }

    public boolean hasPermission() {
        for (String permission : permissions) {
            if (player.hasPermission(permission)) return true;
        }
        return false;
    }

    @Override
    public long getID() {
        return id;
    }

    @Override
    public void assignID(long id) {
        this.id = id;
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return null;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public void setIgnorePage(boolean ignorePage) {

    }

    @Override
    public void setIgnoreSlot(boolean ignoreSlot) {

    }

    @Override
    public boolean isIgnorePage() {
        return false;
    }

    @Override
    public boolean isIgnoreSlot() {
        return false;
    }

    @Override
    public void setPage(int slot) {
        this.page = slot;
    }

    @Override
    public void setSlot(int slot) {
        this.slot = slot;
    }

    @Override
    public List<String> getLore(PagedList list, Page currentPage) {
        return null;
    }

    @Override
    public ItemStack getDisplayItem(PagedList list, Page currentPage) {
        return null;
    }

    @Override
    public ElementResponse getElementResponse(PagedList list, Page currentPage) {
        return null;
    }
}
