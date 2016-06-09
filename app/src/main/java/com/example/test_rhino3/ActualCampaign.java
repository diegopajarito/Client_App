package com.example.test_rhino3;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import project.Base_Question;
import project.FreeTextMulti;
import project.FreeTextSingle;
import project.MultipleChoiceSingle;

public class ActualCampaign extends AppCompatActivity {
    public static String FreeTextSingle = "FreeTextSingle";
    public static String FreeTextMulti = "FreeTextMulti";
    public static String MultipleChoiceSingle = "MultipleChoiceSingle";
    public static String Question_Array = "Question_Array";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actual_campaign);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Gson gson = new Gson();

        Intent intent = getIntent();
        final String message = intent.getStringExtra(MainActivity.CONSTANT1);

        System.out.println(message);

        final JsonObject campaign = new JsonParser().parse(message).getAsJsonObject();

        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams llLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llLP);

        //show("Q003", campaign, ll);

        Displayer myDisplay = new Displayer("khoi", getApplicationContext());
        myDisplay.show("Q004", campaign, ll, this);

        Button bt = new Button(this);
        bt.setText("Next Question");
        ll.addView(bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        setContentView(ll);
    }

    public void show(String ID, JsonObject cam, LinearLayout layout)
    {
        FreeTextSingle temp_FreeTextSingle = null;
        FreeTextMulti temp_FreeTextMulti = null;
        MultipleChoiceSingle temp_MultipleChoiceSingle = null;

        JsonArray array = cam.get(Question_Array).getAsJsonArray();
        for (int i=0;i<array.size();i++)
        {
            Gson gson = new Gson();
            JsonElement temp_element = array.get(i);
            Base_Question temp_basequestion = gson.fromJson(temp_element,Base_Question.class);

            if (temp_basequestion.getQuestionID().equals(ID))
            {
                if (temp_basequestion.getQuestionType().equals(FreeTextSingle))
                {
                    temp_FreeTextSingle = gson.fromJson(temp_element,FreeTextSingle.class);
                    display(temp_FreeTextSingle, layout);
                }

                else if (temp_basequestion.getQuestionType().equals(FreeTextMulti))
                {
                    temp_FreeTextMulti = gson.fromJson(temp_element, FreeTextMulti.class);
                    display(temp_FreeTextMulti, layout);

                }

                else if (temp_basequestion.getQuestionType().equals(MultipleChoiceSingle))
                {
                    temp_MultipleChoiceSingle = gson.fromJson(temp_element, MultipleChoiceSingle.class);
                    display(temp_MultipleChoiceSingle, layout);
                }

            }
        }
    }

    public void common_display(Base_Question obj, final LinearLayout layout)
    {
        System.out.println(obj.getQuestionID() + " " + obj.getQuestionType() + " " + Arrays.toString(obj.getQuestionLabel()));
        String[] quesLabel = obj.getQuestionLabel();
        TextView tv = new TextView(getApplicationContext());
        tv.setText(quesLabel[0]);
        tv.setTextColor(Color.BLACK);
        layout.addView(tv);

        if (quesLabel[1]!= null)
        {
            final String image_URL = quesLabel[1];
            System.out.println("Image URL is " + image_URL);
            final Bitmap[] bmp = new Bitmap[1];
            final ImageView imageview = new ImageView(this);

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        InputStream in = new URL(image_URL).openStream();
                        bmp[0] = BitmapFactory.decodeStream(in);
                    } catch (Exception e) { /*log error*/ }
                    return null;
                }
                @Override
                protected void onPostExecute(Void result) {
                    if (bmp[0] != null)
                    {
                        imageview.setImageBitmap(bmp[0]);
                        layout.addView(imageview);
                    }
                }
            }.execute();
        }

    }

    public void display(FreeTextSingle obj, final LinearLayout layout)
    {
        common_display(obj, layout);

        final EditText edittext = new EditText(this);
        edittext.setText("");
        edittext.setTextColor(Color.BLACK);
        edittext.requestFocus();
        layout.addView(edittext);
    }

    public void display(FreeTextMulti obj, final LinearLayout layout )
    {
        common_display(obj, layout);

        String[] subcomponent = obj.getComponent();
        int size = subcomponent.length;
        TextView[] textview = new TextView[size];
        EditText[] edittext = new EditText[size];
        for (int i=0;i<subcomponent.length;i++)
        {
            textview[i] = new TextView(getApplicationContext());
            textview[i].setText(subcomponent[i]);
            textview[i].setTextColor(Color.BLACK);
            layout.addView(textview[i]);

            edittext[i] = new EditText(getApplicationContext());
            edittext[i].setText("");
            edittext[i].setTextColor(Color.BLACK);
            if (i==0) edittext[i].requestFocus();
            layout.addView(edittext[i]);
        }
    }

    public void display(MultipleChoiceSingle obj, final LinearLayout layout)
    {
        common_display(obj, layout);

        String[] subcomponent = obj.getComponent();
        int size = subcomponent.length;
        RadioGroup radiogroup = new RadioGroup(getApplicationContext());
        for (int i=0;i<size;i++)
        {
            RadioButton radioButton = new RadioButton(getApplicationContext());
            radioButton.setText(subcomponent[i]);
            radioButton.setTextColor(Color.BLACK);
            radiogroup.addView(radioButton);
        }
        layout.addView(radiogroup);

    }
}

class Displayer {
    String name;
    Context context;

