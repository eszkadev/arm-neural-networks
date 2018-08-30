package com.eszkadev.fdeep_mobile_mobilenet;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private void saveToInternalStorage(String result){
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "log.txt");

        FileOutputStream fos = null;
        OutputStreamWriter sw = null;
        try {
            fos = new FileOutputStream(file);
            sw = new OutputStreamWriter(fos);
            sw.write(result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(sw != null)
                    sw.close();
                if(fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.runButton).setEnabled(false);

        try {
            final Bitmap input = BitmapFactory.decodeStream(getAssets().open("bmp/0.bmp"));
            ((ImageView)findViewById(R.id.inputView)).setImageBitmap(input);

            Button buttonLoad = (Button)findViewById(R.id.loadButton);
            buttonLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv = (TextView) findViewById(R.id.sample_text);
                    long startTime = System.currentTimeMillis();
                    if(loadModel(getAssets(),false)) {
                        long difference = System.currentTimeMillis() - startTime;
                        String diff = String.format("%d ms", difference);
                        ((TextView)findViewById(R.id.timeText)).setText(diff);
                        tv.setText("Model loaded.");
                        findViewById(R.id.runButton).setEnabled(true);
                    } else {
                        tv.setText("Model load error.");
                    }
                }
            });

            Button buttonRun = (Button)findViewById(R.id.runButton);
            buttonRun.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String results = "";
                    for(int i = 0; i < 1; i++) {
                        try {
                            final Bitmap input = BitmapFactory.decodeStream(getAssets().open(String.format("bmp/%d.bmp", i)));
                            ((ImageView)findViewById(R.id.inputView)).setImageBitmap(input);
                            TextView tv = (TextView) findViewById(R.id.sample_text);
                            feedModel(input);
                            long startTime = System.currentTimeMillis();
                            runModel();
                            long difference = System.currentTimeMillis() - startTime;
                            results += String.valueOf(fetchOutput()) + "\n";
                            String diff = String.format("%d ms", difference);
                            ((TextView) findViewById(R.id.timeText)).setText(diff);
                            tv.setText(results);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    saveToInternalStorage(results);
                }
            });

        } catch(IOException exception) {
            exception.printStackTrace();
        }

    }

    public native boolean loadModel(AssetManager assetManager, boolean runTests);
    public native void unloadModel();
    public native void feedModel(Bitmap input);
    public native void runModel();
    public native int fetchOutput();
}
