package com.rizzgpt.app.keyboard;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RizzAccessibilityService extends AccessibilityService {

    private static final String PREFS_NAME = "rizzgpt_screen_reader";
    private static final String KEY_MESSAGES = "screen_messages";
    private static final String KEY_TIMESTAMP = "last_read_time";
    private static final String KEY_APP = "last_app";
    private static final int MAX_MESSAGES = 30;

    private static RizzAccessibilityService instance;
    private String lastCapturedText = "";

    public static RizzAccessibilityService getInstance() {
        return instance;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            | AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            | AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        info.notificationTimeout = 200;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        String packageName = "";
        if (event.getPackageName() != null) {
            packageName = event.getPackageName().toString();
        }

        if (!isChatApp(packageName)) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        List<String> texts = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        extractTexts(root, texts, seen, 0);
        root.recycle();

        if (texts.isEmpty()) return;

        List<String> messages = filterChatMessages(texts);
        if (messages.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, messages.size() - MAX_MESSAGES);
        for (int i = start; i < messages.size(); i++) {
            if (sb.length() > 0) sb.append("\n---MSG---\n");
            sb.append(messages.get(i));
        }

        String captured = sb.toString();
        if (!captured.equals(lastCapturedText) && captured.length() > 5) {
            lastCapturedText = captured;
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit()
                .putString(KEY_MESSAGES, captured)
                .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                .putString(KEY_APP, getAppLabel(packageName))
                .apply();
        }
    }

    private boolean isChatApp(String pkg) {
        return pkg.contains("instagram") ||
            pkg.contains("snapchat") ||
            pkg.contains("whatsapp") ||
            pkg.contains("telegram") ||
            pkg.contains("messenger") ||
            pkg.contains("tinder") ||
            pkg.contains("bumble") ||
            pkg.contains("hinge") ||
            pkg.contains("signal") ||
            pkg.contains("discord") ||
            pkg.contains("twitter") ||
            pkg.contains("threads") ||
            pkg.contains("viber") ||
            pkg.contains("line.android") ||
            pkg.contains("wechat") ||
            pkg.contains("kakaotalk") ||
            pkg.contains("skype") ||
            pkg.contains("dating") ||
            pkg.contains("chat") ||
            pkg.contains("messages") ||
            pkg.contains("sms") ||
            pkg.contains("mms");
    }

    private String getAppLabel(String pkg) {
        if (pkg.contains("instagram")) return "Instagram";
        if (pkg.contains("snapchat")) return "Snapchat";
        if (pkg.contains("whatsapp")) return "WhatsApp";
        if (pkg.contains("telegram")) return "Telegram";
        if (pkg.contains("messenger")) return "Messenger";
        if (pkg.contains("tinder")) return "Tinder";
        if (pkg.contains("bumble")) return "Bumble";
        if (pkg.contains("hinge")) return "Hinge";
        if (pkg.contains("signal")) return "Signal";
        if (pkg.contains("discord")) return "Discord";
        if (pkg.contains("twitter")) return "Twitter/X";
        if (pkg.contains("threads")) return "Threads";
        return "Chat";
    }

    private void extractTexts(AccessibilityNodeInfo node, List<String> texts, Set<String> seen, int depth) {
        if (node == null || depth > 20) return;

        CharSequence text = node.getText();
        if (text != null && text.length() > 0) {
            String t = text.toString().trim();
            if (t.length() > 1 && !seen.contains(t)) {
                seen.add(t);
                texts.add(t);
            }
        }

        CharSequence desc = node.getContentDescription();
        if (desc != null && desc.length() > 0) {
            String d = desc.toString().trim();
            if (d.length() > 5 && !seen.contains(d)) {
                seen.add(d);
                texts.add(d);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                extractTexts(child, texts, seen, depth + 1);
                child.recycle();
            }
        }
    }

    private List<String> filterChatMessages(List<String> texts) {
        List<String> messages = new ArrayList<>();
        for (String text : texts) {
            if (isLikelyMessage(text)) {
                messages.add(text);
            }
        }
        return messages;
    }

    private boolean isLikelyMessage(String text) {
        if (text.length() < 2 || text.length() > 2000) return false;

        String lower = text.toLowerCase();
        if (lower.equals("send") || lower.equals("camera") || lower.equals("gallery") ||
            lower.equals("search") || lower.equals("back") || lower.equals("more") ||
            lower.equals("home") || lower.equals("explore") || lower.equals("reels") ||
            lower.equals("profile") || lower.equals("like") || lower.equals("share") ||
            lower.equals("comment") || lower.equals("follow") || lower.equals("following") ||
            lower.equals("followers") || lower.equals("message") || lower.equals("messages") ||
            lower.equals("online") || lower.equals("active now") || lower.equals("typing...") ||
            lower.equals("seen") || lower.equals("delivered") || lower.equals("sent") ||
            lower.equals("today") || lower.equals("yesterday") || lower.equals("new message")) {
            return false;
        }

        if (text.matches("^\\d{1,2}:\\d{2}.*") || text.matches("^\\d{1,2}/\\d{1,2}/.*") ||
            text.matches("^\\d+[hm] ago$") || text.matches("^(Mon|Tue|Wed|Thu|Fri|Sat|Sun).*")) {
            return false;
        }

        if (text.matches("^\\d+$") && text.length() < 5) return false;

        if (text.length() >= 3) return true;

        return false;
    }

    public String getLastMessages() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_MESSAGES, "");
    }

    public String getLastApp() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_APP, "");
    }

    public long getLastTimestamp() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getLong(KEY_TIMESTAMP, 0);
    }

    public static String readScreenMessages(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_MESSAGES, "");
    }

    public static String readLastApp(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_APP, "");
    }

    public static long readTimestamp(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getLong(KEY_TIMESTAMP, 0);
    }

    @Override
    public void onInterrupt() {
        instance = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }
}
