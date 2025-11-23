package com.example.simplechatter.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatter.R;

public class MessageList extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_person);

//        findViewById(R.id.my_view).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MessageList.this,PersonalView.class);
//                startActivity(intent);
//            }
//        });
    }
}
