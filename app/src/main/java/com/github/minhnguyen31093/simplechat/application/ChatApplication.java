package com.github.minhnguyen31093.simplechat.application;

import android.app.Application;
import android.support.annotation.Nullable;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.FontRequestEmojiCompatConfig;
import android.support.v4.provider.FontRequest;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.github.minhnguyen31093.simplechat.R;


public class ChatApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        initEmojiCompat();
    }

    private void initEmojiCompat() {
        final FontRequest fontRequest = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs);
        final EmojiCompat.Config config = new
                FontRequestEmojiCompatConfig(getApplicationContext(), fontRequest)
                .registerInitCallback(new EmojiCompat.InitCallback() {
                    @Override
                    public void onInitialized() {
                        Log.i("MyApplication", "EmojiCompat initialized");
                    }

                    @Override
                    public void onFailed(@Nullable Throwable throwable) {
                        Log.e("MyApplication", "EmojiCompat initialization failed", throwable);
                    }
                });
        EmojiCompat.init(config);
    }
}
