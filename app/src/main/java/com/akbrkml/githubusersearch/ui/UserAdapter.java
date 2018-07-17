package com.akbrkml.githubusersearch.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.akbrkml.githubusersearch.R;
import com.akbrkml.githubusersearch.model.UsersItem;
import com.akbrkml.githubusersearch.util.Constant;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<UsersItem> mUsersItems;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public UserAdapter(Context mContext, RecyclerView view) {
        this.mContext = mContext;
        this.mUsersItems = new ArrayList<>();
        lastItemViewDetector(view);
    }

    public void resetListData() {
        this.mUsersItems = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.mUsersItems.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.item_user_list, parent, false);
            vh = new UserHolder(v);
        } else {
            View v = LayoutInflater.from(mContext).inflate(R.layout.item_loading, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserHolder) {
            final UsersItem item = mUsersItems.get(position);
            UserHolder vItem = (UserHolder) holder;
            vItem.bind(item);
        } else {
            ((ProgressViewHolder) holder).mProgressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return mUsersItems != null ? mUsersItems.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return this.mUsersItems.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    public void insertData(List<UsersItem> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.mUsersItems.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (mUsersItems.get(i) == null) {
                mUsersItems.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    class UserHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.user_image)
        ImageView mUserImage;
        @BindView(R.id.tv_user_name)
        TextView mUserName;

        UserHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(UsersItem item) {
            mUserName.setText(item.getLogin());
            Picasso.get()
                    .load(item.getAvatarUrl())
                    .into(mUserImage);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progress_loading)
        ProgressBar mProgressBar;

        ProgressViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = layoutManager.findLastVisibleItemPosition();
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        if (onLoadMoreListener != null) {
                            int current_page = getItemCount() / Constant.USER_PER_REQUEST;
                            onLoadMoreListener.onLoadMore(current_page);
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }
}
