package alankstewart.satin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;
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
                                         BigDecimal logOutputPowerDividedByInputPower,
                                         BigDecimal outputPowerMinusInputPower) {
        var satin = new Satin();
        satin.gaussianCalculation(inputPower, smallSignalGain).parallelStream()
                .filter(g -> g.saturationIntensity() == saturationIntensity)
                .findFirst()
                .ifPresentOrElse(g ->
                        assertAll(
                                () -> assertEquals(0, BigDecimal.valueOf(g.outputPower()).setScale(3, HALF_UP).compareTo(BigDecimal.valueOf(outputPower).setScale(3, HALF_UP))),
                                () -> assertEquals(0, g.logOutputPowerDividedByInputPower().compareTo(logOutputPowerDividedByInputPower)),
                                () -> assertEquals(0, g.outputPowerMinusInputPower().compareTo(outputPowerMinusInputPower))
                        ), Assertions::fail);
    }
}
