package alankstewart.satin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

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

        satin.gaussianCalculation(inputPower, smallSignalGain).stream()
                .filter(gaussian -> gaussian.saturationIntensity() == saturationIntensity)
                .findAny()
                .ifPresentOrElse(
                        gaussian -> assertAll(
                                () -> assertEquals(outputPower, gaussian.outputPower(), 1e-3),
                                () -> assertEquals(logOutputPowerDividedByInputPower, gaussian.logOutputPowerDividedByInputPower(), 1e-3),
                                () -> assertEquals(outputPowerMinusInputPower, gaussian.outputPowerMinusInputPower(), 1e-3)
                        ), Assertions::fail);
    }
}
