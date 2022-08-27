package alankstewart.satin;

import java.math.BigDecimal;

import static java.lang.Math.log;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;

record Gaussian(int inputPower, double outputPower, int saturationIntensity) {

    public BigDecimal logOutputPowerDividedByInputPower() {
        return valueOf(log(outputPower / inputPower)).setScale(3, HALF_UP);
    }

    public BigDecimal outputPowerMinusInputPower() {
        return valueOf(outputPower).subtract(valueOf(inputPower)).setScale(3, HALF_UP);
    }
}
