package com.atharva.questionsocr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.atharva.questionsocr.MESSAGE";
    private static final String TAG = "MainActivity1111";
    public Button mTextButton;
    public Button mUploadButton;
    private DatabaseReference mDatabase;

    public Bitmap mSelectedImage;
    public CropImageView mCropImageView;

    String mLatestText;
    String[] mQuestion;
    public String[][] allQuestions;
    String mTopic = "Cengage Algebra";
    public int mQNoToAdd = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        loadSpinner();

        mCropImageView = findViewById(R.id.image_view);

        mTextButton = findViewById(R.id.button_text);
        mUploadButton = findViewById(R.id.upload);
        Button mCaptureButton = findViewById(R.id.button_camera);

        findViewById(R.id.add_answers_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAnswersActivity.class);
            EditText e4 = findViewById(R.id.add_topic_box);
            mTopic = e4.getText().toString();
            intent.putExtra(EXTRA_MESSAGE, mTopic);
            startActivity(intent);
        });

        mTextButton.setOnClickListener(v -> uploadImage());
        mCaptureButton.setOnClickListener(v -> openCropping());
        mUploadButton.setOnClickListener(v -> uploadQuestion());
        findViewById(R.id.add_topic_button).setOnClickListener(v -> {
            @SuppressLint("CutPasteId") EditText e = findViewById(R.id.add_topic_box);
             String reqTopic = e.getText().toString();
             mDatabase.child(reqTopic).setValue("1");
             loadSpinner();
        });
    }

    private void loadSpinner(){
        Spinner spinner = findViewById(R.id.spinner);
        List<String> list = new ArrayList<>();
        ValueEventListener v1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    Log.e(TAG, Objects.requireNonNull(postSnapshot.getKey()));
                    list.add(postSnapshot.getKey());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        mDatabase.addListenerForSingleValueEvent(v1);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        adapter.notifyDataSetChanged();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, spinner.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
                mTopic = spinner.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void uploadQuestion() {
        EditText e3 = findViewById(R.id.add_topic_box);
        mTopic = e3.getText().toString();
        ValueEventListener v2 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EditText e5 = findViewById(R.id.qnotbadded);
                String[][] qstoadd = new String[allQuestions.length][5];
                LinearLayout linearLayout = findViewById(R.id.edit_text_layout);
                for (int i = 0; i<allQuestions.length;i++){
                    for(int j = 0; j<5; j++){
                        qstoadd[i][j] = ((EditText)((LinearLayout)linearLayout.getChildAt(i)).getChildAt(j)).getText().toString();
                    }

                }
                try {
                    mQNoToAdd = Integer.getInteger(e5.getText().toString());
                } catch (Exception e) { e.printStackTrace();  mQNoToAdd = 0;}
                if(mQNoToAdd != 0){
                    for(int i = 0; i<qstoadd.length; i++){
                        mDatabase = FirebaseDatabase.getInstance().getReference();
                        mDatabase.child(mTopic).child(Integer.toString(mQNoToAdd + i))
                                .setValue(new Question(qstoadd[i][0],qstoadd[i][1],qstoadd[i][2],qstoadd[i][3],qstoadd[i][4],""));
                    }
                    /*mDatabase.child(mTopic).child(Integer.toString(mQNoToAdd))
                            .setValue(new Question(mQuestion[0],mQuestion[1],mQuestion[2],mQuestion[3],mQuestion[4],""));*/
                }else{
                    /*mDatabase.child(mTopic).child(Integer.toString((int)dataSnapshot.getChildrenCount()+1))
                            .setValue(new Question(mQuestion[0],mQuestion[1],mQuestion[2],mQuestion[3],mQuestion[4],""));*/
                    for (int m =0 ; m<qstoadd.length; m++) {
                        String[] allQuestion = qstoadd[m];
                        mDatabase.child(mTopic).child(Integer.toString(((int) dataSnapshot.getChildrenCount()) + 1 + m))
                                .setValue(new Question(allQuestion[0], allQuestion[1], allQuestion[2], allQuestion[3], allQuestion[4], ""));
                    }
                }

               int QNoToAdd = ((int) dataSnapshot.getChildrenCount())+qstoadd.length;
               // addQNo(QNoToAdd);
                //showToast("Uploaded");

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast(databaseError.getMessage());
                Log.d(TAG, databaseError.getDetails());

            }
        };
        mDatabase.child(mTopic).addListenerForSingleValueEvent(v2);
        showToast(mTopic);

    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void openCropping(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE){
            super.onActivityResult(requestCode, resultCode, data);
            //setPic();
            //galleryAddPic();
        }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri photoURI = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                    mSelectedImage = getResizedBitmap(bitmap,800);
                    mCropImageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                showToast(error.getMessage());
            }
        }

    }

    private void uploadImage() {
        Bitmap bm = getResizedBitmap(mSelectedImage, 700);
        UploadImageTask.UploadParams params = new UploadImageTask.UploadParams(bm);
        UploadImageTask task = new UploadImageTask(new UploadImageTask.ResultListener() {
            @Override
            public void onError(String message) {
                showToast(message);
            }

            @Override
            public void onSuccess(String text) {

                LinearLayout linearLayout = findViewById(R.id.edit_text_layout);
                linearLayout.removeAllViews();

                mLatestText = text;
                Log.d("Text_NEW", mLatestText);
                String reg = "1\\. |22\\.|16\\.|17\\.";
                for (int i = 2; i<200; i++){
                   reg =  reg.concat("|"+i+"\\. ");
                }
                mQuestion = mLatestText.split(reg);

                allQuestions = new String[mQuestion.length-1][5];
                linearLayout = findViewById(R.id.edit_text_layout);
                String[] currentQ;
                for (int i = 0; i<mQuestion.length-1; i++) {
                    currentQ = mQuestion[i+1].split("\\(a\\) |\\(b\\) |\\(c\\) |\\(d\\) |a\\. |b\\. |c\\. |d\\. |\\(A\\) |\\(B\\) |\\(C\\) |\\(D\\) ");
                    System.arraycopy(currentQ, 0, allQuestions[i], 0, currentQ.length);
                    LinearLayout a = new LinearLayout(MainActivity.this);
                    a.setOrientation(LinearLayout.VERTICAL);
                    //a.setId(100+i);

                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    int scale = (int)getResources().getDisplayMetrics().density;
                    EditText et1 = new EditText(MainActivity.this);
                    EditText et2 = new EditText(MainActivity.this);
                    EditText et3 = new EditText(MainActivity.this);
                    EditText et4 = new EditText(MainActivity.this);
                    EditText et5 = new EditText(MainActivity.this);
                    et1.setLayoutParams(p); et1.setText("a"); et1.setBackgroundColor(Color.CYAN);
                    et2.setLayoutParams(p); et2.setText("a"); et2.setPadding(20*scale,0,0,0); et2.setBackgroundColor(Color.parseColor("#FFFFED"));
                    et3.setLayoutParams(p); et3.setText("a"); et3.setPadding(20*scale,0,0,0); et3.setBackgroundColor(Color.parseColor("#FFFFED"));
                    et4.setLayoutParams(p); et4.setText("a"); et4.setPadding(20*scale,0,0,0); et4.setBackgroundColor(Color.parseColor("#FFFFED"));
                    et5.setLayoutParams(p); et5.setText("a"); et5.setPadding(20*scale,0,0,0); et5.setBackgroundColor(Color.parseColor("#FFFFED"));
                    EditText[] et = {et1, et2, et3, et4, et5};

                    for (int j = 0; j<5; j++) {
                        try {
                            et[j].setText(currentQ[j]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(et[j].getParent() != null){
                            ((ViewGroup)et[j].getParent()).removeView(et[j]);
                        }
                        a.addView(et[j]);
                    }
                    linearLayout.addView(a);
                }
                String s = "";
                for(String[] cq: allQuestions){
                    s = s.concat(Arrays.toString(cq));
                }
            }
        });
        task.execute(params);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public void addQNo(int n){
        runOnUiThread(() -> {
            EditText edit = findViewById(R.id.qnotbadded);
            edit.setText(n);
        });


    }
}
