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
    private static final String HOST = "localhost";

    public static void main(String[] argv) {
        System.out.println("Getting iris data");
        final List<Iris> list = IrisCSVUtil.csvResourceAsListIris("iris.csv");
        final Map<String, DoubleStandardizer> standardizers = createStandardizerMap(list);

        System.out.println("Starting Ignite thin client");
        ClientConfiguration cfg = new ClientConfiguration()
                .setAddresses(HOST + ":10800", HOST + ":10801", HOST + ":10802")
                .setTimeout(5000);
        try (IgniteClient client = Ignition.startClient(cfg)) {
            System.out.println("Uploading data");
            cacheStandardizers(standardizers, client);
            final ClientCache<Integer, Iris> irisCache = client.getOrCreateCache("iris_cache");
            irisCache.clear();
            list.stream()
                    .map((iris) -> iris.standardizedWith(standardizers))
                    .forEach((iris) -> irisCache.put(iris.getId(), iris));
        }
        System.out.println("Data uploaded");
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
}
