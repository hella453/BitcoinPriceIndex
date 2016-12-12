package com.mybitcoin.helena.bitcoinpriceindex;

import android.os.AsyncTask;
import android.os.SystemClock;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static android.content.ContentValues.TAG;

/**
 * Created by Helena on 12/11/2016.
 */

public class HistoryAsyncTask extends AsyncTask<Void, Void, String> {

        private Exception exception;
        public MainActivity activity;

        public HistoryAsyncTask(MainActivity a) {
                this.activity = a;
        }

        protected void onPreExecute() {
                activity.progressBar1.setVisibility(View.VISIBLE);
        }
        protected String doInBackground(Void... urls) {

                // Do some validation here

                try {
                        String secretKey = "MjZjYzYxMTVmNzZjNDc4ZmEzNjZhZTc4MTUzMTJmNDBmODQ5MTI3OTg4MGY0YjlkYTljNjE5ZmJkNDgyZGNhNA";
                        String publicKey = "YjNhNjYwOGY4ODNjNDUzNDk1OWM2YWY2ZWY4Mjg1MDg";
                        String signature = getSignature(secretKey, publicKey);
                        String url = "https://apiv2.bitcoinaverage.com/indices/global/history/BTCEUR?period=alltime&format=json";
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



        protected void onPostExecute(String response) {
                if (response == null) {
                        response = "THERE WAS AN ERROR";
                }
                activity.progressBar1.setVisibility(View.GONE);
                //Log.i("INFO", response);
                setValues(response);
        }

        private void setValues(String response) {
                if (response != null) {
                        try {
                                JSONArray jArray = new JSONArray(response);
                                for (int i = 0; i < 7; i++) {
                                        JSONObject json_data = jArray.getJSONObject(i);
                                        HistoryModel data = new HistoryModel();// Create Object Here
                                        float average = Float.parseFloat(json_data.getString("average"));
                                        data.setAverage(average);
                                        String dtStart = json_data.getString("time");
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        SimpleDateFormat outputFormat = new SimpleDateFormat("yyy");
                                        format.setTimeZone(TimeZone.getTimeZone("UTC+1"));
                                        try {
                                                Date date = format.parse(dtStart);
                                                data.setTime(date.getTime());
                                        } catch (ParseException e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                        }
                                        activity.list.add(data);// Finally adding the model to List
                                }
                            activity.createChart();
                                /*for(int i=0;i<activity.list.size();i++){
                                        HistoryModel history=activity.list.get(i);
                                        float average = history.getAverage();
                                        long time = history.getTime();
                                        System.out.println("Average: "+average+"Time "+time);
                                }
                                */

                        } catch (JSONException e) {
                                e.printStackTrace();
                        }
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
}


