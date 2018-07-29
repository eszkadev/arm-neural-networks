package com.eszkadev.fdeep_mobile_unet;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.runButton).setEnabled(false);

        try {
            final Bitmap input = BitmapFactory.decodeStream(getAssets().open("input.bmp"));
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
                    Bitmap output = Bitmap.createBitmap(128,128, Bitmap.Config.ARGB_8888);
                    long startTime = System.currentTimeMillis();
                    runModel(input, output);
                    long difference = System.currentTimeMillis() - startTime;
                    String diff = String.format("%d ms", difference);
                    ((TextView)findViewById(R.id.timeText)).setText(diff);
                    tv.setText("Finished.");
                    ((ImageView)findViewById(R.id.outputView)).setImageBitmap(output);
                }
            });

        } catch(IOException exception) {
            exception.printStackTrace();
        }

    }

    public native boolean loadModel(AssetManager assetManager, boolean runTests);
    public native void unloadModel();
    public native void runModel(Bitmap input, Bitmap output);
}
