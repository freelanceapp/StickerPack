package com.ideas.stickermaker.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ideas.stickermaker.R;
import com.ideas.stickermaker.modals.category.StickerList;
import com.ideas.stickermaker.modals.category.StickerSubcategory;
import com.ideas.stickermaker.utils.AppPreference;

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
        List<StickerList> stickerLists = new ArrayList<>();
        stickerLists.addAll(cartList.get(position).getSticker());

        String strTotalLike = item.getLikes();
        String strDownload = item.getDownloads();
        if (strTotalLike == null || strTotalLike.isEmpty()) {
            strTotalLike = "0";
        }
        if (strDownload == null || strDownload.isEmpty()) {
            strDownload = "0";
        }
        holder.tvDownloadCount.setText(strDownload + " " + "downloads");
        holder.tvLikesCount.setText(strTotalLike + " " + "Likes");

        String strDownloadPackId = item.getId() + item.getSubcatName();
        if (AppPreference.getStringPreference(context, strDownloadPackId).equalsIgnoreCase(strDownloadPackId)) {
            holder.imgAdd.setImageResource(R.drawable.sticker_3rdparty_added);
        } else {
            holder.imgAdd.setImageResource(android.R.drawable.stat_sys_download);
            holder.imgAdd.setTag(position);
            holder.imgAdd.setOnClickListener(onClickListener);
        }
        holder.linearPack.setTag(position);
        holder.linearPack.setOnClickListener(onClickListener);

        stickerListAdapter = new StickerListAdapter(context, stickerLists, null, "horizontal");
        holder.recyclerviewHorizontalSticker.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerviewHorizontalSticker.setAdapter(stickerListAdapter);
        stickerListAdapter.notifyDataSetChanged();
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
