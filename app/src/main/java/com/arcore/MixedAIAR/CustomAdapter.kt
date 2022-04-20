package com.arcore.MixedAIAR;

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AbsListView.CHOICE_MODE_SINGLE
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException

/**
 * Adapter for RecyclerView to populate ai_settings_card_view_design.xml
 * Heavy lifting for instantiating the producer:consumer (BitmapSource:BitmapCollector) relationship
 *
 * Holds the BitmapCollector, changes to the ImageClassifier model happen here
 */
class CustomAdapter(var mList: MutableList<ItemsViewModel>, val streamSource: DynamicBitmapSource/*BitmapSource*/, val activity: Activity) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the ai_settings_card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ai_settings_card_view_design, parent, false)
        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel = mList[position]
        holder.modelListView.choiceMode = CHOICE_MODE_SINGLE
        holder.deviceListView.choiceMode = CHOICE_MODE_SINGLE
        val modelAdapter = ArrayAdapter(
            holder.modelListView.context,
            R.layout.ai_settings_listview_row,
            R.id.listview_row_text,
            itemsViewModel.models
        )
        val deviceAdapter = ArrayAdapter(
            holder.modelListView.context,
            R.layout.ai_settings_listview_row,
            R.id.listview_row_text,
            itemsViewModel.devices
        )

        holder.modelListView.adapter = modelAdapter
        holder.deviceListView.adapter = deviceAdapter
        holder.numberPicker.minValue = 1
        holder.numberPicker.maxValue = 10
        holder.numberPicker.wrapSelectorWheel = true
        holder.modelListView.setItemChecked(0, true)
        holder.deviceListView.setItemChecked(0, true)


        // set current consumer to device[0] and model[0] from ItemsViewModel
        initializeActiveModel(itemsViewModel)
        itemsViewModel.classifier?.numThreads = holder.numberPicker.value

        // update consumer when new options are selected
        holder.numberPicker.setOnValueChangedListener {
                picker, oldVal, newVal -> updateActiveModel(holder, itemsViewModel)
        }
        holder.modelListView.setOnItemClickListener {
                parent, view, pos, id -> updateActiveModel(holder, itemsViewModel)
        }
        holder.deviceListView.setOnItemClickListener {
                parent, view, pos, id -> updateActiveModel(holder, itemsViewModel)
        }
        // display current model info
//        holder.textAiInfo.text = "${itemsViewModel.classifier?.modelName} ${itemsViewModel.classifier?.device}"
        holder.textAiInfo.text = "Threads: ${itemsViewModel.classifier?.numThreads}\n" +
                "Model: ${itemsViewModel.classifier?.modelName}\n" +
                "Device: ${itemsViewModel.classifier?.device}"
    }

    /**
     * return amount of items in the recyclerview list
     */
    override fun getItemCount(): Int {
        return mList.size
    }

    /**
     * Holds the views for adding it to image and text
     */
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        var modelListView: ListView = itemView.findViewById(R.id.model)
        val deviceListView: ListView = itemView.findViewById(R.id.device)
        val numberPicker: NumberPicker= itemView.findViewById(R.id.numberPicker_aiThreadCount)
        val textAiInfo : TextView = itemView.findViewById(R.id.textView_aiModelInfo)
    }

    /**
     * Initializes model to default parameters
     */
    fun initializeActiveModel(itemsView: ItemsViewModel) {
        itemsView.currentModel = 0
        itemsView.currentDevice = 0
        itemsView.currentNumThreads = 1
        itemsView.classifier=ImageClassifierFloatMobileNet(activity)
        itemsView.consumer = BitmapCollector(streamSource, itemsView.classifier, activity)
        itemsView.classifier?.useCPU()
    }

    /**
     * Updates model to parameters chosen by pressing ai_settings_card_view_design
     * Stops collector, stops bitmap stream
     */
    fun updateActiveModel(holder: ViewHolder, itemsView : ItemsViewModel) {
        val switchToggleStream = activity.findViewById<Switch>(R.id.switch_streamToggle)

        itemsView.consumer?.pauseCollect()
        switchToggleStream.isChecked = false


        // Get UI information before delegating to background
        val modelIndex: Int = holder.modelListView.checkedItemPosition
        val deviceIndex: Int = holder.deviceListView.checkedItemPosition
        val numThreads: Int = holder.numberPicker.value

        // Do not update if there is no change
        if (modelIndex == itemsView.currentModel
            && deviceIndex == itemsView.currentDevice
            && numThreads == itemsView.currentNumThreads) {
            return
        }
        itemsView.currentModel = modelIndex
        itemsView.currentDevice = deviceIndex
        itemsView.currentNumThreads = numThreads


        // Disable classifier while updating
        if (itemsView.classifier != null) {
            itemsView.classifier?.close()
            itemsView.classifier = null
        }

        // Lookup names of parameters.
        val model: String = itemsView.models[itemsView.currentModel]
        val device: String = itemsView.devices[itemsView.currentDevice]
        val threads = itemsView.currentNumThreads

        Log.i("Custom Adapter",
            "Changing model to $model device $device"
        )

        // Try to load model.
        try {
            when(model) {
                itemsView.models[0]->itemsView.classifier=ImageClassifierFloatMobileNet(activity)
                itemsView.models[1]->itemsView.classifier=ImageClassifierQuantizedMobileNet(activity)
            }
        } catch (e: IOException) {
            Log.d(
                "Custom Adapter",
                "Failed to load",
                e
            )
            itemsView.classifier = null
        }

        when(device) {
            itemsView.devices[0]-> itemsView.classifier?.useCPU()
            itemsView.devices[1]-> itemsView.classifier?.useGpu()
            itemsView.devices[2]-> itemsView.classifier?.useNNAPI()
        }

        itemsView.classifier?.numThreads = threads
        holder.textAiInfo.text = "Threads: ${itemsView.classifier?.numThreads}\n" +
                "Model: ${itemsView.classifier?.modelName}\n" +
                "Device: ${itemsView.classifier?.device}"
        itemsView.setCollector(BitmapCollector(streamSource, itemsView.classifier, activity))
    }


}
