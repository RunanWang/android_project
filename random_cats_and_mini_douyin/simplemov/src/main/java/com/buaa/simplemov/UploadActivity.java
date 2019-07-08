package com.buaa.simplemov;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
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

import static com.buaa.simplemov.utils.Utils.MEDIA_TYPE_VIDEO;
import static com.buaa.simplemov.utils.Utils.getOutputMediaFile;

public class UploadActivity extends AppCompatActivity {
    private ActionSheetDialog imageDialog;
    private ActionSheetDialog videoDialog;
    private LoadingDialog loadingDialog;
    private ImageView done;
    private MediaRecorder mMediaRecorder;
    private final int INIT_IMAGE = 1;
    private final int INIT_VIDEO = 2;
    private final int INIT_POST = 3;
    private final int PICK_IMAGE = 1;
    private final int PICK_VIDEO = 2;
    private final int TAKE_PHOTO = 3;
    private final int TAKE_VIDEO = 4;
    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;
    private Uri imageUri;
    private Uri videoUri;
    private File imageFile;
    private File videoFile;
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private int rotationDegree = 0;
    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        initCamera();
        final ImageView record = findViewById(R.id.record);
        record.setOnClickListener(new View.OnClickListener() {
            boolean recording = false;
            @Override
            public void onClick(View v) {
                if(!recording){
                   if(!prepareVideoRecorder()){
                       Toast.makeText(UploadActivity.this,
                               "error to start record",Toast.LENGTH_SHORT).show();
                   }
                   recording  = true;
                   done = findViewById(R.id.video);
                   done.setImageResource(R.drawable.ic_add_to_queue_black_24dp);
                }else{
                    releaseMediaRecorder();
                    recording = false;
                    done = findViewById(R.id.video);
                    done.setImageResource(R.drawable.ic_check_black_24dp);
                }
            }
        });
    }
    void initCamera(){
        mSurfaceView = findViewById(R.id.img);
        //todo 给SurfaceHolder添加Callback
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    mCamera = getCamera(CAMERA_TYPE);
                    mCamera.setPreviewDisplay(surfaceHolder);
                    mCamera.startPreview();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                releaseCameraAndPreview();
            }
        });
    }
    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);
        //todo 摄像头添加属性，例是否自动对焦，设置旋转方向等
        int degree =  getCameraDisplayOrientation(position);
        rotationDegree = degree;
        cam.setDisplayOrientation(rotationDegree);
        return cam;
    }
    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }
    private void releaseCameraAndPreview() {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    private boolean prepareVideoRecorder() {
        //todo 准备MediaRecorder
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        videoFile =  getOutputMediaFile(MEDIA_TYPE_VIDEO);
        mMediaRecorder.setOutputFile(videoFile.toString());
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(rotationDegree);
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            return true;

        }catch (Exception e){
            releaseMediaRecorder();
            return false;
        }
    }
    private void releaseMediaRecorder() {
        //todo 释放MediaRecorder
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        mCamera.lock();
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
        imageFile = getOutputMediaFile(Utils.MEDIA_TYPE_IMAGE);
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
                done = findViewById(R.id.cover_image);
                done.setImageResource(R.drawable.ic_check_black_24dp);
            } else if (requestCode == PICK_VIDEO) {
                videoUri = data.getData();
                videoFile = new File(ResourceUtils.getRealPath(UploadActivity.this, videoUri));
                done = findViewById(R.id.video);
                done.setImageResource(R.drawable.ic_check_black_24dp);
            } else if (requestCode == TAKE_PHOTO) {
                setPic();
                done = findViewById(R.id.cover_image);
                done.setImageResource(R.drawable.ic_check_black_24dp);
            }else if(requestCode == TAKE_VIDEO){
                videoUri = data.getData();
                videoFile = new File(ResourceUtils.getRealPath(UploadActivity.this,videoUri));
                done = findViewById(R.id.video);
                done.setImageResource(R.drawable.ic_check_black_24dp);
            }
        }
        done = findViewById(R.id.post);
        done.setImageResource(R.drawable.ic_cloud_upload_black_24dp);
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
                            done = findViewById(R.id.post);
                            done.setImageResource(R.drawable.ic_check_black_24dp);
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
