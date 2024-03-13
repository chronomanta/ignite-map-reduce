package pl.jlabs.example.ignite.task;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.compute.*;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.jlabs.example.ignite.data.DistanceIrisId;
import pl.jlabs.example.ignite.data.Iris;
import pl.jlabs.example.ignite.data.IrisNeighboursJobResponse;
import pl.jlabs.example.ignite.data.IrisNeighboursRequest;

import javax.cache.Cache;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IrisNeighboursTask implements ComputeTask<IrisNeighboursRequest, String> {

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
    public @Nullable String reduce(List<ComputeJobResult> list) throws IgniteException {

        final List<IrisNeighboursJobResponse> results = list.stream()
                .filter((result) -> result.getData() != null)
                .map((result) -> (IrisNeighboursJobResponse)result.getData())
                .collect(Collectors.toList());
        if (results.isEmpty()) {
            return "<no match>";
        }
        final int requestedCount = results.get(0).getRequestedCount();
        final Set<Integer> keys = results.stream()
                .map(IrisNeighboursJobResponse::getDistances)
                .flatMap(SortedSet::stream)
                .distinct()
                .sorted()
                .limit(requestedCount)
                .map(DistanceIrisId::getIrisId)
                .collect(Collectors.toSet());
        System.out.println("Reduced collected results to iris IDs: " + keys);
        return mostFrequentType(keys);
    }

    private ComputeJob createIrisNeighboursJob(final IrisNeighboursRequest request) {
        return new ComputeJobAdapter() {
            @Override
            public Object execute() throws IgniteException {
                final IgniteCache<Integer, Iris> cache = ignite.cache("iris_cache");
                final QueryCursor<Cache.Entry<Integer, Iris>> cursor = cache.query(new ScanQuery<Integer, Iris>().setLocal(true));
                final SortedSet<DistanceIrisId> distances = new TreeSet<>();
                cursor.forEach((entry) -> updateDistancesWith(distances, request, entry));
                System.out.println("In metric " + request.getDistanceStrategy() + ", [" + request.getCount() + "] nearest neighbours IDs are " + distances.stream().map(DistanceIrisId::getIrisId).collect(Collectors.toList()));
                return new IrisNeighboursJobResponse(request.getCount(), distances);
            }
        };
    }

    private void updateDistancesWith(final SortedSet<DistanceIrisId> distances,
                                        final IrisNeighboursRequest request,
                                        final Cache.Entry<Integer, Iris> entry) {
        System.out.println("Checking Iris with key: " + entry.getKey());
        final double distance = request.getDistanceStrategy().calculateDistance(request.getIris(), entry.getValue());
        distances.add(new DistanceIrisId(distance, entry.getKey()));
        while(distances.size() > request.getCount()) {
            distances.remove(distances.last());
        }
    }

    private String mostFrequentType(final Set<Integer> keys) {
        final IgniteCache<Integer, Iris> cache = ignite.cache("iris_cache");
        final Map<Integer, Iris> matched = cache.getAll(keys);
        return matched.values().stream()
                .map(Iris::getType)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("<no match>");
    }
}
