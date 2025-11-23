package com.example.simplechatter.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatter.R;

public class PersonalView extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_message);
        ReturnMessage();
    }
    private void ReturnMessage(){
        findViewById(R.id.personal_message_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PersonalView.this,MessageList.class);
                startActivity(intent);
            }
        });
    }
}
