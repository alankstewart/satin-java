package alankstewart.satin;

import java.util.Comparator;

record Gaussian(int inputPower, double outputPower, int saturationIntensity) implements Comparable<Gaussian> {

    public double logOutputPowerDividedByInputPower() {
        return roundUp(Math.log(outputPower / inputPower));
    }

    public double outputPowerMinusInputPower() {
        return roundUp(outputPower - inputPower);
    }

    private double roundUp(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    @Override
    public int compareTo(Gaussian o) {
        return Comparator.comparing(Gaussian::inputPower)
                .thenComparing(Gaussian::saturationIntensity)
                .compare(this, o);
    }
}
