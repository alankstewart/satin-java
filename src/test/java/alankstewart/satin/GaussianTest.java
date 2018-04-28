package alankstewart.satin;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertTrue;

public class GaussianTest {

    private final Gaussian gaussian = new Gaussian(150, 179.13933259214042, 25000);

    @Test
    public void shouldReturnOutputPowerAsBigDecimal() {
        assertTrue(gaussian.outputPower.compareTo(new BigDecimal("179.139")) == 0);
    }

    @Test
    public void shouldReturnLogOutputPowerDividedByInputPowerAsBigDecimal() {
        assertTrue(gaussian.logOutputPowerDividedByInputPower.compareTo(new BigDecimal("0.178")) == 0);
    }

    @Test
    public void shouldReturnOutputPowerMinusInputPowerAsBigDecimal() {
        assertTrue(gaussian.outputPowerMinusInputPower.compareTo(new BigDecimal("29.139")) == 0);
    }
}
