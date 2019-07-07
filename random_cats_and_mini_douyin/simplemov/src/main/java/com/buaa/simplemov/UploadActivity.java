package com.buaa.simplemov;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.buaa.simplemov.bean.PostVideoResponse;
import com.buaa.simplemov.newtork.IMiniDouyinService;
import com.buaa.simplemov.utils.ResourceUtils;
import com.hb.dialog.dialog.LoadingDialog;
import com.hb.dialog.myDialog.ActionSheetDialog;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
            case INIT_POST:{
                loadingDialog = new LoadingDialog(this);
                loadingDialog.setMessage("loading");
                loadingDialog.show();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        ImageView selectImage = findViewById(R.id.cover_image);
        ImageView selectVideo = findViewById(R.id.video);
        ImageView post = findViewById(R.id.post);
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
                uploadVideo();

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
        Toast.makeText(this, "in take photo", Toast.LENGTH_SHORT).show();

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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && null !=data){
            if(requestCode == PICK_IMAGE){
                imageUri = data.getData();
                ImageView pic = findViewById(R.id.preview);
                pic.setImageURI(imageUri);
            }else if(requestCode == PICK_VIDEO){
                videoUri = data.getData();
            }
        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        try {

            File f = new File(ResourceUtils.getRealPath(UploadActivity.this, uri));
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
            return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    void uploadVideo(){
        // TODO-C2 (6) Send Request to post a video with its cover image
        // if success, make a text Toast and show
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://test.androidcamp.bytedance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        MultipartBody.Part cover_Image = getMultipartFromUri("cover_image", imageUri);
        MultipartBody.Part video = getMultipartFromUri("video", videoUri);
        retrofit.create(IMiniDouyinService.class).createVideo("16231187", "chenjinyu",
                cover_Image,video).
                enqueue(new Callback<PostVideoResponse>() {
                    @Override
                    public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                        PostVideoResponse result = response.body();
                        if(result != null && result.isSuccess()) {
                            Toast.makeText(getApplicationContext(),
                                    "post success", Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }else{
                            Toast.makeText(getApplicationContext(),
                                    "post failed", Toast.LENGTH_SHORT).show();
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
