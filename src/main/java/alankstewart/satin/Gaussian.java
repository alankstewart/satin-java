package alankstewart.satin;

import java.math.BigDecimal;
import java.math.BigInteger;

import static java.lang.Math.log;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.BigDecimal.valueOf;

public final class Gaussian {

    private final int inputPower;
    private final double outputPower;
    private final int saturationIntensity;

    public Gaussian(final int inputPower, final double outputPower, final int saturationIntensity) {
        this.inputPower = inputPower;
        this.outputPower = outputPower;
        this.saturationIntensity = saturationIntensity;
    }

    public BigInteger getInputPower() {
        return BigInteger.valueOf(inputPower);
    }

    public BigDecimal getOutputPower() {
        return valueOf(outputPower).setScale(3, ROUND_HALF_UP);
    }

    public BigInteger getSaturationIntensity() {
        return BigInteger.valueOf(saturationIntensity);
    }

    public BigDecimal getLogOutputPowerDividedByInputPower() {
        return valueOf(log(outputPower / inputPower)).setScale(3, ROUND_HALF_UP);
    }

    public BigDecimal getOutputPowerMinusInputPower() {
        return valueOf(outputPower).subtract(valueOf(inputPower)).setScale(3, ROUND_HALF_UP);
    }
}
