package com.skullzbones.vortexconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.internal.NavigationMenu;
import com.google.api.LabelDescriptor;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.skullzbones.vortexconnect.API.ChatAPI;
import com.skullzbones.vortexconnect.API.UserAPI;
import com.skullzbones.vortexconnect.Utils.ToastUtils;
import com.skullzbones.vortexconnect.database.DatabaseMain;
import com.skullzbones.vortexconnect.interfaces.UserAPIExchange;
import com.skullzbones.vortexconnect.model.User;

import com.skullzbones.vortexconnect.ui.chat.ChatViewModel;

import io.reactivex.rxjava3.schedulers.Schedulers;
import me.ibrahimsn.lib.SmoothBottomBar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "t/MainActivity";
    NavController navController;
    private SharedViewModel mainViewModel;
    public static DatabaseMain databaseMain;
    public static MutableLiveData<User> myAccount = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        getSupportActionBar().hide();

        mainViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        FirebaseApp.initializeApp(this);
       // enableDebugVars();

        mainViewModel.mDatabase = FirebaseDatabase.getInstance().getReference();
        //mainViewModel.basicPrefs = Preference_BasicPrefs.getInstance(this);
        databaseMain = Room.databaseBuilder(getApplicationContext(), DatabaseMain.class, "dbmain")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries().build();

        SmoothBottomBar bottomBar = findViewById(R.id.bottomBar);

        PopupMenu popupMenu = new PopupMenu(this, null);
        popupMenu.inflate(R.menu.menu_main);
        Menu menu = popupMenu.getMenu();

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        bottomBar.setupWithNavController(menu, navController);

        mainViewModel.mAuth = FirebaseAuth.getInstance();
        initilizeAccount();

        initilizeChat();

    }

    private void initilizeChat() {
        ChatAPI.selfInit();
        myAccount.observe(this, user -> {
            ChatAPI.detachAllListener(user);
            ChatAPI.attachAllListener(user, recv -> {
                MainActivity.databaseMain.messageDAO().insert(recv).subscribeOn(Schedulers.io()).subscribe();
            });
        });
    }

    private void enableDebugVars() {
        String target_ip = "192.168.18.3";

        // TODO: Remove in production
        FirebaseAuth.getInstance().useEmulator(target_ip, 9099);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.useEmulator(target_ip, 8080);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        firestore.setFirestoreSettings(settings);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
        database.useEmulator(target_ip, 9000);
    }

    private void initilizeAccount() {
        FirebaseUser currentUser = mainViewModel.mAuth.getCurrentUser();
        if(currentUser==null){
            mainViewModel.mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInAnonymously:success");
                                FirebaseUser user = mainViewModel.mAuth.getCurrentUser();
                                onSignInSuccess(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInAnonymously:failure", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else{
            onSignInSuccess(currentUser);
        }
    }

    private void onSignInSuccess(FirebaseUser o) {
        ToastUtils.out(this, R.string.successful_login);
        mainViewModel.setFirebaseUser(o);
        firestoreMappings(o);
    }

    private void firestoreMappings(FirebaseUser o) {
       // ToastUtils.out(this, R.string.first_time_open);
        UserAPI.userEntryInit(o, documentSnapshot -> myAccount.setValue(new User(documentSnapshot)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "OnDestroy");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }
}