package pl.jlabs.example.ignite.data.standardization;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DoubleStandardizer {

    private final double avg;
    private final double stdDeviation;

    public double standardize(final double value) {
        return (value - avg) / stdDeviation;
    }

    public static DoubleStandardizer fromValues(List<Double> values) {
        final double avg = values.stream().mapToDouble((d) -> d).average().orElse(0.0);
        double variance = 0.0;
        for (double value : values) {
            final double squareDist = Math.pow(value - avg, 2);
            variance += squareDist / values.size();
        }
        return new DoubleStandardizer(avg, Math.sqrt(variance));
    }
}
