package com.example.test_rhino3;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import GeoC_QuestionHierarchy.Answer;
import GeoC_QuestionHierarchy.AudioSensor;
import GeoC_QuestionHierarchy.BaseQuestion_Deserializer;
import GeoC_QuestionHierarchy.Base_Question;
import GeoC_QuestionHierarchy.Branch;
import GeoC_QuestionHierarchy.Branch_Deserializer;
import GeoC_QuestionHierarchy.Campaign;
import GeoC_QuestionHierarchy.Campaign_Deserializer;
import GeoC_QuestionHierarchy.ContRange;
import GeoC_QuestionHierarchy.DateTimeConverter;
import GeoC_QuestionHierarchy.DateWrapFactory;
import GeoC_QuestionHierarchy.FreeTextMulti;
import GeoC_QuestionHierarchy.FreeTextSingle;
import GeoC_QuestionHierarchy.MultipleChoiceMulti;
import GeoC_QuestionHierarchy.MultipleChoiceSingle;
import GeoC_QuestionHierarchy.MyAnswerWrapper;
import GeoC_QuestionHierarchy.Workflow_Element;
import GeoC_QuestionHierarchy.Workflow_Element_Deserializer;

public class ActualCampaign extends AppCompatActivity {
    public static String FreeTextSingle = "FreeTextSingle";
    public static String FreeTextMulti = "FreeTextMulti";
    public static String MultipleChoiceSingle = "MultipleChoiceSingle";
    public static String MultipleChoiceMulti = "MultipleChoiceMulti";
    public static String ContRange = "ContRange";
    public static String AudioSensor = "AudioSensor";

    public static String CONTENT = "content from ArrayList that stores Answer objects";
    public static String CampaignID = "the ID of the current campaign";
    public static String CampaignConfig = "The JSON config of the campaign";

    ArrayList<String> tag_list = new ArrayList();

    int count = 0; //control index for answer_array

    //AudioSensor variables
    double sum = 0.0;
    int sample_number = 0;
    long startTime;
    long stopTime;
    double average=0;
    double max=0;
    double min=Double.MAX_VALUE;
    MediaRecorder mRecorder;
    Thread runner;
    final Runnable updater = new Runnable(){

        public void run(){
            updateTv();
        };
    };
    final Handler mHandler = new Handler();
    public Button start;
    public Button stop;
    TextView currentValue;

    String temp = null;
    String message = null;

