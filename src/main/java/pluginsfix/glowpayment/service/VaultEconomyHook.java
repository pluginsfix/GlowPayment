package pluginsfix.glowpayment.service;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.logging.Logger;

public final class VaultEconomyHook implements Listener {

    private final JavaPlugin plugin;
    private final Logger logger;
    private Economy economy;

    public VaultEconomyHook(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        setupEconomy();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public boolean setupEconomy() {
        if (this.economy != null) {
            return true;
        }

        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            logger.warning("Vault plugin not detected on server.");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        this.economy = rsp.getProvider();
        if (this.economy != null) {
            logger.info("Successfully hooked into Vault Economy provider: " + this.economy.getName());
            return true;
        }

        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (Economy.class.isAssignableFrom(event.getProvider().getService())) {
            setupEconomy();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServiceUnregister(ServiceUnregisterEvent event) {
        if (Economy.class.isAssignableFrom(event.getProvider().getService())) {
            this.economy = null;
            setupEconomy();
        }
    }

    public boolean isAvailable() {
        if (economy == null) {
            setupEconomy();
        }
        return economy != null;
    }

    public Economy getEconomy() {
        if (economy == null) {
            setupEconomy();
        }
        return economy;
    }

    public BigDecimal getBalance(OfflinePlayer player) {
        if (!isAvailable() || player == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(economy.getBalance(player));
    }

    public boolean has(OfflinePlayer player, BigDecimal amount) {
        if (!isAvailable() || player == null || amount == null) {
            return false;
        }
        return economy.has(player, amount.doubleValue());
    }

    public boolean withdraw(OfflinePlayer player, BigDecimal amount) {
        if (!isAvailable() || player == null || amount == null) {
            return false;
        }
        EconomyResponse response = economy.withdrawPlayer(player, amount.doubleValue());
        return response.transactionSuccess();
    }

    public boolean deposit(OfflinePlayer player, BigDecimal amount) {
        if (!isAvailable() || player == null || amount == null) {
            return false;
        }
        EconomyResponse response = economy.depositPlayer(player, amount.doubleValue());
        return response.transactionSuccess();
    }
}
