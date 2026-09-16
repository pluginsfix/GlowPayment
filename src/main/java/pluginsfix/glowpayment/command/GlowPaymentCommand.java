package pluginsfix.glowpayment.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pluginsfix.glowpayment.config.PaymentConfig;
import pluginsfix.glowpayment.service.PaymentService;
import pluginsfix.glowpayment.text.Messages;

import java.util.Collections;
import java.util.List;

public final class GlowPaymentCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final PaymentService paymentService;
    private final Messages messages;

    public GlowPaymentCommand(JavaPlugin plugin, PaymentService paymentService, Messages messages) {
        this.plugin = plugin;
        this.paymentService = paymentService;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args
    ) {
        if (!sender.hasPermission("glowpayment.admin")) {
            messages.send(sender, "command.no-permission");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            PaymentConfig newConfig = PaymentConfig.fromBukkit(plugin.getConfig());
            paymentService.updateConfig(newConfig);
            messages.reload();
            messages.send(sender, "command.reloaded");
            return true;
        }

        messages.send(sender, "command.admin-usage");
        return true;
    }

    @Override
    public List<String> onTabComplete(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args
    ) {
        if (args.length == 1 && sender.hasPermission("glowpayment.admin")) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                return List.of("reload");
            }
        }
        return Collections.emptyList();
    }
}
