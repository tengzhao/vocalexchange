package to52.utbm.com.vocalexchange.dbutil;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import org.litepal.LitePal;

import java.util.List;

import to52.utbm.com.vocalexchange.R;

public class HistoryActivity extends AppCompatActivity {
    String TAG ="debugHistory";
    private TextView mAnswer,mQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Log.i(TAG,"create histort activity");


    }
    @Override
    protected void onStart(){
        super.onStart();
        LinearLayout allcontent = findViewById(R.id.list_view);
        List<QuestionAnswer> qaList = LitePal.findAll(QuestionAnswer.class);

        for (int i = 0; i < qaList.size(); i++){
            Log.i(TAG,"create content");
            View oneqa = View.inflate(this, R.layout.content_qa, null);
            mQuestion = oneqa.findViewById(R.id.questionhistory);
            mAnswer = oneqa.findViewById(R.id.answerhistory);
            mQuestion.setText(qaList.get(i).getQuestion());
            mAnswer.setText(qaList.get(i).getAnswer());
            allcontent.addView(oneqa);
        }
        Log.i(TAG,"onstart");

    }



}
