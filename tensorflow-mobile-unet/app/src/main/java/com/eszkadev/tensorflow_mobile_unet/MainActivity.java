package com.eszkadev.tensorflow_mobile_unet;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
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

    private TensorFlowInferenceInterface inferenceInterface;
    private float[] inputValues;
    private float[] outputValues;

    private final static String INPUT_NODE = "input_1";
    private final static String OUTPUT_NODE = "conv2d_24/BiasAdd";

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
                    TextView tv = (TextView) findViewById(R.id.sample_text);
                    Bitmap output = Bitmap.createBitmap(128,128, Bitmap.Config.ARGB_8888);
                    feedModel(input);
                    long startTime = System.currentTimeMillis();
                    runModel();
                    long difference = System.currentTimeMillis() - startTime;
                    fetchOutput(output);
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

    boolean loadModel() {
        inferenceInterface = new TensorFlowInferenceInterface(getAssets(), "unet.pb");
        return true;
    }

    void feedModel(Bitmap input) {
        inputValues = new float[128 * 128];
        for(int y = 0; y < input.getHeight(); y++) {
            for(int x = 0; x < input.getWidth(); x++) {
                inputValues[x + y * input.getWidth()] = input.getPixel(x, y) & 0xff;
            }
        }
        }

    void runModel() {

        inferenceInterface.feed(INPUT_NODE, inputValues, 1, 128, 128, 1);
        inferenceInterface.run(new String[] {OUTPUT_NODE}, false);

        outputValues = new float[128 * 128];
        inferenceInterface.fetch(OUTPUT_NODE, outputValues);
    }

    void fetchOutput(Bitmap output) {
        for(int y = 0; y < 128; y++) {
            for(int x = 0; x < 128; x++) {
                int pixel = outputValues[x + y * 128] < 0 ? 0xff000000 : 0xffffffff;
                output.setPixel(x, y, pixel);
            }
        }
    }
}
