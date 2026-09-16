package pluginsfix.glowpayment.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PaymentCalculator {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int SCALE = 2;

    public static TransferCalculation calculateTransfer(BigDecimal grossAmount, BigDecimal taxPercent, boolean taxEnabled) {
        if (grossAmount == null || grossAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Gross amount must be positive");
        }

        BigDecimal scaledGross = grossAmount.setScale(SCALE, RoundingMode.HALF_UP);

        if (!taxEnabled || taxPercent == null || taxPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return new TransferCalculation(
                scaledGross,
                BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP),
                BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP),
                scaledGross
            );
        }

        BigDecimal scaledTaxPercent = taxPercent.setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal taxAmount = scaledGross.multiply(scaledTaxPercent)
            .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);

        if (taxAmount.compareTo(scaledGross) > 0) {
            taxAmount = scaledGross;
        }

        BigDecimal netAmount = scaledGross.subtract(taxAmount);
        return new TransferCalculation(scaledGross, scaledTaxPercent, taxAmount, netAmount);
    }

    public static PvPLossCalculation calculatePvPLoss(BigDecimal currentBalance, BigDecimal lossPercent) {
        if (currentBalance == null || currentBalance.compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal zero = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
            return new PvPLossCalculation(zero, zero, zero, zero);
        }

        BigDecimal scaledBalance = currentBalance.setScale(SCALE, RoundingMode.HALF_UP);
        if (lossPercent == null || lossPercent.compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal zero = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
            return new PvPLossCalculation(scaledBalance, zero, zero, scaledBalance);
        }

        BigDecimal scaledPercent = lossPercent.setScale(SCALE, RoundingMode.HALF_UP);
        if (scaledPercent.compareTo(HUNDRED) > 0) {
            scaledPercent = HUNDRED;
        }

        BigDecimal lossAmount = scaledBalance.multiply(scaledPercent)
            .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);

        if (lossAmount.compareTo(scaledBalance) > 0) {
            lossAmount = scaledBalance;
        }

        BigDecimal remainingBalance = scaledBalance.subtract(lossAmount);
        return new PvPLossCalculation(scaledBalance, scaledPercent, lossAmount, remainingBalance);
    }
}
