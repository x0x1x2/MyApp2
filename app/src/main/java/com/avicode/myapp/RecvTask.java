package com.avicode.myapp;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by vxa on 26/07/2016.
 */
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
                InetAddress laddr = InetAddress.getByName("172.0.0.1");
                Log.e("YS", laddr.getHostAddress());
                Log.e("YS", laddr.getHostName());
                while(runListener){

                    DatagramPacket datagramPacket = new DatagramPacket(recvData,MESSAGE_SIZE);
                    socket.receive(datagramPacket);
                    Log.e("YS","recv:" +  new String(datagramPacket.getData(), 0, datagramPacket.getLength())  );
                    String recvMsg = new String(datagramPacket.getData(), 0, datagramPacket.getLength());

                    recvHandler.post(new MyActivity.UpdateChat(recvMsg));

                }
            }catch (Exception ex){
                ex.printStackTrace();
                Log.e("YS", "recv:"+ex.getMessage() );
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
            Log.e("YS", ex.getMessage() );
        }
    }

}
