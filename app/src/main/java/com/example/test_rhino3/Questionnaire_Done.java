package com.example.test_rhino3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;


public class Questionnaire_Done extends AppCompatActivity {
    //public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences pref;

    public static String server = "http://192.168.1.138:8880/send_result";
    public static String IV = "AAAAAAAAAAAAAAAA";
    public static String encryptionKey = "0123456789abcdef";

    String campaign = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire__done);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams llLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llLP);

        Intent intent = getIntent();
        final String tobesent_text = intent.getStringExtra(ActualCampaign.CONTENT);
         String tobesent_with_padding = tobesent_text;
        int plaintext_length_without_padding = tobesent_text.length();
        int padding_size = ((plaintext_length_without_padding/16)+1)*16 - plaintext_length_without_padding ;
        for (int i=0;i<padding_size;i++)
        {
            tobesent_with_padding += "0";
        }
        System.out.println(tobesent_with_padding);
        byte[] cipher = new byte[0];
        try {
              cipher = encrypt(tobesent_with_padding, encryptionKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String hexString = byteArrayToHexString(cipher);
        System.out.println(byteArrayToHexString(cipher));


        final String campaignID = intent.getStringExtra(ActualCampaign.CampaignID);
        final String campaignConfig = intent.getStringExtra(ActualCampaign.CampaignConfig);


        System.out.println(campaignID + " has been done!");

        HashMap<String , String> map = new HashMap<String, String>();
        map.put("paddingSize", String.valueOf(padding_size));
        map.put("CampaignID",campaignID);
        map.put("userID","khoiboo");

        String param = null;
        try {
            param = convertMaptoParam(map);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final Intent intent2 = new Intent(this, MainActivity.class);

        final String myURL = server + /*campaignID +*/ "?" + param;

        Button addFavorite = new Button(this);
        addFavorite.setText("Add to your favorite list");
        ll.addView(addFavorite);
        addFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                pref = getSharedPreferences("pref.xml", MODE_PRIVATE);
                final SharedPreferences.Editor editor = pref.edit();
                Set<String> tempFavCampaign = pref.getStringSet("fav.xml",null);
                System.out.println("Size of favoriteCampaign is " + tempFavCampaign.size());
                tempFavCampaign.add(campaignConfig);
                System.out.println("After adding a new campaign, size of favoriteCampaign is " + tempFavCampaign.size());
                editor.putStringSet("fav.xml", tempFavCampaign);
                editor.commit();
            }
        });

        //Send the results LATER
        final Button sendLater = new Button(this);
        sendLater.setText("Send later");
        ll.addView(sendLater);
        sendLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pref = getSharedPreferences("pref.xml", MODE_PRIVATE);
                final SharedPreferences.Editor editor = pref.edit();
                Set<String> tempSet = pref.getStringSet("pref.xml",null);
                tempSet.add(tobesent_text);
                editor.putStringSet(MainActivity.sendLater_String, tempSet);
                editor.commit();


                startActivity(intent2);
            }
        });

        //Send the results NOW
        Button bt = new Button(this);
        bt.setText("Send results");
        ll.addView(bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendPostRequest().execute(myURL, hexString);

                startActivity(intent2);
            }
        });

        setContentView(ll);
    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg) {

            try {
                String text = arg[0];

                URL url = new URL(text); // here is your URL path

                System.out.println("URL is " + url.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(arg[1]);

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
        }
    }

    public static String convertMaptoParam(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public static byte[] encrypt(String plainText, String encryptionKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
        return cipher.doFinal(plainText.getBytes("UTF-8"));
    }

    public static String byteArrayToHexString(byte[] array) {
        StringBuffer hexString = new StringBuffer();
        for (byte b : array) {
            int intVal = b & 0xff;
            if (intVal < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(intVal));
        }
        return hexString.toString();
    }


}
