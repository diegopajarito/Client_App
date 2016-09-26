package com.example.test_rhino3;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import GeoC_QuestionHierarchy.Answer;
import GeoC_QuestionHierarchy.Answer_Deserializer;
import GeoC_QuestionHierarchy.DateTimeConverter;

public class MainActivity extends AppCompatActivity {
    String campaign_configuration = null;
    public static String KEY1 = "key1 of MainActivity.java";
    String lat = "0.00000000";
    String lon = "0.00000000";
    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences pref;
    public static String sendLater_String = "to be sent later";
    Set<String> sendLater = null;
    Set<String> favoriteCampaign = null;
    public static String setCreated_boolean = "boolean status of sendLater";
    boolean setCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String My_URL = "http://192.168.1.138:8880/get_test2";

        System.out.println("The current language setting of the phone is " + Locale.getDefault().getLanguage().toString());
        System.out.println("The current get default of the phone is " + Locale.getDefault().toString());

        pref = getSharedPreferences("pref.xml", MODE_PRIVATE);
        sendLater          = pref.getStringSet("pref.xml", null);
        favoriteCampaign   = pref.getStringSet("fav.xml", null);
        setCreated = pref.getBoolean(setCreated_boolean,false);


        if (setCreated == false)
        {
            sendLater = new HashSet<String>();

            SharedPreferences.Editor editor = pref.edit();
            editor.putStringSet("pref.xml", sendLater);

            favoriteCampaign = new HashSet<String>();
            editor.putStringSet("fav.xml", favoriteCampaign);

            setCreated = true;
            editor.putBoolean(setCreated_boolean, setCreated);
            System.out.println("Create a new set for SendLater and favoriteCampaign");
            System.out.println("The size of sendLater is " + sendLater.size());
            System.out.println("The size of favoriteCampaign is " + favoriteCampaign.size());
            Toast.makeText(getBaseContext(), "Create a new set for SendLater and favoriteCampaign", Toast.LENGTH_LONG).show();

            editor.commit();
        }
        else
        {
            System.out.println("SendLater and favoriteCampaign already exists");
            Toast.makeText(getBaseContext(), "SendLater and favoriteCampaign already exists", Toast.LENGTH_LONG).show();
            sendLater = pref.getStringSet("pref.xml", null);
            favoriteCampaign = pref.getStringSet("fav.xml", null);

            System.out.println("The size of the existing sendLater is " + sendLater.size());
            System.out.println("The size of the existing favoriteCampaign is " + favoriteCampaign.size());

            Iterator<String> it = sendLater.iterator();
            while(it.hasNext()){
                System.out.println(it.next());
            }

        }

        //=================Code for obtaining location

        LocationManager locationManager;
        String context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) getSystemService(context);

        Criteria crta = new Criteria();
        crta.setAccuracy(Criteria.ACCURACY_FINE);
        crta.setAltitudeRequired(false);
        crta.setBearingRequired(false);
        crta.setCostAllowed(true);
        crta.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(crta, true);

         //String provider = LocationManager.GPS_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        updateWithNewLocation(location);

        locationManager.requestLocationUpdates(provider, 1000, 0,
                locationListener);

        //=================Code for obtaining location - END

        System.out.println("Latitude is " + lat);
        System.out.println("Longtitude is " + lon);

        HashMap<String , String> map = new HashMap<String, String>();
        map.put("lat", lat);
        map.put("lon", lon);
        map.put("language", Locale.getDefault().getLanguage().toString() );

        String param = null;
        try {
            param = convertMaptoParam(map);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        My_URL = My_URL + "?" + param;
        System.out.println("URL is " + My_URL);


        new HttpAsyncGET().execute(My_URL);

        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams llLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llLP);

        Button favorite = new Button(this);
        favorite.setText("Favorite campaigns");
        ll.addView(favorite);

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent goto_ListFavoriteCampaign = new Intent(getApplicationContext(), ListFavoriteCampaign.class);
                startActivity(goto_ListFavoriteCampaign);

            }
        });

        Button bt = new Button(this);
        bt.setText(R.string.go_to_Campaign);
        ll.addView(bt);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (campaign_configuration != null) {
                    //Intent intent = new Intent(getApplicationContext(), ActualCampaign.class);
                    Intent intent = new Intent(getApplicationContext(), ListOfCampaign.class);
                    intent.putExtra(KEY1, campaign_configuration);
                    startActivity(intent);
                }
            }
        });

        Button send = new Button(this);
        send.setText("Send the saved results");
        ll.addView(send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (campaign_configuration != null)
                {
                    sendLater = pref.getStringSet("pref.xml", null);
                    System.out.println("The size of sendLater is " + sendLater.size());
                    System.out.println(Questionnaire_Done.server);

                    Iterator<String> it = sendLater.iterator();
                    while(it.hasNext())
                    {

                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeConverter());
                        gsonBuilder.registerTypeAdapter(Answer.class, new Answer_Deserializer());
                        Gson gson = gsonBuilder.create();

                        String content = it.next();

                        Answer[] answer_array = gson.fromJson(content, Answer[].class);


                        String content_with_padding = content;
                        int plaintext_length_without_padding = content.length();
                        int padding_size = ((plaintext_length_without_padding/16)+1)*16 - plaintext_length_without_padding ;
                        for (int i=0;i<padding_size;i++)
                        {
                            content_with_padding += "0";
                        }
                        byte[] cipher = new byte[0];
                        try {
                            cipher = encrypt(content_with_padding, Questionnaire_Done.encryptionKey);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String hexString = byteArrayToHexString(cipher);
                        HashMap<String , String> map = new HashMap<String, String>();
                        map.put("paddingSize", String.valueOf(padding_size));
                        map.put("CampaignID",answer_array[0].getCampaignID());
                        map.put("userID","khoiboo");

                        String param = null;
                        try {
                            param = convertMaptoParam(map);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        final String myURL = Questionnaire_Done.server + "?" + param;

                        new SendPostRequest().execute(myURL, hexString);
                    }

                    sendLater.removeAll(sendLater);
                    pref = getSharedPreferences("pref.xml", MODE_PRIVATE);
                    final SharedPreferences.Editor editor = pref.edit();
                    editor.putStringSet(MainActivity.sendLater_String, sendLater);
                    editor.commit();
                    System.out.println("Now size of sendLater is " + sendLater.size());


                }

            }
        });

        setContentView(ll);
    }

    private class HttpAsyncGET extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Campaign downloaded!", Toast.LENGTH_LONG).show();

            campaign_configuration = result;
            System.out.println("Campaign config is " + campaign_configuration);
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = null;
        try {
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private final LocationListener locationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location)
        {
            updateWithNewLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            updateWithNewLocation(null);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }
    };

    private void updateWithNewLocation(Location location)
    {

        if (location != null)
        {
             lat = String.valueOf(location.getLatitude());
             lon = String.valueOf(location.getLongitude());

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
        cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(Questionnaire_Done.IV.getBytes("UTF-8")));
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


}


