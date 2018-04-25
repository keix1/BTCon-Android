package com.keix1.btcon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import com.keix1.btcon.SensorAdapter;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBTAdapter = null;
    private BluetoothDevice mBTDevice = null;
    private BluetoothSocket mBTSocket = null;
    private OutputStream mOutputStream = null;//出力ストリーム

    private Button btnSend;//送信用ボタン
    private Button btnFinish;//終了用ボタン
    private TextView textview;//MacAddress表示用
    private String MacAddress = "DC:A9:04:84:9D:1B";
    private String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
//    private String MY_UUID = "21001101-0000-1000-8000-00805F9B34FB";
    private SensorAdapter sensorAdapter;
    private float[] position = {0.0f, 0.0f, 0.0f};
    private int[] positionInt = {0, 0, 0};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSend = (Button)findViewById(R.id.btnSend);
        btnFinish = (Button)findViewById(R.id.btnFinish);
        textview = (TextView)findViewById(R.id.textView);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setZero();
                if(mBTSocket != null) {
//                    SendZero();
                }
            }
        });
        btnFinish.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //ソケットを確立する関数
        BTConnect();

        //ソケットが取得出来たら、出力用ストリームを作成する
        if(mBTSocket != null){
            try{
                mOutputStream = mBTSocket.getOutputStream();
            }catch(IOException e){/*ignore*/}
        }else{
            btnSend.setText("mBTSocket == null !!");
        }


        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sensorAdapter = new SensorAdapter(mSensorManager);


//        new Thread(new Runnable(){
//            @Override
//            public void run() {
//                while(true) {
//                    try {
//                        Thread.sleep(100);
////                        position[0] += sensorAdapter.getx() * 10;
////                        position[1] += sensorAdapter.gety() * 10;
////                        position[2] += sensorAdapter.getz() * 10;
                        positionInt[0] += (int)sensorAdapter.getx() * 10;
                        positionInt[1] += (int)sensorAdapter.gety() * 10;
                        positionInt[2] += (int)sensorAdapter.getz() * 10;
//                    } catch(Exception e) {
//
//                    }
//                }
//
//            }
//        }).start();

        new Thread(new Runnable(){
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(100);
                        Send();
                    } catch(Exception e) {

                    }
                }

            }
        }).start();

    }

    private void BTConnect(){
        //BTアダプタのインスタンスを取得
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        textview.setText(MacAddress);
        //相手先BTデバイスのインスタンスを取得
        mBTDevice = mBTAdapter.getRemoteDevice(MacAddress);
        //ソケットの設定
        try {
            mBTSocket = mBTDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
        } catch (IOException e) {
            mBTSocket = null;
        }

        if(mBTSocket != null) {
            //接続開始
            mBTAdapter.cancelDiscovery();
            try {
                mBTSocket.connect();
            } catch (IOException connectException) {
                try {
                    mBTSocket.close();
                    mBTSocket = null;
                } catch (IOException closeException) {
                    return;
                }
            }
        }
    }

    private void Send(){
        //文字列を送信する
        byte[] bytes = {};


        position[0] += sensorAdapter.getx();
        position[1] += sensorAdapter.gety();
        position[2] += sensorAdapter.getz();
//        positionInt[0] += (int)sensorAdapter.getx() * 30;
//        positionInt[1] += (int)sensorAdapter.gety() * 30;
//        positionInt[2] += (int)sensorAdapter.getz() * 30;

        String x = String.format("%.1f", position[0]);
        String y = String.format("%.1f", position[1]);
        String z = String.format("%.1f", position[2]);
//        String x = "" + position[0];
//        String y = "" + position[1];
//        String z = "" + position[2];

        String str = "{" + x + "," + y + "," + z + "}";
        Log.d("TAGGGGGG", str);
        bytes = str.getBytes();
        try {
            //ここで送信
            mOutputStream.write(bytes);
        } catch (IOException e) {
            try{
                mBTSocket.close();
            }catch(IOException e1){/*ignore*/}
        }
    }

    private void setZero() {
        positionInt[0] = 0;
        positionInt[1] = 0;
        positionInt[2] = 0;

        position[0] = 0.0f;
        position[1] = 0.0f;
        position[2] = 0.0f;
    }

    private void SendZero(){
        //文字列を送信する
        byte[] bytes = {};
        String x = "0";
        String y = "0";
        String z = "0";

        String str = "{" + x + "," + y + "," + z + "}";
        Log.d("TAGGGGGG", str);
        bytes = str.getBytes();
        try {
            //ここで送信
            mOutputStream.write(bytes);
        } catch (IOException e) {
            try{
                mBTSocket.close();
            }catch(IOException e1){/*ignore*/}
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mBTSocket != null){
            try {
                mBTSocket.connect();
            } catch (IOException connectException) {/*ignore*/}
            mBTSocket = null;
        }
    }

}
