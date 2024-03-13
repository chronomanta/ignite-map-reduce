package pl.jlabs.example.ignite.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

@RequiredArgsConstructor
@Getter
public class IrisNeighboursJobResponse {
    private final int requestedCount;
    private final SortedSet<DistanceIrisId> distances;
}
