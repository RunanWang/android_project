package com.buaa.simplemov.newtork;


import com.buaa.simplemov.bean.Cat;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * @author Xavier.S
 * @date 2019.01.15 16:42
 */
public interface ICatService {
    //  Implement your Cat Request here, url: https://api.thecatapi.com/v1/images/search?limit=5
    @GET("/v1/images/search?limit=5") Call<List<Cat>> randomFiveCats();
}
