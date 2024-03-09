package pl.jlabs.example.ignite.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.SneakyThrows;
import pl.jlabs.example.ignite.LoadIris;
import pl.jlabs.example.ignite.data.Iris;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class IrisCSVUtil {
    @SneakyThrows
    public static List<Iris> csvResourceAsListIris(final String fileName) {
        try (final InputStream is = LoadIris.class.getClassLoader().getResourceAsStream(fileName);
             final CSVReader reader = new CSVReaderBuilder(new InputStreamReader(is)).withSkipLines(1).build()) {
            return reader.readAll()
                    .stream()
                    .map(IrisCSVUtil::mapToIris)
                    .collect(Collectors.toList());
        }
    }

    private static Iris mapToIris(String[] fields) {
        return Iris.builder()
                .id(Integer.parseInt(fields[0]))
                .sepalLength(Double.parseDouble(fields[1]))
                .sepalWidth(Double.parseDouble(fields[2]))
                .petalLength(Double.parseDouble(fields[3]))
                .petalWidth(Double.parseDouble(fields[4]))
                .type(fields[5])
                .build();
    }
}
