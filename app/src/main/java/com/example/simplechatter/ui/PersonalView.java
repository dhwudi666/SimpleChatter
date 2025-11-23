package com.example.simplechatter.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatter.R;
import com.example.simplechatter.activity.ContactsActivity;

public class PersonalView extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        ReturnMessage();
    }
    private void ReturnMessage(){
        findViewById(R.id.personal_message_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PersonalView.this, ContactsActivity.class);
                startActivity(intent);
            }
        });
    }
}
