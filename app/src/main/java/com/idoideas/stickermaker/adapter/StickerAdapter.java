package com.idoideas.stickermaker.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.idoideas.stickermaker.R;
import com.idoideas.stickermaker.constant.Constant;
import com.idoideas.stickermaker.ui.activity.MainActivity;
import com.idoideas.stickermaker.ui.activity.StickerDetailsActivity;
import com.idoideas.stickermaker.ui.fragment.Sticker;
import com.idoideas.stickermaker.ui.fragment.StickerPack;
import com.idoideas.stickermaker.utils.AppPreference;

import java.io.File;
import java.util.List;

import static com.idoideas.stickermaker.ui.activity.MainActivity.EXTRA_STICKERPACK;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.ViewHolder> {

    Context context;
    List<StickerPack> stickerPack;

    public StickerAdapter(Context context, List<StickerPack> stickerPack) {
        this.context = context;
        this.stickerPack = stickerPack;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_sticker, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        final List<Sticker> models = stickerPack.get(i).getStickers();
        viewHolder.name.setText(stickerPack.get(i).name);

        if (models.size() > 0) {

            for (int j = 0; j < models.size(); j++) {
                if (j == 0) {
                    Glide.with(context)
                            .load(Constant.IMAGE_URL + models.get(j).imageFileName.replace(".webp", ".png"))
                            .into(viewHolder.imone);
                }
                if (j == 1) {
                    Glide.with(context)
                            .load(Constant.IMAGE_URL + models.get(j).imageFileName.replace(".webp", ".png"))
                            .into(viewHolder.imtwo);
                }
                if (j == 2) {
                    Glide.with(context)
                            .load(Constant.IMAGE_URL + models.get(j).imageFileName.replace(".webp", ".png"))
                            .into(viewHolder.imthree);
                }
                if (j == 3) {
                    Glide.with(context)
                            .load(Constant.IMAGE_URL + models.get(j).imageFileName.replace(".webp", ".png"))
                            .into(viewHolder.imfour);
                }
            }

            File file = new File(MainActivity.path + "/" + stickerPack.get(i).identifier + "/" + models.get(0).imageFileName);
            if (!file.exists()) {
                viewHolder.rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppPreference.setStringPreference(context, Constant.DownloadPack, "download");
                        ((Activity) context).runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        viewHolder.download.setVisibility(View.INVISIBLE);
                                        viewHolder.bar.setVisibility(View.VISIBLE);
                                        for (final Sticker s : stickerPack.get(viewHolder.getAdapterPosition()).getStickers()) {
                                            Log.d("adapter", "onClick: " + s.imageFileName);
                                            String strName = s.imageFileName.replace(".webp", ".png");
                                            Glide.with(context)
                                                    .asBitmap()
                                                    .apply(new RequestOptions().override(512, 512))
                                                    .load(Constant.IMAGE_URL + strName)
                                                    .addListener(new RequestListener<Bitmap>() {
                                                        @Override
                                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                                            return false;
                                                        }

                                                        @Override
                                                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                                            Bitmap bitmap1 = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
                                                            Matrix matrix = new Matrix();
                                                            Canvas canvas = new Canvas(bitmap1);
                                                            canvas.drawColor(Color.TRANSPARENT);
                                                            matrix.postTranslate(
                                                                    canvas.getWidth() / 2 - resource.getWidth() / 2,
                                                                    canvas.getHeight() / 2 - resource.getHeight() / 2
                                                            );
                                                            canvas.drawBitmap(resource, matrix, null);
                                                            MainActivity.SaveImage(bitmap1, s.imageFileName, stickerPack.get(viewHolder.getAdapterPosition()).identifier);
                                                            return true;
                                                        }
                                                    }).submit();
                                        }
                                        viewHolder.download.setVisibility(View.INVISIBLE);
                                        viewHolder.bar.setVisibility(View.INVISIBLE);
                                    }
                                }
                        );

                    }
                });
            } else {
                viewHolder.rl.setVisibility(View.INVISIBLE);
            }
        }

        Glide.with(context)
                .asBitmap()
                .load(Constant.IMAGE_URL + stickerPack.get(i).trayImageFile)
                .addListener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target,
                                                boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target,
                                                   DataSource dataSource, boolean isFirstResource) {
                        Bitmap bitmap1 = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
                        Matrix matrix = new Matrix();
                        Canvas canvas = new Canvas(bitmap1);
                        canvas.drawColor(Color.TRANSPARENT);
                        matrix.postTranslate(
                                canvas.getWidth() / 2 - resource.getWidth() / 2,
                                canvas.getHeight() / 2 - resource.getHeight() / 2
                        );
                        canvas.drawBitmap(resource, matrix, null);
                        MainActivity.SaveTryImage(bitmap1, stickerPack.get(i).trayImageFile, stickerPack.get(i).identifier);
                        return false;
                    }
                })
                .submit();

        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StickerDetailsActivity.class);
                intent.putExtra(EXTRA_STICKERPACK, stickerPack.get(viewHolder.getAdapterPosition()));
                context.startActivity(new Intent(context, StickerDetailsActivity.class)
                                .putExtra(EXTRA_STICKERPACK, stickerPack.get(viewHolder.getAdapterPosition())),
                        ActivityOptionsCompat.makeScaleUpAnimation(v, (int) v.getX(), (int) v.getY(), v.getWidth(),
                                v.getHeight()).toBundle());
            }
        });

    }

    @Override
    public int getItemCount() {
        return stickerPack.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView imone, imtwo, imthree, imfour, download;
        CardView cardView;
        ProgressBar bar;
        RelativeLayout rl;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.rv_sticker_name);
            imone = itemView.findViewById(R.id.sticker_one);
            imtwo = itemView.findViewById(R.id.sticker_two);
            imthree = itemView.findViewById(R.id.sticker_three);
            imfour = itemView.findViewById(R.id.sticker_four);
            download = itemView.findViewById(R.id.download);
            cardView = itemView.findViewById(R.id.card_view);
            bar = itemView.findViewById(R.id.progressBar);
            rl = itemView.findViewById(R.id.download_layout);
        }
    }
}
