package com.github.minhnguyen31093.simplechat.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.minhnguyen31093.simplechat.R;
import com.github.minhnguyen31093.simplechat.models.Message;
import com.github.minhnguyen31093.simplechat.utils.ImageUtils;
import com.github.minhnguyen31093.simplechat.utils.TextUtils;
import com.github.minhnguyen31093.simplechat.views.IMEEditText;
import com.github.minhnguyen31093.simplechat.views.LinkClickableTextView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Message> items;
    private OnItemClickListener onItemClickListener;

    public ChatAdapter(List<Message> items, OnItemClickListener onItemClickListener) {
        this.items = items;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message item = items.get(position);

        String text = item.message;
        if (text.startsWith(IMEEditText.TAG)) {
            holder.txtContent.setVisibility(View.GONE);
        } else if (text.contains(IMEEditText.TAG)) {
            text = text.replace(IMEEditText.TAG, "");
            holder.txtContent.setText(text);
            holder.txtContent.setVisibility(View.VISIBLE);
        } else {
            holder.txtContent.setText(text);
            holder.txtContent.setVisibility(View.VISIBLE);
        }
        String image = TextUtils.getImageUrl(item.message);
        if (image != null && !image.isEmpty()) {
            ImageUtils.loadChatContent(holder.sdvContent, image);
            holder.sdvContent.setVisibility(View.VISIBLE);
        } else {
            holder.sdvContent.setVisibility(View.GONE);
        }

        holder.txtContent.setOnTextClickListener(hyperLink -> {
            if (onItemClickListener != null) {
                switch (hyperLink.type) {
                    case HASH_TAG:
                        onItemClickListener.onHashTag(hyperLink.span.replace("#", ""));
                        break;
                    case TAG:
                        onItemClickListener.onTag(hyperLink.span.replace("@", ""));
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void insert(String content) {
        Message message = new Message();
        message.message = content;
        items.add(message);
        notifyItemInserted(items.size() - 1);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LinkClickableTextView txtContent;
        SimpleDraweeView sdvContent;

        ViewHolder(View itemView) {
            super(itemView);
            txtContent = itemView.findViewById(R.id.txtContent);
            sdvContent = itemView.findViewById(R.id.sdvContent);
        }
    }

    public interface OnItemClickListener {
        void onHashTag(String hashTag);
        void onTag(String tag);
    }
}
