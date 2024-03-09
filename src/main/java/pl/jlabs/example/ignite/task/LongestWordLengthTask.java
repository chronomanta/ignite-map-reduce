package pl.jlabs.example.ignite.task;

import org.apache.ignite.IgniteException;
import org.apache.ignite.compute.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LongestWordLengthTask extends ComputeTaskSplitAdapter<String, Integer> {
    @Override
    protected Collection<? extends ComputeJob> split(int gridSize, String argument) throws IgniteException {
        return Stream.of(argument.split(" "))
                .map(this::wordLength)
                .collect(Collectors.toList());
    }

    @Override
    public Integer reduce(List<ComputeJobResult> list) throws IgniteException {
        return list.stream()
                .mapToInt(ComputeJobResult::getData)
                .max()
                .orElse(0);
    }

    private ComputeJob wordLength(String word) {
        return new ComputeJobAdapter() {
            @Override
            public Object execute() throws IgniteException {
                System.out.println("Calculating mapped word length: " + word);
                return word.length();
            }
        };
    }

}
