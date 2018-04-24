package com.github.minhnguyen31093.simplechat.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

    public static String checkUrl(String url) {
        if (url != null && !url.isEmpty() && !url.startsWith("http")) {
            url = "http://" + url;
        }
        return url;
    }

    public static void hideKeyboardWhenNotFocus(@NonNull Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void showKeyboardWhenFocus(@NonNull Activity activity) {
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            if (view != null) {
                inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    public static void forceToHideKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static void forceToHideKeyboard(@Nullable Activity activity, @Nullable View view) {
        if (activity != null && view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @SuppressWarnings("deprecation")
    public static Locale getLocale(Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? context.getResources().getConfiguration().getLocales().get(0) : context.getResources().getConfiguration().locale;
    }

    @SuppressWarnings("deprecation")
    public static Spanned text2html(String text) {
        text = text == null ? "" : text;
        text = text.replaceAll("(\r\n|\n)", "<br />");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(text);
        }
    }

    public static boolean isTooLarge(TextView text, String newText) {
        float textWidth = text.getPaint().measureText(newText);
        return (textWidth >= text.getMeasuredWidth());
    }

    public static boolean isTooLarge(TextView textView, String newText, int width) {
        float textWidth = textView.getPaint().measureText(newText);
        return (textWidth >= width);
    }

    public static int countObj(String text, String obj) {
        return text.split(obj).length - 1;
    }

    public static Locale getPrimaryLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            return Resources.getSystem().getConfiguration().locale;
        }
    }

    public static Locale getSecondaryLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = Resources.getSystem().getConfiguration().getLocales();
            if (localeList.size() > 1) {
                return Resources.getSystem().getConfiguration().getLocales().get(1);
            }
        }
        return null;
    }

    @SuppressLint({"NewApi"})
    public static String unAccent(String s) {
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(Normalizer.normalize(s, Normalizer.Form.NFD)).replaceAll("");
    }

    public static String getUnAccentFirstCharacter(@NonNull String s) {
        return unAccent(String.valueOf(s.charAt(0)));
    }

    public static String getUnAcentCanChi(@NonNull String canchi) {
        String rs = unAccent(canchi);
        if (canchi.equalsIgnoreCase("tý")) {
            rs = String.valueOf(rs) + "s";
        } else if (canchi.equalsIgnoreCase("tỵ")) {
            rs = String.valueOf(rs) + "j";
        }
        return rs.toLowerCase();
    }

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
    }

    public static Spanned getComment(String text) {
        text = shortenUrlInText(text);
        text = addHtmlForEmailInText(text);
        text = addHtmlForPhoneInText(text);
        text = addHashTagInText(text);
        text = addTagInText(text);
        return text2html(text);
    }

    public static String shortenUrlInText(String text) {
        String completeText = "", leftText = text;
        Pattern pattern = Pattern.compile("(?<![@.,%&#-])(?<![a-zA-Z0-9])(?<protocol>\\w{2,10}:\\/\\/)?([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?");
        Matcher matcher = pattern.matcher(text);
        boolean result = matcher.find();
        while (result) {
            String longUrl = matcher.group(0);
            String replaceText = "<a href=\"" + longUrl + "\">" + shortenUrl(longUrl) + "</a>";
            leftText = replace(leftText, longUrl, replaceText);
            completeText += leftText.substring(0, leftText.indexOf(replaceText) + replaceText.length());
            leftText = leftText.substring(leftText.indexOf(replaceText) + replaceText.length());

            result = matcher.find();
        }
        if (leftText.length() > 0) {
            completeText += leftText;
        }
        return completeText;
    }

    public static String addHtmlForPhoneInText(String text) {
        String completeText = "", leftText = text;
        Pattern pattern = Patterns.PHONE;
        pattern = Pattern.compile("(?<![@.,%&#-\\//])(?<![a-zA-Z0-9])(\\+[0-9]+[\\- \\.]*)?(\\([0-9]+\\)[\\- \\.]*)?([0-9][0-9\\- \\.]+[0-9])(?![a-zA-Z0-9])");
        Matcher matcher = pattern.matcher(text);
        boolean result = matcher.find();
        while (result) {
            String phone = matcher.group(0);
            String replaceText = "<a href=\"tel:" + phone + "\">" + phone + "</a>";
            leftText = replace(leftText, phone, replaceText);
            completeText += leftText.substring(0, leftText.indexOf(replaceText) + replaceText.length());
            leftText = leftText.substring(leftText.indexOf(replaceText) + replaceText.length());

            result = matcher.find();
        }
        if (leftText.length() > 0) {
            completeText += leftText;
        }
        return completeText;
    }

    public static String addHtmlForEmailInText(String text) {
        String completeText = "", leftText = text;
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        Matcher matcher = pattern.matcher(text);
        boolean result = matcher.find();
        while (result) {
            String email = matcher.group(0);
            String replaceText = "<a href=\"mailto:" + email + "\">" + email + "</a>";
            leftText = replace(leftText, email, replaceText);
            completeText += leftText.substring(0, leftText.indexOf(replaceText) + replaceText.length());
            leftText = leftText.substring(leftText.indexOf(replaceText) + replaceText.length());

            result = matcher.find();
        }
        if (leftText.length() > 0) {
            completeText += leftText;
        }
        return completeText;
    }

    public static String addHashTagInText(String text) {
        String completeText = "", leftText = text;
        Pattern pattern = Pattern.compile("\\B(\\#[a-zA-Z]+\\b)(?!;)");
        Matcher matcher = pattern.matcher(text);
        boolean result = matcher.find();
        while (result) {
            String hashTag = matcher.group(0);
            String replaceText = "<i><font color='#8A0099'>" + hashTag + "</font></i>";
            leftText = replace(leftText, hashTag, replaceText);
            completeText += leftText.substring(0, leftText.indexOf(replaceText) + replaceText.length());
            leftText = leftText.substring(leftText.indexOf(replaceText) + replaceText.length());

            result = matcher.find();
        }
        if (leftText.length() > 0) {
            completeText += leftText;
        }
        return completeText;
    }

    public static String addTagInText(String text) {
        String completeText = "", leftText = text;
        Pattern pattern = Pattern.compile("\\B(\\@[a-zA-Z]+\\b)(?!;)");
        Matcher matcher = pattern.matcher(text);
        boolean result = matcher.find();
        while (result) {
            String tag = matcher.group(0);
            String replaceText = "<font color='#009975'>" + tag + "</font>";
            leftText = replace(leftText, tag, replaceText);
            completeText += leftText.substring(0, leftText.indexOf(replaceText) + replaceText.length());
            leftText = leftText.substring(leftText.indexOf(replaceText) + replaceText.length());

            result = matcher.find();
        }
        if (leftText.length() > 0) {
            completeText += leftText;
        }
        return completeText;
    }

    public static String replace(String text, String regex, String replaceText) {
        String finalText = text.replaceFirst(regex, replaceText);
        if (finalText.equals(text)) {
            finalText = text.replace(regex, replaceText);
        }
        return finalText;
    }

    public static String shortenUrl(String longUrl) {
        try {
            URL url = new URL(longUrl);
            return url.getProtocol() + "://" + url.getHost();
        } catch (MalformedURLException e) {
            Log.e("Error", e.getMessage());
        }
        return longUrl;
    }

    public static String getImageUrl(String text) {
        String regex = "(http(s?):/)(/[^/]+)+\\.(?:jpg|JPG|jpeg|JPEG|gif|GIF|png|PNG|bmp|BMP|webp|WEBP)";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    public static void copyToClipBoard(View view, String label, String text) {
        if (view != null && text != null) {
            ClipboardManager cm = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText(label != null ? label : "", text));
                Toast.makeText(view.getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static String reduce(String text, int max) {
        if (text != null && !text.isEmpty() && text.length() > max) {
            return text.substring(0, max);
        } else {
            return text;
        }
    }

    public static String eclipse(String text, int max) {
        if (text != null && !text.isEmpty() && text.length() > max) {
            return text.substring(0, max - 1) + "…";
        } else {
            return text;
        }
    }

}
