package com.avicode.myapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class DisplaySettingActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.avicode.myapp.MESSAGE";//UI
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_setting);

//        Intent intent = getIntent();
//        String message = intent.getStringExtra(MyActivity.EXTRA_MESSAGE);


    }

    // Send message
    public void onClick_ok(View view){
        //get user
        EditText editText = (EditText) findViewById(R.id.edit_user_name);
        String name = editText.getText().toString();
        //get server
        editText = (EditText) findViewById(R.id.edit_server_name);
        String server = editText.getText().toString();



        Intent intent = new Intent(this, MyActivity.class);
        intent.putExtra(EXTRA_MESSAGE, name + "@" +server);
        startActivity(intent);


    }

}
