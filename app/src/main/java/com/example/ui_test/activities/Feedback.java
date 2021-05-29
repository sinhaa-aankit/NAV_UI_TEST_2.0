package com.example.ui_test.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ui_test.R;

public class Feedback extends AppCompatActivity {

    Intent intent = new Intent(Intent.ACTION_SEND);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
    }

    public void feedback(View view) {
        TextView feedback_text = (TextView) findViewById(R.id.feedbackText);

        String emailBody = feedback_text.getText().toString();

        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "some@email.address" });
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
        intent.putExtra(Intent.EXTRA_TEXT, emailBody);
        intent.setPackage("com.google.android.gm");

        startActivity(Intent.createChooser(intent, "Choose an Email client :"));


        feedback_text.setText("");
//        Toast.makeText(this,"ThankYou for your Feedback!" , Toast.LENGTH_SHORT).show();
    }
}