package com.atharva.questionsocr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.Objects;

public class AddAnswersActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    public String mTopic;
    public int mInt = 1;
    String[] sampleString;
    String chosenOption;
    EditText qno;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_answers);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        mTopic = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        EditText topicText = findViewById(R.id.topic_text);
        topicText.setText(mTopic);
        qno = findViewById(R.id.question_no);
        qno.setText(Integer.toString(mInt));

        ValueEventListener v2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                int length = (int) dataSnapshot.getChildrenCount();
                sampleString = new String[length];
                for(int i=0;i < length;i++) {
                    sampleString[i] = Objects.requireNonNull(iterator.next().getKey());
                    Log.d(Integer.toString(i), sampleString[i]);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mTopic = topicText.getText().toString();
        mDatabase.child(mTopic).addListenerForSingleValueEvent(v2);

        findViewById(R.id.add_button).setOnClickListener(v -> {
            RadioGroup r = findViewById(R.id.radio_group);
            int c = r.getCheckedRadioButtonId();
            qno = findViewById(R.id.question_no);
            switch (c){
                case R.id.A: chosenOption = "A";
                break;
                case R.id.B: chosenOption = "B";
                break;
                case R.id.C: chosenOption = "C";
                break;
                case R.id.D: chosenOption = "D";
                break;
            }
            mInt = Integer.parseInt(qno.getText().toString());
            mTopic = topicText.getText().toString();
            if(mInt<=sampleString.length){
                Log.d("TAG",sampleString[mInt-1]);
                mDatabase.child(mTopic).child(sampleString[mInt-1]).child("ans").setValue(chosenOption);
                mInt++;
                qno.setText(Integer.toString(mInt));
                Toast.makeText(this,"Added",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this,"DONE!",Toast.LENGTH_LONG).show();
                mInt--;
            }
        });
        
    }
}
