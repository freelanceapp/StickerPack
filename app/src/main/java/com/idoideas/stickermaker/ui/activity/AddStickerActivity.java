package com.idoideas.stickermaker.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.idoideas.stickermaker.ImageManipulation;
import com.idoideas.stickermaker.R;
import com.idoideas.stickermaker.WhatsAppBasedCode.StickerPackListActivity;
import com.idoideas.stickermaker.constant.Constant;
import com.idoideas.stickermaker.utils_editor.BrushImageView;
import com.idoideas.stickermaker.utils_editor.TouchImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

public class AddStickerActivity extends AppCompatActivity {

    private int initialDrawingCountLimit = 20;
    private int offset = 250;
    private int undoLimit = 10;
    private float brushSize = 70.0f;

    private static final int CAMERA_REQUEST = 52;
    private static final int PICK_REQUEST = 53;

    private boolean isMultipleTouchErasing;
    private boolean isTouchOnBitmap;
    private int initialDrawingCount;
    private int updatedBrushSize;
    private int imageViewWidth;

    private int imageViewHeight;
    private float currentx;
    private float currenty;

    private Bitmap bitmapMaster;
    private Bitmap lastEditedBitmap;
    private Bitmap originalBitmap;
    private Bitmap resizedBitmap;
    private Bitmap highResolutionOutput;

    private Canvas canvasMaster;
    private Point mainViewSize;
    private Path drawingPath;

    private Vector<Integer> brushSizes;
    private Vector<Integer> redoBrushSizes;

    private ArrayList<Path> paths;
    private ArrayList<Path> redoPaths;

    private RelativeLayout rlImageViewContainer;
    private LinearLayout llTopBar;
    private ImageView ivRedo, ivUndo, ivDone, imgCamera, imgGallery;
    private SeekBar sbOffset;
    private SeekBar sbWidth;
    private TouchImageView touchImageView;
    private BrushImageView brushImageView;
    static final Integer WRITE_EXST = 0x1;
    private boolean isImageResized;
    private MediaScannerConnection msConn;
    private int MODE;

    public AddStickerActivity() {
        paths = new ArrayList();
        redoPaths = new ArrayList();
        brushSizes = new Vector();
        redoBrushSizes = new Vector();
        MODE = 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sticker);

