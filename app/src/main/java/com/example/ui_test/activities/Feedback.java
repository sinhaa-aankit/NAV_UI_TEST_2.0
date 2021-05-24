package com.example.ui_test.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ui_test.R;

public class Feedback extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
    }

    public void feedback(View view) {
        TextView feedback_text = (TextView) findViewById(R.id.feedbackText);
        feedback_text.setText("");
        Toast.makeText(this,"ThankYou for your Feedback!" , Toast.LENGTH_SHORT).show();
    }
}