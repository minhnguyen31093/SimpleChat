package com.github.minhnguyen31093.simplechat.activity;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.customtabs.CustomTabsIntent;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.github.minhnguyen31093.simplechat.R;
import com.github.minhnguyen31093.simplechat.adapter.ChatAdapter;
import com.github.minhnguyen31093.simplechat.models.Message;
import com.github.minhnguyen31093.simplechat.utils.TextUtils;
import com.github.minhnguyen31093.simplechat.views.IMEEditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ChatAdapter chatAdapter;

    ConstraintLayout cslChat;
    RecyclerView rvChat;
    IMEEditText edtChat;
    ImageButton btnSend;

    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int heightDiff = cslChat.getRootView().getHeight() - cslChat.getHeight();
            int contentViewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
            if (heightDiff <= contentViewTop) {
                edtChat.clearFocus();
            } else {
                if (edtChat.isFocused() && rvChat.getAdapter() != null && rvChat.getAdapter().getItemCount() > 0) {
                    rvChat.smoothScrollToPosition(rvChat.getAdapter().getItemCount() - 1);
                }
            }
        }
    };

    private IMEEditText.KeyBoardInputCallbackListener keyBoardInputCallbackListener = new IMEEditText.KeyBoardInputCallbackListener() {
        @Override
        public void onCommitContent(InputContentInfoCompat inputContentInfo, int flags, Bundle opts) {
            if (inputContentInfo != null && inputContentInfo.getLinkUri() != null && inputContentInfo.getLinkUri().toString() != null && !inputContentInfo.getLinkUri().toString().isEmpty()) {
                TextUtils.hideKeyboardWhenNotFocus(MainActivity.this);
                chatAdapter.insert(IMEEditText.TAG + inputContentInfo.getLinkUri().toString());
                Log.i("Message", IMEEditText.TAG + inputContentInfo.getLinkUri().toString());
            }
        }
    };

    private ChatAdapter.OnItemClickListener onItemClickListener = new ChatAdapter.OnItemClickListener() {
        @Override
        public void onHashTag(String hashTag) {
            if (hashTag != null && !hashTag.isEmpty()) {
                String url = "https://www.google.com.vn/search?q=%23" + hashTag;
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(MainActivity.this, Uri.parse(url));
            }
        }

        @Override
        public void onTag(String tag) {
            if (tag != null && !tag.isEmpty()) {
                String url = "https://www.google.com.vn/search?q=%40" + tag;
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(MainActivity.this, Uri.parse(url));
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextUtils.hideKeyboardWhenNotFocus(MainActivity.this);
            if (!edtChat.getText().toString().isEmpty()) {
                chatAdapter.insert(edtChat.getText().toString());
                Log.i("Message", edtChat.getText().toString());
                edtChat.setText("");
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null && (view instanceof AppCompatEditText || view instanceof EditText)) {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    view.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert imm != null;
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_chat);
        cslChat = findViewById(R.id.cslChat);
        rvChat = findViewById(R.id.rvChat);
        edtChat = findViewById(R.id.edtChat);
        btnSend = findViewById(R.id.btnSend);

        rvChat.setLayoutManager(new LinearLayoutManager(this));
        cslChat.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
        edtChat.setKeyBoardInputCallbackListener(keyBoardInputCallbackListener);
        btnSend.setOnClickListener(onClickListener);

        chatAdapter = new ChatAdapter(getSample(), onItemClickListener);
        rvChat.setAdapter(chatAdapter);
    }

    private List<Message> getSample() {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("#dbz"));
        messages.add(new Message("@therock"));
        messages.add(new Message("https://fir-ui-demo-84a6c.firebaseapp.com/widget#recaptcha=normal"));
        messages.add(new Message("01654987321"));
        messages.add(new Message("[image/mime_link]https://media3.giphy.com/media/3M4NpbLCTxBqU/giphy.gif"));
        messages.add(new Message("[image/mime_link]https://www.gstatic.com/allo/stickers/pack-100001/v3/xxhdpi/10.gif"));
        messages.add(new Message("\uD83E\uDD2A\uD83E\uDD2E\uD83E\uDD2D\uD83E\uDD2C\uD83E\uDD23\uD83E\uDD2B\uD83E\uDD2F\uD83E\uDD29"));
        messages.add(new Message("https://images-na.ssl-images-amazon.com/images/I/41km3E3lW8L._SY400_.jpg"));
        return messages;
    }
}
