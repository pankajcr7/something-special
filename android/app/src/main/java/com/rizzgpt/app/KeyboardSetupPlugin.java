package com.rizzgpt.app;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.List;

@CapacitorPlugin(name = "KeyboardSetup")
public class KeyboardSetupPlugin extends Plugin {

    private static final String KEYBOARD_ID = "com.rizzgpt.app/.keyboard.RizzKeyboardService";

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

        JSObject result = new JSObject();
        result.put("isEnabled", isEnabled);
        result.put("isSelected", isSelected);
        call.resolve(result);
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
}
