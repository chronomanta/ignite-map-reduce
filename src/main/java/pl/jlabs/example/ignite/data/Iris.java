package pl.jlabs.example.ignite.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import pl.jlabs.example.ignite.data.distance.IrisDistanceStrategy;
import pl.jlabs.example.ignite.data.standardization.DoubleStandardizer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Getter
@RequiredArgsConstructor
@Builder
public class Iris {
    @Nullable
    private final Integer id;
    private final double sepalLength;
    private final double sepalWidth;
    private final double petalLength;
    private final double petalWidth;
    @Nullable
    private final String type;

    public double distanceTo(final Iris other, final IrisDistanceStrategy distanceStrategy) {
        return distanceStrategy.calculateDistance(this, other);
    }

    public Iris standardizedWith(final Map<String, DoubleStandardizer> valueStandardizers) {
        return Iris.builder()
                .id(id)
                .sepalLength(valueStandardizers.get("sepalLength").standardize(sepalLength))
                .sepalWidth(valueStandardizers.get("sepalWidth").standardize(sepalWidth))
                .petalLength(valueStandardizers.get("petalLength").standardize(petalLength))
                .petalWidth(valueStandardizers.get("petalWidth").standardize(petalWidth))
                .type(type)
                .build();
    }


}
