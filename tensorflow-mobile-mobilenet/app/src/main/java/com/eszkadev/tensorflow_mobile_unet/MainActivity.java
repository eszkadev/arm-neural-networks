package com.eszkadev.tensorflow_mobile_unet;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
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

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

public class MainActivity extends AppCompatActivity {

    private TensorFlowInferenceInterface inferenceInterface;
    private float[] inputValues;
    private float[] outputValues;

    private final static String INPUT_NODE = "input_1";
    private final static String OUTPUT_NODE = "reshape_2/Reshape";

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
                    if(loadModel()) {
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
                    long difference = 0;
                    for (int i = 0; i < 1; i++) {
                        try {
                            final Bitmap input = BitmapFactory.decodeStream(getAssets().open(String.format("bmp/%d.bmp", i)));
                            ((ImageView) findViewById(R.id.inputView)).setImageBitmap(input);
                            TextView tv = (TextView) findViewById(R.id.sample_text);
                            feedModel(input);
                            long startTime = System.currentTimeMillis();
                            runModel();
                            difference += System.currentTimeMillis() - startTime;
                            results = fetchOutput(results);
                            String diff = String.format("%f s", difference/1000.0);
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

    boolean loadModel() {
        inferenceInterface = new TensorFlowInferenceInterface(getAssets(), "mobilenet.pb");
        return true;
    }

    void feedModel(Bitmap input) {
        inputValues = new float[224 * 224 * 3];
        for(int y = 0; y < 224; y++) {
            for(int x = 0; x < 224; x++) {
                int pixel = input.getPixel(x, y);
                float r = (float)red(pixel);
                float g = (float)green(pixel);
                float b = (float)blue(pixel);
                inputValues[x * 3 + 0 + y * 224 * 3] = r / 127.5F - 1.0F;
                inputValues[x * 3 + 1 + y * 224 * 3] = g / 127.5F - 1.0F;
                inputValues[x * 3 + 2 + y * 224 * 3] = b / 127.5F - 1.0F;
            }
        }
    }

    void runModel() {

        inferenceInterface.feed(INPUT_NODE, inputValues, 1, 224, 224, 3);
        inferenceInterface.run(new String[] {OUTPUT_NODE}, false);

        outputValues = new float[1000];
        inferenceInterface.fetch(OUTPUT_NODE, outputValues);
    }

    String fetchOutput(String results) {
        int maxpos = 0;
        float value = 0;
        for(int i = 0; i < 1000; i++)
        {
            if(outputValues[i] > value)
            {
                value = outputValues[i];
                maxpos = i;
            }
        }
        results += String.valueOf(maxpos) + "\n";
        return results;
    }
}
