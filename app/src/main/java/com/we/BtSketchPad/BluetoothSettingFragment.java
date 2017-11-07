package com.we.BtSketchPad;

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

public class BluetoothSettingFragment extends Fragment {

	private static final int MSG_CONNECTED          = 0x101;
	private static final int MSG_CONNECTING         = 0x102;
	private static final int MSG_CONNECTION_FAILED  = 0x103;

	private static final UUID SPP_UUID
		= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	FragmentBroadcastReceiver fragmentBroadcastReceiver
		= new FragmentBroadcastReceiver(this);
	FragmentHandler fragmentHandler = new FragmentHandler(this);
	ConnectThread connectThread;

	ArrayList<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
	BluetoothAdapter bluetoothAdapter;
	BluetoothDevice bluetoothDevice;
	BluetoothSocket bluetoothSocket;
	String strTips = "";
	
	Activity activity;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.bluetooth_setting, container, false);

	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		bluetoothAdapter = BluetoothService.getBluetoothAdapter();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		activity = getActivity();
		if (null != activity) {
			activity.setTitle("Connect");

			ListView listBluetoothDevice
				= activity.findViewById(R.id.listBluetoothDevice);
			listBluetoothDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//				BluetoothDevice device = ;
					bluetoothDevice = bluetoothDeviceList.get(i);
					refreshDeviceListView();
//				BluetoothService.setBluetoothDevice(bluetoothDevice);
					connectBluetoothDevice();
				}
			});
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.bluetooth_setting_action, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();

		switch (itemId) {
			case R.id.action_refresh:
				startBluetoothDiscovery();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onResume() {
 		super.onResume();
//		activity.registerReceiver(fragmentBroadcastReceiver,
//			new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED));
		activity.registerReceiver(fragmentBroadcastReceiver,
			new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		activity.registerReceiver(fragmentBroadcastReceiver,
			new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
		activity.registerReceiver(fragmentBroadcastReceiver,
			new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		activity.registerReceiver(fragmentBroadcastReceiver,
			new IntentFilter(BluetoothDevice.ACTION_FOUND));
		if (BluetoothAdapter.STATE_ON == bluetoothAdapter.getState()) {
			startBluetoothDiscovery();
		}
	}

	@Override
	public void onPause() {
		activity.unregisterReceiver(fragmentBroadcastReceiver);
		super.onPause();
	}

	private void connectBluetoothDevice() {
		if (null != connectThread) {
			connectThread.disconnect(bluetoothSocket);
			connectThread = null;
		}
		if (null != bluetoothSocket) {
			try {
				bluetoothSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		connectThread = new ConnectThread();
		connectThread.start();
	}

	private void refreshDeviceListView() {
		ListView listBluetoothDevice
			= activity.findViewById(R.id.listBluetoothDevice);
		ArrayList<Map<String, String>> bluetoothDeviceShowList =
			new ArrayList<>();
		for (BluetoothDevice device:bluetoothDeviceList) {
			String tmpStr
				= format(Locale.getDefault(), "%s", device.getName());
			Map<String, String> map = new HashMap<>();
			map.put("name", tmpStr);
			if (device == bluetoothDevice) {
				map.put("tips", strTips);
			}
			else {
				map.put("tips", "");
			}
			bluetoothDeviceShowList.add(map);
		}
		listBluetoothDevice.setAdapter(new SimpleAdapter(getContext(),
			bluetoothDeviceShowList,
			R.layout.array_adaptor,
			new String[]{"name", "tips"},
			new int[]{R.id.TextName, R.id.TextTips}));
	}

	private void startBluetoothDiscovery() {
		if (!bluetoothAdapter.isDiscovering()) {
			bluetoothDeviceList.clear();
			refreshDeviceListView();
			bluetoothAdapter.startDiscovery();
			bluetoothDevice = BluetoothService.getBluetoothDevice();
		}
	}

	private void onBluetoothConnectionStateChanged(int connectionState) {
		switch (connectionState) {
			case BluetoothAdapter.STATE_CONNECTED:
				BluetoothService.setBluetoothDevice(bluetoothDevice);
				strTips = "Connected.";
				break;
			case BluetoothAdapter.STATE_CONNECTING:
				strTips = "Connecting...";
				break;
			case BluetoothAdapter.STATE_DISCONNECTED:
				BluetoothService.setBluetoothDevice(null);
				bluetoothDevice = null;
				strTips = "Disconnected";
				break;
			case BluetoothAdapter.STATE_DISCONNECTING:
				strTips = "Disconnecting...";
				break;
			default:
				strTips = "";
				break;
		}
		refreshDeviceListView();
	}

	private void onBluetoothStateChanged(int state) {
		switch (state) {
			case BluetoothAdapter.STATE_ON:
				startBluetoothDiscovery();
				break;
			default:
				break;
		}
	}

	private static final class FragmentBroadcastReceiver extends BroadcastReceiver {
		WeakReference<BluetoothSettingFragment> activityWeakReference;

		FragmentBroadcastReceiver(BluetoothSettingFragment fragment) {
			activityWeakReference = new WeakReference<>(fragment);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			BluetoothSettingFragment fragment = activityWeakReference.get();
//			SketchPadActivity activity = (SketchPadActivity) fragment.activity;
			String intentAction = intent.getAction();
			if (null != intentAction)
				switch (intentAction) {
//				    case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
//				    	int connectionState
//				    		= intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
//				    	fragment.onBluetoothConnectionStateChanged(connectionState);
//				    	break;
					case BluetoothAdapter.ACTION_STATE_CHANGED:
						int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
						fragment.onBluetoothStateChanged(state);
						break;
					case BluetoothDevice.ACTION_FOUND:
						BluetoothDevice bluetoothDevice =
							intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
						if (!fragment.bluetoothDeviceList.contains(bluetoothDevice)) {
							fragment.bluetoothDeviceList.add(bluetoothDevice);
						}
						fragment.refreshDeviceListView();
						break;
					default:
						break;
				}
		}
	}

	private static class FragmentHandler extends Handler {

		WeakReference<BluetoothSettingFragment> activityWeakReference;

		FragmentHandler(BluetoothSettingFragment fragment) {
			activityWeakReference = new WeakReference<>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			BluetoothSettingFragment fragment = activityWeakReference.get();
			Context context = fragment.getContext();
			switch (msg.what) {
				case MSG_CONNECTED:
					BluetoothService.setBluetoothSocket(fragment.bluetoothSocket);
					fragment.onBluetoothConnectionStateChanged(BluetoothAdapter.STATE_CONNECTED);
					BluetoothService.setNewListeningThread(this);
					break;
				case MSG_CONNECTING:
					fragment.onBluetoothConnectionStateChanged(BluetoothAdapter.STATE_CONNECTING);
					break;
				case MSG_CONNECTION_FAILED:
					if (null != context)
						new AlertDialog.Builder(context)
							.setTitle("Failed")
							.setMessage("Failed")
							.setPositiveButton("OK", null)
							.show();
					fragment.onBluetoothConnectionStateChanged(-1);
					break;
				default:
					super.handleMessage(msg);
					fragment.onBluetoothConnectionStateChanged(-1);
					break;
			}
		}
	}

	private class ConnectThread extends Thread {
		BluetoothSocket socket;

		ConnectThread() {
			try {
				socket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
			} catch (IOException e) {
				e.printStackTrace();
				socket = null;
			}
		}

		@Override
		public void run() {
			if (null == socket) {
				failure();
				return;
			}
			fragmentHandler.obtainMessage(MSG_CONNECTING).sendToTarget();
			try {
				socket.connect();
			} catch (IOException e) {
				e.printStackTrace();
				disconnect(socket);
				socket = null;
				failure();
				return;
			}
			bluetoothSocket = socket;
			fragmentHandler.obtainMessage(MSG_CONNECTED).sendToTarget();
			synchronized (BluetoothSettingFragment.this) {
				connectThread = null;
			}
		}

		void disconnect(BluetoothSocket bSocket) {
			if (null != bSocket) {
				try {
					bSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		void failure() {
			fragmentHandler.obtainMessage(MSG_CONNECTION_FAILED).sendToTarget();
			synchronized (BluetoothSettingFragment.this) {
				connectThread = null;
			}
		}
	}
}
