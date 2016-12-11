package com.mybitcoin.helena.bitcoinpriceindex;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static android.content.ContentValues.TAG;
import static java.lang.System.in;

/**
 * Created by Helena on 12/10/2016.
 */

class DataAsyncTask extends AsyncTask<Void, Void, String> {

    private Exception exception;
    public MainActivity activity;
    public String variable1;
    public String variable2;
    public DataAsyncTask(MainActivity a) {
        this.activity = a;
    }

    protected void onPreExecute() {
        activity.progressBar.setVisibility(View.VISIBLE);

    }

    protected String doInBackground(Void... urls) {

        // Do some validation here

        try {
            String secretKey = "MjZjYzYxMTVmNzZjNDc4ZmEzNjZhZTc4MTUzMTJmNDBmODQ5MTI3OTg4MGY0YjlkYTljNjE5ZmJkNDgyZGNhNA";
            String publicKey = "YjNhNjYwOGY4ODNjNDUzNDk1OWM2YWY2ZWY4Mjg1MDg";
            String signature = getSignature(secretKey, publicKey);
            String url = "https://apiv2.bitcoinaverage.com/indices/global/ticker/BTCEUR";
            URL urlObj = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            try {

                connection.setRequestMethod("GET");
                connection.setRequestProperty("X-Signature", signature);

                // read all the lines of the response into response StringBuffer
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = bufferedReader.readLine()) != null) {
                    response.append(inputLine);
                }
                bufferedReader.close();
                return response.toString();
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }

    }

    private String getSignature(String secretKey, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException

    {

        long timestamp = System.currentTimeMillis() / 1000L;
        String payload = timestamp + "." + publicKey;

        try {
            SecretKeySpec key = new SecretKeySpec((secretKey).getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] bytes = mac.doFinal(payload.getBytes("ASCII"));
            StringBuffer hash = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            String signature = payload + "." + hash;

            return signature;
        } catch (UnsupportedEncodingException e) {
            return "1";
        } catch (InvalidKeyException e) {
            return "2";
        } catch (NoSuchAlgorithmException e) {
            return "3";
        }


    }

    protected void onPostExecute(String response) {
        if (response == null) {
            response = "THERE WAS AN ERROR";
        }
        activity.progressBar.setVisibility(View.GONE);

        Log.i("INFO", response);
        if (response != null) {
            try {


                JSONObject jsonObj = new JSONObject(response);

                if (jsonObj != null) {
                    JSONObject changes = jsonObj.getJSONObject("changes");

                    //Price, percentage minus, plus
                    JSONObject priceObj = changes.getJSONObject("price");
                    String price = priceObj.getString("day");
                    JSONObject percentObj = changes.getJSONObject("percent");
                    String percent = percentObj.getString("day");
                    if (percent.contains("-")){
                        activity.TV_minus_plus_change.setTextColor(Color.RED);
                    }else{
                        activity.TV_minus_plus_change.setTextColor(Color.GREEN);
                    }
                    activity.TV_minus_plus_change.setText(price + "€ " + (percent) + "%");

                    //Today's open
                    JSONObject openObj = jsonObj.getJSONObject("open");
                    String open = openObj.getString("day");
                    activity.TV_today_open.setText("Today's open: " + open);



                //Last update
                    String dtStart = jsonObj.getString("display_timestamp");
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyy - HH:mm");
                    format.setTimeZone(TimeZone.getTimeZone("UTC+1"));
                    try {
                        Date date = format.parse(dtStart);
                        String str = outputFormat.format(date);
                        activity.TV_last_update.setText("Last updated: " + str);

                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                activity.TV_today_high.setText("Today's high: " + jsonObj.getString("high").toString());
                activity.TV_today_low.setText("Today's low: " + jsonObj.getString("low").toString());
                activity.TV_bitcoin_index.setText("€ " + jsonObj.getString("last").toString());
            }
            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity.getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });




        try {
           // if you don't want to use Gson, you can just print the plain response
            System.out.println(response.toString());
            // print result in nice format using the Gson library
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(response.toString());
            String prettyJsonResponse = gson.toJson(je);
            System.out.println(prettyJsonResponse);



        }
        catch (Exception o) {
            o.printStackTrace();
        }


            }
        }
    }
}

