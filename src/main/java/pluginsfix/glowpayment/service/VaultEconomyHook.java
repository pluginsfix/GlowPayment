package pluginsfix.glowpayment.service;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.logging.Logger;

public final class VaultEconomyHook {

    private final JavaPlugin plugin;
    private final Logger logger;
    private Economy economy;

    public VaultEconomyHook(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        setupEconomy();
    }

    public boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            logger.severe("Vault plugin not found! GlowPayment requires Vault.");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            logger.severe("No economy provider registered in Vault!");
            return false;
        }

        this.economy = rsp.getProvider();
        return this.economy != null;
    }

    public boolean isAvailable() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public BigDecimal getBalance(OfflinePlayer player) {
        if (economy == null || player == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(economy.getBalance(player));
    }

    public boolean has(OfflinePlayer player, BigDecimal amount) {
        if (economy == null || player == null || amount == null) {
            return false;
        }
        return economy.has(player, amount.doubleValue());
    }

    public boolean withdraw(OfflinePlayer player, BigDecimal amount) {
        if (economy == null || player == null || amount == null) {
            return false;
        }
        EconomyResponse response = economy.withdrawPlayer(player, amount.doubleValue());
        return response.transactionSuccess();
    }

    public boolean deposit(OfflinePlayer player, BigDecimal amount) {
        if (economy == null || player == null || amount == null) {
            return false;
        }
        EconomyResponse response = economy.depositPlayer(player, amount.doubleValue());
        return response.transactionSuccess();
    }
}
