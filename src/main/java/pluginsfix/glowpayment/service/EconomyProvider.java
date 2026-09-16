package pluginsfix.glowpayment.service;

import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public interface EconomyProvider {
    boolean isAvailable();
    String getName();
    BigDecimal getBalance(OfflinePlayer player);
    boolean has(OfflinePlayer player, BigDecimal amount);
    boolean withdraw(OfflinePlayer player, BigDecimal amount);
    boolean deposit(OfflinePlayer player, BigDecimal amount);
}
