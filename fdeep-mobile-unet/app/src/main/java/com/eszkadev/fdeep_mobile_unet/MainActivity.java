package com.eszkadev.fdeep_mobile_unet;

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

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private void saveToInternalStorage(Bitmap bitmapImage, String name){
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), name);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
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
            Integer i = 0;
            String fileName = "test/" + i.toString() + ".png";
            final Bitmap input = BitmapFactory.decodeStream(getAssets().open(fileName));
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
                    TextView tv = (TextView) findViewById(R.id.sample_text);
                    for(Integer i = 0; i < 30; i++) {
                        String fileName = "test/" + i.toString() + ".png";
                        try {
                            final Bitmap input = BitmapFactory.decodeStream(getAssets().open(fileName));
                            Bitmap output = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
                            feedModel(input);
                            long startTime = System.currentTimeMillis();
                            runModel();
                            long difference = System.currentTimeMillis() - startTime;
                            fetchOutput(output);
                            String diff = String.format("%d ms", difference);
                            ((TextView) findViewById(R.id.timeText)).setText(diff);
                            tv.setText("Finished.");
                            saveToInternalStorage(output, i.toString() + ".png");
                            ((ImageView) findViewById(R.id.outputView)).setImageBitmap(output);
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    }
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
    public native void fetchOutput(Bitmap output);
}
