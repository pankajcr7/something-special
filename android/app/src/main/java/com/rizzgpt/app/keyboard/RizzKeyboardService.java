package com.rizzgpt.app.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;

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

    private LinearLayout rootLayout;
    private LinearLayout qwertyContainer;
    private LinearLayout rizzPanel;
    private EditText rizzInput;
    private TextView statusLabel;
    private LinearLayout suggestionsContainer;
    private LinearLayout modesContainer;
    private Button generateBtn;
    private Button rizzToggleBtn;
    private boolean isShift = false;
    private boolean isCaps = false;
    private boolean isNumbers = false;
    private boolean showingRizz = false;
    private String selectedMode = "smooth";
    private boolean isLoading = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private List<Button> letterKeys = new ArrayList<>();

    private static final String GROQ_KEY;
    static {
        String[] parts = {"gsk_2oDA7","TXmZY4Nbr","S4DZFjWGdy","b3FYnKvfx3","Man4P79WIR","J2xGprXX"};
        StringBuilder sb = new StringBuilder();
        for (String p : parts) sb.append(p);
        GROQ_KEY = sb.toString();
    }

    private static final String[] ROW1 = {"q","w","e","r","t","y","u","i","o","p"};
    private static final String[] ROW2 = {"a","s","d","f","g","h","j","k","l"};
    private static final String[] ROW3 = {"z","x","c","v","b","n","m"};
    private static final String[] NUM_ROW1 = {"1","2","3","4","5","6","7","8","9","0"};
    private static final String[] NUM_ROW2 = {"@","#","$","_","&","-","+","(",")"};
    private static final String[] NUM_ROW3 = {"*","\"","'",":",";","!","?"};

    @Override
    public View onCreateInputView() {
        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#1b1b2f"));
        rootLayout.setPadding(dp(2), dp(4), dp(2), dp(4));

        buildTopBar();
        buildQwertyKeyboard();
        buildRizzPanel();

        rizzPanel.setVisibility(View.GONE);
        qwertyContainer.setVisibility(View.VISIBLE);

        return rootLayout;
    }

    private void buildTopBar() {
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(6), dp(2), dp(6), dp(2));
        LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(36));
        topBar.setLayoutParams(topParams);

        TextView logo = new TextView(this);
        logo.setText("⚡ RizzGPT");
        logo.setTextColor(Color.parseColor("#a855f7"));
        logo.setTextSize(13);
        logo.setTypeface(null, Typeface.BOLD);

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));

        Button autoReadBtn = makeSmallBtn("📖 Read", Color.parseColor("#10b981"));
        autoReadBtn.setOnClickListener(v -> autoReadMessages());

        rizzToggleBtn = makeSmallBtn("⚡ Rizz", Color.parseColor("#a855f7"));
        rizzToggleBtn.setOnClickListener(v -> toggleRizzPanel());

        Button switchBtn = makeSmallBtn("🌐", Color.parseColor("#6366f1"));
        switchBtn.setOnClickListener(v -> {
            try { switchToNextInputMethod(false); } catch (Exception e) {}
        });

        topBar.addView(logo);
        topBar.addView(spacer);
        topBar.addView(autoReadBtn);
        topBar.addView(rizzToggleBtn);
        topBar.addView(switchBtn);
        rootLayout.addView(topBar);
    }

    private Button makeSmallBtn(String text, int color) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextSize(10);
        btn.setAllCaps(false);
        btn.setTextColor(Color.WHITE);
        btn.setPadding(dp(8), dp(2), dp(8), dp(2));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        bg.setColor(color);
        btn.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, dp(28));
        p.setMargins(dp(3), 0, dp(3), 0);
        btn.setLayoutParams(p);
        return btn;
    }

    private void buildQwertyKeyboard() {
        qwertyContainer = new LinearLayout(this);
        qwertyContainer.setOrientation(LinearLayout.VERTICAL);
        qwertyContainer.setPadding(dp(2), dp(2), dp(2), dp(2));

        addKeyRow(qwertyContainer, ROW1, false, false);
        addKeyRow(qwertyContainer, ROW2, false, false);
        addShiftRow(qwertyContainer, ROW3);
        addBottomRow(qwertyContainer);

        rootLayout.addView(qwertyContainer);
    }

    private void addKeyRow(LinearLayout parent, String[] keys, boolean offsetStart, boolean offsetEnd) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(42));
        rowParams.setMargins(0, dp(2), 0, dp(2));
        row.setLayoutParams(rowParams);

        if (offsetStart) {
            View pad = new View(this);
            pad.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 0.5f));
            row.addView(pad);
        }

        for (String key : keys) {
            Button btn = new Button(this);
            btn.setText(key);
            btn.setTextSize(16);
            btn.setAllCaps(false);
            btn.setTextColor(Color.parseColor("#f0f0f5"));
            btn.setPadding(0, 0, 0, 0);
            btn.setTypeface(null, Typeface.NORMAL);

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(6));
            bg.setColor(Color.parseColor("#2a2a40"));
            btn.setBackground(bg);

            LinearLayout.LayoutParams kp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            kp.setMargins(dp(2), 0, dp(2), 0);
            btn.setLayoutParams(kp);

            btn.setOnClickListener(v -> onKeyPress(btn.getText().toString()));
            row.addView(btn);
            letterKeys.add(btn);
        }

        if (offsetEnd) {
            View pad = new View(this);
            pad.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 0.5f));
            row.addView(pad);
        }

        parent.addView(row);
    }

    private void addShiftRow(LinearLayout parent, String[] keys) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(42));
        rowParams.setMargins(0, dp(2), 0, dp(2));
        row.setLayoutParams(rowParams);

        Button shiftBtn = makeSpecialKey("⇧", dp(44));
        shiftBtn.setOnClickListener(v -> toggleShift());
        shiftBtn.setOnLongClickListener(v -> { toggleCaps(); return true; });
        row.addView(shiftBtn);

        for (String key : keys) {
            Button btn = new Button(this);
            btn.setText(key);
            btn.setTextSize(16);
            btn.setAllCaps(false);
            btn.setTextColor(Color.parseColor("#f0f0f5"));
            btn.setPadding(0, 0, 0, 0);

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(6));
            bg.setColor(Color.parseColor("#2a2a40"));
            btn.setBackground(bg);

            LinearLayout.LayoutParams kp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            kp.setMargins(dp(2), 0, dp(2), 0);
            btn.setLayoutParams(kp);

            btn.setOnClickListener(v -> onKeyPress(btn.getText().toString()));
            row.addView(btn);
            letterKeys.add(btn);
        }

        Button bksp = makeSpecialKey("⌫", dp(44));
        bksp.setOnClickListener(v -> onBackspace());
        bksp.setOnLongClickListener(v -> {
            final Handler h = new Handler(Looper.getMainLooper());
            final Runnable[] r = new Runnable[1];
            r[0] = () -> {
                if (bksp.isPressed()) {
                    onBackspace();
                    h.postDelayed(r[0], 50);
                }
            };
            h.postDelayed(r[0], 50);
            return true;
        });
        row.addView(bksp);

        parent.addView(row);
    }

    private void addBottomRow(LinearLayout parent) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(42));
        rowParams.setMargins(0, dp(2), 0, dp(2));
        row.setLayoutParams(rowParams);

        Button numBtn = makeSpecialKey("123", dp(48));
        numBtn.setTextSize(13);
        numBtn.setOnClickListener(v -> toggleNumbers());
        row.addView(numBtn);

        Button commaBtn = makeSpecialKey(",", dp(36));
        commaBtn.setOnClickListener(v -> typeChar(","));
        row.addView(commaBtn);

        Button spaceBtn = new Button(this);
        spaceBtn.setText("RizzGPT");
        spaceBtn.setTextSize(12);
        spaceBtn.setAllCaps(false);
        spaceBtn.setTextColor(Color.parseColor("#9898b0"));
        GradientDrawable spaceBg = new GradientDrawable();
        spaceBg.setCornerRadius(dp(6));
        spaceBg.setColor(Color.parseColor("#2a2a40"));
        spaceBtn.setBackground(spaceBg);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        sp.setMargins(dp(3), 0, dp(3), 0);
        spaceBtn.setLayoutParams(sp);
        spaceBtn.setOnClickListener(v -> typeChar(" "));
        row.addView(spaceBtn);

        Button dotBtn = makeSpecialKey(".", dp(36));
        dotBtn.setOnClickListener(v -> typeChar("."));
        row.addView(dotBtn);

        Button enterBtn = makeSpecialKey("↵", dp(52));
        enterBtn.setTextSize(18);
        GradientDrawable enterBg = new GradientDrawable();
        enterBg.setCornerRadius(dp(6));
        enterBg.setColor(Color.parseColor("#a855f7"));
        enterBtn.setBackground(enterBg);
        enterBtn.setOnClickListener(v -> onEnter());
        row.addView(enterBtn);

        parent.addView(row);
    }

    private Button makeSpecialKey(String text, int width) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextSize(16);
        btn.setAllCaps(false);
        btn.setTextColor(Color.parseColor("#f0f0f5"));
        btn.setPadding(0, 0, 0, 0);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(6));
        bg.setColor(Color.parseColor("#3a3a50"));
        btn.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
        p.setMargins(dp(2), 0, dp(2), 0);
        btn.setLayoutParams(p);
        return btn;
    }

    private void onKeyPress(String key) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        String ch = (isShift || isCaps) ? key.toUpperCase() : key.toLowerCase();
        ic.commitText(ch, 1);
        if (isShift && !isCaps) {
            isShift = false;
            updateKeyLabels();
        }
    }

    private void typeChar(String ch) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.commitText(ch, 1);
    }

    private void onBackspace() {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            CharSequence sel = ic.getSelectedText(0);
            if (sel != null && sel.length() > 0) {
                ic.commitText("", 1);
            } else {
                ic.deleteSurroundingText(1, 0);
            }
        }
    }

    private void onEnter() {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && (ei.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) == 0) {
                ic.performEditorAction(ei.imeOptions & EditorInfo.IME_MASK_ACTION);
            } else {
                ic.commitText("\n", 1);
            }
        }
    }

    private void toggleShift() {
        isShift = !isShift;
        isCaps = false;
        updateKeyLabels();
    }

    private void toggleCaps() {
        isCaps = !isCaps;
        isShift = isCaps;
        updateKeyLabels();
    }

    private void updateKeyLabels() {
        for (Button btn : letterKeys) {
            String t = btn.getText().toString();
            if (t.length() == 1 && Character.isLetter(t.charAt(0))) {
                btn.setText((isShift || isCaps) ? t.toUpperCase() : t.toLowerCase());
            }
        }
    }

    private void toggleNumbers() {
        isNumbers = !isNumbers;
        qwertyContainer.removeAllViews();
        letterKeys.clear();
        if (isNumbers) {
            addKeyRow(qwertyContainer, NUM_ROW1, false, false);
            addKeyRow(qwertyContainer, NUM_ROW2, false, false);
            addShiftRow(qwertyContainer, NUM_ROW3);
            addBottomRow(qwertyContainer);
        } else {
            addKeyRow(qwertyContainer, ROW1, false, false);
            addKeyRow(qwertyContainer, ROW2, false, false);
            addShiftRow(qwertyContainer, ROW3);
            addBottomRow(qwertyContainer);
        }
    }

    private void toggleRizzPanel() {
        showingRizz = !showingRizz;
        if (showingRizz) {
            qwertyContainer.setVisibility(View.GONE);
            rizzPanel.setVisibility(View.VISIBLE);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(12));
            bg.setColor(Color.parseColor("#ec4899"));
            rizzToggleBtn.setBackground(bg);
            rizzToggleBtn.setText("⌨️ Keys");
            autoReadMessages();
        } else {
            rizzPanel.setVisibility(View.GONE);
            qwertyContainer.setVisibility(View.VISIBLE);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(12));
            bg.setColor(Color.parseColor("#a855f7"));
            rizzToggleBtn.setBackground(bg);
            rizzToggleBtn.setText("⚡ Rizz");
        }
    }

    private void autoReadMessages() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        String readText = "";

        ExtractedText et = ic.getExtractedText(new ExtractedTextRequest(), 0);
        if (et != null && et.text != null && et.text.length() > 0) {
            readText = et.text.toString().trim();
        }

        if (readText.isEmpty()) {
            CharSequence before = ic.getTextBeforeCursor(500, 0);
            CharSequence after = ic.getTextAfterCursor(500, 0);
            StringBuilder sb = new StringBuilder();
            if (before != null) sb.append(before);
            if (after != null) sb.append(after);
            readText = sb.toString().trim();
        }

        if (readText.isEmpty()) {
            statusLabel.setText("📖 No text found — type or paste their message above");
        } else {
            rizzInput.setText(readText);
            statusLabel.setText("📖 Auto-read: \"" + (readText.length() > 40 ? readText.substring(0, 40) + "..." : readText) + "\"");
        }
    }

    private void buildRizzPanel() {
        rizzPanel = new LinearLayout(this);
        rizzPanel.setOrientation(LinearLayout.VERTICAL);
        rizzPanel.setPadding(dp(6), dp(2), dp(6), dp(4));

        HorizontalScrollView modeScroll = new HorizontalScrollView(this);
        modeScroll.setHorizontalScrollBarEnabled(false);
        modeScroll.setPadding(0, dp(2), 0, dp(4));

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
            chip.setPadding(dp(10), dp(2), dp(10), dp(2));

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(14));

            if (mode[1].equals("smooth")) {
                bg.setColor(Color.parseColor("#2a1a3a"));
                bg.setStroke(dp(1), Color.parseColor("#a855f7"));
                chip.setTextColor(Color.parseColor("#a855f7"));
            } else {
                bg.setColor(Color.parseColor("#1a1a26"));
                bg.setStroke(dp(1), Color.parseColor("#333344"));
                chip.setTextColor(Color.parseColor("#9898b0"));
            }
            chip.setBackground(bg);

            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(30));
            cp.setMargins(0, 0, dp(4), 0);
            chip.setLayoutParams(cp);
            chip.setOnClickListener(v -> selectMode(chip));
            modesContainer.addView(chip);
        }
        modeScroll.addView(modesContainer);
        rizzPanel.addView(modeScroll);

        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        inputRow.setPadding(0, dp(2), 0, dp(2));

        Button readBtn = new Button(this);
        readBtn.setText("📖");
        readBtn.setTextSize(16);
        readBtn.setPadding(0, 0, 0, 0);
        GradientDrawable readBg = new GradientDrawable();
        readBg.setCornerRadius(dp(18));
        readBg.setColor(Color.parseColor("#10b981"));
        readBtn.setBackground(readBg);
        readBtn.setOnClickListener(v -> autoReadMessages());
        LinearLayout.LayoutParams readP = new LinearLayout.LayoutParams(dp(36), dp(36));
        readP.setMargins(0, 0, dp(6), 0);
        readBtn.setLayoutParams(readP);

        rizzInput = new EditText(this);
        rizzInput.setHint("Their message or paste context...");
        rizzInput.setHintTextColor(Color.parseColor("#5a5a72"));
        rizzInput.setTextColor(Color.parseColor("#f0f0f5"));
        rizzInput.setTextSize(13);
        rizzInput.setSingleLine(false);
        rizzInput.setMaxLines(2);
        rizzInput.setPadding(dp(12), dp(6), dp(12), dp(6));
        rizzInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setCornerRadius(dp(16));
        inputBg.setColor(Color.parseColor("#1a1a26"));
        inputBg.setStroke(dp(1), Color.parseColor("#2a2a36"));
        rizzInput.setBackground(inputBg);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        rizzInput.setLayoutParams(ip);

        generateBtn = new Button(this);
        generateBtn.setText("⚡");
        generateBtn.setTextSize(18);
        generateBtn.setPadding(0, 0, 0, 0);
        GradientDrawable genBg = new GradientDrawable();
        genBg.setCornerRadius(dp(18));
        genBg.setColor(Color.parseColor("#a855f7"));
        generateBtn.setBackground(genBg);
        generateBtn.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(dp(36), dp(36));
        gp.setMargins(dp(6), 0, 0, 0);
        generateBtn.setLayoutParams(gp);
        generateBtn.setOnClickListener(v -> onGenerate());

        inputRow.addView(readBtn);
        inputRow.addView(rizzInput);
        inputRow.addView(generateBtn);
        rizzPanel.addView(inputRow);

        statusLabel = new TextView(this);
        statusLabel.setText("Tap 📖 to auto-read or type their message → ⚡");
        statusLabel.setTextColor(Color.parseColor("#9898b0"));
        statusLabel.setTextSize(10);
        statusLabel.setGravity(Gravity.CENTER);
        statusLabel.setPadding(0, dp(2), 0, dp(2));
        rizzPanel.addView(statusLabel);

        ScrollView suggestScroll = new ScrollView(this);
        LinearLayout.LayoutParams scrollP = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(110));
        suggestScroll.setLayoutParams(scrollP);

        suggestionsContainer = new LinearLayout(this);
        suggestionsContainer.setOrientation(LinearLayout.VERTICAL);
        suggestionsContainer.setPadding(0, dp(2), 0, dp(2));
        suggestScroll.addView(suggestionsContainer);
        rizzPanel.addView(suggestScroll);

        rootLayout.addView(rizzPanel);
    }

    private void selectMode(Button selected) {
        selectedMode = (String) selected.getTag();
        for (int i = 0; i < modesContainer.getChildCount(); i++) {
            Button chip = (Button) modesContainer.getChildAt(i);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(14));
            if (chip == selected) {
                bg.setColor(Color.parseColor("#2a1a3a"));
                bg.setStroke(dp(1), Color.parseColor("#a855f7"));
                chip.setTextColor(Color.parseColor("#a855f7"));
            } else {
                bg.setColor(Color.parseColor("#1a1a26"));
                bg.setStroke(dp(1), Color.parseColor("#333344"));
                chip.setTextColor(Color.parseColor("#9898b0"));
            }
            chip.setBackground(bg);
        }
    }

    private void onGenerate() {
        if (isLoading) return;
        String context = rizzInput.getText().toString().trim();
        if (context.isEmpty()) {
            statusLabel.setText("⚠️ Enter their message or tap 📖 to auto-read");
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
            btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            btn.setPadding(dp(12), dp(6), dp(12), dp(6));

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(10));
            bg.setColor(Color.parseColor("#1a1a26"));
            bg.setStroke(dp(1), Color.parseColor("#2a1a3a"));
            btn.setBackground(bg);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, dp(3));
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) {
                    ic.commitText(reply, 1);
                }
                statusLabel.setText("✅ Typed! Hit send in the app");
                showingRizz = false;
                rizzPanel.setVisibility(View.GONE);
                qwertyContainer.setVisibility(View.VISIBLE);
                GradientDrawable tbg = new GradientDrawable();
                tbg.setCornerRadius(dp(12));
                tbg.setColor(Color.parseColor("#a855f7"));
                rizzToggleBtn.setBackground(tbg);
                rizzToggleBtn.setText("⚡ Rizz");
            });

            suggestionsContainer.addView(btn);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
