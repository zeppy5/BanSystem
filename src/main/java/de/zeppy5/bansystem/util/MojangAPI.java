package de.zeppy5.bansystem.util;

import com.google.gson.Gson;
import org.bukkit.Bukkit;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class MojangAPI {

    public static String getAPI(URI uri) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(uri).build();
        CompletableFuture<HttpResponse<String>> responseCompletableFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response;

        try {
            response = responseCompletableFuture.get();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "API CONNECTION ERROR: " + e.getMessage());
            throw new RuntimeException();
        }

        return response.body();
    }

    public static String getUUID(String name) {
        try {
            return new Gson().fromJson(getAPI(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name)), Profile.class)
                    .getId().replaceAll(
                            "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                            "$1-$2-$3-$4-$5");
        } catch (Exception e) {
            return null;
        }
    }

}
