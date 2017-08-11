package com.timkonieczny.rss;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

public class ChooseUpdateFrequencyDialog extends DialogFragment {

    FragmentManager fragmentManager;
    Source source;
    int selectedItem = -1;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final long[] updateFrequencies = new long[]{3600000, 7200000, 10800000, 21600000, 43200000, 86400000, 900000};

        for(int i = 0; i < updateFrequencies.length; i++){
            if(updateFrequencies[i] == source.updateFrequency){
                selectedItem = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + source.title)
        .setCancelable(true)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                source.updateFrequency = updateFrequencies[selectedItem];
                source.changeBackgroundUpdateFrequency();
            }
        })
        .setNegativeButton(R.string.cancel, null)
        .setSingleChoiceItems(R.array.update_freq_values, selectedItem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                selectedItem = which;
            }
        });
        return builder.create();
    }

    void show(){
        show(fragmentManager, "ChooseUpdateFrequencyDialog");
    }

}
