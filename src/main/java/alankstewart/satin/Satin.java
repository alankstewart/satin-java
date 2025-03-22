/*
 * User: Alan K Stewart Date: 19/03/2014
 */

package alankstewart.satin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.PI;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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

    public record Gaussian(int inputPower, double outputPower, int saturationIntensity) {
    }

    private record Laser(String outputFile, double smallSignalGain, int dischargePressure, String carbonDioxide) {
    }

    public static void main(final String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");
        var satin = new Satin();
        satin.calculate();
    }

    private void calculate() {
        final var start = Instant.now();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var is = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(LASER_FILE), "Laser data is null");
             var sc = new Scanner(is)) {
            final var inputPowers = getInputPowers();
            sc.findAll("((?:md|pi)[a-z]{2}\\.out)\\s+(\\d{2}\\.\\d)\\s+(\\d+)\\s+(MD|PI)")
                    .map(mr -> new Laser(mr.group(1), parseDouble(mr.group(2)), parseInt(mr.group(3)), mr.group(4)))
                    .map(laser -> CompletableFuture.runAsync(() -> process(inputPowers, laser), executor))
                    .forEach(CompletableFuture::join);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        } finally {
            LOGGER.log(Level.INFO, "The time was {0} seconds", Duration.between(start, Instant.now()).toNanos() / 1E9);
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

    private void process(final int[] inputPowers, final Laser laser) {
        try {
            var path = Paths.get(System.getProperty("user.dir")).resolve(laser.outputFile());
            Files.writeString(path, String.format("""
                            Start date: %s
                            
                            Gaussian Beam
                            
                            Pressure in Main Discharge = %skPa
                            Small-signal Gain = %s
                            CO2 via %s
                            
                            Pin       Pout                 Sat. Int      ln(Pout/Pin)   Pout-Pin
                            (watts)   (watts)              (watts/cm2)                  (watts)
                            """, ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()), laser.dischargePressure(),
                    laser.smallSignalGain(), laser.carbonDioxide()), CREATE, TRUNCATE_EXISTING);

            var lines = Arrays.stream(inputPowers)
                    .mapToObj(inputPower -> gaussianCalculation(inputPower, laser.smallSignalGain()))
                    .flatMap(List::stream)
                    .map(gaussian -> "%-10s%-21.14f%-14s%5.3f%16.3f".formatted(
                            gaussian.inputPower,
                            gaussian.outputPower,
                            gaussian.saturationIntensity,
                            Math.log(gaussian.outputPower / gaussian.inputPower),
                            gaussian.outputPower - gaussian.inputPower))
                    .toList();

            Files.write(path, lines, APPEND);
            Files.writeString(path, String.format("%nEnd date: %s", ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())), APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    List<Gaussian> gaussianCalculation(final int inputPower, final double smallSignalGain) {
        return IntStream.iterate(10000, i -> i <= 25000, i -> i + 1000)
                .parallel()
                .mapToObj(saturationIntensity -> createGaussian(inputPower, smallSignalGain, saturationIntensity))
                .toList();
    }

    private Gaussian createGaussian(int inputPower, double smallSignalGain, int saturationIntensity) {
        var outputPower = calculateOutputPower(inputPower, smallSignalGain, saturationIntensity);
        return new Gaussian(inputPower, outputPower, saturationIntensity);
    }

    private double calculateOutputPower(int inputPower, double smallSignalGain, int saturationIntensity) {
        final var expr2 = saturationIntensity * smallSignalGain / 32000 * DZ;
        final var inputIntensity = 2 * inputPower / AREA;
        return DoubleStream.iterate(0, r -> r < 0.5, r -> r + DR)
                .map(r -> DoubleStream.iterate(0, j -> j < INCR, j -> j + 1)
                        .reduce(inputIntensity * exp(-2 * pow(r, 2) / RAD2), (outputIntensity, j) ->
                                outputIntensity * (1 + expr2 / (saturationIntensity + outputIntensity) - EXPR1[(int) j])) * EXPR * r)
                .sum();
    }
}