    String lat = "0.000000";
    String lon = "0.000000";




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actual_campaign);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Branch.class, new Branch_Deserializer());
        gsonBuilder.registerTypeAdapter(Workflow_Element.class, new Workflow_Element_Deserializer());
        gsonBuilder.registerTypeAdapter(Base_Question.class, new BaseQuestion_Deserializer());
        gsonBuilder.registerTypeAdapter(Campaign.class, new Campaign_Deserializer());
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeConverter());
        final Gson gson = gsonBuilder.create();

        Intent intent = getIntent();
        //final String message = intent.getStringExtra(MainActivity.KEY1);
        message = intent.getStringExtra(ListOfCampaign.campaignConfig);
        temp = intent.getStringExtra(ListFavoriteCampaign.CONTENT_FAV_CAMPAIGN);

        if (message == null) message = temp;

        System.out.println("Value of temp string is " + temp);

        System.out.println(message);

        final Campaign campaign_obj = gson.fromJson(message, Campaign.class);
        //final Campaign[] Campaign_Array = gson.fromJson(message, Campaign[].class);

        String startQuestion = campaign_obj.getStartQuestion();
        List<Base_Question> ques_array = campaign_obj.getQuestionArray();

        final List<Workflow_Element> workflow_element = campaign_obj.getWorkflow();
        for(int i=0;i<workflow_element.size();i++)
        {
            System.out.println(workflow_element.get(i).getID());
            for(int j=0;j<workflow_element.get(i).getCondition().size();j++)
            {
                System.out.println(       ((Branch) workflow_element.get(i).getCondition().get(j)).getExpression() + " ---> " + ((Branch) workflow_element.get(i).getCondition().get(j)).getNext()   );
            }
        }

        final String[] pointer = new String[]{startQuestion};
        System.out.println("First question is -----------> " + pointer[0]);

        final Map<String , String> map = new HashMap<String, String>();

        int number_of_question = ques_array.size();
        final String[] variable_name = new String[number_of_question];
        for (int i=0;i<ques_array.size();i++)
        {
            Base_Question temp_basequestion = gson.fromJson(gson.toJson(ques_array.get(i)),Base_Question.class);
            variable_name[i] = temp_basequestion.getQuestionID();
            map.put(temp_basequestion.getQuestionID(),temp_basequestion.getQuestionType());
        }
        for(String key: map.keySet()) System.out.println(key + " - " + map.get(key));

        System.out.println("The variable names are " + Arrays.toString(variable_name));

        final Answer[] answer_array = new Answer[number_of_question];
        List<Object> blanklist = new ArrayList<Object>();
        for(int i=0;i<number_of_question;i++) answer_array[i] = new Answer("blank","blank",blanklist,new DateTime());

        final ArrayList<Answer> tobesent = new ArrayList<Answer>();

        count = 0; //control index for answer_array

        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams llLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llLP);



        //show(pointer[0], campaign, ll, tag_list);
        final Displayer myDisplay = new Displayer("khoi", getApplicationContext());
        myDisplay.show(pointer[0], campaign_obj, ll, this, tag_list);

        final Intent intent2 = new Intent(this, Questionnaire_Done.class);

        currentValue = new TextView(this);
        currentValue.setVisibility(View.INVISIBLE);
        ll.addView(currentValue);

        start = new Button(this);
        start.setText("Start");
        start.setVisibility(View.INVISIBLE);
        ll.addView(start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startTime = System.currentTimeMillis();

                runner = new Thread() {
                    public void run() {
                        while (runner != null) {
                            try {
                                Thread.sleep(500);
                                Log.i("Noise", "Tock");
                            } catch (InterruptedException e) {
                            }
                            ;
                            mHandler.post(updater);
                        }
                    }
                };
                runner.start();
                Log.d("Noise", "start runner()");
            }
        });

        stop = new Button(this);
        stop.setText("Stop");
        stop.setVisibility(View.INVISIBLE);
        ll.addView(stop);

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stopTime = System.currentTimeMillis();
                long duration = stopTime - startTime;
                average = sum / sample_number;
                Toast.makeText(getBaseContext(), "Duration was " + duration + " milisec, sample_numer was " + sample_number + ", sum was " + sum + ", average was " + average + " , max was " + max, Toast.LENGTH_LONG).show();

                stopRecorder();
            }
        });

        final Button bt = new Button(this);
        bt.setText("Next Question");
        ll.addView(bt);
        bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                start.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.INVISIBLE);
                currentValue.setVisibility(View.INVISIBLE);

                List<Object> replies = new ArrayList<Object>();
                String tag = tag_list.get(0);
                String quesID = tag.substring(0, tag.indexOf("_"));

                for (int i = 0; i < tag_list.size(); i++)
                {
                    String full_tag = tag_list.get(i);
                    String short_tag = full_tag.substring(0, full_tag.indexOf("_"));
                    System.out.println("Full tag is " + full_tag + " short tag is " + short_tag);
                    v = ll.findViewWithTag(full_tag);

                    if (map.get(short_tag).equals(FreeTextSingle))
                    {
                        start.setVisibility(View.INVISIBLE);
                        stop.setVisibility(View.INVISIBLE);
                        currentValue.setVisibility(View.INVISIBLE);
                        EditText et = (EditText) v.findViewWithTag(full_tag);
                        System.out.println("Print from NEXT button " + et.getText().toString());
                        replies.add(et.getText().toString());
                    }
                    else if (map.get(short_tag).equals(FreeTextMulti))
                    {
                        start.setVisibility(View.INVISIBLE);
                        stop.setVisibility(View.INVISIBLE);
                        currentValue.setVisibility(View.INVISIBLE);
                        EditText et = (EditText) v.findViewWithTag(full_tag);
                        System.out.println("Print from NEXT button " + et.getText().toString());
                        replies.add(et.getText().toString());
                    }
                    else if (map.get(short_tag).equals(MultipleChoiceSingle))
                    {
                        start.setVisibility(View.INVISIBLE);
                        stop.setVisibility(View.INVISIBLE);
                        currentValue.setVisibility(View.INVISIBLE);
                        RadioGroup radioGroup = (RadioGroup) v.findViewWithTag(full_tag);

                        int selectedId = radioGroup.getCheckedRadioButtonId();
                        View myview = radioGroup.findViewById(selectedId);
                        int position = radioGroup.indexOfChild(myview);
                        RadioButton clickedRadioButton = (RadioButton) radioGroup.getChildAt(position);
                        String result = clickedRadioButton.getText().toString();

                        replies.add(result);

                        System.out.println(result);
                    }
                    else if (map.get(short_tag).equals(MultipleChoiceMulti)    )
                    {
                        start.setVisibility(View.INVISIBLE);
                        stop.setVisibility(View.INVISIBLE);
                        currentValue.setVisibility(View.INVISIBLE);
                        CheckBox checkbox = (CheckBox) v.findViewWithTag(full_tag);
                        if (checkbox.isChecked())
                        {
                            System.out.println(checkbox.getText().toString());
                            replies.add(checkbox.getText().toString());
                        }

                    }
                    else if (map.get(short_tag).equals(ContRange))
                    {
                        start.setVisibility(View.INVISIBLE);
                        stop.setVisibility(View.INVISIBLE);
                        currentValue.setVisibility(View.INVISIBLE);
                        SeekBar my_seekbar = (SeekBar) v.findViewWithTag(full_tag);
                        System.out.println("The max value of this seekbar is " + my_seekbar.getMax());

                        replies.add(String.valueOf(myDisplay.seekbar_value));
                        System.out.println("Value of seekbar is " + myDisplay.seekbar_value);

                    }
                    else if (map.get(short_tag).equals(AudioSensor))
                    {

                        TextView tv = (TextView) v.findViewWithTag(full_tag);
                        start.setVisibility(View.VISIBLE);
                        stop.setVisibility(View.VISIBLE);
                        currentValue.setVisibility(View.VISIBLE);
                        replies.add(min);
                        replies.add(average);
                        replies.add(max);
                        //start.setVisibility(View.INVISIBLE);
                        //stop.setVisibility(View.INVISIBLE);
                        System.out.println("AudioSensor: Lat is " + lat);
                        System.out.println("AudioSensor: Long is " + lon);
                        replies.add(lat);
                        replies.add(lon);

                    }
                }
                Answer ans = new Answer(campaign_obj.getID(),quesID, replies, new DateTime());
                System.out.println("Answer for question " + quesID + " has been collected");
                tobesent.add(ans);
                //ans.print();
                answer_array[count] = ans;
                count++;

                //for (int i = 0; i < answer_array.length; i++)
                //    answer_array[i].print();

                try {
                    System.out.println("After " + campaign_obj.getStartQuestion() + " next question should be " + flow(pointer[0],answer_array,workflow_element,variable_name));
                    pointer[0] = flow(pointer[0],answer_array,workflow_element,variable_name);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (pointer[0] != null)
                {
                    ll.removeAllViews();
                    tag_list = new ArrayList<String>();
                    //show(pointer[0], campaign, ll, tag_list);
                    myDisplay.show(pointer[0], campaign_obj, ll, getApplicationContext(), tag_list);
                    ll.addView(start);
                    ll.addView(currentValue);
                    ll.addView(stop);
                    ll.addView(bt);

                    if (map.get(pointer[0]).equals(AudioSensor))
                    {
                        start.setVisibility(View.VISIBLE);
                        stop.setVisibility(View.VISIBLE);
                        currentValue.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        start.setVisibility(View.INVISIBLE);
                        stop.setVisibility(View.INVISIBLE);
                        currentValue.setVisibility(View.INVISIBLE);
                    }

                }
                else
                {
                    System.out.println("QUESTIONNAIRE DONE !!!");
                    String tobesent_text = gson.toJson(tobesent);
                    intent2.putExtra(CONTENT, tobesent_text);
                    intent2.putExtra(CampaignID, campaign_obj.getID());
                    intent2.putExtra(CampaignConfig, message);
                    startActivity(intent2);
                }

            }
        });

        setContentView(ll);
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

    public void onResume()
    {
        super.onResume();
        startRecorder();
    }

    public void onPause()
    {
        super.onPause();
        stopRecorder();
    }

    public void startRecorder(){
        if (mRecorder == null)
        {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try
            {
                mRecorder.prepare();
            }catch (java.io.IOException ioe) {
                android.util.Log.e("[Monkey]", "IOException: " + android.util.Log.getStackTraceString(ioe));

            }catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
            }
            try
            {
                mRecorder.start();
            }catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
            }

            //mEMA = 0.0;
        }

    }

    public void stopRecorder() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void updateTv(){
        //mStatusView.setText(Double.toString((getAmplitudeEMA())) + " dB");
        //mStatusView.setText(Double.toString((getAmplitude())) + " dB");

        //tv.setText( "getMax " + Double.toString(getAmplitudeKhoi()) );

        //tv.setText("getMax " + value);
        double value = getAmplitudeKhoi();
        currentValue.setText("Current noise level is " + value + " dB");
        if (value >0) {
            sum = sum + value;
            if (value > max) max = value;
            if (value < min) min = value;
        }
        sample_number++;
        //mStatusView.setText(Double.toString((soundDb(getAmplitude()))) + " dB");
    }
    public double getAmplitudeKhoi() {
        if (mRecorder != null)
            return       20 * Math.log10(mRecorder.getMaxAmplitude());
        else
            return 0;

    }

    public static Object check(String expression, Answer[] array, String[] var_name) throws IllegalAccessException, InstantiationException, InvocationTargetException, InvocationTargetException {
        Object result = null;

        //for (int i=0;i<namelist.size();i++) System.out.println(namelist.get(i));
        org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();
        cx.setOptimizationLevel(-1);
        DateWrapFactory wrapFactory = new DateWrapFactory();
        //wrapFactory.setJavaPrimitiveWrap(false);

        cx.setWrapFactory(wrapFactory);

        try {
            Scriptable scope = cx.initStandardObjects();
            ScriptableObject.defineClass(scope, MyAnswerWrapper.class);

            MyAnswerWrapper[] scriptable_array = new MyAnswerWrapper[var_name.length];

            for (int i=0;i<var_name.length;i++)
            {
                String ID = var_name[i];
                List<Object> temp = null;
                for (int j=0;j< array.length;j++)
                {
                    if (array[j].getID().equals(ID)) temp = array[j].getValues();
                }
                Answer temp_answer = new Answer("anything",ID,temp,new DateTime());
                Object[] arguments = new Object[]{temp_answer};
                scriptable_array[i] = (MyAnswerWrapper) cx.newObject(scope, "MyWrapperAnswer",arguments);

                scope.put(ID, scope, scriptable_array[i]);
            }

            result =  cx.evaluateString(scope, expression, "anything", 1, null);
            //.out.println(result);
        } finally {
            org.mozilla.javascript.Context.exit();
        }

        System.out.println(expression + " --> " + result);
        return result;
    }

    public static String flow(String quesID, Answer[] answer_array, List<Workflow_Element> workflow_arraylist, String[] var_name) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        String result = null;
        for (int i=0;i< workflow_arraylist.size();i++)
        {
            Workflow_Element element;
            if (workflow_arraylist.get(i).getID().equals(quesID))
            {
                element = workflow_arraylist.get(i);
                ArrayList<Branch> branch_list = element.getCondition();
                if (branch_list.size() >1)
                {
                    String[] expression = new String[branch_list.size()];

                    for (int j=0;j<branch_list.size();j++)
                    {
                        expression[j] = branch_list.get(j).getExpression();
                        if ( ((boolean) check(expression[j],answer_array,var_name)) == true   )
                        result = branch_list.get(j).getNext();
                    }
                }
                else if (branch_list.size()==1)
                {
                    result = branch_list.get(0).getNext();
                }
            }
        }
        return result;
    }
}

