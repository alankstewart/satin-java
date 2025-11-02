package alankstewart.satin;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by alanstewart on 26/03/15.
 */
class SatinTest {

    private static Satin satin;
    private static final Map<String, Map<Integer, Satin.Gaussian>> cache = new ConcurrentHashMap<>();

    @BeforeAll
    static void setUp() {
        satin = new Satin();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/satin.csv")
    void shouldCalculateGaussians(int inputPower,
                                  double smallSignalGain,
                                  int saturationIntensity,
                                  double outputPower,
                                  double logOutputPowerDividedByInputPower,
                                  double outputPowerMinusInputPower) {
        var gaussian = cache.computeIfAbsent(inputPower + ":" + smallSignalGain,
                        k -> satin.gaussianCalculation(inputPower, smallSignalGain).stream()
                                .collect(toMap(Satin.Gaussian::saturationIntensity, Function.identity())))
                .get(saturationIntensity);
        assertNotNull(gaussian);

        assertEquals(outputPower, gaussian.outputPower(), 1e-3);
        assertEquals(logOutputPowerDividedByInputPower, gaussian.logOutputPowerDividedByInputPower(), 1e-3);
        assertEquals(outputPowerMinusInputPower, gaussian.outputPowerMinusInputPower(), 1e-3);
    }
}
