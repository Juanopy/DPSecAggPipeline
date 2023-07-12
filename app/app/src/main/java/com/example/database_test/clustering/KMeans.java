package com.example.database_test.clustering;


import static com.example.database_test.MainActivity.*;
import static com.example.database_test.MainActivity.scanDisposable;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.database_test.MainActivity;
import com.example.database_test.R;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



public class KMeans {
    static final Double PRECISION = 0.0;

    /* K-Means++ implementation, initializes K centroids from data */
    static LinkedList<HashMap<String, Double>> kmeanspp(DataSet data, int K) {
        LinkedList<HashMap<String, Double>> centroids = new LinkedList<>();

        centroids.add(data.randomFromDataSet());

        for (int i = 1; i < K; i++) {
            centroids.add(data.calculateWeighedCentroid());
        }

        return centroids;
    }

    /* K-Means itself, it takes a dataset and a number K and adds class numbers
     * to records in the dataset */
    static void kmeans(DataSet data, int K) {
        // select K initial centroids
        LinkedList<HashMap<String, Double>> centroids = kmeanspp(data, K);

        // initialize Sum of Squared Errors to max, we'll lower it at each iteration
        Double SSE = Double.MAX_VALUE;

        while (true) {

            // assign observations to centroids

            LinkedList<DataSet.Record> records = data.getRecords();

            // for each record
            for (DataSet.Record record : records) {
                Double minDist = Double.MAX_VALUE;
                // find the centroid at a minimum distance from it and add the record to its cluster
                for (int i = 0; i < centroids.size(); i++) {
                    Double dist = DataSet.euclideanDistance(centroids.get(i), record.getRecord());
                    if (dist < minDist) {
                        minDist = dist;
                        record.setClusterNo(i);
                    }
                }

            }

            // recompute centroids according to new cluster assignments
            centroids = data.recomputeCentroids(K);

            // exit condition, SSE changed less than PRECISION parameter
            Double newSSE = data.calculateTotalSSE(centroids);
            if (SSE - newSSE <= PRECISION) {
                break;
            }
            SSE = newSSE;
        }
    }




    public static void Kmeansmain(MainActivity context) throws IOException {
            // read data
            DataSet data = new DataSet(context.getAssets().open("sample.csv"));

            // remove prior classification attr if it exists (input any irrelevant attributes)
            data.removeAttr("Class");

            // cluster
            kmeans(data, 3);

            // output into a csv
            data.createCsvOutput(context.getCacheDir().getPath() + "/" + "sampleClustered.csv");


            ByteArrayOutputStream wr = new ByteArrayOutputStream();
            OutputStream tmp = Base64.getEncoder().wrap(wr);
            data.createCsvOutput(tmp);
            tmp.close();
            if (scanDisposable == null) {
                scanBleDevices();
                com.example.database_test.audioclassify.Utils.executeLater(10 * 1000L,() -> {
                    try {
                        sendData(context);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
            }


    }

    //send data to server
    public static void sendData(MainActivity context) throws IOException {

        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        String send_data = "Android ID:" + android_id + ",Label:" + lastlabel + ",Prob:" + lastlabelprob + ",Devices:" + devices;


        System.out.println("Sending data: " + send_data);
        MainActivity.snackbar_show("Data sent to server");
        //make send_data base64 encoded
        byte[] send_data_bytes = send_data.getBytes(StandardCharsets.UTF_8);
        String send_data_base64 = Base64.getEncoder().encodeToString(send_data_bytes);


        String url = "https://127.0.0.1:41070/privee_post";
        StringRequest sr = new StringRequest(Request.Method.POST, url,
                response -> System.out.println("success! response: " + response.toString()),
                error -> System.out.println( "error: " + error.toString())) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("payload", send_data_base64.toString());
                return params;
            }
        };
        requestQueue.add(sr);
        try {
            scanDisposable.dispose();
        }   catch (Exception e) {
            System.out.println("Error: " + e);
        }


    }


}
