package pluginsfix.glowpayment.service;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;

public final class EconomyManager implements EconomyProvider {

    private final VaultEconomyBridge vaultBridge;
    private final PlayerPointsBridge playerPointsBridge;

    public EconomyManager(JavaPlugin plugin) {
        this.vaultBridge = new VaultEconomyBridge(plugin);
        this.playerPointsBridge = new PlayerPointsBridge(plugin.getLogger());
    }

    private EconomyProvider getActiveProvider() {
        if (vaultBridge.isAvailable()) {
            return vaultBridge;
        }
        if (playerPointsBridge.isAvailable()) {
            return playerPointsBridge;
        }
        return null;
    }

    @Override
    public boolean isAvailable() {
        return getActiveProvider() != null;
    }

    @Override
    public String getName() {
        EconomyProvider provider = getActiveProvider();
        return provider != null ? provider.getName() : "None";
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        EconomyProvider provider = getActiveProvider();
        return provider != null ? provider.getBalance(player) : BigDecimal.ZERO;
    }

    @Override
    public boolean has(OfflinePlayer player, BigDecimal amount) {
        EconomyProvider provider = getActiveProvider();
        return provider != null && provider.has(player, amount);
    }

    @Override
    public boolean withdraw(OfflinePlayer player, BigDecimal amount) {
        EconomyProvider provider = getActiveProvider();
        return provider != null && provider.withdraw(player, amount);
    }

    @Override
    public boolean deposit(OfflinePlayer player, BigDecimal amount) {
        EconomyProvider provider = getActiveProvider();
        return provider != null && provider.deposit(player, amount);
    }
}
