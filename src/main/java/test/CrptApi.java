package test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {
    private final Lock lock = new ReentrantLock();
    private final int requestLimit;
    private final long intervalMillis;
    private long lastRequestTime = System.currentTimeMillis();
    private int requestCount = 0;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.intervalMillis = timeUnit.toMillis(1);
    }

    public void createDocument(String documentJson, String signature) {
        lock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRequestTime >= intervalMillis) {
                // Reset the request count if the interval has passed
                requestCount = 0;
            }

            // Check if request limit is reached
            if (requestCount >= requestLimit) {
                // Sleep until the next interval
                long sleepTime = intervalMillis - (currentTime - lastRequestTime);
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                // Reset the request count and update last request time
                requestCount = 0;
                lastRequestTime = System.currentTimeMillis();
            }

            // Make the API call
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(documentJson))
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                // Placeholder code to handle response if needed
                System.out.println("API call response: " + response.statusCode());
            } catch (Exception e) {
                e.printStackTrace();
                // Handle exception
            }

            // Increment request count and update last request time
            requestCount++;
            lastRequestTime = System.currentTimeMillis();
        } finally {
            lock.unlock();
        }
    }
}