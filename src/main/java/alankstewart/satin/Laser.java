package alankstewart.satin;

public final class Laser {

    enum CO2 {MD, PI}

    private final String outputFile;
    private final double smallSignalGain;
    private final int dischargePressure;
    private final CO2 carbonDioxide;

    public Laser(final String outputFile, final double smallSignalGain, final int dischargePressure, final CO2 carbonDioxide) {
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

    public CO2 getCarbonDioxide() {
        return carbonDioxide;
    }

    @Override
    public String toString() {
        return String.format("%s  %s  %s  %s", outputFile, smallSignalGain, dischargePressure, carbonDioxide);
    }
}
