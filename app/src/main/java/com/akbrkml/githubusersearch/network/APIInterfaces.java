package com.akbrkml.githubusersearch.network;

import com.akbrkml.githubusersearch.model.ResponseAPI;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by akbar on 19/10/17.
 */

public interface APIInterfaces {

    @GET("search/users")
    Call<ResponseAPI> searchUser(
            @Query("q") String query,
            @Query("page") int page
    );

}