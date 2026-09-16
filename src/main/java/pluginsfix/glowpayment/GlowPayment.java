package pluginsfix.glowpayment;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pluginsfix.glowpayment.command.GlowPaymentCommand;
import pluginsfix.glowpayment.command.PayCommand;
import pluginsfix.glowpayment.config.PaymentConfig;
import pluginsfix.glowpayment.listener.PvPDeathListener;
import pluginsfix.glowpayment.service.PaymentService;
import pluginsfix.glowpayment.service.VaultEconomyHook;
import pluginsfix.glowpayment.text.Messages;

public final class GlowPayment extends JavaPlugin {

    private VaultEconomyHook economyHook;
    private Messages messages;
    private PaymentService paymentService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.messages = new Messages(this);
        this.economyHook = new VaultEconomyHook(this);

        PaymentConfig config = PaymentConfig.fromBukkit(getConfig());
        this.paymentService = new PaymentService(economyHook, messages, config);

        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        this.paymentService = null;
        this.economyHook = null;
        this.messages = null;
    }

    private void registerCommands() {
        PluginCommand payCmd = getCommand("pay");
        if (payCmd != null) {
            PayCommand payExecutor = new PayCommand(paymentService, messages);
            payCmd.setExecutor(payExecutor);
            payCmd.setTabCompleter(payExecutor);
        }

        PluginCommand adminCmd = getCommand("glowpayment");
        if (adminCmd != null) {
            GlowPaymentCommand adminExecutor = new GlowPaymentCommand(this, paymentService, messages);
            adminCmd.setExecutor(adminExecutor);
            adminCmd.setTabCompleter(adminExecutor);
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PvPDeathListener(paymentService), this);
    }
}
