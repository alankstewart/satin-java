package alankstewart.satin;

import java.math.BigDecimal;

import static java.lang.Math.log;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.BigDecimal.valueOf;

final class Gaussian {

    private final int inputPower;
    private final double outputPower;
    private final int saturationIntensity;
    private final BigDecimal logOutputPowerDividedByInputPower;
    private final BigDecimal outputPowerMinusInputPower;

    public Gaussian(final int inputPower, final double outputPower, final int saturationIntensity) {
        this.inputPower = inputPower;
        this.outputPower = outputPower;
        this.saturationIntensity = saturationIntensity;
        logOutputPowerDividedByInputPower = valueOf(log(this.outputPower / this.inputPower)).setScale(3, ROUND_HALF_UP);
        outputPowerMinusInputPower = valueOf(this.outputPower).subtract(valueOf(this.inputPower)).setScale(3, ROUND_HALF_UP);
    }

    public int getInputPower() {
        return inputPower;
    }

    public BigDecimal getOutputPower() {
        return valueOf(outputPower).setScale(3, ROUND_HALF_UP);
    }

    public int getSaturationIntensity() {
        return saturationIntensity;
    }

    public BigDecimal getLogOutputPowerDividedByInputPower() {
        return logOutputPowerDividedByInputPower;
    }

    public BigDecimal getOutputPowerMinusInputPower() {
        return outputPowerMinusInputPower;
    }
}
