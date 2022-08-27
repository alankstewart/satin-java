package alankstewart.satin;

import java.math.BigDecimal;
import java.util.Comparator;

import static java.lang.Math.log;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;

record Gaussian(int inputPower, double outputPower, int saturationIntensity) implements Comparable<Gaussian> {

    public BigDecimal logOutputPowerDividedByInputPower() {
        return valueOf(log(outputPower / inputPower)).setScale(3, HALF_UP);
    }

    public BigDecimal outputPowerMinusInputPower() {
        return valueOf(outputPower).subtract(valueOf(inputPower)).setScale(3, HALF_UP);
    }

    @Override
    public int compareTo(Gaussian o) {
        return Comparator.comparing(Gaussian::inputPower)
                .thenComparing(Gaussian::saturationIntensity)
                .compare(this, o);
    }
}
