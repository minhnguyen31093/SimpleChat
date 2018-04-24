package com.github.minhnguyen31093.simplechat.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.text.emoji.widget.EmojiAppCompatTextView;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.github.minhnguyen31093.simplechat.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by minh on 10/19/17.
 */

public class LinkClickableTextView extends EmojiAppCompatTextView {

    private static final int TYPE_HTML = 1;
    private static final int TYPE_SPANNABLE = 2;
    public static final String SHARED_CLICKED_HYPER_LINKS = "SHARED_CLICKED_HYPER_LINKS";

    private boolean isShortenUrlEnabled = true, isUrlEnabled = false, isEmailEnabled = true,
            isPhoneEnabled = true, isHashTagEnabled = false, isTagEnabled = false, isCacheEnabled = true;
    private int type;
    private CharSequence realText;
    private List<String> clickedHyperLinks;
    private List<HyperLink> hyperLinks;
    private String spannableText;
    private Pattern patternUrl = Pattern.compile("((?:https\\:\\/\\/)|(?:http\\:\\/\\/)|(?:www\\.))?([a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(?:\\??)[a-zA-Z0-9\\-\\._\\?\\,\\'\\/\\\\\\+&%\\$#\\=~]+)");
    private Pattern patternEmail = Patterns.EMAIL_ADDRESS;
    private Pattern patternPhone = Pattern.compile("(09|01[2|6|8|9])+([0-9]{8})\\b");
    private Pattern patternHashTag = Pattern.compile("\\B(\\#[a-zA-Z]+\\b)(?!;)");
    private Pattern patternTag = Pattern.compile("\\B(\\@[a-zA-Z]+\\b)(?!;)");

    private OnTextClickListener onTextClickListener;

    public LinkClickableTextView(Context context) {
        super(context);
        init(context, null);
    }

