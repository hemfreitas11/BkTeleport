package me.bkrmt.bkteleport.edit.options.home;

import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.bkgui.event.ElementResponse;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkteleport.edit.EditOption;
import me.bkrmt.bkteleport.teleportable.Home;
import me.bkrmt.bkteleport.teleportable.Teleportable;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Visibility extends EditOption {
    public Visibility(Object customObject, Player player) {
        super(customObject, player, new String[] {"bkteleport.edit.visibility", "bkteleport.admin", "bkteleport.edit.*"});
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return translateName(getPlugin().getLangFile().get("homes-menu.edit-menu.set-public.name"));
    }

    @Override
    public List<String> getLore(PagedList list, Page currentPage) {
        List<String> lore = new ArrayList<>();
        Teleportable teleportable = getTeleportable();
        Home home = null;
        if (teleportable != null) home = (Home) teleportable;
        if (home != null) {
            String publicString = getPlugin().getLangFile().get(null, "homes-menu.visibility.public");
            String privateString = getPlugin().getLangFile().get(null, "homes-menu.visibility.private");
            for (String line : getPlugin().getLangFile().getStringList("homes-menu.edit-menu.set-public.lore")) {
                lore.add(
                        line.replace("{visibility}", home.isPublic() ? publicString : privateString)
                );
            }
            return translateLore(lore);
        } else return translateLore(getPlugin().getLangFile().getStringList("homes-menu.edit-menu.set-public.lore"));
    }

    @Override
    public ItemStack getDisplayItem(PagedList list, Page currentPage) {
        return translateItem(XMaterial.ENDER_PEARL.parseItem());
    }

    @Override
    public ElementResponse getElementResponse(PagedList list, Page currentPage) {
        if (!hasPermission()) return event -> {};

        Teleportable teleportable = getTeleportable();
        return event -> {
            if (teleportable == null) {
                currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "error.option-error"), null);
            } else {
                Home home = (Home) teleportable;
                Player player = (Player) event.getWhoClicked();
                home.setPublic(!home.isPublic());
                home.saveValues();
                PagedList editMenu = home.buildEditMenu(player, currentPage.getPreviousMenus().get(0));
                editMenu.openPage(0);
                editMenu.getPages().get(0).displayItemMessage(event.getSlot(), 2, (home.isPublic() ? ChatColor.YELLOW : ChatColor.RED), getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "info.visibility." +
                    (home.isPublic() ? "set-public" : "set-private")), null);
            }
        };
    }
}
