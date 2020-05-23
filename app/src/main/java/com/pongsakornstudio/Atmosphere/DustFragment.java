package com.pongsakornstudio.Atmosphere;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekn.gruzer.gaugelibrary.ArcGauge;
import com.ekn.gruzer.gaugelibrary.Range;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DustFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DustFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("pm25");
    private DatabaseReference myRef_loc = database.getReference();
    public ArcGauge gauge;
    public Range range;
    private String status="";
    private int colorGauge ;

    private int Cmax = 0 ,Cmin = 0 ,Imax = 0 ,Imin = 0;
    private TextView aqi_tv,aqi_status;
    private ImageView aqi_img;
    private  NotificationManagerCompat notificationManager;
    private String CHANNEL_ID = "MY_NOTIFICATION";
    private NotificationCompat.Builder builder;
    boolean checkNotify;
    String levelofdust;
    private Chip chip_latlng;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DustFragment() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DustFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DustFragment newInstance(String param1, String param2) {
        DustFragment fragment = new DustFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dust, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gauge = getView().findViewById(R.id.Gauge);
        range = new Range();

        aqi_tv = getView().findViewById(R.id.aqi);
        aqi_status = getView().findViewById(R.id.aqi_status);
        aqi_img = getView().findViewById(R.id.aqi_icon);

        range.setTo(200.0);
        range.setFrom(0.0);

        chip_latlng = getView().findViewById(R.id.chip_latlng);

        builder = new NotificationCompat.Builder(view.getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bad)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notify_content))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.notify_content)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        notificationManager = NotificationManagerCompat.from(view.getContext());


        setDustValue();
        setChipLatLng();
    }

    public void setDustValue() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getView().getContext());
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                float value = dataSnapshot.getValue(Float.class);
                Resources res = getResources();
                Drawable aqi_icon;
                int aqi = 0;
                levelofdust = sharedPreferences.getString("level", "99999999");
                checkNotify = sharedPreferences.getBoolean("notify",false);
                final int level = Integer.parseInt(levelofdust);
                if(value < 26){
                    Cmax = 25;
                    Cmin = 0;
                    Imax = 25;
                    Imin = 0;
                    colorGauge = ResourcesCompat.getColor(res, R.color.very_good, null);
                    aqi_icon = ResourcesCompat.getDrawable(res, R.drawable.ic_verygood, null);
                    status = getString(R.string.very_good);
                }else if(value < 51){
                    Cmax = 37;
                    Cmin = 26;
                    Imax = 50;
                    Imin = 26;
                    colorGauge = ResourcesCompat.getColor(res, R.color.good, null);
                    aqi_icon = ResourcesCompat.getDrawable(res, R.drawable.ic_good, null);
                    status = getString(R.string.good);
                }else if(value < 101){
                    Cmax = 50;
                    Cmin = 38;
                    Imax = 100;
                    Imin = 51;
                    colorGauge = ResourcesCompat.getColor(res, R.color.normal, null);
                    aqi_icon = ResourcesCompat.getDrawable(res, R.drawable.ic_normal, null);
                    status = getString(R.string.moderate);
                }else if(value < 201){
                    Cmax = 90;
                    Cmin = 51;
                    Imax = 200;
                    Imin = 101;
                    colorGauge = ResourcesCompat.getColor(res, R.color.bad, null);
                    aqi_icon = ResourcesCompat.getDrawable(res, R.drawable.ic_bad, null);
                    status = getString(R.string.unhealthy_some);
                }else {
                    Cmax = 25;
                    Cmin = 0;
                    Imax = 25;
                    Imin = 0;
                    colorGauge = ResourcesCompat.getColor(res, R.color.very_bad, null);
                    aqi_icon = ResourcesCompat.getDrawable(res, R.drawable.ic_verybad, null);
                    status = getString(R.string.unhealthy);
                }

                if(checkNotify&&value >= level){
                    notificationManager.notify(100, builder.build());
                }

                aqi = calAqi(value,Cmax,Cmin,Imax,Imin);
                range.setColor(colorGauge);
                gauge.addRange(range);
                gauge.setValue(Double.parseDouble(String.format("%.2f", value)));
                aqi_img.setImageDrawable(aqi_icon);
                aqi_tv.setText(aqi+" AQI");
                aqi_status.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException());
            }

        });
    }

    private void setChipLatLng(){
        myRef_loc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue();
                Object latitude = data.get("lat");
                Object longitude = data.get("long");
                chip_latlng.setText(latitude+","+longitude);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    private int calAqi(float dust, int Cmax, int Cmin, int Imax, int Imin){
        return (int) (((Imax-Imin)/(Cmax-Cmin))*(dust-Cmin)+Imin);
    }

}
