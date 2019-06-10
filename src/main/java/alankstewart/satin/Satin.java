/*
 * User: Alan K Stewart Date: 19/03/2014
 */

package alankstewart.satin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.PI;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.System.nanoTime;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static java.nio.charset.Charset.defaultCharset;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toUnmodifiableList;

public final class Satin {

    private static final Path PATH = Paths.get(System.getProperty("user.dir"));
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss.SSS");
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

    public static void main(final String[] args) {
        final var start = nanoTime();
        final var satin = new Satin();
        try {
            satin.calculate();
        } catch (final Exception e) {
            System.err.println(e.getMessage());
        } finally {
            System.out.printf("The time was %.3f seconds\n", valueOf(nanoTime() - start).divide(valueOf(1E9), 3, HALF_UP));
        }
    }

    private void calculate() throws IOException, URISyntaxException, InterruptedException, ExecutionException {
        final var inputPowers = getInputPowers();
        final var tasks = getLaserData()
                .parallelStream()
                .map(laser -> (Callable<File>) () -> process(inputPowers, laser))
                .collect(toUnmodifiableList());

        final var executorService = Executors.newCachedThreadPool();
        try {
            for (final var future : executorService.invokeAll(tasks)) {
                future.get();
            }
        } finally {
            executorService.shutdown();
        }
    }

    private List<Integer> getInputPowers() throws IOException, URISyntaxException {
        final var url = getClass().getClassLoader().getResource("pin.dat");
        Objects.requireNonNull(url, "Failed to find pin.dat");
        return Files.lines(Paths.get(url.toURI())).mapToInt(Integer::parseInt).boxed().collect(toUnmodifiableList());
    }

    private List<Laser> getLaserData() throws IOException, URISyntaxException {
        final var url = getClass().getClassLoader().getResource("laser.dat");
        Objects.requireNonNull(url, "Failed to find laser.dat");
        final var p = Pattern.compile("((md|pi)[a-z]{2}\\.out)\\s+([0-9]{2}\\.[0-9])\\s+([0-9]+)\\s+(?i:\\2)");
        return Files.lines(Paths.get(url.toURI()))
                .map(p::matcher)
                .filter(Matcher::matches)
                .map(this::getLaser)
                .collect(toUnmodifiableList());
    }

    private Laser getLaser(Matcher m) {
        return new Laser(m.group(1), parseDouble(m.group(3)), parseInt(m.group(4)), m.group(2));
    }

    private File process(final List<Integer> inputPowers, final Laser laser) {
        final var path = PATH.resolve(laser.outputFile);
        final var header = "Start date: %s\n\nGaussian Beam\n\nPressure in Main Discharge = %skPa\nSmall-signal Gain = %s\nCO2 via %s\n\nPin\t\tPout\t\tSat. Int\tln(Pout/Pin\tPout-Pin\n(watts)\t\t(watts)\t\t(watts/cm2)\t\t\t(watts)\n";
        try (final var writer = Files.newBufferedWriter(path, defaultCharset(), CREATE, WRITE, TRUNCATE_EXISTING);
             final var formatter = new Formatter(writer)) {
            formatter.format(header,
                    now().format(DATE_TIME_FORMATTER),
                    laser.dischargePressure,
                    laser.smallSignalGain,
                    laser.carbonDioxide);

            inputPowers.forEach(inputPower -> gaussianCalculation(inputPower, laser.smallSignalGain)
                    .forEach(gaussian -> formatter.format("%d\t\t%s\t\t%d\t\t%s\t\t%s\n",
                            gaussian.inputPower,
                            gaussian.outputPower,
                            gaussian.saturationIntensity,
                            gaussian.logOutputPowerDividedByInputPower,
                            gaussian.outputPowerMinusInputPower)));

            formatter.format("\nEnd date: %s\n", now().format(DATE_TIME_FORMATTER));
            formatter.flush();
            return path.toFile();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    List<Gaussian> gaussianCalculation(final int inputPower, final double smallSignalGain) {
        final var expr1 = IntStream.range(0, INCR)
                .mapToDouble(i -> ((double) i - INCR / 2) / 25)
                .map(zInc -> 2 * zInc * DZ / (Z12 + pow(zInc, 2)))
                .toArray();
        final var expr2 = smallSignalGain / 32000 * DZ;
        final var inputIntensity = 2 * inputPower / AREA;

        return IntStream.iterate(10000, i -> i <= 25000, i -> i + 1000)
                .mapToObj(saturationIntensity -> {
                    var expr3 = saturationIntensity * expr2;
                    var outputPower = DoubleStream.iterate(0, r -> r < 0.5, r -> r + DR)
                            .map(r -> {
                                var outputIntensity = inputIntensity * exp(-2 * pow(r, 2) / RAD2);
                                for (var j = 0; j < INCR; j++) {
                                    outputIntensity *= 1 + expr3 / (saturationIntensity + outputIntensity) - expr1[j];
                                }
                                return outputIntensity * EXPR * r;
                            })
                            .sum();
                    return new Gaussian(inputPower, outputPower, saturationIntensity);
                })
                .collect(toUnmodifiableList());
    }
}
