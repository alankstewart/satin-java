package alankstewart.satin;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class GaussianTest {

    private Gaussian gaussian;

    @Before
    public void setUp() {
        gaussian = new Gaussian(150, 179.139, 25000);
    }

    @Test
    public void shoulReturnInputPowerAsBigInteger() {
        assertTrue(gaussian.getInputPower().compareTo(new BigInteger("150")) == 0);
    }

    @Test
    public void shouldReturnOutputPowerAsBigDecimal() {
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("179.139")) == 0);
    }

    @Test
    public void shouldReturnSaturationIntensityAsBigInteger() {
        assertTrue(gaussian.getSaturationIntensity().compareTo(new BigInteger("25000")) == 0);
    }

    @Test
    public void shouldReturnLogOutputPowerDividedByInputPowerAsBigDecimal() {
        assertTrue(gaussian.getLogOutputPowerDividedByInputPower().compareTo(new BigDecimal("0.178")) == 0);
    }

    @Test
    public void shouldReturnOutputPowerMinusInputPowerAsBigDecimal() {
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("29.139")) == 0);
    }
}
