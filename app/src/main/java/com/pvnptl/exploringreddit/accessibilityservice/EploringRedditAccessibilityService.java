package com.pvnptl.exploringreddit.accessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class EploringRedditAccessibilityService extends AccessibilityService {
    private static String TAG = "EploringRedditAccessibilityService";

    private static final int POST_NOTIFICATION = 0;
    private static final int UPDATE_NOTIFICATION = 1;
    private static final int CLEAR_NOTIFICATION = 2;

    private static final int EVENT_NOTIFICATION_TIMEOUT_MILLIS = 80;

    private static final String CLASS_NAME_EDIT_TEXT = "android.widget.EditText";

    private static final String[] PACKAGE_NAMES = new String[]{
            "com.android.chrome"
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //Log.i(TAG, AccessibilityEvent.eventTypeToString(event.getEventType()));
        //if (AccessibilityEvent.eventTypeToString(event.getEventType()).contains("WINDOW")) {
        AccessibilityNodeInfo nodeInfo = event.getSource();
        dfs(nodeInfo, event.getClassName().toString(), AccessibilityEvent.eventTypeToString(event.getEventType()));
        // }
    }

    @Override
    public void onInterrupt() {
    }

    public void dfs(AccessibilityNodeInfo info, String className, String eventType) {
        if (info == null) {
            Log.i(TAG, "Info is null");
            return;
        }
        if (info.getText() != null && info.getText().length() > 0 && CLASS_NAME_EDIT_TEXT.equals(className)) {
            Log.i(TAG, "window id: " + info.getWindowId() + ", " + info.getText() + " class: " + info.getClassName());
            //Log.i(TAG, info.toString());
        }
        for (int i = 0; i < info.getChildCount(); i++) {
            dfs(info.getChild(i), className, eventType);
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case POST_NOTIFICATION:

                    return;
                case UPDATE_NOTIFICATION:

                    return;
                case CLEAR_NOTIFICATION:

                    return;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // We are interested in all types of accessibility events.
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        // We want to provide specific type of feedback.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        // We want to receive events in a certain interval.
        info.notificationTimeout = EVENT_NOTIFICATION_TIMEOUT_MILLIS;
        // We want to receive accessibility events only from certain packages.
        info.packageNames = PACKAGE_NAMES;
        setServiceInfo(info);

    }
}
