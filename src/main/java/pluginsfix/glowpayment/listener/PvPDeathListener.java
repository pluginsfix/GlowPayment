package pluginsfix.glowpayment.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import pluginsfix.glowpayment.service.PaymentService;

public final class PvPDeathListener implements Listener {

    private final PaymentService paymentService;

    public PvPDeathListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) {
            return;
        }

        paymentService.processPvPDeath(victim, killer);
    }
}
