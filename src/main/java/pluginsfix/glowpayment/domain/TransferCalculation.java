package pluginsfix.glowpayment.domain;

import java.math.BigDecimal;

public record TransferCalculation(
    BigDecimal grossAmount,
    BigDecimal taxPercent,
    BigDecimal taxAmount,
    BigDecimal netAmount
) {
    public TransferCalculation {
        if (grossAmount == null || taxPercent == null || taxAmount == null || netAmount == null) {
            throw new IllegalArgumentException("Amounts cannot be null");
        }
    }
}
