package pluginsfix.glowpayment.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentCalculatorTest {

    @Test
    void calculateTransferWithTax() {
        TransferCalculation calc = PaymentCalculator.calculateTransfer(
            new BigDecimal("1000.00"),
            new BigDecimal("5.0"),
            true
        );

        assertThat(calc.grossAmount()).isEqualByComparingTo("1000.00");
        assertThat(calc.taxPercent()).isEqualByComparingTo("5.00");
        assertThat(calc.taxAmount()).isEqualByComparingTo("50.00");
        assertThat(calc.netAmount()).isEqualByComparingTo("950.00");
    }

    @Test
    void calculateTransferTaxDisabled() {
        TransferCalculation calc = PaymentCalculator.calculateTransfer(
            new BigDecimal("1000.00"),
            new BigDecimal("5.0"),
            false
        );

        assertThat(calc.grossAmount()).isEqualByComparingTo("1000.00");
        assertThat(calc.taxPercent()).isEqualByComparingTo("0.00");
        assertThat(calc.taxAmount()).isEqualByComparingTo("0.00");
        assertThat(calc.netAmount()).isEqualByComparingTo("1000.00");
    }

    @Test
    void calculateTransferWithRounding() {
        TransferCalculation calc = PaymentCalculator.calculateTransfer(
            new BigDecimal("99.99"),
            new BigDecimal("3.5"),
            true
        );

        assertThat(calc.grossAmount()).isEqualByComparingTo("99.99");
        assertThat(calc.taxAmount()).isEqualByComparingTo("3.50");
        assertThat(calc.netAmount()).isEqualByComparingTo("96.49");
    }

    @Test
    void calculateTransferZeroOrNegativeThrows() {
        assertThatThrownBy(() -> PaymentCalculator.calculateTransfer(BigDecimal.ZERO, new BigDecimal("5.0"), true))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> PaymentCalculator.calculateTransfer(new BigDecimal("-10.0"), new BigDecimal("5.0"), true))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void calculatePvPLossNormal() {
        PvPLossCalculation loss = PaymentCalculator.calculatePvPLoss(
            new BigDecimal("5000.00"),
            new BigDecimal("10.0")
        );

        assertThat(loss.initialBalance()).isEqualByComparingTo("5000.00");
        assertThat(loss.lossPercent()).isEqualByComparingTo("10.00");
        assertThat(loss.lossAmount()).isEqualByComparingTo("500.00");
        assertThat(loss.remainingBalance()).isEqualByComparingTo("4500.00");
    }

    @Test
    void calculatePvPLossZeroBalance() {
        PvPLossCalculation loss = PaymentCalculator.calculatePvPLoss(
            BigDecimal.ZERO,
            new BigDecimal("10.0")
        );

        assertThat(loss.lossAmount()).isEqualByComparingTo("0.00");
        assertThat(loss.remainingBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void calculatePvPLossCappedAtHundredPercent() {
        PvPLossCalculation loss = PaymentCalculator.calculatePvPLoss(
            new BigDecimal("100.00"),
            new BigDecimal("150.0")
        );

        assertThat(loss.lossPercent()).isEqualByComparingTo("100.00");
        assertThat(loss.lossAmount()).isEqualByComparingTo("100.00");
        assertThat(loss.remainingBalance()).isEqualByComparingTo("0.00");
    }
}