class Displayer {
    String name;
    Context context;
    int seekbar_value;
    MainActivity mainActivity = new MainActivity();



    public Displayer(String name, android.content.Context context)    {
        this.name = name;
        this.context = context;
    }

    public void show(String ID, Campaign cam, LinearLayout layout, android.content.Context context, ArrayList tag_list) {
        /*String FreeTextSingle = "FreeTextSingle";
        String FreeTextMulti = "FreeTextMulti";
        String MultipleChoiceSingle = "MultipleChoiceSingle";
*/
        FreeTextSingle temp_FreeTextSingle = null;
        FreeTextMulti temp_FreeTextMulti = null;
        MultipleChoiceSingle temp_MultipleChoiceSingle = null;
        MultipleChoiceMulti temp_MultipleChoiceMulti = null;
        ContRange temp_ContRange = null;
        AudioSensor temp_AudioSensor = null;

        ArrayList<Base_Question> question_array = (ArrayList) cam.getQuestionArray();
        for (int i=0;i<question_array.size();i++)
        {
            Base_Question base_question = question_array.get(i);
            Base_Question temp_basequestion = base_question;

            if (temp_basequestion.getQuestionID().equals(ID))
            {
                if ((base_question instanceof FreeTextSingle))
                {
                    temp_FreeTextSingle = (FreeTextSingle) base_question;
                    display(temp_FreeTextSingle, layout, context, tag_list);
                }

                else if ( base_question instanceof FreeTextMulti)
                {
                    temp_FreeTextMulti = (FreeTextMulti) base_question;
                    display(temp_FreeTextMulti, layout, context, tag_list);
                }

                else if (base_question instanceof MultipleChoiceSingle)
                {
                    temp_MultipleChoiceSingle = (MultipleChoiceSingle) base_question;
                    display(temp_MultipleChoiceSingle, layout, context, tag_list);
                }

                else if (base_question instanceof MultipleChoiceMulti)
                {
                    temp_MultipleChoiceMulti = (MultipleChoiceMulti) base_question;
                    display(temp_MultipleChoiceMulti, layout, context, tag_list);
                }
                else if (base_question instanceof ContRange)
                {
                    temp_ContRange = (ContRange) base_question;
                    display(temp_ContRange, layout, context, tag_list);

                }
                else if (base_question instanceof AudioSensor)
                {
                    temp_AudioSensor = (AudioSensor) base_question;
                    display(temp_AudioSensor, layout, context, tag_list);

                }
            }
        }

    }

