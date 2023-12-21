package alankstewart.satin;

import java.util.Comparator;

record Gaussian(int inputPower, double outputPower, int saturationIntensity) implements Comparable<Gaussian> {

    public double logOutputPowerDividedByInputPower() {
        return Math.log(outputPower / inputPower);
    }

    public double outputPowerMinusInputPower() {
        return outputPower - inputPower;
    }

    @Override
    public int compareTo(Gaussian o) {
        return Comparator.comparing(Gaussian::inputPower)
                .thenComparing(Gaussian::saturationIntensity)
                .compare(this, o);
    }
}
