package to52.utbm.com.vocalexchange;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.litepal.LitePal;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import to52.utbm.com.vocalexchange.dbutil.HistoryActivity;
import to52.utbm.com.vocalexchange.dbutil.QuestionAnswer;

public class MainActivity extends AppCompatActivity implements MessageDialogFragment.Listener{
    private String TAG = "debugMain";
    public static final String KEY_PARAM_UTTERANCE_ID = "speak";
    //private EditText mAnswer,mQuestion;
    Intent recognizerIntent;
    SpeechRecognizer speechRecognizer;
    private Button speech,change,history,save;
    private TextToSpeech textToSpeech;
    private static int countQuestion;
    private HashMap<String,String> hashMap;
    private String serverIp =  "192.168.1.11";
    private ResultAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private static final String STATE_RESULTS = "results";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private static final int REQUEST_RECORD_PERMISSION = 100;
    private String textResult = "";
    private static String questionNow;
    private static List<QuestionAnswer> qaList;
    private static QuestionAnswer qa;
    private static int questionNum;
    @Override
    protected void onStart(){
        super.onStart();
        //qaList = LitePal.findAll(QuestionAnswer.class);
        Log.i(TAG + "testDBStart",qaList.toString());
        Log.i(TAG,"onstart");
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onresume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onpause");
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"onstop");
    }
    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Log.i(TAG,"oncreate");
          /*  LitePal.deleteAll(QuestionAnswer.class,null);
            List<QuestionAnswer> qaL = LitePal.findAll(QuestionAnswer.class);
            Log.i(TAG+"delete",Boolean.toString(qaL.isEmpty()));*/
            initializeQuestion();
           // mQuestion = findViewById(R.id.question);
            //mAnswer = findViewById(R.id.answer);

            //final List<QuestionAnswer> qaList = LitePal.findAll(QuestionAnswer.class);
            qaList = LitePal.findAll(QuestionAnswer.class);
            //mQuestion.setText( qaList.get(0).getQuestion());
            TextView notification = findViewById(R.id.notification);
            notification.setText("Bonjour, aujourd'hui vous avez "+questionNum+"questions a repondre, pour commencer votre questionnaire, veuillez appuyez sur le bouton " +
                    "suivant, une fois vous avez entendu la question, vous avez 30 second a repondre, si vous n'avez pas entendu, veuillez cliquer recoutez");

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
        /*    history = findViewById(R.id.histroy);
            history.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivity(intent);
                }
            });*/

            ///////////////////////////////////////////////////////////
            speech = findViewById(R.id.speech);
            speech.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                 /*   countQuestion++;
                    if(countQuestion>=qaList.size()){
                        countQuestion = 0;
                    }
*/
                 if(countQuestion == 0){
                     questionNow = qaList.get(countQuestion).getQuestion();
                 }
                 else{
                     questionNow = qaList.get(countQuestion-1).getQuestion();
                 }
                    startSpeaking(questionNow);
                }
            });

            change = findViewById(R.id.change);
            change.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if(countQuestion<qaList.size()){
                        questionNow = qaList.get(countQuestion).getQuestion();
                        startSpeaking(questionNow);
                        countQuestion++;
                    }
                   else{
                        countQuestion = 0;
                        savaOrModify();
                    }

                    //mQuestion.setText(qaList.get(countQuestion).getQuestion());
                }
            });
    /////////////////////////////////////////

        save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
               savaOrModify();
            }
        });
            ActivityCompat.requestPermissions
                (MainActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_PERMISSION);
            ///////////////////////////////////////////////////////////
            qa = new QuestionAnswer();
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener(){
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.i(TAG,"ready speech rec");
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.i(TAG,"begin speech rec");
                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {
                    Log.i(TAG,"end speech rec");
                }

                @Override
                public void onError(int error) {
                    String errorMessage = getErrorText(error);
                    Log.d(TAG, "FAILED " + errorMessage);
                    textResult = "unconnue";
                    //mAdapter.addResult(mQuestion.getText().toString()+"\n"+textResult);
                   /* if(mRecyclerView.getChildCount()<questionNum){
                        mAdapter.addResult("Question"+countQuestion+" :"+questionNow +"\n"+"Reponse"+textResult);
                        mRecyclerView.smoothScrollToPosition(0);
                    }
                    else{
                        savaOrModify();
                    }*/
                    mAdapter.addResult("Question"+countQuestion+" :"+questionNow +"\n"+"Reponse: "+textResult);
                    mRecyclerView.smoothScrollToPosition(0);
                }
                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    textResult = result.get(0);
                    //mAnswer.setText(textResult);
                    qa.setAnswer(textResult);
                    //qa.updateAll("question = ? ",mQuestion.getText().toString());
                    qa.updateAll("question = ? ",questionNow);
                   // List<QuestionAnswer> qaList = LitePal.findAll(QuestionAnswer.class);
                    qaList = LitePal.findAll(QuestionAnswer.class);
                    Log.i(TAG + "testDB",qaList.toString());
                  /*  inttext = 2;
                    Log.i(TAG + "inttext",Integer.toString(inttext));*/
                    //mAdapter.addResult(mQuestion.getText().toString()+"\n"+textResult);
                    Log.i(TAG+"testAdapter",mAdapter.getResults().toString());
           /*         if(mRecyclerView.getChildCount()< questionNum){
                        mAdapter.addResult("Question"+countQuestion+" :"+questionNow +"\n"+"Reponse"+textResult);
                        mRecyclerView.smoothScrollToPosition(0);
                    }
                    else{
                        savaOrModify();
                    }*/
                    mAdapter.addResult("Question"+countQuestion+" :"+questionNow +"\n"+"Reponse: "+textResult);
                    mRecyclerView.smoothScrollToPosition(0);
                }
                @Override
                public void onPartialResults(Bundle partialResults) {
                }
                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });
            recognizerIntent =new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3);

             if( speechRecognizer.isRecognitionAvailable(this)) {
                 Log.i(TAG,"speech rec available");
             }
        /*final AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                .create();
        alert.setTitle("悟空提示:");
        alert.setMessage("您是第8位用户");
        alert.show();*/

      /*  new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               // alert.dismiss();
            }
        },5000);*/
        //////////////////////////////////////////

      /*  if( isForeground(getApplicationContext(),"MainActivity")){
            Log.i(TAG,"on top of stack");
        }
        else{
            Log.i(TAG,"not on top of stack");
        }
        */
      ////////////////////////
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ArrayList<String> results = savedInstanceState == null ? null :
                savedInstanceState.getStringArrayList(STATE_RESULTS);
        mAdapter = new ResultAdapter(results);
        mRecyclerView.setAdapter(mAdapter);
    }

    //text to speech, app speaking
    private void startSpeaking(String q){
        hashMap = new HashMap<String,String>();
        hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,KEY_PARAM_UTTERANCE_ID);
        textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
       /* textToSpeech.speak(mQuestion.getText().toString(),
                TextToSpeech.QUEUE_ADD, hashMap);*/
        textToSpeech.speak(q, TextToSpeech.QUEUE_ADD, hashMap);
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
            //startSpeechRecognizer();
            runOnUiThread (new Thread(new Runnable() {
                public void run() {
                    speechRecognizer.startListening(recognizerIntent);
                    Log.i(TAG,"start listening");
                }
                }));
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
    //@Override
   /* protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    //startSpeaking();
                    mAdapter.addResult(mQuestion.getText().toString()+"\n"+textResult);
                    mRecyclerView.smoothScrollToPosition(0);
                }
                else{
                    Log.i(TAG,Integer.toString(resultCode));
                }
                break;
            }
        }
    }*/

   @Override
    protected void onDestroy() {
        if (textToSpeech != null)
            textToSpeech.shutdown();
        super.onDestroy();
        Log.i(TAG,"ondestroy called");
    }


    private void initializeQuestion(){
        /*ConnectionMysql connectionMysql = new ConnectionMysql(MainActivity.this);
        connectionMysql.execute("");
        Log.i(TAG,"initialize MySQL");
*/    /*  Map<String, String> token = new HashMap<>();
        token.put("token", "synchronize");
        JSONArray jsonArray = synchroDb(token,serverIp);
*/      Log.i(TAG,"initialize called");
        Map<String, String> synchroDb = new HashMap<>();
        synchroDb.put("synchronize", "true");
        JSONArray jsonArray = getQuestionsFromServer(synchroDb,serverIp);
        if (jsonArray==null){
            LitePal.getDatabase();
            Toast.makeText(MainActivity.this, "Synchrnoze failed, service not aviable",
                    Toast.LENGTH_SHORT).show();
        }
        else{
            List<String> qList = new ArrayList<String>();
            questionNum =jsonArray.length();
            try{
                for(int i =0; i<jsonArray.length();i++){
                    qList.add(jsonArray.getString(i));
                }
            }catch(JSONException e) {
                e.printStackTrace();
            }
            LitePal.getDatabase();
            LitePal.deleteAll(QuestionAnswer.class,"");

            //List<String> qList = new ArrayList<String>();
            //qList.add(json);
            //List<String> qList = Arrays.asList(json.split(","));
            //if (qaList.isEmpty()){
            for(int i =0; i<qList.size();i++){
                QuestionAnswer qa = new QuestionAnswer();
                qa.setQuestion(qList.get(i));
                qa.save();
            }
        }

       /* LitePal.getDatabase();
        List<String> qList = new ArrayList<String>();
        qList.add("Vous êtes fatigué?");
        qList.add("Vous êtes en forme?");
        qList.add("Vous avez pris le médicament?");
        qList.add("Vous avez fait du sport?");
        qList.add("Vous vous sentez bien?");
        List<QuestionAnswer> qaList = LitePal.findAll(QuestionAnswer.class);
        //if (qaList.isEmpty()){
            for(int i =0; i<qList.size();i++){
                QuestionAnswer qa = new QuestionAnswer();
                qa.setQuestion(qList.get(i));
                qa.save();
            }*/
        //}
    }

    public JSONArray getQuestionsFromServer(Map<String, String> token, String serverIP) {
        try {
            String serverurl = "http://" + serverIP + "/androidMysql/receiveToken.php";
            SendToServer SendFcmtoken = new SendToServer(token, serverurl);
            String result = SendFcmtoken.execute().get();
            if(result==null)
                return null;
            else{
                JSONArray jsonArray = new JSONArray(result);
                Log.i(TAG, jsonArray.length()+"");
                return jsonArray;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean  isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;

    }



    private static class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        Button buttonRecoutez;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_result, parent, false));
            Log.i("debugMain","called0");
            text = (TextView) itemView.findViewById(R.id.text);
           /* buttonRecoutez = itemView.findViewById(R.id.button);
            buttonRecoutez.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    questionNow = qaList.get(countQuestion).getQuestion();

                }
            });*/

        }

    }
    private static class ResultAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final ArrayList<String> mResults = new ArrayList<>();

        ResultAdapter(ArrayList<String> results) {
            if (results != null) {
                mResults.addAll(results);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.i("debugMain","called1");
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Log.i("debugMain","called2");
            holder.text.setText(mResults.get(position));
        }

        @Override
        public int getItemCount() {
            Log.i("debugMain","called3");
            return mResults.size();
        }

        void addResult(String result) {
            Log.i("debugMain","called4");
            mResults.add(0, result);
            notifyItemInserted(0);
        }

        public ArrayList<String> getResults() {
            Log.i("debugMain","called5");
            return mResults;
        }

    }
    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG,"permission get");
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast
                            .LENGTH_SHORT).show();
                }
        }
    }


    public void savaOrModify(){
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        //builder = new AlertDialog.Builder(this);
                //Uncomment the below code to Set the message and title from the strings.xml file
               // builder.setMessage(R.string.dialog_message) .setTitle(R.string.dialog_title);

                //Setting message manually and performing action on button click
                builder.setMessage("click yes to save, click no to modify your response")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //finish();
                                Toast.makeText(getApplicationContext(),"you choose yes action for save",
                                        Toast.LENGTH_SHORT).show();

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();
                                Toast.makeText(getApplicationContext(),"you choose no action for alertbox",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                //Creating dialog box
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("Do you want to save your answer");
                alert.show();
        }
}
