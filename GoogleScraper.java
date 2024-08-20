

package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class GoogleScraper {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the search query: ");
        String query = scanner.nextLine();

        try {
            executeGooglerCommand(query);
            processFile("result.txt");

            System.out.println("Processing complete.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void executeGooglerCommand(String query) throws IOException {
        String command = "googler -n 500 \"" + query + "\" > result.txt --np";
        Process process = Runtime.getRuntime().exec(new String[]{"cmd", "/c", command});

        // Wait for the process to complete
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Googler command failed with exit code " + exitCode);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void processFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("File not found: " + filePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             PrintWriter writer = new PrintWriter(new FileWriter("output.txt", true))) {

            String line;
            Pattern urlPattern = Pattern.compile("http[s]?://\\S+");
            while ((line = reader.readLine()) != null) {
                Matcher matcher = urlPattern.matcher(line);
                while (matcher.find()) {
                    String url = matcher.group();
                    System.out.println("Fetching  from URL: " + url); 
                    String content = fetchContentFromUrl(url);
                    if (content != null) {
                        String result = formatResult(url, content);
                        writer.println(result);
                    } else {
                        System.err.println("Failed to fetch content from URL: " + url);
                    }
                }
            }
        }
    }

    private static String fetchContentFromUrl(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                return new String(response.getEntity().getContent().readAllBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String formatResult(String url, String content) {
        String strippedContent = stripHtml(content);
        // Format the result as needed
        return "URL: " + url + "\nContent:\n" + strippedContent + "\n";
    }

    private static String stripHtml(String html) {
        Document doc = Jsoup.parse(html);
        return doc.text();
    }
}

