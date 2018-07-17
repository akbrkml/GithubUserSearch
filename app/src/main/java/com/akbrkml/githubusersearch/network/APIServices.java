package com.akbrkml.githubusersearch.network;


import retrofit2.Callback;

/**
 * Created by akbar on 14/12/17.
 */

public class APIServices {
    private APIInterfaces apiInterface;

    public APIServices() {
        apiInterface = APIClient.builder()
                .create(APIInterfaces.class);
    }

    public void search(String query, int page, Callback callback) {
        apiInterface.searchUser(query, page).enqueue(callback);
    }

}
