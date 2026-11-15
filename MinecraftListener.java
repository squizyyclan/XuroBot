package de.discordbridge;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Leitet Minecraft-Ereignisse nach Discord weiter. */
public class MinecraftListener implements Listener {

    private final DiscordBridge plugin;

    public MinecraftListener(DiscordBridge plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!plugin.getConfig().getBoolean("bridge.minecraft-to-discord")) return;
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        String text = plugin.getConfig().getString("messages.chat", "%player%: %message%")
                .replace("%player%", event.getPlayer().getName())
                .replace("%message%", message);
        plugin.sendToDiscord(text);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("bridge.join-quit-messages")) return;
        plugin.sendToDiscord(plugin.getConfig().getString("messages.join", "%player% joined")
                .replace("%player%", event.getPlayer().getName()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!plugin.getConfig().getBoolean("bridge.join-quit-messages")) return;
        plugin.sendToDiscord(plugin.getConfig().getString("messages.quit", "%player% left")
                .replace("%player%", event.getPlayer().getName()));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.getConfig().getBoolean("bridge.death-messages")) return;
        if (event.deathMessage() == null) return;
        String msg = PlainTextComponentSerializer.plainText().serialize(event.deathMessage());
        plugin.sendToDiscord(plugin.getConfig().getString("messages.death", "%message%")
                .replace("%message%", msg));
    }
}
