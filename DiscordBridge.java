package de.discordbridge;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class DiscordBridge extends JavaPlugin {

    private volatile JDA jda;
    private volatile TextChannel channel;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new MinecraftListener(this), this);
        startBot();
    }

    @Override
    public void onDisable() {
        stopBot();
    }

    /** Startet den Bot asynchron, damit der Server-Start nicht blockiert wird. */
    private void startBot() {
        String token = System.getenv("DISCORD_TOKEN");
        if (token == null || token.isBlank()) {
            token = getConfig().getString("token", "");
        }
        if (token == null || token.isBlank()) {
            getLogger().warning("Kein Bot-Token gesetzt! Trage ihn in der config.yml ein und nutze /discordbridge reload.");
            return;
        }

        final String finalToken = token;
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                JDA instance = JDABuilder.createLight(finalToken,
                                GatewayIntent.GUILD_MESSAGES,
                                GatewayIntent.MESSAGE_CONTENT)
                        .setActivity(Activity.playing(getConfig().getString("activity", "Minecraft")))
                        .addEventListeners(new DiscordListener(this))
                        .build();
                instance.awaitReady();
                this.jda = instance;

                instance.updateCommands().addCommands(
                        Commands.slash("players", "Zeigt die Spieler, die gerade online sind")
                ).queue();

                String channelId = getConfig().getString("channel-id", "");
                if (!channelId.isBlank()) {
                    channel = instance.getTextChannelById(channelId);
                }
                if (channel == null) {
                    getLogger().warning("Kanal mit der ID '" + channelId + "' wurde nicht gefunden. Chat-Brücke ist inaktiv.");
                }

                getLogger().info("Discord-Bot verbunden als " + instance.getSelfUser().getName());
                if (getConfig().getBoolean("bridge.server-status-messages")) {
                    sendToDiscord(getConfig().getString("messages.server-start", "Server online"));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                getLogger().severe("Discord-Bot konnte nicht gestartet werden: " + e.getMessage());
            }
        });
    }

    private void stopBot() {
        JDA instance = this.jda;
        if (instance == null) return;
        try {
            TextChannel c = channel;
            if (c != null && getConfig().getBoolean("bridge.server-status-messages")) {
                c.sendMessage(getConfig().getString("messages.server-stop", "Server offline")).complete();
            }
        } catch (Exception ignored) {
        }
        instance.shutdown();
        try {
            if (!instance.awaitShutdown(5, TimeUnit.SECONDS)) {
                instance.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.jda = null;
        this.channel = null;
    }

    /** Sendet eine Nachricht in den konfigurierten Kanal (ohne @everyone/@here-Pings). */
    public void sendToDiscord(String text) {
        TextChannel c = this.channel;
        if (c == null) return;
        c.sendMessage(text)
                .setAllowedMentions(EnumSet.noneOf(Message.MentionType.class))
                .queue(null, err -> getLogger().warning("Senden an Discord fehlgeschlagen: " + err.getMessage()));
    }

    public boolean isConnected() {
        return jda != null && channel != null;
    }

    public String getChannelId() {
        return getConfig().getString("channel-id", "");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§7Nutzung: /" + label + " <status|reload>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "status" -> sender.sendMessage(jda == null
                    ? "§cDer Bot ist nicht verbunden."
                    : "§aBot verbunden als §f" + jda.getSelfUser().getName()
                      + " §7(Ping: " + jda.getGatewayPing() + " ms)");
            case "reload" -> {
                sender.sendMessage("§eLade Konfiguration neu und starte den Bot neu...");
                stopBot();
                reloadConfig();
                startBot();
            }
            default -> sender.sendMessage("§7Nutzung: /" + label + " <status|reload>");
        }
        return true;
    }
}
