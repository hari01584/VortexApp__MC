package com.skullzbones.vortexconnect.ui.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.skullzbones.vortexconnect.API.ChatAPI;
import com.skullzbones.vortexconnect.API.UserAPI;
import com.skullzbones.vortexconnect.MainActivity;
import com.skullzbones.vortexconnect.R;
import com.skullzbones.vortexconnect.SharedViewModel;
import com.skullzbones.vortexconnect.Utils.ToastUtils;
import com.skullzbones.vortexconnect.converters.ArrayConverter;
import com.skullzbones.vortexconnect.fake.DialogsFixtures;
import com.skullzbones.vortexconnect.fake.UserPersona;
import com.skullzbones.vortexconnect.interfaces.ChatAPIMessage;
import com.skullzbones.vortexconnect.interfaces.UserAPIQuery;
import com.skullzbones.vortexconnect.model.Dialog;
import com.skullzbones.vortexconnect.model.Message;
import com.skullzbones.vortexconnect.model.User;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.utils.DateFormatter;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collector;

import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class ChatFragment extends Fragment implements DialogsListAdapter.OnDialogClickListener, DateFormatter.Formatter, PopupMenu.OnMenuItemClickListener, MaterialSearchBar.OnSearchActionListener {

    private static final String TAG = "r/ChatFragment";
    private ChatViewModel mViewModel;
    private SharedViewModel sharedViewModel;
    private DialogsList dialogsList;
    private MaterialSearchBar searchBar;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_fragment, container, false);
        dialogsList = v.findViewById(R.id.dialogsList);
        searchBar = v.findViewById(R.id.searchBar);
        searchBar.setSpeechMode(false);
        searchBar.inflateMenu(R.menu.menu_chat);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        sharedViewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        // TODO: Use the ViewModel
        if(mViewModel.dialogsAdapter==null){
            mViewModel.imageLoader = (imageView, url, payload) -> Picasso.get().load(url).into(imageView);
            initAdapter();
            searchBar.setOnSearchActionListener(this);
            searchBar.getMenu().setOnMenuItemClickListener(this);


           mViewModel.dialogs.observe(getViewLifecycleOwner(), new Observer<List<Dialog>>() {
               @Override
               public void onChanged(List<Dialog> dialogs) {
                   mViewModel.dialogsAdapter.setItems(dialogs);
               }
           });


            MainActivity.databaseMain.messageDAO().getDialogMakerMessages()
                    .observe(getViewLifecycleOwner(), this::refreshDialogList);
        }


    }

    private void refreshDialogList(List<Message> messages) {
        ArrayList<com.skullzbones.vortexconnect.model.Dialog> d = new ArrayList<>();
        for(Message message : messages){
            Log.i(TAG, "Ref"+message.toString());
            User sender = message.getSenderUser();
            String id = sender.id;
            String dialogPhoto = sender.avatar;
            String dialogName = sender.name;
            ArrayList<User> users = new ArrayList<>();
            users.add(MainActivity.myAccount.getValue());
            users.add(sender);
            int count = sender.lastChecked > message.createdAt.getTime()?0:1;
            if(sender.getId().equals("0")) count = 0;
            com.skullzbones.vortexconnect.model.Dialog fromMess =
                    new com.skullzbones.vortexconnect.model.Dialog(
                            id,
                            dialogName,
                            dialogPhoto,
                            users,
                            message,
                            count);
            if(fromMess.getId().equals("0")) continue;
            d.add(fromMess);
        }
        if(d.size() == 0){
            d.add(UserPersona.getAdminMessage());
        }
        mViewModel.dialogs.setValue(d);
    }

    public void unMarklist(String uid){
        ArrayList<com.skullzbones.vortexconnect.model.Dialog> dialogs = new ArrayList<>();
        for(Dialog d : mViewModel.dialogs.getValue()){
            if(d.getId().equals(uid)){
                d.setUnreadCount(0);
            }
            dialogs.add(d);
        }
        mViewModel.dialogs.setValue(dialogs);
    }

    private void initAdapter() {
        mViewModel.dialogsAdapter = new DialogsListAdapter<>(mViewModel.imageLoader);
//        mViewModel.dialogsAdapter.setItems(DialogsFixtures.getDialogs());
        mViewModel.dialogsAdapter.setOnDialogClickListener(this);
        mViewModel.dialogsAdapter.setDatesFormatter(this);
        dialogsList.setAdapter(mViewModel.dialogsAdapter);

    }

    @Override
    public void onDialogClick(IDialog dialog) {
        unMarklist(dialog.getId());
        List<User> usr = dialog.getUsers();
        openTalkFragment(usr.get(1).id);
    }


    @Override
    public String format(Date date) {
        if (DateFormatter.isToday(date)) {
            return DateFormatter.format(date, DateFormatter.Template.TIME);
        } else if (DateFormatter.isYesterday(date)) {
            return getString(R.string.date_header_yesterday);
        } else if (DateFormatter.isCurrentYear(date)) {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH);
        } else {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        if(enabled){
            setSuggestionSingleton("Search for user, Please use exact name");
        }
    }

    public void setSuggestionSingleton(String sugges){
        ArrayList<String> lis = new ArrayList<>(Arrays.asList(sugges));
        searchBar.setLastSuggestions(lis);
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        setSuggestionSingleton("Finding user with name "+text.toString());
        UserAPI.searchUserByName(text.toString(), new UserAPIQuery() {
            @Override
            public void returnsUser(User user) {
                if(user != null){
                    openTalkFragment(user.id);
                }
                else{
                    ToastUtils.out(getContext(), R.string.search_no_user_found);
                }
            }
        });
    }

    private void openTalkFragment(String user) {
        ToastUtils.make(getContext(), "Found "+user);
        Intent intent = new Intent(getContext(), TalkFragment.class);
        intent.putExtra("EXTRA_USERID", user);
        startActivity(intent);
    }

    @Override
    public void onButtonClicked(int buttonCode) {

    }

    @Override
    public void onResume() {
        super.onResume();
    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
//    }
}