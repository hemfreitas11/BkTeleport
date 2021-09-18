package me.bkrmt.bkteleport.commands;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkteleport.BkTeleport;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandHandler implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String commandLabel = event.getMessage().toLowerCase().split(" ")[0];
        Player sender = event.getPlayer();
        BkPlugin plugin = BkTeleport.getInstance();
        if (BkTeleport.getInstance().getCommands().get("spawn").contains(commandLabel)) {
            runCommand(sender, commandLabel, "bkteleport:" + plugin.getLangFile().get(sender, "commands.spawn.command"), event);
        } else if (BkTeleport.getInstance().getCommands().get("setspawn").contains(commandLabel)) {
            runCommand(sender, commandLabel, "bkteleport:" + plugin.getLangFile().get(sender, "commands.setspawn.command"), event);
        } else if (BkTeleport.getInstance().getCommands().get("warp").contains(commandLabel)) {
            runCommand(sender, commandLabel, "bkteleport:" + plugin.getLangFile().get(sender, "commands.warp.command"), event);
        } else if (BkTeleport.getInstance().getCommands().get("warps").contains(commandLabel)) {
            String editSub = plugin.getLangFile().get(sender, "commands.warp.subcommands.edit.command");
            event.setMessage("bkteleport:" + plugin.getLangFile().get(sender, "commands.warps.command") +
                 (event.getMessage().contains(editSub) ? " " + editSub : ""));
            runCommand(sender, "", "", event);
        } else if (BkTeleport.getInstance().getCommands().get("setwarp").contains(commandLabel)) {
            runCommand(sender, commandLabel, "bkteleport:" + plugin.getLangFile().get(sender, "commands.setwarp.command"), event);
        } else if (BkTeleport.getInstance().getCommands().get("delwarp").contains(commandLabel)) {
            runCommand(sender, commandLabel, "bkteleport:" + plugin.getLangFile().get(sender, "commands.delwarp.command"), event);
        } else if (BkTeleport.getInstance().getCommands().get("home").contains(commandLabel)) {
            runCommand(sender, commandLabel, "bkteleport:" + plugin.getLangFile().get(sender, "commands.home.command"), event);
        } else if (BkTeleport.getInstance().getCommands().get("homes").contains(commandLabel)) {
            event.setMessage("bkteleport:" + plugin.getLangFile().get(sender, "commands.homes.command"));
            runCommand(sender, "", "", event);
        } else if (BkTeleport.getInstance().getCommands().get("sethome").contains(commandLabel)) {
            runCommand(sender, commandLabel, "bkteleport:" + plugin.getLangFile().get(sender, "commands.sethome.command"), event);
        } else if (BkTeleport.getInstance().getCommands().get("delhome").contains(commandLabel)) {
            runCommand(sender, commandLabel, "bkteleport:" + plugin.getLangFile().get(sender, "commands.delhome.command"), event);
        } else if (BkTeleport.getInstance().getCommands().get("back").contains(commandLabel)) {
            runCommand(sender, commandLabel, "bkteleport:" + plugin.getLangFile().get(sender, "commands.back.command"), event);
        } else if (BkTeleport.getInstance().getCommands().get("tpa").contains(commandLabel)) {
            runCommand(sender, commandLabel, "bkteleport:" + plugin.getLangFile().get(sender, "commands.tpa.command"), event);
        } else if (BkTeleport.getInstance().getCommands().get("tpahere").contains(commandLabel)) {
            runCommand(sender, commandLabel, "bkteleport:" + plugin.getLangFile().get(sender, "commands.tpahere.command"), event);
        } else if (BkTeleport.getInstance().getCommands().get("tpaccept").contains(commandLabel)) {
            runCommand(sender, commandLabel, "bkteleport:" + plugin.getLangFile().get(sender, "commands.tpaccept.command"), event);
        } else if (BkTeleport.getInstance().getCommands().get("tpdeny").contains(commandLabel)) {
            runCommand(sender, commandLabel, "bkteleport:" + plugin.getLangFile().get(sender, "commands.tpdeny.command"), event);
        }
    }

    private void runCommand(Player sender, String command, String placeholder, PlayerCommandPreprocessEvent event) {
        event.setCancelled(true);
        sender.performCommand(event.getMessage().replace(command, placeholder));
    }
}