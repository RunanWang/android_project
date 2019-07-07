package com.buaa.simplemov;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.buaa.simplemov.bean.Feed;
import com.buaa.simplemov.bean.FeedResponse;
import com.buaa.simplemov.newtork.IMiniDouyinService;
import com.buaa.simplemov.utils.MyRecycleViewAdapter;
import com.buaa.simplemov.utils.WaitForWhile;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Main2Activity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private RecyclerView mRv;
    private LottieAnimationView animation;
    private List<Feed> mFeeds = new ArrayList<>();
    private MyRecycleViewAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private static final String TAG = "debugR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar2 = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar2);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        ActionBar actionBar = getSupportActionBar();
        FloatingActionButton uploadButton = findViewById(R.id.fab_update);
        animation = findViewById(R.id.animation_view2);
        animation.setVisibility(View.GONE);
        mRv = findViewById(R.id.recycle_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {

                }
                return true;
            }
        });
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Main2Activity.this, UploadActivity.class);
                startActivity(intent);
            }
        });
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchFeed();
            }
        });
        fetchFeed();
        initRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    public void fetchFeed() {
        //refreshButton.setText("requesting...");
        //refreshButton.setEnabled(false);
        mRv.setVisibility(View.GONE);
        animation.setVisibility(View.VISIBLE);
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
                mFeeds = response.body().getFeeds();
                mRv.getAdapter().notifyDataSetChanged();
//                resetRefreshBtn();
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        Log.d(TAG, "get mFeeds =[" +mFeeds + "]");
                        mRv.setVisibility(View.VISIBLE);
                        animation.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                    }
                };
                WaitForWhile waitForWhile = new WaitForWhile(handler);
                waitForWhile.start();
            }

            @Override
            public void onFailure(Call<FeedResponse> arg0, Throwable arg1) {
                //Log.d(TAG, "onFailure() called with: arg0 = [" + arg0 + "], arg1 = [" + arg1 + "]");
                //resetRefreshBtn();
            }
        });
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        TextView user;
        TextView time;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            imageView = itemView.findViewById(R.id.feed_img);
            user = itemView.findViewById(R.id.feed_user);
            time = itemView.findViewById(R.id.feed_time);
        }
    }

    private void initRecyclerView(){
        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecycleViewAdapter(mFeeds);
        mRv.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feed_item, viewGroup, false);
                final MyViewHolder holder = new MyViewHolder(view);
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        Feed feed = mFeeds.get(position);
                        Intent intent = new Intent(viewGroup.getContext(),VideoPlayActivity.class);
                        intent.putExtra("videoUrl",feed.getVideo_url());
                        intent.putExtra("user",feed.getUser_name());
                        startActivity(intent);
                    }
                });
                return holder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                Feed feed = mFeeds.get(i);
                MyViewHolder myViewHolder = ((MyViewHolder)viewHolder);
                myViewHolder.user.setText(feed.getUser_name());
                String url = feed.getImage_url();
                Glide.with(myViewHolder.cardView.getContext()).load(url).into(myViewHolder.imageView);
                myViewHolder.time.setText(feed.getCreatedAt());
            }

            @Override
            public int getItemCount() {
                return mFeeds.size();
            }
        });
    }
}
