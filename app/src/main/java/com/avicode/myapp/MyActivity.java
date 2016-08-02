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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;

public class MyActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.avicode.myapp.MESSAGE";

    public final static String serverAddr = "192.168.1.119";
    public final static int serverPort = 25810;

    public final static String addr = "127.0.0.1";

    public final static int port = 25800;
    private DatagramSocket socket;
    private RecvTask recvTask;
    private Handler recvHandler = new Handler();
    private String remoteAddr = "";

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


        //start receive thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //init recv
                    InetAddress laddr = InetAddress.getByName(addr);
                    Log.e("YS", "begin:" + laddr.getHostAddress());

                    //socket = new DatagramSocket(port,InetAddress.getByName("localhost"));
                    socket = new DatagramSocket(port);

                    recvTask = new RecvTask(socket,recvHandler);
                    Log.e("YS", "end:" +  laddr.getHostName());
                }catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e("YS", "onCreate exception" + ex.getMessage());
                }

            }
        }).start();

        //connect to server
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    do {


                        InetAddress addr = InetAddress.getByName(serverAddr);

                        Socket socket = new Socket(addr, serverPort);
                        Log.e("YS", "connected to server: " + serverAddr + ':' + serverPort);
                        String regMsgStr = "REG";
                        char[] regMsgData = regMsgStr.toCharArray();
                        //socket.getOutputStream().write(regMsgData);
//                    socket.getOutputStream().flush();
//                    socket.getInputStream().wait();

                        PrintWriter bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                        bufferOut.write(regMsgData);
                        bufferOut.flush();
                        Thread.sleep(200,0);
                        BufferedReader bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        char[] recvMsgData = new char[128];
                        bufferIn.read(recvMsgData, 0, 128);

                        String recvMsgStr = new String(recvMsgData);
                        String[] clientList = recvMsgStr.split("\n");
                        int token = recvMsgStr.indexOf('\n');


                        Log.e("YS", "RECV from server:" + recvMsgStr);

                        if (clientList.length > 1 && clientList[1] != null) {
                            remoteAddr = clientList[1];
                        }
                        bufferOut.close();
                        bufferIn.close();
                        socket.close();
                        Thread.sleep(10*1000,0);

                    }while(remoteAddr=="");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e("YS", "onCreate server connection exception " + ex.getMessage());
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
    private void sendMessage(final String message, final String remoteIp ){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] buf = message.getBytes("UTF-8");
                    Log.e("YS", "begin send message:" + message);
                    DatagramPacket packet = new DatagramPacket(buf ,buf.length ,InetAddress.getByName(remoteIp), port);

                    socket.send(packet);

                    Log.e("YS", "done send message:" + new String(packet.getData(), 0, packet.getLength()));
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                    Log.e("YS","sendMessage exception" + ex.getMessage() );
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
        if(remoteAddr != "")
        sendMessage(message,remoteAddr);

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
        private final static int MESSAGE_SIZE = 1500;
        private DatagramSocket socket;
        private boolean runListener;
        private Thread recvTread;
        private Handler recvHandler;
        private Runnable runnableThread = new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] recvData = new byte[MESSAGE_SIZE];
//                    InetAddress laddr = InetAddress.getByName("172.0.0.1");
//                    Log.e("YS", laddr.getHostAddress());
//                    Log.e("YS", laddr.getHostName());
                    Log.e("YS", "starting RecvTask");
                    while(runListener){

                        DatagramPacket datagramPacket = new DatagramPacket(recvData,MESSAGE_SIZE);
                        socket.receive(datagramPacket);

                        String recvMsg = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                        Log.e("YS","RecvTask: posting msg:" +  recvMsg  );

                        recvHandler.post(new MyActivity.UpdateChat(recvMsg));
                        Log.e("YS","RecvTask: posting msg: done!" +  recvMsg  );

                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                    Log.e("YS", "RecvTask:"+ex.getMessage() );
                }
            }
        };

        public RecvTask(int port, String address){
            try {

                InetAddress laddr = InetAddress.getByName(address);
                socket = new DatagramSocket(port, laddr);

                recvTread = new Thread(runnableThread);
                runListener = true;
                recvTread.start();


            }catch (Exception ex){
                ex.printStackTrace();
                Log.e("YS", ex.getMessage() );
            }
        }

        public RecvTask(DatagramSocket socket, Handler recvHandler){
            try {
                this.socket = socket;
                this.recvHandler = recvHandler;
                recvTread = new Thread(runnableThread);
                runListener = true;
                recvTread.start();


            }catch (Exception ex){
                ex.printStackTrace();
                Log.e("YS","RecvTask ctor" +ex.getMessage() );
            }
        }

    }
}
