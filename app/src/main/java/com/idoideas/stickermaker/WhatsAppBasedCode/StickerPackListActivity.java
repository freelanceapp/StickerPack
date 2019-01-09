package com.idoideas.stickermaker.WhatsAppBasedCode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.soloader.SoLoader;
import com.idoideas.stickermaker.BuildConfig;
import com.idoideas.stickermaker.DataArchiver;
import com.idoideas.stickermaker.R;
import com.idoideas.stickermaker.StickerBook;
import com.idoideas.stickermaker.ui.activity.AddStickerActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.idoideas.stickermaker.NewUserIntroActivity.verifyStoragePermissions;

public class StickerPackListActivity extends BaseActivity {

    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;
    private static final String TAG = "StickerPackList";
    private LinearLayoutManager packLayoutManager;
    private static RecyclerView packRecyclerView;
    private static StickerPackListAdapter allStickerPacksListAdapter;
    WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    ArrayList<StickerPackModal> stickerPackList;
    public static Context context;
    public static String newName, newCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_pack_list);

        init();
    }

    private void init() {
        ((LinearLayout) findViewById(R.id.llAddSticker)).setOnClickListener(v ->
                startActivity(new Intent(StickerPackListActivity.this, AddStickerActivity.class)));

        ((LinearLayout) findViewById(R.id.llStickerPack)).setOnClickListener(v ->
                addNewStickerPackInInterface());

        findViewById(R.id.imgBack).setOnClickListener(v -> finish());

        StickerBook.init(this);
        Fresco.initialize(this);
        context = getApplicationContext();
        SoLoader.init(this, false);

        packRecyclerView = findViewById(R.id.sticker_pack_list);
        stickerPackList = StickerBook.getAllStickerPacks();
        showStickerPackList(stickerPackList);

        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            Bundle extras = getIntent().getExtras();
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                if (uri != null) {
                    DataArchiver.importZipFileToStickerPack(uri, StickerPackListActivity.this);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String action = getIntent().getAction();
        if (action == null) {
            Log.v("Example", "Force restart");
            Intent intent = new Intent(this, StickerPackListActivity.class);
            intent.setAction("Already created");
            startActivity(intent);
            finish();
        }
        whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
        whiteListCheckAsyncTask.execute(stickerPackList);

    }

    @Override
    protected void onPause() {
        super.onPause();
        DataArchiver.writeStickerBookJSON(StickerBook.getAllStickerPacks(), this);
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
    }

    @Override
    protected void onDestroy() {
        DataArchiver.writeStickerBookJSON(StickerBook.getAllStickerPacks(), this);
        super.onDestroy();
    }


    public void showStickerPackList(List<StickerPackModal> stickerPackList) {
        allStickerPacksListAdapter = new StickerPackListAdapter(context,stickerPackList, onAddButtonClickedListener);
        packRecyclerView.setAdapter(allStickerPacksListAdapter);
        packLayoutManager = new LinearLayoutManager(this);
        packLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(packRecyclerView.getContext(),
                packLayoutManager.getOrientation()
        );
        packRecyclerView.addItemDecoration(dividerItemDecoration);
        packRecyclerView.setLayoutManager(packLayoutManager);
        packRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this::recalculateColumnCount);
    }

    private StickerPackListAdapter.OnAddButtonClickedListener onAddButtonClickedListener = new StickerPackListAdapter.OnAddButtonClickedListener() {
        @Override
        public void onAddButtonClicked(StickerPackModal pack) {
            if (pack.getStickers().size() >= 3) {
                Intent intent = new Intent();
                intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
                intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_ID, pack.identifier);
                intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY);
                intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_NAME, pack.name);
                try {
                    StickerPackListActivity.this.startActivityForResult(intent, StickerPackDetailsActivity.ADD_PACK);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(StickerPackListActivity.this, R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show();
                }
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(StickerPackListActivity.this)
                        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create();
                alertDialog.setTitle("Invalid Action");
                alertDialog.setMessage("In order to be applied to WhatsApp, the sticker pack must have at least 3 stickers. Please add more stickers first.");
                alertDialog.show();
            }
        }
    };

    private void recalculateColumnCount() {
        final int previewSize = getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size);
        int firstVisibleItemPosition = packLayoutManager.findFirstVisibleItemPosition();
        StickerPackListItemViewHolder viewHolder = (StickerPackListItemViewHolder)
                packRecyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition);
        if (viewHolder != null) {
            final int max = Math.max(viewHolder.imageRowView.getMeasuredWidth() / previewSize, 1);
            int numColumns = Math.min(STICKER_PREVIEW_DISPLAY_LIMIT, max);
            allStickerPacksListAdapter.setMaxNumberOfStickersInARow(numColumns);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == StickerPackDetailsActivity.ADD_PACK) {
            if (resultCode == Activity.RESULT_CANCELED && data != null) {
                final String validationError = data.getStringExtra("validation_error");
                if (validationError != null) {
                    if (BuildConfig.DEBUG) {
                        MessageDialogFragment.newInstance(R.string.title_validation_error, validationError).show(getSupportFragmentManager(), "validation error");
                    }
                }
            }
        } else if (data != null && requestCode == 2319) {
            Uri uri = data.getData();
            createNewStickerPackAndOpenIt(newName, uri);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {

            return true;
        } else if (item.getItemId() == R.id.action_info) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.about_layout, null);

            dialogView.findViewById(R.id.redditlogo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.reddit.com/u/idoideas"));
                    startActivity(browserIntent);
                }
            });

            dialogView.findViewById(R.id.twitterlogo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.twitter.com/idoideas"));
                    startActivity(browserIntent);
                }
            });

            dialogView.findViewById(R.id.githublogo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.github.com/idoideas"));
                    startActivity(browserIntent);
                }
            });

            dialogBuilder.setView(dialogView);
            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();
        } else if (item.getItemId() == R.id.action_donate) {
        }
        return super.onOptionsItemSelected(item);
    }

    static class WhiteListCheckAsyncTask extends AsyncTask<List<StickerPackModal>, Void, List<StickerPackModal>> {
        private final WeakReference<StickerPackListActivity> stickerPackListActivityWeakReference;

        WhiteListCheckAsyncTask(StickerPackListActivity stickerPackListActivity) {
            this.stickerPackListActivityWeakReference = new WeakReference<>(stickerPackListActivity);
        }

        @SafeVarargs
        @Override
        protected final List<StickerPackModal> doInBackground(List<StickerPackModal>... lists) {
            List<StickerPackModal> stickerPackList = lists[0];
            final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity == null) {
                return stickerPackList;
            }
            for (StickerPackModal stickerPack : stickerPackList) {
                stickerPack.setIsWhitelisted(WhitelistCheck.isWhitelisted(stickerPackListActivity, stickerPack.identifier));
            }
            return stickerPackList;
        }

        @Override
        protected void onPostExecute(List<StickerPackModal> stickerPackList) {
            final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity != null) {
                stickerPackListActivity.allStickerPacksListAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setPositiveButton("Let's Go", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    verifyStoragePermissions(StickerPackListActivity.this);
                                }
                            })
                            .create();
                    alertDialog.setTitle("Notice!");
                    alertDialog.setMessage("We've recognized you denied the storage access permission for this app."
                            + "\n\nIn order for this app to work, storage access is required.");
                    alertDialog.show();
                }
                break;
        }
    }

    private void addNewStickerPackInInterface() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Create New StickerList Pack");
        dialog.setMessage("Please specify title and creator for the pack.");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameBox = new EditText(this);
        nameBox.setLines(1);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayoutParams.setMargins(50, 0, 50, 10);
        nameBox.setLayoutParams(buttonLayoutParams);
        nameBox.setHint("Pack Name");
        nameBox.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        layout.addView(nameBox);

        final EditText creatorBox = new EditText(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            creatorBox.setAutofillHints("name");
        }
        creatorBox.setLines(1);
        creatorBox.setLayoutParams(buttonLayoutParams);
        creatorBox.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        creatorBox.setHint("Creator");
        layout.addView(creatorBox);

        dialog.setView(layout);

        dialog.setPositiveButton("OK", null);

        dialog.setNegativeButton("Cancel", (dialog1, id) -> dialog1.cancel());

        final AlertDialog ad = dialog.create();

        ad.show();

        Button b = ad.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(view -> {
            if (TextUtils.isEmpty(nameBox.getText())) {
                nameBox.setError("Package name is required!");
            }

            if (TextUtils.isEmpty(creatorBox.getText())) {
                creatorBox.setError("Creator is required!");
            }

            if (!TextUtils.isEmpty(nameBox.getText()) && !TextUtils.isEmpty(creatorBox.getText())) {
                ad.dismiss();
                createDialogForPickingIconImage(nameBox, creatorBox);
            }
        });

        creatorBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    b.performClick();
                }
                return false;
            }
        });
    }

    private void createDialogForPickingIconImage(EditText nameBox, EditText creatorBox) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick your pack's icon image");
        builder.setMessage("Now you will pick the new sticker pack's icon image.")
                .setCancelable(false)
                .setPositiveButton("Let's go", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        openFileTray(nameBox.getText().toString(), creatorBox.getText().toString());
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createNewStickerPackAndOpenIt(String name, Uri trayImage) {
        String newId = UUID.randomUUID().toString();
        StickerPackModal sp = new StickerPackModal(newId, name, getApplicationContext().getString(R.string.app_name),
                trayImage, "", "", "", "", this);
        StickerBook.addStickerPackExisting(sp);

        Intent intent = new Intent(this, StickerPackDetailsActivity.class);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, true);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, newId);
        intent.putExtra("isNewlyCreated", true);
        this.startActivity(intent);
    }

    private void openFileTray(String name, String creator) {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        newName = name;
        newCreator = creator;
        startActivityForResult(i, 2319);
    }

  /*  private void makeIntroNotRunAgain() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean("isAlreadyShown", false);
        if (!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("isAlreadyShown", false);
            edit.commit();
        }
    }

    private boolean toShowIntro() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return prefs.getBoolean("isAlreadyShown", true);
    }*/
}
