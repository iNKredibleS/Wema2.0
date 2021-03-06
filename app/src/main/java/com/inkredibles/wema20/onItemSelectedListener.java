package com.inkredibles.wema20;


import android.support.v7.widget.CardView;
import android.widget.TextView;

import com.inkredibles.wema20.models.Post;
import com.inkredibles.wema20.models.Rak;
import com.parse.ParseImageView;
import com.parse.ParseRole;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public interface onItemSelectedListener {

    public void fromFeedtoDetail(Post post, ParseImageView parseImageView, String sharedTransitionName, int position, ArrayList<Post> posts, TextView title, String titleTransition, CardView cardView, String cardTransition);

    //Transitions from any fragment to the feed fragment
    void toFeed();

    //Transitions from any fragment to the CreatePost Fragment
    void fromRAKtoCreatePost(Rak rak);

    void toCreateRak();

    void toAddUsers();


    void addRakToServer(String s, String stringDate);

    void fromAddUserstoCreateGroup (List<ParseUser> addedUsers);

    void fromCreateGrouptoCurrentGroup (ParseRole currentRole);

    void fromCurrentGrouptoCreatePost (ParseRole currentRole);

    void fromCurrentGrouptoCreateRak(ParseRole currentRole);

    void toCurrentGroup(ParseRole currentRole);

  //  void fromGroupAdaptertoCurrentGroup(ParseRole currentRole);

    void fromGroupstoCreateGroup();



//    void setIsGroup (Boolean bool);
//
//    void setIsReflection (Boolean bool);
//
//    void setIsRak(Boolean bool);



}
