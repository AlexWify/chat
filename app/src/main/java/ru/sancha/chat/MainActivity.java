package ru.sancha.chat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import android. text.format.DateFormat;

public class MainActivity extends AppCompatActivity {

    private static int SING_IN_CODE = 1;
    private RelativeLayout activity_main;
    private FirebaseListAdapter<Message> adapter;
    private final String FareBaseLink = "https://chat-7efa2-default-rtdb.europe-west1.firebasedatabase.app";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SING_IN_CODE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(activity_main, "Регестрация прошла успешно)", Snackbar.LENGTH_LONG).show();
                displayAllMessages();
            } else {
                Snackbar.make(activity_main, "К сожалению вы не зарегестрированы :(", Snackbar.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = findViewById(R.id.activity_main);
        FloatingActionButton sendBtn = findViewById(R.id.btnSend);

        // Действия при нажатии на кнопку
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText textFiled = findViewById(R.id.messageField);
                if (textFiled.getText().toString().length() <=0) {
                    Snackbar.make(activity_main, "Пусто - негусто)", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                // отправка сообщения в БД Фаербазе
                FirebaseDatabase.getInstance(FareBaseLink).getReference().push().setValue(
                        new Message(
                                FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                                textFiled.getText().toString()
                        )
                );
                textFiled.setText("");
            }
        });

        // не зареган пользователь
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            // регестрирует пользователя
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SING_IN_CODE);
        else {
            Snackbar.make(activity_main, "Регистрация прошла успешно)", Snackbar.LENGTH_SHORT).show();
            displayAllMessages();
        }
    }

    // Вывод сообщений на экран
    private void displayAllMessages() {
        ListView listOfMessages = findViewById(R.id.list_of_messages);

        Query query = FirebaseDatabase.getInstance(FareBaseLink).getReference();

        FirebaseListOptions<Message> options = new FirebaseListOptions.Builder<Message>()
                        .setQuery(query, Message.class)
                        .setLayout(R.layout.list_item)
                        .build();

        adapter = new FirebaseListAdapter<Message>(options) {
            @Override
            protected void populateView(View v, Message model, int position) {
//                Log.e("LOG_SANYA", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                TextView mess_user, mess_time, mess_text;
                mess_user = v.findViewById(R.id.message_user);
                mess_time = v.findViewById(R.id.message_time);
                mess_text = v.findViewById(R.id.message_text);

                mess_user.setText(model.getUserName());
                mess_text.setText(model.getTextMessage());
                mess_time.setText(DateFormat.format("dd-mm-yyyy HH:mm:ss", model.getMessageTime()));

//                Log.e("LOG_SANYA", model.getUserName() + model.getTextMessage() + DateFormat.format("dd-mm-yyyy HH:mm:ss", model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
        adapter.startListening();
    }


    @Override
    protected void onStart() {
        super.onStart();
//        adapter.startListening();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

}