    public void common_display(Base_Question obj, final LinearLayout layout, android.content.Context context) {
        System.out.println(obj.getQuestionID() + " " + obj.getQuestionType() + " " + Arrays.toString(obj.getQuestionLabel()));
        String[] quesLabel = obj.getQuestionLabel();
        TextView tv = new TextView(context);
        tv.setText(quesLabel[0]);
        tv.setTextColor(Color.BLACK);
        layout.addView(tv);



        if (    (!quesLabel[1].isEmpty()) && (  quesLabel[0].endsWith("jpg") || quesLabel[0].endsWith("jpeg")  )    )
        {
            final String image_URL = quesLabel[1];
            System.out.println("Image URL is " + image_URL);
            //final Bitmap[] bmp = new Bitmap[1];
            final ImageView imageview = new ImageView(context);

            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    try {
                        InputStream in = new URL(image_URL).openStream();
                        /*bmp[0] =*/ return BitmapFactory.decodeStream(in);
                    } catch (Exception e) {  }
                    return null;
                }
                @Override
                protected void onPostExecute(Bitmap result) {
                    if (/*bmp[0]*/ result!= null)
                    {
                        imageview.setImageBitmap(result /* bmp[0]*/);

                        layout.addView(imageview);
                    }
                }
            }.execute();
        }
    }

    public void display(FreeTextSingle obj, final LinearLayout layout, android.content.Context context, ArrayList tag_list )    {
        common_display(obj, layout, context);

        final EditText edittext = new EditText(context);
        edittext.setText("");
        edittext.setTextColor(Color.BLACK);
        edittext.requestFocus();
        //edittext.setInputType(InputType.TYPE_CLASS_DATETIME);
        String tag = obj.getQuestionID() + "_";
        edittext.setTag(tag);
        tag_list.add(tag);
        layout.addView(edittext);
    }

    public void display(FreeTextMulti obj, final LinearLayout layout, android.content.Context context, ArrayList tag_list )    {
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
            String tag = obj.getQuestionID() + "_" + String.valueOf(i);
            edittext[i].setTag(tag);
            tag_list.add(tag);
            layout.addView(edittext[i]);
        }
    }

    public void display(MultipleChoiceSingle obj, final LinearLayout layout, android.content.Context context, ArrayList tag_list )    {
        common_display(obj, layout, context);

        String[] subcomponent = obj.getComponent();
        int size = subcomponent.length;
        RadioGroup radiogroup = new RadioGroup(context);
        String tag = obj.getQuestionID() + "_";
        radiogroup.setTag(tag);
        tag_list.add(tag);
        for (int i=0;i<size;i++)
        {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setText(subcomponent[i]);
            radioButton.setTextColor(Color.BLACK);
            radiogroup.addView(radioButton);
        }
        layout.addView(radiogroup);
    }

    public void display(MultipleChoiceMulti obj, final LinearLayout layout, android.content.Context context, ArrayList tag_list) {
        common_display(obj, layout, context);

        String[] subcomponent = obj.getComponent();
        int size = subcomponent.length;
        CheckBox[] checkbox_array = new CheckBox[size];
        for (int i=0;i<size;i++)
        {
            checkbox_array[i] = new CheckBox(context);
            checkbox_array[i].setText(subcomponent[i]);
            checkbox_array[i].setTextColor(Color.BLACK);
            String tag = obj.getQuestionID() + "_" + String.valueOf(i);
            checkbox_array[i].setTag(tag);
            tag_list.add(tag);
            layout.addView(checkbox_array[i]);
        }
    }

    public void display(ContRange obj, final LinearLayout layout, final android.content.Context context, ArrayList tag_list) {
        common_display(obj, layout, context);

        String[] subcomponent = obj.getComponent();
        final int min = Integer.parseInt(subcomponent[0]);
        int max = Integer.parseInt(subcomponent[1]);

        SeekBar my_seekbar = new SeekBar(context);
        my_seekbar.setMax(max - min);

        final TextView textView_of_seekBar = new TextView(context);
        textView_of_seekBar.setText("Covered: " + my_seekbar.getProgress() + "/" + my_seekbar.getMax());
        layout.addView(textView_of_seekBar);

        my_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = min;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue + min;
                seekbar_value = progressValue + min;
                //Toast.makeText(context, "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(context, "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView_of_seekBar.setText("Covered: " + progress + "/" + seekBar.getMax());
                Toast.makeText(context, "You have selected " + String.valueOf(progress), Toast.LENGTH_SHORT).show();
            }
        });

        String tag = obj.getQuestionID() + "_";
        my_seekbar.setTag(tag);
        tag_list.add(tag);
        layout.addView(my_seekbar);
    }

    public void display(AudioSensor obj, final LinearLayout layout, final android.content.Context context, ArrayList tag_list)
    {
        common_display(obj, layout, context);



        TextView tv = new TextView(context);
        tv.setText("This is a hidden TextView");
        String tag = obj.getQuestionID() + "_";
        tv.setTag(tag);
        tag_list.add(tag);
        layout.addView(tv);
    }

}


