package pluginsfix.glowpayment.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public final class Messages {

    private final JavaPlugin plugin;
    private final Logger logger;
    private final MiniMessage miniMessage;
    private YamlConfiguration configuration;

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.miniMessage = MiniMessage.miniMessage();
        load();
    }

    public void load() {
        Path messagesPath = plugin.getDataFolder().toPath().resolve("messages.yml");
        if (!Files.exists(messagesPath)) {
            plugin.saveResource("messages.yml", false);
        }
        this.configuration = YamlConfiguration.loadConfiguration(messagesPath.toFile());
    }

    public void reload() {
        load();
    }

    public Component get(String key, TagResolver... tagResolvers) {
        String rawText = configuration.getString(key);
        if (rawText == null) {
            logger.warning("Missing message key in messages.yml: " + key);
            return Component.empty();
        }

        String prefix = configuration.getString("prefix");
        String fullText = (prefix != null && !key.equals("prefix")) ? prefix + rawText : rawText;

        return miniMessage.deserialize(fullText, tagResolvers);
    }

    public void send(CommandSender sender, String key, TagResolver... tagResolvers) {
        if (sender == null) {
            return;
        }
        Component component = get(key, tagResolvers);
        if (component.equals(Component.empty())) {
            return;
        }
        sender.sendMessage(component);
    }
}
