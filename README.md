# DiscordBridge

Paper-Plugin (1.21+), das einen Discord-Bot startet und den Chat zwischen Minecraft und Discord verbindet.

## Bauen
Java 21 und Maven nötig:

    mvn clean package

Die fertige Datei liegt in `target/DiscordBridge.jar` -> in den `plugins`-Ordner kopieren.
Beim ersten Start lädt Paper JDA automatisch herunter (Server braucht Internet).

## Bot einrichten
1. https://discord.com/developers/applications -> New Application -> Bot -> Token kopieren
2. Beim Bot unter "Privileged Gateway Intents" **Message Content Intent** aktivieren
3. OAuth2 -> URL Generator: Scopes `bot` + `applications.commands`,
   Rechte: View Channels, Send Messages, Read Message History -> Bot einladen
4. In Discord Entwicklermodus aktivieren, Rechtsklick auf den Kanal -> ID kopieren
5. `plugins/DiscordBridge/config.yml`: `token` und `channel-id` eintragen
6. `/discordbridge reload`

## Befehle
- `/discordbridge status` - zeigt Verbindungsstatus
- `/discordbridge reload` - lädt Config neu und startet Bot neu
- Discord: `/players` - zeigt Online-Spieler

## Build über GitHub Actions
Die Datei `.github/workflows/build.yml` baut das Plugin automatisch:
- Bei jedem Push und Pull Request -> JAR unter *Actions -> Lauf auswählen -> Artifacts*
- Manuell über *Actions -> Build -> Run workflow*
- Bei einem Tag wie `v1.0.0` wird zusätzlich ein Release mit der JAR erstellt:

      git tag v1.0.0
      git push origin v1.0.0
