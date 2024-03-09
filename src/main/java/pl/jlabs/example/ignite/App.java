package pl.jlabs.example.ignite;

import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.ClientCompute;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

import javax.cache.Cache;
import java.util.Set;

public class App
{
    public static void main( String[] args ) throws InterruptedException
    {
        System.out.println("Starting Ignite thin client");
        ClientConfiguration cfg = new ClientConfiguration()
                .setAddresses("192.168.1.16:10800", "192.168.1.16:10801", "192.168.1.16:10802")
                .setTimeout(5000);
        try (IgniteClient client = Ignition.startClient(cfg)) {
            System.out.println("Starting distributed task");
            ClientCompute compute = client.compute();
            Integer longestWordLength = compute.<String, Integer>execute("pl.jlabs.example.ignite.task.LongestWordLengthTask", "Ala ma kota przeogromnego kulomiota zaczepiastobulwoidowego");
            System.out.println("Longest word length is: " + longestWordLength);
        }
        System.out.println("Exiting");
    }
}
