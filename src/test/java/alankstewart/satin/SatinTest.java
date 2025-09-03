package alankstewart.satin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
                                  double outputPowerMinusInputPower) throws Exception {

        var satin = new Satin();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            satin.gaussianCalculation(inputPower, smallSignalGain).stream()
                    .map(gaussian -> executor.submit(() -> gaussian))
                    .map(this::getGaussian)
                    .filter(gaussian -> gaussian.saturationIntensity() == saturationIntensity)
                    .findAny()
                    .ifPresentOrElse(
                            gaussian -> assertAll(
                                    () -> assertEquals(outputPower, roundUp(gaussian.outputPower())),
                                    () -> assertEquals(logOutputPowerDividedByInputPower, roundUp(gaussian.logOutputPowerDividedByInputPower())),
                                    () -> assertEquals(outputPowerMinusInputPower, roundUp(gaussian.outputPowerMinusInputPower()))
                            ), Assertions::fail);
        }
    }

    private Satin.Gaussian getGaussian(Future<Satin.Gaussian> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private double roundUp(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
