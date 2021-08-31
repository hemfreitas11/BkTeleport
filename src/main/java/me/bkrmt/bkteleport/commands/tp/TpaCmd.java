package me.bkrmt.bkteleport.commands.tp;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.request.ClickableRequest;
import me.bkrmt.bkteleport.PluginUtils;
import me.bkrmt.bkteleport.api.events.PlayerBkTeleportSendEvent;
import me.bkrmt.teleport.TeleportCore;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaCmd extends Executor {
    public TpaCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    public static final String TPA_IDENTIFIER = "tpa-request";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender) && !sender.hasPermission("bkteleport.player")) {
            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
        } else {
            if (args.length == 1) {
                if (!(getPlugin().getServer().getPlayer(args[0]) == null)) {
                    if (TeleportCore.INSTANCE.getPlayersInCooldown().get(sender.getName()) == null) {
                        Player senderPlayer = ((Player) sender);
                        Player targetPlayer = getPlugin().getServer().getPlayer(args[0]);
                        if (!senderPlayer.equals(targetPlayer)) {
                            ClickableRequest senderRequest = ClickableRequest.getInteraction(TPA_IDENTIFIER, senderPlayer.getUniqueId());
                            if (senderRequest == null) {
                                PlayerBkTeleportSendEvent reqSendEvent = new PlayerBkTeleportSendEvent((Player) sender, targetPlayer);
                                getPlugin().getServer().getPluginManager().callEvent(reqSendEvent);
                                if (!reqSendEvent.isCancelled()) {
                                    PluginUtils.sendRequest(senderPlayer, targetPlayer, TPA_IDENTIFIER);
                                }
                            } else {
                                sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.cant-invite-again"));
                            }
                        } else {
                            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.cant-invite-self"));
                        }
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.already-waiting"));
                    }
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.player-not-found").replace("{player}", args[0]));
                }
            } else {
                sendUsage(sender);
            }
        }
        return true;
    }
}