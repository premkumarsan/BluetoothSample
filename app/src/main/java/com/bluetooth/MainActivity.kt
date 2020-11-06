package com.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bluetooth.DeviceListAdapter.OnPairButtonClickListener
import com.bluetooth.databinding.ActivityMainBinding
import java.lang.reflect.Method


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var pairedDevices: Set<BluetoothDevice>? = null
    private var list = ArrayList<Any>()
    private var adapter: DeviceListAdapter? = null
    private var deviceList = ArrayList<BluetoothDevice>()

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()


        adapter?.setListener(object : OnPairButtonClickListener {
            override fun onPairButtonClick(position: Int) {
                val device: BluetoothDevice = deviceList.get(position)
                if (device.bondState == BluetoothDevice.BOND_BONDED) {
                    unpairDevice(device)
                } else {
                    showToast("Pairing...")
                    pairDevice(device)
                }
            }
        })


        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        val filter1 = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(mPairReceiver, filter1)





    }

    private fun initialize() {
        adapter = DeviceListAdapter(this,deviceList)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            binding.button.isEnabled = false;
            binding.button2.isEnabled = false;
            binding.button3.isEnabled = false;
            binding.button4.isEnabled = false;

            Toast.makeText(
                applicationContext, "Your device does not support Bluetooth",
                Toast.LENGTH_LONG
            ).show();
        }

    }


    fun on(v: View?) {
        if (!bluetoothAdapter?.isEnabled!!) {
            val turnOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(turnOn, REQUEST_ENABLE_BT)
            Toast.makeText(applicationContext, "Turned on", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(applicationContext, "Already on", Toast.LENGTH_LONG).show()
        }
    }

    fun off(v: View?) {
        bluetoothAdapter?.disable()
        Toast.makeText(applicationContext, "Turned off", Toast.LENGTH_LONG).show()
    }


    fun visible(v: View?) {
        val discoverableIntent: Intent =
            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
        startActivityForResult(discoverableIntent, 0)
    }


    fun list(v: View?) {
        pairedDevices = bluetoothAdapter?.bondedDevices
        deviceList.clear()
        list.clear()
        for (device in pairedDevices!!) {
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            list.add("$deviceName $deviceHardwareAddress")
            deviceList.add(device)
        }
        Toast.makeText(applicationContext, "Showing Paired Devices", Toast.LENGTH_SHORT).show()

        binding.listView.adapter = adapter
    }


    private fun pairDevice(device: BluetoothDevice) {
        try {
            val method: Method =
                device.javaClass.getMethod("createBond")
            method.invoke(device)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unpairDevice(device: BluetoothDevice) {
        try {
            val method: Method =
                device.javaClass.getMethod("removeBond")
            method.invoke(device)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_ENABLE_BT) {
            if (bluetoothAdapter?.isEnabled!!) {
                Toast.makeText(applicationContext, "Status: Enabled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, "Status: Disabled", Toast.LENGTH_LONG).show()
            }
        }
    }


    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address

                    list.add("$deviceName $deviceHardwareAddress")
                    if (device != null) {
                        deviceList.add(device)
                    }
                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }


    private val mPairReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val state =
                    intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                val prevState = intent.getIntExtra(
                    BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                    BluetoothDevice.ERROR
                )
                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    showToast("Paired")
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    showToast("Unpaired")
                }
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
        unregisterReceiver(mPairReceiver);
    }

}