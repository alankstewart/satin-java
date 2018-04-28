package alankstewart.satin;

import java.math.BigDecimal;

import static java.lang.Math.log;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;

final class Gaussian {

    public final int inputPower;
    public final int saturationIntensity;
    public final BigDecimal outputPower;
    public final BigDecimal logOutputPowerDividedByInputPower;
    public final BigDecimal outputPowerMinusInputPower;

    public Gaussian(final int inputPower, final double outputPower, final int saturationIntensity) {
        this.inputPower = inputPower;
        this.outputPower = valueOf(outputPower).setScale(3, HALF_UP);
        this.saturationIntensity = saturationIntensity;
        logOutputPowerDividedByInputPower = valueOf(log(outputPower / inputPower)).setScale(3, HALF_UP);
        outputPowerMinusInputPower = valueOf(outputPower).subtract(valueOf(inputPower)).setScale(3, HALF_UP);

    }
}
