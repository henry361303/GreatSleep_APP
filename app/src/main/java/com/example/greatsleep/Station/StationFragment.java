package com.example.greatsleep.Station;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.greatsleep.R;
import com.example.greatsleep.SettingActivity;
import com.example.greatsleep.SettingMainActivity;
import com.example.greatsleep.TypeFaceProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;

import dmax.dialog.SpotsDialog;

public class StationFragment extends Fragment implements View.OnClickListener{
    private View mainView;
    private Spinner startlines;
    private Spinner endlines;
    private String startStation;
    private String endStation;
    private String linecolor;
    private int stopt,runt,totalt;
    Button blue,brown,red,green,orange,yellow, confirm;
    int time;
    Button time1,time2,time3;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Intent intent;
    PendingIntent pendingIntentSet;
    AlarmManager alarm;
    private ArrayAdapter<CharSequence> BL;
    private ArrayAdapter<CharSequence> BR;
    private ArrayAdapter<CharSequence> Red;
    private ArrayAdapter<CharSequence> Gre;
    private ArrayAdapter<CharSequence> Org;
    private ArrayAdapter<CharSequence> Yel;
    Calendar calendar;
    private Toolbar toolbar;
    TextView information;
    private AlertDialog dialog;

    private JSONArray jsonArray;
    private JSONObject jsonObject,stStation,edStation,station_orderByDesc,station_orderBy;
    private String url = "http://163.13.201.92/STtest.php";
    Typeface typeface;
    TextView start,end,target,minutes;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        typeface= TypeFaceProvider.getTypeFace(getActivity(),"introtype.ttf");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView=inflater.inflate(R.layout.fragment_station, container, false);
        toolbar=mainView.findViewById(R.id.toolbar_station);
        preferences = getActivity().getSharedPreferences("station", Context.MODE_PRIVATE);
        editor = preferences.edit();
        start=mainView.findViewById(R.id.start);
        end=mainView.findViewById(R.id.end);
        target=mainView.findViewById(R.id.target);
        minutes=mainView.findViewById(R.id.minutes);

        start.setTypeface(typeface);
        end.setTypeface(typeface);
        target.setTypeface(typeface);
        minutes.setTypeface(typeface);

        toolbar.inflateMenu(R.menu.menu_example);
        information=mainView.findViewById(R.id.station_information);

        intent = new Intent(getActivity(), StationAlarm.class);
        pendingIntentSet= PendingIntent.getActivity(getActivity(), 2, intent, 0);
        alarm= (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);

        blue=mainView.findViewById(R.id.s1);
        brown=mainView.findViewById(R.id.s2);
        red=mainView.findViewById(R.id.s3);
        green=mainView.findViewById(R.id.s4);
        orange=mainView.findViewById(R.id.s5);
        yellow=mainView.findViewById(R.id.s6);
        confirm=mainView.findViewById(R.id.confirm);

        confirm.setTypeface(typeface);

        blue.setOnClickListener(this);
        brown.setOnClickListener(this);
        red.setOnClickListener(this);
        green.setOnClickListener(this);
        orange.setOnClickListener(this);
        yellow.setOnClickListener(this);
        confirm.setOnClickListener(this);

        blue.getBackground().setAlpha(255);
        brown.getBackground().setAlpha(160);
        red.getBackground().setAlpha(160);
        green.getBackground().setAlpha(160);
        orange.getBackground().setAlpha(160);
        yellow.getBackground().setAlpha(160);

        time1=(Button)mainView.findViewById(R.id.time1);
        time2=(Button)mainView.findViewById(R.id.time2);
        time3=(Button)mainView.findViewById(R.id.time3);

        time1.setOnClickListener(this);
        time2.setOnClickListener(this);
        time3.setOnClickListener(this);

        calendar =Calendar.getInstance();
        //??????????????????
        SubsamplingScaleImageView imageView = mainView.findViewById(R.id.imageView);
        imageView.setImage(ImageSource.resource(R.drawable.metrotaipeimap));
        imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
        imageView.setScaleAndCenter(0.18f, new PointF(2500, 4500));
        imageView.setMaxScale(0.8f);
        imageView.setMinScale(0.2f);
        imageView.bringToFront();

