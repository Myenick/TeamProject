package net.mienik.measureit;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;


/**
 * Created by DawidM on 2017-08-09.
 */

class BluetoothService {

    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "MeasureIt";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;
    private ConnectedThread mConnectedThread;

    public BluetoothService(Context mContext) {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mContext = mContext;
    }

    public static boolean isBluetoothEnabled() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return (bluetoothAdapter != null && bluetoothAdapter.isEnabled());
    }

    private class AcceptThread extends Thread {
        //Local server socket
        private final BluetoothServerSocket mServerSocket;

        /**
         * This thread runs while listening for incoming connections. It behaves
         * like a server-side client. It runs until a connection is accepted
         * (or until cancelled).
         */
        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            //Create new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
                Log.d(TAG, "AcceptThread: Setting up server with: " + MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.d(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            mServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start...");

                socket = mServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");

            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            //talk about this is in the 3rd
            if (socket != null) {
                connected(socket, mDevice);
            }

            Log.i(TAG, "END mAcceptThread ");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Closing of AcceptThread failed. " + e.getMessage());
            }
        }


    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            //Get a BluetoothSocket for a connection with the given Bluetooth device
            try {
                Log.e(TAG, "Connect Thread: Trying to create InsecureRfcommSocket using UUID ");
                tmp = mDevice.createInsecureRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "Connect Thread: Could not create INSECURERfcommSocket " + e.getMessage());
            }

            mSocket = tmp;

            //Always cancel discovery cause it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            /*Make connection to BluetoothSocket
             */


            try {
                //This is a blocking call and will only return on a successful connection or an exception
                mSocket.connect();
                Log.d(TAG, "run: ConnectThread connected. ");
            } catch (IOException e) {
                //Close the socket
                try {
                    mSocket.close();
                    Log.d(TAG, "run: Socket closed. ");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE);
            }

            connected(mSocket, mDevice);
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in ConnectThread failed. " + e.getMessage());
            }
        }

        /**
         * Start the chat service. Specifically start AcceptThread to begin a
         * session in listening (server) mode. Called by the Activity onResume()
         */
        public synchronized void start() {
            Log.d(TAG, "Start");

            //Cancel any thread attempting to make a connection
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
            if (mInsecureAcceptThread == null) {
                mInsecureAcceptThread = new AcceptThread();
                mInsecureAcceptThread.start();
            }
        }

        /**
         * AcceptThread starts and sits waiting for a connection.
         * Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
         **/
        public void startClient(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "startClient: Started.");

            //initprocess dialog
            mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please wait...", true);

            mConnectThread = new ConnectThread(device, uuid);
            mConnectThread.start();
        }

    }

    /**
     * Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     * receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established
            try {
                mProgressDialog.dismiss();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    Intent incomingMessageIntent = new Intent("incomingMessages");
                    incomingMessageIntent.putExtra("theMessage", incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);


                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }
}