    public Displayer(String name, Context context)
    {
        this.name = name;
        this.context = context;
    }

    public void show(String ID, JsonObject cam, LinearLayout layout, Context context)
    {
        String FreeTextSingle = "FreeTextSingle";
        String FreeTextMulti = "FreeTextMulti";
        String MultipleChoiceSingle = "MultipleChoiceSingle";
        String Question_Array = "Question_Array";

        FreeTextSingle temp_FreeTextSingle = null;
        FreeTextMulti temp_FreeTextMulti = null;
        MultipleChoiceSingle temp_MultipleChoiceSingle = null;

        JsonArray array = cam.get(Question_Array).getAsJsonArray();
        for (int i=0;i<array.size();i++)
        {
            Gson gson = new Gson();
            JsonElement temp_element = array.get(i);
            Base_Question temp_basequestion = gson.fromJson(temp_element,Base_Question.class);

            if (temp_basequestion.getQuestionID().equals(ID))
            {
                if (temp_basequestion.getQuestionType().equals(FreeTextSingle))
                {
                    temp_FreeTextSingle = gson.fromJson(temp_element,FreeTextSingle.class);
                    display(temp_FreeTextSingle, layout, context);
                }

                else if (temp_basequestion.getQuestionType().equals(FreeTextMulti))
                {
                    temp_FreeTextMulti = gson.fromJson(temp_element, FreeTextMulti.class);
                    display(temp_FreeTextMulti, layout, context);

                }

                else if (temp_basequestion.getQuestionType().equals(MultipleChoiceSingle))
                {
                    temp_MultipleChoiceSingle = gson.fromJson(temp_element, MultipleChoiceSingle.class);
                    display(temp_MultipleChoiceSingle, layout, context);
                }

            }
        }
    }

    public void common_display(Base_Question obj, final LinearLayout layout, Context context )
    {
        System.out.println(obj.getQuestionID() + " " + obj.getQuestionType() + " " + Arrays.toString(obj.getQuestionLabel()));
        String[] quesLabel = obj.getQuestionLabel();
        TextView tv = new TextView(context);
        tv.setText(quesLabel[0]);
        tv.setTextColor(Color.BLACK);
        layout.addView(tv);

        if (quesLabel[1]!= null)
        {
            final String image_URL = quesLabel[1];
            System.out.println("Image URL is " + image_URL);
            final Bitmap[] bmp = new Bitmap[1];
            final ImageView imageview = new ImageView(context);

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        InputStream in = new URL(image_URL).openStream();
                        bmp[0] = BitmapFactory.decodeStream(in);
                    } catch (Exception e) {  }
                    return null;
                }
                @Override
                protected void onPostExecute(Void result) {
                    if (bmp[0] != null)
                    {
                        imageview.setImageBitmap(bmp[0]);
                        layout.addView(imageview);
                    }
                }
            }.execute();
        }
    }

    public void display(FreeTextSingle obj, final LinearLayout layout, Context context )
    {
        common_display(obj, layout, context);

        final EditText edittext = new EditText(context);
        edittext.setText("");
        edittext.setTextColor(Color.BLACK);
        edittext.requestFocus();
        layout.addView(edittext);
    }

    public void display(FreeTextMulti obj, final LinearLayout layout, Context context )
    {
        common_display(obj, layout, context);

        String[] subcomponent = obj.getComponent();
        int size = subcomponent.length;
        TextView[] textview = new TextView[size];
        EditText[] edittext = new EditText[size];
        for (int i=0;i<subcomponent.length;i++)
        {
            textview[i] = new TextView(context);
            textview[i].setText(subcomponent[i]);
            textview[i].setTextColor(Color.BLACK);
            layout.addView(textview[i]);

            edittext[i] = new EditText(context);
            edittext[i].setText("");
            edittext[i].setTextColor(Color.BLACK);
            if (i==0) edittext[i].requestFocus();
            layout.addView(edittext[i]);
        }
    }

    public void display(MultipleChoiceSingle obj, final LinearLayout layout, Context context )
    {
        common_display(obj, layout, context);

        String[] subcomponent = obj.getComponent();
        int size = subcomponent.length;
        RadioGroup radiogroup = new RadioGroup(context);
        for (int i=0;i<size;i++)
        {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setText(subcomponent[i]);
            radioButton.setTextColor(Color.BLACK);
            radiogroup.addView(radioButton);
        }
        layout.addView(radiogroup);
    }
}
/*
        System.out.println(obj.getQuestionID() + " " + obj.getQuestionType() + " " + Arrays.toString(obj.getQuestionLabel()) + " " + Arrays.toString(obj.getComponent()));
        String[] quesLabel = obj.getQuestionLabel();
        TextView tv = new TextView(context);
        tv.setText(quesLabel[0]);
        tv.setTextColor(Color.BLACK);
        layout.addView(tv);

        if (quesLabel[1]!= null)
        {
            final String image_URL = quesLabel[1];
            System.out.println("Image URL is " + image_URL);
            final Bitmap[] bmp = new Bitmap[1];
            final ImageView imageview = new ImageView(context);

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        InputStream in = new URL(image_URL).openStream();
                        bmp[0] = BitmapFactory.decodeStream(in);
                    } catch (Exception e) { }
                    return null;
                }
                @Override
                protected void onPostExecute(Void result) {
                    if (bmp[0] != null)
                    {
                        imageview.setImageBitmap(bmp[0]);
                        layout.addView(imageview);
                    }
                }
            }.execute();
        }
         */