package alankstewart.satin;

import java.math.BigDecimal;

import static java.lang.Math.log;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;

final class Gaussian {

    private final int inputPower;
    private final double outputPower;
    private final int saturationIntensity;

    public Gaussian(final int inputPower, final double outputPower, final int saturationIntensity) {
        this.inputPower = inputPower;
        this.outputPower = outputPower;
        this.saturationIntensity = saturationIntensity;
    }

    public int getInputPower() {
        return inputPower;
    }

    public BigDecimal getOutputPower() {
        return valueOf(outputPower).setScale(3, HALF_UP);
    }

    public int getSaturationIntensity() {
        return saturationIntensity;
    }

    public BigDecimal getLogOutputPowerDividedByInputPower() {
        return valueOf(log(outputPower / inputPower)).setScale(3, HALF_UP);
    }

    public BigDecimal getOutputPowerMinusInputPower() {
        return valueOf(outputPower).subtract(valueOf(inputPower)).setScale(3, HALF_UP);
    }
}
