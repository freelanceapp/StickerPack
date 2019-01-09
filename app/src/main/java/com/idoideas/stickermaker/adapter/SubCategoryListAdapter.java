package com.idoideas.stickermaker.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.idoideas.stickermaker.R;
import com.idoideas.stickermaker.constant.Constant;
import com.idoideas.stickermaker.modals.category.StickerDatum;
import com.idoideas.stickermaker.modals.category.StickerList;
import com.idoideas.stickermaker.modals.category.StickerSubcategory;

import java.util.ArrayList;
import java.util.List;

public class SubCategoryListAdapter extends RecyclerView.Adapter<SubCategoryListAdapter.MyViewHolder> {

    private Context context;
    private List<StickerSubcategory> cartList;
    private View.OnClickListener onClickListener;
    private StickerListAdapter stickerListAdapter;

    public SubCategoryListAdapter(Context context, List<StickerSubcategory> cartList, View.OnClickListener onClickListener) {
        this.context = context;
        this.cartList = cartList;
        this.onClickListener = onClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sub_category, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final StickerSubcategory item = cartList.get(position);
        holder.tvTitle.setText(item.getSubcatName());
        /*holder.tvDownloadCount.setText(item.getSubcatName());
        holder.tvLikesCount.setText(item.getSubcatName());*/
        List<StickerList> stickerLists = new ArrayList<>();
        stickerLists.addAll(cartList.get(position).getSticker());

        holder.linearPack.setTag(position);
        holder.linearPack.setOnClickListener(onClickListener);
        holder.imgAdd.setTag(position);
        holder.imgAdd.setOnClickListener(onClickListener);

        stickerListAdapter = new StickerListAdapter(context, stickerLists, null, "horizontal");
        holder.recyclerviewHorizontalSticker.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerviewHorizontalSticker.setAdapter(stickerListAdapter);
        stickerListAdapter.notifyDataSetChanged();

       /* Glide.with(context)
                .load(Constant.IMAGE_URL + item.getCategoryImage())
                .into(holder.imgCategory);*/
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private RecyclerView recyclerviewHorizontalSticker;
        private LinearLayout linearPack;
        public TextView tvTitle, tvDownloadCount, tvLikesCount;
        public ImageView imgAdd;

        public MyViewHolder(View view) {
            super(view);
            recyclerviewHorizontalSticker = view.findViewById(R.id.recyclerviewHorizontalSticker);
            linearPack = view.findViewById(R.id.linearPack);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvDownloadCount = view.findViewById(R.id.tvDownloadCount);
            tvLikesCount = view.findViewById(R.id.tvLikesCount);
            imgAdd = view.findViewById(R.id.imgAdd);
        }
    }

}
