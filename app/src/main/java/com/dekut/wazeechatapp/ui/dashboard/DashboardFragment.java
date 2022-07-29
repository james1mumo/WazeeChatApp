package com.dekut.wazeechatapp.ui.dashboard;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dekut.wazeechatapp.R;
import com.dekut.wazeechatapp.SmsActivity;
import com.dekut.wazeechatapp.databinding.FragmentDashboardBinding;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;
    private DashboardFragmentAdapter dashboardFragmentAdapter;
    private ArrayList<Map<String , String >> hashMapNames, allContacts;
    private EditText editTextSearch;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        hashMapNames = new ArrayList<>();
        allContacts = new ArrayList<>();

        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dashboardFragmentAdapter = new DashboardFragmentAdapter(getContext(), hashMapNames);
        recyclerView.setAdapter(dashboardFragmentAdapter);

        if(isPermissionGranted()){
            getCallDetails(getContext());
        }else {
            FancyToast.makeText(getContext(), "Grant Call Log Permission", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
        }

        editTextSearch = root.findViewById(R.id.editTextSearch);

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filterContacts(editable.toString());
            }
        });


        return root;
    }

    private boolean isPermissionGranted() {
        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALL_LOG);

        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        }else{
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CALL_LOG}, 2);
            FancyToast.makeText(getContext(), "Grant Call Log Permission", FancyToast.LENGTH_LONG, FancyToast.INFO, false).show();

            return isPermissionGranted();
        }
    }


    private void getCallDetails(Context context) {
        StringBuffer stringBuffer = new StringBuffer();
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");

        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int name = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);

        int i = 0;
        while (cursor.moveToNext()) {
            String phNumber = cursor.getString(number);
            String phName = cursor.getString(name);
            String callType = cursor.getString(type);
            String callDate = cursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = cursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }

            HashMap<String, String > hashMap =  new HashMap<>();
            hashMap.put("phone", phNumber);
            hashMap.put("name", phName);
            hashMap.put("date", callDate);
            hashMap.put("duration", callDuration);
            hashMap.put("type", dir);

            hashMapNames.add(hashMap);
            allContacts.add(hashMap);

            i++;
            if (i>20) break;

        }
        cursor.close();
    }

    private void filterContacts(String search) {
        ArrayList<Map<String , String >> hashMapNamesFiltered = new ArrayList<>();

        if (search.isEmpty()){
            hashMapNames.clear();
            hashMapNames.addAll(allContacts);
        }else{
            for (Map<String , String > hashMap: allContacts){
                if (hashMap.get("name").toString().toLowerCase().contains(search.toLowerCase(Locale.ROOT)))
                    hashMapNamesFiltered.add(hashMap);
            }
            hashMapNames.clear();
            hashMapNames.addAll(hashMapNamesFiltered);
        }
        dashboardFragmentAdapter.notifyDataSetChanged();


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

class DashboardFragmentAdapter extends RecyclerView.Adapter<DashboardFragmentAdapter.ViewHolder>{

    private ArrayList<Map<String, String>> mData;
    private LayoutInflater mInflater;
    private DashboardFragmentAdapter.ItemClickListener mClickListener;
    private Map<String, String> message;
    private Context context;
    private TextToSpeech textToSpeechSystem;

    // data is passed into the constructor
    public DashboardFragmentAdapter(Context context, ArrayList<Map<String, String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public DashboardFragmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_recent_contact, parent, false);
        return new DashboardFragmentAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(DashboardFragmentAdapter.ViewHolder holder, int position) {
        message = mData.get(position);

        String name = message.get("name");
        String phone = message.get("phone");


        holder.textViewName.setText(name);
        if (name!=null)
            holder.textViewPic.setText(name.substring(0,1).toUpperCase());
        else {
            holder.textViewPic.setText("+");

        }
        holder.textViewPhone.setText(phone);
        holder.textViewCallType.setText(message.get("type")+" CALL for "+message.get("duration")+" seconds");

        holder.buttonCall.setOnClickListener(v -> {
            //add calling feature
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (!name.isEmpty())
                builder.setTitle("Do you want to call "+name+"?");
            else {
                builder.setTitle("Do you want to call "+phone+"?");

            }
                textToSpeechSystem = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    if (!name.isEmpty())
                        textToSpeechSystem.speak("Do you want to call "+name+"?", TextToSpeech.QUEUE_ADD, null);
                    else {
                        textToSpeechSystem.speak("Do you want to make this call?", TextToSpeech.QUEUE_ADD, null);

                    }
                }
            });

            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    textToSpeechSystem = new TextToSpeech(context, status -> {
                        if (status == TextToSpeech.SUCCESS) {
                            textToSpeechSystem.speak("Calling "+name, TextToSpeech.QUEUE_ADD, null);
                        }
                    });
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:"+phone));
                    context.startActivity(intent);

                    FancyToast.makeText(context,"Choose Sim card to call with", FancyToast.LENGTH_LONG, FancyToast.INFO, false).show();

                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

        });
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
        TextView textViewName, textViewPhone, textViewPic, textViewCallType;
        Button buttonCall, buttonSms;

        ViewHolder(View itemView) {
            super(itemView);
            textViewPic = itemView.findViewById(R.id.textViewPic);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewCallType = itemView.findViewById(R.id.textViewCallType);
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
    public void setClickListener(DashboardFragmentAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
