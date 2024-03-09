package pl.jlabs.example.ignite.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.jlabs.example.ignite.data.distance.IrisDistanceStrategy;

@RequiredArgsConstructor
@Getter
public class IrisNeighboursRequest {

    private final Iris iris;
    private final int count;
    private final IrisDistanceStrategy distanceStrategy;
}
