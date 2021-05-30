package com.skullzbones.vortexconnect.ui.me;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skullzbones.vortexconnect.MainActivity;
import com.skullzbones.vortexconnect.R;
import com.skullzbones.vortexconnect.model.User;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;

public class MeFragment extends Fragment {

    private MeViewModel mViewModel;
    private ImageView avatarImageView;
    private TextView textViewHeader;
    private TextView textViewHeaderSub;
    private TextView myName;
    private TextView myEmail;
    private TextView myDebugKey;
    public static MeFragment newInstance() {
        return new MeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppTheme_ProfileTheme);

        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View v = localInflater.inflate(R.layout.me_fragment, container, false);
        avatarImageView = v.findViewById(R.id.profile_image);
        textViewHeader = v.findViewById(R.id.me_headName);
        textViewHeaderSub = v.findViewById(R.id.me_headSname);
        myName = v.findViewById(R.id.me_myname);
        myEmail = v.findViewById(R.id.me_myemail);
        myDebugKey = v.findViewById(R.id.me_debugkey);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view,
        @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(MeViewModel.class);
        if(mViewModel.uid==null || mViewModel.uid.isEmpty())
        {
            MainActivity.myAccount.observe(getViewLifecycleOwner(), new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    mViewModel.avatarUrl = user.avatar;
                    mViewModel.name = user.name;
                    mViewModel.uid = user.id;
                    mViewModel.debugKey = user.id;
                    setProfileData();
                }
            });
        }
        else{
            setProfileData();
        }
    }

    void setProfileData(){
        Picasso.get().load(mViewModel.avatarUrl).fit().into(avatarImageView);
        textViewHeader.setText(mViewModel.name);
        textViewHeaderSub.setText("Welcome to profile of "+mViewModel.name);
        myName.setText(mViewModel.name);
        myDebugKey.setText(mViewModel.uid);

    }
}