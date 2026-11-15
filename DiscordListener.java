package de.discordbridge;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

/** Verarbeitet Discord-Nachrichten und Slash-Commands. */
public class DiscordListener extends ListenerAdapter {

    private final DiscordBridge plugin;

    public DiscordListener(DiscordBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!plugin.getConfig().getBoolean("bridge.discord-to-minecraft")) return;
        if (event.getAuthor().isBot() || event.isWebhookMessage()) return;
        if (!event.getChannel().getId().equals(plugin.getChannelId())) return;

        String content = event.getMessage().getContentDisplay();
        if (content.isBlank()) return;

        String name = event.getMember() != null
                ? event.getMember().getEffectiveName()
                : event.getAuthor().getName();

        Component line = Component.text("[Discord] ", NamedTextColor.BLUE)
                .append(Component.text(name, NamedTextColor.GRAY))
                .append(Component.text(": " + content, NamedTextColor.WHITE));
        Bukkit.broadcast(line);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("players")) return;

        var players = Bukkit.getOnlinePlayers();
        if (players.isEmpty()) {
            event.reply("Gerade ist niemand online.").queue();
            return;
        }
        String list = players.stream().map(Player::getName).collect(Collectors.joining(", "));
        event.reply("**" + players.size() + " Spieler online:** " + list).queue();
    }
}
