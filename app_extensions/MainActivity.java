package com.example.bachelormarcelheiselsecureaggregation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.bachelormarcelheiselsecureaggregation.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {




    // Used to load the 'bachelormarcelheiselsecureaggregation' library on application startup.
    static {
        System.loadLibrary("bachelormarcelheiselsecureaggregation");
    }

    private TextView mMiddleTextView;
    private TextView mResponseTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMiddleTextView = findViewById(R.id.sample_text);
        mResponseTextView = findViewById(R.id.response_text_view);

        String url = "http://192.168.178.51:8081?Step0";

        /*
        String step2Message = stringFromJNI("NULL");
        url+= step2Message;*/



        if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_SEND)) {
            handleSendIntent(getIntent());
        }




        //new GetDataTask().execute(url);


    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND)) {
            handleSendIntent(intent);
        }
    }

    private void handleSendIntent(Intent intent) {
        // Get the shared text
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        personalXString = sharedText;
        // do something with the shared text, you can show it to the user or save it to the storage
        // for example
        mMiddleTextView.setText(sharedText);
        String url = "http://127.0.0.1:8888?Step0";
        new GetDataTask().execute(url);
    }


    // * A native method that is implemented by the 'bachelormarcelheiselsecureaggregation' native library,
     //* which is packaged with this application.

    public native String stringFromJNI(String argument);

    //Debug variables
    int sleepTime = 5000;


    //Public Parameters
    int l = 16;


    public String id;
    boolean step0Finished = false;
    public String t;
    boolean getTFinished = false;
    boolean step2Finished = false;
    String mySecretKey1;
    String mySecretKey2;
    boolean getNeighbours = false;
    List<String> myNeighbours = new ArrayList<>();
    String myNeighboursString;
    boolean getPublicKeys = false;
    Map<String, String> pk1Map = new HashMap<String, String>();
    Map<String, String> pk2Map = new HashMap<String, String>();
    String bBase64;
    boolean step3Finished = false;
    Map<String, String> shareHbMap = new HashMap<String, String>();
    Map<String, String> shareHsMap = new HashMap<String, String>();
    boolean step4CheckFinished = false;
    boolean getA1Finished = false;
    List<String> a1 = new ArrayList<>();
    String a1String;
    boolean step5Finished = false;
    boolean step6CheckFinished = false;
    List<String> r1 = new ArrayList<>();
    List<String> r2 = new ArrayList<>();
    boolean checkStep8 = false;
    String personalXString;

class GetDataTask extends AsyncTask<Object, Void, List<String>> {
    private TextView mTextViewResult;
    @Override
    protected List<String> doInBackground(Object... params) {
        String urlString = (String) params[0];
        // Erstellen Sie eine neue Liste, um die Antwort zu speichern
        List<String> responseList = new ArrayList<>();


        //String urlString = "https://www.tutorialspoint.com/de/xml/xml_comments.htm";

        try {
            // Erstellen Sie eine neue URL-Verbindung
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(urlString).openConnection();

            // Legen Sie den HTTP-Methode auf GET fest
            urlConnection.setRequestMethod("GET");

            // Erhalten Sie den HTTP-Status-Code
            int responseCode = urlConnection.getResponseCode();

            // Wenn der Aufruf erfolgreich war (HTTP-Status-Code 200), lesen Sie die Antwort ein
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Fügen Sie jede Zeile der Antwort der Liste hinzu
                    responseList.add(line);
                }
                reader.close();
            }

