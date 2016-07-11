package com.nstd.tools.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.LogRecord;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTouch;

/**
 * Created by maoting on 2016/7/7.
 */
public class MouseActivity extends Activity {
    public static final String TAG = MouseActivity.class.getSimpleName();

    @BindView(R.id.click_region)
    TextView clickRegion;

    @OnLongClick(R.id.click_region)
    public boolean longClickClickRegion(View v) {
        log(TAG, "long click");
        return true;
    }

    @OnClick(R.id.click_region)
    public void clickClickRegion(View v) {
        log(TAG, "just click");
    }

    int lastX;
    int lastY;

    int num = 0;
    public void log(String msg) {
        Message hmsg = handler.obtainMessage(1);
        Bundle b = new Bundle();
        b.putString("log", msg);
        hmsg.setData(b);
        handler.sendMessage(hmsg);
    }

    public void log(Exception e) {
        log(e.getMessage());
    }
    
    public void log(String tag, String msg) {
        log(msg);
    }

    @OnTouch(R.id.click_region)
    public boolean touchClickRegion(View v, MotionEvent event) {
//        log(TAG, "touch left region");
//        log(TAG, "action=" + event.getAction() + " count=" + event.getPointerCount());
        if(event.getPointerCount() > 1) {
            int pointId0 = event.getPointerId(0);
            int pointId1 = event.getPointerId(1);
            log(TAG, "aId=" + event.getActionIndex() + " pId=" + pointId0 + "pId1=" + pointId1);
        }

        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                log(TAG, "left down");
                lastX = (int)event.getX();
                lastY = (int)event.getY();
                break;
            case MotionEvent.ACTION_UP:
                log(TAG, "left up");
                break;
            case MotionEvent.ACTION_MOVE:
//                log(TAG, "left move:" + event.getX() + " " + event.getY());
                int x = (int) event.getX();
                int y = (int) event.getY();
                if((Math.abs(lastX - x) > 3 || Math.abs(lastY - y) > 3) && clientThread != null) {
                    clientThread.enqueue(new Op("m", x-lastX, y-lastY));
                } else {
                    lastX = x;
                    lastY = y;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                log(TAG, "left cancel");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                log(TAG, "left 2 down");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                log(TAG, "left 2 up");
                break;
        }
        return true;
    }

    @OnTouch(R.id.scroll_vertical)
    public boolean touchLeftScrollVertical(View v, MotionEvent event) {
        log(TAG, "touch scroll vertical");
        return true;
    }

