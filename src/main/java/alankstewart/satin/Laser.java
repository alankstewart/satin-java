package alankstewart.satin;

final class Laser {

    private final String outputFile;
    private final double smallSignalGain;
    private final int dischargePressure;
    private final String carbonDioxide;

    public Laser(final String outputFile, final double smallSignalGain, final int dischargePressure, final String carbonDioxide) {
        this.outputFile = outputFile;
        this.smallSignalGain = smallSignalGain;
        this.dischargePressure = dischargePressure;
        this.carbonDioxide = carbonDioxide;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public double getSmallSignalGain() {
        return smallSignalGain;
    }

    public int getDischargePressure() {
        return dischargePressure;
    }

    public String getCarbonDioxide() {
        return carbonDioxide;
    }

    @Override
    public String toString() {
        return String.format("%s  %s  %s  %s", outputFile, smallSignalGain, dischargePressure, carbonDioxide);
    }
}
