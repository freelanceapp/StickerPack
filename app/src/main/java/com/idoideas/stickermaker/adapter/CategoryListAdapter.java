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

import java.util.List;

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.MyViewHolder> {

    private Context context;
    private List<StickerDatum> cartList;
    private View.OnClickListener onClickListener;

    public CategoryListAdapter(Context context, List<StickerDatum> cartList, View.OnClickListener onClickListener) {
        this.context = context;
        this.cartList = cartList;
        this.onClickListener = onClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_category, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final StickerDatum item = cartList.get(position);
        holder.tvCategory.setText(item.getCategoryName());
        int subCatCount = cartList.get(position).getSubcategory().size();
        holder.tvCount.setText(subCatCount + " " + "Stickers pack");

        holder.relativeItem.setTag(position);
        holder.relativeItem.setOnClickListener(onClickListener);

        Glide.with(context)
                .load(Constant.IMAGE_URL + item.getCategoryImage())
                .into(holder.imgCategory);
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout relativeItem;
        public TextView tvCategory, tvCount;
        public ImageView imgCategory;

        public MyViewHolder(View view) {
            super(view);
            relativeItem = view.findViewById(R.id.relativeItem);
            tvCategory = view.findViewById(R.id.tvCategory);
            tvCount = view.findViewById(R.id.tvCount);
            imgCategory = view.findViewById(R.id.imgCategory);
        }
    }

}
