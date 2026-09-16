package pluginsfix.glowpayment.domain;

import java.math.BigDecimal;

public record PvPLossCalculation(
    BigDecimal initialBalance,
    BigDecimal lossPercent,
    BigDecimal lossAmount,
    BigDecimal remainingBalance
) {
    public PvPLossCalculation {
        if (initialBalance == null || lossPercent == null || lossAmount == null || remainingBalance == null) {
            throw new IllegalArgumentException("Amounts cannot be null");
        }
    }
}
