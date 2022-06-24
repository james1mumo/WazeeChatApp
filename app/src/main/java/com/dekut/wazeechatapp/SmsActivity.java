package com.dekut.wazeechatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.himanshusoni.chatmessageview.ChatMessageView;

public class SmsActivity extends AppCompatActivity {
    private EditText editTextMessage;
    private FloatingActionButton floatingActionButtonSendMessage;
    private String sendersPhone, receiversName, receiversPhone, dbPath;
    private boolean isSender = false, isReceiver = false;
    private SmsActivityAdapter smsActivityAdapter;
    private ArrayList<Map<String , String >> messages;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);


        editTextMessage = findViewById(R.id.editTextMessage);
        floatingActionButtonSendMessage = findViewById(R.id.fabSendMessage);

        sendersPhone = getIntent().getStringExtra("sendersPhone");
        receiversName = getIntent().getStringExtra("receiversName");
        receiversPhone = getIntent().getStringExtra("receiversPhone");



        isSender = true;
        floatingActionButtonSendMessage.setOnClickListener(v -> sendMessage());

        dbPath = getDbPath();


        messages = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        smsActivityAdapter = new SmsActivityAdapter(this, messages, sendersPhone);
        recyclerView.setAdapter(smsActivityAdapter);


        loadMessages();

    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy @ HH:mm", Locale.US);
        String date = dateFormat.format(new Date());

        if (message.equalsIgnoreCase("")){
            FancyToast.makeText(getBaseContext(), "Enter message to send", FancyToast.LENGTH_LONG, FancyToast.WARNING,false).show();
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Sms")
                .child(dbPath);
        HashMap<String ,String > hashMap = new HashMap<>();
        hashMap.put("message", message);
        hashMap.put("date", date);
        hashMap.put("sender", sendersPhone);

        databaseReference.push().setValue(hashMap);

        DatabaseReference databaseReferenceNames = FirebaseDatabase.getInstance().getReference("Names").child(dbPath);
        databaseReferenceNames.child(receiversPhone+"-name").setValue(receiversName);

        messages.add(hashMap);
        smsActivityAdapter.notifyDataSetChanged();

        editTextMessage.setText("");
        FancyToast.makeText(getBaseContext(), "Message send Successfully", FancyToast.LENGTH_LONG, FancyToast.SUCCESS,false).show();


    }

    private void loadMessages(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Sms")
                .child(dbPath);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot dataSnapshotMessage: snapshot.getChildren()){
                    HashMap<String ,String > hashMap = new HashMap<>();
                    hashMap.put("message", dataSnapshotMessage.child("message").getValue().toString());
                    hashMap.put("date", dataSnapshotMessage.child("date").getValue().toString());
                    hashMap.put("sender", dataSnapshotMessage.child("sender").getValue().toString());

                    messages.add(hashMap);
                    smsActivityAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private String getDbPath(){
        String dbPath;
        sendersPhone = sendersPhone.replaceAll(" ","");
        receiversPhone = receiversPhone.replaceAll(" ","");
        sendersPhone = sendersPhone.replace("+", "");
        receiversPhone = receiversPhone.replace("+", "");

        if (sendersPhone.startsWith("07")) sendersPhone = sendersPhone.replace("07", "2547");
        if (sendersPhone.startsWith("01")) sendersPhone = sendersPhone.replace("01", "2541");
        if (receiversPhone.startsWith("07")) receiversPhone = receiversPhone.replace("07", "2547");
        if (receiversPhone.startsWith("01")) receiversPhone = receiversPhone.replace("01", "2541");


        if (sendersPhone.compareTo(receiversPhone) < 0){
            dbPath = sendersPhone+'-'+receiversPhone;
        }else {
            dbPath = receiversPhone+'-'+sendersPhone;
        }

        return dbPath;
    }
}

class SmsActivityAdapter extends RecyclerView.Adapter<SmsActivityAdapter.ViewHolder>{

    private ArrayList<Map<String, String>> mData;
    private LayoutInflater mInflater;
    private SmsActivityAdapter.ItemClickListener mClickListener;
    private Map<String, String> message;
    private Context context;
    private String applicationID = "", applicationUsername = "", sendersPhone;
    private ProgressDialog progressDialog;
    private TextToSpeech textToSpeechSystem;

    // data is passed into the constructor
    public SmsActivityAdapter(Context context, ArrayList<Map<String, String>> data, String sendersPhone) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.sendersPhone = sendersPhone;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public SmsActivityAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_chat_message, parent, false);
        return new SmsActivityAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(SmsActivityAdapter.ViewHolder holder, int position) {
        message = mData.get(position);


        if (!message.get("sender").equalsIgnoreCase(sendersPhone)){
            holder.chatMessageView.setArrowPosition(ChatMessageView.ArrowPosition.LEFT);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.START;
            holder.chatMessageView.setLayoutParams(layoutParams);
            holder.linearLayoutMessage.setGravity(Gravity.START);


        }

        holder.textViewMessage.setText(message.get("message"));
        holder.textViewDate.setText(message.get("date"));

        holder.chatMessageView.setOnClickListener(v -> {
            //say the sms out loud
            textToSpeechSystem = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechSystem.speak(holder.textViewMessage.getText().toString(), TextToSpeech.QUEUE_ADD, null);
                }
            });
        });



    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }



    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewMessage, textViewDate;
        ChatMessageView chatMessageView;
        LinearLayout linearLayoutMessage;

        ViewHolder(View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            chatMessageView = itemView.findViewById(R.id.chatMessageView);
            linearLayoutMessage = itemView.findViewById(R.id.linearLayoutMessage);



            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());


        }
    }

    // convenience method for getting data at click position
    Map<String, String> getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(SmsActivityAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
