package com.github.minhnguyen31093.simplechat.views;

import android.content.Context;
import android.os.Bundle;
import android.support.text.emoji.widget.EmojiAppCompatEditText;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.os.BuildCompat;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * Created by minh on 3/8/18.
 */

public class IMEEditText extends EmojiAppCompatEditText {

    public static final String TAG = "[image/mime_link]";
    private String[] imgTypeString;
    private KeyBoardInputCallbackListener keyBoardInputCallbackListener;

    public IMEEditText(Context context) {
        super(context);
        initView();
    }

    public IMEEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public IMEEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        imgTypeString = new String[]{"image/png", "image/gif", "image/jpeg", "image/webp"};
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        final InputConnection ic = super.onCreateInputConnection(outAttrs);
        EditorInfoCompat.setContentMimeTypes(outAttrs, imgTypeString);
        return InputConnectionCompat.createWrapper(ic, outAttrs, callback);
    }


    final InputConnectionCompat.OnCommitContentListener callback = new InputConnectionCompat.OnCommitContentListener() {
        @Override
        public boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags, Bundle opts) {

            // read and display inputContentInfo asynchronously
            if (BuildCompat.isAtLeastNMR1() && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                try {
                    inputContentInfo.requestPermission();
                } catch (Exception e) {
                    return false; // return false if failed
                }
            }
            boolean supported = false;
            for (final String mimeType : imgTypeString) {
                if (inputContentInfo.getDescription().hasMimeType(mimeType)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                return false;
            }

            if (keyBoardInputCallbackListener != null) {
                keyBoardInputCallbackListener.onCommitContent(inputContentInfo, flags, opts);
            }
            return true;  // return true if succeeded
        }
    };

    public interface KeyBoardInputCallbackListener {
        void onCommitContent(InputContentInfoCompat inputContentInfo, int flags, Bundle opts);
    }

    public void setKeyBoardInputCallbackListener(KeyBoardInputCallbackListener keyBoardInputCallbackListener) {
        this.keyBoardInputCallbackListener = keyBoardInputCallbackListener;
    }

    public String[] getImgTypeString() {
        return imgTypeString;
    }

    public void setImgTypeString(String[] imgTypeString) {
        this.imgTypeString = imgTypeString;
    }
}