    public LinkClickableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LinkClickableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        getClickedHyperLinks();
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LinkClickableTextView, 0, 0);
            try {
                type = ta.getInteger(R.styleable.LinkClickableTextView_ltvType, TYPE_SPANNABLE);
                isShortenUrlEnabled = ta.getBoolean(R.styleable.LinkClickableTextView_ltvShortUrlEnabled, true);
                isUrlEnabled = !isShortenUrlEnabled && ta.getBoolean(R.styleable.LinkClickableTextView_ltvUrlEnabled, false);
                isEmailEnabled = ta.getBoolean(R.styleable.LinkClickableTextView_ltvEmailEnabled, true);
                isPhoneEnabled = ta.getBoolean(R.styleable.LinkClickableTextView_ltvPhoneEnabled, true);
                isHashTagEnabled = ta.getBoolean(R.styleable.LinkClickableTextView_ltvHashTagEnabled, false);
                isTagEnabled = ta.getBoolean(R.styleable.LinkClickableTextView_ltvTagEnabled, false);
                isCacheEnabled = ta.getBoolean(R.styleable.LinkClickableTextView_ltvCacheEnabled, true);
                setLinkTextColor(ContextCompat.getColor(getContext(), R.color.link));
                setText(realText);
            } finally {
                ta.recycle();
            }
        }
        if (type == TYPE_SPANNABLE) {
            setMovementMethod(new LinkTouchMovementMethod());
        } else {
            setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        if (realText == null && LinkClickableTextView.this.type == 0) {
            realText = text;
        } else {
            switch (LinkClickableTextView.this.type) {
                case TYPE_HTML:
                    html(text);
                    break;
                case TYPE_SPANNABLE:
                default:
                    span(text);
                    break;
            }
        }
    }

    private void getClickedHyperLinks() {
        clickedHyperLinks = new ArrayList<>();
        if (isCacheEnabled) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            if (sharedPreferences.contains(SHARED_CLICKED_HYPER_LINKS)) {
                String clicked = sharedPreferences.getString(SHARED_CLICKED_HYPER_LINKS, "");
                if (!clicked.isEmpty()) {
                    if (clicked.contains(", ")) {
                        clickedHyperLinks = new ArrayList<>(Arrays.asList(clicked.split(", ")));
                    } else {
                        clickedHyperLinks.add(clicked);
                    }
                }
            }
        }
    }

    private void setClickedHyperLinks() {
        if (isCacheEnabled && clickedHyperLinks != null && clickedHyperLinks.size() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String clicked = TextUtils.join(", ", clickedHyperLinks);
            sharedPreferences.edit().putString(SHARED_CLICKED_HYPER_LINKS, clicked).apply();
        }
    }

    private void setClickedForHyperLinks() {
        if (isCacheEnabled && clickedHyperLinks != null && clickedHyperLinks.size() > 0 && hyperLinks != null && hyperLinks.size() > 0) {
            for (HyperLink hyperLink : hyperLinks) {
                hyperLink.isClicked = clickedHyperLinks.contains(hyperLink.span);
            }
        }
    }

    private void html(CharSequence text) {
        spannableText = text.toString();
        spannableText = isShortenUrlEnabled ? reformatToHtml(HyperLinkType.URL) : spannableText;
        spannableText = isUrlEnabled ? reformatToHtml(HyperLinkType.URL) : spannableText;
        spannableText = isEmailEnabled ? reformatToHtml(HyperLinkType.EMAIL) : spannableText;
        spannableText = isPhoneEnabled ? reformatToHtml(HyperLinkType.PHONE) : spannableText;
        spannableText = isHashTagEnabled ? reformatToHtml(HyperLinkType.HASH_TAG) : spannableText;
        spannableText = isTagEnabled ? reformatToHtml(HyperLinkType.TAG) : spannableText;
        super.setText(text2html(spannableText), TextView.BufferType.NORMAL);
    }

    private String reformatToHtml(HyperLinkType type) {
        String completeText = "", leftText = spannableText;
        Matcher matcher = getPattern(type).matcher(spannableText);
        boolean result = matcher.find();
        while (result) {
            String match = matcher.group(0);
            String replaceText = match;
            switch (type) {
                case URL:
                    replaceText = "<a href=\"" + match + "\">" + (isShortenUrlEnabled ? shortenUrl(match) : match) + "</a>";
                    break;
                case EMAIL:
                    replaceText = "<a href=\"mailto:" + match + "\">" + match + "</a>";
                    break;
                case PHONE:
                    replaceText = "<a href=\"tel:" + match + "\">" + match + "</a>";
                    break;
                case HASH_TAG:
                    replaceText = "<i><font color='#8A0099'>" + match + "</font></i>";
                    break;
                case TAG:
                    replaceText = "<font color='#009975'>" + match + "</font>";
                    break;
            }
            leftText = replace(leftText, match, replaceText);
            completeText += leftText.substring(0, leftText.indexOf(replaceText) + replaceText.length());
            leftText = leftText.substring(leftText.indexOf(replaceText) + replaceText.length());

            result = matcher.find();
        }
        if (leftText.length() > 0) {
            completeText += leftText;
        }
        return completeText;
    }

    private void span(CharSequence text) {
        hyperLinks = new ArrayList<>();
        spannableText = text.toString();
        if (isShortenUrlEnabled || isUrlEnabled) {
            getHyperlinks(HyperLinkType.URL);
        }
        if (isEmailEnabled) {
            getHyperlinks(HyperLinkType.EMAIL);
        }
        if (isPhoneEnabled) {
            getHyperlinks(HyperLinkType.PHONE);
        }
        if (isHashTagEnabled) {
            getHyperlinks(HyperLinkType.HASH_TAG);
        }
        if (isTagEnabled) {
            getHyperlinks(HyperLinkType.TAG);
        }
        setClickedForHyperLinks();
        SpannableString spannableString = new SpannableString(spannableText);
        for (HyperLink hyperLink : hyperLinks) {
//            if (hyperLink.type == HyperLinkType.HASH_TAG) {
//                spannableString.setSpan(new RoundedBackgroundSpan(hyperLink), hyperLink.start, hyperLink.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            } else {
            spannableString.setSpan(new InternalURLSpan(hyperLink), hyperLink.start, hyperLink.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            }
        }
        setLinksClickable(true);
        super.setText(spannableString, TextView.BufferType.SPANNABLE);
    }

    private void getHyperlinks(HyperLinkType type) {
        Matcher matcher = getPattern(type).matcher(spannableText);
        if (isShortenUrlEnabled && type == HyperLinkType.URL) {
            String completeText = "", leftText = spannableText;
            List<HyperLink> urlHyperLinks = new ArrayList<>();
            boolean result = matcher.find();
            while (result) {
                String url = matcher.group(0);
                String shortUrl = shortenUrl(url);
                leftText = replace(leftText, url, shortUrl);
                completeText += leftText.substring(0, leftText.indexOf(shortUrl) + shortUrl.length());
                leftText = leftText.substring(leftText.indexOf(shortUrl) + shortUrl.length());
                urlHyperLinks.add(new HyperLink(url, HyperLinkType.URL, 0, 0));
                result = matcher.find();
            }
            if (leftText.length() > 0) {
                completeText += leftText;
            }
            spannableText = completeText;

            int i = 0;
            matcher = getPattern(type).matcher(spannableText);
            result = matcher.find();
            while (result) {
                HyperLink hyperLink = urlHyperLinks.get(i);
                hyperLink.start = matcher.start();
                hyperLink.end = matcher.end();
                hyperLinks.add(hyperLink);
                i++;
                result = matcher.find();
            }
        } else {
            boolean result = matcher.find();
            while (result) {
                hyperLinks.add(new HyperLink(matcher.group(0), type, matcher.start(), matcher.end()));
                result = matcher.find();
            }
        }
    }

    private Pattern getPattern(HyperLinkType type) {
        switch (type) {
            case URL:
                return patternUrl;
            case EMAIL:
                return patternEmail;
            case PHONE:
                return patternPhone;
            case HASH_TAG:
                return patternHashTag;
            case TAG:
                return patternTag;
            default:
                return patternUrl;
        }
    }

    public String replace(String text, String regex, String replaceText) {
        String finalText = text.replaceFirst(regex, replaceText);
        if (finalText.equals(text)) {
            finalText = text.replace(regex, replaceText);
        }
        return finalText;
    }

    public String shortenUrl(String longUrl) {
        try {
            URL url = new URL(longUrl);
            return url.getProtocol() + "://" + url.getHost();
        } catch (MalformedURLException e) {
            Log.e("Error", e.getMessage());
        }
        return longUrl;
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

    public class HyperLink {
        public String span;
        public HyperLinkType type;
        public int start;
        public int end;
        public boolean isClicked;

        public HyperLink(String span, HyperLinkType type, int start, int end) {
            this.span = span;
            this.type = type;
            this.start = start;
            this.end = end;
        }
    }

    public enum HyperLinkType {
        URL, EMAIL, PHONE, HASH_TAG, TAG
    }

    public class RoundedBackgroundSpan extends ReplacementSpan {

        private final int mPadding = 10;
        private HyperLink hyperLink;

        public RoundedBackgroundSpan(HyperLink hyperLink) {
            this.hyperLink = hyperLink;
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            return (int) (mPadding + paint.measureText(text.subSequence(start, end).toString()) + mPadding);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            float width = paint.measureText(text.subSequence(start, end).toString());
            RectF rect = new RectF(x - mPadding, top, x + width + mPadding, bottom);
            paint.setColor(Color.parseColor("#DDDDDD"));
            canvas.drawRoundRect(rect, 20, 20, paint);
            paint.setColor(Color.parseColor("#8A0099"));
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
            canvas.drawText(text, start, end, x, y, paint);
        }
    }

    public class InternalURLSpan extends ClickableSpan {

        private HyperLink hyperLink;
        private boolean mIsPressed;

        public InternalURLSpan(HyperLink hyperLink) {
            this.hyperLink = hyperLink;
        }

        public void setPressed(boolean isSelected) {
            mIsPressed = isSelected;
        }

        @Override
        public void onClick(View textView) {
            if (onTextClickListener != null) {
                onTextClickListener.onClick(hyperLink);
            }
            switch (hyperLink.type) {
                case URL:
                    String url = hyperLink.span;
                    if (!url.startsWith("http")) {
                        url = "http://" + url;
                    }
                    getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    break;
                case EMAIL:
                    getContext().startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + hyperLink.span)));
                    break;
                case PHONE:
                    getContext().startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + hyperLink.span)));
                    break;
            }
            hyperLink.isClicked = true;
            if (clickedHyperLinks != null && !clickedHyperLinks.contains(hyperLink.span)) {
                clickedHyperLinks.add(hyperLink.span);
                setClickedHyperLinks();
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            switch (hyperLink.type) {
                case URL:
                case EMAIL:
                case PHONE:
                    if (hyperLink.isClicked) {
                        ds.setColor(ContextCompat.getColor(getContext(), mIsPressed ? R.color.linkVisitedPressed : R.color.linkVisited));
                    } else {
                        ds.setColor(ContextCompat.getColor(getContext(), mIsPressed ? R.color.linkPressed : R.color.link));
                    }
                    ds.setUnderlineText(false);
                    break;
                case HASH_TAG:
                    ds.setColor(ContextCompat.getColor(getContext(), mIsPressed ? R.color.hashTagPressed : R.color.hashTag));
                    ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
                    ds.setUnderlineText(false);
                    break;
                case TAG:
                    ds.setColor(ContextCompat.getColor(getContext(), mIsPressed ? R.color.tagPressed : R.color.tag));
                    ds.setUnderlineText(false);
                    break;
            }
        }
    }

    private class LinkTouchMovementMethod extends LinkMovementMethod {
        private InternalURLSpan mPressedSpan;

        @Override
        public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mPressedSpan = getPressedSpan(textView, spannable, event);
                if (mPressedSpan != null) {
                    mPressedSpan.setPressed(true);
                    Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                            spannable.getSpanEnd(mPressedSpan));
                }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                InternalURLSpan touchedSpan = getPressedSpan(textView, spannable, event);
                if (mPressedSpan != null && touchedSpan != mPressedSpan) {
                    mPressedSpan.setPressed(false);
                    mPressedSpan = null;
                    Selection.removeSelection(spannable);
                }
            } else {
                if (mPressedSpan != null) {
                    mPressedSpan.setPressed(false);
                    super.onTouchEvent(textView, spannable, event);
                }
                mPressedSpan = null;
                Selection.removeSelection(spannable);
            }
            return true;
        }

        private InternalURLSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= textView.getTotalPaddingLeft();
            y -= textView.getTotalPaddingTop();

            x += textView.getScrollX();
            y += textView.getScrollY();

            Layout layout = textView.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            InternalURLSpan[] link = spannable.getSpans(off, off, InternalURLSpan.class);
            InternalURLSpan touchedSpan = null;
            if (link.length > 0) {
                touchedSpan = link[0];
            }
            return touchedSpan;
        }
    }

    public void setOnTextClickListener(OnTextClickListener onTextClickListener) {
        this.onTextClickListener = onTextClickListener;
    }

    public interface OnTextClickListener {
        void onClick(HyperLink hyperLink);
    }
}
