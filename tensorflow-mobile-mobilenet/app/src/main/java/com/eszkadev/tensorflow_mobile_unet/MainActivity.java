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
    private final static String OUTPUT_NODE = "reshape_2/Reshape";

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
                    Bitmap output = Bitmap.createBitmap(224,224, Bitmap.Config.ARGB_8888);
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
        inferenceInterface = new TensorFlowInferenceInterface(getAssets(), "mobilenet_op4.pb");
        return true;
    }

    void feedModel(Bitmap input) {
        inputValues = new float[224 * 224 * 3];
        for(int y = 0; y < input.getHeight(); y++) {
            for(int x = 0; x < input.getWidth(); x++) {
                inputValues[x + y * input.getWidth()] = input.getPixel(x, y) & 0xff;
                inputValues[x + 1 + y * input.getWidth()] = input.getPixel(x, y) & 0xff00;
                inputValues[x + 2 + y * input.getWidth()] = input.getPixel(x, y) & 0xff0000;
            }
        }
        }

    void runModel() {

        inferenceInterface.feed(INPUT_NODE, inputValues, 1, 224, 224, 3);
        inferenceInterface.run(new String[] {OUTPUT_NODE}, false);

        outputValues = new float[1000];
        inferenceInterface.fetch(OUTPUT_NODE, outputValues);
    }

    void fetchOutput(Bitmap output) {
    }
}
