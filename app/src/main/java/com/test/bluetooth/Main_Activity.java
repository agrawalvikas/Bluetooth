package com.test.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Main_Activity extends Activity implements OnItemClickListener {

	ArrayAdapter<String> listAdapter;
	TextView tvSearch;
	ListView listView;
	Button search,disconnect;
	BluetoothAdapter btAdapter;
	Set<BluetoothDevice> devicesArray;
	ArrayList<String> pairedDevices;
	ArrayList<BluetoothDevice> devices;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;
	protected static final int CHK_BLUE = 2;
	BluetoothDevice temp;
	IntentFilter filter;
	BluetoothSocket tempSocket;
	BroadcastReceiver receiver;
	String tag = "debugging";
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Log.i(tag, "in handler");

			switch(msg.what){
				case CHK_BLUE:
					String chk = (String)msg.obj;
					Toast.makeText(getApplicationContext(), chk, Toast.LENGTH_SHORT).show();
					break;

				case SUCCESS_CONNECT:
					// DO something
					Log.i(tag, "connected");
					Toast.makeText(getApplicationContext(), "CONNECTED", Toast.LENGTH_SHORT).show();
					tempSocket = (BluetoothSocket)msg.obj;
					Intent joystickIntent = new Intent(getApplicationContext(),JoyStickActivity.class);
					//joystickIntent.putExtra("device",temp);
					startActivity(joystickIntent);

					//String s = "successfully connected";
					//connectedThread.write(s.getBytes());
					break;
				case MESSAGE_READ:
					byte[] readBuf = (byte[])msg.obj;
					String string = new String(readBuf);
					Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
					break;

			}
		}
	};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
        if(btAdapter==null){
        	Toast.makeText(getApplicationContext(), "No bluetooth detected", Toast.LENGTH_LONG).show();
        	finish();
        }
        else{
        	if(!btAdapter.isEnabled()) {
				turnOnBT();
			}
        }
    }
	private void init() {
		//Comment
		// TODO Auto-generated method stub
		listView=(ListView)findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		tvSearch = (TextView)findViewById(R.id.tvSearch);
		disconnect = (Button)findViewById(R.id.bDisconnect);
		disconnect.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if(tempSocket!=null){
					ConnectedThread connectedThread = new ConnectedThread(tempSocket);
					connectedThread.cancel();
					Toast.makeText(getApplication(),"DISCONNECTED",Toast.LENGTH_SHORT).show();
					tempSocket =null;
				}
				else{
					Toast.makeText(getApplication(),"No connected device found!..",Toast.LENGTH_SHORT).show();
				}

			}
		});
		search = (Button)findViewById(R.id.button);
		search.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				getPairedDevices();
				startDiscovery();
			}
		});
		pairedDevices = new ArrayList<String>();
		devices = new ArrayList<BluetoothDevice>();
		listAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
		listView.setAdapter(listAdapter);

		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		receiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();

				if(BluetoothDevice.ACTION_FOUND.equals(action)){
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					devices.add(device);
					String s = "";
					for(int a = 0; a < pairedDevices.size(); a++){
						if(device.getName().equals(pairedDevices.get(a))){
							//append
							s = "(Paired)";
							break;
						}
					}
					listAdapter.add(device.getName()+" "+s+" "+"\n"+device.getAddress());
				}

				else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
					// run some code
					tvSearch.setText("Searching... Please Wait");
				}
				else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
					// run some code
					tvSearch.setText("Search Completed");
				}
				else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
					if(btAdapter.getState() == btAdapter.STATE_OFF){
						turnOnBT();
					}
				}

			}
		};

		registerReceiver(receiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(receiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, filter);
	}
	private void startDiscovery() {
		// TODO Auto-generated method stub
		btAdapter.cancelDiscovery();
		btAdapter.startDiscovery();
		
	}
	private void turnOnBT() {
		// TODO Auto-generated method stub
		Intent intent =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, 1);
	}
	private void getPairedDevices() {
		// TODO Auto-generated method stub
		devicesArray = btAdapter.getBondedDevices();
		if(devicesArray.size()>0) {
			for (BluetoothDevice device : devicesArray) {
				pairedDevices.add(device.getName());
			}
		}
		return;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//unregisterReceiver(receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			// TODO Auto-generated method stub
			super.onActivityResult(requestCode, resultCode, data);
			if(resultCode == RESULT_CANCELED){
				Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			
			if(btAdapter.isDiscovering()){
				btAdapter.cancelDiscovery();
			}
			//if(listAdapter.getItem(arg2).contains("Paired")){

				BluetoothDevice selectedDevice = devices.get(arg2);
				ConnectThread connect = new ConnectThread(selectedDevice);
				connect.start();
				Log.i(tag, "in click listener");
			//}
			/*else{
				Toast.makeText(getApplicationContext(), "device is not paired", Toast.LENGTH_LONG).show();
			}*/
		}
		
		public class ConnectThread extends Thread {
		
			private final BluetoothSocket mmSocket;
		    private final BluetoothDevice mmDevice;
			String chk_blue = "Check bluetooth status on robot";


			public ConnectThread(BluetoothDevice device) {
				// Use a temporary object that is later assigned to mmSocket,
				// because mmSocket is final
				BluetoothSocket tmp = null;
				mmDevice = device;
				Log.i(tag, "In connectThread");
				// Get a BluetoothSocket to connect with the given BluetoothDevice
				try {
					// MY_UUID is the app's UUID string, also used by the server code
					tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
					Log.i(tag,"Got Socket");
				} catch (IOException e) {
					Log.i(tag, "get socket failed");

				}
				mmSocket = tmp;
			}

			public void run() {
				// Cancel discovery because it will slow down the connection
				btAdapter.cancelDiscovery();
				Log.i(tag, "connect - run");
				try {
					// Connect the device through the socket. This will block
					// until it succeeds or throws an exception
					mmSocket.connect();
					Log.i(tag, "connect - succeeded");

				} catch (IOException connectException) {
					Log.i(tag, "connect failed");
					// Unable to connect; close the socket and get out
					try {
						mmSocket.close();
						mHandler.obtainMessage(CHK_BLUE,chk_blue).sendToTarget();
					} catch (IOException closeException) { }
					return;
				}

				// Do work to manage the connection (in a separate thread)

				mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
			}



			/** Will cancel an in-progress connection, and close the socket */
			public void cancel() {
				try {
					mmSocket.close();
				} catch (Exception e) {
					Log.i(tag,"Can not close :"+e.getMessage());
				}
			}
		}


	public class ConnectedThread extends Thread {
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		private final BluetoothSocket mmSocket;

		public ConnectedThread(BluetoothSocket socket) {

				mmSocket = socket;

			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) { }

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer;  // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					buffer = new byte[1024];
					bytes = mmInStream.read(buffer);
					// Send the obtained bytes to the UI activity
					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
							.sendToTarget();
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) { }
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
					mmSocket.close();
			} catch (IOException e) {
				Toast.makeText(getApplication(),"could not disconnect",Toast.LENGTH_LONG).show();
			}
		}
	}
}