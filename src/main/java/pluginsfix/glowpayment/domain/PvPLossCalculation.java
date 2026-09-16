package pluginsfix.glowpayment.domain;

import java.math.BigDecimal;

public record PvPLossCalculation(
    BigDecimal initialBalance,
    BigDecimal lossPercent,
    BigDecimal grossLossAmount,
    BigDecimal taxPercent,
    BigDecimal taxAmount,
    BigDecimal netKillerReward,
    BigDecimal remainingBalance
) {
    public PvPLossCalculation {
        if (initialBalance == null || lossPercent == null || grossLossAmount == null ||
            taxPercent == null || taxAmount == null || netKillerReward == null || remainingBalance == null) {
            throw new IllegalArgumentException("Amounts cannot be null");
        }
    }
}
