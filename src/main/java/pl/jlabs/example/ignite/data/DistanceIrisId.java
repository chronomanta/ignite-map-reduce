package pl.jlabs.example.ignite.data;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

@Data
public class DistanceIrisId implements Comparable<DistanceIrisId> {
    private static final Comparator<DistanceIrisId> NATURAL_ORDER_COMPARATOR = Comparator.comparingDouble(DistanceIrisId::getDistance).thenComparingInt(DistanceIrisId::getIrisId);

    private final double distance;
    private final int irisId;

    @Override
    public int compareTo(@NotNull DistanceIrisId distanceIrisId) {
        return NATURAL_ORDER_COMPARATOR.compare(this, distanceIrisId);
    }
}
