package to52.utbm.com.vocalexchange;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private String TAG = "debug";
    public static final String KEY_PARAM_UTTERANCE_ID = "speak";
    private EditText input;
    private Button speech,record;
    private TextToSpeech textToSpeech;
    private HashMap<String,String> hashMap;
    private abstract class runnable implements Runnable {
    }
    private final int REQUEST_SPEECH_RECOGNIZER = 666;
    private TextView mTextView;
    private final String mQuestion = "Say something";


    UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            Log.i(TAG,"start speaking");
        }

        @Override
        public void onDone(String utteranceId) {
            Log.i(TAG,"end speaking");
            startSpeechRecognizer();
        }

        @Override
        public void onError(String utteranceId) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      //  checkPermission();
      //  askAllRequiredPermissions();
        mTextView = (TextView)findViewById(R.id.tvstt);

     /*   Button boutonListen = (Button) findViewById(R.id.listen);
        boutonListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              *//*  Toast.makeText(getApplicationContext(),
                        "click",
                        Toast.LENGTH_SHORT).show();*//*
                startSpeechRecognizer();
            }
        });*/

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == textToSpeech.SUCCESS) {
                    Locale locale = getResources().getConfiguration().locale;
                    String lan = locale.getLanguage();
                    Log.i("Lan",lan);
                    int result = textToSpeech.setLanguage(Locale.FRENCH);
                    if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                            && result != TextToSpeech.LANG_AVAILABLE){
                        Toast.makeText(MainActivity.this, "TTS暂时不支持这种语音的朗读！",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        input = (EditText) findViewById(R.id.input_text);
        speech = (Button) findViewById(R.id.speech);

        speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              /*  textToSpeech.speak(input.getText().toString(),
                        TextToSpeech.QUEUE_ADD, null);*/
              hashMap = new HashMap<String,String>();
                hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,KEY_PARAM_UTTERANCE_ID);
                textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
                textToSpeech.speak(input.getText().toString(),
                        TextToSpeech.QUEUE_ADD, hashMap);
            }
        });

    }

    //speech to text
    private void startSpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Je vous ecoute");
        try {
            startActivityForResult(intent, 666);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Speech recognition is not supported in this device.",
                    Toast.LENGTH_SHORT).show();
        }
    }

   @Override
    protected void onDestroy() {
        if (textToSpeech != null)
            textToSpeech.shutdown();
        super.onDestroy();
        Log.i("test","ondestroy called");
    }


    private static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 666: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String text = result.get(0);
                    mTextView.setText(text);
                }
                break;
            }
        }
    }
    private void askAllRequiredPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};

        if (!hasPermissions(this, PERMISSIONS)) {
            this.requestPermissions(PERMISSIONS, PERMISSION_ALL);
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }
}
