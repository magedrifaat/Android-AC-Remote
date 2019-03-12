package com.example.androidac;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.ConsumerIrManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Arrays;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    static int temperature, fan, mode, tnow_units, tnow_tens, tnow_hours, checksum, tsleep_hours, tsleep_tens, twake_hours, twake_tens;
    static boolean power, swing, sleep, wake, tnow_pm, tsleep_pm, twake_pm;

    private static final int FAN_AUTO = 0, FAN_1 = 1, FAN_2 = 2, FAN_3 = 3;
    private static final int MODE_DRY = 0, MODE_HOT = 1, MODE_COLD = 2, MODE_FAN = 3;
    private static final int TEMP_16 = 0;
    private static final int LOW = 552, HIGH = 1683;

    static SharedPreferences sharedPref;
    static SharedPreferences.Editor prefEditor;

    private ConsumerIrManager irManager;

    private static int[] raw = { 8835,4497,552,552,552,552,552,1683,552,552,552,552,552,552,552,552,552,1683,552,1683,552,552,552,1683,552,552,552,1683,552,552,552,1683,552,552,552,1683,552,1683,552,1683,552,1683,552,552,552,1683,552,1683,552,552,552,1683,552,552,552,552,552,1683,552,552,552,552,552,552,552,552,552,1683,552,552,552,1683,552,552,552,1683,552,552,552,552,552,552,552,1683,552,552,552,1683,552,1683,552,1683,552,552,552,1683,552,552,552,552,552,552,552,552,552,552,552,552,552,1683,552,1683,552,552,552,552,552,552,552,552,552,1683,552,552,552,1683,552,552,552,1683,552,552,552,1683,552,1683,552,552,552,552,552,552,552,552,552,552,552,50067 };
                            //       x        0       0       1        0       0       0       0       1        1        0       1        0       1        0       1        0       1        1        1        1
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Configuration config = this.getResources().getConfiguration();

        if (config.smallestScreenWidthDp >= 600) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        prefEditor = sharedPref.edit();

        irManager = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);

        restore_data();
        update_view();

        findViewById(R.id.powerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                power = !power;
                update_view();
                send();
                prefEditor.putBoolean("Power", power);
                prefEditor.apply();
            }
        });

        findViewById(R.id.tempUpButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                temperature = Math.min(temperature + 1, TEMP_16 + 14);
                update_view();
                send();
                prefEditor.putInt("Temperature", temperature);
                prefEditor.apply();
            }
        });

        findViewById(R.id.tempDownButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                temperature = Math.max(temperature - 1, TEMP_16);
                update_view();
                send();
                prefEditor.putInt("Temperature", temperature);
                prefEditor.apply();
            }
        });

        findViewById(R.id.swingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swing = !swing;
                update_view();
                send();
                prefEditor.putBoolean("Swing", swing);
                prefEditor.apply();
            }
        });

        findViewById(R.id.modeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = (mode + 1) % 4;
                if (mode == MODE_FAN && fan == FAN_AUTO) {
                    fan = (fan + 1) % 4;
                }
                update_view();
                send();
                prefEditor.putInt("Mode", mode);
                prefEditor.apply();
            }
        });

        findViewById(R.id.fanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fan = (fan + 1) % 4;
                if (mode == MODE_FAN && fan == FAN_AUTO)
                    fan = (fan + 1) % 4;

                update_view();
                send();
                prefEditor.putInt("Fan", fan);
                prefEditor.apply();
            }
        });

        findViewById(R.id.sleepButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sleep) {
                    sleep = false;
                    tsleep_hours = 10;
                    tsleep_tens = 0;
                    tsleep_pm = true;
                    update_view();
                    send();
                    prefEditor.putBoolean("Sleep", sleep);
                    prefEditor.putInt("SleepHour", tsleep_hours);
                    prefEditor.putInt("SleepTens", tsleep_tens);
                    prefEditor.putBoolean("SleepPM", tsleep_pm);
                    prefEditor.apply();
                }
                else
                {
                    Calendar currentTime = Calendar.getInstance();
                    int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = currentTime.get(Calendar.MINUTE);
                    TimePickerDialog timepicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hourofday, int selectedminute) {
                            sleep = true;
                            tsleep_hours = hourofday % 12;
                            tsleep_pm = hourofday >= 12;
                            tsleep_tens = selectedminute / 10;
                            update_view();
                            send();
                            prefEditor.putBoolean("Sleep", sleep);
                            prefEditor.putInt("SleepHour", tsleep_hours);
                            prefEditor.putInt("SleepTens", tsleep_tens);
                            prefEditor.putBoolean("SleepPM", tsleep_pm);
                            prefEditor.apply();
                        }
                    }, hour, minute, false);
                    timepicker.setTitle("Select Sleep Time");
                    timepicker.show();
                }
            }
        });

        findViewById(R.id.wakeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wake) {
                    wake = false;
                    twake_hours = 6;
                    twake_tens = 0;
                    twake_pm = false;
                    update_view();
                    send();
                    prefEditor.putBoolean("Wake", wake);
                    prefEditor.putInt("WakeHour", twake_hours);
                    prefEditor.putInt("WakeTens", twake_tens);
                    prefEditor.putBoolean("WakePM", twake_pm);
                    prefEditor.apply();
                }
                else
                {
                    Calendar currentTime = Calendar.getInstance();
                    int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = currentTime.get(Calendar.MINUTE);
                    TimePickerDialog timepicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hourofday, int selectedminute) {
                            wake = true;
                            twake_hours = hourofday % 12;
                            twake_pm = hourofday >= 12;
                            twake_tens = selectedminute / 10;
                            update_view();
                            send();
                            prefEditor.putBoolean("Wake", wake);
                            prefEditor.putInt("WakeHour", twake_hours);
                            prefEditor.putInt("WakeTens", twake_tens);
                            prefEditor.putBoolean("WakePM", twake_pm);
                            prefEditor.apply();
                        }
                    }, hour, minute, false);
                    timepicker.setTitle("Select Wake up Time");
                    timepicker.show();
                }
            }
        });

        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send();
            }
        });

    }

    void update_view() {

        ((TextView)findViewById(R.id.powerText)).setText(power? "On": "Off");
        ((TextView)findViewById(R.id.tempratureText)).setText(Integer.toString(temperature + (16 - TEMP_16)));

        String state = "Cold";
        switch (mode)
        {
            case MODE_COLD:
                state = "Cold";
                break;
            case MODE_HOT:
                state = "Hot";
                break;
            case MODE_DRY:
                state = "Dry";
                break;
            case MODE_FAN:
                state = "Fan";
                break;
        }
        ((TextView)findViewById(R.id.modeText)).setText(state);


        ((TextView)findViewById(R.id.swinText)).setText(swing? "On": "Off");

        switch (fan)
        {
            case FAN_AUTO:
                findViewById(R.id.fanImage).setVisibility(View.INVISIBLE);
                findViewById(R.id.autoText).setVisibility(View.VISIBLE);
                break;
            case FAN_1:
                findViewById(R.id.fanImage).setVisibility(View.VISIBLE);
                findViewById(R.id.autoText).setVisibility(View.INVISIBLE);
                ((ImageView)findViewById(R.id.fanImage)).setImageResource(R.drawable.fan1);
                break;
            case FAN_2:
                findViewById(R.id.fanImage).setVisibility(View.VISIBLE);
                findViewById(R.id.autoText).setVisibility(View.INVISIBLE);
                ((ImageView)findViewById(R.id.fanImage)).setImageResource(R.drawable.fan2);
                break;
            case FAN_3:
                findViewById(R.id.fanImage).setVisibility(View.VISIBLE);
                findViewById(R.id.autoText).setVisibility(View.INVISIBLE);
                ((ImageView)findViewById(R.id.fanImage)).setImageResource(R.drawable.fan3);
                break;
        }

        if (sleep) {
            TextView sleepText = findViewById(R.id.sleepText);
            sleepText.setVisibility(View.VISIBLE);
            sleepText.setText("Sleep at " + Integer.toString(tsleep_hours) + ":" + Integer.toString(tsleep_tens * 10) + " " + (tsleep_pm? "PM": "AM"));
        }
        else {
            findViewById(R.id.sleepText).setVisibility(View.INVISIBLE);
        }

        if (wake) {
            TextView wakeText = findViewById(R.id.wakeText);
            wakeText.setVisibility(View.VISIBLE);
            wakeText.setText("Wake up at " + Integer.toString(twake_hours) + ":" + Integer.toString(twake_tens * 10) + " " + (twake_pm? "PM": "AM"));
        }
        else {
            findViewById(R.id.wakeText).setVisibility(View.INVISIBLE);
        }
    }

    void restore_data() {
        power = sharedPref.getBoolean("Power", false);
        temperature = sharedPref.getInt("Temperature", TEMP_16 + 8);
        fan = sharedPref.getInt("Fan", FAN_1);
        mode = sharedPref.getInt("Mode", MODE_COLD);
        swing = sharedPref.getBoolean("Swing", false);
        sleep = sharedPref.getBoolean("Sleep", false);
        wake = sharedPref.getBoolean("Wake", false);
        tsleep_hours = sharedPref.getInt("SleepHour", 10);
        tsleep_tens = sharedPref.getInt("SleepTens", 0);
        tsleep_pm = sharedPref.getBoolean("SleepPM", true);
        twake_hours = sharedPref.getInt("WakeHour", 6);
        twake_tens = sharedPref.getInt("WakeTens", 0);
        twake_pm = sharedPref.getBoolean("WakePM", false);
    }

    void send() {
        set_time();
        set_checksum();
        update_raw();
        Log.d("raw", Arrays.toString(raw));
        if (irManager.hasIrEmitter()) {
            irManager.transmit(38000, raw);
        }
        else {
            Log.e("ir", "No ir emmiter detected");
        }
    }

    static void set_checksum() {
        checksum = (tsleep_hours & 0b1111) + (tsleep_pm? 8: 0) + (tsleep_tens & 0b111)
                + (twake_hours & 0b1111) + (twake_pm? 8: 0) + (twake_tens & 0b111)
                + (tnow_hours & 0b1111) + (tnow_pm? 8: 0) + (tnow_tens & 0b111)
                + (wake? 4: 0) + (sleep? 2: 0) + (power? 1: 0) + (tnow_units & 0b1111)
                + (swing? 2: 0) + (temperature & 0b1111) + (fan & 0b11) * 4 + (mode & 0b11);
        checksum = checksum % 16;
    }

    static void set_time() {
        Calendar currentTime = Calendar.getInstance();
        tnow_hours = currentTime.get(Calendar.HOUR);
        tnow_pm = currentTime.get(Calendar.AM_PM) == Calendar.PM;
        tnow_tens = currentTime.get(Calendar.MINUTE) / 10;
        tnow_units = currentTime.get(Calendar.MINUTE) % 10;
    }

    static void update_raw() {
        raw[35] = (checksum & 0b0001) > 0 ? HIGH : LOW;
        raw[37] = (checksum & 0b0010) > 0 ? HIGH : LOW;
        raw[39] = (checksum & 0b0100) > 0 ? HIGH : LOW;
        raw[41] = (checksum & 0b1000) > 0 ? HIGH : LOW;
        raw[43] = (mode & 0b01) > 0 ? HIGH : LOW;
        raw[45] = (mode & 0b10) > 0 ? HIGH : LOW;
        raw[47] = (fan & 0b01) > 0 ? HIGH : LOW;
        raw[49] = (fan & 0b10) > 0 ? HIGH : LOW;
        raw[51] = (temperature & 0b0001) > 0 ? HIGH : LOW;
        raw[53] = (temperature & 0b0010) > 0 ? HIGH : LOW;
        raw[55] = (temperature & 0b0100) > 0 ? HIGH : LOW;
        raw[57] = (temperature & 0b1000) > 0 ? HIGH : LOW;
        raw[61] = swing ? HIGH : LOW;
        raw[67] = (tnow_units & 0b0001) > 0 ? HIGH : LOW;
        raw[69] = (tnow_units & 0b0010) > 0 ? HIGH : LOW;
        raw[71] = (tnow_units & 0b0100) > 0 ? HIGH : LOW;
        raw[73] = (tnow_units & 0b1000) > 0 ? HIGH : LOW;
        raw[75] = power ? HIGH : LOW;
        raw[77] = sleep ? HIGH : LOW;
        raw[79] = wake ? HIGH : LOW;
        raw[83] = (tnow_tens & 0b001) > 0 ? HIGH : LOW;
        raw[85] = (tnow_tens & 0b010) > 0 ? HIGH : LOW;
        raw[87] = (tnow_tens & 0b100) > 0 ? HIGH : LOW;
        raw[89] = tnow_pm ? HIGH : LOW;
        raw[91] = (tnow_hours & 0b0001) > 0 ? HIGH : LOW;
        raw[93] = (tnow_hours & 0b0010) > 0 ? HIGH : LOW;
        raw[95] = (tnow_hours & 0b0100) > 0 ? HIGH : LOW;
        raw[97] = (tnow_hours & 0b1000) > 0 ? HIGH : LOW;
        raw[99] = (twake_tens & 0b001) > 0 ? HIGH : LOW;
        raw[101] = (twake_tens & 0b010) > 0 ? HIGH : LOW;
        raw[103] = (twake_tens & 0b100) > 0 ? HIGH : LOW;
        raw[105] = twake_pm ? HIGH : LOW;
        raw[107] = (twake_hours & 0b0001) > 0 ? HIGH : LOW;
        raw[109] = (twake_hours & 0b0010) > 0 ? HIGH : LOW;
        raw[111] = (twake_hours & 0b0100) > 0 ? HIGH : LOW;
        raw[113] = (twake_hours & 0b1000) > 0 ? HIGH : LOW;
        raw[115] = (tsleep_tens & 0b001) > 0 ? HIGH : LOW;
        raw[117] = (tsleep_tens & 0b010) > 0 ? HIGH : LOW;
        raw[119] = (tsleep_tens & 0b100) > 0 ? HIGH : LOW;
        raw[121] = tsleep_pm ? HIGH : LOW;
        raw[123] = (tsleep_hours & 0b0001) > 0 ? HIGH : LOW;
        raw[125] = (tsleep_hours & 0b0010) > 0 ? HIGH : LOW;
        raw[127] = (tsleep_hours & 0b0100) > 0 ? HIGH : LOW;
        raw[129] = (tsleep_hours & 0b1000) > 0 ? HIGH : LOW;
    }
}
