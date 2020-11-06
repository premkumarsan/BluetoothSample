package com.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.bluetooth.databinding.ListItemBinding


/**
 * Created by Prem Kumar on 05-11-2020.
 */
class DeviceListAdapter(
    private val context: Context,
    private val data: ArrayList<BluetoothDevice>
) : BaseAdapter() {

    private var mListener: OnPairButtonClickListener? = null
    private var mInflater: LayoutInflater? = null

    init {
        mInflater = LayoutInflater.from(context)
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    fun setListener(listener: OnPairButtonClickListener) {
        mListener = listener
    }

    class ViewHolder() {
        var nameTv: TextView? = null
        var addressTv: TextView? = null
        var pairBtn: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = mInflater?.inflate(R.layout.list_item, parent, false)!!
            holder = ViewHolder()

            holder.nameTv = view.findViewById(R.id.tv_name) as TextView
            holder.addressTv = view.findViewById(R.id.tv_address) as TextView
            holder.pairBtn = view.findViewById(R.id.btn_pair) as Button

            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val device: BluetoothDevice = data[position]

        holder.nameTv!!.text = device.name
        holder.addressTv!!.text = device.address
        holder.pairBtn!!.text =
            if (device.bondState == BluetoothDevice.BOND_BONDED) "Unpair" else "Pair"
        holder.pairBtn!!.setOnClickListener {
            if (mListener != null) {
                mListener?.onPairButtonClick(position)
            }
        }


        return view

    }


    interface OnPairButtonClickListener {
        fun onPairButtonClick(position: Int)
    }
}