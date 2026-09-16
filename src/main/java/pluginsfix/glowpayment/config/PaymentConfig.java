package pluginsfix.glowpayment.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record PaymentConfig(
    boolean taxEnabled,
    BigDecimal taxPercent,
    BigDecimal minAmount,
    BigDecimal maxAmount,
    boolean pvpEnabled,
    BigDecimal pvpLossPercent,
    boolean pvpTransferToKiller,
    BigDecimal pvpMinBalanceThreshold,
    Set<String> pvpDisabledWorlds
) {
    public static PaymentConfig fromBukkit(FileConfiguration config) {
        boolean taxEnabled = config.getBoolean("transfer.tax-enabled", true);
        BigDecimal taxPercent = BigDecimal.valueOf(config.getDouble("transfer.tax-percent", 5.0));
        BigDecimal minAmount = BigDecimal.valueOf(config.getDouble("transfer.min-amount", 1.0));
        BigDecimal maxAmount = BigDecimal.valueOf(config.getDouble("transfer.max-amount", 1000000.0));

        boolean pvpEnabled = config.getBoolean("pvp.enabled", true);
        BigDecimal pvpLossPercent = BigDecimal.valueOf(config.getDouble("pvp.loss-percent", 5.0));
        boolean pvpTransferToKiller = config.getBoolean("pvp.transfer-to-killer", true);
        BigDecimal pvpMinBalanceThreshold = BigDecimal.valueOf(config.getDouble("pvp.min-balance-threshold", 1.0));

        List<String> disabledWorldsList = config.getStringList("pvp.disabled-worlds");
        Set<String> pvpDisabledWorlds = new HashSet<>(disabledWorldsList);

        return new PaymentConfig(
            taxEnabled,
            taxPercent,
            minAmount,
            maxAmount,
            pvpEnabled,
            pvpLossPercent,
            pvpTransferToKiller,
            pvpMinBalanceThreshold,
            Set.copyOf(pvpDisabledWorlds)
        );
    }
}
