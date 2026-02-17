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
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.MotionEvent;

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
    private LinearLayout chatPickerPanel;
    private EditText rizzInput;
    private TextView statusLabel;
    private LinearLayout suggestionsContainer;
    private LinearLayout modesContainer;
    private LinearLayout toolsContainer;
    private Button generateBtn;
    private Button rizzToggleBtn;
    private Button chatBtn;
    private boolean isShift = false;
    private boolean isCaps = false;
    private boolean isNumbers = false;
    private boolean showingRizz = false;
    private boolean showingChatPicker = false;
    private String selectedMode = "smooth";
    private String selectedTool = "reply";
    private String selectedLang = "english";
    private String selectedIntensity = "bold";
    private String activeChat = null;
    private List<String> activeChatMessages = new ArrayList<>();
    private boolean isLoading = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private List<Button> letterKeys = new ArrayList<>();

    private static final String CHAT_PREFS = "rizzgpt_chatbrain";

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
        {"🎭", "Mind Game", "mindgame"},
        {"🧠", "Manipulate", "manipulate"},
        {"👑", "Power Move", "powermove"},
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
        buildChatPickerPanel();

        rizzPanel.setVisibility(View.GONE);
        chatPickerPanel.setVisibility(View.GONE);
        qwertyContainer.setVisibility(View.VISIBLE);

        return rootLayout;
    }

    // ===== TOP BAR =====
    private void buildTopBar() {
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(4), dp(2), dp(4), dp(2));
        topBar.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(34)));

        TextView logo = new TextView(this);
        logo.setText("⚡RizzGPT");
        logo.setTextColor(Color.parseColor("#a855f7"));
        logo.setTextSize(12);
        logo.setTypeface(null, Typeface.BOLD);

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));

        chatBtn = makeTopBtn("🧠", Color.parseColor("#ec4899"));
        chatBtn.setOnClickListener(v -> toggleChatPicker());
        updateChatBtnLabel();

        Button autoReadBtn = makeTopBtn("📖", Color.parseColor("#10b981"));
        autoReadBtn.setOnClickListener(v -> autoReadMessages());

        rizzToggleBtn = makeTopBtn("⚡ Rizz", Color.parseColor("#a855f7"));
        rizzToggleBtn.setOnClickListener(v -> toggleRizzPanel());

        Button switchBtn = makeTopBtn("🌐", Color.parseColor("#6366f1"));
        switchBtn.setOnClickListener(v -> {
            try { switchToNextInputMethod(false); } catch (Exception e) {}
        });

        topBar.addView(logo);
        topBar.addView(spacer);
        topBar.addView(chatBtn);
        topBar.addView(autoReadBtn);
        topBar.addView(rizzToggleBtn);
        topBar.addView(switchBtn);
        rootLayout.addView(topBar);
    }

    private Button makeTopBtn(String text, int color) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextSize(9);
        btn.setAllCaps(false);
        btn.setTextColor(Color.WHITE);
        btn.setPadding(dp(7), dp(1), dp(7), dp(1));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(10));
        bg.setColor(color);
        btn.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, dp(26));
        p.setMargins(dp(2), 0, dp(2), 0);
        btn.setLayoutParams(p);
        return btn;
    }

    private void updateChatBtnLabel() {
        if (activeChat != null) {
            String short_name = activeChat.length() > 6 ? activeChat.substring(0, 6) + ".." : activeChat;
            chatBtn.setText("🧠" + short_name);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(10));
            bg.setColor(Color.parseColor("#22c55e"));
            chatBtn.setBackground(bg);
        } else {
            chatBtn.setText("🧠 Chats");
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(10));
            bg.setColor(Color.parseColor("#ec4899"));
            chatBtn.setBackground(bg);
        }
    }

    // ===== QWERTY KEYBOARD =====
    private void buildQwertyKeyboard() {
        qwertyContainer = new LinearLayout(this);
        qwertyContainer.setOrientation(LinearLayout.VERTICAL);
        qwertyContainer.setPadding(dp(2), dp(2), dp(2), dp(2));
        addKeyRow(qwertyContainer, ROW1);
        addKeyRow(qwertyContainer, ROW2);
        addShiftRow(qwertyContainer, ROW3);
        addBottomRow(qwertyContainer);
        rootLayout.addView(qwertyContainer);
    }

    private void addKeyRow(LinearLayout parent, String[] keys) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(42)));
        ((LinearLayout.LayoutParams) row.getLayoutParams()).setMargins(0, dp(2), 0, dp(2));

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
        parent.addView(row);
    }

    private void addShiftRow(LinearLayout parent, String[] keys) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(42)));
        ((LinearLayout.LayoutParams) row.getLayoutParams()).setMargins(0, dp(2), 0, dp(2));

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
            r[0] = () -> { if (bksp.isPressed()) { onBackspace(); h.postDelayed(r[0], 50); } };
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
        row.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(42)));
        ((LinearLayout.LayoutParams) row.getLayoutParams()).setMargins(0, dp(2), 0, dp(2));

        Button numBtn = makeSpecialKey("123", dp(48));
        numBtn.setTextSize(13);
        numBtn.setOnClickListener(v -> toggleNumbers());
        row.addView(numBtn);

        Button comma = makeSpecialKey(",", dp(36));
        comma.setOnClickListener(v -> typeChar(","));
        row.addView(comma);

        Button space = new Button(this);
        space.setText(activeChat != null ? "🧠 " + activeChat : "RizzGPT");
        space.setTextSize(11);
        space.setAllCaps(false);
        space.setTextColor(activeChat != null ? Color.parseColor("#22c55e") : Color.parseColor("#9898b0"));
        GradientDrawable sBg = new GradientDrawable();
        sBg.setCornerRadius(dp(6));
        sBg.setColor(Color.parseColor("#2a2a40"));
        space.setBackground(sBg);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        sp.setMargins(dp(3), 0, dp(3), 0);
        space.setLayoutParams(sp);
        space.setOnClickListener(v -> typeChar(" "));
        row.addView(space);

        Button dot = makeSpecialKey(".", dp(36));
        dot.setOnClickListener(v -> typeChar("."));
        row.addView(dot);

        Button enter = makeSpecialKey("↵", dp(52));
        enter.setTextSize(18);
        GradientDrawable eBg = new GradientDrawable();
        eBg.setCornerRadius(dp(6));
        eBg.setColor(Color.parseColor("#a855f7"));
        enter.setBackground(eBg);
        enter.setOnClickListener(v -> onEnter());
        row.addView(enter);

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
        if (isShift && !isCaps) { isShift = false; updateKeyLabels(); }
    }

    private void typeChar(String ch) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.commitText(ch, 1);
    }

    private void onBackspace() {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            CharSequence sel = ic.getSelectedText(0);
            if (sel != null && sel.length() > 0) ic.commitText("", 1);
            else ic.deleteSurroundingText(1, 0);
        }
    }

    private void onEnter() {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && (ei.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) == 0)
                ic.performEditorAction(ei.imeOptions & EditorInfo.IME_MASK_ACTION);
            else ic.commitText("\n", 1);
        }
    }

    private void toggleShift() { isShift = !isShift; isCaps = false; updateKeyLabels(); }
    private void toggleCaps() { isCaps = !isCaps; isShift = isCaps; updateKeyLabels(); }

    private void updateKeyLabels() {
        for (Button btn : letterKeys) {
            String t = btn.getText().toString();
            if (t.length() == 1 && Character.isLetter(t.charAt(0)))
                btn.setText((isShift || isCaps) ? t.toUpperCase() : t.toLowerCase());
        }
    }

    private void toggleNumbers() {
        isNumbers = !isNumbers;
        qwertyContainer.removeAllViews();
        letterKeys.clear();
        if (isNumbers) {
            addKeyRow(qwertyContainer, NUM_ROW1);
            addKeyRow(qwertyContainer, NUM_ROW2);
            addShiftRow(qwertyContainer, NUM_ROW3);
        } else {
            addKeyRow(qwertyContainer, ROW1);
            addKeyRow(qwertyContainer, ROW2);
            addShiftRow(qwertyContainer, ROW3);
        }
        addBottomRow(qwertyContainer);
    }

    // ===== CHAT PICKER PANEL =====
    private void buildChatPickerPanel() {
        chatPickerPanel = new LinearLayout(this);
        chatPickerPanel.setOrientation(LinearLayout.VERTICAL);
        chatPickerPanel.setPadding(dp(6), dp(4), dp(6), dp(4));
        rootLayout.addView(chatPickerPanel);
    }

    private void toggleChatPicker() {
        showingChatPicker = !showingChatPicker;
        if (showingChatPicker) {
            qwertyContainer.setVisibility(View.GONE);
            rizzPanel.setVisibility(View.GONE);
            chatPickerPanel.setVisibility(View.VISIBLE);
            showingRizz = false;
            refreshChatPicker();
        } else {
            chatPickerPanel.setVisibility(View.GONE);
            qwertyContainer.setVisibility(View.VISIBLE);
        }
    }

    private void refreshChatPicker() {
        chatPickerPanel.removeAllViews();

        TextView title = new TextView(this);
        title.setText("🧠 Chat Brain — Saved Chats");
        title.setTextColor(Color.parseColor("#a855f7"));
        title.setTextSize(13);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, dp(6));
        chatPickerPanel.addView(title);

        if (activeChat != null) {
            LinearLayout activeBar = new LinearLayout(this);
            activeBar.setOrientation(LinearLayout.HORIZONTAL);
            activeBar.setGravity(Gravity.CENTER_VERTICAL);
            activeBar.setPadding(dp(8), dp(6), dp(8), dp(6));
            GradientDrawable abg = new GradientDrawable();
            abg.setCornerRadius(dp(10));
            abg.setColor(Color.parseColor("#0d3320"));
            abg.setStroke(dp(1), Color.parseColor("#22c55e"));
            activeBar.setBackground(abg);
            activeBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            ((LinearLayout.LayoutParams)activeBar.getLayoutParams()).setMargins(0, 0, 0, dp(6));

            TextView activeTxt = new TextView(this);
            activeTxt.setText("✅ Active: " + activeChat + " (" + activeChatMessages.size() + " msgs)");
            activeTxt.setTextColor(Color.parseColor("#22c55e"));
            activeTxt.setTextSize(11);
            activeTxt.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            activeBar.addView(activeTxt);

            Button disconnBtn = new Button(this);
            disconnBtn.setText("✕ Disconnect");
            disconnBtn.setTextSize(9);
            disconnBtn.setAllCaps(false);
            disconnBtn.setTextColor(Color.parseColor("#ef4444"));
            disconnBtn.setPadding(dp(8), dp(2), dp(8), dp(2));
            GradientDrawable dbg = new GradientDrawable();
            dbg.setCornerRadius(dp(8));
            dbg.setColor(Color.parseColor("#2a1515"));
            disconnBtn.setBackground(dbg);
            disconnBtn.setOnClickListener(v -> {
                activeChat = null;
                activeChatMessages.clear();
                updateChatBtnLabel();
                refreshChatPicker();
                rebuildQwerty();
            });
            activeBar.addView(disconnBtn);
            chatPickerPanel.addView(activeBar);
        }

        SharedPreferences prefs = getSharedPreferences(CHAT_PREFS, MODE_PRIVATE);
        String indexStr = prefs.getString("chats_index", "[]");
        try {
            JSONArray index = new JSONArray(indexStr);
            if (index.length() == 0) {
                TextView empty = new TextView(this);
                empty.setText("No saved chats. Open RizzGPT app → Tools → 🧠 Chat Brain to upload screenshots and save chats.");
                empty.setTextColor(Color.parseColor("#9898b0"));
                empty.setTextSize(11);
                empty.setPadding(dp(8), dp(12), dp(8), dp(12));
                chatPickerPanel.addView(empty);
            } else {
                ScrollView scroll = new ScrollView(this);
                scroll.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(140)));
                LinearLayout list = new LinearLayout(this);
                list.setOrientation(LinearLayout.VERTICAL);

                for (int i = 0; i < index.length(); i++) {
                    final String chatName = index.getString(i);
                    String msgsStr = prefs.getString("chat_" + chatName, "[]");
                    JSONArray msgs = new JSONArray(msgsStr);
                    int count = msgs.length();
                    String last = count > 0 ? msgs.getString(count - 1) : "";
                    if (last.length() > 40) last = last.substring(0, 40) + "...";
                    boolean isActive = chatName.equals(activeChat);

                    LinearLayout item = new LinearLayout(this);
                    item.setOrientation(LinearLayout.HORIZONTAL);
                    item.setGravity(Gravity.CENTER_VERTICAL);
                    item.setPadding(dp(10), dp(8), dp(10), dp(8));
                    GradientDrawable ibg = new GradientDrawable();
                    ibg.setCornerRadius(dp(10));
                    if (isActive) {
                        ibg.setColor(Color.parseColor("#0d3320"));
                        ibg.setStroke(dp(1), Color.parseColor("#22c55e"));
                    } else {
                        ibg.setColor(Color.parseColor("#1a1a26"));
                        ibg.setStroke(dp(1), Color.parseColor("#2a2a36"));
                    }
                    item.setBackground(ibg);
                    LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    ilp.setMargins(0, 0, 0, dp(4));
                    item.setLayoutParams(ilp);

                    LinearLayout info = new LinearLayout(this);
                    info.setOrientation(LinearLayout.VERTICAL);
                    info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                    TextView name = new TextView(this);
                    name.setText((isActive ? "✅ " : "💬 ") + chatName);
                    name.setTextColor(isActive ? Color.parseColor("#22c55e") : Color.parseColor("#f0f0f5"));
                    name.setTextSize(12);
                    name.setTypeface(null, Typeface.BOLD);
                    info.addView(name);

                    TextView details = new TextView(this);
                    details.setText(count + " msgs" + (last.isEmpty() ? "" : " • " + last));
                    details.setTextColor(Color.parseColor("#9898b0"));
                    details.setTextSize(9);
                    details.setSingleLine(true);
                    info.addView(details);

                    item.addView(info);

                    Button connectBtn = new Button(this);
                    connectBtn.setText(isActive ? "Active" : "Connect");
                    connectBtn.setTextSize(9);
                    connectBtn.setAllCaps(false);
                    connectBtn.setTextColor(Color.WHITE);
                    connectBtn.setPadding(dp(10), dp(3), dp(10), dp(3));
                    GradientDrawable cbg = new GradientDrawable();
                    cbg.setCornerRadius(dp(8));
                    cbg.setColor(isActive ? Color.parseColor("#22c55e") : Color.parseColor("#a855f7"));
                    connectBtn.setBackground(cbg);
                    connectBtn.setEnabled(!isActive);
                    connectBtn.setOnClickListener(v -> connectToChat(chatName));
                    item.addView(connectBtn);

                    list.addView(item);
                }
                scroll.addView(list);
                chatPickerPanel.addView(scroll);
            }
        } catch (Exception e) {}

        Button backBtn = new Button(this);
        backBtn.setText("⌨️ Back to Keyboard");
        backBtn.setTextSize(11);
        backBtn.setAllCaps(false);
        backBtn.setTextColor(Color.WHITE);
        backBtn.setPadding(dp(10), dp(6), dp(10), dp(6));
        GradientDrawable bbg = new GradientDrawable();
        bbg.setCornerRadius(dp(10));
        bbg.setColor(Color.parseColor("#6366f1"));
        backBtn.setBackground(bbg);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(36));
        blp.setMargins(0, dp(6), 0, 0);
        backBtn.setLayoutParams(blp);
        backBtn.setOnClickListener(v -> toggleChatPicker());
        chatPickerPanel.addView(backBtn);
    }

    private void connectToChat(String name) {
        activeChat = name;
        loadChatMessages(name);
        updateChatBtnLabel();
        rebuildQwerty();
        showingChatPicker = false;
        chatPickerPanel.setVisibility(View.GONE);
        qwertyContainer.setVisibility(View.VISIBLE);
        statusLabel.setText("🧠 Connected to " + name + " — AI reads full history");
    }

    private void loadChatMessages(String name) {
        activeChatMessages.clear();
        SharedPreferences prefs = getSharedPreferences(CHAT_PREFS, MODE_PRIVATE);
        String msgsStr = prefs.getString("chat_" + name, "[]");
        try {
            JSONArray arr = new JSONArray(msgsStr);
            for (int i = 0; i < arr.length(); i++) {
                activeChatMessages.add(arr.getString(i));
            }
        } catch (Exception e) {}
    }

    private void saveChatMessages() {
        if (activeChat == null) return;
        SharedPreferences prefs = getSharedPreferences(CHAT_PREFS, MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        for (String msg : activeChatMessages) arr.put(msg);
        prefs.edit()
            .putString("chat_" + activeChat, arr.toString())
            .putLong("chat_updated_" + activeChat, System.currentTimeMillis())
            .apply();

        String indexStr = prefs.getString("chats_index", "[]");
        try {
            JSONArray index = new JSONArray(indexStr);
            boolean found = false;
            for (int i = 0; i < index.length(); i++) {
                if (index.getString(i).equals(activeChat)) { found = true; break; }
            }
            if (!found) {
                index.put(activeChat);
                prefs.edit().putString("chats_index", index.toString()).apply();
            }
        } catch (Exception e) {}
    }

    private void addMessageToChat(String msg) {
        if (activeChat == null) return;
        activeChatMessages.add(msg);
        saveChatMessages();
    }

    private void rebuildQwerty() {
        qwertyContainer.removeAllViews();
        letterKeys.clear();
        if (isNumbers) {
            addKeyRow(qwertyContainer, NUM_ROW1);
            addKeyRow(qwertyContainer, NUM_ROW2);
            addShiftRow(qwertyContainer, NUM_ROW3);
        } else {
            addKeyRow(qwertyContainer, ROW1);
            addKeyRow(qwertyContainer, ROW2);
            addShiftRow(qwertyContainer, ROW3);
        }
        addBottomRow(qwertyContainer);
    }

    // ===== PANEL TOGGLES =====
    private void toggleRizzPanel() {
        showingRizz = !showingRizz;
        showingChatPicker = false;
        chatPickerPanel.setVisibility(View.GONE);
        if (showingRizz) {
            qwertyContainer.setVisibility(View.GONE);
            rizzPanel.setVisibility(View.VISIBLE);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(10));
            bg.setColor(Color.parseColor("#ec4899"));
            rizzToggleBtn.setBackground(bg);
            rizzToggleBtn.setText("⌨️");
            autoReadMessages();
        } else {
            rizzPanel.setVisibility(View.GONE);
            qwertyContainer.setVisibility(View.VISIBLE);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(10));
            bg.setColor(Color.parseColor("#a855f7"));
            rizzToggleBtn.setBackground(bg);
            rizzToggleBtn.setText("⚡ Rizz");
        }
    }

    // ===== AUTO READ =====
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
                    if (msg.length() > 2) { if (recent.length() > 0) recent.append("\n"); recent.append(msg); }
                }
                if (recent.length() > 3) { readText = recent.toString(); source = appName != null && !appName.isEmpty() ? appName : "Screen"; }
            }
        } catch (Exception e) {}

        if (readText.isEmpty()) {
            try {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && clipboard.hasPrimaryClip()) {
                    ClipData clip = clipboard.getPrimaryClip();
                    if (clip != null && clip.getItemCount() > 0) {
                        CharSequence ct = clip.getItemAt(0).getText();
                        if (ct != null && ct.length() > 0) { readText = ct.toString().trim(); source = "Clipboard"; }
                    }
                }
            } catch (Exception e) {}
        }

        if (readText.isEmpty()) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ExtractedText et = ic.getExtractedText(new ExtractedTextRequest(), 0);
                if (et != null && et.text != null && et.text.length() > 0) { readText = et.text.toString().trim(); source = "Input"; }
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
            boolean accOn = RizzAccessibilityService.isRunning();
            statusLabel.setText(accOn ? "📋 No messages — open a chat or copy text" : "⚠️ Enable Screen Reader: Settings → Accessibility → RizzGPT");
        } else {
            rizzInput.setText(readText);
            String preview = readText.length() > 40 ? readText.substring(0, 40) + "..." : readText;
            statusLabel.setText("📖 " + source + ": " + preview);

            if (activeChat != null) {
                String[] lines = readText.split("\n");
                for (String line : lines) {
                    String l = line.trim();
                    if (l.length() > 2) {
                        boolean isDuplicate = false;
                        for (int i = Math.max(0, activeChatMessages.size() - 10); i < activeChatMessages.size(); i++) {
                            if (activeChatMessages.get(i).equals("Them: " + l) || activeChatMessages.get(i).equals(l)) {
                                isDuplicate = true; break;
                            }
                        }
                        if (!isDuplicate) {
                            addMessageToChat("Them: " + l);
                        }
                    }
                }
                statusLabel.setText("📖 " + source + " → auto-saved to " + activeChat);
            }
        }
    }

    // ===== RIZZ PANEL =====
    private HorizontalScrollView createTouchScrollView() {
        HorizontalScrollView sv = new HorizontalScrollView(this) {
            private float startX, startY;
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = ev.getX();
                        startY = ev.getY();
                        getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = Math.abs(ev.getX() - startX);
                        float dy = Math.abs(ev.getY() - startY);
                        if (dx > dy) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                            return true;
                        }
                        break;
                }
                return super.onInterceptTouchEvent(ev);
            }
            @Override
            public boolean onTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.onTouchEvent(ev);
            }
        };
        sv.setHorizontalScrollBarEnabled(false);
        sv.setPadding(0, 0, 0, dp(2));
        sv.setFillViewport(false);
        sv.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        return sv;
    }

    private void buildRizzPanel() {
        rizzPanel = new LinearLayout(this);
        rizzPanel.setOrientation(LinearLayout.VERTICAL);
        rizzPanel.setPadding(dp(4), dp(2), dp(4), dp(2));

        HorizontalScrollView toolScroll = createTouchScrollView();
        toolsContainer = new LinearLayout(this);
        toolsContainer.setOrientation(LinearLayout.HORIZONTAL);
        for (String[] tool : TOOLS) {
            Button chip = makeChip(tool[0] + " " + tool[1], tool[2], tool[2].equals("reply"));
            chip.setOnClickListener(v -> selectTool(chip));
            toolsContainer.addView(chip);
        }
        toolScroll.addView(toolsContainer);
        rizzPanel.addView(toolScroll);

        HorizontalScrollView modeScroll = createTouchScrollView();
        modesContainer = new LinearLayout(this);
        modesContainer.setOrientation(LinearLayout.HORIZONTAL);
        String[][] modes = {
            {"😏 Smooth", "smooth"}, {"😂 Funny", "funny"}, {"🔥 Bold", "bold"},
            {"💜 Flirty", "flirty"}, {"💀 Savage", "savage"}, {"🍯 Sweet", "sweet"},
            {"🇮🇳 Hinglish", "lang_hinglish"}, {"🇬🇧 English", "lang_english"}, {"🇮🇳 Hindi", "lang_hindi"},
            {"🔥 Extreme", "int_extreme"}, {"💪 Bold", "int_bold"}, {"🌸 Mild", "int_mild"}
        };
        for (String[] m : modes) {
            boolean active = m[1].equals("smooth") || m[1].equals("lang_english") || m[1].equals("int_bold");
            Button chip = makeChip(m[0], m[1], active);
            chip.setOnClickListener(v -> selectMode(chip));
            modesContainer.addView(chip);
        }
        modeScroll.addView(modesContainer);
        rizzPanel.addView(modeScroll);

        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        inputRow.setPadding(0, dp(2), 0, dp(2));

        Button readBtn = makeRoundBtn("📖", Color.parseColor("#10b981"), dp(30));
        readBtn.setOnClickListener(v -> autoReadMessages());
        inputRow.addView(readBtn);

        rizzInput = new EditText(this);
        rizzInput.setHint(activeChat != null ? "🧠 " + activeChat + " — context auto-loaded" : "Copy msg → 📖 or type...");
        rizzInput.setHintTextColor(Color.parseColor("#5a5a72"));
        rizzInput.setTextColor(Color.parseColor("#f0f0f5"));
        rizzInput.setTextSize(11);
        rizzInput.setSingleLine(false);
        rizzInput.setMaxLines(2);
        rizzInput.setPadding(dp(10), dp(4), dp(10), dp(4));
        rizzInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        GradientDrawable ibg = new GradientDrawable();
        ibg.setCornerRadius(dp(12));
        ibg.setColor(Color.parseColor("#1a1a26"));
        ibg.setStroke(dp(1), Color.parseColor("#2a2a36"));
        rizzInput.setBackground(ibg);
        rizzInput.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        ((LinearLayout.LayoutParams) rizzInput.getLayoutParams()).setMargins(dp(4), 0, dp(4), 0);
        inputRow.addView(rizzInput);

        generateBtn = makeRoundBtn("⚡", Color.parseColor("#a855f7"), dp(30));
        generateBtn.setOnClickListener(v -> onGenerate());
        inputRow.addView(generateBtn);

        rizzPanel.addView(inputRow);

        statusLabel = new TextView(this);
        statusLabel.setText(activeChat != null ? "🧠 Connected: " + activeChat + " — AI reads full history" : "📋 Copy msg → Read | Pick tool → ⚡");
        statusLabel.setTextColor(Color.parseColor("#9898b0"));
        statusLabel.setTextSize(9);
        statusLabel.setGravity(Gravity.CENTER);
        statusLabel.setPadding(0, dp(1), 0, dp(1));
        rizzPanel.addView(statusLabel);

        ScrollView sugScroll = new ScrollView(this);
        sugScroll.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(100)));
        suggestionsContainer = new LinearLayout(this);
        suggestionsContainer.setOrientation(LinearLayout.VERTICAL);
        suggestionsContainer.setPadding(0, dp(2), 0, dp(2));
        sugScroll.addView(suggestionsContainer);
        rizzPanel.addView(sugScroll);

        rootLayout.addView(rizzPanel);
    }

    private Button makeChip(String text, String tag, boolean active) {
        Button chip = new Button(this);
        chip.setText(text);
        chip.setTextSize(9);
        chip.setAllCaps(false);
        chip.setTag(tag);
        chip.setPadding(dp(8), dp(1), dp(8), dp(1));
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
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, dp(26));
        p.setMargins(0, 0, dp(4), 0);
        chip.setLayoutParams(p);
        return chip;
    }

    private Button makeRoundBtn(String text, int color, int size) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextSize(13);
        btn.setPadding(0, 0, 0, 0);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(15));
        bg.setColor(color);
        btn.setBackground(bg);
        btn.setTextColor(Color.WHITE);
        btn.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        return btn;
    }

    private void selectTool(Button selected) {
        selectedTool = (String) selected.getTag();
        for (int i = 0; i < toolsContainer.getChildCount(); i++) {
            Button c = (Button) toolsContainer.getChildAt(i);
            setChipActive(c, c == selected);
        }
        updateHintForTool();
    }

    private void updateHintForTool() {
        String prefix = activeChat != null ? "🧠 " + activeChat + ": " : "";
        switch (selectedTool) {
            case "reply": rizzInput.setHint(prefix + "Their message to reply to..."); break;
            case "coach": rizzInput.setHint(prefix + "Paste conversation..."); break;
            case "wingman": rizzInput.setHint(prefix + "Their last message..."); break;
            case "mindgame": rizzInput.setHint(prefix + "Paste chat for mind games..."); break;
            case "manipulate": rizzInput.setHint(prefix + "Paste for manipulation..."); break;
            case "powermove": rizzInput.setHint(prefix + "Paste for power move..."); break;
            case "profileRoast": rizzInput.setHint("Paste dating profile bio..."); break;
            case "responsePred": rizzInput.setHint(prefix + "What did you send?"); break;
            case "remixer": rizzInput.setHint("Line to remix..."); break;
            case "situational": rizzInput.setHint("Situation: coffee shop, gym..."); break;
            default: rizzInput.setHint(prefix + "Enter context..."); break;
        }
        statusLabel.setText("🔧 " + getToolName(selectedTool));
    }

    private String getToolName(String tool) {
        for (String[] t : TOOLS) { if (t[2].equals(tool)) return t[0] + " " + t[1]; }
        return tool;
    }

    private void selectMode(Button selected) {
        String tag = (String) selected.getTag();
        if (tag.startsWith("lang_")) {
            selectedLang = tag.replace("lang_", "");
            for (int i = 0; i < modesContainer.getChildCount(); i++) {
                Button c = (Button) modesContainer.getChildAt(i);
                if (((String) c.getTag()).startsWith("lang_")) setChipActive(c, c == selected);
            }
        } else if (tag.startsWith("int_")) {
            selectedIntensity = tag.replace("int_", "");
            for (int i = 0; i < modesContainer.getChildCount(); i++) {
                Button c = (Button) modesContainer.getChildAt(i);
                if (((String) c.getTag()).startsWith("int_")) setChipActive(c, c == selected);
            }
        } else {
            selectedMode = tag;
            for (int i = 0; i < modesContainer.getChildCount(); i++) {
                Button c = (Button) modesContainer.getChildAt(i);
                String t = (String) c.getTag();
                if (!t.startsWith("lang_") && !t.startsWith("int_")) setChipActive(c, c == selected);
            }
        }
    }

    private void setChipActive(Button chip, boolean active) {
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

    // ===== GENERATE =====
    private void onGenerate() {
        if (isLoading) return;
        String context = rizzInput.getText().toString().trim();
        if (context.isEmpty() && activeChat == null && !selectedTool.equals("pickup") && !selectedTool.equals("trendingLines")) {
            statusLabel.setText("⚠️ Enter context or connect a chat first");
            return;
        }
        runTool(context, selectedTool, selectedMode);
    }

    private String getChatContext() {
        if (activeChat == null || activeChatMessages.isEmpty()) return "";
        int start = Math.max(0, activeChatMessages.size() - 25);
        StringBuilder sb = new StringBuilder();
        sb.append("[FULL CHAT HISTORY with ").append(activeChat).append("]\n");
        for (int i = start; i < activeChatMessages.size(); i++) {
            sb.append(activeChatMessages.get(i)).append("\n");
        }
        return sb.toString();
    }

    private String getLangInstruction() {
        switch (selectedLang) {
            case "hinglish": return " Write in Hinglish (Hindi+English mix, Roman script) like young Indians text on WhatsApp.";
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
            statusLabel.setText(emoji + " Generating" + (activeChat != null ? " for " + activeChat : "") + "...");
            generateBtn.setEnabled(false);
            suggestionsContainer.removeAllViews();
        });

        executor.execute(() -> {
            try {
                String chatCtx = getChatContext();
                String fullContext = chatCtx.isEmpty() ? context : chatCtx + "\n[Current input]: " + context;

                String systemPrompt = buildSystemPrompt(tool, style, fullContext);
                String userPrompt = buildUserPrompt(tool, style, fullContext);

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
                    .getJSONObject(0).getJSONObject("message").getString("content");

                List<String> replies = parseReplies(content, tool);

                mainHandler.post(() -> {
                    isLoading = false;
                    generateBtn.setEnabled(true);
                    if (replies.isEmpty()) { statusLabel.setText("❌ No results. Try again!"); return; }
                    statusLabel.setText(emoji + " Tap to type ↓" + (activeChat != null ? " (auto-saves to " + activeChat + ")" : ""));
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
        String lang = getLangInstruction();
        String intNote = getIntensityDesc();
        boolean hasChat = activeChat != null && !activeChatMessages.isEmpty();
        String chatNote = hasChat ? " You have access to the FULL chat history with " + activeChat + ". Use it to understand their personality, texting patterns, interest level, and conversation dynamics. Generate responses that fit perfectly into this specific conversation." : "";

        switch (tool) {
            case "reply":
                return "You generate text message replies. Style: " + style + ". Intensity: " + intNote + "." + lang + chatNote +
                    "\nRules: 1. Type like real person, lowercase casual 2. Short 5-20 words 3. Sound human NOT AI 4. Give exactly 5 replies numbered 1-5";
            case "coach":
                return "You're a dating coach." + lang + chatNote +
                    "\nFormat: VIBE CHECK, GREEN FLAGS, RED FLAGS, 3 TIPS, NEXT MOVE, 5 REPLIES numbered 1-5";
            case "wingman":
                return "You are an AI Wingman." + lang + chatNote +
                    "\nGive: MOOD, INTEREST /10, 5 QUICK REPLIES numbered, DO NOT SAY, STRATEGY";
            case "mindgame":
                return "You're a master psychological strategist." + lang + chatNote +
                    "\nGive: CURRENT DYNAMIC, THEIR PSYCHOLOGY, 5 MIND GAMES numbered 1-5 with exact messages. Include push-pull, scarcity, jealousy.";
            case "manipulate":
                return "You're a manipulation expert." + lang + chatNote +
                    "\nGive: VULNERABILITY MAP, EMOTIONAL HOOKS, 5 MANIPULATION MESSAGES numbered 1-5 to create dependency. Include intermittent reinforcement.";
            case "powermove":
                return "You're a power dynamics expert." + lang + chatNote +
                    "\nGive: POWER SCORE, THE POWER MOVE (1 devastating msg), 5 ALTERNATIVES numbered 1-5 with timing.";
            case "profileRoast":
                return "You're a dating profile reviewer." + lang + "\nROAST, SCORE /10, 5 IMPROVED BIOS numbered 1-5.";
            case "responsePred":
                return "You predict text responses." + lang + chatNote + "\n5 predictions numbered 1-5 with probability %.";
            case "remixer":
                return "You remix pickup lines. Style: " + style + "." + lang + "\n5 remixed versions numbered 1-5.";
            case "situational":
                return "You generate situation-specific openers. Style: " + style + "." + lang + "\n5 openers numbered 1-5.";
            case "flowBuilder":
                return "You build conversation flows." + lang + chatNote + "\n5 next messages numbered 1-5 plus flow strategy.";
            case "compatibility":
                return "You analyze zodiac/MBTI compatibility." + lang + "\nMATCH /10, STRENGTHS, CHALLENGES, 5 STARTERS numbered 1-5.";
            case "emojiDecoder":
                return "You decode emojis." + lang + "\nDecode, MEANING, INTEREST /10, 5 REPLY OPTIONS numbered 1-5.";
            case "dateIdea":
                return "You generate date ideas." + lang + "\n5 date ideas numbered 1-5 with details.";
            case "complimentSched":
                return "You generate compliments. Style: " + style + "." + lang + "\n5 compliments numbered 1-5.";
            case "redFlag":
                return "You detect red/green flags." + lang + chatNote + "\nRED FLAGS, GREEN FLAGS, RATING /10, 5 RESPONSES numbered 1-5.";
            case "saveConvo":
                return "You rescue dying conversations." + lang + chatNote + "\nDIAGNOSIS, 5 RECOVERY MESSAGES numbered 1-5.";
            case "antiCringe":
                return "You're a cringe detector." + lang + "\nCRINGE SCORE /10, 5 IMPROVED VERSIONS numbered 1-5.";
            case "toneTranslator":
                return "You translate message tone to: " + style + "." + lang + "\n5 versions numbered 1-5.";
            case "perspectiveFlip":
                return "You flip perspective." + lang + chatNote + "\nIMPRESSION, INTEREST /10, 5 BETTER VERSIONS numbered 1-5.";
            case "ghostRecovery":
                return "You recover from ghosting." + lang + chatNote + "\nDIAGNOSIS, 5 COMEBACKS numbered 1-5.";
            case "rizzRater":
                return "You rate rizz." + lang + "\nSCORE /10, BREAKDOWN, 5 UPGRADES numbered 1-5.";
            case "bioBeforeAfter":
                return "You transform bios." + lang + "\nSCORE, PROBLEMS, 5 TRANSFORMS numbered 1-5.";
            case "dateScript":
                return "You write date scripts." + lang + "\nFULL SCRIPT, 5 KEY LINES numbered 1-5.";
            case "trendingLines":
                return "You generate trending pickup lines. Style: " + style + "." + lang + "\n5 trending lines numbered 1-5.";
            case "msgDecoder":
                return "You decode messages." + lang + chatNote + "\nSURFACE, HIDDEN, INTEREST /10, 5 REPLIES numbered 1-5.";
            case "pickup":
                return "You generate pickup lines. Style: " + style + ". Intensity: " + intNote + "." + lang + "\n5 lines numbered 1-5.";
            case "flirty":
                return "You generate flirty texts. Intensity: " + intNote + "." + lang + "\n5 messages numbered 1-5.";
            case "savage":
                return "You generate savage replies. Intensity: " + intNote + "." + lang + "\n5 replies numbered 1-5.";
            default:
                return "You generate text replies. Style: " + style + "." + lang + "\n5 replies numbered 1-5.";
        }
    }

    private String buildUserPrompt(String tool, String style, String context) {
        if (context.isEmpty()) context = "general/any topic";
        switch (tool) {
            case "reply": return "Generate 5 " + style + " replies to: \"" + context + "\"";
            case "coach": return "Coach me on:\n" + context;
            case "wingman": return "Wingman: " + context;
            case "mindgame": return "Mind game strategy:\n" + context;
            case "manipulate": return "Manipulation playbook:\n" + context;
            case "powermove": return "Power move:\n" + context;
            case "profileRoast": return "Roast bio:\n\"" + context + "\"";
            case "responsePred": return "Predict response to: \"" + context + "\"";
            case "remixer": return "Remix: \"" + context + "\"";
            case "situational": return "Openers for: " + context;
            case "flowBuilder": return "Build flow from: \"" + context + "\"";
            case "compatibility": return "Analyze: " + context;
            case "emojiDecoder": return "Decode: " + context;
            case "dateIdea": return "Date ideas for: " + context;
            case "complimentSched": return "Compliments for: " + context;
            case "redFlag": return "Detect flags: \"" + context + "\"";
            case "saveConvo": return "Save convo:\n" + context;
            case "antiCringe": return "Cringe-check: \"" + context + "\"";
            case "toneTranslator": return "Translate to " + style + ": \"" + context + "\"";
            case "perspectiveFlip": return "Flip perspective: \"" + context + "\"";
            case "ghostRecovery": return "Ghost recovery:\n" + context;
            case "rizzRater": return "Rate: \"" + context + "\"";
            case "bioBeforeAfter": return "Transform bio:\n\"" + context + "\"";
            case "dateScript": return "Date script for: " + context;
            case "trendingLines": return "Trending lines: " + context;
            case "msgDecoder": return "Decode: \"" + context + "\"";
            case "pickup": return "5 " + style + " pickup lines" + (context.length() > 20 ? " about: " + context : "");
            case "flirty": return "5 flirty texts for: " + context;
            case "savage": return "5 savage replies to: \"" + context + "\"";
            default: return "Generate 5 replies to: \"" + context + "\"";
        }
    }

    private List<String> parseReplies(String content, String tool) {
        List<String> replies = new ArrayList<>();
        boolean isAnalysis = tool.equals("coach") || tool.equals("wingman") || tool.equals("mindgame") ||
            tool.equals("manipulate") || tool.equals("powermove") || tool.equals("profileRoast") ||
            tool.equals("responsePred") || tool.equals("compatibility") || tool.equals("emojiDecoder") ||
            tool.equals("redFlag") || tool.equals("antiCringe") || tool.equals("perspectiveFlip") ||
            tool.equals("rizzRater") || tool.equals("bioBeforeAfter") || tool.equals("dateScript") ||
            tool.equals("msgDecoder") || tool.equals("dateIdea");

        if (isAnalysis) {
            String[] sections = content.split("\n\n");
            StringBuilder analysis = new StringBuilder();
            List<String> items = new ArrayList<>();
            for (String section : sections) {
                String trimmed = section.trim();
                if (trimmed.isEmpty()) continue;
                boolean hasNum = false;
                for (String line : trimmed.split("\n")) {
                    String cl = line.trim();
                    if (cl.matches("^\\d+[.)\\-:\\s]+.+") && cl.length() > 8) {
                        String item = cl.replaceAll("^\\d+[.)\\-:\\s]+", "").replaceAll("^\"|\"$", "").trim();
                        if (item.length() > 3) { items.add(item); hasNum = true; }
                    }
                }
                if (!hasNum && analysis.length() < 400) {
                    if (analysis.length() > 0) analysis.append("\n");
                    analysis.append(trimmed);
                }
            }
            if (analysis.length() > 0) {
                String a = analysis.toString();
                if (a.length() > 200) a = a.substring(0, 200) + "...";
                replies.add("📊 " + a);
            }
            for (String item : items) { replies.add(item); if (replies.size() >= 8) break; }
        } else {
            for (String l : content.split("\n")) {
                String cl = l.trim().replaceAll("^\\d+[.)\\-:\\s]+", "").replaceAll("^\"|\"$", "").trim();
                if (cl.length() > 3 && !cl.startsWith("#") && !cl.startsWith("*") && !cl.startsWith("---"))
                    replies.add(cl);
            }
        }
        return replies;
    }

    private void showReplies(List<String> replies) {
        suggestionsContainer.removeAllViews();
        int count = Math.min(replies.size(), 8);
        for (int i = 0; i < count; i++) {
            final String reply = replies.get(i);
            final boolean isInfo = reply.startsWith("📊 ");

            Button btn = new Button(this);
            btn.setText(reply);
            btn.setTextSize(11);
            btn.setAllCaps(false);
            btn.setTextColor(Color.parseColor(isInfo ? "#a0a0c0" : "#f0f0f5"));
            btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            btn.setPadding(dp(10), dp(4), dp(10), dp(4));

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(8));
            bg.setColor(Color.parseColor(isInfo ? "#12121e" : "#1a1a26"));
            bg.setStroke(dp(1), Color.parseColor(isInfo ? "#333355" : "#2a1a3a"));
            btn.setBackground(bg);

            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            p.setMargins(0, 0, 0, dp(2));
            btn.setLayoutParams(p);

            btn.setOnClickListener(v -> {
                String text = isInfo ? reply.substring(2).trim() : reply;
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) ic.commitText(text, 1);

                if (activeChat != null) {
                    addMessageToChat("Me: " + text);
                    statusLabel.setText("✅ Typed + saved to " + activeChat);
                } else {
                    statusLabel.setText("✅ Typed! Hit send");
                }

                showingRizz = false;
                rizzPanel.setVisibility(View.GONE);
                qwertyContainer.setVisibility(View.VISIBLE);
                GradientDrawable tbg = new GradientDrawable();
                tbg.setCornerRadius(dp(10));
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
