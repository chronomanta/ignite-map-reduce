package pl.jlabs.example.ignite;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import pl.jlabs.example.ignite.data.Iris;
import pl.jlabs.example.ignite.data.standardization.DoubleStandardizer;
import pl.jlabs.example.ignite.util.IrisCSVUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoadIris {
    public static void main(String[] argv) {
        System.out.println("Getting iris data");
        final List<Iris> list = IrisCSVUtil.csvResourceAsListIris("iris.csv");
        System.out.println("\nCreating data standardizer");
        final Map<String, DoubleStandardizer> standardizers = createStandardizerMap(list);

        System.out.println("\nStarting Ignite thin client");
        ClientConfiguration cfg = new ClientConfiguration()
                .setAddresses("192.168.1.16:10800", "192.168.1.16:10801", "192.168.1.16:10802")
                .setTimeout(5000);
        try (IgniteClient client = Ignition.startClient(cfg)) {
            System.out.println("\nCaching data standardizer");
            cacheStandardizers(standardizers, client);

            System.out.println("\nStandardizing data");
            final List<Iris> standarizedList = list.stream()
                    .peek(LoadIris::printDot)
                    .map((iris) -> iris.standardizedWith(standardizers))
                    .collect(Collectors.toList());

            System.out.println("\nCaching iris data");
            final ClientCache<Integer, Iris> irisCache = client.getOrCreateCache("iris_cache");
            irisCache.clear();
            standarizedList.forEach((iris) -> {
                irisCache.put(iris.getId(), iris);
                printDot();
            });
        }
        System.out.println("\nData loaded - exiting");
    }

    private static Map<String, DoubleStandardizer> createStandardizerMap(final List<Iris> list) {
        final Map<String, DoubleStandardizer> ret = new HashMap<>();
        ret.put("sepalLength", DoubleStandardizer.fromValues(list.stream().map(Iris::getSepalLength).collect(Collectors.toList())));
        ret.put("sepalWidth", DoubleStandardizer.fromValues(list.stream().map(Iris::getSepalWidth).collect(Collectors.toList())));
        ret.put("petalLength", DoubleStandardizer.fromValues(list.stream().map(Iris::getPetalLength).collect(Collectors.toList())));
        ret.put("petalWidth", DoubleStandardizer.fromValues(list.stream().map(Iris::getPetalWidth).collect(Collectors.toList())));
        return ret;
    }

    private static void cacheStandardizers(final Map<String, DoubleStandardizer> standardizers, final IgniteClient client) {
        final ClientCache<String, DoubleStandardizer> standardizerCache = client.getOrCreateCache("iris_standardizer");
        standardizerCache.clear();
        standardizerCache.putAll(standardizers);
    }

    private static void printDot(Object o) {
        printDot();
    }
    private static void printDot() {
        System.out.print(".");
    }


}
