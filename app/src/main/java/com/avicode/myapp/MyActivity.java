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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;

public class MyActivity extends AppCompatActivity {
    private final String tag = "YS" +  " MyActivity";
    public final static String EXTRA_MESSAGE = "com.avicode.myapp.MESSAGE";//UI

    //public final static String serverAddr = "192.168.1.108"; // server address
    public final static String serverAddr = "192.168.1.118"; // server address
    public final static int serverPort = 25810; // server port

    private Socket socket=null;
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


        //connect to server
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    do {
                        InetAddress lAddr = InetAddress.getByName(serverAddr);
                        Log.e(tag, "begin:" + lAddr.getHostAddress());
                        try {
                            //Create socket
                            socket = new Socket(lAddr, serverPort);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Log.e(tag, "onCreate socket exception " + ex.getMessage());
                        }
                        Log.e(tag, "end:" + lAddr.getHostName());
                    } while(socket == null);

                    //create receive thread
                    recvTask = new RecvTask(socket, recvHandler);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e(tag, "onCreate server connection exception " + ex.getMessage());
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

        if(socket == null ) {
            Log.e(tag, "sendMessage: no connection to server" );
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //byte[] buf = message.getBytes("UTF-8");
                    Log.e(tag, "begin send message:" + message);

                    BufferedWriter bufferOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    bufferOut.write(message);
                    bufferOut.flush();
                    //bufferOut.close();

                    Log.e(tag, "done send message:");
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                    Log.e(tag,"sendMessage exception" + ex.getMessage() );
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
    public class RecvTask {
        private final String tag = "YS" +  "RecvTask: ";
        private final static int MESSAGE_SIZE = 1500;
        private Socket socket;
        private boolean runListener;
        private Thread recvTread;
        private Handler recvHandler;
        private Runnable runnableThread = new Runnable() {
            @Override
            public void run() {
                try {

                    Log.e(tag, "starting RecvTask");
                    BufferedReader bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    char[] recvMsgData = new char[MESSAGE_SIZE];

                    while(runListener){
                         int len=0;
                        try {
                            len = bufferIn.read(recvMsgData, 0, MESSAGE_SIZE);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            Log.e(tag, "buffer read exception " + ex.getMessage());
                        }

                        String recvMsgStr = new String(recvMsgData, 0, len);
                        String[] msgList = recvMsgStr.split("\0");
                        Log.e(tag, "RECV from server:" + recvMsgStr);

                        recvHandler.post(new MyActivity.UpdateChat(msgList[0]));
                        Log.e(tag," posting msg: done!" +  msgList[0]);

                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                    Log.e(tag, "Exception "+ex.getMessage() );
                }
            }
        };

        public RecvTask(Socket socket, Handler recvHandler){
            try {
                //Save data
                this.socket = socket;
                this.recvHandler = recvHandler;
                //Receive thread
                recvTread = new Thread(runnableThread); // creat
                runListener = true;  // set flag true
                recvTread.start(); // start it


            }catch (Exception ex){
                ex.printStackTrace();
                Log.e(tag,"c'tor Exception " +ex.getMessage() );
            }
        }

    }
}
