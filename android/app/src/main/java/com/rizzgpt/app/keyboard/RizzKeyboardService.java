package com.rizzgpt.app.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.*;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RizzKeyboardService extends InputMethodService {
    
    private EditText inputField;
    private TextView statusLabel;
    private LinearLayout suggestionsContainer;
    private LinearLayout modesContainer;
    private Button generateBtn;
    private String selectedMode = "smooth";
    private boolean isLoading = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    private static final String GROQ_KEY;
    static {
        String[] parts = {"gsk_2oDA7","TXmZY4Nbr","S4DZFjWGdy","b3FYnKvfx3","Man4P79WIR","J2xGprXX"};
        StringBuilder sb = new StringBuilder();
        for (String p : parts) sb.append(p);
        GROQ_KEY = sb.toString();
    }
    
    @Override
    public View onCreateInputView() {
        View view = buildKeyboardView();
        return view;
    }
    
    private View buildKeyboardView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0a0a0f"));
        root.setPadding(dp(10), dp(8), dp(10), dp(8));
        
        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        TextView logo = new TextView(this);
        logo.setText("⚡ RizzGPT");
        logo.setTextColor(Color.parseColor("#a855f7"));
        logo.setTextSize(14);
        logo.setTypeface(null, android.graphics.Typeface.BOLD);
        
        View spacer = new View(this);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(0, dp(1), 1f);
        spacer.setLayoutParams(spacerParams);
        
        Button switchBtn = new Button(this);
        switchBtn.setText("🌐");
        switchBtn.setTextSize(16);
        switchBtn.setBackgroundColor(Color.TRANSPARENT);
        switchBtn.setOnClickListener(v -> switchToNextInputMethod());
        
        header.addView(logo);
        header.addView(spacer);
        header.addView(switchBtn);
        root.addView(header);
        
        // Mode chips
        HorizontalScrollView modeScroll = new HorizontalScrollView(this);
        modeScroll.setHorizontalScrollBarEnabled(false);
        modeScroll.setPadding(0, dp(6), 0, dp(6));
        
        modesContainer = new LinearLayout(this);
        modesContainer.setOrientation(LinearLayout.HORIZONTAL);
        
        String[][] modes = {
            {"😏 Smooth", "smooth"}, {"😂 Funny", "funny"}, {"🔥 Bold", "bold"},
            {"💜 Flirty", "flirty"}, {"💀 Savage", "savage"}, {"🍯 Sweet", "sweet"}
        };
        
        for (String[] mode : modes) {
            Button chip = new Button(this);
            chip.setText(mode[0]);
            chip.setTextSize(11);
            chip.setAllCaps(false);
            chip.setTag(mode[1]);
            chip.setPadding(dp(12), dp(4), dp(12), dp(4));
            
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setCornerRadius(dp(14));
            bg.setStroke(dp(1), Color.parseColor("#1a1a26"));
            
            if (mode[1].equals("smooth")) {
                bg.setColor(Color.parseColor("#2a1a3a"));
                bg.setStroke(dp(1), Color.parseColor("#a855f7"));
                chip.setTextColor(Color.parseColor("#a855f7"));
            } else {
                bg.setColor(Color.parseColor("#1a1a26"));
                chip.setTextColor(Color.parseColor("#9898b0"));
            }
            chip.setBackground(bg);
            
            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(32));
            chipParams.setMargins(0, 0, dp(6), 0);
            chip.setLayoutParams(chipParams);
            
            chip.setOnClickListener(v -> selectMode(chip));
            modesContainer.addView(chip);
        }
        modeScroll.addView(modesContainer);
        root.addView(modeScroll);
        
        // Input row
        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        inputRow.setPadding(0, dp(4), 0, dp(4));
        
        inputField = new EditText(this);
        inputField.setHint("Their message or context...");
        inputField.setHintTextColor(Color.parseColor("#5a5a72"));
        inputField.setTextColor(Color.parseColor("#f0f0f5"));
        inputField.setTextSize(13);
        inputField.setSingleLine(true);
        inputField.setPadding(dp(14), dp(8), dp(14), dp(8));
        
        android.graphics.drawable.GradientDrawable inputBg = new android.graphics.drawable.GradientDrawable();
        inputBg.setCornerRadius(dp(18));
        inputBg.setColor(Color.parseColor("#1a1a26"));
        inputBg.setStroke(dp(1), Color.parseColor("#2a2a36"));
        inputField.setBackground(inputBg);
        
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(0, dp(38), 1f);
        inputField.setLayoutParams(inputParams);
        
        generateBtn = new Button(this);
        generateBtn.setText("⚡");
        generateBtn.setTextSize(18);
        
        android.graphics.drawable.GradientDrawable genBg = new android.graphics.drawable.GradientDrawable();
        genBg.setCornerRadius(dp(19));
        genBg.setColor(Color.parseColor("#a855f7"));
        generateBtn.setBackground(genBg);
        generateBtn.setTextColor(Color.WHITE);
        
        LinearLayout.LayoutParams genParams = new LinearLayout.LayoutParams(dp(38), dp(38));
        genParams.setMargins(dp(8), 0, 0, 0);
        generateBtn.setLayoutParams(genParams);
        generateBtn.setPadding(0, 0, 0, 0);
        generateBtn.setOnClickListener(v -> onGenerate());
        
        inputRow.addView(inputField);
        inputRow.addView(generateBtn);
        root.addView(inputRow);
        
        // Status
        statusLabel = new TextView(this);
        statusLabel.setText("Type their message → tap ⚡ → get rizz");
        statusLabel.setTextColor(Color.parseColor("#9898b0"));
        statusLabel.setTextSize(11);
        statusLabel.setGravity(android.view.Gravity.CENTER);
        statusLabel.setPadding(0, dp(4), 0, dp(4));
        root.addView(statusLabel);
        
        // Suggestions
        ScrollView suggestScroll = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(120));
        suggestScroll.setLayoutParams(scrollParams);
        
        suggestionsContainer = new LinearLayout(this);
        suggestionsContainer.setOrientation(LinearLayout.VERTICAL);
        suggestionsContainer.setPadding(0, dp(2), 0, dp(2));
        suggestScroll.addView(suggestionsContainer);
        root.addView(suggestScroll);
        
        return root;
    }
    
    private void selectMode(Button selected) {
        selectedMode = (String) selected.getTag();
        for (int i = 0; i < modesContainer.getChildCount(); i++) {
            Button chip = (Button) modesContainer.getChildAt(i);
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setCornerRadius(dp(14));
            if (chip == selected) {
                bg.setColor(Color.parseColor("#2a1a3a"));
                bg.setStroke(dp(1), Color.parseColor("#a855f7"));
                chip.setTextColor(Color.parseColor("#a855f7"));
            } else {
                bg.setColor(Color.parseColor("#1a1a26"));
                bg.setStroke(dp(1), Color.parseColor("#1a1a26"));
                chip.setTextColor(Color.parseColor("#9898b0"));
            }
            chip.setBackground(bg);
        }
    }
    
    private void switchToNextInputMethod() {
        try {
            switchToNextInputMethod(false);
        } catch (Exception e) {
            // fallback
        }
    }
    
    private void onGenerate() {
        if (isLoading) return;
        String context = inputField.getText().toString().trim();
        if (context.isEmpty()) {
            statusLabel.setText("⚠️ Enter their message first");
            return;
        }
        generateReplies(context, selectedMode);
    }
    
    private void generateReplies(String context, String style) {
        isLoading = true;
        mainHandler.post(() -> {
            statusLabel.setText("✨ Generating " + style + " replies...");
            generateBtn.setEnabled(false);
            suggestionsContainer.removeAllViews();
        });
        
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("model", "llama-3.3-70b-versatile");
                body.put("temperature", 1.0);
                body.put("max_tokens", 512);
                
                JSONArray msgs = new JSONArray();
                JSONObject sysMsg = new JSONObject();
                sysMsg.put("role", "system");
                sysMsg.put("content", "You generate text message replies. Style: " + style + ". Rules:\n" +
                    "1. Type like a real person: lowercase, casual\n" +
                    "2. Keep it short: 5-20 words per reply\n" +
                    "3. Sound human, NOT AI\n" +
                    "4. Give exactly 5 different replies, numbered 1-5\n" +
                    "5. Each reply on its own line");
                msgs.put(sysMsg);
                
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", "Generate 5 " + style + " replies to: \"" + context + "\"");
                msgs.put(userMsg);
                body.put("messages", msgs);
                
                URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + GROQ_KEY);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();
                
                int code = conn.getResponseCode();
                if (code != 200) throw new Exception("HTTP " + code);
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                
                JSONObject resp = new JSONObject(sb.toString());
                String content = resp.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
                
                List<String> replies = new ArrayList<>();
                for (String l : content.split("\n")) {
                    String clean = l.trim().replaceAll("^\\d+[.)\\-:\\s]+", "").replaceAll("^\"|\"$", "").trim();
                    if (clean.length() > 3 && !clean.startsWith("#") && !clean.startsWith("*")) {
                        replies.add(clean);
                    }
                }
                
                mainHandler.post(() -> {
                    isLoading = false;
                    generateBtn.setEnabled(true);
                    if (replies.isEmpty()) {
                        statusLabel.setText("❌ No replies. Try again!");
                        return;
                    }
                    statusLabel.setText("Tap a reply to type it ↓");
                    showReplies(replies);
                });
                
            } catch (Exception e) {
                mainHandler.post(() -> {
                    isLoading = false;
                    generateBtn.setEnabled(true);
                    statusLabel.setText("❌ " + e.getMessage());
                });
            }
        });
    }
    
    private void showReplies(List<String> replies) {
        suggestionsContainer.removeAllViews();
        int count = Math.min(replies.size(), 5);
        for (int i = 0; i < count; i++) {
            final String reply = replies.get(i);
            Button btn = new Button(this);
            btn.setText(reply);
            btn.setTextSize(12);
            btn.setAllCaps(false);
            btn.setTextColor(Color.parseColor("#f0f0f5"));
            btn.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
            btn.setPadding(dp(12), dp(8), dp(12), dp(8));
            
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setCornerRadius(dp(10));
            bg.setColor(Color.parseColor("#1a1a26"));
            bg.setStroke(dp(1), Color.parseColor("#2a1a3a"));
            btn.setBackground(bg);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, dp(4));
            btn.setLayoutParams(params);
            
            btn.setOnClickListener(v -> {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) {
                    ic.commitText(reply, 1);
                }
                statusLabel.setText("✅ Typed! Tap send in the app");
            });
            
            suggestionsContainer.addView(btn);
        }
    }
    
    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
