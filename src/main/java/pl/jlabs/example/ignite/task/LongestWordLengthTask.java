package pl.jlabs.example.ignite.task;

import org.apache.ignite.IgniteException;
import org.apache.ignite.compute.ComputeJob;
import org.apache.ignite.compute.ComputeJobAdapter;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.compute.ComputeTaskSplitAdapter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LongestWordLengthTask extends ComputeTaskSplitAdapter<String, Integer> {
    @Override
    protected Collection<? extends ComputeJob> split(final int gridSize, final String argument) throws IgniteException {
        return Stream.of(argument.split(" "))
                .map(this::wordLength)
                .collect(Collectors.toList());
    }

    @Override
    public Integer reduce(final List<ComputeJobResult> list) throws IgniteException {
        return list.stream()
                .mapToInt(ComputeJobResult::getData)
                .max()
                .orElse(0);
    }

    private ComputeJob wordLength(final String word) {
        return new ComputeJobAdapter() {
            @Override
            public Object execute() throws IgniteException {
                System.out.println("Calculating mapped word length: " + word);
                return word.length();
            }
        };
    }

}
