package alankstewart.satin;

final class Laser {

    public final String outputFile;
    public final double smallSignalGain;
    public final int dischargePressure;
    public final String carbonDioxide;

    public Laser(final String outputFile, final double smallSignalGain, final int dischargePressure, final String carbonDioxide) {
        this.outputFile = outputFile;
        this.smallSignalGain = smallSignalGain;
        this.dischargePressure = dischargePressure;
        this.carbonDioxide = carbonDioxide;
    }

    @Override
    public String toString() {
        return String.format("%s  %s  %s  %s", outputFile, smallSignalGain, dischargePressure, carbonDioxide);
    }
}
