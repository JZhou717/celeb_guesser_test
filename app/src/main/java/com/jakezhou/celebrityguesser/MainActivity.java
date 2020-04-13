package com.jakezhou.celebrityguesser;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;

/**
 * A Test App from Rob Percival's "Learn Android App Development with Android 7 Nougt" Udemy Course
 * Rob's instructions are used as a guideline for my self learning
 * Includes new topics for me such as connecting to the internet via Android
 * Displaying images from urls
 * AsyncTask, Regex, etc
 */

public class MainActivity extends AppCompatActivity {

    //View objects
    TextView scoreboard;
    ImageView celebImage;
    Button option1;
    Button option2;
    Button option3;
    Button option4;
    //Custom Button Click Listener
    MyListener onClick;
    //Celeb image links and names along with String of website that they came from
    String imdb;
    List<String> urls;
    List<String> names;
    List<String> allNames = new ArrayList<>();
    //Stores the index and name of the current celebrity shown
    int curr = -1;
    String name;
    //Keeps track of user score
    int correct = 0;
    int total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onClick = new MyListener();

        scoreboard = findViewById(R.id.scoreBoard);
        celebImage = findViewById(R.id.celebImage);
        option1 = findViewById(R.id.option1);
        option1.setOnClickListener(onClick);
        option2 = findViewById(R.id.option2);
        option2.setOnClickListener(onClick);
        option3 = findViewById(R.id.option3);
        option3.setOnClickListener(onClick);
        option4 = findViewById(R.id.option4);
        option4.setOnClickListener(onClick);

        //Must add internet to permissions in manifest
        DownloadTask task = new DownloadTask();
        try {
            imdb = task.execute("https://www.imdb.com/list/ls052283250/?sort=list_order,asc&mode=grid&page=1&ref_=nmls_vw_grd").get();
            Log.e("Contents of URL", imdb);
            System.out.println(imdb);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        urls = findImageUrls(imdb);
        Log.i("URL list size", urls.size() + "");
        names = findNames(imdb);
        Log.i("Names list size", names.size() + "");
        allNames.addAll(names);
        newCeleb();
    }

    //Sets a new image for the celeb and creates the options
    private void newCeleb() {
        Random r = new Random();

        //Getting a random celeb
        curr = r.nextInt(urls.size());

        //Setting the image for the celeb
        ImageDownloader downloader = new ImageDownloader();
        try {
            //Removes this link so we do not get the same celeb again
            celebImage.setImageBitmap(downloader.execute(urls.remove(curr)).get());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //Setting the button options using a list to avoid redundant answers
        List<String> options = new ArrayList<>();
        //Removes the name from the original list of names
        name = names.remove(curr);
        options.add(name);
        while(options.size() < 4) {
            String temp = allNames.get(r.nextInt(allNames.size()));
            if(!options.contains(temp))
                options.add(temp);
        }
        option1.setText(options.remove(r.nextInt(4)));
        option2.setText(options.remove(r.nextInt(3)));
        option3.setText(options.remove(r.nextInt(2)));
        option4.setText(options.remove(0));
    }

    private List<String> findImageUrls(String html) {
        List<String> urls = new ArrayList<>();

        Pattern p = Pattern.compile("\nsrc=\"(.*?)\"");
        Matcher m = p.matcher(html);

        while(m.find()) {
            urls.add(m.group(1));
        }

        return urls;
    }

    private List<String> findNames(String html) {
        List<String> names = new ArrayList<>();

        Pattern p = Pattern.compile("<img alt=\"(.*?)\"");
        Matcher m = p.matcher(html);

        while(m.find()) {
            names.add(m.group(1));
        }

        return names;
    }

    //This class grabs the HTML code from the URL
    //Not a reliable way to get content, but it is practice for connecting to the internet in Android
    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            StringBuilder sb = new StringBuilder();
            URL url;
            HttpsURLConnection urlConnection;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();
                while(data != -1) {
                    char current = (char) data;
                    sb.append(current);
                    data = reader.read();
                }

            }
            catch(Exception e) {
                Log.e("ERROR", e.toString());
                e.printStackTrace();
            }

            Log.i("CONTENTS", sb.toString());
            return sb.toString();
        }
    }

    //This class grabs the images from the given URL
    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            Bitmap ret = null;

            try {
                URL url = new URL(urls[0]);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                ret =  BitmapFactory.decodeStream(in);

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return ret;
        }
    }

    //Custom Listener for Button Clicks
    private class MyListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Button button = (Button) v;
            Toast toast;

            if(button.getText().equals(name)) {
                toast = Toast.makeText(MainActivity.this, "You got it!", Toast.LENGTH_SHORT);
                correct++;
            }
            else {
                toast = Toast.makeText(MainActivity.this, "Sorry, it was " + name, Toast.LENGTH_SHORT);
            }
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            total++;
            scoreboard.setText("Your Score: " + correct + "/" + total);

            if(total == 100) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("All Done!")
                        .setMessage("Thank you for playing! You've finished all the names. \n Your final score was " + correct + " out of 100.")
                        .show();
            }
            else {
                newCeleb();
            }
        }
    }
}
