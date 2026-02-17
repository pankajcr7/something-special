package com.rizzgpt.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.text.TextUtils;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.List;

@CapacitorPlugin(name = "KeyboardSetup")
public class KeyboardSetupPlugin extends Plugin {

    private static final String KEYBOARD_ID = "com.rizzgpt.app/.keyboard.RizzKeyboardService";
    private static final String ACCESSIBILITY_ID = "com.rizzgpt.app/.keyboard.RizzAccessibilityService";

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
}
