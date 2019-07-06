package com.buaa.simplemov;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.buaa.simplemov.bean.Feed;
import com.buaa.simplemov.bean.FeedResponse;
import com.buaa.simplemov.bean.PostVideoResponse;
import com.buaa.simplemov.newtork.IMiniDouyinService;
import com.buaa.simplemov.utils.ResourceUtils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final String TAG = "DebugR";
    private RecyclerView mRv;
    private List<Feed> mFeeds = new ArrayList<>();
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    public Button mBtn;
    private Button mBtnRefresh;

    private String[] mPermissionsArrays = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};

    private final static int REQUEST_PERMISSION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRecyclerView();
        initBtns();
        if (!checkPermissionAllGranted(mPermissionsArrays)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(mPermissionsArrays, REQUEST_PERMISSION);
            }
        } else {
            Toast.makeText(MainActivity.this, "已经获取所有所需权限", Toast.LENGTH_SHORT).show();
        }
    }

    private void initBtns() {
        mBtn = findViewById(R.id.btn);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = mBtn.getText().toString();
                if (getString(R.string.select_an_image).equals(s)) {
                    chooseImage();
                } else if (getString(R.string.select_a_video).equals(s)) {
                    chooseVideo();
                } else if (getString(R.string.post_it).equals(s)) {
                    if (mSelectedVideo != null && mSelectedImage != null) {
                        postVideo();
                    } else {
                        throw new IllegalArgumentException("error data uri, mSelectedVideo = " + mSelectedVideo + ", mSelectedImage = " + mSelectedImage);
                    }
                } else if ((getString(R.string.success_try_refresh).equals(s))) {
                    mBtn.setText(R.string.select_an_image);
                }
            }
        });

        mBtnRefresh = findViewById(R.id.btn_refresh);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private void initRecyclerView() {
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                ImageView imageView = new ImageView(viewGroup.getContext());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setAdjustViewBounds(true);
                return new MyViewHolder(imageView);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                ImageView iv = (ImageView) viewHolder.itemView;

                // TODO-C2 (10) Uncomment these 2 lines, assign image url of Feed to this url variable
                String url = mFeeds.get(i).getImage_url();
                //Log.d(TAG, "onBindViewHolder() called with: viewHolder = [" + viewHolder + "], i = [" + i + "]");
                Glide.with(iv.getContext()).load(url).into(iv);
            }

            @Override
            public int getItemCount() {
                return mFeeds.size();
            }
        });
    }

    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE);
        mSelectedImage = intent.getData();
    }


    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"),
                PICK_VIDEO);
        mSelectedVideo = intent.getData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        if (resultCode == RESULT_OK && null != data) {

            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d(TAG, "selectedImage = " + mSelectedImage);

                mBtn.setText(R.string.select_a_video);
            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);

                mBtn.setText(R.string.post_it);
            }
        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(MainActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    private void postVideo() {
        mBtn.setText("POSTING...");
        mBtn.setEnabled(false);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        MultipartBody.Part coverImage = getMultipartFromUri("cover_image", mSelectedImage);
        MultipartBody.Part video = getMultipartFromUri("video", mSelectedVideo);
        // if success, make a text Toast and show
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://test.androidcamp.bytedance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        Call<PostVideoResponse> call = retrofit.create(IMiniDouyinService.class).createVideo(
                "16161616", "Mouse",
                coverImage,
                video
        );

        call.enqueue(new Callback<PostVideoResponse>() {

            @Override
            public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                ;
                //Log.d(TAG, "getCat() get cat = [" + call + "]");
                //ArrayList<Cat> cats = new ArrayList<>();
                //cats.add(catTransfer);
                //Log.d(TAG, "onResponse() called with: call = [" + call + "], response = [" + response + "]");
                PostVideoResponse response1 = response.body();
                if (response1.isSuccess()) {
                    Log.d(TAG, "post is OK ");
                }
                Log.d(TAG, "response is" + response1);
                Toast.makeText(MainActivity.this,"post success!",Toast.LENGTH_SHORT).show();
                mBtn.setText(R.string.select_an_image);
                mBtn.setEnabled(true);
            }

            @Override
            public void onFailure(Call<PostVideoResponse> arg0, Throwable arg1) {
                Log.d(TAG, "onFailure() called with: arg0 = [" + arg0 + "], arg1 = [" + arg1 + "]");
                mBtn.setText(R.string.select_an_image);
                mBtn.setEnabled(true);
            }
        });
    }

    public void fetchFeed(View view) {
        mBtnRefresh.setText("requesting...");
        mBtnRefresh.setEnabled(false);

        // if success, assign data to mFeeds and call mRv.getAdapter().notifyDataSetChanged()
        // don't forget to call resetRefreshBtn() after response received
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://test.androidcamp.bytedance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Call<FeedResponse> call = retrofit.create(IMiniDouyinService.class).getFeedResponse();
        call.enqueue(new Callback<FeedResponse>() {

            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                ;
                //Log.d(TAG, "getCat() get cat = [" + call + "]");
                //ArrayList<Cat> cats = new ArrayList<>();
                //cats.add(catTransfer);
                //Log.d(TAG, "onResponse() called with: call = [" + call + "], response = [" + response + "]");
                mFeeds = response.body().getFeeds();
                Log.d(TAG, "get feed is OK and feed is " + mFeeds);
                mRv.getAdapter().notifyDataSetChanged();
                resetRefreshBtn();
            }

            @Override
            public void onFailure(Call<FeedResponse> arg0, Throwable arg1) {
                Log.d(TAG, "onFailure() called with: arg0 = [" + arg0 + "], arg1 = [" + arg1 + "]");
                resetRefreshBtn();
            }
        });
    }

    private void resetRefreshBtn() {
        mBtnRefresh.setText(R.string.refresh_feed);
        mBtnRefresh.setEnabled(true);
    }

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
}
