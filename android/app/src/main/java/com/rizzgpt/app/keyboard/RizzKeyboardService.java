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
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;

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
    private LinearLayout toolsContainer;
    private Button generateBtn;
    private Button rizzToggleBtn;
    private HorizontalScrollView toolScroll;
    private boolean isShift = false;
    private boolean isCaps = false;
    private boolean isNumbers = false;
    private boolean showingRizz = false;
    private String selectedMode = "smooth";
    private String selectedTool = "reply";
    private String selectedLang = "english";
    private String selectedIntensity = "bold";
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

    private static final String[][] TOOLS = {
        {"⚡", "Reply", "reply"},
        {"🎯", "Coach", "coach"},
        {"🤖", "Wingman", "wingman"},
        {"🔥", "Roast Bio", "profileRoast"},
        {"🔮", "Predict", "responsePred"},
        {"🎵", "Remixer", "remixer"},
        {"📍", "Situational", "situational"},
        {"🌊", "Flow", "flowBuilder"},
        {"💕", "Compat", "compatibility"},
        {"🧩", "Emoji", "emojiDecoder"},
        {"💡", "Date Idea", "dateIdea"},
        {"⏰", "Compliment", "complimentSched"},
        {"🚩", "Red Flag", "redFlag"},
        {"🆘", "Save Convo", "saveConvo"},
        {"😬", "Anti-Cringe", "antiCringe"},
        {"🔄", "Tone", "toneTranslator"},
        {"🪞", "Flip View", "perspectiveFlip"},
        {"👻", "Ghost Fix", "ghostRecovery"},
        {"⭐", "Rizz Rate", "rizzRater"},
        {"📊", "Bio B&A", "bioBeforeAfter"},
        {"🎬", "Date Script", "dateScript"},
        {"📈", "Trending", "trendingLines"},
        {"🔍", "Decode Msg", "msgDecoder"},
        {"💬", "Pickup", "pickup"},
        {"💘", "Flirty", "flirty"},
        {"💀", "Savage", "savage"},
        {"🧠", "Mind Game", "mindgame"},
        {"🎭", "Manipulate", "manipulate"},
        {"👑", "Power Move", "powermove"},
    };

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

        Button autoReadBtn = makeSmallBtn("📋 Read", Color.parseColor("#10b981"));
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
        String readText = "";
        String source = "";

        try {
            String screenMsgs = RizzAccessibilityService.readScreenMessages(this);
            String appName = RizzAccessibilityService.readLastApp(this);
            long ts = RizzAccessibilityService.readTimestamp(this);
            long age = System.currentTimeMillis() - ts;

            if (screenMsgs != null && screenMsgs.length() > 3 && age < 60000) {
                String[] parts = screenMsgs.split("\\n---MSG---\\n");
                StringBuilder recent = new StringBuilder();
                int start = Math.max(0, parts.length - 8);
                for (int i = start; i < parts.length; i++) {
                    String msg = parts[i].trim();
                    if (msg.length() > 2) {
                        if (recent.length() > 0) recent.append("\n");
                        recent.append(msg);
                    }
                }
                if (recent.length() > 3) {
                    readText = recent.toString();
                    source = appName != null && !appName.isEmpty() ? appName : "Screen";
                }
            }
        } catch (Exception e) {}

        if (readText.isEmpty()) {
            try {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && clipboard.hasPrimaryClip()) {
                    ClipData clip = clipboard.getPrimaryClip();
                    if (clip != null && clip.getItemCount() > 0) {
                        CharSequence clipText = clip.getItemAt(0).getText();
                        if (clipText != null && clipText.length() > 0) {
                            readText = clipText.toString().trim();
                            source = "Clipboard";
                        }
                    }
                }
            } catch (Exception e) {}
        }

        if (readText.isEmpty()) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ExtractedText et = ic.getExtractedText(new ExtractedTextRequest(), 0);
                if (et != null && et.text != null && et.text.length() > 0) {
                    readText = et.text.toString().trim();
                    source = "Input";
                }
                if (readText.isEmpty()) {
                    CharSequence before = ic.getTextBeforeCursor(500, 0);
                    CharSequence after = ic.getTextAfterCursor(500, 0);
                    StringBuilder sb = new StringBuilder();
                    if (before != null) sb.append(before);
                    if (after != null) sb.append(after);
                    readText = sb.toString().trim();
                    if (!readText.isEmpty()) source = "Input";
                }
            }
        }

        if (readText.isEmpty()) {
            boolean accessibilityOn = RizzAccessibilityService.isRunning();
            if (!accessibilityOn) {
                statusLabel.setText("⚠️ Enable Screen Reader in Settings → Accessibility → RizzGPT");
            } else {
                statusLabel.setText("📋 No messages found — open a chat first or copy text");
            }
        } else {
            rizzInput.setText(readText);
            String preview = readText.length() > 45 ? readText.substring(0, 45) + "..." : readText;
            statusLabel.setText("📖 " + source + ": \"" + preview + "\"");
        }
    }

    private void buildRizzPanel() {
        rizzPanel = new LinearLayout(this);
        rizzPanel.setOrientation(LinearLayout.VERTICAL);
        rizzPanel.setPadding(dp(4), dp(2), dp(4), dp(2));

        toolScroll = new HorizontalScrollView(this);
        toolScroll.setHorizontalScrollBarEnabled(false);
        toolScroll.setPadding(0, 0, 0, dp(2));

        toolsContainer = new LinearLayout(this);
        toolsContainer.setOrientation(LinearLayout.HORIZONTAL);

        for (String[] tool : TOOLS) {
            Button chip = new Button(this);
            chip.setText(tool[0] + " " + tool[1]);
            chip.setTextSize(9);
            chip.setAllCaps(false);
            chip.setTag(tool[2]);
            chip.setPadding(dp(8), dp(1), dp(8), dp(1));

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(12));

            if (tool[2].equals("reply")) {
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
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(26));
            cp.setMargins(0, 0, dp(4), 0);
            chip.setLayoutParams(cp);
            chip.setOnClickListener(v -> selectTool(chip));
            toolsContainer.addView(chip);
        }
        toolScroll.addView(toolsContainer);
        rizzPanel.addView(toolScroll);

        HorizontalScrollView modeScroll = new HorizontalScrollView(this);
        modeScroll.setHorizontalScrollBarEnabled(false);
        modeScroll.setPadding(0, 0, 0, dp(2));

        modesContainer = new LinearLayout(this);
        modesContainer.setOrientation(LinearLayout.HORIZONTAL);

        String[][] modes = {
            {"😏 Smooth", "smooth"}, {"😂 Funny", "funny"}, {"🔥 Bold", "bold"},
            {"💜 Flirty", "flirty"}, {"💀 Savage", "savage"}, {"🍯 Sweet", "sweet"},
            {"🇮🇳 Hinglish", "lang_hinglish"}, {"🇬🇧 English", "lang_english"}, {"🇮🇳 Hindi", "lang_hindi"},
            {"🔥 Extreme", "int_extreme"}, {"💪 Bold", "int_bold"}, {"🌸 Mild", "int_mild"}
        };

        for (String[] mode : modes) {
            Button chip = new Button(this);
            chip.setText(mode[0]);
            chip.setTextSize(9);
            chip.setAllCaps(false);
            chip.setTag(mode[1]);
            chip.setPadding(dp(8), dp(1), dp(8), dp(1));

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(12));

            boolean isActive = mode[1].equals("smooth") || mode[1].equals("lang_english") || mode[1].equals("int_bold");
            if (isActive) {
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
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(26));
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
        readBtn.setText("📋");
        readBtn.setTextSize(14);
        readBtn.setPadding(0, 0, 0, 0);
        GradientDrawable readBg = new GradientDrawable();
        readBg.setCornerRadius(dp(16));
        readBg.setColor(Color.parseColor("#10b981"));
        readBtn.setBackground(readBg);
        readBtn.setOnClickListener(v -> autoReadMessages());
        LinearLayout.LayoutParams readP = new LinearLayout.LayoutParams(dp(32), dp(32));
        readP.setMargins(0, 0, dp(4), 0);
        readBtn.setLayoutParams(readP);

        rizzInput = new EditText(this);
        rizzInput.setHint("Copy msg → tap 📋 or type here...");
        rizzInput.setHintTextColor(Color.parseColor("#5a5a72"));
        rizzInput.setTextColor(Color.parseColor("#f0f0f5"));
        rizzInput.setTextSize(12);
        rizzInput.setSingleLine(false);
        rizzInput.setMaxLines(2);
        rizzInput.setPadding(dp(10), dp(4), dp(10), dp(4));
        rizzInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setCornerRadius(dp(14));
        inputBg.setColor(Color.parseColor("#1a1a26"));
        inputBg.setStroke(dp(1), Color.parseColor("#2a2a36"));
        rizzInput.setBackground(inputBg);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        rizzInput.setLayoutParams(ip);

        generateBtn = new Button(this);
        generateBtn.setText("⚡");
        generateBtn.setTextSize(16);
        generateBtn.setPadding(0, 0, 0, 0);
        GradientDrawable genBg = new GradientDrawable();
        genBg.setCornerRadius(dp(16));
        genBg.setColor(Color.parseColor("#a855f7"));
        generateBtn.setBackground(genBg);
        generateBtn.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(dp(32), dp(32));
        gp.setMargins(dp(4), 0, 0, 0);
        generateBtn.setLayoutParams(gp);
        generateBtn.setOnClickListener(v -> onGenerate());

        inputRow.addView(readBtn);
        inputRow.addView(rizzInput);
        inputRow.addView(generateBtn);
        rizzPanel.addView(inputRow);

        statusLabel = new TextView(this);
        statusLabel.setText("📋 Copy their msg → Read | Pick tool → ⚡ Generate");
        statusLabel.setTextColor(Color.parseColor("#9898b0"));
        statusLabel.setTextSize(9);
        statusLabel.setGravity(Gravity.CENTER);
        statusLabel.setPadding(0, dp(1), 0, dp(1));
        rizzPanel.addView(statusLabel);

        ScrollView suggestScroll = new ScrollView(this);
        LinearLayout.LayoutParams scrollP = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(100));
        suggestScroll.setLayoutParams(scrollP);

        suggestionsContainer = new LinearLayout(this);
        suggestionsContainer.setOrientation(LinearLayout.VERTICAL);
        suggestionsContainer.setPadding(0, dp(2), 0, dp(2));
        suggestScroll.addView(suggestionsContainer);
        rizzPanel.addView(suggestScroll);

        rootLayout.addView(rizzPanel);
    }

    private void selectTool(Button selected) {
        selectedTool = (String) selected.getTag();
        for (int i = 0; i < toolsContainer.getChildCount(); i++) {
            Button chip = (Button) toolsContainer.getChildAt(i);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(12));
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
        updateHintForTool();
    }

    private void updateHintForTool() {
        switch (selectedTool) {
            case "reply": rizzInput.setHint("Their message to reply to..."); break;
            case "coach": rizzInput.setHint("Paste full conversation..."); break;
            case "wingman": rizzInput.setHint("Their last message..."); break;
            case "profileRoast": rizzInput.setHint("Paste dating profile bio..."); break;
            case "responsePred": rizzInput.setHint("What did you send them?"); break;
            case "remixer": rizzInput.setHint("Paste a pickup line to remix..."); break;
            case "situational": rizzInput.setHint("Situation: coffee shop, gym, party..."); break;
            case "flowBuilder": rizzInput.setHint("Their opening message..."); break;
            case "compatibility": rizzInput.setHint("Your zodiac/MBTI + theirs..."); break;
            case "emojiDecoder": rizzInput.setHint("Paste emojis to decode..."); break;
            case "dateIdea": rizzInput.setHint("Budget & interests..."); break;
            case "complimentSched": rizzInput.setHint("Person's name & style..."); break;
            case "redFlag": rizzInput.setHint("Paste their message or profile..."); break;
            case "saveConvo": rizzInput.setHint("Paste the dying conversation..."); break;
            case "antiCringe": rizzInput.setHint("What you're about to send..."); break;
            case "toneTranslator": rizzInput.setHint("Your message to translate tone..."); break;
            case "perspectiveFlip": rizzInput.setHint("What you're about to send..."); break;
            case "ghostRecovery": rizzInput.setHint("Last msgs before being ghosted..."); break;
            case "rizzRater": rizzInput.setHint("Your line or message to rate..."); break;
            case "bioBeforeAfter": rizzInput.setHint("Your current bio to transform..."); break;
            case "dateScript": rizzInput.setHint("Date type & their interests..."); break;
            case "trendingLines": rizzInput.setHint("Category: memes, movies, pop..."); break;
            case "msgDecoder": rizzInput.setHint("Their confusing message..."); break;
            case "pickup": rizzInput.setHint("Topic or their name (optional)..."); break;
            case "flirty": rizzInput.setHint("Context for flirty texts..."); break;
            case "savage": rizzInput.setHint("Context for savage reply..."); break;
            case "mindgame": rizzInput.setHint("Paste their messages for mind games..."); break;
            case "manipulate": rizzInput.setHint("Paste conversation for manipulation..."); break;
            case "powermove": rizzInput.setHint("Paste chat for power move..."); break;
        }
        statusLabel.setText("🔧 " + getToolName(selectedTool) + " selected — type context → ⚡");
    }

    private String getToolName(String tool) {
        for (String[] t : TOOLS) {
            if (t[2].equals(tool)) return t[0] + " " + t[1];
        }
        return tool;
    }

    private void selectMode(Button selected) {
        String tag = (String) selected.getTag();

        if (tag.startsWith("lang_")) {
            selectedLang = tag.replace("lang_", "");
            for (int i = 0; i < modesContainer.getChildCount(); i++) {
                Button chip = (Button) modesContainer.getChildAt(i);
                String ctag = (String) chip.getTag();
                if (ctag.startsWith("lang_")) {
                    updateChipStyle(chip, chip == selected);
                }
            }
        } else if (tag.startsWith("int_")) {
            selectedIntensity = tag.replace("int_", "");
            for (int i = 0; i < modesContainer.getChildCount(); i++) {
                Button chip = (Button) modesContainer.getChildAt(i);
                String ctag = (String) chip.getTag();
                if (ctag.startsWith("int_")) {
                    updateChipStyle(chip, chip == selected);
                }
            }
        } else {
            selectedMode = tag;
            for (int i = 0; i < modesContainer.getChildCount(); i++) {
                Button chip = (Button) modesContainer.getChildAt(i);
                String ctag = (String) chip.getTag();
                if (!ctag.startsWith("lang_") && !ctag.startsWith("int_")) {
                    updateChipStyle(chip, chip == selected);
                }
            }
        }
    }

    private void updateChipStyle(Button chip, boolean active) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        if (active) {
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

    private void onGenerate() {
        if (isLoading) return;
        String context = rizzInput.getText().toString().trim();

        if (context.isEmpty() && !selectedTool.equals("pickup") && !selectedTool.equals("trendingLines")) {
            statusLabel.setText("⚠️ Enter context first or tap 📋 to read clipboard");
            return;
        }
        runTool(context, selectedTool, selectedMode);
    }

    private String getLangInstruction() {
        switch (selectedLang) {
            case "hinglish": return " Write in Hinglish (Hindi+English mix, Roman script) like young Indians text on WhatsApp. Use yaar/arre/accha naturally.";
            case "hindi": return " Write in casual Roman Hindi (no Devanagari).";
            default: return "";
        }
    }

    private String getIntensityDesc() {
        switch (selectedIntensity) {
            case "extreme": return "extremely bold, intense, and daring";
            case "mild": return "mild, subtle, and gentle";
            default: return "confident and charming";
        }
    }

    private void runTool(String context, String tool, String style) {
        isLoading = true;
        String toolEmoji = "";
        for (String[] t : TOOLS) { if (t[2].equals(tool)) { toolEmoji = t[0]; break; } }
        final String emoji = toolEmoji;

        mainHandler.post(() -> {
            statusLabel.setText(emoji + " Generating...");
            generateBtn.setEnabled(false);
            suggestionsContainer.removeAllViews();
        });

        executor.execute(() -> {
            try {
                String systemPrompt = buildSystemPrompt(tool, style, context);
                String userPrompt = buildUserPrompt(tool, style, context);

                JSONObject body = new JSONObject();
                body.put("model", "llama-3.3-70b-versatile");
                body.put("temperature", 1.0);
                body.put("max_tokens", 800);

                JSONArray msgs = new JSONArray();
                JSONObject sysMsg = new JSONObject();
                sysMsg.put("role", "system");
                sysMsg.put("content", systemPrompt);
                msgs.put(sysMsg);

                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", userPrompt);
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

                List<String> replies = parseReplies(content, tool);

                mainHandler.post(() -> {
                    isLoading = false;
                    generateBtn.setEnabled(true);
                    if (replies.isEmpty()) {
                        statusLabel.setText("❌ No results. Try again!");
                        return;
                    }
                    statusLabel.setText(emoji + " Tap to type it ↓");
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

    private String buildSystemPrompt(String tool, String style, String context) {
        String langNote = getLangInstruction();
        String intNote = getIntensityDesc();

        switch (tool) {
            case "reply":
                return "You generate text message replies. Style: " + style + ". Intensity: " + intNote + "." + langNote + "\n" +
                    "Rules: 1. Type like a real person: lowercase, casual 2. Keep short: 5-20 words per reply 3. Sound human NOT AI 4. Give exactly 5 different replies numbered 1-5 5. Each on its own line";

            case "coach":
                return "You're a dating/texting coach. Analyze the conversation and give coaching advice." + langNote + "\n" +
                    "Format:\nVIBE CHECK: [1 line assessment]\nGREEN FLAGS: [what's going well]\nRED FLAGS: [concerns]\n3 TIPS: [numbered tips]\nNEXT MOVE: [what to do next]\n5 SUGGESTED REPLIES: [numbered 1-5, short natural messages]";

            case "wingman":
                return "You are an AI Wingman providing REAL-TIME coaching during a live conversation. Be quick and actionable." + langNote + "\n" +
                    "Give: MOOD (their vibe), INTEREST LEVEL (1-10), 5 QUICK REPLIES (numbered), DO NOT SAY (what to avoid), STRATEGY (1-line advice). Format each reply on numbered lines.";

            case "profileRoast":
                return "You're a brutally honest but helpful dating profile reviewer." + langNote + "\n" +
                    "First ROAST the bio (be savage, 2-3 lines). Score /10. Then give 5 IMPROVED BIO versions numbered 1-5 that are short, catchy, and swipe-right worthy.";

            case "responsePred":
                return "You predict how someone will reply to a text." + langNote + "\n" +
                    "Give 5 predicted responses numbered 1-5 with probability %. Range from best to worst case. Each line: N. [probability%] \"predicted reply\" → Best response: \"your reply\"";

            case "remixer":
                return "You remix pickup lines into different styles: " + style + ". Intensity: " + intNote + "." + langNote + "\n" +
                    "Give 5 remixed versions numbered 1-5. Each should feel unique and fresh.";

            case "situational":
                return "You generate pickup lines and conversation starters for specific real-life situations. Style: " + style + "." + langNote + "\n" +
                    "Give 5 situation-specific openers numbered 1-5. Make them feel natural for the setting.";

            case "flowBuilder":
                return "You build conversation flows - a step-by-step texting strategy." + langNote + "\n" +
                    "Give 5 possible next messages numbered 1-5, then a FLOW: Opening → Build rapport → Create interest → Close with action. Keep each step as a sendable message.";

            case "compatibility":
                return "You analyze zodiac/MBTI compatibility for dating." + langNote + "\n" +
                    "Give: MATCH SCORE /10, CHEMISTRY (1 line), STRENGTHS (3 points), CHALLENGES (3 points), then 5 CONVERSATION STARTERS numbered 1-5 specific to this combo.";

            case "emojiDecoder":
                return "You decode emoji messages and reveal hidden meanings." + langNote + "\n" +
                    "Decode each emoji, give OVERALL MEANING, HIDDEN SUBTEXT, INTEREST LEVEL /10, then 5 REPLY OPTIONS numbered 1-5.";

            case "dateIdea":
                return "You generate creative, unique date ideas." + langNote + "\n" +
                    "Give 5 date ideas numbered 1-5. Each: name, what to do (2 lines), pro tip, estimated cost. Make them memorable and unique.";

            case "complimentSched":
                return "You generate personalized compliments." + langNote + " Style: " + style + ". Intensity: " + intNote + ".\n" +
                    "Give 5 unique compliments numbered 1-5. Make each feel genuine, specific, and personal. Mix different types: appearance, personality, talent, vibe, energy.";

            case "redFlag":
                return "You detect red flags and green flags in messages/profiles." + langNote + "\n" +
                    "Analyze and give: RED FLAGS (list), GREEN FLAGS (list), OVERALL RATING /10, VERDICT, then 5 RESPONSE OPTIONS numbered 1-5 ranging from cautious to bold.";

            case "saveConvo":
                return "You rescue dying/awkward conversations." + langNote + " Intensity: " + intNote + ".\n" +
                    "Diagnose WHAT WENT WRONG, give 5 RECOVERY MESSAGES numbered 1-5 that can save the convo. Range from safe to bold. Make them natural.";

            case "antiCringe":
                return "You're a cringe detector that checks messages before sending." + langNote + "\n" +
                    "CRINGE SCORE /10 (10=max cringe), WHAT'S WRONG (honest feedback), then 5 IMPROVED VERSIONS numbered 1-5 from least to most changed.";

            case "toneTranslator":
                return "You translate messages into different tones. Target tone: " + style + ". Intensity: " + intNote + "." + langNote + "\n" +
                    "Give 5 versions numbered 1-5 in the " + style + " tone. Each should feel distinct but carry the same meaning.";

            case "perspectiveFlip":
                return "You flip perspective - show how the receiver sees a message." + langNote + "\n" +
                    "THEIR FIRST IMPRESSION, INTEREST LEVEL /10, WHAT THEY'RE THINKING, then 5 BETTER VERSIONS numbered 1-5 that would impress them more.";

            case "ghostRecovery":
                return "You help recover from being ghosted." + langNote + " Intensity: " + intNote + ".\n" +
                    "DIAGNOSIS (why they ghosted), STRATEGY, then 5 COMEBACK MESSAGES numbered 1-5. Range from casual check-in to bold power move.";

            case "rizzRater":
                return "You rate pickup lines and messages on a rizz scale." + langNote + "\n" +
                    "RIZZ SCORE /10, BREAKDOWN (delivery, creativity, confidence), WHAT WORKS, WHAT DOESN'T, then 5 UPGRADED VERSIONS numbered 1-5.";

            case "bioBeforeAfter":
                return "You transform dating profile bios from boring to fire." + langNote + "\n" +
                    "BEFORE SCORE /10, PROBLEMS (what's wrong), then 5 TRANSFORMED BIOS numbered 1-5. Each should be short, catchy, and swipe-right worthy. AFTER SCORE /10.";

            case "dateScript":
                return "You write first date scripts - what to say and when." + langNote + "\n" +
                    "Give a FULL SCRIPT: Opening (first 5 min), Warmup topics, Deep dive questions, Fun activities to suggest, Closing move. Then 5 KEY LINES numbered 1-5 to memorize.";

            case "trendingLines":
                return "You generate trending, viral-style pickup lines based on current pop culture, memes, movies, music. Style: " + style + "." + langNote + "\n" +
                    "Give 5 trending pickup lines numbered 1-5. Reference current trends, movies, shows, songs, memes. Make them feel fresh and shareable.";

            case "msgDecoder":
                return "You decode confusing/ambiguous text messages to reveal what they REALLY mean." + langNote + "\n" +
                    "SURFACE MEANING, HIDDEN MEANING, INTEREST LEVEL /10, WHAT TO DO, then 5 REPLY OPTIONS numbered 1-5.";

            case "pickup":
                return "You generate the best pickup lines. Style: " + style + ". Intensity: " + intNote + "." + langNote + "\n" +
                    "Give 5 unique, creative pickup lines numbered 1-5. Make each feel fresh, not overused.";

            case "flirty":
                return "You generate flirty text messages. Intensity: " + intNote + "." + langNote + "\n" +
                    "Give 5 flirty messages numbered 1-5. Each should be playful, teasing, and make them smile. Keep it natural.";

            case "savage":
                return "You generate savage, witty comebacks and replies. Intensity: " + intNote + "." + langNote + "\n" +
                    "Give 5 savage replies numbered 1-5. Be witty, sharp, and confident. Not mean - just alpha energy.";

            case "mindgame":
                return "You're a master psychological strategist in dating. Analyze this conversation and suggest mind games." + langNote + "\n" +
                    "Give: CURRENT DYNAMIC, THEIR PSYCHOLOGY, then 5 MIND GAME STRATEGIES numbered 1-5 with exact messages to send. Include push-pull, scarcity, jealousy triggers.";

            case "manipulate":
                return "You're an advanced conversation manipulation expert." + langNote + "\n" +
                    "Analyze and give: VULNERABILITY MAP, EMOTIONAL HOOKS, INTERMITTENT REINFORCEMENT schedule, then 5 MANIPULATION MESSAGES numbered 1-5 designed to create emotional dependency.";

            case "powermove":
                return "You're a power dynamics expert in dating." + langNote + "\n" +
                    "Give: POWER SCORE (you vs them), THE POWER MOVE (1 devastating message), then 5 ALTERNATIVE POWER MOVES numbered 1-5. Each with exact message and timing.";

            default:
                return "You generate text message replies. Style: " + style + "." + langNote + "\nGive 5 replies numbered 1-5.";
        }
    }

    private String buildUserPrompt(String tool, String style, String context) {
        if (context.isEmpty()) context = "general/any topic";

        switch (tool) {
            case "reply": return "Generate 5 " + style + " replies to: \"" + context + "\"";
            case "coach": return "Analyze this conversation and coach me:\n" + context;
            case "wingman": return "Wingman analysis - their message: \"" + context + "\"\nGive quick replies and strategy.";
            case "profileRoast": return "Roast and improve this dating bio:\n\"" + context + "\"";
            case "responsePred": return "Predict how they'll respond to: \"" + context + "\"";
            case "remixer": return "Remix this line in " + style + " style: \"" + context + "\"";
            case "situational": return "Generate openers for this situation: " + context;
            case "flowBuilder": return "Build a conversation flow starting from: \"" + context + "\"";
            case "compatibility": return "Analyze dating compatibility: " + context;
            case "emojiDecoder": return "Decode these emojis/message: " + context;
            case "dateIdea": return "Generate date ideas for: " + context;
            case "complimentSched": return "Generate compliments for: " + context;
            case "redFlag": return "Detect red/green flags in: \"" + context + "\"";
            case "saveConvo": return "Save this dying conversation:\n" + context;
            case "antiCringe": return "Cringe-check this message before I send it: \"" + context + "\"";
            case "toneTranslator": return "Translate this to " + style + " tone: \"" + context + "\"";
            case "perspectiveFlip": return "How would they see this message: \"" + context + "\"";
            case "ghostRecovery": return "Help me recover from ghosting. Last messages:\n" + context;
            case "rizzRater": return "Rate the rizz of: \"" + context + "\"";
            case "bioBeforeAfter": return "Transform this bio:\n\"" + context + "\"";
            case "dateScript": return "Write a first date script for: " + context;
            case "trendingLines": return "Generate trending pickup lines about: " + context;
            case "msgDecoder": return "Decode this message: \"" + context + "\"";
            case "pickup": return "Generate 5 " + style + " pickup lines" + (context.isEmpty() ? "" : " about: " + context);
            case "flirty": return "Generate 5 flirty texts for: " + context;
            case "savage": return "Generate 5 savage replies to: \"" + context + "\"";
            case "mindgame": return "Create mind game strategies for this conversation:\n" + context;
            case "manipulate": return "Create manipulation playbook for:\n" + context;
            case "powermove": return "Create power move strategy for:\n" + context;
            default: return "Generate 5 " + style + " replies to: \"" + context + "\"";
        }
    }

    private List<String> parseReplies(String content, String tool) {
        List<String> replies = new ArrayList<>();

        boolean isAnalysisTool = tool.equals("coach") || tool.equals("wingman") ||
            tool.equals("profileRoast") || tool.equals("responsePred") ||
            tool.equals("compatibility") || tool.equals("emojiDecoder") ||
            tool.equals("redFlag") || tool.equals("antiCringe") ||
            tool.equals("perspectiveFlip") || tool.equals("rizzRater") ||
            tool.equals("bioBeforeAfter") || tool.equals("dateScript") ||
            tool.equals("msgDecoder") || tool.equals("dateIdea") ||
            tool.equals("mindgame") || tool.equals("manipulate") || tool.equals("powermove");

        if (isAnalysisTool) {
            String[] sections = content.split("\n\n");
            StringBuilder analysis = new StringBuilder();
            List<String> actionItems = new ArrayList<>();

            for (String section : sections) {
                String trimmed = section.trim();
                if (trimmed.isEmpty()) continue;

                boolean hasNumbered = false;
                for (String line : trimmed.split("\n")) {
                    String clean = line.trim();
                    if (clean.matches("^\\d+[.)\\-:\\s]+.+") && clean.length() > 8) {
                        String item = clean.replaceAll("^\\d+[.)\\-:\\s]+", "").replaceAll("^\"|\"$", "").trim();
                        if (item.length() > 3) {
                            actionItems.add(item);
                            hasNumbered = true;
                        }
                    }
                }
                if (!hasNumbered && analysis.length() < 400) {
                    if (analysis.length() > 0) analysis.append("\n");
                    analysis.append(trimmed);
                }
            }

            if (analysis.length() > 0) {
                String analysisText = analysis.toString();
                if (analysisText.length() > 200) {
                    analysisText = analysisText.substring(0, 200) + "...";
                }
                replies.add("📊 " + analysisText);
            }

            for (String item : actionItems) {
                replies.add(item);
                if (replies.size() >= 8) break;
            }
        } else {
            for (String l : content.split("\n")) {
                String clean = l.trim().replaceAll("^\\d+[.)\\-:\\s]+", "").replaceAll("^\"|\"$", "").trim();
                if (clean.length() > 3 && !clean.startsWith("#") && !clean.startsWith("*") && !clean.startsWith("---")) {
                    replies.add(clean);
                }
            }
        }

        return replies;
    }

    private void showReplies(List<String> replies) {
        suggestionsContainer.removeAllViews();
        int count = Math.min(replies.size(), 8);
        for (int i = 0; i < count; i++) {
            final String reply = replies.get(i);
            final boolean isAnalysis = reply.startsWith("📊 ");

            Button btn = new Button(this);
            btn.setText(reply);
            btn.setTextSize(11);
            btn.setAllCaps(false);
            btn.setTextColor(Color.parseColor(isAnalysis ? "#a0a0c0" : "#f0f0f5"));
            btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            btn.setPadding(dp(10), dp(5), dp(10), dp(5));

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(8));
            bg.setColor(Color.parseColor(isAnalysis ? "#12121e" : "#1a1a26"));
            bg.setStroke(dp(1), Color.parseColor(isAnalysis ? "#333355" : "#2a1a3a"));
            btn.setBackground(bg);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, dp(2));
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                String textToType = isAnalysis ? reply.substring(2).trim() : reply;
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) {
                    ic.commitText(textToType, 1);
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
