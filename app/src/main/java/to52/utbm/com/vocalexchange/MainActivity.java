package to52.utbm.com.vocalexchange;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.litepal.LitePal;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import to52.utbm.com.vocalexchange.dbutil.HistoryActivity;
import to52.utbm.com.vocalexchange.dbutil.QuestionAnswer;

public class MainActivity extends AppCompatActivity{
    private String TAG = "debugMain";
    public static final String KEY_PARAM_UTTERANCE_ID = "speak";
    private EditText mAnswer,mQuestion;
    private Button speech,change,history;
    private TextToSpeech textToSpeech;
    private int countQuestion;
    private HashMap<String,String> hashMap;
    private final int REQUEST_SPEECH_RECOGNIZER = 666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      /*  LitePal.deleteAll(QuestionAnswer.class,null);
        List<QuestionAnswer> qaL = LitePal.findAll(QuestionAnswer.class);
        Log.i(TAG+"delete",Boolean.toString(qaL.isEmpty()));*/
        initializeQuestion();
        //checkPermission();
      //  askAllRequiredPermissions();
        mQuestion = findViewById(R.id.question);
        mAnswer = findViewById(R.id.answer);

        final List<QuestionAnswer> qaList = LitePal.findAll(QuestionAnswer.class);
        mQuestion.setText( qaList.get(0).getQuestion());

        //////////////////initializaTextToSpeech/////////////////
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
                        Toast.makeText(MainActivity.this, "Langage non supporte",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        //////////////////////////////////////////////////////////

        history = findViewById(R.id.histroy);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });



        ///////////////////////////////////////////////////////////
        speech = findViewById(R.id.speech);
        speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSpeaking();
            }
        });

        change = findViewById(R.id.change);
        change.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                countQuestion++;
                if(countQuestion>=qaList.size()){
                    countQuestion = 0;
                }
                mQuestion.setText(qaList.get(countQuestion).getQuestion());
            }
        });
    }

    //text to speech, app speaking
    private void startSpeaking(){
        hashMap = new HashMap<String,String>();
        hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,KEY_PARAM_UTTERANCE_ID);
        textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
        textToSpeech.speak(mQuestion.getText().toString(),
                TextToSpeech.QUEUE_ADD, hashMap);
    }

    //after app speaking,listening to user
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

    //speech to text, user speaking
    private void startSpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Je vous écoute ");
        try {
            startActivityForResult(intent, 666);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Désolé,reconnaissance vocale non supporte ",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //update UI, display the user's words
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 666: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String textResult = result.get(0);
                    mAnswer.setText(textResult);
                    QuestionAnswer qa = new QuestionAnswer();
                    qa.setAnswer(textResult);
                    qa.updateAll("question = ? ",mQuestion.getText().toString());
                    List<QuestionAnswer> qaList = LitePal.findAll(QuestionAnswer.class);
                    Log.i(TAG + "testDB",qaList.toString());
                    //  startSpeaking();
                }
                else{
                    Log.i(TAG,Integer.toString(resultCode));
                }
                break;
            }
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


    private void initializeQuestion(){
        LitePal.getDatabase();
        List<String> qList = new ArrayList<String>();
        qList.add("Vous êtes fatigué?");
        qList.add("Vous êtes en forme?");
        qList.add("Vous avez pris le médicament?");
        qList.add("Vous avez fait du sport?");
        qList.add("Vous vous sentez bien?");
        List<QuestionAnswer> qaList = LitePal.findAll(QuestionAnswer.class);
        if (qaList.isEmpty()){
            for(int i =0; i<qList.size();i++){
                QuestionAnswer qa = new QuestionAnswer();
                qa.setQuestion(qList.get(i));
                qa.save();
            }
        }
    }

}
