package com.avicode.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MyActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.avicode.myapp.MESSAGE";

    public final static String addr = "127.0.0.1";
    public final static int port = 25800;
    private DatagramSocket socket;
    private RecvTask recvTask;
    private Handler recvHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //init recv
                    InetAddress laddr = InetAddress.getByName(addr);
                    Log.e("YS", laddr.getHostAddress());
                    Log.e("YS", laddr.getHostName());
                    //socket = new DatagramSocket(port,InetAddress.getByName("localhost"));
                    socket = new DatagramSocket(port);

                    recvTask = new RecvTask(socket,recvHandler);
                }catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e("YS", "onCreate" + ex.getMessage());
                }

            }
        }).start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void echoMessage(String src, String message){

        TextView textView = (TextView) findViewById(R.id.text_chat);
        textView.append("\n" + src +": "+message);

    }
    private void sendMessage(final String message){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    DatagramPacket packet = new DatagramPacket( message.getBytes(), message.length(),InetAddress.getByName("localhost"), port);
                    socket.send(packet);

                }catch (Exception ex)
                {
                    ex.printStackTrace();
                    Log.e("YS","sendMessage" + ex.getMessage() );
                }

            }
        }).start();


    }
    // Send message
    public void onClick_sendMessage(View view){


        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();



        //print
        echoMessage("me", message);
        //send to remote side
        sendMessage(message);

//        Intent intent = new Intent(this, DisplayMessageActivity.class);
//        intent.putExtra(EXTRA_MESSAGE, message);
//        startActivity(intent);
        //clear text box
        editText.setText("");
    }

    public class UpdateChat implements Runnable {
        private String recvMessage;
        public UpdateChat(String message){
            recvMessage = message;
        }
        @Override
        public void run(){
            echoMessage("he", recvMessage);

        }
    }
}
