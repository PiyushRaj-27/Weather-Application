package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    TextView temperature;
    TextView wind;
    TextView humidity;
    TextView condition;
    TextView precipitation;
    TextView pm;
    TextView visi;
    Button button1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("onCreate1", "onCreate: done! ");
        button1 = findViewById(R.id.refresh);
        LinearLayout l1 = findViewById(R.id.mainlayout);
        AnimationDrawable anim = (AnimationDrawable) l1.getBackground();
        anim.setEnterFadeDuration(2000);
        anim.setExitFadeDuration(2000);
        anim.start();

        weatherFetch fetch1 = new weatherFetch();
        fetch1.execute();
        new forcastFetch().execute();
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new weatherFetch().execute();
                new forcastFetch().execute();
            }
        });

    }

    private class forcastFetch extends AsyncTask<Void, Void, HashMap<Integer, String>>{

        @Override
        protected HashMap<Integer, String> doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            HashMap<Integer, String> hourlyReport = new HashMap<Integer, String>();
            String weatherRes;
            Request request = new Request.Builder()
                    .url("http://api.weatherapi.com/v1/forecast.json?key=222acf3bdaad4a28ae645014230107&q=Patna&days=1&aqi=yes&alerts=yes")
                    .get()
                    .build();

            try {
                Response response = client.newCall(request).execute();
                weatherRes = response.body().string();
                JSONObject obj = new JSONObject(weatherRes);
                JSONObject forecast = new JSONObject(obj.getJSONObject("forecast").getJSONArray("forecastday").get(0).toString());
                JSONArray hourlyForecast = forecast.getJSONArray("hour");

                for(int i = 0; i< hourlyForecast.length(); i++){
                    hourlyReport.put(i, hourlyForecast.getJSONObject(i).getString("temp_c"));
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            Log.i("forecast", hourlyReport.toString());
            return hourlyReport;
        }

        @Override
        protected void onPostExecute(HashMap<Integer, String> integerStringHashMap) {
            super.onPostExecute(integerStringHashMap);
            LineChart chart = (LineChart) findViewById(R.id.chart);
            List<Entry> entries = new ArrayList<Entry>();
            for(int i = 0; i<24; i++){
                entries.add(new Entry(i, Float.parseFloat(integerStringHashMap.get(i))));
            }

            //Line Data set
            LineDataSet dataSet = new LineDataSet(entries, "Label");
            dataSet.setColor(getResources().getColor(R.color.white));
            dataSet.setLineWidth(1f);

            //Line Data settings
            LineData lineData = new LineData(dataSet);
            lineData.setValueTextColor(getResources().getColor(R.color.white));
            lineData.setHighlightEnabled(false);
            chart.setData(lineData);

            //Description details
            Description desc = new Description();
            desc.setText("24hr temperature Forecast");
            desc.setTextColor(getResources().getColor(R.color.white));
            desc.setEnabled(false);
            chart.setDescription(desc);

            //chart Settings

            chart.getLegend().setEnabled(false);
            chart.setDrawGridBackground(false);

            chart.setPinchZoom(true);
            chart.getXAxis().setTextColor(getResources().getColor(R.color.white));
            chart.getXAxis().setDrawAxisLine(false);
            chart.getXAxis().setDrawGridLines(false);
            chart.getAxis(YAxis.AxisDependency.RIGHT).setEnabled(false);
            chart.getAxis(YAxis.AxisDependency.LEFT).setTextColor(getResources().getColor(R.color.white));
            chart.getAxis(YAxis.AxisDependency.LEFT).setDrawAxisLine(false);
            chart.getAxis(YAxis.AxisDependency.LEFT).setDrawGridLines(false);
            chart.getAxis(YAxis.AxisDependency.LEFT).setEnabled(false);
            chart.invalidate();
        }
    }

    private class weatherFetch extends AsyncTask<Void, Void, HashMap<String, String>>{

        @Override
        protected HashMap<String, String> doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            HashMap<String, String> report = new HashMap<String, String>();
            String weatherRes;
            Request request = new Request.Builder()
                    .url("http://api.weatherapi.com/v1/forecast.json?key=222acf3bdaad4a28ae645014230107&q=Patna&days=1&aqi=yes&alerts=yes")
                    .get()
                    .build();

            try {
                Response response = client.newCall(request).execute();
                weatherRes = response.body().string();
                JSONObject obj = new JSONObject(weatherRes);
                report.put("temp",obj.getJSONObject("current").getString("temp_c"));
                report.put("is_day", obj.getJSONObject("current").getString("is_day"));
                report.put("condition", obj.getJSONObject("current").getJSONObject("condition").getString("text"));
                report.put("wind", obj.getJSONObject("current").getString("wind_kph"));
                report.put("precip_mm", obj.getJSONObject("current").getString("precip_mm"));
                report.put("humidity",obj.getJSONObject("current").getString("humidity"));
                report.put("imgURL", obj.getJSONObject("current").getJSONObject("condition").getString("icon"));
                report.put("visibility", obj.getJSONObject("current").getString("vis_km"));
                report.put("pm", (obj.getJSONObject("current").getJSONObject("air_quality").getString("pm2_5").substring(0,obj.getJSONObject("current").getJSONObject("air_quality").getString("pm2_5").length()/2)));
                Log.i("Result1", weatherRes);


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return report;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> stringStringHashMap) {
            temperature = findViewById(R.id.temperature);
            wind = findViewById(R.id.wind);
            humidity = findViewById(R.id.humidity);
            condition = findViewById(R.id.condition);
            precipitation = findViewById(R.id.precip);
            pm = findViewById(R.id.pm);
            visi = findViewById(R.id.visibility);

            temperature.setText(stringStringHashMap.get("temp").concat("\u2103"));
            wind.setText(stringStringHashMap.get("wind").concat(" kph"));
            humidity.setText(stringStringHashMap.get("humidity"));
            condition.setText(stringStringHashMap.get("condition"));
            precipitation.setText(stringStringHashMap.get("precip_mm").concat(" mm"));
            pm.setText(stringStringHashMap.get("pm"));
            visi.setText(stringStringHashMap.get("visibility").concat(" km"));

            new DownloadImageTask((ImageView) findViewById(R.id.primary))
                    .execute("https:".concat(stringStringHashMap.get("imgURL")));

        }
    }

    // new change
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}