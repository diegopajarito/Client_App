package com.example.test_rhino3;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    MyString def = new MyString("");
    public static String CONSTANT1 = "constant1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String My_URL = "http://192.168.1.138:8880/get_test2";
        new HttpAsyncGET().execute(My_URL);

        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams llLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llLP);

        Button bt = new Button(this);
        bt.setText("Go to Campaign");
        ll.addView(bt);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!def.getString().equals(""))
                {
                    Intent intent = new Intent(getApplicationContext(), ActualCampaign.class);
                    intent.putExtra(CONSTANT1, def.getString());
                    startActivity(intent);
                }
            }
        });

        setContentView(ll);
    }

    private class HttpAsyncGET extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            //a.setNumber(GET2(urls[0]));
            return GET(urls[0]);

        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Campaign downloaded!", Toast.LENGTH_LONG).show();
            //etResponse.setText(result);
            //abc.setNumber(Integer.parseInt(result));
            def.setString(result);
            //System.out.println(String.valueOf(abc.getNumber()));
            System.out.println(def.getString());

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
        String result = "";
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
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    public class MyString{
        String str = "blank";

        public MyString(String a){
            this.str = a;
        }
        public String getString(){
            return str;
        }
        public void setString(String a){
            str = a;
        }
    }
}

/*
class Base_Question {
    protected String quesType;
    protected String quesID;
    protected String[] quesLabel;

    //Constructor
    public Base_Question (String Type, String ID, String[] Label) {
        this.quesType = Type;
        this.quesID = ID;
        this.quesLabel = Label;
    }

    @Override
    public String toString() {
        return String.format(" Type = %s, ID = %s, Label = %s", quesType, quesID, Arrays.toString(quesLabel));
    }

    public String getQuestionType(){
        return this.quesType;
    }

    public String getQuestionID(){
        return this.quesID;
    }

    public String[] getQuestionLabel(){
        return this.quesLabel;
    }

}

class Branch {
    String expression;
    String next;

    public Branch(String expr, String nextID )
    {
        this.expression = expr;
        this.next = nextID;
    }
    public String getExpression() {return this.expression;}
    public String getNext() {return this.next;}
}

class Campaign {
    String Campaign_ID;
    String Campaign_Description;
    String startQuestion;
    ArrayList Question_Array;
    ArrayList workflow;

    public Campaign(String ID, String description, String start, ArrayList quesArray, ArrayList workflow)
    {
        this.Campaign_ID = ID;
        this.Campaign_Description = description;
        this.startQuestion = start;
        this.Question_Array = quesArray;
        this.workflow = workflow;
    }

    public String getID() {return this.Campaign_ID;}
    public String getDescription() {return this.Campaign_Description;}
    public String getStartQuestion() {return this.startQuestion;}
    public ArrayList getQuestionArray() {return this.Question_Array;}
    public ArrayList getWorkflow() {return this.workflow;}

    public Base_Question getQuestionByID(String ID){
        Base_Question result = null;
        for (int i=0;i<Question_Array.size();i++)
        {
            if (    ((Base_Question) Question_Array.get(i)).getQuestionID().equals(ID) )
                result = (Base_Question) Question_Array.get(i);
        }
        return result;

    }

}

class FreeTextMulti extends Base_Question {
    String[] subcomponent;

    public FreeTextMulti(String Type, String ID, String[] Label, String[] subcomponent) {
        super(Type, ID, Label);
        this.subcomponent = subcomponent;
    }

    @Override
    public String toString() {
        return String.format(" Type = %s, ID = %s, Label = %s, Subcomponent = %s", quesType, quesID, Arrays.toString(quesLabel), Arrays.toString(subcomponent));
    }

    public String[] getComponent() {
        return this.subcomponent;
    }

}

class FreeTextSingle extends Base_Question{
    public FreeTextSingle(String Type, String ID, String[] Label) {
        super(Type, ID, Label);
    }

    @Override
    public String toString() {
        return String.format(" Type = %s, ID = %s, Label = %s", quesType, quesID, Arrays.toString(quesLabel));
    }

}

class MultipleChoiceSingle extends Base_Question {
    String[] subcomponent;

    public MultipleChoiceSingle(String Type, String ID, String[] Label, String[] subcomponent) {
        super(Type, ID, Label);
        this.subcomponent = subcomponent;
    }

    @Override
    public String toString() {
        return String.format(" Type = %s, ID = %s, Label = %s, Subcomponent = %s", quesType, quesID, Arrays.toString(quesLabel), Arrays.toString(subcomponent));
    }

    public String[] getComponent() {
        return this.subcomponent;
    }

}

class Workflow_Element {
    String QuesID;
    ArrayList Condition;

    public Workflow_Element (String quesID, ArrayList array)
    {
        this.QuesID = quesID;
        this.Condition = array;
    }

    public String getID() {	return this.QuesID;	}
    public ArrayList getCondition() { return this.Condition; }
}
 */