package pl.jlabs.example.ignite;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.ClientCompute;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import pl.jlabs.example.ignite.data.Iris;
import pl.jlabs.example.ignite.data.IrisNeighboursRequest;
import pl.jlabs.example.ignite.data.distance.IrisDistanceStrategy;
import pl.jlabs.example.ignite.data.standardization.DoubleStandardizer;
import pl.jlabs.example.ignite.util.IrisCSVUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestIrisRecognition {
    public static void main(String[] argv) throws InterruptedException {
        System.out.println("Getting test iris data");
        final List<Iris> testList = IrisCSVUtil.csvResourceAsListIris("iris-test.csv");

        System.out.println("\nStarting Ignite thin client");
        ClientConfiguration cfg = new ClientConfiguration()
                .setAddresses("192.168.1.16:10800", "192.168.1.16:10801", "192.168.1.16:10802")
                .setTimeout(5000);
        try (IgniteClient client = Ignition.startClient(cfg)) {
            System.out.println("\nGetting data standardizer");
            final Map<String, DoubleStandardizer> standardizers = getStandardizerMap(client);

            System.out.println("\nStandardizing test data");
            final List<Iris> standardizedList = testList.stream()
                    .map((iris) -> iris.standardizedWith(standardizers))
                    .collect(Collectors.toList());

            System.out.println("\nTesting ML matcher");
            ClientCompute compute = client.compute();
            int successCount = 0;
            for (Iris item : standardizedList) {
                final long start = System.currentTimeMillis();
                Set<Integer> result = compute.execute(
                        "pl.jlabs.example.ignite.task.IrisNeighboursTask",
                        new IrisNeighboursRequest(item, 3, IrisDistanceStrategy.MANHATTAN)
                );
                System.out.println("Distributed task finished in " + (System.currentTimeMillis() - start) + "ms.");
                final String matched = getMostFrequentType(client, result);
                final boolean success = item.getType().equals(matched);
                successCount += success ? 1 : 0;
                System.out.println((success ? "MATCH" : "FAIL") + " - For tested type '" + item.getType() + "' model matched '" + matched);
            }
            System.out.println("Successfully matched " + successCount
                    + " of " + standardizedList.size()
                    + " irises. Rate: " + (100.0 * successCount / standardizedList.size())
                    + "%"
            );
        }
        System.out.println("\nData tested - exiting");
    }

    private static Map<String, DoubleStandardizer> getStandardizerMap(final IgniteClient client) {
        final ClientCache<String, DoubleStandardizer> standardizerCache = client.getOrCreateCache("iris_standardizer");
        final Set<String> fields = Stream.of("sepalLength", "sepalWidth", "petalLength", "petalWidth").collect(Collectors.toSet());
        return standardizerCache.getAll(fields);
    }

    private static String getMostFrequentType(final IgniteClient client, final Set<Integer> ids) {
        final long start = System.currentTimeMillis();
        final ClientCache<Integer, Iris> irisCache = client.getOrCreateCache("iris_cache");
        final Map<Integer, Iris> matched = irisCache.getAll(ids);
        System.out.println("Nearest irises loaded in " + (System.currentTimeMillis() - start) + "ms.");
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
