package com.crazicrafter1.crutils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class TestMain {

    public static void main(String[] args) {
        try {
            URLConnection con = new URL(
                    String.format("https://github.com/%s/%s/releases/latest/", "PeriodicSeizures", "LootCrates")).openConnection();
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);
            con.connect();

            InputStream is = con.getInputStream();
            URL download = con.getURL();
            is.close();

            String path = download.toString();
            System.out.println(download.toString().lastIndexOf('/'));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
