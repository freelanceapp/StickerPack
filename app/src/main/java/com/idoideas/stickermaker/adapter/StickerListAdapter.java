package com.idoideas.stickermaker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.idoideas.stickermaker.R;
import com.idoideas.stickermaker.constant.Constant;
import com.idoideas.stickermaker.modals.category.StickerDatum;
import com.idoideas.stickermaker.modals.category.StickerList;

import java.util.List;

public class StickerListAdapter extends RecyclerView.Adapter<StickerListAdapter.MyViewHolder> {

    private Context context;
    private List<StickerList> cartList;
    private View.OnClickListener onClickListener;
    private String strType = "";

    public StickerListAdapter(Context context, List<StickerList> cartList, View.OnClickListener onClickListener,
                              String strType) {
        this.context = context;
        this.cartList = cartList;
        this.onClickListener = onClickListener;
        this.strType = strType;
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
        if (strType.equalsIgnoreCase("horizontal")) {
            if (cartList.size() > 5) {
                return 5;
            } else {
                return cartList.size();
            }
        } else if (strType.equalsIgnoreCase("vertical")) {
            return cartList.size();
        }
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgSticker;

        public MyViewHolder(View view) {
            super(view);
            imgSticker = view.findViewById(R.id.imgSticker);
        }
    }

}
