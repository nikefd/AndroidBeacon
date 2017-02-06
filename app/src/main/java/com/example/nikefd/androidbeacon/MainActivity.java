package com.example.nikefd.androidbeacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    Button startButton, stopButton;
    TextView time, uuid, slot, deviceId, firstND, curSlot;
    private BluetoothAdapter mBluetoothAdapter;

    //Adv
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseData mAdvertiseData;
    private AdvertiseSettings mAdvertiseSettings;
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
    };

    //Scan
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanFilter mScanFilter;
    private ScanSettings mScanSettings;

//    private String uuid_phone = "00000000-0000-0000-0000-000000000000";    //2180
//    private String uuid_phone = "11111111-1111-1111-1111-111111111111";  //1727
//    private String uuid_phone = "22222222-2222-2222-2222-222222222222";  //0917
//    private String uuid_phone = "33333333-3333-3333-3333-333333333333";  //4479
//    private String uuid_phone = "44444444-4444-4444-4444-444444444444";  //0125
//    private String uuid_phone = "55555555-5555-5555-5555-555555555555";  //我的手机
//    private String uuid_phone = "66666666-6666-6666-6666-666666666666";   //7184
//    private String uuid_phone = "77777777-7777-7777-7777-777777777777";   //5099
    private String uuid_phone = "88888888-8888-8888-8888-888888888888";   //8043

    Map<String, String> map = new HashMap<String, String>();
    String[] keys = {"vqwAAAAAAAAAAAAAAAAAAAAAAAkABrUA", "vqwRERERERERERERERERERERAAkABrUA", "vqwiIiIiIiIiIiIiIiIiIiIiAAkABrUA",
            "vqwzMzMzMzMzMzMzMzMzMzMzAAkABrUA", "vqxEREREREREREREREREREREAAkABrUA", "vqxVVVVVVVVVVVVVVVVVVVVVAAkABrUA",
            "vqxmZmZmZmZmZmZmZmZmZmZmAAkABrUA", "vqx3d3d3d3d3d3d3d3d3d3d3AAkABrUA", "vqyIiIiIiIiIiIiIiIiIiIiIAAkABrUA"};

    String[] keys1 = {"vqwAAAAAAAAAAAAAAAAAAAAAAAkABrUA", "vqwRERERERERERERERERERERAAkABrUA", "vqwiIiIiIiIiIiIiIiIiIiIiAAkABrUA",
            "vqwzMzMzMzMzMzMzMzMzMzMzAAkABrUA"};

    String[] keys2 = {"vqxEREREREREREREREREREREAAkABrUA", "vqxmZmZmZmZmZmZmZmZmZmZmAAkABrUA", "vqx3d3d3d3d3d3d3d3d3d3d3AAkABrUA",
            "vqyIiIiIiIiIiIiIiIiIiIiIAAkABrUA"};

    public int[] sequence;      //0表示sleep, 1表示active
    private long wake_time = 50;   //唤醒蓝牙时间(小米note 50ms, 小米4c 2000ms)
    private long time_slot = 3000;   //时间槽大小
    private int no_slot = 0;         //当前时间槽标号
    private boolean isDiscover = false; //是否进行邻居发现
    private int type = 4;  //0 Disco, 1 U-connect, 2 Searchlight, 3 Hedis, 4 Todis
    private int dc = 2; //0 5%, 1 10%, 2 20%
    private int t = 0; //周期长度

    private static final String ROOT_FOLDER = "/ND";
    private String NDFileName = "log.txt";
    File rootDir, logFile;

    Thread NDdiscovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        initMap();
        //Disco 1%-10%
        initSequence(type, dc);

        initView();

        initBluetooth();

        initFile();
    }

    private void initMap() {
        for (String item : keys)
            map.put(item, "0");
    }

    private void initSequence(int type, int dc) {
        switch (type) {
            case 0:
                switch (dc) {
                    case 0:
                        Disco(37, 43);  //5%
                        break;
                    case 1:
                        Disco(17, 23);  //10%
                        break;
                    case 2:
                        Disco(7, 13);  //20%
                        break;
                    case 3:
                        Disco(3, 5);  //test
                        break;
                }
            case 1:
                switch (dc) {
                    case 0:
                        Uconnect(31);  //5%
                        break;
                    case 1:
                        Uconnect(15);  //10%
                        break;
                    case 2:
                        Uconnect(7);  //20%
                        break;
                    case 3:
                }
                break;
            case 2:
                switch (dc) {
                    case 0:
                        Searchlight(40);  //5%
                        break;
                    case 1:
                        Searchlight(20);  //10%
                        break;
                    case 2:
                        Searchlight(10);  //20%
                        break;
                    case 3:
                }
                break;
            case 3:
                switch (dc) {
                    case 0:
                        Hedis(40);  //5%
                        break;
                    case 1:
                        Hedis(20);  //10%
                        break;
                    case 2:
                        Hedis(10);  //20%
                        break;
                    case 3:
                }
                break;
            case 4:
                switch (dc) {
                    case 0:
                        Todis(29);  //5%
                        break;
                    case 1:
                        Todis(14);  //10%
                        break;
                    case 2:
                        Todis(7);  //20%
                        break;
                    case 3:
                }
                break;
        }
    }

    private void Disco(int p1, int p2) {
        t = p1 * p2;
        sequence = new int[t];
        for (int i=0; i<t; ++i) {
            if(i%p1==0 || i%p2==0)
                sequence[i] = 1;
        }
        int r = (int)(Math.random() * t);
        rotate(r);
    }

    private void Uconnect(int p) {
        t = p * p;
        sequence = new int[t];
        for (int i=0; i<=p/2; ++i)
            sequence[i] = 1;
        for (int i=0; i<t; ++i)
            if (i%p == 0)
                sequence[i] = 1;
        int r = (int)(Math.random() * t);
        rotate(r);
    }

    private void Searchlight(int z) {
        t = z * (z/2+1);
        sequence = new int[t];
        int[] probe;
        probe = randomCommon(z/2);
        if (probe == null)
            return;
        for (int i=0; i<t; ++i) {
            if (i % z == 0)
                sequence[i] = 1;
        }
        for (int i=0; i<probe.length; ++i)
            sequence[z*i+probe[i]] = 1;
        int r = (int)(Math.random() * t);
        rotate(r);
    }

    public static int[] randomCommon(int n) {
        if (n < 1) {
            return null;
        }
        int[] result = new int[n];
        int count = 0;
        while(count < n) {
            int num = (int) (Math.random() * n) + 1;
            boolean flag = true;
            for (int j = 0; j < n; j++) {
                if(num == result[j]) {
                    flag = false;
                    break;
                }
            }
            if(flag) {
                result[count] = num;
                count++;
            }
        }
        return result;
    }

    private void Hedis(int z) {
        int t = z * (z - 1);
        sequence = new int[t];
        for (int i=0; i<t; ++i)
            if (i % z == 0)
                sequence[i] = 1;
        for (int i=0; i<z-1; ++i)
            sequence[z*i+i+1] = 1;
        int r = (int)(Math.random() * t);
        rotate(r);
    }

    private void Todis(int z) {
//        int t = (2*z-1) * (2*z+1) * (2*z+3);
//        sequence = new int[t];
//        for (int i=0; i<t; ++i)
//            if (i % (2*z-1) == 0 || i % (2*z+1) == 0 || i % (2*z+3) == 0)
//                sequence[i] = 1;
//        int r = (int)(Math.random() * t);
//        rotate(r);
        int t_real = (2*z-1) * (2*z+1) * (2*z+3);
//        int t = (2*z+1) * (2*z+3);
        int t = 1000;
        int r = (int)(Math.random() * t_real);
        sequence = new int[t];
        for (int i=0; i<t; ++i) {
            if ((i+r) % (2*z-1) == 0 || (i+r) % (2*z+1) == 0 || (i+r) % (2*z+3) == 0)
                sequence[i] = 1;
        }
    }

    public void rotate(int r) {
        int n = sequence.length;
        swapArray(1, r);
        swapArray(r + 1, n);
        swapArray(1, n);
    }

    public void swapArray(int start, int end) {
        for (int s = start - 1, e = end - 1; s < e; s++, e--) {
            int temp = sequence[s];
            sequence[s] = sequence[e];
            sequence[e] = temp;
        }
    }

    // init view
    private void initView() {
        startButton = (Button) findViewById(R.id.start);
        stopButton = (Button) findViewById(R.id.stop);
        time = (TextView) findViewById(R.id.time);
        uuid = (TextView) findViewById(R.id.uuid);
        deviceId = (TextView) findViewById(R.id.deviceId);
        slot = (TextView) findViewById(R.id.slot);
        firstND = (TextView) findViewById(R.id.firstND);
        curSlot = (TextView) findViewById(R.id.curSlot);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled())
                    mBluetoothAdapter.enable();
                isDiscover = true;
                if (NDdiscovery == null) {
                    NDdiscovery = new Thread(new Task());
                    NDdiscovery.start();
                    Toast.makeText(mContext, "Start!", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(mContext, "Neighbor Discovery has already start!", Toast.LENGTH_SHORT).show();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDiscover = false;
                Thread.interrupted();
                NDdiscovery = null;
                Toast.makeText(mContext, "Stop!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // init file directory
    private void initFile() {
        rootDir = new File(Environment.getExternalStorageDirectory(), ROOT_FOLDER);
        NDFileName = String.valueOf(System.currentTimeMillis()) + ".txt";
        logFile = new File(rootDir, NDFileName);
        if(!rootDir.exists()){
            if(rootDir.mkdir()){
                Toast.makeText(mContext, "Successfully set up root folder", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // init bluetooth
    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Adv
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        setAdvertiseData();
        setAdvertiseSettings();

        //Scan
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        setScanSettings();
//        setScanFilter();
    }

    private Handler handlerupdate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            curSlot.setText(String.valueOf(msg.arg1));
        }
    };

    class Task implements Runnable {
        @Override
        public void run() {
            int n = sequence.length;
            no_slot = 0;
            while (isDiscover) {
//                try {
//                    Thread.sleep((long)(2000 * (Math.random())));
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                for (int j=0; j<n; ++j) {
                    no_slot++;
                    Message msg = new Message();
                    msg.arg1 = no_slot;
                    handlerupdate.sendMessage(msg);
                    Log.v("Time_NO:", String.valueOf(j+1));
                    Log.v("Time:", String.valueOf(System.currentTimeMillis()));
                    if (sequence[j] == 1)
                        startNDScan();
//                    else if (j < n - 1 && sequence[j] == 0 && sequence[j + 1] == 1)
//                        try {
//                            Thread.sleep(3000-wake_time);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    else
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                }
            }
        }
    }

    private void startNDScan()
    {
        mBluetoothLeScanner.startScan(null, mScanSettings, mScanCallback);
        mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSettings, mAdvertiseData, mAdvertiseCallback);
        Log.v("Time1:", String.valueOf(System.currentTimeMillis()));
        try {
            Thread.sleep(175);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        try {
            Thread.sleep(1520);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSettings, mAdvertiseData, mAdvertiseCallback);
        try {
            Thread.sleep(175);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        mBluetoothLeScanner.stopScan(mScanCallback);
        Log.v("Time2:", String.valueOf(System.currentTimeMillis()));
    }

    public static byte[] getIdAsByte(java.util.UUID uuid)
    {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    //Adv
    protected void setAdvertiseData() {
        AdvertiseData.Builder mBuilder = new AdvertiseData.Builder();
        ByteBuffer mManufacturerData = ByteBuffer.allocate(24);
        byte[] uuid = getIdAsByte(UUID.fromString(uuid_phone));
        mManufacturerData.put(0, (byte)0xBE); // Beacon Identifier
        mManufacturerData.put(1, (byte)0xAC); // Beacon Identifier
        for (int i=2; i<=17; i++) {
            mManufacturerData.put(i, uuid[i-2]); // adding the UUID
        }
        mManufacturerData.put(18, (byte)0x00); // first byte of Major
        mManufacturerData.put(19, (byte)0x09); // second byte of Major
        mManufacturerData.put(20, (byte)0x00); // first minor
        mManufacturerData.put(21, (byte)0x06); // second minor
        mManufacturerData.put(22, (byte)0xB5); // txPower
        mBuilder.addManufacturerData(224, mManufacturerData.array()); // using google's company ID
        mAdvertiseData = mBuilder.build();
    }

    protected void setAdvertiseSettings() {
        AdvertiseSettings.Builder mBuilder = new AdvertiseSettings.Builder();
        mBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        mBuilder.setConnectable(false);
        mBuilder.setTimeout(0);
        mBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        mAdvertiseSettings = mBuilder.build();
    }

    //Scan
//    private void setScanFilter() {
//        ScanFilter.Builder mBuilder = new ScanFilter.Builder();
//        ByteBuffer mManufacturerData = ByteBuffer.allocate(23);
//        ByteBuffer mManufacturerDataMask = ByteBuffer.allocate(24);
//        byte[] uuid = getIdAsByte(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
//        mManufacturerData.put(0, (byte)0xBE);
//        mManufacturerData.put(1, (byte)0xAC);
//        for (int i=2; i<=17; i++) {
//            mManufacturerData.put(i, uuid[i-2]);
//        }
//        for (int i=0; i<=17; i++) {
//            mManufacturerDataMask.put((byte)0x01);
//        }
//        mBuilder.setManufacturerData(224, mManufacturerData.array(), mManufacturerDataMask.array());
//        mScanFilter = mBuilder.build();
//    }

    private void setScanSettings() {
        ScanSettings.Builder mBuilder = new ScanSettings.Builder();
        mBuilder.setReportDelay(0);
        mBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        mScanSettings = mBuilder.build();
    }

    protected ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            ScanRecord mScanRecord = result.getScanRecord();
            byte[] manufacturerData = mScanRecord.getManufacturerSpecificData(224);
            int mRssi = result.getRssi();
            String str = "";
            if (manufacturerData != null)
                str = Base64.encodeToString(manufacturerData, Base64.NO_WRAP);


            if (str.equals(""))
                return;

            if (map.get(str).equals("1"))
                return;

            boolean label = true;

            map.put(str, "1");
            //for 1-4
//            if (str.equals("vqwAAAAAAAAAAAAAAAAAAAAAAAkABrUA") || str.equals("vqwRERERERERERERERERERERAAkABrUA") || str.equals("vqwiIiIiIiIiIiIiIiIiIiIiAAkABrUA") || str.equals("vqwzMzMzMzMzMzMzMzMzMzMzAAkABrUA"))
//                return;
//            map.put(str, "1");
//            for (String item : keys2)
//                if (map.get(item).equals("0"))
//                    label = false;

            //for 5-8  20%
//            if (str.equals("vqxEREREREREREREREREREREAAkABrUA") || str.equals("vqxmZmZmZmZmZmZmZmZmZmZmAAkABrUA") || str.equals("vqx3d3d3d3d3d3d3d3d3d3d3AAkABrUA") || str.equals("vqyIiIiIiIiIiIiIiIiIiIiIAAkABrUA"))
//                return;
//            map.put(str, "1");
//            for (String item : keys1)
//                if (map.get(item).equals("0"))
//                    label = false;

            int count = 0;
            for (String item : keys)
                if (map.get(item).equals("1"))
                    count++;
            if (count == 8)
                firstND.setText("Success!");
            else
                firstND.setText(String.valueOf(count));

//            if (label)
//                firstND.setText("Success!");

            time.setText(String.valueOf(result.getTimestampNanos()));
            deviceId.setText(String.valueOf(result.getDevice()));
            uuid.setText(str);
            slot.setText(String.valueOf(no_slot));

            Log.v("ND/Time", String.valueOf(result.getTimestampNanos()));
            Log.v("ND/DeviceID", String.valueOf(result.getDevice()));
            Log.v("ND/UUID", str);
            Log.v("ND/Slot", String.valueOf(no_slot));
            String text = no_slot + ":" + str;
            if (!logFile.exists())
            {
                try
                {
                    logFile.createNewFile();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try
            {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(text);
                buf.newLine();
                buf.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };
}
