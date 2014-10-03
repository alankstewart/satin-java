/*
 * User: Alan K Stewart Date: 19/03/2014
 */

package alankstewart.satin;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.PI;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.System.nanoTime;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.BigDecimal.valueOf;
import static java.nio.charset.Charset.defaultCharset;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.time.LocalDateTime.now;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public final class Satin {

    private static final Logger LOGGER = Logger.getLogger(Satin.class.getName());
    private static final Path PATH = Paths.get(System.getProperty("user.dir"));
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss.SSS");
    private static final double RAD = 0.18;
    private static final double RAD2 = pow(RAD, 2);
    private static final double W1 = 0.3;
    private static final double DR = 0.002;
    private static final double DZ = 0.04;
    private static final double LAMDA = 0.0106;
    private static final double AREA = PI * RAD2;
    private static final double Z1 = PI * pow(W1, 2) / LAMDA;
    private static final double Z12 = pow(Z1, 2);
    private static final double EXPR = 2 * PI * DR;
    private static final int INCR = 8001;

    public static void main(final String[] args) {
        final long start = nanoTime();
        final Satin satin = new Satin();
        try {
            if (args.length > 0 && args[0].equals("-concurrent")) {
                satin.calculateConcurrently();
            } else {
                satin.calculate();
            }
        } catch (final Exception e) {
            LOGGER.severe("Failed to complete: " + e.getMessage());
        } finally {
            LOGGER.info("The time was " + valueOf(nanoTime() - start).divide(valueOf(1E9), 3, ROUND_HALF_UP) + " seconds");
        }
    }

    private void calculateConcurrently() throws IOException, URISyntaxException, InterruptedException, ExecutionException {
        final List<Integer> inputPowers = getInputPowers();
        final List<Callable<Void>> tasks = getLaserData()
                .parallelStream()
                .map(laser -> (Callable<Void>) () -> {
                    process(inputPowers, laser);
                    return null;
                }).collect(toList());

        final ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            for (final Future<Void> future : executorService.invokeAll(tasks)) {
                future.get();
            }
        } finally {
            executorService.shutdown();
        }
    }

    private void calculate() throws IOException, URISyntaxException {
        final List<Integer> inputPowers = getInputPowers();
        getLaserData().forEach(laser -> process(inputPowers, laser));
    }

    private List<Integer> getInputPowers() throws IOException, URISyntaxException {
        return readDataFileC("pin.dat").map(Integer::parseInt).collect(toList());
    }

    private List<Laser> getLaserData() throws IOException, URISyntaxException {
        final Pattern p = Pattern.compile("((md|pi)[a-z]{2}\\.out)\\s+([0-9]{2}\\.[0-9])\\s+([0-9]+)\\s+(?i:\\2)");
        return readDataFileC("laser.dat")
                .map(p::matcher)
                .filter(Matcher::matches)
                .map(m -> new Laser(m.group(1), m.group(3), m.group(4), m.group(2)))
                .collect(toList());
    }

    private Stream<String> readDataFileC(String fileName) throws IOException, URISyntaxException {
        return Files.lines(getDataFilePath(fileName));
    }

    private Path getDataFilePath(String fileName) throws URISyntaxException {
        final URL resource = getClass().getClassLoader().getResource(fileName);
        requireNonNull(resource, "Failed to find resource " + fileName);
        return Paths.get(resource.toURI());
    }

    private void process(final List<Integer> inputPowers, final Laser laser) {
        final Path path = PATH.resolve(laser.getOutputFile());
        try (BufferedWriter writer = Files.newBufferedWriter(path, defaultCharset(), CREATE, WRITE, TRUNCATE_EXISTING);
             final Formatter formatter = new Formatter(writer)) {
            formatter.format("Start date: %s\n\nGaussian Beam\n\nPressure in Main Discharge = %skPa\nSmall-signal Gain = %s\nCO2 via %s\n\nPin\t\tPout\t\tSat. Int\tln(Pout/Pin\tPout-Pin\n(watts)\t\t(watts)\t\t(watts/cm2)\t\t\t(watts)\n",
                    now().format(DATE_TIME_FORMATTER),
                    laser.getDischargePressure(),
                    laser.getSmallSignalGain(),
                    laser.getCarbonDioxide().name());

            inputPowers.forEach(inputPower -> gaussianCalculation(inputPower, laser.getSmallSignalGain())
                    .forEach(gaussian -> formatter.format("%s\t\t%s\t\t%s\t\t%s\t\t%s\n",
                            gaussian.getInputPower(),
                            gaussian.getOutputPower(),
                            gaussian.getSaturationIntensity(),
                            gaussian.getLogOutputPowerDividedByInputPower(),
                            gaussian.getOutputPowerMinusInputPower())));

            formatter.format("\nEnd date: %s\n", now().format(DATE_TIME_FORMATTER));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<Gaussian> gaussianCalculation(final int inputPower, final double smallSignalGain) {
        final double[] expr1 = IntStream.range(0, INCR).mapToDouble(i -> {
            final double zInc = ((double) i - INCR / 2) / 25;
            return 2 * zInc * DZ / (Z12 + pow(zInc, 2));
        }).toArray();

        final double inputIntensity = 2 * inputPower / AREA;
        final double expr2 = (smallSignalGain / 32E3) * DZ;

        return IntStream.rangeClosed(10, 25).mapToObj(i -> {
            final int saturationIntensity = i * 1000;
            final double expr3 = saturationIntensity * expr2;
            double outputPower = 0.0;
            for (double r = 0; r <= 0.5; r += DR) {
                double outputIntensity = inputIntensity * exp(-2 * pow(r, 2) / RAD2);
                for (int j = 0; j < INCR; j++) {
                    outputIntensity *= (1 + expr3 / (saturationIntensity + outputIntensity) - expr1[j]);
                }
                outputPower += (outputIntensity * EXPR * r);
            }
            return new Gaussian(inputPower, outputPower, saturationIntensity);
        }).collect(toList());
    }
}