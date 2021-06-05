package com.skullzbones.vortexconnect.ui.me;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
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

import com.skullzbones.vortexconnect.API.UserAPI;
import com.skullzbones.vortexconnect.MainActivity;
import com.skullzbones.vortexconnect.R;
import com.skullzbones.vortexconnect.SharedViewModel;
import com.skullzbones.vortexconnect.Utils.ToastUtils;
import com.skullzbones.vortexconnect.interfaces.ClickInterface;
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
    private TextView editProfile;
    private SharedViewModel sharedViewModel;


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
        editProfile = v.findViewById(R.id.textOnclick);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view,
        @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(MeViewModel.class);
        sharedViewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);

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

        bindListeners();
    }

    private void bindListeners() {
        editProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtils.out(getContext(), R.string.to_change_prof);
            }
        });

        avatarImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showAndRetrievePopup("Image change", "Paste a public url having your image",
                    (ok) -> {
                        UserAPI.updateImage(getContext(), ok, sharedViewModel.mAuth.getCurrentUser());
                    },
                    (cancel) -> {

                    });
            }
        });

        textViewHeader.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showAndRetrievePopup("Display Name Change", "Enter a name (<20 chars)",
                    (ok) -> {
                        UserAPI.updateDisplayName(getContext(), ok, sharedViewModel.mAuth.getCurrentUser());
                    }, null);
            }
        });
    }

    private void showAndRetrievePopup(String title, String message, ClickInterface onClick, ClickInterface cancel) {
        if(sharedViewModel.mAuth.getCurrentUser().isAnonymous()){
            ToastUtils.out(getContext(), R.string.anony_user_found);
            return;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(title);
        alert.setMessage(message);
        final EditText input = new EditText(getContext());
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onClick.execute(input.getText().toString());
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //cancel.execute(input.getText().toString());
            }
        });
        alert.show();
    }
}