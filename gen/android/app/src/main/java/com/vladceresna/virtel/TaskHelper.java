package com.vladceresna.virtel;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Color;

public final class TaskHelper {
    public static void setTaskColor(Activity activity, int color) {
        ActivityManager.TaskDescription td =
                new ActivityManager.TaskDescription(
                        activity.getTitle().toString(),
                        null,
                        color
                );
        activity.setTaskDescription(td);
    }
}
