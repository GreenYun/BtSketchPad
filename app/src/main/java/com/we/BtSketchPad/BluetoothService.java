package com.we.BtSketchPad;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

class BluetoothService {

	private static BluetoothAdapter bluetoothAdapter;
	private static BluetoothDevice bluetoothDevice;
	private static BluetoothSocket bluetoothSocket;

	public static BluetoothAdapter getBluetoothAdapter() {
		return bluetoothAdapter;
	}

	public static void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
		BluetoothService.bluetoothAdapter = bluetoothAdapter;
	}

	public static BluetoothDevice getBluetoothDevice() {
		return bluetoothDevice;
	}

	public static void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
		BluetoothService.bluetoothDevice = bluetoothDevice;
	}

	public static BluetoothSocket getBluetoothSocket() {
		return bluetoothSocket;
	}

	public static void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
		BluetoothService.bluetoothSocket = bluetoothSocket;
	}

}
