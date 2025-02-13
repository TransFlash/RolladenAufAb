package com.wb.rolladenaufab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent updateIntent = new Intent(context, MainActivity.class);
        updateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(updateIntent);
    }
}