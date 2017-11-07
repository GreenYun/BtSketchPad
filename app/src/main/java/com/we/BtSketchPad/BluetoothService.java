package com.we.BtSketchPad;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

class BluetoothService {

	static final int MSG_DATA_READ          = 0x200;
	static final int MSG_LISTENING_FAILED   = 0x201;
	static final int MSG_LISTENING_STANDBY  = 0x202;
	static final int MSG_FINGER_LEFT        = 0x203;

	private static BluetoothAdapter bluetoothAdapter;
	private static BluetoothDevice bluetoothDevice;
	private static BluetoothSocket bluetoothSocket;
//	private static ArrayList<BluetoothDevice> bluetoothDeviceArrayList
//		= new ArrayList<>();

	private static ListeningThread listeningThread;
//	private static SendingThread sendingThread;

	static BluetoothAdapter getBluetoothAdapter() {
		return bluetoothAdapter;
	}

	static void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
		BluetoothService.bluetoothAdapter = bluetoothAdapter;
	}

	static BluetoothDevice getBluetoothDevice() {
		return bluetoothDevice;
	}

	static void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
		BluetoothService.bluetoothDevice = bluetoothDevice;
	}

	static BluetoothSocket getBluetoothSocket() {
		return bluetoothSocket;
	}

	static void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
		BluetoothService.bluetoothSocket = bluetoothSocket;
	}

//	public static ArrayList<BluetoothDevice> getBluetoothDeviceArrayList() {
//		return bluetoothDeviceArrayList;
//	}

//	public static void setBluetoothDeviceArrayList(ArrayList<BluetoothDevice> bluetoothDeviceArrayList) {
//		BluetoothService.bluetoothDeviceArrayList = bluetoothDeviceArrayList;
//	}

//	public static ListeningThread getListeningThread() {
//		return listeningThread;
//	}

//	public static void setListeningThread(ListeningThread listeningThread) {
//		BluetoothService.listeningThread = listeningThread;
//	}

	static void setNewListeningThread(Handler handler) {
		if (null != listeningThread) {
			listeningThread.stopTask();
			listeningThread = null;
		}
		if (null != handler)
			listeningThread = new ListeningThread(handler);
	}

	static void startListeningThread() {
		if (null != listeningThread) {
			listeningThread.start();
		}
	}

	private static class ListeningThread extends Thread {
		InputStream inputStream;
		Handler mHandler;
		ArrayList<float[]> dataArrayList = new ArrayList<>();

		private volatile boolean isRunning = false;

		ListeningThread(Handler handler) {
			mHandler = handler;
			try {
				inputStream = bluetoothSocket.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
				inputStream = null;
			}
			if (null != mHandler) {
				if (null != inputStream) {
					mHandler.obtainMessage(MSG_LISTENING_STANDBY).sendToTarget();
				}
			}
		}

		@Override
		public void run() {
			if (null == mHandler) {
				return;
			}
			isRunning = true;

			final int bufferLength  = 6;
			final int dataLength    = 2;
			int[] buffer = new int[bufferLength];
			float[] data = new float[dataLength];
			int i = 0;
			DataInputStream dataInputStream = new DataInputStream(inputStream);
			buffer[0] = 0xff;
			while (true) {
				try {
					while (0 == i) {
						if (0 == buffer[0]) {
							++i;
							break;
						}
						buffer[0] = dataInputStream.readUnsignedByte();
						if (0xf1 == buffer[0]) {
							mHandler.obtainMessage(MSG_FINGER_LEFT).sendToTarget();
						}
					}
					for (; i < bufferLength; ++i) {
						buffer[i] = dataInputStream.readUnsignedByte();
					}
					if (!isRunning)
						return;
					if ((0 == buffer[0]) && (0xff == buffer[bufferLength - 1])) {
						for (int j = 0; j < dataLength; ++j)
							data[j] = (buffer[j * 2 + 1] * 256 + buffer[j * 2 + 2]) / 10.0f;
						dataArrayList.add(data);
						mHandler.obtainMessage(MSG_DATA_READ, -1, -1,
							dataArrayList.get(dataArrayList.size() - 1))
							.sendToTarget();
						data = new float[dataLength];
						i = 0;
						buffer[0] = 0xff;
						while (100 < dataArrayList.size())
							dataArrayList.remove(0);
					}
					else {
						int m;
						for (m = bufferLength - 1; m > 0; --m)
							if (0 == buffer[m])
								break;
						for (i = 0; (0 < m) && (m < bufferLength); ++i, ++m)
							buffer[i] = buffer[m];
					}
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
			mHandler.obtainMessage(MSG_LISTENING_FAILED).sendToTarget();
		}

		void stopTask() {
			isRunning = false;
			interrupt();
		}

	}

//	private class SendingThread extends Thread {
//		OutputStream outputStream;
//
//		public SendingThread() {
//			try {
//				outputStream = bluetoothSocket.getOutputStream();
//			} catch (IOException e) {
//				e.printStackTrace();
//				failure();
//			}
//		}
//
//		@Override
//		public void run() {
//
//		}
//
//		void failure() {
//			synchronized (BluetoothService.this) {
//				sendingThread = null;
//			}
//		}
//	}

}
