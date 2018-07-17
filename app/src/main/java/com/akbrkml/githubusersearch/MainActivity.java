package com.akbrkml.githubusersearch;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.akbrkml.githubusersearch.model.ResponseAPI;
import com.akbrkml.githubusersearch.model.UsersItem;
import com.akbrkml.githubusersearch.network.APIServices;
import com.akbrkml.githubusersearch.ui.UserAdapter;
import com.akbrkml.githubusersearch.util.NetworkCheck;
import com.akbrkml.githubusersearch.util.TimeUtils;

import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)Toolbar mToolbar;
    @BindView(R.id.et_search)EditText mSearchEditText;
    @BindView(android.R.id.content)View mParentView;
    @BindView(R.id.swipe_refresh_layout)SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recyclerView)RecyclerView mRecyclerView;
    @BindView(R.id.lyt_failed)View mLayoutFailedView;
    @BindView(R.id.lyt_no_item)View mLayoutNoItemView;

    private int failed_page = 0;
    private int post_total = 0;
    private String query = "";
    private UserAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initComponents();
        initToolbar();
    }

    private void initComponents(){
        mSearchEditText.addTextChangedListener(textWatcher);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new UserAdapter(this, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        // detect when scroll reach bottom
        mAdapter.setOnLoadMoreListener(new UserAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (post_total > mAdapter.getItemCount() && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                } else {
                    mAdapter.setLoaded();
                }
            }
        });

        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    searchAction();
                    return true;
                }
                return false;
            }
        });

        // on swipe list
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.resetListData();
                requestAction(1);
            }
        });

        showNoItemView(true);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() > 0) {
                searchAction();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void searchData(final int page_no){
        APIServices apiServices = new APIServices();
        apiServices.search(query, page_no, new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                ResponseAPI responseAPI = (ResponseAPI) response.body();
                if (responseAPI != null && response.code() == 200){
                    post_total = responseAPI.getTotalCount();
                    displayApiResult(responseAPI.getItems());
                } else if(response.code() == 403){
                    long dateTime = TimeUtils
                            .convertFullDateToMillis(response.headers().get("Date"));
                    long rateLimitResetTime = Long.parseLong(response.headers().get("X-RateLimit-Reset")) * 1000;

                    new CountDownTimer(rateLimitResetTime - dateTime, 1000){
                        @Override
                        public void onTick(long millisUntilFinished) {
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.layout),
                                    "Can't Make Request, Please wait in " +
                                            TimeUtils.convertMillisToTimeFormat(millisUntilFinished),
                                    Snackbar.LENGTH_SHORT);
                            snackbar.show();
                            mSearchEditText.setEnabled(false);
                        }

                        @Override
                        public void onFinish() {
                            mSearchEditText.setEnabled(true);

                        }
                    }.start();
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }
        });
    }

    private void showFailedView(boolean show, String message) {
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            mRecyclerView.setVisibility(View.GONE);
            mLayoutFailedView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mLayoutFailedView.setVisibility(View.GONE);
        }
        (findViewById(R.id.failed_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                query = mSearchEditText.getText().toString();
                requestAction(failed_page);
            }
        });
    }

    private void displayApiResult(final List<UsersItem> items) {
        mAdapter.insertData(items);
        swipeProgress(false);
        if (items.size() == 0) showNoItemView(true);
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        swipeProgress(false);
        if (NetworkCheck.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.no_internet_text));
        }
    }

    private void showNoItemView(boolean show) {
        if (show) {
            mRecyclerView.setVisibility(View.GONE);
            mLayoutNoItemView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mLayoutNoItemView.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            mSwipeRefreshLayout.setRefreshing(show);
            return;
        }
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(show);
            }
        });
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            mAdapter.setLoading();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                searchData(page_no);
            }
        }, 1000);
    }

    private void searchAction() {
        query = mSearchEditText.getText().toString();
        if (!query.equals("")) {
            mAdapter.resetListData();
            // request action will be here
            requestAction(1);
        } else {
            Toast.makeText(this, R.string.please_fill, Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