        startStation="??????";
        endStation="??????";
        //???????????????
        StationChoose();

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_setting:
                        Intent intent=new Intent(getActivity(), SettingMainActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_cancel:
                        AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
                        View view = getLayoutInflater().inflate(R.layout.alarm_dialog_design,null);//??????View
                        Button d_confirm=view.findViewById(R.id.alarm_dialog_confirm);
                        Button d_cancel=view.findViewById(R.id.alarm_dialog_cancel);
                        TextView dialog_message=view.findViewById(R.id.alarm_dialog_message);
                        ab.setView(view);
                        AlertDialog dialog=ab.create();
                        dialog_message.setText("???????????????????????????????????????????");
                        d_confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                editor.putBoolean("is_alarm",false);
                                editor.putString("name",null);
                                information.setText(null);
                                editor.apply();

                                toolbar.getMenu().findItem(R.id.action_cancel).setVisible(false);
                                Toast.makeText(getActivity(), "?????????", Toast.LENGTH_SHORT).show();
                                alarm.cancel(pendingIntentSet);
                                dialog.dismiss();
                            }
                        });
                        d_cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        break;
                }
                return false;
            }
        });
        return mainView;
    }

    private void StationChoose() {
        //0:?????? 1:?????? 2:?????? 3:?????? 4:?????? 5:??????
        startlines=(Spinner)mainView.findViewById(R.id.start_station);
        endlines=(Spinner)mainView.findViewById(R.id.end_station);

        BL=ArrayAdapter.createFromResource(getActivity().getApplicationContext(),R.array.station_bl, R.layout.spinner_style);
        BR=ArrayAdapter.createFromResource(getActivity().getApplicationContext(),R.array.station_br, R.layout.spinner_style);
        Red=ArrayAdapter.createFromResource(getActivity().getApplicationContext(),R.array.station_red, R.layout.spinner_style);
        Gre=ArrayAdapter.createFromResource(getActivity().getApplicationContext(),R.array.station_green, R.layout.spinner_style);
        Org=ArrayAdapter.createFromResource(getActivity().getApplicationContext(),R.array.station_o, R.layout.spinner_style);
        Yel=ArrayAdapter.createFromResource(getActivity().getApplicationContext(),R.array.station_y, R.layout.spinner_style);


        BL.setDropDownViewResource(R.layout.spinner_item);
        BR.setDropDownViewResource(R.layout.spinner_item);
        Red.setDropDownViewResource(R.layout.spinner_item);
        Gre.setDropDownViewResource(R.layout.spinner_item);
        Org.setDropDownViewResource(R.layout.spinner_item);
        Yel.setDropDownViewResource(R.layout.spinner_item);

        //Adapter??????Spinner??????
        stationSelect(BL);
        linecolor="BL";
    }


    //????????????
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.s1:
                blue.getBackground().setAlpha(255);
                brown.getBackground().setAlpha(160);
                red.getBackground().setAlpha(160);
                green.getBackground().setAlpha(160);
                orange.getBackground().setAlpha(160);
                yellow.getBackground().setAlpha(160);
                stationSelect(BL);
                linecolor="BL";
                break;
            case R.id.s2:
                blue.getBackground().setAlpha(160);
                brown.getBackground().setAlpha(255);
                red.getBackground().setAlpha(160);
                green.getBackground().setAlpha(160);
                orange.getBackground().setAlpha(160);
                yellow.getBackground().setAlpha(160);
                stationSelect(BR);
                linecolor="BR";
                break;
            case R.id.s3:
                blue.getBackground().setAlpha(160);
                brown.getBackground().setAlpha(160);
                red.getBackground().setAlpha(255);
                green.getBackground().setAlpha(160);
                orange.getBackground().setAlpha(160);
                yellow.getBackground().setAlpha(160);
                stationSelect(Red);
                linecolor="R";
                break;
            case R.id.s4:
                blue.getBackground().setAlpha(160);
                brown.getBackground().setAlpha(160);
                red.getBackground().setAlpha(160);
                green.getBackground().setAlpha(255);
                orange.getBackground().setAlpha(160);
                yellow.getBackground().setAlpha(160);
                stationSelect(Gre);
                linecolor="G";
                break;
            case R.id.s5:
                blue.getBackground().setAlpha(160);
                brown.getBackground().setAlpha(160);
                red.getBackground().setAlpha(160);
                green.getBackground().setAlpha(160);
                orange.getBackground().setAlpha(255);
                yellow.getBackground().setAlpha(160);
                stationSelect(Org);
                linecolor="O";
                break;
            case R.id.s6:
                blue.getBackground().setAlpha(160);
                brown.getBackground().setAlpha(160);
                red.getBackground().setAlpha(160);
                green.getBackground().setAlpha(160);
                orange.getBackground().setAlpha(160);
                yellow.getBackground().setAlpha(255);
                stationSelect(Yel);
                linecolor="Y";
                break;
            case R.id.time1:
                time1.setBackgroundResource(R.drawable.station_time_selected);
                time2.setBackgroundResource(R.drawable.station_time_no_selected);
                time3.setBackgroundResource(R.drawable.station_time_no_selected);
                time=90;
                break;
            case R.id.time2:
                time1.setBackgroundResource(R.drawable.station_time_no_selected);
                time2.setBackgroundResource(R.drawable.station_time_selected);
                time3.setBackgroundResource(R.drawable.station_time_no_selected);
                time=150;
                break;
            case R.id.time3:
                time1.setBackgroundResource(R.drawable.station_time_no_selected);
                time2.setBackgroundResource(R.drawable.station_time_no_selected);
                time3.setBackgroundResource(R.drawable.station_time_selected);
                time=210;
                break;
            case R.id.confirm:
                if(startStation.equals(endStation))
                    Toast.makeText(getActivity(), "?????????????????????", Toast.LENGTH_SHORT).show();
                else if(diff())
                    Toast.makeText(getActivity(), "??????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                else if(preferences.getBoolean("is_alarm",false)){
                    Toast.makeText(getActivity(), "???????????????????????????????????????", Toast.LENGTH_SHORT).show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View view = getLayoutInflater().inflate(R.layout.dialog_design,null);//??????View
                    Button d_confirm=view.findViewById(R.id.dialog_confirm);
                    Button d_cancel=view.findViewById(R.id.dialog_cancel);
                    TextView dialog_message=view.findViewById(R.id.dialog_message);
                    builder.setView(view);
                    AlertDialog dialog=builder.create();
                    dialog_message.setText("????????????????????????????????????????????????????????????????????????");
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            stopt = 0;
                            runt = 0;
                            totalt = 0;
                        }
                    });
                    d_confirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Async().execute();
                            dialog.dismiss();
                        }
                    });
                    d_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            stopt = 0;
                            runt = 0;
                            totalt = 0;
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
                break;
        }
    }

    public void stationSelect(SpinnerAdapter line){

        startlines.setAdapter(line);
        startlines.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        startStation=startlines.getSelectedItem().toString();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        startStation = startlines.getSelectedItem().toString();
                    }
                });
        endlines.setAdapter(line);
        endlines.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        endStation=endlines.getSelectedItem().toString();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        endStation=endlines.getSelectedItem().toString();
                    }
                });
    }
    @Override
    public void onResume() {
        super.onResume();
        time=90;
        information.setText(preferences.getString("name",""));
        if(preferences.getBoolean("is_alarm",false)){
            toolbar.getMenu().findItem(R.id.action_cancel).setVisible(true);
        }
        else {
            toolbar.getMenu().findItem(R.id.action_cancel).setVisible(false);
        }
    }

    class Async extends AsyncTask<Void, Void, Void> {
        String error = "";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new SpotsDialog(getActivity(),"?????????...",R.style.Custom);
            dialog.setCancelable(false);
            dialog.show();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            int startid=0,endid=0;
            try {
                //??????????????????????????????????????????
                if(startStation.equals("??????"))
                    startid=1;
                else if(startStation.equals("?????????"))
                    startid=23;
                else if(startStation.equals("??????"))
                    startid=46;
                else if(startStation.equals("?????????"))
                    startid=64;
                else if(startStation.equals("??????"))
                    startid=89;
                else if(startStation.equals("?????????")&&linecolor.equals("Y"))
                    startid=115;
                else{
                    jsonArray = StationSQL.DB1("SELECT * FROM signature WHERE "+"("+"fsnt = "+"\'"+startStation+"\'" + " AND fsid LIKE "+"\'"+linecolor+"%"+"\'"+")",url);
                    for(int i =0;i<jsonArray.length();i++)
                    {
                        stStation = jsonArray.getJSONObject(i);
                        startid = stStation.getInt("signatureid");
                    }
                }
                //?????????????????????????????????????????? ?????????????????????
                if(endStation.equals("??????"))
                    endid=1;
                else if(endStation.equals("?????????"))
                    endid=23;
                else if(endStation.equals("??????"))
                    endid=46;
                else if(endStation.equals("?????????"))
                    endid=64;
                else if(endStation.equals("??????"))
                    endid=89;
                else if(endStation.equals("?????????")&&linecolor.equals("Y"))
                    endid=115;
                else{
                    jsonArray = StationSQL.DB1("SELECT * FROM signature WHERE"+"("+"fsnt ="+"\'"+endStation+"\'" + " AND fsid LIKE "+"\'"+linecolor+"%"+"\'"+")",url);
                    for(int i=0;i<jsonArray.length();i++)
                    {
                        edStation = jsonArray.getJSONObject(i);
                        endid = edStation.getInt("signatureid")+1;
                    }
                }
                //???  while?????? if???????????????????????????
                if((startid-endid)>=0){
                    //????????????" 9stopt=0,8rout=0,totalt=0
                    jsonArray = StationSQL.DB1("SELECT * FROM signature WHERE "+"("
                            +endid+" <= signatureid AND signatureid <= "+startid+")"
                            +"ORDER BY signatureid DESC",url);
                    for(int i =0;i<jsonArray.length();i++)
                    {
                        station_orderByDesc = jsonArray.getJSONObject(i);
                        if(station_orderByDesc.getInt("signatureid")==endid)
                        {
                            runt = runt+station_orderByDesc.getInt("runt");
                            break;
                        }
                        runt = runt+station_orderByDesc.getInt("runt");
                        stopt = stopt+station_orderByDesc.getInt("stopt");
                    }
                    totalt=runt+stopt;
                }
                //??? for?????? ??? ???
                else {
                    //????????????
                    jsonArray = StationSQL.DB1("SELECT * FROM signature WHERE "+"("
                            +endid+" >= signatureid and signatureid >= "+startid+")"
                            +"ORDER BY signatureid",url);

                    //????????????
                    if(startid==endid-1){
                        station_orderBy= jsonArray.getJSONObject(0);//????????????
                        runt=runt+station_orderBy.getInt("runt");
                    }
                    else{
                        //???????????????
                        if(startStation.equals("??????")||startStation.equals("?????????")||startStation.equals("??????")||
                                startStation.equals("?????????")||startStation.equals("??????")||(startStation.equals("?????????")&&linecolor.equals("Y"))){
                            station_orderBy= jsonArray.getJSONObject(0);
                            runt=station_orderBy.getInt("runt");
                            startid=startid+1;
                            for(int i =1;i<jsonArray.length();i++)
                            {
                                station_orderBy= jsonArray.getJSONObject(i);
                                if(startid==endid-1)
                                {
                                    runt = runt+station_orderBy.getInt("runt");
                                    stopt=stopt+station_orderBy.getInt("stopt");
                                    break;
                                }
                                runt = runt+station_orderBy.getInt("runt");
                                stopt=stopt+station_orderBy.getInt("stopt");
                                startid++;
                            }
                        }
                        //??????????????????
                        else{
                            station_orderBy= jsonArray.getJSONObject(1);
                            runt = station_orderBy.getInt("runt");
                            startid=startid+1;
                            for(int i =2;i<jsonArray.length();i++)
                            {
                                station_orderBy= jsonArray.getJSONObject(i);
                                if(startid==endid-1){
                                    break;
                                }
                                runt = runt+station_orderBy.getInt("runt");
                                stopt=stopt+station_orderBy.getInt("stopt");
                                startid++;
                            }
                        }
                    }
                    totalt=runt+stopt;
                }
                Log.v("Second", "?????????: "+totalt+"Runt"+runt+"Stopt"+stopt);
            } catch (Exception e) {
                error = e.toString();
            }
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //????????????
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int s = calendar.get(Calendar.SECOND);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, totalt-time);
            //??????????????????
            if(totalt-time<=time){
                return null;
            }
            else {
                long triggerAtMillis = calendar.getTimeInMillis();
                alarm.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntentSet);
                editor.putBoolean("is_alarm", true);
                editor.putString("name", startStation + " To \n" + endStation);
                editor.apply();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void unused) {
            if(!error.equals("")){
                Toast.makeText(getActivity(),  "??????????????????????????????!", Toast.LENGTH_SHORT).show();
            }
            else if(totalt-time<=180){
                Toast.makeText(getActivity(),  "???????????????????????????????????????", Toast.LENGTH_SHORT).show();
            }
            else{
                int left_minute = (totalt-time) / 60;
                String left_time = "??????" + left_minute + "??????????????????";
                Toast.makeText(getActivity(), left_time + "", Toast.LENGTH_SHORT).show();
                information.setText(startStation + " To \n" + endStation);
                toolbar.getMenu().findItem(R.id.action_cancel).setVisible(true);
            }
            dialog.dismiss();
            super.onPostExecute(unused);
        }
    }

    private boolean diff(){
        Boolean d=false;
        if(startStation.equals("?????????")||startStation.equals("??????")||startStation.equals("??????")||startStation.equals("?????????")
                ||startStation.equals("?????????")||startStation.equals("??????")||startStation.equals("??????")||startStation.equals("??????")
                ||startStation.equals("??????"))
        {
            if(endStation.equals("????????????")||endStation.equals("????????????")||endStation.equals("????????????")||endStation.equals("????????????")
                    ||endStation.equals("??????"))
            {
                d= true;
            }
        }
        else if(startStation.equals("????????????")||startStation.equals("????????????")||startStation.equals("????????????")||startStation.equals("????????????")
                ||startStation.equals("??????")){
            if(endStation.equals("?????????")||endStation.equals("??????")||endStation.equals("??????")||endStation.equals("?????????")
                    ||endStation.equals("?????????")||endStation.equals("??????")||endStation.equals("??????")||endStation.equals("??????")
                    ||endStation.equals("??????")){
                d= true;
            }
        }
        return d;
    }

}