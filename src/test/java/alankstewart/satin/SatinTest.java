package alankstewart.satin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by alanstewart on 26/03/15.
 */
class SatinTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/satin.csv")
    void shouldCalculateGaussians(int inputPower,
                                  double smallSignalGain,
                                  int saturationIntensity,
                                  double outputPower,
                                  double logOutputPowerDividedByInputPower,
                                  double outputPowerMinusInputPower) {
        var satin = new Satin();
        satin.gaussianCalculation(inputPower, smallSignalGain).parallelStream()
                .filter(gaussian -> gaussian.saturationIntensity() == saturationIntensity)
                .findFirst()
                .ifPresentOrElse(gaussian ->
                        assertAll(
                                () -> assertEquals(outputPower, roundUp(gaussian.outputPower())),
                                () -> assertEquals(logOutputPowerDividedByInputPower, gaussian.logOutputPowerDividedByInputPower()),
                                () -> assertEquals(outputPowerMinusInputPower, gaussian.outputPowerMinusInputPower())
                        ), Assertions::fail);
    }

    private double roundUp(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

//    @Disabled
//    @Test
//    public void decodeBase64() throws IOException {
//        var outputPath = Paths.get("D:\\temp", "mdcf-parent.zip");
//        var path = Paths.get("D:\\temp", "mdcf-parent.txt");
//        Files.write(outputPath, Base64.getDecoder().decode(Files.readAllBytes(path)));
//    }
}
