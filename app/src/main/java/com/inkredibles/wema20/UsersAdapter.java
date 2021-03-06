package com.inkredibles.wema20;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.List;
/*
Add users fragment allows the user to choose which users he or she would like to be in their group.
The user can choose from a list of all users and when a user is chosen their view in the recyclerview
is highlighted in background.
 */

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    protected List<ParseUser> mUsers;
    private Context context;
    protected List<ParseUser> mAddedUsers;

    public UsersAdapter(List<ParseUser> allUsers, List<ParseUser> addedUsers) {
        mUsers = allUsers;
        mAddedUsers = addedUsers;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        public ImageView ivUserProfile;
        public TextView tvUser;

        public ViewHolder(View itemView) {
            super(itemView);

            ivUserProfile = (ImageView) itemView.findViewById(R.id.ivUserProfile);
            tvUser = (TextView) itemView.findViewById(R.id.tvUser);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            ParseUser user = mUsers.get(getAdapterPosition());
            if (isSelected(user)) {
                view.setSelected(false);
                removeUser(user);
            } else {
                view.setSelected(true);
                mAddedUsers.add(user);
            }

        }

        public void removeUser(ParseUser user) {
            for (int i = 0; i < mAddedUsers.size(); i++) {
                if (mAddedUsers.get(i).getUsername().equals(user.getUsername()))
                    mAddedUsers.remove(i);
            }

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View userView = inflater.inflate(R.layout.item_user, parent, false);
        ViewHolder viewHolder = new ViewHolder(userView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseUser user = mUsers.get(position);
        holder.tvUser.setText(user.getUsername());
        holder.itemView.setSelected(isSelected(user));
    }

    public boolean isSelected(ParseUser user) {
        for (int i = 0; i < mAddedUsers.size(); i++) {
            if (mAddedUsers.get(i).getUsername().equals(user.getUsername())) return true;
        }

        return false;
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public List<ParseUser> getAddedUsers() {
        return mAddedUsers;
    }

    public void clear() {
        mUsers.clear();
        notifyDataSetChanged();
    }
}
