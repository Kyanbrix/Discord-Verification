package com.github.kyanbrix.restapi;

import com.google.gson.Gson;
import okhttp3.*;

public class RandomQuote {


    class Quote {

        public String quote;
        public String author;

    }


    public String randomQuote() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .get()
                .url("https://api.quotable.io/quotes/random")
                .build();


        try (Response response = client.newCall(request).execute()) {

            String json = response.body().string();
            Gson gson = new Gson();
            Quote quote = gson.fromJson(json,Quote.class);

            return quote.quote;
        }catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }






}
