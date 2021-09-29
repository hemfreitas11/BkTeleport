package me.bkrmt.bkteleport.commands.tp;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.request.ClickableRequest;
import me.bkrmt.bkteleport.api.events.PlayerBkTeleportReplyEvent;
import me.bkrmt.teleport.Teleport;
import me.bkrmt.teleport.TeleportCore;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaAcceptCmd extends Executor {
    public TpaAcceptCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player requestTarget = (Player) sender;
        if (!hasPermission(sender) && !sender.hasPermission("bkteleport.player")) {
            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
        } else {
            if (args.length == 0) {
                for (Player inviter : getPlugin().getHandler().getMethodManager().getOnlinePlayers()) {
                    ClickableRequest tpHereRequest = ClickableRequest.getInteraction(TpHereCmd.TPHERE_IDENTIFIER, inviter.getUniqueId());
                    if (tpHereRequest != null && tpHereRequest.getTarget().getUniqueId().equals(requestTarget.getUniqueId())) {
                        args = new String[]{tpHereRequest.getSender().getName()};
                        break;
                    }

                    ClickableRequest tpaRequest = ClickableRequest.getInteraction(TpaCmd.TPA_IDENTIFIER, inviter.getUniqueId());
                    if (tpaRequest != null && tpaRequest.getTarget().getUniqueId().equals(requestTarget.getUniqueId())) {
                        args = new String[]{tpaRequest.getSender().getName()};
                        break;
                    }
                }
            }

            if (args.length == 0) {
                sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-pending-invite"));
                return true;
            }

            Player requestSender = Utils.getPlayer(args[0]);
            if (requestSender == null) {
                requestTarget.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.not-online").replace("{player}", args[0]));
            } else {
                PlayerBkTeleportReplyEvent replyEvent = new PlayerBkTeleportReplyEvent((Player) sender, requestSender);
                getPlugin().getServer().getPluginManager().callEvent(replyEvent);

                if (!replyEvent.isCancelled()) {
                    ClickableRequest tpHereRequest = ClickableRequest.getInteraction(TpHereCmd.TPHERE_IDENTIFIER, requestSender.getUniqueId(), requestTarget.getUniqueId());
                    ClickableRequest tpaRequest = ClickableRequest.getInteraction(TpaCmd.TPA_IDENTIFIER, requestSender.getUniqueId(), requestTarget.getUniqueId());

                    if (tpHereRequest != null || tpaRequest != null) {
                        if (TeleportCore.INSTANCE.getPlayersInCooldown().get(requestSender.getName()) == null) {
                            if (TeleportCore.INSTANCE.getPlayersInCooldown().get(requestTarget.getName()) == null) {
                                requestSender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "info.invite-accepted.self").replace("{player}", requestTarget.getName()));
                                requestTarget.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "info.invite-accepted.others").replace("{player}", requestSender.getName()));

                                if (tpHereRequest == null) {
                                    ClickableRequest.removeInteraction(TpaCmd.TPA_IDENTIFIER, requestSender.getUniqueId());
                                    startTeleport(requestTarget.getLocation(), requestSender, requestTarget.getName());
                                } else {
                                    ClickableRequest.removeInteraction(TpHereCmd.TPHERE_IDENTIFIER, requestSender.getUniqueId());
                                    startTeleport(requestSender.getLocation(), requestTarget, requestSender.getName());
                                }
                            } else {
                                requestSender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.already-waiting"));
                                cancelRequest(tpHereRequest, requestTarget);
                            }
                        } else {
                            requestSender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.other-already-waiting").replace("{player}", requestSender.getName()));
                            cancelRequest(tpHereRequest, requestTarget);
                        }
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-pending-invite"));
                    }
                }
            }
        }
        return true;
    }

    private void startTeleport(Location location, Player player, String name) {
        new Teleport(getPlugin(), player, getPlugin().getConfigManager().getConfig().getBoolean("teleport-countdown.cancel-on-move"))
                .setLocation(name, location)
                .setDuration(Utils.intFromPermission(player, 5, "bkteleport.countdown", new String[]{"bkteleport.countdown.0", "bkteleport.admin"}))
                .setIsCancellable(true)
                .startTeleport();
    }

    private void cancelRequest(ClickableRequest tpHere, Player player) {
        if (tpHere == null) {
            ClickableRequest.removeInteraction(TpaCmd.TPA_IDENTIFIER, player.getUniqueId());
        } else {
            ClickableRequest.removeInteraction(TpHereCmd.TPHERE_IDENTIFIER, player.getUniqueId());
        }
    }
}