            // Schließen Sie die Verbindung
            urlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseList;
    }

    @Override
    protected void onPostExecute(List<String> responseList) {
        if(!step0Finished){

            step0Finished = true;
            StringBuilder sb = new StringBuilder();
            for (String s : responseList) {
                sb.append(s);
            }
            Log.d("initial", sb.toString());
            id = sb.toString();
            mResponseTextView.setText(id);
            String url = "http://192.168.178.51:8081?GetT";



            new GetDataTask().execute(url);
        }
        else if(!getTFinished){

            getTFinished = true;
            StringBuilder sb = new StringBuilder();
            for (String s : responseList) {
                sb.append(s);
            }
            t = sb.toString();
            Log.d("t lautet:", t);
            String step2Message = stringFromJNI("step2");
            Log.d("NotUpdatedStep2", step2Message);
            String[] array = step2Message.split(";");
            mySecretKey1 = array[0];
            Log.d("SecretKey", mySecretKey1);
            mySecretKey2 = array[1];
            Log.d("SecretKey2", mySecretKey2);
            step2Message = array[2] + ";" + array[3];
            Log.d("UpdatedStep2", step2Message);
            String url = "http://192.168.178.51:8081?Step2;" + id + ";" + step2Message;
            mMiddleTextView.setText(url);
            new GetDataTask().execute(url);
        }
        else if(!step2Finished){

            step2Finished = true;
            String url = "http://192.168.178.51:8081?GetNeighbours;" + id;

            /*
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            new GetDataTask().execute(url);
        }
        else if(!getNeighbours){

            getNeighbours = true;
            StringBuilder sb = new StringBuilder();
            for (String s : responseList) {
                sb.append(s);
            }
            myNeighboursString = sb.toString();
            Log.d("MyNeighboursString", myNeighboursString);
            String[] array = sb.toString().split(";");
            myNeighbours.addAll(Arrays.asList(array));
            for(String a: myNeighbours){
                Log.d("Neighbour:", a);
            }
            mMiddleTextView.setText(sb.toString());
            String url = "http://192.168.178.51:8081?GetPublicKeys;" + id;


            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new GetDataTask().execute(url);
        }
        else if(!getPublicKeys){

            getPublicKeys = true;
            StringBuilder sb = new StringBuilder();
            for (String s : responseList) {
                sb.append(s);
            }
            mMiddleTextView.setText(sb.toString());
            String[] array = sb.toString().split(";");
            List<String> tempList = new ArrayList<String>();
            tempList.addAll(Arrays.asList(array));
            while(!tempList.isEmpty()){
                String client = tempList.get(0);
                tempList.remove(0);
                String pk1 = tempList.get(0);
                tempList.remove(0);
                String pk2 = tempList.get(0);
                tempList.remove(0);
                pk1Map.put(client, pk1);
                pk2Map.put(client, pk2);
                Log.d("Id;neighbour;pk1", id + ";" + client + ";" + pk1);
                Log.d("Id;neighbour;pk2", id + ";" + client + ";" + pk2);
            }
            String publicKeys2String = "";
            for(String temp1: myNeighbours){
                String pk2 = pk2Map.get(temp1);
                publicKeys2String += temp1 + ";" + pk2 + ";";
            }
            Log.d("PublicKeys2String", publicKeys2String);
            Log.d("CallStep3JNI", "step3;" + t + ";" + myNeighbours.size() + ";" + mySecretKey1 + ";" + mySecretKey2 + ";" + id + ";" + myNeighboursString + publicKeys2String);
            String step3Message = stringFromJNI("step3;" + t + ";" + myNeighbours.size() + ";" + mySecretKey1 + ";" + mySecretKey2 + ";" + id + ";" + myNeighboursString + publicKeys2String);
            Log.d("Der alte step3String", step3Message);
            String[] array1 = step3Message.split(";");
            List<String> tempList1 = new ArrayList<>();
            tempList1.addAll(Arrays.asList(array1));
            bBase64 = tempList1.get(0);
            Log.d("bBase64", bBase64);
            tempList1.remove(0);
            String tempString = "";
            for(String s: tempList1){
                tempString += s + ";";
            }
             Log.d("Der recoverte step3String", tempString);
            step3Message = tempString;

            Log.d("t;myNeighbours.size()", t + ";" + myNeighbours.size());
            mMiddleTextView.setText(step3Message);


            //Debug only
            /*
            if(id.equals("2")){
                Log.d("Finished", "true");
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/

            String url = "http://192.168.178.51:8081?Step3;" + id + ";" + step3Message;
            Log.d("Step3 zum Server", url);
            new GetDataTask().execute(url);
        }
        else if(!step3Finished){

            step3Finished = true;
            String url = "http://192.168.178.51:8081?Step4Check;" + id;
            mMiddleTextView.setText("Asking Server if enough users finished step3...");
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }



            new GetDataTask().execute(url);
        }
        else if(!step4CheckFinished){



            //Debug only
            if(id.equals("3")){
                Log.d("Finished", "true");
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(id.equals("4")){
                Log.d("Finished", "true");
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(id.equals("5")){
                Log.d("Finished", "true");
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }









            step4CheckFinished = true;
            StringBuilder sb = new StringBuilder();
            for (String s : responseList) {
                sb.append(s);
            }
            String[] array = sb.toString().split(";");
            List<String> tempList = new ArrayList<String>();
            tempList.addAll(Arrays.asList(array));




            Log.d("Step4Check", sb.toString());
            if(tempList.get(0).equals("false")){
                mMiddleTextView.setText("Not enough users finished step3. Aborting...");
                try {
                    Thread.sleep(10000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else if(tempList.get(0).equals("true")){
                tempList.remove(0);

                String encodedMessage = "";
                for(String s: tempList){
                    encodedMessage += s + ";";
                }
                String publicKeys2String = "";
                for(String temp1: myNeighbours){
                    String pk2 = pk2Map.get(temp1);
                    publicKeys2String += temp1 + ";" + pk2 + ";";
                }


                Log.d("Encoded Step3Erhalten", encodedMessage);
                String decodedStep4 = stringFromJNI("DecodeStep4;" + id + ";" + myNeighbours.size() + ";" + mySecretKey2 + ";" + encodedMessage + "BeginPk2s;" +  publicKeys2String);
                Log.d("Rufe step4Decode mit auf", ("DecodeStep4;" + mySecretKey2 + ";" + encodedMessage + "BeginPk2s;" +  publicKeys2String));
                Log.d("BITTEGEH", decodedStep4);
                String[] array1 = decodedStep4.split(";");
                List<String> tempList1 = new ArrayList<String>();
                tempList1.addAll(Arrays.asList(array1));
                tempList = tempList1;


                for(String s : tempList){
                    Log.d("Die neue Liste", s);
                }


//Step4Check: true;5;0;5;AAAAA0OesiPKb0ndnrvXh59TXkC5MJJJYGlu1DN7rCKdvBNYErQD8IWzxkJLbbgqrQec0tQK8BvXFLmVmqfvLm3zakPXcCC1;AAAAA8xPwYialKJFJOSZN26Adx616LWQy5cIhkCZ4RzvzyH3J1RftVFMp9kHYutTUvEP5w==;5;1;5;AAAAA0R+43mkgqtSDG2Pzq5EzbAKRRSmXJaBJNJNwhKp6viI18cWwZ1VsIFgTYvmXi0KB/NZjnmvxDjHxU4NNOg6QKt9y/DL;AAAAA9CgC52QjKju+UlZK/fv60QffdQXLCZfSjtA6SRdyEH2v3S0eiaFnC53rAm6KVSs7A==;5;2;5;AAAAAw56UD9pW2tLbF8S87vO/n8ai8wLoRh/uY2R1/5oXWBLy4/5I+amh7D+NhOy4GGB1mM9aPjO6j6pDzAH1QGuhgBdbyIx;AAAAA+bih1qLEmBxWjFLoGedPg/p1I6GOtJaMwFGP5XycVm327kG0qhy/CjmxnihQ9Ahrg==;5;4;5;AAAAA/rIqg5woBEogslMYjtZoRR7ZVSqgeKtCBsgJL4nB19BY53yRECz/GANpPFRytPsXQRKnBUBLIxXBtasUq05SH5HcKQn;AAAAAwulFkWOdrbigaEGwaVHeh4CkTQFMQkClPO+ASrbZfIGErkV/Ca4omzaDWLv9kGYvQ==;5;6;5;AAAABVlx5W/5xyeJRL7QnYxmzfEFYD/InOEDJZyDFlrddTUAa2LwgQ115yOfTFDDqTVY1E+pYoKC1d1ieoHUi09F2u+7YdFZ;AAAABWbcUSIJHkBeRYWkxOq80M4XX2QGevdYL+RqaKEA6wExsV05u2nCXXl7ymm9YZcJWg==;5;7;5;AAAABUOVkkaiLWUMdEZ3b+xOqosADbjjDPxYdmssi4vZWWt4/dm0kcLR5/iO/7ZuqoVtdii5PL3KVjO8Fg0umiTgkhsYvs0J;AAAABcOlAkcw3qXlanMTRUkZSAay6Lzs4itxN+sCb3uvGqLztjKoEx7IyjMuzwUyry/teA==;5;8;5;AAAABXXaSk6zZA6okD7c8ztp1LXwed9Z3wqOP4dXm1Neh3FwJgUmoKcTl1H1mvB2OQNbpN1Y/dJIhwxSMcEYN2i0qS3pPIOa;AAAABSmajAkzB6GjFsUnbsqIByrEKwHE1DGz8B3jheo/DTHXF6R4h+tyuxb+nyV+SuFu/g==;5;9;5;AAAABbAui+a92svZ0gzuYgiyPD9TwPSbRzbHbYdG7BJFfER0gDh/vSOj7kko1vZEHykBrbxEEirinrAGwRzUPIQW77S7d1rg;AAAABTaGpv+kh+O7BdRhlPRHXtuNbbmBzdAiL6PWo159I3kjm8KqD8gaO4TiM4yZuEqcbQ==;
                while(!tempList.isEmpty()){
                    String client = tempList.get(0);
                    tempList.remove(0);
                    tempList.remove(0);
                    String shareHb = tempList.get(0);
                    tempList.remove(0);
                    String shareHs = tempList.get(0);
                    tempList.remove(0);
                    shareHbMap.put(client, shareHb);
                    Log.d("Added to shareHbMap", client + ":" + shareHb);
                    shareHsMap.put(client, shareHs);
                    Log.d("Added to shareHsMap", client + ":" + shareHs);
                }

                mMiddleTextView.setText("Enough users finished step3. Continuing...");
            }












            else{
                mMiddleTextView.setText("This should never happen. Aborting...");
                finishAffinity();
            }
            String url = "http://192.168.178.51:8081?GetA1;" + id + ";";
            mMiddleTextView.setText("Getting the list A1 from Server...");
            new GetDataTask().execute(url);
        }
        else if(!getA1Finished){

            getA1Finished = true;
            StringBuilder sb = new StringBuilder();
            for (String s : responseList) {
                sb.append(s);
            }
            mMiddleTextView.setText(sb.toString());



            String[] array = sb.toString().split(";");
            List<String> tempList = new ArrayList<String>();
            tempList.addAll(Arrays.asList(array));
            a1 = tempList;
            for(String s: a1){
                Log.d("Client in A1", s);
            }

            a1String = sb.toString();

            String idPk1sString = myNeighbours.size() + ";";
            for(String s: myNeighbours){
                idPk1sString += s + ";" + pk1Map.get(s) + ";";
            }
            Log.d("idPk1sString", idPk1sString);

            Log.d("Size of a1", String.valueOf(a1.size()));
            String step5Message = stringFromJNI("step5;" + l + ";" + personalXString + ";" +  a1.size() + ";" + a1String + idPk1sString + mySecretKey1 + ";" + id + ";" + bBase64 + ";");
            Log.d("id", id);
            Log.d("bBase64", bBase64);
            mMiddleTextView.setText(step5Message);
            Log.d("Step5Message", step5Message);
            String url = "http://192.168.178.51:8081?Step5;" + id + ";" + step5Message;
            new GetDataTask().execute(url);
        }
        else if(!step5Finished){
            /*
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            step5Finished = true;
            String url = "http://192.168.178.51:8081?Step6Check;" + id;
            mMiddleTextView.setText("Asking Server if enough users finished step5...");
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }



            new GetDataTask().execute(url);

        }
        else if(!step6CheckFinished){
            /*
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            step6CheckFinished = true;
            StringBuilder sb = new StringBuilder();
            for (String s : responseList) {
                sb.append(s);
            }
            Log.d("ResponseToCheckStep6", sb.toString());
            if(sb.toString().equals("false")) {
                mMiddleTextView.setText("Not enough users finished step5. Aborting...");
            }
            else{
                String[] array = sb.toString().split(";");
                List<String> tempList = new ArrayList<String>();
                tempList.addAll(Arrays.asList(array));
                tempList.remove(0);
                tempList.remove(0);
                while(!tempList.get(0).equals("R2")){
                    if(!tempList.get(0).equals(id)){
                        r1.add(tempList.get(0));
                        Log.d("Added to r1", tempList.get(0));
                    }
                    tempList.remove(0);
                }
                tempList.remove(0);
                while(!tempList.isEmpty()){
                    if(!tempList.get(0).equals(id)){
                        r2.add(tempList.get(0));
                        Log.d("Added to r2", tempList.get(0));
                    }
                    tempList.remove(0);
                }


                String step7Message;
                String requiredSharesHb = "Hb";
                String requiredSharesHs = ";Hs";
                for(String r1Iterator: r1){
                    requiredSharesHb += ";" + r1Iterator + ";" + shareHbMap.get(r1Iterator);
                }
                for(String r2Iterator: r2){
                    requiredSharesHs += ";" + r2Iterator + ";" + shareHsMap.get(r2Iterator);
                }
                step7Message = requiredSharesHb + requiredSharesHs;
                Log.d("Step7Message", step7Message);
                mMiddleTextView.setText(sb.toString());
                String url = "http://192.168.178.51:8081?Step7;" + id + ";" + step7Message;
                new GetDataTask().execute(url);
            }




        }

        //ONLY FOR DEBUGGING
        else if(!checkStep8){
            if(id.equals("9")){
                /*
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            checkStep8 = true;
            String url = "http://192.168.178.51:8081?Step8Check";

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            new GetDataTask().execute(url);
            }
        }


        else{




            StringBuilder sb = new StringBuilder();
            for (String s : responseList) {
                sb.append(s);
            }
            Log.d("ResponseToCheckStep8", sb.toString());
            mMiddleTextView.setText(sb.toString());
            }





    }







    }
}




