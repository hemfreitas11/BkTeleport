package me.bkrmt.bkteleport.commands.tp;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkteleport.RequestType;
import me.bkrmt.bkteleport.TeleportRequest;
import me.bkrmt.bkteleport.TpaUtils;
import me.bkrmt.bkteleport.events.PlayerBkTeleportSendEvent;
import me.bkrmt.teleport.TeleportCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaHereCmd extends Executor {


    public TpaHereCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
        } else {
            if (args.length == 1) {
                if (!(getPlugin().getServer().getPlayer(args[0]) == null)) {
                    if (TeleportCore.INSTANCE.getPlayersInCooldown().get(sender.getName()) == null) {
                        Player senderPlayer = ((Player) sender);
                        Player targetPlayer = getPlugin().getServer().getPlayer(args[0]);
                        if (!senderPlayer.equals(targetPlayer)) {
                            RequestType type = TpaUtils.isExpiring(senderPlayer.getName().toLowerCase(), targetPlayer.getName().toLowerCase());
                            if (type == null) {
                                new TeleportRequest(senderPlayer, targetPlayer, RequestType.TpaHere).sendMessage();
                                targetPlayer.playSound(targetPlayer.getLocation(), getPlugin().getHandler().getSoundManager().getPling(), 15, 1);
                                senderPlayer.sendMessage(getPlugin().getLangFile().get("info.sent-invite").replace("{player}", targetPlayer.getName()));
                                TpaUtils.checkExpired(((Player) sender), targetPlayer, RequestType.TpaHere);

                                PlayerBkTeleportSendEvent reqSendEvent = new PlayerBkTeleportSendEvent((Player) sender, targetPlayer);
                                getPlugin().getServer().getPluginManager().callEvent(reqSendEvent);
                                if (reqSendEvent.isCancelled()) {
                                    type = TpaUtils.isExpiring(senderPlayer.getName().toLowerCase(), targetPlayer.getName().toLowerCase());
                                    TpaUtils.playerExpiredChecker.get(senderPlayer.getName().toLowerCase() + "-" + targetPlayer.getName().toLowerCase() + "-" + type.toString()).cancel();
                                    TpaUtils.playerExpiredChecker.remove(senderPlayer.getName().toLowerCase() + "-" + targetPlayer.getName().toLowerCase() + "-" + type.toString());
                                }
                            } else {
                                sender.sendMessage(getPlugin().getLangFile().get("error.cant-invite-again"));
                            }
                        } else {
                            sender.sendMessage(getPlugin().getLangFile().get("error.cant-invite-self"));
                        }
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get("error.already-waiting"));
                    }
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get("error.player-not-found").replace("{player}", args[0]));
                }
            } else {
                sendUsage(sender);
            }
        }
        return true;
    }
}