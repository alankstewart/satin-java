package alankstewart.satin;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Created by alanstewart on 26/03/15.
 */
public class SatinTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/satin.csv")
    public void shouldCalculateGaussians(int inputPower,
                                         double smallSignalGain,
                                         int saturationIntensity,
                                         double outputPower,
                                         double logOutputPowerDividedByInputPower,
                                         double outputPowerMinusInputPower) {
        var satin = new Satin();
        var gaussians = satin.gaussianCalculation(inputPower, smallSignalGain);
        gaussians.parallelStream()
                .filter(g -> g.saturationIntensity == saturationIntensity)
                .findFirst()
                .ifPresentOrElse(g ->
                        assertAll(
                                () -> assertEquals(0, g.outputPower.compareTo(BigDecimal.valueOf(outputPower))),
                                () -> assertEquals(0, g.logOutputPowerDividedByInputPower.compareTo(BigDecimal.valueOf(logOutputPowerDividedByInputPower))),
                                () -> assertEquals(0, g.outputPowerMinusInputPower.compareTo(BigDecimal.valueOf(outputPowerMinusInputPower)))
                        ), Assertions::fail);
    }
}
