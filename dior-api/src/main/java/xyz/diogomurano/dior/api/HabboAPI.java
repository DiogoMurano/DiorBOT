package xyz.diogomurano.dior.api;

import com.google.gson.GsonBuilder;
import xyz.diogomurano.dior.api.models.DetailedHabboUser;
import xyz.diogomurano.dior.api.models.HabboUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HabboAPI {

    public static HabboUser getUser(String userName) {
        try {
            URL url = new URL("https://www.habbo.com.br/api/public/users?name=" + userName);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            int status = con.getResponseCode();
            if (status > 299) {
                return null;
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            return new GsonBuilder().create().fromJson(content.toString(), HabboUser.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static DetailedHabboUser getDetailedUser(String userName, HabboUser habboUser) {
        try {
            URL url = new URL("https://www.habbo.com.br/api/public/users/" + habboUser.getUniqueId() + "/profile");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            int status = con.getResponseCode();
            if (status > 299) {
                return null;
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            return new GsonBuilder().create().fromJson(content.toString(), DetailedHabboUser.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
