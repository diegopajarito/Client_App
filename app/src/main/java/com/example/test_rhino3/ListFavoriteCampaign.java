package com.example.test_rhino3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Set;

import GeoC_QuestionHierarchy.BaseQuestion_Deserializer;
import GeoC_QuestionHierarchy.Base_Question;
import GeoC_QuestionHierarchy.Branch;
import GeoC_QuestionHierarchy.Branch_Deserializer;
import GeoC_QuestionHierarchy.Campaign;
import GeoC_QuestionHierarchy.Campaign_Deserializer;
import GeoC_QuestionHierarchy.DateTimeConverter;
import GeoC_QuestionHierarchy.Workflow_Element;
import GeoC_QuestionHierarchy.Workflow_Element_Deserializer;

public class ListFavoriteCampaign extends AppCompatActivity {
    SharedPreferences pref;
    public static String CONTENT_FAV_CAMPAIGN = "content of the favorite campaign";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_favorite_campaign);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pref = getSharedPreferences("pref.xml", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        Set<String> listFavCampaign = pref.getStringSet("fav.xml",null);
        System.out.println("Inside listFavoriteCampaign.java, size of favoriteCampaign is " + listFavCampaign.size());
        Toast.makeText(getBaseContext(), String.valueOf(listFavCampaign.size()), Toast.LENGTH_LONG).show();

        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams llLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llLP);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Branch.class, new Branch_Deserializer());
        gsonBuilder.registerTypeAdapter(Workflow_Element.class, new Workflow_Element_Deserializer());
        gsonBuilder.registerTypeAdapter(Base_Question.class, new BaseQuestion_Deserializer());
        gsonBuilder.registerTypeAdapter(Campaign.class, new Campaign_Deserializer());
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeConverter());
        final Gson gson = gsonBuilder.create();

        final String[] arrayFav = listFavCampaign.toArray(new String[listFavCampaign.size()]);
        System.out.println("The size of arrayFav is " + arrayFav.length);
        System.out.println(Arrays.toString(arrayFav));
        Button[] arrayButton = new Button[arrayFav.length];




        for (int i=0;i<arrayFav.length;i++)
        {
            Campaign cam = gson.fromJson(arrayFav[i], Campaign.class);
            arrayButton[i] = new Button(this);
            arrayButton[i].setText(cam.getID());
            ll.addView(arrayButton[i]);
            final int index = i;

            arrayButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent goto_ActualFavCampaign = new Intent(getApplicationContext(), ActualCampaign.class);
                    goto_ActualFavCampaign.putExtra(CONTENT_FAV_CAMPAIGN, arrayFav[index]);
                    startActivity(goto_ActualFavCampaign);

                }
            });
        }


        setContentView(ll);
    }

}
