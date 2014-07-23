package alankstewart.satin;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GaussianTest {

    private Gaussian gaussian;

    @Before
    public void setUp() {
        gaussian = new Gaussian(150, 179.139, 25000);
    }

    @Test
    public void shoulReturnInputPowerAsBigInteger() {
        assertThat(gaussian.getInputPower(), is(new BigInteger("150")));
    }

    @Test
    public void shouldReturnOutputPowerAsBigDecimal() {
        assertThat(gaussian.getOutputPower(), is(new BigDecimal("179.139")));
    }

    @Test
    public void shouldReturnSaturationIntensityAsBigInteger() {
        assertThat(gaussian.getSaturationIntensity(), is(new BigInteger("25000")));
    }

    @Test
    public void shouldReturnLogOutputPowerDividedByInputPowerAsBigDecimal() {
        assertThat(gaussian.getLogOutputPowerDividedByInputPower(), is(new BigDecimal("0.178")));
    }

    @Test
    public void shouldReturnOutputPowerMinusInputPowerAsBigDecimal() {
        assertThat(gaussian.getOutputPowerMinusInputPower(), is(new BigDecimal("29.139")));
    }
}
