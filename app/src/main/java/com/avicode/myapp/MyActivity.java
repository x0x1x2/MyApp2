package com.avicode.myapp;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MyActivity extends AppCompatActivity {
    private final String tag = "YS" +  " MyActivity";

    public final static String serverAddr = "172.20.10.3"; // server address
    public final static int serverPort = 25810; // server port

    private Socket socket=null;
    private RecvTask recvTask;
    private Handler recvHandler = new Handler();

    private String userName;
    private String serverName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_my);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ((TextView) findViewById(R.id.text_chat)).setMovementMethod(new ScrollingMovementMethod());//scroll bar
        Intent intent = getIntent(); //wait

        //get user and server
        userName = android.os.Build.MODEL;
        serverName = serverAddr;
        try {
            String message = intent.getStringExtra(DisplaySettingActivity.EXTRA_MESSAGE);
            if (message != null) {
                String[] params = message.split("@");

                if (params.length == 2) {
                    if (!params[0].isEmpty()) //user name
                        userName = params[0];
                    if (!params[1].isEmpty()) // server name
                        serverName = params[1];
                }
            }
        } catch (Exception ex) {
            Log.e(tag, ex.getMessage());
            userName = android.os.Build.MODEL;
            serverName = serverAddr;
        }


        //connect to server
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    do {
                        InetAddress lAddr = InetAddress.getByName(serverName);
                        Log.e(tag, "begin:" + lAddr.getHostAddress());
                        try {
                            //Create socket
                            socket = new Socket(lAddr, serverPort);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Log.e(tag, "onCreate socket exception " + ex.getMessage());
                        }
                        Log.e(tag, "end:" + lAddr.getHostName());
                        Thread.sleep(5000,0);//wait 5sec
                    } while(socket == null);

                    Log.e(tag, "connected to :" + serverAddr);
                    //Toast.makeText(MyActivity.this, "Settings clicked", Toast.LENGTH_LONG).show();

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
        try {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                MyPrefFragment.instantiate(MyActivity.this, "MyPrefFragment");
                return true;
            }
        }catch (Exception ex) {
            Log.e(tag, ex.getMessage() );
        }
            return super.onOptionsItemSelected(item);


    }

    private void echoMessage( String message){

        TextView textView = (TextView) findViewById(R.id.text_chat);
        textView.append("\n" + message);

    }
    private void sendMessage(final String message){

        if(socket == null ) {
            Log.e(tag, "sendMessage: no connection to server" );
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedWriter bufferOut;
                try {

                    Log.e(tag, "begin send message:" + message);

                    bufferOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    bufferOut.write(message);
                    bufferOut.flush();
                    //bufferOut.close();

                    Log.e(tag, "done send message:");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e(tag, "sendMessage exception" + ex.getMessage());
                }
            }
        }).start();


    }
    // Send message
    public void onClick_sendMessage(View view){
        //get message
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = userName + ":" + editText.getText().toString();

        //print to screen
        echoMessage(message);
        //send to remote side
        sendMessage(message);

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
            echoMessage( recvMessage);

        }
    }
    public class RecvTask {
        private final String tag = "YS" +  " RecvTask";
        private final static int MESSAGE_SIZE = 1500;
        private Socket socket;
        private boolean runListener;
        private Thread recvTread;
        private Handler recvHandler;
        private Runnable runnableThread = new Runnable() {
            @Override
            public void run() {
                BufferedReader bufferIn = null;
                try {

                    Log.e(tag, "starting RecvTask");
                    bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
                } finally {
                    try {
                        if (bufferIn != null)
                            bufferIn.close();
                        socket.close();
                    } catch (Exception ex) {
                        Log.e(tag, "Exception "+ex.getMessage() );
                    }
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
        public void StopRecvTask()
        {
            runListener = false;  // reset flag


        }
    }
}
