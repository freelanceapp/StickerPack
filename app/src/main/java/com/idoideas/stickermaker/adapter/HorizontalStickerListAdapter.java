package com.idoideas.stickermaker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.idoideas.stickermaker.R;
import com.idoideas.stickermaker.constant.Constant;
import com.idoideas.stickermaker.modals.category.StickerList;

import java.util.List;

public class HorizontalStickerListAdapter extends RecyclerView.Adapter<HorizontalStickerListAdapter.MyViewHolder> {

    private Context context;
    private List<StickerList> cartList;
    private View.OnClickListener onClickListener;

    public HorizontalStickerListAdapter(Context context, List<StickerList> cartList, View.OnClickListener onClickListener) {
        this.context = context;
        this.cartList = cartList;
        this.onClickListener = onClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sticker, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final StickerList item = cartList.get(position);

        Glide.with(context)
                .load(Constant.IMAGE_URL + item.getStickers())
                .into(holder.imgSticker);
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgSticker;

        public MyViewHolder(View view) {
            super(view);
            imgSticker = view.findViewById(R.id.imgSticker);
        }
    }

}
