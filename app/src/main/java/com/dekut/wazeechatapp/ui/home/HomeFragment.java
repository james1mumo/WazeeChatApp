package com.dekut.wazeechatapp.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dekut.wazeechatapp.R;
import com.dekut.wazeechatapp.SmsActivity;
import com.dekut.wazeechatapp.databinding.FragmentHomeBinding;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.himanshusoni.chatmessageview.ChatMessageView;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private HomeFragmentAdapter homeFragmentAdapter;
    private ArrayList<Map<String , String >> hashMapNames, allContacts;
    private EditText editTextSearch;



    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        hashMapNames = new ArrayList<>();
        allContacts = new ArrayList<>();

        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        homeFragmentAdapter = new HomeFragmentAdapter(getContext(), hashMapNames);
        recyclerView.setAdapter(homeFragmentAdapter);

        checkPermissions();

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
        homeFragmentAdapter.notifyDataSetChanged();


    }

    private void getPhoneNumbers() {


        Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (phones.moveToNext()) {

            @SuppressLint("Range") String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            @SuppressLint("Range") String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            // Cleanup the phone number
            phoneNumber = phoneNumber.replaceAll("[()\\s-]+", "");

            HashMap<String, String > hashMap =  new HashMap<>();
            hashMap.put("phone", phoneNumber);
            hashMap.put("name", name);
            hashMapNames.add(hashMap);
            allContacts.add(hashMap);

            homeFragmentAdapter.notifyDataSetChanged();

        }


        phones.close();

    }

    private void checkPermissions(){
        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS);

        if (result == PackageManager.PERMISSION_GRANTED) {
            result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.GET_ACCOUNTS);

            if (result == PackageManager.PERMISSION_GRANTED) {
                getPhoneNumbers();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.GET_ACCOUNTS}, 1);
                FancyToast.makeText(getContext(), "Grant Accounts Permission", FancyToast.LENGTH_LONG, FancyToast.INFO, false).show();
            }

        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 2);
            FancyToast.makeText(getContext(), "Grant Contacts Permission", FancyToast.LENGTH_LONG, FancyToast.INFO, false).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissions();
            } else {
                Toast.makeText(getContext(), "Permission denied to read Contacts", Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == 2) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissions();
            } else {
                Toast.makeText(getContext(), "Permission denied to read Accounts", Toast.LENGTH_SHORT).show();
            }

        }
    }

}

class HomeFragmentAdapter extends RecyclerView.Adapter<HomeFragmentAdapter.ViewHolder>{

    private ArrayList<Map<String, String>> mData;
    private LayoutInflater mInflater;
    private HomeFragmentAdapter.ItemClickListener mClickListener;
    private Map<String, String> message;
    private Context context;

    // data is passed into the constructor
    public HomeFragmentAdapter(Context context, ArrayList<Map<String, String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public HomeFragmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_contact, parent, false);
        return new HomeFragmentAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(HomeFragmentAdapter.ViewHolder holder, int position) {
        message = mData.get(position);

        String name = message.get("name");
        String phone = message.get("phone");

        holder.textViewName.setText(name);
        holder.textViewPic.setText(name.substring(0,1).toUpperCase());
        holder.textViewPhone.setText(phone);

        holder.buttonCall.setOnClickListener(v -> {
            //add calling feature

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
    public void setClickListener(HomeFragmentAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
