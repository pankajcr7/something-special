package com.rizzgpt.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@CapacitorPlugin(name = "KeyboardSetup")
public class KeyboardSetupPlugin extends Plugin {

    private static final String KEYBOARD_ID = "com.rizzgpt.app/.keyboard.RizzKeyboardService";
    private static final String CHAT_PREFS = "rizzgpt_chatbrain";

    @PluginMethod
    public void getKeyboardStatus(PluginCall call) {
        Context ctx = getContext();
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> enabledMethods = imm.getEnabledInputMethodList();

        boolean isEnabled = false;
        boolean isSelected = false;

        for (InputMethodInfo info : enabledMethods) {
            if (info.getId().equals(KEYBOARD_ID)) {
                isEnabled = true;
                break;
            }
        }

        String defaultIme = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        if (defaultIme != null && defaultIme.equals(KEYBOARD_ID)) {
            isSelected = true;
        }

        boolean accessibilityEnabled = isAccessibilityEnabled(ctx);

        JSObject result = new JSObject();
        result.put("isEnabled", isEnabled);
        result.put("isSelected", isSelected);
        result.put("accessibilityEnabled", accessibilityEnabled);
        call.resolve(result);
    }

    private boolean isAccessibilityEnabled(Context ctx) {
        AccessibilityManager am = (AccessibilityManager) ctx.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> services = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : services) {
            if (info.getId() != null && info.getId().contains("RizzAccessibilityService")) {
                return true;
            }
        }
        return false;
    }

    @PluginMethod
    public void openInputMethodSettings(PluginCall call) {
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
        call.resolve();
    }

    @PluginMethod
    public void openInputMethodPicker(PluginCall call) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showInputMethodPicker();
        call.resolve();
    }

    @PluginMethod
    public void openAccessibilitySettings(PluginCall call) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
        call.resolve();
    }

    @PluginMethod
    public void openAppSettings(PluginCall call) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
        call.resolve();
    }

    @PluginMethod
    public void saveChatBrain(PluginCall call) {
        String name = call.getString("name", "");
        String messagesJson = call.getString("messages", "[]");
        if (name.isEmpty()) { call.reject("Name required"); return; }

        SharedPreferences prefs = getContext().getSharedPreferences(CHAT_PREFS, Context.MODE_PRIVATE);
        String existing = prefs.getString("chats_index", "[]");
        try {
            JSONArray index = new JSONArray(existing);
            boolean found = false;
            for (int i = 0; i < index.length(); i++) {
                if (index.getString(i).equals(name)) { found = true; break; }
            }
            if (!found) index.put(name);
            prefs.edit()
                .putString("chats_index", index.toString())
                .putString("chat_" + name, messagesJson)
                .putLong("chat_updated_" + name, System.currentTimeMillis())
                .apply();
        } catch (Exception e) {
            call.reject("Save failed: " + e.getMessage());
            return;
        }
        call.resolve();
    }

    @PluginMethod
    public void getChatBrainList(PluginCall call) {
        SharedPreferences prefs = getContext().getSharedPreferences(CHAT_PREFS, Context.MODE_PRIVATE);
        String indexStr = prefs.getString("chats_index", "[]");
        try {
            JSONArray index = new JSONArray(indexStr);
            JSONArray result = new JSONArray();
            for (int i = 0; i < index.length(); i++) {
                String name = index.getString(i);
                String msgs = prefs.getString("chat_" + name, "[]");
                long updated = prefs.getLong("chat_updated_" + name, 0);
                JSONArray msgArr = new JSONArray(msgs);
                JSONObject item = new JSONObject();
                item.put("name", name);
                item.put("messageCount", msgArr.length());
                item.put("updated", updated);
                if (msgArr.length() > 0) {
                    item.put("lastMessage", msgArr.getString(msgArr.length() - 1));
                }
                result.put(item);
            }
            JSObject res = new JSObject();
            res.put("chats", result.toString());
            call.resolve(res);
        } catch (Exception e) {
            JSObject res = new JSObject();
            res.put("chats", "[]");
            call.resolve(res);
        }
    }

    @PluginMethod
    public void getChatBrainMessages(PluginCall call) {
        String name = call.getString("name", "");
        if (name.isEmpty()) { call.reject("Name required"); return; }
        SharedPreferences prefs = getContext().getSharedPreferences(CHAT_PREFS, Context.MODE_PRIVATE);
        String msgs = prefs.getString("chat_" + name, "[]");
        JSObject res = new JSObject();
        res.put("messages", msgs);
        res.put("updated", prefs.getLong("chat_updated_" + name, 0));
        call.resolve(res);
    }

    @PluginMethod
    public void deleteChatBrain(PluginCall call) {
        String name = call.getString("name", "");
        if (name.isEmpty()) { call.reject("Name required"); return; }
        SharedPreferences prefs = getContext().getSharedPreferences(CHAT_PREFS, Context.MODE_PRIVATE);
        String indexStr = prefs.getString("chats_index", "[]");
        try {
            JSONArray index = new JSONArray(indexStr);
            JSONArray newIndex = new JSONArray();
            for (int i = 0; i < index.length(); i++) {
                if (!index.getString(i).equals(name)) newIndex.put(index.getString(i));
            }
            prefs.edit()
                .putString("chats_index", newIndex.toString())
                .remove("chat_" + name)
                .remove("chat_updated_" + name)
                .apply();
        } catch (Exception e) {}
        call.resolve();
    }
}
