package pluginsfix.glowpayment.service;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.logging.Logger;

public final class PlayerPointsBridge implements EconomyProvider {

    private final Logger logger;
    private Object playerPointsApi;
    private Method lookMethod;
    private Method takeMethod;
    private Method giveMethod;

    public PlayerPointsBridge(Logger logger) {
        this.logger = logger;
        setup();
    }

    public boolean setup() {
        if (playerPointsApi != null) {
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (plugin == null || !plugin.isEnabled()) {
            return false;
        }

        try {
            Class<?> mainClass = Class.forName("org.black_ixx.playerpoints.PlayerPoints");
            Method getInstanceMethod = mainClass.getMethod("getInstance");
            Object instance = getInstanceMethod.invoke(null);
            if (instance == null) {
                return false;
            }

            Method getApiMethod = mainClass.getMethod("getAPI");
            this.playerPointsApi = getApiMethod.invoke(instance);
            if (this.playerPointsApi == null) {
                return false;
            }

            Class<?> apiClass = this.playerPointsApi.getClass();
            this.lookMethod = apiClass.getMethod("look", UUID.class);
            this.takeMethod = apiClass.getMethod("take", UUID.class, int.class);
            this.giveMethod = apiClass.getMethod("give", UUID.class, int.class);

            logger.info("Hooked into PlayerPoints currency provider.");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        if (playerPointsApi == null) {
            setup();
        }
        return playerPointsApi != null;
    }

    @Override
    public String getName() {
        return "PlayerPoints";
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        if (!isAvailable() || player == null) {
            return BigDecimal.ZERO;
        }
        try {
            int balance = (int) lookMethod.invoke(playerPointsApi, player.getUniqueId());
            return BigDecimal.valueOf(balance);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public boolean has(OfflinePlayer player, BigDecimal amount) {
        if (!isAvailable() || player == null || amount == null) {
            return false;
        }
        return getBalance(player).compareTo(amount) >= 0;
    }

    @Override
    public boolean withdraw(OfflinePlayer player, BigDecimal amount) {
        if (!isAvailable() || player == null || amount == null) {
            return false;
        }
        try {
            int value = amount.intValue();
            return (boolean) takeMethod.invoke(playerPointsApi, player.getUniqueId(), value);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deposit(OfflinePlayer player, BigDecimal amount) {
        if (!isAvailable() || player == null || amount == null) {
            return false;
        }
        try {
            int value = amount.intValue();
            return (boolean) giveMethod.invoke(playerPointsApi, player.getUniqueId(), value);
        } catch (Exception e) {
            return false;
        }
    }
}
