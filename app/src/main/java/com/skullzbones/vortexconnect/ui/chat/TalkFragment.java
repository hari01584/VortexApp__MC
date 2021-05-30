package com.skullzbones.vortexconnect.ui.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skullzbones.vortexconnect.API.ChatAPI;
import com.skullzbones.vortexconnect.MainActivity;
import com.skullzbones.vortexconnect.R;
import com.skullzbones.vortexconnect.Utils.RandomGens;
import com.skullzbones.vortexconnect.fake.MessagesFixtures;
import com.skullzbones.vortexconnect.model.Message;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class TalkFragment extends AppCompatActivity implements MessagesListAdapter.SelectionListener, MessagesListAdapter.OnLoadMoreListener, DateFormatter.Formatter, MessageInput.InputListener, MessageInput.AttachmentsListener {

    private static int TOTAL_MESSAGES_COUNT = 1000;
    private static final String TAG = "r/TalkFragment";
    private TalkViewModel mViewModel;
    MessagesList messagesList;
    MessageInput input;
    private int selectionCount;
    private Date lastLoadedDate;

    public static TalkFragment newInstance() {
        return new TalkFragment();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talk_fragment);

        messagesList = findViewById(R.id.messagesList);
        input = findViewById(R.id.input);

        mViewModel = new ViewModelProvider(this).get(TalkViewModel.class);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mViewModel.senderId = extras.getString("EXTRA_USERID");
            Log.i(TAG, "Init with"+mViewModel.senderId);
        }

        TOTAL_MESSAGES_COUNT = MainActivity.databaseMain.messageDAO().getTotalMessageUser(mViewModel.senderId);

        // TODO: Use the ViewModel
        if(mViewModel.messagesAdapter == null){
            mViewModel.imageLoader = (imageView, url, payload) -> Picasso.get().load(url).into(imageView);
            initAdapter();
            input.setInputListener(this);
            input.setAttachmentsListener(this);
            lastLoadedDate = new Date();
            MainActivity.databaseMain.userDAO().updateLastTimestamp(mViewModel.senderId).subscribe();

            MainActivity.databaseMain.messageDAO().loadAllById(mViewModel.senderId, 20)
                    .observe(this, messages -> {
                        Log.i(TAG, mViewModel.senderId+"With "+messages.size());
                        mViewModel.messagesAdapter.clear();
                        mViewModel.messagesAdapter.addToEnd(messages, false);
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed();
        } else {
            mViewModel.messagesAdapter.unselectAllItems();
        }
    }

    private void initAdapter() {
        mViewModel.messagesAdapter = new MessagesListAdapter<>(mViewModel.senderId, mViewModel.imageLoader);
        mViewModel.messagesAdapter.enableSelectionMode(this);
        mViewModel.messagesAdapter.setLoadMoreListener(this);
        mViewModel.messagesAdapter.setDateHeadersFormatter(this);
        messagesList.setAdapter(mViewModel.messagesAdapter);

       // mViewModel.messagesAdapter.addToStart(MessagesFixtures.getTextMessage(), true);
    }

    @Override
    public void onSelectionChanged(int count) {

    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
            loadMessages();
        }
    }


    protected void loadMessages() {
        Log.i(TAG, "onLoadMore: " + lastLoadedDate.getTime());

        MainActivity.databaseMain.messageDAO().loadMoreMessageById(mViewModel.senderId, lastLoadedDate.getTime(), 20)
                .subscribe(new SingleObserver<List<Message>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull List<Message> messages) {
                        Log.i(TAG, "Addin");
                        lastLoadedDate = messages.get(messages.size() - 1).getCreatedAt();
                        mViewModel.messagesAdapter.addToEnd(messages, false);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

    private MessagesListAdapter.Formatter<Message> getMessageStringFormatter() {
        return message -> {
            String createdAt = new SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                    .format(message.getCreatedAt());

            String text = message.getText();
            if (text == null) text = "[attachment]";

            return String.format(Locale.getDefault(), "%s: %s (%s)",
                    message.getUser().getName(), text, createdAt);
        };
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        ChatAPI.sendMessage(mViewModel.senderId, input.toString());

        Message scrap = new Message("mine"+ RandomGens.generateString(), mViewModel.senderId, input.toString(), new Date());
        MainActivity.databaseMain.messageDAO().insert(scrap).subscribe();

        return true;
    }

    @Override
    public void onAddAttachments() {

    }

    @Override
    public String format(Date date) {
        if (DateFormatter.isToday(date)) {
            return getString(R.string.date_header_today);
        } else if (DateFormatter.isYesterday(date)) {
            return getString(R.string.date_header_yesterday);
        } else {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
        }
    }
}