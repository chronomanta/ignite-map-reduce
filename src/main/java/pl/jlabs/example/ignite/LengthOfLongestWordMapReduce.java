package pl.jlabs.example.ignite;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCompute;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

public class LengthOfLongestWordMapReduce
{
    private static final String HOST = "localhost";

    public static void main( String[] args ) throws InterruptedException
    {
        System.out.println("Starting Ignite thin client");
        ClientConfiguration cfg = new ClientConfiguration()
                .setAddresses(HOST + ":10800", HOST + ":10801", HOST + ":10802")
                .setTimeout(5000);
        try (IgniteClient client = Ignition.startClient(cfg)) {
            System.out.println("Starting distributed task");
            ClientCompute compute = client.compute();
            Integer longestWordLength = compute.<String, Integer>execute(
                    "pl.jlabs.example.ignite.task.LongestWordLengthTask",
                    "Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
            );
            System.out.println("Longest word length is: " + longestWordLength);
        }
        System.out.println("Exiting");
    }
}
