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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + source.title)
                .setItems(R.array.update_freq_values, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which){
                            case 0:
                                source.updateFrequency = 3600000;
                                source.changeBackgroundUpdateFrequency();
                                break;
                            case 1:
                                source.updateFrequency = 7200000;
                                source.changeBackgroundUpdateFrequency();
                                break;
                            case 2:
                                source.updateFrequency = 10800000;
                                source.changeBackgroundUpdateFrequency();
                                break;
                            case 3:
                                source.updateFrequency = 21600000;
                                source.changeBackgroundUpdateFrequency();
                                break;
                            case 4:
                                source.updateFrequency = 43200000;
                                source.changeBackgroundUpdateFrequency();
                                break;
                            case 5:
                                source.updateFrequency = 86400000;
                                source.changeBackgroundUpdateFrequency();
                                break;
                            case 6:
                                source.updateFrequency = 900000;
                                source.changeBackgroundUpdateFrequency();
                                break;
                        }
                    }
                })
        .setCancelable(true)
        .setNegativeButton(R.string.cancel, null);
        return builder.create();
    }

    void show(){
        show(fragmentManager, "ChooseUpdateFrequencyDialog");
    }

}
