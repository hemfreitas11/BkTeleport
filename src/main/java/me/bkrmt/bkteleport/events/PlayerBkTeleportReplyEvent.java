package me.bkrmt.bkteleport.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerBkTeleportReplyEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;

    private final Player playerWhoInvited;

    public PlayerBkTeleportReplyEvent(Player who, Player playerWhoInvited) {
        super(who);
        this.playerWhoInvited = playerWhoInvited;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public Player getPlayerWhoInvited() {
        return playerWhoInvited;
    }
}
