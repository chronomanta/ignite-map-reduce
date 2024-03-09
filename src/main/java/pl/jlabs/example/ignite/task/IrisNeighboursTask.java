package pl.jlabs.example.ignite.task;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.compute.*;
import org.apache.ignite.resources.IgniteInstanceResource;
import pl.jlabs.example.ignite.data.Iris;
import pl.jlabs.example.ignite.data.IrisNeighboursJobResponse;
import pl.jlabs.example.ignite.data.IrisNeighboursRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.cache.Cache;
import java.util.HashSet;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class IrisNeighboursTask implements ComputeTask<IrisNeighboursRequest, Set<Integer>> {

    @IgniteInstanceResource
    private Ignite ignite;

    @Override
    public @NotNull Map<? extends ComputeJob, ClusterNode> map(List<ClusterNode> list, @Nullable IrisNeighboursRequest request) throws IgniteException {
        return list.stream().collect(Collectors.toMap(
                (node) -> createIrisNeighboursJob(request),
                (node) -> node
        ));
    }

    @Override
    public ComputeJobResultPolicy result(ComputeJobResult computeJobResult, List<ComputeJobResult> list) throws IgniteException {
        return ComputeJobResultPolicy.WAIT;
    }

    @Override
    public @Nullable Set<Integer> reduce(List<ComputeJobResult> list) throws IgniteException {
        final SortedMap<Double, Set<Integer>> merged = new TreeMap<>();
        final int requestedCount = list.stream()
                .filter((result) -> result.getData() != null)
                .map((result) -> ((IrisNeighboursJobResponse)result.getData()).getRequestedCount())
                .findFirst()
                .orElse(0);
        if (requestedCount <= 0) {
            return new HashSet<>();
        }
        list.stream()
                .filter((result) -> result.getData() != null)
                .map((result) -> (IrisNeighboursJobResponse)result.getData())
                .forEach((item) -> mergeToDistanceMap(merged, item.getDistances(), requestedCount));
        return merged.values().stream()
                .flatMap(Set::stream)
                .limit(requestedCount)
                .collect(Collectors.toSet());
    }

    private ComputeJob createIrisNeighboursJob(final IrisNeighboursRequest request) {
        return new ComputeJobAdapter() {
            @Override
            public Object execute() throws IgniteException {
                final long start = System.currentTimeMillis();
                final IgniteCache<Integer, Iris> cache = ignite.cache("iris_cache");
                final QueryCursor<Cache.Entry<Integer, Iris>> cursor = cache.query(new ScanQuery<Integer, Iris>().setLocal(true));
                final SortedMap<Double, Set<Integer>> distancesMap = new TreeMap<>();
                cursor.forEach((entry) -> updateDistancesMapWith(distancesMap, request, entry));
                System.out.println("Cache scanned in " + (System.currentTimeMillis() - start) + "ms");
                return new IrisNeighboursJobResponse(request.getCount(), distancesMap);
            }
        };
    }

    private void mergeToDistanceMap(final SortedMap<Double, Set<Integer>> distancesMap, final Map<Double, Set<Integer>> toMerge, final int count) {
        toMerge.forEach((key, value) ->
                distancesMap.compute(key, (k, v) -> valueWithAddedIds(v, value))
        );
        while(distancesMap.size() > count) {
            distancesMap.remove(distancesMap.lastKey());
        }
    }

    private void updateDistancesMapWith(final SortedMap<Double, Set<Integer>> distancesMap,
                                        final IrisNeighboursRequest request,
                                        final Cache.Entry<Integer, Iris> entry) {
        System.out.println("Checking Iris with key: " + entry.getKey());
        final double distance = request.getIris().distanceTo(entry.getValue(), request.getDistanceStrategy());
        distancesMap.compute(distance, (key, value) -> valueWithAddedId(value, entry.getKey()));
        while(distancesMap.size() > request.getCount()) {
            distancesMap.remove(distancesMap.lastKey());
        }
    }

    private Set<Integer> valueWithAddedId(final Set<Integer> previous, final Integer idToAdd) {
        final Set<Integer> ret = Optional.ofNullable(previous).orElse(new HashSet<>());
        ret.add(idToAdd);
        return ret;
    }

    private Set<Integer> valueWithAddedIds(final Set<Integer> previous, final Set<Integer> idsToAdd) {
        final Set<Integer> ret = Optional.ofNullable(previous).orElse(new HashSet<>());
        ret.addAll(idsToAdd);
        return ret;
    }

}
