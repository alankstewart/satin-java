/*
 * User: Alan K Stewart Date: 19/03/2014
 */

package alankstewart.satin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.stream.IntStream;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.PI;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.System.nanoTime;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public final class Satin {

    private static final Logger LOGGER = Logger.getLogger(Satin.class.getName());
    private static final double RAD = 0.18;
    private static final double RAD2 = pow(RAD, 2);
    private static final double W1 = 0.3;
    private static final double DR = 0.002;
    private static final double DZ = 0.04;
    private static final double LAMBDA = 0.0106;
    private static final double AREA = PI * RAD2;
    private static final double Z1 = PI * pow(W1, 2) / LAMBDA;
    private static final double Z12 = pow(Z1, 2);
    private static final double EXPR = 2 * PI * DR;
    private static final int INCR = 8001;
    private static final double[] EXPR1 = IntStream.range(0, INCR)
            .mapToDouble(i -> ((double) i - (INCR >> 1)) / 25)
            .map(zInc -> 2 * zInc * DZ / (Z12 + pow(zInc, 2)))
            .toArray();
    public static final String LASER_FILE = "laser.dat";
    public static final String PIN_FILE = "pin.dat";

    public static void main(final String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");
        var satin = new Satin();
        final var start = nanoTime();
        try (var is = Objects.requireNonNull(satin.getClass().getClassLoader().getResourceAsStream(LASER_FILE), "Laser data is null");
             var sc = new Scanner(is);
             var executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            final var inputPowers = satin.getInputPowers();
            var tasks = sc.findAll("((?:md|pi)[a-z]{2}\\.out)\\s+(\\d{2}\\.\\d)\\s+(\\d+)\\s+(MD|PI)")
                    .map(mr -> new Laser(mr.group(1), parseDouble(mr.group(2)), parseInt(mr.group(3)), mr.group(4)))
                    .map(laser -> (Callable<String>) () -> satin.process(inputPowers, laser))
                    .toList();
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.severe(e.getMessage());
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        } finally {
            LOGGER.log(Level.INFO, "The time was {0} seconds", (nanoTime() - start) / 1E9);
        }
    }

    private int[] getInputPowers() throws IOException {
        try (var is = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(PIN_FILE), "Input power data is null");
             var sc = new Scanner(is)) {
            return sc.findAll("\\d+")
                    .map(MatchResult::group)
                    .mapToInt(Integer::parseInt)
                    .toArray();
        }
    }

    private String process(final int[] inputPowers, final Laser laser) throws IOException {
        var path = Paths.get(System.getProperty("user.dir")).resolve(laser.outputFile());
        Files.writeString(path, STR."""
                Start date: \{ISO_DATE_TIME.format(now())}

                Gaussian Beam

                Pressure in Main Discharge = \{laser.dischargePressure()}kPa
                Small-signal Gain = \{laser.smallSignalGain()}
                CO2 via \{laser.carbonDioxide()}

                Pin      Pout                 Sat. Int      ln(Pout/Pin)   Pout-Pin
                (watts)  (watts)              (watts/cm2)                   (watts)
                """, CREATE, TRUNCATE_EXISTING);

        var lines = Arrays.stream(inputPowers)
                .mapToObj(inputPower -> gaussianCalculation(inputPower, laser.smallSignalGain()))
                .flatMap(List::stream)
                .map(gaussian -> "%7s  %-19s  %-12s  %12.3f  %9.3f".formatted(
                        gaussian.inputPower(),
                        gaussian.outputPower(),
                        gaussian.saturationIntensity(),
                        gaussian.logOutputPowerDividedByInputPower(),
                        gaussian.outputPowerMinusInputPower()))
                .toList();
        Files.write(path, lines, APPEND);

        Files.writeString(path, STR."\nEnd date: \{ISO_DATE_TIME.format(now())}", APPEND);

        return path.getFileName().toString();
    }

    List<Gaussian> gaussianCalculation(final int inputPower, final double smallSignalGain) {
        return IntStream.iterate(10000, i -> i <= 25000, i -> i + 1000)
                .parallel()
                .mapToObj(saturationIntensity -> calculateOutputPower(inputPower, smallSignalGain, saturationIntensity))
                .toList();
    }

//    private Gaussian calculateOutputPower(int inputPower, double smallSignalGain, int saturationIntensity) {
//        final var expr2 = saturationIntensity * smallSignalGain / 32000 * DZ;
//        final var inputIntensity = 2 * inputPower / AREA;
//        var outputPower = DoubleStream.iterate(0, r -> r < 0.5, r -> r + DR)
//                .map(r -> DoubleStream.iterate(0, j -> j < INCR, j -> j + 1)
//                        .reduce(inputIntensity * exp(-2 * pow(r, 2) / RAD2), (outputIntensity, j) ->
//                                outputIntensity * (1 + expr2 / (saturationIntensity + outputIntensity) - EXPR1[(int) j])) * EXPR * r)
//                .sum();
//        return new Gaussian(inputPower, outputPower, saturationIntensity);
//    }

    private Gaussian calculateOutputPower(int inputPower, double smallSignalGain, int saturationIntensity) {
        final var expr2 = saturationIntensity * smallSignalGain / 32000 * DZ;
        final var inputIntensity = 2 * inputPower / AREA;

        var outputPower = 0.0;
        for (double r = 0; r < 0.5; r += DR) {
            var outputIntensity = inputIntensity * exp(-2 * pow(r, 2) / RAD2);
            for (int j = 0; j < INCR; j++) {
                outputIntensity *= (1 + expr2 / (saturationIntensity + outputIntensity) - EXPR1[j]);
            }
            outputPower += outputIntensity * EXPR * r;
        }

        return new Gaussian(inputPower, outputPower, saturationIntensity);
    }

    private record Laser(String outputFile, double smallSignalGain, int dischargePressure, String carbonDioxide) {
    }

    public record Gaussian(int inputPower, double outputPower, int saturationIntensity) {

        public double logOutputPowerDividedByInputPower() {
            return Math.log(outputPower / inputPower);
        }

        public double outputPowerMinusInputPower() {
            return outputPower - inputPower;
        }
    }
}
