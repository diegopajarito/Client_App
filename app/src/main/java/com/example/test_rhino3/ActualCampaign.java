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

import GeoC_AnswerObject.Answer;
import GeoC_AnswerObject.DateWrapFactory;
import GeoC_AnswerObject.MyAnswerWrapper;
import GeoC_QuestionHierarchy.BaseQuestion_Deserializer;
import GeoC_QuestionHierarchy.Base_Question;
import GeoC_QuestionHierarchy.Branch;
import GeoC_QuestionHierarchy.Branch_Deserializer;
import GeoC_QuestionHierarchy.Campaign;
import GeoC_QuestionHierarchy.Campaign_Deserializer;
import GeoC_QuestionHierarchy.DateTimeConverter;
import GeoC_QuestionHierarchy.FreeTextMulti;
import GeoC_QuestionHierarchy.FreeTextSingle;
import GeoC_QuestionHierarchy.MultipleChoiceSingle;
import GeoC_QuestionHierarchy.Workflow_Element;
import GeoC_QuestionHierarchy.Workflow_Element_Deserializer;

public class ActualCampaign extends AppCompatActivity {
    public static String FreeTextSingle = "FreeTextSingle";
    public static String FreeTextMulti = "FreeTextMulti";
    public static String MultipleChoiceSingle = "MultipleChoiceSingle";

    public static String CONTENT = "content from ArrayList that stores Answer objects";
    public static String CampaignID = "the ID of the current campaign";

    ArrayList<String> tag_list = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actual_campaign);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Branch.class, new Branch_Deserializer());
        gsonBuilder.registerTypeAdapter(Workflow_Element.class, new Workflow_Element_Deserializer());
        gsonBuilder.registerTypeAdapter(Base_Question.class, new BaseQuestion_Deserializer());
        gsonBuilder.registerTypeAdapter(Campaign.class, new Campaign_Deserializer());
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeConverter());
        final Gson gson = gsonBuilder.create();

        Intent intent = getIntent();
        final String message = intent.getStringExtra(MainActivity.KEY1);

        System.out.println(message);

        final Campaign campaign_obj = gson.fromJson(message, Campaign.class);

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
        for(int i=0;i<number_of_question;i++) answer_array[i] = new Answer("blank",blanklist,new DateTime());

        final ArrayList<Answer> tobesent = new ArrayList<Answer>();

        final int[] count = {0}; //control index for answer_array

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

        final Button bt = new Button(this);
        bt.setText("Next Question");
        ll.addView(bt);
        bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                List<Object> replies = new ArrayList<Object>();
                String tag = tag_list.get(0);
                String quesID = tag.substring(0, tag.indexOf("_"));

                for (int i = 0; i < tag_list.size(); i++)
                {
                    String full_tag = tag_list.get(i);
                    String short_tag = full_tag.substring(0, full_tag.indexOf("_"));
                    System.out.println("Full tag is " + full_tag + " short tag is " + short_tag);
                    v = ll.findViewWithTag(full_tag);

                    if (map.get(short_tag).equals(FreeTextSingle)) {
                        EditText et = (EditText) v.findViewWithTag(full_tag);
                        System.out.println("Print from NEXT button " + et.getText().toString());
                        replies.add(et.getText().toString());
                    } else if (map.get(short_tag).equals(FreeTextMulti)) {
                        EditText et = (EditText) v.findViewWithTag(full_tag);
                        System.out.println("Print from NEXT button " + et.getText().toString());
                        replies.add(et.getText().toString());
                    } else if (map.get(short_tag).equals(MultipleChoiceSingle)) {
                        RadioGroup radioGroup = (RadioGroup) v.findViewWithTag(full_tag);

                        int selectedId = radioGroup.getCheckedRadioButtonId();
                        View myview = radioGroup.findViewById(selectedId);
                        int position = radioGroup.indexOfChild(myview);
                        RadioButton clickedRadioButton = (RadioButton) radioGroup.getChildAt(position);
                        String result = clickedRadioButton.getText().toString();

                        replies.add(result);

                        System.out.println(result);
                    }
                }
                Answer ans = new Answer(quesID, replies, new DateTime());
                System.out.println("Answer for question " + quesID + " has been collected");
                tobesent.add(ans);
                //ans.print();
                answer_array[count[0]] = ans;
                count[0]++;

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
                    ll.addView(bt);
                }
                else
                {
                    System.out.println("QUESTIONNAIRE DONE !!!");
                    String tobesent_text = gson.toJson(tobesent);
                    intent2.putExtra(CONTENT, tobesent_text);
                    intent2.putExtra(CampaignID, campaign_obj.getID());
                    startActivity(intent2);
                }

            }
        });

        setContentView(ll);
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
                Answer temp_answer = new Answer(ID,temp,new DateTime());
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

    public Displayer(String name, android.content.Context context)    {
        this.name = name;
        this.context = context;
    }

    public void show(String ID, Campaign cam, LinearLayout layout, android.content.Context context, ArrayList tag_list) {
        String FreeTextSingle = "FreeTextSingle";
        String FreeTextMulti = "FreeTextMulti";
        String MultipleChoiceSingle = "MultipleChoiceSingle";

        FreeTextSingle temp_FreeTextSingle = null;
        FreeTextMulti temp_FreeTextMulti = null;
        MultipleChoiceSingle temp_MultipleChoiceSingle = null;

        Gson gson = new Gson();

        ArrayList<Base_Question> question_array = (ArrayList) cam.getQuestionArray();
        for (int i=0;i<question_array.size();i++)
        {
            Base_Question temp_basequestion = question_array.get(i);

            if (temp_basequestion.getQuestionID().equals(ID))
            {
                if (temp_basequestion.getQuestionType().equals(FreeTextSingle))
                {
                    temp_FreeTextSingle = gson.fromJson(gson.toJson(question_array.get(i)),FreeTextSingle.class);
                    display(temp_FreeTextSingle, layout, context, tag_list);
                }

                else if (temp_basequestion.getQuestionType().equals(FreeTextMulti))
                {
                    temp_FreeTextMulti = gson.fromJson(gson.toJson(question_array.get(i)), FreeTextMulti.class);
                    display(temp_FreeTextMulti, layout, context, tag_list);
                }

                else if (temp_basequestion.getQuestionType().equals(MultipleChoiceSingle))
                {
                    temp_MultipleChoiceSingle = gson.fromJson(gson.toJson(question_array.get(i)), MultipleChoiceSingle.class);
                    display(temp_MultipleChoiceSingle, layout, context, tag_list);
                }
            }
        }

    }

    public void common_display(Base_Question obj, final LinearLayout layout, android.content.Context context )    {
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
}


