package pluginsfix.glowpayment.service;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import pluginsfix.glowpayment.config.PaymentConfig;
import pluginsfix.glowpayment.domain.PaymentCalculator;
import pluginsfix.glowpayment.domain.PvPLossCalculation;
import pluginsfix.glowpayment.domain.TransferCalculation;
import pluginsfix.glowpayment.text.Messages;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class PaymentService {

    private final VaultEconomyHook economyHook;
    private final Messages messages;
    private volatile PaymentConfig config;

    public PaymentService(VaultEconomyHook economyHook, Messages messages, PaymentConfig config) {
        this.economyHook = economyHook;
        this.messages = messages;
        this.config = config;
    }

    public void updateConfig(PaymentConfig config) {
        this.config = config;
    }

    public PaymentConfig getConfig() {
        return config;
    }

    public boolean isEconomyAvailable() {
        return economyHook.isAvailable();
    }

    public boolean executeTransfer(Player sender, Player target, BigDecimal rawAmount) {
        if (sender == null || target == null) {
            return false;
        }

        if (!economyHook.isAvailable()) {
            messages.send(sender, "economy.not-found");
            return false;
        }

        if (sender.getUniqueId().equals(target.getUniqueId())) {
            messages.send(sender, "pay.self-transfer");
            return false;
        }

        if (rawAmount == null || rawAmount.compareTo(BigDecimal.ZERO) <= 0) {
            messages.send(sender, "pay.invalid-amount");
            return false;
        }

        BigDecimal grossAmount = rawAmount.setScale(2, RoundingMode.HALF_UP);

        if (config.minAmount().compareTo(BigDecimal.ZERO) > 0 && grossAmount.compareTo(config.minAmount()) < 0) {
            messages.send(sender, "pay.min-amount", Placeholder.parsed("min", format(config.minAmount())));
            return false;
        }

        if (config.maxAmount().compareTo(BigDecimal.ZERO) > 0 && grossAmount.compareTo(config.maxAmount()) > 0) {
            messages.send(sender, "pay.max-amount", Placeholder.parsed("max", format(config.maxAmount())));
            return false;
        }

        BigDecimal senderBalance = economyHook.getBalance(sender);
        if (senderBalance.compareTo(grossAmount) < 0) {
            messages.send(sender, "pay.insufficient-funds", Placeholder.parsed("balance", format(senderBalance)));
            return false;
        }

        boolean bypassTax = sender.hasPermission("glowpayment.bypass.tax");
        boolean applyTax = config.taxEnabled() && !bypassTax;

        TransferCalculation calc = PaymentCalculator.calculateTransfer(grossAmount, config.taxPercent(), applyTax);

        boolean withdrawn = economyHook.withdraw(sender, calc.grossAmount());
        if (!withdrawn) {
            BigDecimal currentBalance = economyHook.getBalance(sender);
            messages.send(sender, "pay.insufficient-funds", Placeholder.parsed("balance", format(currentBalance)));
            return false;
        }

        boolean deposited = economyHook.deposit(target, calc.netAmount());
        if (!deposited) {
            economyHook.deposit(sender, calc.grossAmount());
            messages.send(sender, "pay.invalid-amount");
            return false;
        }

        if (calc.taxAmount().compareTo(BigDecimal.ZERO) > 0) {
            messages.send(
                sender,
                "pay.sent-with-tax",
                Placeholder.parsed("target", target.getName()),
                Placeholder.parsed("gross", format(calc.grossAmount())),
                Placeholder.parsed("tax", format(calc.taxAmount())),
                Placeholder.parsed("percent", format(calc.taxPercent())),
                Placeholder.parsed("net", format(calc.netAmount()))
            );
        } else {
            messages.send(
                sender,
                "pay.sent-no-tax",
                Placeholder.parsed("target", target.getName()),
                Placeholder.parsed("gross", format(calc.grossAmount())),
                Placeholder.parsed("net", format(calc.netAmount()))
            );
        }

        messages.send(
            target,
            "pay.received",
            Placeholder.parsed("sender", sender.getName()),
            Placeholder.parsed("net", format(calc.netAmount()))
        );

        return true;
    }

    public void processPvPDeath(Player victim, Player killer) {
        if (!config.pvpEnabled() || victim == null || killer == null || !economyHook.isAvailable()) {
            return;
        }

        if (victim.getUniqueId().equals(killer.getUniqueId())) {
            return;
        }

        if (victim.hasPermission("glowpayment.bypass.pvploss")) {
            return;
        }

        String worldName = victim.getWorld().getName();
        if (config.pvpDisabledWorlds().contains(worldName)) {
            return;
        }

        BigDecimal balance = economyHook.getBalance(victim);
        if (balance.compareTo(config.pvpMinBalanceThreshold()) < 0) {
            return;
        }

        PvPLossCalculation loss = PaymentCalculator.calculatePvPLoss(balance, config.pvpLossPercent());
        if (loss.lossAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        boolean withdrawn = economyHook.withdraw(victim, loss.lossAmount());
        if (!withdrawn) {
            return;
        }

        if (config.pvpTransferToKiller()) {
            economyHook.deposit(killer, loss.lossAmount());
            messages.send(
                killer,
                "pvp.killer-reward",
                Placeholder.parsed("victim", victim.getName()),
                Placeholder.parsed("reward", format(loss.lossAmount())),
                Placeholder.parsed("percent", format(loss.lossPercent()))
            );
        } else {
            messages.send(
                killer,
                "pvp.killer-burned",
                Placeholder.parsed("victim", victim.getName()),
                Placeholder.parsed("lost", format(loss.lossAmount()))
            );
        }

        messages.send(
            victim,
            "pvp.victim-lost",
            Placeholder.parsed("killer", killer.getName()),
            Placeholder.parsed("lost", format(loss.lossAmount())),
            Placeholder.parsed("percent", format(loss.lossPercent()))
        );
    }

    private String format(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return String.format(Locale.US, "%.2f", value.doubleValue());
    }
}
