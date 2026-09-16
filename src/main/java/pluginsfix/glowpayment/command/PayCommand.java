package pluginsfix.glowpayment.command;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pluginsfix.glowpayment.service.PaymentService;
import pluginsfix.glowpayment.text.Messages;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PayCommand implements CommandExecutor, TabCompleter {

    private final PaymentService paymentService;
    private final Messages messages;

    public PayCommand(PaymentService paymentService, Messages messages) {
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
        if (!sender.hasPermission("glowpayment.use")) {
            messages.send(sender, "command.no-permission");
            return true;
        }

        if (!(sender instanceof Player player)) {
            messages.send(sender, "command.player-only");
            return true;
        }

        if (args.length < 2) {
            messages.send(player, "command.pay-usage");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || !target.isOnline()) {
            messages.send(player, "pay.player-not-found", Placeholder.parsed("target", targetName));
            return true;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(args[1]);
        } catch (NumberFormatException e) {
            messages.send(player, "pay.invalid-amount");
            return true;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            messages.send(player, "pay.invalid-amount");
            return true;
        }

        paymentService.executeTransfer(player, target, amount);
        return true;
    }

    @Override
    public List<String> onTabComplete(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args
    ) {
        if (args.length == 1) {
            String query = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (sender instanceof Player p && p.getUniqueId().equals(online.getUniqueId())) {
                    continue;
                }
                if (online.getName().toLowerCase().startsWith(query)) {
                    matches.add(online.getName());
                }
            }
            return matches;
        }

        if (args.length == 2) {
            return List.of("10", "50", "100", "500", "1000");
        }

        return Collections.emptyList();
    }
}
