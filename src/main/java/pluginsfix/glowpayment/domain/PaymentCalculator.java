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

    public static PvPLossCalculation calculatePvPLoss(
        BigDecimal currentBalance,
        BigDecimal lossPercent,
        BigDecimal taxPercent,
        boolean taxEnabled
    ) {
        BigDecimal zero = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);

        if (currentBalance == null || currentBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return new PvPLossCalculation(zero, zero, zero, zero, zero, zero, zero);
        }

        BigDecimal scaledBalance = currentBalance.setScale(SCALE, RoundingMode.HALF_UP);
        if (lossPercent == null || lossPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return new PvPLossCalculation(scaledBalance, zero, zero, zero, zero, zero, scaledBalance);
        }

        BigDecimal scaledLossPercent = lossPercent.setScale(SCALE, RoundingMode.HALF_UP);
        if (scaledLossPercent.compareTo(HUNDRED) > 0) {
            scaledLossPercent = HUNDRED;
        }

        BigDecimal grossLossAmount = scaledBalance.multiply(scaledLossPercent)
            .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);

        if (grossLossAmount.compareTo(scaledBalance) > 0) {
            grossLossAmount = scaledBalance;
        }

        BigDecimal scaledTaxPercent = zero;
        BigDecimal taxAmount = zero;
        BigDecimal netReward = grossLossAmount;

        if (taxEnabled && taxPercent != null && taxPercent.compareTo(BigDecimal.ZERO) > 0) {
            scaledTaxPercent = taxPercent.setScale(SCALE, RoundingMode.HALF_UP);
            if (scaledTaxPercent.compareTo(HUNDRED) > 0) {
                scaledTaxPercent = HUNDRED;
            }
            taxAmount = grossLossAmount.multiply(scaledTaxPercent)
                .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);

            if (taxAmount.compareTo(grossLossAmount) > 0) {
                taxAmount = grossLossAmount;
            }
            netReward = grossLossAmount.subtract(taxAmount);
        }

        BigDecimal remainingBalance = scaledBalance.subtract(grossLossAmount);
        return new PvPLossCalculation(
            scaledBalance,
            scaledLossPercent,
            grossLossAmount,
            scaledTaxPercent,
            taxAmount,
            netReward,
            remainingBalance
        );
    }
}
