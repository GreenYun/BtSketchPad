package com.we.BtSketchPad;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.ArrayList;

class BluetoothService {

	private static BluetoothAdapter bluetoothAdapter;
	private static BluetoothDevice bluetoothDevice;
	private static BluetoothSocket bluetoothSocket;
	private static ArrayList<BluetoothDevice> bluetoothDeviceArrayList
		= new ArrayList<>();

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

	public static ArrayList<BluetoothDevice> getBluetoothDeviceArrayList() {
		return bluetoothDeviceArrayList;
	}

	public static void setBluetoothDeviceArrayList(ArrayList<BluetoothDevice> bluetoothDeviceArrayList) {
		BluetoothService.bluetoothDeviceArrayList = bluetoothDeviceArrayList;
	}

}