    @OnTouch(R.id.scroll_horizontal)
    public boolean touchBottomScrollHorizontal(View v, MotionEvent event) {
        log(TAG, "touch scroll horizontal");
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mouse);
        ButterKnife.bind(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(clientListenerThread == null) {
            clientListenerThread = new ClientListenerThread(this);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    log("开始服务器监听线程");
                    clientListenerThread.start();
                }
            }, 1000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            clientListenerThread.interrupt();
            clientListenerThread = null;
            if(clientThread != null) {
                clientThread.interrupt();
                clientThread = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log(e);
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1) {
                Bundle b = msg.getData();
                String mmsg = b.getString("log", "");
                num ++;
                if(num > 15) {
                    clickRegion.setText(mmsg + "\n");
                    num = 0;
                } else {
                    clickRegion.append(mmsg + "\n");
                }
            }

        }
    };
    static ClientThread clientThread;
    static ClientListenerThread clientListenerThread;


    public static class Op {
        public String op;
        public int x;
        public int y;

        public Op() {}
        public Op(String op) {
            this.op = op;
        }

        public Op(String op, int x, int y) {
            this.op = op;
            this.x = x;
            this.y = y;
        }
    }



    public static class ClientThread extends Thread {
        MouseActivity parent;
        private Socket mSocket = null;
        private String msg = null;
        BufferedReader in = null;
        PrintWriter out = null;
        private ConcurrentLinkedQueue<Op> opQueue = new ConcurrentLinkedQueue<>();

        public ClientThread(Socket socket, MouseActivity parent) {
            this.mSocket = socket;
            this.parent = parent;
        }

        public void enqueue(Op op) {
            opQueue.add(op);
        }

        public Op dequeue() {
            return opQueue.poll();
        }

        public boolean isEmpty() {
            return opQueue.isEmpty();
        }

        public String extractMsg(Op op) {
            return op.op.equals("m") ? "m:" + op.x + ":" + op.y : op.op;
        }

        @Override
        public void run() {
            try {
                parent.log("开始输出");
//                in = new BufferedReader(new InputStreamReader(System.in));
                out = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream()), true);
                while(!this.isInterrupted()){
//                    msg = in.readLine();
//         		log("msg=" + msg);
                    if(isEmpty()) {
                        Thread.sleep(100);
                    } else {
                        msg = extractMsg(dequeue());
                        out.println(msg);
                        if (msg.trim().equals("exit")) {
//                        in.close();
                            out.close();
                            break;
                        }
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static final int SERVER_PORT = 1713;
    private static Socket Client = null;

    public static class ClientListenerThread extends Thread {

        private MouseActivity parent;

        static final int INPORT = 1712;
        private byte[] buf = new byte[1000];
        private DatagramPacket dp = new DatagramPacket(buf, buf.length);
        private DatagramSocket socket;
        InetAddress hostAddress;

        public ClientListenerThread() {}

        public ClientListenerThread(MouseActivity main) {
            parent = main;
        }

        @Override
        public void run() {
            parent.log("clientListenerThread start");
            try {
                socket = new DatagramSocket(INPORT + 5);
                socket.setSoTimeout(5000);
                String host = "192.168.0.102";
//                String host = "255.255.255.255";
//                String[] ipStr = host.split("\\.");
//                byte[] ipBuf = new byte[4];
//                for(int i = 0; i < 4; i++){
//                    ipBuf[i] = (byte)(Integer.parseInt(ipStr[i])&0xff);
//                }
//                hostAddress = InetAddress.getByAddress(ipBuf);
//                parent.log("serverIp:" + hostAddress.getHostAddress());

                hostAddress = InetAddress.getByName(host);
                String outMsg = "request";

                socket.send(Dgram.toDatagram(outMsg, hostAddress, INPORT));
                parent.log("request msg send to server");
                while(!this.isInterrupted()) {
                    boolean hasMsg = false;
                    try {
                        socket.receive(dp);
                        hasMsg = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        parent.log("链接超时, 重新发送");
                        socket.send(Dgram.toDatagram(outMsg, hostAddress, INPORT));
                    }

                    if(hasMsg) {
                        String msg = Dgram.toString(dp).trim();
                        String rcvd = msg + ", from address:"
                                + dp.getAddress() + ", port:" + dp.getPort();
                        parent.log(rcvd);

                        //TODO 判断是否是从服务器ip、端口发来的消息
                        if (true) {
                            if (msg.equals("start")) {
                                parent.log("started");
                                socket.close();
                                break;
                            } else {
                                Thread.sleep(1000);
                                socket.send(Dgram.toDatagram(outMsg, hostAddress, INPORT));
                            }
                        }
                    }
                }

                Client = new Socket(host, SERVER_PORT);
                clientThread = new ClientThread(Client, parent);
                clientThread.start();
            } catch (Exception e) {
                parent.log(e);
                e.printStackTrace();
            }
        }
    }

    public static class Dgram {

        public static DatagramPacket toDatagram(String s, InetAddress destIA,
                                                int destPort) {
            byte[] buf = new byte[s.length() + 1];
            s.getBytes(0, s.length(), buf, 0);
            return new DatagramPacket(buf, buf.length, destIA, destPort);
        }

        public static String toString(DatagramPacket p) {
            return new String(p.getData(), 0, p.getLength());
        }
    }
}
