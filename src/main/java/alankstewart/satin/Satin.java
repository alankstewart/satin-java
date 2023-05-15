/*
 * User: Alan K Stewart Date: 19/03/2014
 */

package alankstewart.satin;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.PI;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.System.nanoTime;
import static java.time.LocalDateTime.now;

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
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss.SSS");
    private static final String TABLE_HEADER = "%7s  %-19s  %-12s  %-13s  %8s%n";

    public static void main(final String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");
        new Satin().calculate();
    }

    private void calculate() {
        final var start = nanoTime();
        try (var sc = new Scanner(Objects.requireNonNull(getInputStream("laser.dat"), "Laser data is null"));
             var executorService = Executors.newFixedThreadPool(8)) {
            final var inputPowers = getInputPowers();
            var tasks = sc.findAll(Pattern.compile("((md|pi)[a-z]{2}\\.out)\\s+(\\d{2}\\.\\d)\\s+(\\d+)\\s+(?i:\\2)?"))
                    .parallel()
                    .map(mr -> new Laser(mr.group(1), parseDouble(mr.group(3)), parseInt(mr.group(4)), mr.group(2)))
                    .map(laser -> (Callable<String>) () -> process(inputPowers, laser))
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

    private int[] getInputPowers() {
        try (var sc = new Scanner(Objects.requireNonNull(getInputStream("pin.dat"), "Input power data is null"))) {
            return sc.findAll(Pattern.compile("\\d+"))
                    .parallel()
                    .map(MatchResult::group)
                    .mapToInt(Integer::parseInt)
                    .toArray();
        }
    }

    private InputStream getInputStream(String fileName) {
        return getClass().getClassLoader().getResourceAsStream(fileName);
    }

    private String process(final int[] inputPowers, final Laser laser) throws FileNotFoundException {
        final var file = Paths.get(System.getProperty("user.dir")).resolve(laser.outputFile()).toFile();
        try (final var formatter = new Formatter(file)) {
            formatter.format("Start date: %s%n%nGaussian Beam%n%nPressure in Main Discharge = %skPa%nSmall-signal Gain = %s%nCO2 via %s%n%n",
                            now().format(DATE_TIME_FORMATTER),
                            laser.dischargePressure(),
                            laser.smallSignalGain(),
                            laser.carbonDioxide())
                    .format(TABLE_HEADER, "Pin", "Pout", "Sat. Int", "ln(Pout/Pin)", "Pout-Pin")
                    .format(TABLE_HEADER, "(watts)", "(watts)", "(watts/cm2)", "", "(watts)");

            Arrays.stream(inputPowers)
                    .parallel()
                    .mapToObj(inputPower -> gaussianCalculation(inputPower, laser.smallSignalGain()))
                    .flatMap(List::stream)
                    .sorted()
                    .forEachOrdered(gaussian -> formatter.format("%7s  %-19s  %-12s  %12.3f  %9.3f%n",
                            gaussian.inputPower(),
                            gaussian.outputPower(),
                            gaussian.saturationIntensity(),
                            gaussian.logOutputPowerDividedByInputPower(),
                            gaussian.outputPowerMinusInputPower()));

            formatter.format("%nEnd date: %s%n", now().format(DATE_TIME_FORMATTER)).flush();
            return file.getAbsolutePath();
        }
    }

    List<Gaussian> gaussianCalculation(final int inputPower, final double smallSignalGain) {
        return IntStream.iterate(10000, i -> i <= 25000, i -> i + 1000)
                .parallel()
                .mapToObj(saturationIntensity -> new Gaussian(inputPower,
                        calculateOutputPower(inputPower, smallSignalGain, saturationIntensity),
                        saturationIntensity))
                .sorted()
                .toList();
    }

    private double calculateOutputPower(int inputPower, double smallSignalGain, int saturationIntensity) {
        final var inputIntensity = 2 * inputPower / AREA;
        return DoubleStream.iterate(0, r -> r < 0.5, r -> r + DR)
                .map(r -> DoubleStream.iterate(0, j -> j < INCR, j -> j + 1)
                        .reduce(inputIntensity * exp(-2 * pow(r, 2) / RAD2), (outputIntensity, j) ->
                                outputIntensity * (1 + (saturationIntensity * smallSignalGain / 32000 * DZ) / (saturationIntensity + outputIntensity) - EXPR1[(int) j])) * EXPR * r)
                .sum();
    }
}
