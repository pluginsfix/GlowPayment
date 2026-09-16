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

    private final EconomyProvider economy;
    private final Messages messages;
    private volatile PaymentConfig config;

    public PaymentService(EconomyProvider economy, Messages messages, PaymentConfig config) {
        this.economy = economy;
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
        return economy.isAvailable();
    }

    public boolean executeTransfer(Player sender, Player target, BigDecimal rawAmount) {
        if (sender == null || target == null) {
            return false;
        }

        if (!economy.isAvailable()) {
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

        BigDecimal senderBalance = economy.getBalance(sender);
        if (senderBalance.compareTo(grossAmount) < 0) {
            messages.send(sender, "pay.insufficient-funds", Placeholder.parsed("balance", format(senderBalance)));
            return false;
        }

        boolean bypassTax = sender.hasPermission("glowpayment.bypass.tax");
        boolean applyTax = config.taxEnabled() && !bypassTax;

        TransferCalculation calc = PaymentCalculator.calculateTransfer(grossAmount, config.taxPercent(), applyTax);

        boolean withdrawn = economy.withdraw(sender, calc.grossAmount());
        if (!withdrawn) {
            BigDecimal currentBalance = economy.getBalance(sender);
            messages.send(sender, "pay.insufficient-funds", Placeholder.parsed("balance", format(currentBalance)));
            return false;
        }

        boolean deposited = economy.deposit(target, calc.netAmount());
        if (!deposited) {
            economyHookDepositSender(sender, calc.grossAmount());
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
            messages.send(
                target,
                "pay.received-with-tax",
                Placeholder.parsed("sender", sender.getName()),
                Placeholder.parsed("net", format(calc.netAmount())),
                Placeholder.parsed("tax", format(calc.taxAmount()))
            );
        } else {
            messages.send(
                sender,
                "pay.sent-no-tax",
                Placeholder.parsed("target", target.getName()),
                Placeholder.parsed("gross", format(calc.grossAmount())),
                Placeholder.parsed("net", format(calc.netAmount()))
            );
            messages.send(
                target,
                "pay.received",
                Placeholder.parsed("sender", sender.getName()),
                Placeholder.parsed("net", format(calc.netAmount()))
            );
        }

        return true;
    }

    private void economyHookDepositSender(Player sender, BigDecimal amount) {
        economy.deposit(sender, amount);
    }

    public void processPvPDeath(Player victim, Player killer) {
        if (!config.pvpEnabled() || victim == null || killer == null || !economy.isAvailable()) {
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

        BigDecimal balance = economy.getBalance(victim);
        if (balance.compareTo(config.pvpMinBalanceThreshold()) < 0) {
            return;
        }

        PvPLossCalculation loss = PaymentCalculator.calculatePvPLoss(
            balance,
            config.pvpLossPercent(),
            config.pvpTaxPercent(),
            config.pvpTaxEnabled()
        );

        if (loss.grossLossAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        boolean withdrawn = economy.withdraw(victim, loss.grossLossAmount());
        if (!withdrawn) {
            return;
        }

        if (config.pvpTransferToKiller()) {
            economy.deposit(killer, loss.netKillerReward());
            if (loss.taxAmount().compareTo(BigDecimal.ZERO) > 0) {
                messages.send(
                    killer,
                    "pvp.killer-reward-with-tax",
                    Placeholder.parsed("victim", victim.getName()),
                    Placeholder.parsed("net", format(loss.netKillerReward())),
                    Placeholder.parsed("gross", format(loss.grossLossAmount())),
                    Placeholder.parsed("tax", format(loss.taxAmount())),
                    Placeholder.parsed("percent", format(loss.taxPercent()))
                );
            } else {
                messages.send(
                    killer,
                    "pvp.killer-reward",
                    Placeholder.parsed("victim", victim.getName()),
                    Placeholder.parsed("reward", format(loss.netKillerReward())),
                    Placeholder.parsed("percent", format(loss.lossPercent()))
                );
            }
        } else {
            messages.send(
                killer,
                "pvp.killer-burned",
                Placeholder.parsed("victim", victim.getName()),
                Placeholder.parsed("lost", format(loss.grossLossAmount()))
            );
        }

        messages.send(
            victim,
            "pvp.victim-lost",
            Placeholder.parsed("killer", killer.getName()),
            Placeholder.parsed("lost", format(loss.grossLossAmount())),
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
