package alankstewart.satin;

record Gaussian(int inputPower, double outputPower, int saturationIntensity) {

    public double logOutputPowerDividedByInputPower() {
        return Math.log(outputPower / inputPower);
    }

    public double outputPowerMinusInputPower() {
        return outputPower - inputPower;
    }
}
