package com.pvnptl.exploringreddit.accessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class EploringRedditAccessibilityService extends AccessibilityService {
    private static String TAG = "EploringRedditAccessibilityService";

    private static final int POST_NOTIFICATION = 0;
    private static final int UPDATE_NOTIFICATION = 1;
    private static final int CLEAR_NOTIFICATION = 2;

    private static final int EVENT_NOTIFICATION_TIMEOUT_MILLIS = 0;

    private static final String CLASS_NAME_EDIT_TEXT = "android.widget.EditText";

    private static final String[] PACKAGE_NAMES = new String[]{
            "com.android.chrome",     // for chrome application
            "com.android.launcher3"  // For home button press
    };

    private static final String[] SUPPORTED_SUB_NAMES = new String[]{
            "all",
            "alternativeart",
            "aww",
            "adviceanimals",
            "cats",
            "gifs",
            "images",
            "photoshopbattles",
            "pics",
            "hmmm"
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo source = event.getSource();
        if (source != null) {
            if (source.getPackageName().toString().equalsIgnoreCase("com.android.launcher3")) {
                handleLauncherApplication();
            } else if (source.getPackageName().toString().equalsIgnoreCase("com.android.chrome")) {
                handleChomeApplication(source);
            }
        }
    }

    private void handleLauncherApplication() {
        mHandler.obtainMessage(CLEAR_NOTIFICATION).sendToTarget();
    }

    private void handleChomeApplication(AccessibilityNodeInfo source) {
        depthFirstSearch(source);
    }

    @Override
    public void onInterrupt() {

    }

    public void depthFirstSearch(AccessibilityNodeInfo info) {

        if (info == null) {
            return;
        }

        if (CLASS_NAME_EDIT_TEXT.equals(info.getClassName())) {
            String nodeText = info.getText().toString();

            Uri uri = Uri.parse(nodeText);

            List<String> pathSegments = uri.getPathSegments();

            // Supporting https://m.reddit.com/r/pics/<any random string>
            if (uri.getScheme() != null
                    && uri.getScheme().equalsIgnoreCase("https")
                    && uri.getHost() != null
                    && uri.getHost().equalsIgnoreCase("m.reddit.com")
                    && pathSegments.size() >= 2
                    && isSubSupported(pathSegments.get(0), pathSegments.get(1))) {
                handleRedditUrl(info, nodeText, pathSegments.get(1));
                return;
            }
            mHandler.obtainMessage(CLEAR_NOTIFICATION).sendToTarget();
            return;
        }
        for (int i = 0; i < info.getChildCount(); i++) {
            depthFirstSearch(info.getChild(i));
        }
    }

    private boolean isSubSupported(String pathSegment1, String pathSegment2) {
        // Comparing r/all, r/all?<some query params>
        if (pathSegment1.equalsIgnoreCase("r")) {
            for (int i = 0; i < SUPPORTED_SUB_NAMES.length; i++) {
                if (pathSegment2.equalsIgnoreCase(SUPPORTED_SUB_NAMES[i])) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case POST_NOTIFICATION:
                    String subName = (String) message.obj;
                    Notifier.getInstance().notify(String.format(getString(R.string.noti_message), subName),
                            getString(R.string.tap_to_open),
                            subName,
                            Notifier.TYPE_EXPLORING_REDDIT_SERVICE_RUNNING, true);
                    return;
                case CLEAR_NOTIFICATION:
                    Notifier.getInstance().cancelByType(Notifier.TYPE_EXPLORING_REDDIT_SERVICE_RUNNING);
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
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        // We want to provide specific type of feedback.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        // We want to receive events in a certain interval.
        info.notificationTimeout = EVENT_NOTIFICATION_TIMEOUT_MILLIS;
        // We want to receive accessibility events only from certain packages.
        info.packageNames = PACKAGE_NAMES;
        setServiceInfo(info);

    }

    public void handleRedditUrl(AccessibilityNodeInfo info, String string, String subName) {
        if (info == null)
            return;
        List<AccessibilityNodeInfo> resultList = info.findAccessibilityNodeInfosByText(string);
        if (resultList != null && !resultList.isEmpty()) {
            for (int i = resultList.size() - 1; i >= 0; i--) {
                mHandler.obtainMessage(POST_NOTIFICATION, subName).sendToTarget();
            }
        } else {
            mHandler.obtainMessage(CLEAR_NOTIFICATION).sendToTarget();
        }
        return;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.obtainMessage(CLEAR_NOTIFICATION).sendToTarget();
    }
}
