package com.fit.steps;

import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.apimanager.GoogleApiManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;

import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

import actify.achmea.com.fitstepcount.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FitHistoryActivity extends AppCompatActivity {

    public static final String TAG = FitHistoryActivity.class.getName();
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    @BindView(R.id.stepCount)
    TextView stepCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fit_step_count);
        ButterKnife.bind(this);


        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        //Way 1
        FitApplication.getGoogleApiManager().setConnectionListener(new GoogleApiManager.ConnectionListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Log.d(TAG, "Connection Failed");
                if (!authInProgress) {
                    try {
                        authInProgress = true;
                        connectionResult.startResolutionForResult(FitHistoryActivity.this, REQUEST_OAUTH);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e("GoogleFit", "sendingIntentException " + e.getMessage());
                    }
                } else {
                    Log.e("GoogleFit", "authInProgress");
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.d(TAG, "Connection Suspended");
            }

            @Override
            public void onConnected(Bundle bundle) {
                Log.d(TAG, "Connected");
                new TodayStepCount().execute();

            }

        });

        //Way 2
//        if(ActifyApplication.getGoogleApiManager().isConnected()){
//            GoogleApiClient googleApiClient = ActifyApplication.getGoogleApiManager().getmGoogleApiClient();
//
//            googleApiClient.connect();
//        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                if (!FitApplication.getGoogleApiManager().getmGoogleApiClient().isConnecting() && !FitApplication.getGoogleApiManager().getmGoogleApiClient().isConnected()) {
                    FitApplication.getGoogleApiManager().getmGoogleApiClient().connect();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.e("GoogleFit", "RESULT_CANCELED");
            }
        } else {
            Log.e("GoogleFit", "requestCode NOT request_oauth");
        }
    }

    private class TodayStepCount extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            getTodayStepsCount();
            return null;
        }
    }

    private void getTodayStepsCount() {
        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal(FitApplication.getGoogleApiManager().getmGoogleApiClient(),
                DataType.TYPE_STEP_COUNT_DELTA).await(1, TimeUnit.MINUTES);
        showDataSet(result.getTotal());
    }


    String strStep = "";

    private void showDataSet(DataSet dataSet) {
        Log.e("History", "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.e("History", "Data point:");
            Log.e("History", "\tType: " + dp.getDataType().getName());
            String startDate = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
            Log.e("History", "\tStart: " + startDate);
            String endDate = dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
            Log.e("History", "\tEnd: " + endDate);
            for (Field field : dp.getDataType().getFields()) {
                Log.e("History", "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));

                strStep += "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field) + "\nStart:" +
                        startDate + "\n\tEnd: " + endDate + "\n\n";
                ;
                final String finalStrStep = strStep;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stepCount.setText(strStep);
                    }
                });

            }
        }
    }
}
