package com.skullzbones.vortexconnect.ui.clans;

import android.view.View.OnClickListener;
import android.widget.Button;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skullzbones.vortexconnect.IAP.DonationsManager;
import com.skullzbones.vortexconnect.R;
import org.jetbrains.annotations.NotNull;

public class ClansFragment extends Fragment {

    private ClansViewModel mViewModel;
    private DonationsManager donationsManager;
    public static ClansFragment newInstance() {
        return new ClansFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.clans_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view,
        @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(ClansViewModel.class);
        // TODO: Use the ViewModel

        donationsManager = new DonationsManager(getContext());

        Button m1 = getActivity().findViewById(R.id.button_donate_3);
        m1.setOnClickListener(view1 -> donationsManager.donateIndex(getActivity(), 0));
        Button m2 = getActivity().findViewById(R.id.button_donate_10);
        m2.setOnClickListener(view1 -> donationsManager.donateIndex(getActivity(), 1));
        Button m3 = getActivity().findViewById(R.id.button_donate_50);
        m3.setOnClickListener(view1 -> donationsManager.donateIndex(getActivity(), 2));
        Button m4 = getActivity().findViewById(R.id.button_donate_100);
        m4.setOnClickListener(view1 -> donationsManager.donateIndex(getActivity(), 3));
    }

}