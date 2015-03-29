package alankstewart.satin;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by alanstewart on 26/03/15.
 */
public class SatinTest {

    private final Satin satin = new Satin();

    @Test
    public void shoulReturnCorrectResultsFor1WattInputPower() {
        // mdtw.out
        List<Gaussian> gaussians = satin.gaussianCalculation(1, 24.2);

        Gaussian gaussian = getGaussian(gaussians, 10000);
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("1.266")) == 0);
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("0.266")) == 0);

        gaussian = getGaussian(gaussians, 25000);
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("1.268")) == 0);
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("0.268")) == 0);

        // pisi.out
        gaussians = satin.gaussianCalculation(1, 17.6);

        gaussian = getGaussian(gaussians, 10000);
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("1.186")) == 0);
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("0.186")) == 0);

        gaussian = getGaussian(gaussians, 25000);
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("1.187")) == 0);
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("0.187")) == 0);
    }

    @Test
    public void shoulReturnCorrectResultsFor10WattsInputPower() {
        // mdtw.out
        List<Gaussian> gaussians = satin.gaussianCalculation(10, 24.2);

        Gaussian gaussian = getGaussian(gaussians, 10000);
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("12.463")) == 0);
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("2.463")) == 0);

        gaussian = getGaussian(gaussians, 25000);
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("12.586")) == 0);
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("2.586")) == 0);

        // mdfo.out
        gaussians = satin.gaussianCalculation(10, 21.2);

        gaussian = getGaussian(gaussians, 10000);
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("12.124")) == 0);
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("2.124")) == 0);

        gaussian = getGaussian(gaussians, 25000);
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("12.227")) == 0);
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("2.227")) == 0);
    }

    @Test
    public void shoulReturnCorrectResultsFor150WattsInputPower() {
        // mdtw.out
        List<Gaussian> gaussians = satin.gaussianCalculation(150, 24.2);

        Gaussian gaussian = getGaussian(gaussians, 10000);
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("173.134")) == 0);
        assertTrue(gaussian.getLogOutputPowerDividedByInputPower().compareTo(new BigDecimal("0.143")) == 0);
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("23.134")) == 0);

        gaussian = getGaussian(gaussians, 14000);
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("175.448")) == 0);
        assertTrue(gaussian.getLogOutputPowerDividedByInputPower().compareTo(new BigDecimal("0.157")) == 0);
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("25.448")) == 0);

        gaussian = getGaussian(gaussians, 25000);
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("179.139")) == 0);
        assertTrue(gaussian.getLogOutputPowerDividedByInputPower().compareTo(new BigDecimal("0.178")) == 0);
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("29.139")) == 0);

        // pitw.out
        gaussians = satin.gaussianCalculation(150, 22.7);

        gaussian = getGaussian(gaussians, 10000);
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("171.584")) == 0);
        assertTrue(gaussian.getLogOutputPowerDividedByInputPower().compareTo(new BigDecimal("0.134")) == 0);
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("21.584")) == 0);

        gaussian = getGaussian(gaussians, 18000);
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("175.274")) == 0);
        assertTrue(gaussian.getLogOutputPowerDividedByInputPower().compareTo(new BigDecimal("0.156")) == 0);
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("25.274")) == 0);

        gaussian = getGaussian(gaussians, 25000);
        assertTrue(gaussian.getOutputPower().compareTo(new BigDecimal("177.165")) == 0);
        assertTrue(gaussian.getLogOutputPowerDividedByInputPower().compareTo(new BigDecimal("0.166")) == 0);
        assertTrue(gaussian.getOutputPowerMinusInputPower().compareTo(new BigDecimal("27.165")) == 0);
    }

    private Gaussian getGaussian(List<Gaussian> gaussians, int saturationIntensity) {
        return gaussians.stream()
                .filter(g -> g.getSaturationIntensity() == saturationIntensity)
                .findAny()
                .get();
    }
}