        init();
    }

    private void init() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        drawingPath = new Path();
        Display display = getWindowManager().getDefaultDisplay();
        mainViewSize = new Point();
        display.getSize(mainViewSize);
        initViews();

        findViewById(R.id.imgBack).setOnClickListener(v -> finish());

        ((ImageView) findViewById(R.id.imgToSticker)).setOnClickListener(v -> {
            File newFolder = null;
            String fileName = "StickerList-" + System.currentTimeMillis() + ".webp";

            if (!(Build.VERSION.SDK_INT >= 19)) {
                newFolder = new File(Environment.getExternalStorageDirectory(), "/ImageEraser/CustomSticker/");
            } else if (Environment.getExternalStorageState().equals("mounted")) {
                newFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "/ImageEraser/CustomSticker/");
            }
            if (!newFolder.exists()) {
                newFolder.mkdirs();
            }
            File fileNamePath = new File(newFolder, fileName);
            String strPath = fileNamePath.toString();

            try {
                URL url = new URL(Constant.DEFAULT_IMAGE);
                ImageManipulation.imageUrlToWebP();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });
    }

    private class MyAsyncTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                return null;
            }
        }

        protected void onPostExecute(Bitmap result) {
            //do what you want with your bitmap result on the UI thread
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    public void initViews() {
        touchImageView = findViewById(R.id.drawingImageView);
        brushImageView = findViewById(R.id.brushContainingView);
        llTopBar = findViewById(R.id.ll_top_bar);
        rlImageViewContainer = findViewById(R.id.rl_image_view_container);
        ivUndo = findViewById(R.id.iv_undo);
        ivRedo = findViewById(R.id.iv_redo);
        ivDone = findViewById(R.id.iv_done);
        imgCamera = findViewById(R.id.imgCamera);
        imgGallery = findViewById(R.id.imgGallery);
        sbOffset = findViewById(R.id.sb_offset);
        sbWidth = findViewById(R.id.sb_width);

        imgCamera.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        });

        imgGallery.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_REQUEST);
        });

        rlImageViewContainer.getLayoutParams().height = mainViewSize.y
                - (llTopBar.getLayoutParams().height);
        imageViewWidth = mainViewSize.x;
        imageViewHeight = rlImageViewContainer.getLayoutParams().height;

        ivUndo.setOnClickListener(v -> undo());
        ivRedo.setOnClickListener(v -> redo());
        ivDone.setOnClickListener(v -> saveImage());

        touchImageView.setOnTouchListener(new OnTouchListner());
        sbWidth.setMax(150);
        sbWidth.setProgress((int) (brushSize - 20.0f));
        sbWidth.setOnSeekBarChangeListener(new OnWidthSeekbarChangeListner());
        sbOffset.setMax(350);
        sbOffset.setProgress(offset);
        sbOffset.setOnSeekBarChangeListener(new OnOffsetSeekbarChangeListner());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST:
                    originalBitmap = (Bitmap) data.getExtras().get("data");
                    //touchImageView.setImageBitmap(originalBitmap);
                    //touchImageView.setPan(false);
                    setBitMap();
                    updateBrush((float) (mainViewSize.x / 2), (float) (mainViewSize.y / 2));
                    break;
                case PICK_REQUEST:
                    try {
                        Uri uri = data.getData();
                        originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        //touchImageView.setImageBitmap(originalBitmap);
                        setBitMap();
                        updateBrush((float) (mainViewSize.x / 2), (float) (mainViewSize.y / 2));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public void resetPathArrays() {
        ivUndo.setEnabled(false);
        ivRedo.setEnabled(false);
        paths.clear();
        brushSizes.clear();
        redoPaths.clear();
        redoBrushSizes.clear();
    }

    public void resetRedoPathArrays() {
        ivRedo.setEnabled(false);
        redoPaths.clear();
        redoBrushSizes.clear();
    }

    public void undo() {
        int size = this.paths.size();
        if (size != 0) {
            if (size == 1) {
                this.ivUndo.setEnabled(false);
            }
            size--;
            redoPaths.add(paths.remove(size));
            redoBrushSizes.add(brushSizes.remove(size));
            if (!ivRedo.isEnabled()) {
                ivRedo.setEnabled(true);
            }
            UpdateCanvas();
        }
    }

    public void redo() {
        int size = redoPaths.size();
        if (size != 0) {
            if (size == 1) {
                ivRedo.setEnabled(false);
            }
            size--;
            paths.add(redoPaths.remove(size));
            brushSizes.add(redoBrushSizes.remove(size));
            if (!ivUndo.isEnabled()) {
                ivUndo.setEnabled(true);
            }
            UpdateCanvas();
        }
    }

    public void setBitMap() {
        this.isImageResized = false;
        if (resizedBitmap != null) {
            resizedBitmap.recycle();
            resizedBitmap = null;
        }
        if (bitmapMaster != null) {
            bitmapMaster.recycle();
            bitmapMaster = null;
        }
        canvasMaster = null;
        resizedBitmap = resizeBitmapByCanvas();

        lastEditedBitmap = resizedBitmap.copy(Config.ARGB_8888, true);
        bitmapMaster = Bitmap.createBitmap(lastEditedBitmap.getWidth(), lastEditedBitmap.getHeight(), Config.ARGB_8888);
        canvasMaster = new Canvas(bitmapMaster);
        canvasMaster.drawBitmap(lastEditedBitmap, 0.0f, 0.0f, null);
        touchImageView.setImageBitmap(bitmapMaster);
        resetPathArrays();
        touchImageView.setPan(false);
        brushImageView.invalidate();
    }

    public Bitmap resizeBitmapByCanvas() {
        float width;
        float heigth;
        float orginalWidth = (float) originalBitmap.getWidth();
        float orginalHeight = (float) originalBitmap.getHeight();
        if (orginalWidth > orginalHeight) {
            width = (float) imageViewWidth;
            heigth = (((float) imageViewWidth) * orginalHeight) / orginalWidth;
        } else {
            heigth = (float) imageViewHeight;
            width = (((float) imageViewHeight) * orginalWidth) / orginalHeight;
        }
        if (width > orginalWidth || heigth > orginalHeight) {
            return originalBitmap;
        }
        Bitmap background = Bitmap.createBitmap((int) width, (int) heigth, Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        float scale = width / orginalWidth;
        float yTranslation = (heigth - (orginalHeight * scale)) / 2.0f;
        Matrix transformation = new Matrix();
        transformation.postTranslate(0.0f, yTranslation);
        transformation.preScale(scale, scale);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(originalBitmap, transformation, paint);
        //this.isImageResized = true;
        return background;
    }

    private void moveTopoint(float startx, float starty) {
        float zoomScale = getImageViewZoom();
        starty -= (float) offset;
        if (redoPaths.size() > 0) {
            resetRedoPathArrays();
        }
        PointF transLation = getImageViewTranslation();
        int projectedX = (int) ((float) (((double) (startx - transLation.x)) / ((double) zoomScale)));
        int projectedY = (int) ((float) (((double) (starty - transLation.y)) / ((double) zoomScale)));
        drawingPath.moveTo((float) projectedX, (float) projectedY);

        updatedBrushSize = (int) (brushSize / zoomScale);
    }

    private void lineTopoint(Bitmap bm, float startx, float starty) {
        if (initialDrawingCount < initialDrawingCountLimit) {
            initialDrawingCount += 1;
            if (initialDrawingCount == initialDrawingCountLimit) {
                isMultipleTouchErasing = true;
            }
        }
        float zoomScale = getImageViewZoom();
        starty -= (float) offset;
        PointF transLation = getImageViewTranslation();
        int projectedX = (int) ((float) (((double) (startx - transLation.x)) / ((double) zoomScale)));
        int projectedY = (int) ((float) (((double) (starty - transLation.y)) / ((double) zoomScale)));
        if (!isTouchOnBitmap && projectedX > 0 && projectedX < bm.getWidth() && projectedY > 0 && projectedY < bm.getHeight()) {
            isTouchOnBitmap = true;
        }
        drawingPath.lineTo((float) projectedX, (float) projectedY);
    }

    private void addDrawingPathToArrayList() {
        if (paths.size() >= undoLimit) {
            UpdateLastEiditedBitmapForUndoLimit();
            paths.remove(0);
            brushSizes.remove(0);
        }
        if (paths.size() == 0) {
            ivUndo.setEnabled(true);
            ivRedo.setEnabled(false);
        }
        brushSizes.add(updatedBrushSize);
        paths.add(drawingPath);
        drawingPath = new Path();
    }

    private void drawOnTouchMove() {
        Paint paint = new Paint();
        paint.setStrokeWidth((float) updatedBrushSize);
        paint.setColor(0);
        paint.setStyle(Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Join.ROUND);
        paint.setStrokeCap(Cap.ROUND);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
        canvasMaster.drawPath(drawingPath, paint);
        touchImageView.invalidate();
    }

    public void UpdateLastEiditedBitmapForUndoLimit() {
        Canvas canvas = new Canvas(lastEditedBitmap);
        for (int i = 0; i < 1; i += 1) {
            int brushSize = brushSizes.get(i);
            Paint paint = new Paint();
            paint.setColor(0);
            paint.setStyle(Style.STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeJoin(Join.ROUND);
            paint.setStrokeCap(Cap.ROUND);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
            paint.setStrokeWidth((float) brushSize);
            canvas.drawPath(paths.get(i), paint);
        }
    }

    public void UpdateCanvas() {
        canvasMaster.drawColor(0, Mode.CLEAR);
        canvasMaster.drawBitmap(lastEditedBitmap, 0.0f, 0.0f, null);
        int i = 0;
        while (true) {
            if (i >= paths.size()) {
                break;
            }
            int brushSize = brushSizes.get(i);
            Paint paint = new Paint();
            paint.setColor(0);
            paint.setStyle(Style.STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeJoin(Join.ROUND);
            paint.setStrokeCap(Cap.ROUND);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
            paint.setStrokeWidth((float) brushSize);
            canvasMaster.drawPath(paths.get(i), paint);
            i += 1;
        }
        touchImageView.invalidate();
    }

    public void updateBrushWidth() {
        brushImageView.width = brushSize / 2.0f;
        brushImageView.invalidate();
    }

    public void updateBrushOffset() {
        float doffest = ((float) offset) - brushImageView.offset;
        BrushImageView brushImageViewView = brushImageView;
        brushImageViewView.centery += doffest;
        brushImageView.offset = (float) offset;
        brushImageView.invalidate();
    }

    public void updateBrush(float x, float y) {
        brushImageView.offset = (float) offset;
        brushImageView.centerx = x;
        brushImageView.centery = y;
        brushImageView.width = brushSize / 2.0f;
        brushImageView.invalidate();
    }

    public float getImageViewZoom() {
        return touchImageView.getCurrentZoom();
    }

    public PointF getImageViewTranslation() {
        return touchImageView.getTransForm();
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void onDestroy() {
        super.onDestroy();
        //UpdateCanvas();
        if (lastEditedBitmap != null) {
            lastEditedBitmap.recycle();
            lastEditedBitmap = null;
        }
        if (originalBitmap != null) {
            originalBitmap.recycle();
            originalBitmap = null;
        }
        if (resizedBitmap != null) {
            resizedBitmap.recycle();
            resizedBitmap = null;
        }
        if (bitmapMaster != null) {
            bitmapMaster.recycle();
            bitmapMaster = null;
        }
        if (this.highResolutionOutput != null) {
            this.highResolutionOutput.recycle();
            this.highResolutionOutput = null;
        }
    }

    private class OnTouchListner implements OnTouchListener {
        OnTouchListner() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if (!(event.getPointerCount() == 1 || isMultipleTouchErasing)) {
                if (initialDrawingCount > 0) {
                    UpdateCanvas();
                    drawingPath.reset();
                    initialDrawingCount = 0;
                }
                touchImageView.onTouchEvent(event);
                MODE = 2;
            } else if (action == MotionEvent.ACTION_DOWN) {
                isTouchOnBitmap = false;
                touchImageView.onTouchEvent(event);
                MODE = 1;
                initialDrawingCount = 0;
                isMultipleTouchErasing = false;
                moveTopoint(event.getX(), event.getY());

                updateBrush(event.getX(), event.getY());
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (MODE == 1) {
                    currentx = event.getX();
                    currenty = event.getY();

                    updateBrush(currentx, currenty);
                    lineTopoint(bitmapMaster, currentx, currenty);

                    drawOnTouchMove();
                }
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                if (MODE == 1) {
                    if (isTouchOnBitmap) {
                        addDrawingPathToArrayList();
                    }
                }
                isMultipleTouchErasing = false;
                initialDrawingCount = 0;
                MODE = 0;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                MODE = 0;
            }
            return true;
        }
    }

    private class OnWidthSeekbarChangeListner implements OnSeekBarChangeListener {
        OnWidthSeekbarChangeListner() {
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            brushSize = ((float) progress) + 20.0f;
            updateBrushWidth();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    private class OnOffsetSeekbarChangeListner implements OnSeekBarChangeListener {
        OnOffsetSeekbarChangeListner() {
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            offset = progress;
            updateBrushOffset();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    private void saveImage() {
        makeHighResolutionOutput();
        new imageSaveByAsync().execute();
    }

    private void makeHighResolutionOutput() {
        if (this.isImageResized) {
            Bitmap solidColor = Bitmap.createBitmap(this.originalBitmap.getWidth(), this.originalBitmap.getHeight(), this.originalBitmap.getConfig());
            Canvas canvas = new Canvas(solidColor);
            Paint paint = new Paint();
            paint.setColor(Color.argb(255, 255, 255, 255));
            Rect src = new Rect(0, 0, this.bitmapMaster.getWidth(), this.bitmapMaster.getHeight());
            Rect dest = new Rect(0, 0, this.originalBitmap.getWidth(), this.originalBitmap.getHeight());
            canvas.drawRect(dest, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
            canvas.drawBitmap(this.bitmapMaster, src, dest, paint);
            this.highResolutionOutput = null;
            this.highResolutionOutput = Bitmap.createBitmap(this.originalBitmap.getWidth(), this.originalBitmap.getHeight(), this.originalBitmap.getConfig());
            Canvas canvas1 = new Canvas(this.highResolutionOutput);
            canvas1.drawBitmap(this.originalBitmap, 0.0f, 0.0f, null);
            Paint paint1 = new Paint();
            paint1.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
            canvas1.drawBitmap(solidColor, 0.0f, 0.0f, paint1);
            if (solidColor != null && !solidColor.isRecycled()) {
                solidColor.recycle();
                solidColor = null;
            }
            return;
        }
        this.highResolutionOutput = null;
        //this.highResolutionOutput = this.bitmapMaster.copy(this.bitmapMaster.getConfig(), true);
        int intHeight = bitmapMaster.getHeight();
        int intWidth = bitmapMaster.getWidth();
        this.highResolutionOutput = getResizedBitmap(bitmapMaster, intHeight, intWidth);
    }

    public static Bitmap getResizedBitmap(Bitmap image, int newHeight, int newWidth) {
        int width = image.getWidth();
        int height = image.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    private class imageSaveByAsync extends AsyncTask<String, Void, Boolean> {
        private imageSaveByAsync() {
        }

        protected void onPreExecute() {
            getWindow().setFlags(16, 16);
        }

        protected Boolean doInBackground(String... args) {
            try {
                askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXST);
                return Boolean.valueOf(true);
            } catch (Exception e) {
                return Boolean.valueOf(false);
            }
        }

        protected void onPostExecute(Boolean success) {
            Toast toast = Toast.makeText(getBaseContext(), "Image Saved", Toast.LENGTH_LONG);
            toast.setGravity(17, 0, 0);
            toast.show();
            getWindow().clearFlags(16);
            startActivity(new Intent(getApplicationContext(), StickerPackListActivity.class));
            finish();
        }
    }

    public void savePhoto(Bitmap bmp) {

        File newFolder = null;
        String fileName = "Photo-" + System.currentTimeMillis() + ".png";

        if (!(Build.VERSION.SDK_INT >= 19)) {
            newFolder = new File(Environment.getExternalStorageDirectory(), "/ImageEraser/");
        } else if (Environment.getExternalStorageState().equals("mounted")) {
            newFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/ImageEraser/");
        }
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }

        //////////////////////////////////////////////////////////
        File imageFileName = null;
        FileOutputStream out;

        Calendar c = Calendar.getInstance();
        FileOutputStream out2;

        try {
            imageFileName = File.createTempFile(fileName, ".png", newFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out2 = new FileOutputStream(imageFileName);
            bmp.compress(Bitmap.CompressFormat.PNG, 50, out2);
            out = out2;
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
            bmp = null;
        }
        scanPhoto(imageFileName.toString());
    }

    public void scanPhoto(String imageFileName) {
        this.msConn = new MediaScannerConnection(this, new ScanPhotoConnection(imageFileName));
        this.msConn.connect();
    }

    class ScanPhotoConnection implements MediaScannerConnection.MediaScannerConnectionClient {
        final String val$imageFileName;

        ScanPhotoConnection(String str) {
            this.val$imageFileName = str;
        }

        public void onMediaScannerConnected() {
            msConn.scanFile(this.val$imageFileName, null);
        }

        public void onScanCompleted(String path, Uri uri) {
            msConn.disconnect();
        }
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddStickerActivity.this, permission)) {
                ActivityCompat.requestPermissions(AddStickerActivity.this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(AddStickerActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Bitmap bitmap = createTrimmedBitmap(highResolutionOutput);
            savePhoto(bitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(AddStickerActivity.this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case 1:
                    Bitmap bitmap = createTrimmedBitmap(highResolutionOutput);
                    savePhoto(bitmap);
                    break;
            }
        }
    }

    static Bitmap trim(Bitmap source) {
        int firstX = 0, firstY = 0;
        int lastX = source.getWidth();
        int lastY = source.getHeight();
        int[] pixels = new int[source.getWidth() * source.getHeight()];
        source.getPixels(pixels, 0, source.getWidth(), 0, 0, source.getWidth(), source.getHeight());
        loop:
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    firstX = x;
                    break loop;
                }
            }
        }
        loop:
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = firstX; x < source.getWidth(); x++) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    firstY = y;
                    break loop;
                }
            }
        }
        loop:
        for (int x = source.getWidth() - 1; x >= firstX; x--) {
            for (int y = source.getHeight() - 1; y >= firstY; y--) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    lastX = x;
                    break loop;
                }
            }
        }
        loop:
        for (int y = source.getHeight() - 1; y >= firstY; y--) {
            for (int x = source.getWidth() - 1; x >= firstX; x--) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    lastY = y;
                    break loop;
                }
            }
        }
        return Bitmap.createBitmap(source, firstX, firstY, lastX - firstX, lastY - firstY);
    }

    static Bitmap createTrimmedBitmap(Bitmap bmp) {
        int imgHeight = bmp.getHeight();
        int imgWidth = bmp.getWidth();
        int smallX = 0, largeX = imgWidth, smallY = 0, largeY = imgHeight;
        int left = imgWidth, right = imgWidth, top = imgHeight, bottom = imgHeight;
        for (int i = 0; i < imgWidth; i++) {
            for (int j = 0; j < imgHeight; j++) {
                if (bmp.getPixel(i, j) != Color.TRANSPARENT) {
                    if ((i - smallX) < left) {
                        left = (i - smallX);
                    }
                    if ((largeX - i) < right) {
                        right = (largeX - i);
                    }
                    if ((j - smallY) < top) {
                        top = (j - smallY);
                    }
                    if ((largeY - j) < bottom) {
                        bottom = (largeY - j);
                    }
                }
            }
        }
        bmp = Bitmap.createBitmap(bmp, left, top, imgWidth - left - right, imgHeight - top - bottom);
        return bmp;
    }


}
