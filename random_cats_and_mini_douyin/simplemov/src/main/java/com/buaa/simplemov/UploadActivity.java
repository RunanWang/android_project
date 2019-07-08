package com.buaa.simplemov;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.buaa.simplemov.bean.PostVideoResponse;
import com.buaa.simplemov.newtork.IMiniDouyinService;
import com.buaa.simplemov.utils.ResourceUtils;
import com.buaa.simplemov.utils.Utils;
import com.hb.dialog.dialog.LoadingDialog;
import com.hb.dialog.myDialog.ActionSheetDialog;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Utf8;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UploadActivity extends AppCompatActivity {
    private ActionSheetDialog imageDialog;
    private ActionSheetDialog videoDialog;
    private LoadingDialog loadingDialog;
    private final int INIT_IMAGE = 1;
    private final int INIT_VIDEO = 2;
    private final int INIT_POST = 3;
    private final int PICK_IMAGE = 1;
    private final int PICK_VIDEO = 2;
    private final int TAKE_PHOTO = 3;
    private final int TAKE_VIDEO = 4;
    private Uri imageUri;
    private Uri videoUri;
    private File imageFile;
    private File videoFile;
    private String[] mPermissionsArrays = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};
    private final static int REQUEST_PERMISSION = 123;
    private boolean checkPermissionAllGranted(String[] permissions) {
        // 6.0以下不需要
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    void initDialog(int initCase) {
        //init three different Dialogs
        switch (initCase) {
            case INIT_IMAGE: {
                imageDialog = new ActionSheetDialog(this).builder().setTitle("请选择封面来源")
                        .addSheetItem("相册", null, new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                findImageInAlbum();
                            }
                        }).addSheetItem("拍照", null, new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                takePhoto();
                            }
                        });
                break;
            }
            case INIT_VIDEO: {
                videoDialog = new ActionSheetDialog(this).builder().setTitle("请选择视频内容")
                        .addSheetItem("相册", null, new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                findVideoInAlbum();
                            }
                        }).addSheetItem("拍摄", null, new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                takeVideo();
                            }
                        });
            }
            break;
            case INIT_POST: {
                loadingDialog = new LoadingDialog(this);
                if (imageUri != null || videoUri != null) {
                    loadingDialog.setMessage("uploading");
                    loadingDialog.show();
                    uploadVideo();
                } else {
                    Toast.makeText(this, "封面和视频内容的选择没有完成", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        if (!checkPermissionAllGranted(mPermissionsArrays)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(mPermissionsArrays, REQUEST_PERMISSION);
            }
        }
        ImageView selectImage = findViewById(R.id.cover_image);
        ImageView selectVideo = findViewById(R.id.video);
        ImageView post = findViewById(R.id.post);
        View button = findViewById(R.id.buttonPanel);
        button.bringToFront();
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDialog(INIT_IMAGE);
                imageDialog.show();
            }
        });
        selectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDialog(INIT_VIDEO);
                videoDialog.show();
            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDialog(INIT_POST);
            }
        });
        final ImageView preview = findViewById(R.id.preview);
        final VideoView videoView = findViewById(R.id.video_preview);
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoUri!=null){
                    v.setVisibility(View.GONE);
                    videoView.setVisibility(View.VISIBLE);
                    videoView.setVideoURI(videoUri);
                    videoView.start();
                }
            }
        });
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(((VideoView)v).isPlaying()){
                        ((VideoView)v).pause();
                    }else{
                        ((VideoView)v).start();
                    }
                }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                preview.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
            }
        });
    }

    void findImageInAlbum() {
        Toast.makeText(this, "in find photo", Toast.LENGTH_SHORT).show();
        Intent findImage = new Intent();
        findImage.setType("image/*");
        findImage.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(findImage, "Select Picture"),
                PICK_IMAGE);

    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFile = Utils.getOutputMediaFile(Utils.MEDIA_TYPE_IMAGE);
        if (imageFile != null) {
            imageUri = FileProvider.getUriForFile(this, "com.bytedance.simplemov", imageFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, TAKE_PHOTO);
        }
    }

    private void findVideoInAlbum() {
        Toast.makeText(this, "in find video", Toast.LENGTH_SHORT).show();

        Toast.makeText(this, "in find photo", Toast.LENGTH_SHORT).show();
        Intent findVideo = new Intent();
        findVideo.setType("video/*");
        findVideo.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(findVideo, "Select Video"),
                PICK_VIDEO);
    }

    private void takeVideo() {
        Toast.makeText(this, "in take video", Toast.LENGTH_SHORT).show();
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if(takeVideoIntent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takeVideoIntent,TAKE_VIDEO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && null != data) {
                imageUri = data.getData();
                ImageView pic = findViewById(R.id.preview);
                pic.setImageURI(imageUri);
                imageFile = new File(ResourceUtils.getRealPath(UploadActivity.this, imageUri));
            } else if (requestCode == PICK_VIDEO) {
                videoUri = data.getData();
                videoFile = new File(ResourceUtils.getRealPath(UploadActivity.this, videoUri));
            } else if (requestCode == TAKE_PHOTO) {
                setPic();
            }else if(requestCode == TAKE_VIDEO){
                videoUri = data.getData();
                videoFile = new File(ResourceUtils.getRealPath(UploadActivity.this,videoUri));

            }
        }
    }

    private void setPic() {
        ImageView imageView = findViewById(R.id.preview);
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();
        BitmapFactory.Options bmOpt = new BitmapFactory.Options();
        bmOpt.inJustDecodeBounds = true;
        //todo 根据缩放比例读取文件，生成Bitmap
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOpt);
        int photoW = bmOpt.outWidth;
        int photoH = bmOpt.outHeight;
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        //todo 如果存在预览方向改变，进行图片旋转
        bmOpt.inJustDecodeBounds = false;
        bmOpt.inSampleSize = scaleFactor;
        bmOpt.inPurgeable = true;
        //todo 如果存在预览方向改变，进行图片旋转
        Bitmap bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOpt);
        try {
            bmp = rotateImage(bmp, imageFile.getAbsolutePath());
        }catch (Exception e){
         e.printStackTrace();
        }finally {
            imageView.setImageBitmap(bmp);
        }
    }

    private Bitmap rotateImage(Bitmap bitmap, String path) throws Exception {
        ExifInterface srcExif = new ExifInterface(path);
        Matrix matrix = new Matrix();
        int orietation = srcExif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        int angle = 0;
        switch (orietation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                angle = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                angle = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                angle = 270;
                break;
        }
        matrix.postRotate(angle);
        return  Bitmap.createBitmap(bitmap,0,0,
                bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }


    //    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
//        // if NullPointerException thrown, try to allow storage permission in system settings
//        try {
//
//            File f = new File(ResourceUtils.getRealPath(UploadActivity.this, uri));
//            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
//            return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
    private MultipartBody.Part getMultipartFromFile(String name, File f) {
        try {
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
            return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    void uploadVideo() {
        // TODO-C2 (6) Send Request to post a video with its cover image
        // if success, make a text Toast and show
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://test.androidcamp.bytedance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        MultipartBody.Part cover_Image = getMultipartFromFile("cover_image", imageFile);
        MultipartBody.Part video = getMultipartFromFile("video", videoFile);
        retrofit.create(IMiniDouyinService.class).createVideo("16231187", "lalala",
                cover_Image, video).
                enqueue(new Callback<PostVideoResponse>() {
                    @Override
                    public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                        PostVideoResponse result = response.body();
                        if (result != null && result.isSuccess()) {
                            Toast.makeText(getApplicationContext(),
                                    "post success", Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                            imageFile = null;
                            videoFile = null;
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "post failed", Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    }
                    @Override
                    public void onFailure(Call<PostVideoResponse> call, Throwable t) {
                        Toast.makeText(getApplicationContext(),
                                "post failed", Toast.LENGTH_SHORT).show();

                    }
                });
    }
}
