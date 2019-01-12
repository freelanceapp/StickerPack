package com.ideas.stickermaker.adapter.view_holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ideas.stickermaker.R;

public class DownloadStickerPackListItemViewHolder extends RecyclerView.ViewHolder {

    public View container;
    public TextView titleView;
    public TextView publisherView;
    //TextView filesizeView;
    public ImageView addButton;
    public ImageView shareButton;
    public LinearLayout imageRowView;

    public DownloadStickerPackListItemViewHolder(final View itemView) {
        super(itemView);
        container = itemView;
        titleView = itemView.findViewById(R.id.sticker_pack_title);
        publisherView = itemView.findViewById(R.id.sticker_pack_publisher);
        //filesizeView = itemView.findViewById(R.id.sticker_pack_filesize);
        addButton = itemView.findViewById(R.id.add_button_on_list);
        shareButton = itemView.findViewById(R.id.export_button_on_list);
        imageRowView = itemView.findViewById(R.id.sticker_packs_list_item_image_list);
    }
}