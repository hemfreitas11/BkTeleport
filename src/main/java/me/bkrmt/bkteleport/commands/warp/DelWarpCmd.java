package me.bkrmt.bkteleport.commands.warp;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.command.Executor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;

public class DelWarpCmd extends Executor {

    public DelWarpCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
        } else {
            if (args.length == 1) {
                File warpFile = getPlugin().getFile("warps", args[0].toLowerCase() + ".yml");
                if (!warpFile.exists()) {
                    sender.sendMessage(getPlugin().getLangFile().get("error.unknown-warp").replace("{warp-name}", args[0]));
                } else {
                    warpFile.delete();
                    sender.sendMessage(getPlugin().getLangFile().get("info.warp-deleted"));
                }
            } else {
                sendUsage(sender);
            }
        }
        return true;
    }
}
