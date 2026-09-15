package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) throws Exception {

        String[] artists = {
                "Chappell Roan",
                "Reneé Rapp",
                "Troye Sivan",
                "Sam Smith",
                "Lil Nas X",
                "Billie Eilish",
                "Miley Cyrus",
                "Halsey",
                "Frank Ocean",
                "Janelle Monáe",
                "Omar Apollo",
                "Tove Lo"
        };

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        String date = LocalDate.now().toString();

        FileWriter fileWriter = new FileWriter("deezer_data.csv", true);
        PrintWriter writer = new PrintWriter(fileWriter);

        // Add the header only if the file is empty
        if (new java.io.File("deezer_data.csv").length() == 0) {
            writer.println("Date,Artist,Song,Rank");
        }

        for (String artistName : artists) {

            String searchUrl =
                    "https://api.deezer.com/search/artist?q="
                            + artistName.replace(" ", "%20");

            HttpRequest searchRequest = HttpRequest.newBuilder()
                    .uri(URI.create(searchUrl))
                    .GET()
                    .build();

            HttpResponse<String> searchResponse =
                    client.send(searchRequest, HttpResponse.BodyHandlers.ofString());

            JsonNode searchData = mapper.readTree(searchResponse.body());

            if (searchData.get("data").isEmpty()) {
                System.out.println("Artist not found: " + artistName);
                continue;
            }

            int artistId = searchData.get("data").get(0).get("id").asInt();

            String tracksUrl =
                    "https://api.deezer.com/artist/" + artistId + "/top?limit=10";

            HttpRequest tracksRequest = HttpRequest.newBuilder()
                    .uri(URI.create(tracksUrl))
                    .GET()
                    .build();

            HttpResponse<String> tracksResponse =
                    client.send(tracksRequest, HttpResponse.BodyHandlers.ofString());

            JsonNode tracksData = mapper.readTree(tracksResponse.body());

            for (JsonNode track : tracksData.get("data")) {

                String song = track.get("title").asText();
                int rank = track.get("rank").asInt();

                writer.println(
                        date + "," + artistName + "," + song + "," + rank
                );
            }

            System.out.println("Collected: " + artistName);
        }

        writer.close();

        System.out.println("Data saved to deezer_data.csv");
    }
}