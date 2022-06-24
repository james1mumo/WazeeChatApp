package com.dekut.wazeechatapp.ui.sms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dekut.wazeechatapp.R;
import com.dekut.wazeechatapp.SmsActivity;
import com.dekut.wazeechatapp.databinding.FragmentSmsBinding;
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

public class SmsFragment extends Fragment {

    private SmsViewModel smsViewModel;
    private FragmentSmsBinding binding;

    private SmsFragmentAdapter smsFragmentAdapter;
    private ArrayList<Map<String , String >> hashMapNames;
    private TextView textViewError;
    String phone;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        smsViewModel =
                new ViewModelProvider(this).get(SmsViewModel.class);

        binding = FragmentSmsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        hashMapNames = new ArrayList<>();
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        smsFragmentAdapter = new SmsFragmentAdapter(getContext(), hashMapNames);
        recyclerView.setAdapter(smsFragmentAdapter);

        textViewError = root.findViewById(R.id.textViewError);

        loadRecentSms();

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadRecentSms(){
        SharedPreferences prefs = getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        phone = prefs.getString("phone", "");

        phone = phone.replaceAll(" ","");
        phone = phone.replaceAll(" ","");

        if (phone.startsWith("07")) phone = phone.replace("07", "2547");
        if (phone.startsWith("01")) phone = phone.replace("01", "2541");


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Sms");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean anSmsExists = false;

                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    if (dataSnapshot.getKey().toString().contains(phone)){
                        DatabaseReference databaseReferenceNames = FirebaseDatabase.getInstance().getReference("Names")
                                .child(dataSnapshot.getKey().toString());

                        databaseReferenceNames.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String otherPhone = snapshot.getKey().toString().replace("-", "").replace(phone,"");
                                String name;

                                Object objectName = snapshot.child(otherPhone+"-name").getValue();
                                if (objectName == null) {
                                    name = "New Message...";
                                }else {
                                    name = objectName.toString();
                                }
                                HashMap<String, String > hashMap =  new HashMap<>();
                                hashMap.put("phone", otherPhone);
                                hashMap.put("name", name);
                                hashMapNames.add(hashMap);
                                smsFragmentAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                        anSmsExists = true;
                    }
                }

                if (!anSmsExists){
                    textViewError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}

class SmsFragmentAdapter extends RecyclerView.Adapter<SmsFragmentAdapter.ViewHolder>{

    private ArrayList<Map<String, String>> mData;
    private LayoutInflater mInflater;
    private SmsFragmentAdapter.ItemClickListener mClickListener;
    private Map<String, String> message;
    private Context context;

    // data is passed into the constructor
    public SmsFragmentAdapter(Context context, ArrayList<Map<String, String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public SmsFragmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_contact, parent, false);
        return new SmsFragmentAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(SmsFragmentAdapter.ViewHolder holder, int position) {
        message = mData.get(position);

        String name = message.get("name");
        String phone = message.get("phone");

        holder.textViewName.setText(name);
        holder.textViewPic.setText(name.substring(0,1).toUpperCase());
        holder.textViewPhone.setText(phone);

        holder.buttonCall.setVisibility(View.GONE);
        holder.buttonSms.setText("View Messages");
        holder.buttonSms.setOnClickListener(v -> {
            Intent intent = new Intent(context, SmsActivity.class);
            intent.putExtra("sendersPhone", getUsersPhone());
            intent.putExtra("receiversName", name);
            intent.putExtra("receiversPhone", phone);
            context.startActivity(intent);
        });




    }

    private String getUsersPhone(){
//        String[] columnNames = new String[] {ContactsContract.Profile.DISPLAY_NAME, ContactsContract.Profile.PHOTO_ID};
//        String name = "";
//
//        Cursor c = context.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, columnNames, null, null, null);
//        int count = c.getCount();
//        boolean b = c.moveToFirst();
//        int position = c.getPosition();
//        if (count == 1 && position == 0) {
//            for (int j = 0; j < count; j++) {
//                name = c.getString(c.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME));
//                @SuppressLint("Range") String photoID = c.getString(c.getColumnIndex(ContactsContract.Profile.PHOTO_ID));
//            }
//        }
//        c.close();
//
//        TelephonyManager tm = (TelephonyManager)context.getSystemService(TELEPHONY_SERVICE);
//        String number = tm.getLine1Number();
//        return name;

        SharedPreferences prefs = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        return prefs.getString("phone", "");

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }



    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewName, textViewPhone, textViewPic;
        Button buttonCall, buttonSms;

        ViewHolder(View itemView) {
            super(itemView);
            textViewPic = itemView.findViewById(R.id.textViewPic);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPhone = itemView.findViewById(R.id.textViewPhone);
            buttonCall = itemView.findViewById(R.id.buttonCall);
            buttonSms = itemView.findViewById(R.id.buttonSms);



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
    public void setClickListener(SmsFragmentAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}


