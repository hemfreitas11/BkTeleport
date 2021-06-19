package me.bkrmt.bkteleport.commands.tp;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkteleport.RequestType;
import me.bkrmt.bkteleport.TpaUtils;
import me.bkrmt.bkteleport.events.PlayerBkTeleportReplyEvent;
import me.bkrmt.teleport.Teleport;
import me.bkrmt.teleport.TeleportCore;
import me.bkrmt.teleport.TeleportType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TpaAcceptCmd extends Executor {
    public TpaAcceptCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
        } else {
            if (args.length == 0) {
                for (Player inviter : getPlugin().getHandler().getMethodManager().getOnlinePlayers()) {
                    if (TpaUtils.playerExpiredChecker.containsKey(inviter.getName().toLowerCase() + "-" + sender.getName().toLowerCase() + "-" + TeleportType.Tpa.toString()) ||
                            TpaUtils.playerExpiredChecker.containsKey(inviter.getName().toLowerCase() + "-" + sender.getName().toLowerCase() + "-" + TeleportType.TpaHere.toString())) {
                        args = new String[]{inviter.getName()};
                    }
                }
            }
            if (args.length == 0) {
                sender.sendMessage(getPlugin().getLangFile().get("error.no-pending-invite"));
                return true;
            }
            Player inviter = getPlugin().getServer().getPlayer(args[0]);
            String name = inviter == null ? args[0] : inviter.getName();

            PlayerBkTeleportReplyEvent replyEvent = new PlayerBkTeleportReplyEvent((Player) sender, inviter);
            getPlugin().getServer().getPluginManager().callEvent(replyEvent);

            RequestType type = TpaUtils.isExpiring(name.toLowerCase(), sender.getName().toLowerCase());

            if (!replyEvent.isCancelled()) {
                if (type != null) {
                    if (inviter != null) {
                        if (TeleportCore.INSTANCE.getPlayersInCooldown().get(inviter.getName()) == null) {
                            inviter.sendMessage(getPlugin().getLangFile().get("info.invite-accepted").replace("{player}", sender.getName()));
                            sender.sendMessage(getPlugin().getLangFile().get("info.invite-accepted-self").replace("{player}", inviter.getName()));

                            TeleportType teleportType = type.equals(RequestType.Tpa) ? TeleportType.Tpa : TeleportType.TpaHere;
                            if (teleportType.equals(TeleportType.Tpa)) {
                                new Teleport(getPlugin(), sender, inviter.getName(), TeleportType.Tpa);
                            } else {
                                new Teleport(getPlugin(), inviter, sender.getName(), TeleportType.Tpa);
                            }
                        } else {
                            sender.sendMessage(getPlugin().getLangFile().get("error.other-already-waiting").replace("{player}", inviter.getName()));
                        }
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get("error.not-online").replace("{player}", name));
                    }

                    TpaUtils.playerExpiredChecker.get(name.toLowerCase() + "-" + sender.getName().toLowerCase() + "-" + type.toString()).cancel();
                    TpaUtils.playerExpiredChecker.remove(name.toLowerCase() + "-" + sender.getName().toLowerCase() + "-" + type.toString());
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get("error.no-pending-invite"));
                }
            } else {
                if (type != null) {
                    TpaUtils.playerExpiredChecker.get(name.toLowerCase() + "-" + sender.getName().toLowerCase() + "-" + type.toString()).cancel();
                    TpaUtils.playerExpiredChecker.remove(name.toLowerCase() + "-" + sender.getName().toLowerCase() + "-" + type.toString());
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get("error.no-pending-invite"));
                }
            }
        }
        return true;
    }
}
/*if (args.length > 0) {
                Player inviter = getPlugin().getServer().getPlayer(args[0]);
                String name = inviter == null ? args[0] : inviter.getName();
                RequestType type = BkTeleport.isExpiring(name.toLowerCase(), sender.getName().toLowerCase());
                if (type != null) {
                    if (inviter != null) {
                        if (TeleportTeleportCore.INSTANCE.getPlayersInCooldown().get(inviter.getName()) == null) {
                            inviter.sendMessage(getPlugin().getLangFile().get("info.invite-accepted", sender.getName()));
                            sender.sendMessage(getPlugin().getLangFile().get("info.invite-accepted-self", inviter.getName()));

                            TeleportType teleportType = type.equals(RequestType.Tpa) ? TeleportType.Tpa : TeleportType.TpaHere;
                            if (teleportType.equals(TeleportType.Tpa)) {
                                new Teleport(bkPlugin, sender, inviter.getName(), TeleportType.Tpa);
                            } else {
                                new Teleport(bkPlugin, inviter, sender.getName(), TeleportType.Tpa);
                            }
                        } else {
                            sender.sendMessage(getPlugin().getLangFile().get("error.other-already-waiting", inviter.getName()));
                        }
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get("error.not-online", name));
                    }

                    TpaUtils.playerExpiredChecker.get(name.toLowerCase() + "-" + sender.getName().toLowerCase() + "-" + type.toString()).cancel();
                    TpaUtils.playerExpiredChecker.remove(name.toLowerCase() + "-" + sender.getName().toLowerCase() + "-" + type.toString());
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get("error.no-pending-invite", name));
                }
            } else {
                sender.sendMessage(getPlugin().getLangFile().get("error.no-player-specified"));
            }*/