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
    private static final String HOST = "localhost";

    public static void main(String[] argv) throws InterruptedException {
        System.out.println("Getting test iris data");
        final List<Iris> testList = IrisCSVUtil.csvResourceAsListIris("iris-test.csv");

        System.out.println("Starting Ignite thin client");
        ClientConfiguration cfg = new ClientConfiguration()
                .setAddresses(HOST + ":10800", HOST + ":10801", HOST + ":10802")
                .setTimeout(5000);
        try (IgniteClient client = Ignition.startClient(cfg)) {
            System.out.println("Getting data standardizer");
            final Map<String, DoubleStandardizer> standardizers = getStandardizerMap(client);

            final List<Iris> standardizedList = testList.stream()
                    .map((iris) -> iris.standardizedWith(standardizers))
                    .collect(Collectors.toList());

            System.out.println("Testing ML matcher");
            ClientCompute compute = client.compute();
            int successCount = 0;
            for (Iris item : standardizedList) {
                final String result = compute.execute(
                        "pl.jlabs.example.ignite.task.IrisNeighboursTask",
                        new IrisNeighboursRequest(item, 5, IrisDistanceStrategy.EUCLID)
                );
                final boolean success = item.getType().equals(result);
                successCount += success ? 1 : 0;
                System.out.println((success ? "MATCH" : "FAIL") + " - For tested type '" + item.getType() + "' model matched '" + result);
            }
            System.out.println("Successfully matched " + successCount
                    + " of " + standardizedList.size()
                    + " irises. Success rate: " + (100.0 * successCount / standardizedList.size())
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

}
