package pl.jlabs.example.ignite.data.distance;

import lombok.RequiredArgsConstructor;
import pl.jlabs.example.ignite.data.Iris;

import java.util.function.BiFunction;

@RequiredArgsConstructor
public enum IrisDistanceStrategy {
    MANHATTAN(IrisDistanceStrategy::manhattanDistance), EUCLID(IrisDistanceStrategy::euclidDistance);

    private final BiFunction<Iris, Iris, Double> distanceCalculation;

    private static Double manhattanDistance(final Iris iris, final Iris other) {
        return Math.abs(iris.getSepalLength() - other.getSepalLength())
                + Math.abs(iris.getSepalWidth() - other.getSepalWidth())
                + Math.abs(iris.getPetalLength() - other.getPetalLength())
                + Math.abs(iris.getPetalWidth() - other.getPetalWidth());
    }

    private static Double euclidDistance(final Iris iris, final Iris other) {
        return Math.sqrt(
                Math.pow(iris.getSepalLength() - other.getSepalLength(), 2)
                + Math.pow(iris.getSepalWidth() - other.getSepalWidth(), 2)
                + Math.pow(iris.getPetalLength() - other.getPetalLength(), 2)
                + Math.pow(iris.getPetalWidth() - other.getPetalWidth(), 2)
        );
    }

    public double calculateDistance(final Iris iris, final Iris other) {
        return this.distanceCalculation.apply(iris, other);
    }
}
