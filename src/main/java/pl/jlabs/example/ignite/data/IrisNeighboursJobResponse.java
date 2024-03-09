package pl.jlabs.example.ignite.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public class IrisNeighboursJobResponse {
    private final int requestedCount;
    private final Map<Double, Set<Integer>> distances;
}
