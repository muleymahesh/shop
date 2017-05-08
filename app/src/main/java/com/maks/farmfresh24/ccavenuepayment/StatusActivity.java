package com.maks.farmfresh24.ccavenuepayment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.maks.farmfresh24.PlaceOrderActivity;
import com.maks.farmfresh24.R;
import com.maks.farmfresh24.dbutils.SQLiteUtil;
import com.maks.farmfresh24.utils.AppPreferences;
import com.maks.farmfresh24.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StatusActivity extends Activity {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_status);

        Intent mainIntent = getIntent();
        TextView tv4 = (TextView) findViewById(R.id.textView1);
        tv4.setText(mainIntent.getStringExtra("transStatus"));
    }

    public void showToast(String msg) {
        Toast.makeText(this, "Toast: " + msg, Toast.LENGTH_LONG).show();
    }

    private void placeOrder(String OrderMode) {
        new HttpAsyncTask().execute(Constants.WS_URL, PlaceOrderActivity.req);
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(StatusActivity.this);
            pd.setMessage("Loading...");
            pd.show();
        }


        @Override
        protected String doInBackground(String... ulr) {
            Response response = null;

            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody body = RequestBody.create(JSON, ulr[1]);
            Request request = new Request.Builder()
                    .url(ulr[0])
                    .post(body)
                    .build();

            try {
                response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            if (s != null) {
                Log.e("response->", s);
                try {
                    JSONObject json = new JSONObject(s);
                    if (json.getString("result").equalsIgnoreCase("success")) {
                        new SQLiteUtil().emptyCart(StatusActivity.this);
//                        if (json.optInt("orderCount") > 1) {
//                            pref.setFirstOrderStatus(false);
//                        } else {
//                            pref.setFirstOrderStatus(true);
//                        }
                        AppPreferences pref = new AppPreferences(StatusActivity.this);
                        pref.setOrderCount(json.optInt("orderCount"));
                        //int baseamount = Integer.parseInt(txtAmt.getText().toString());
                        //pref.setFirstOrderAmount("" + (baseamount));
                        AlertDialog.Builder alert = new AlertDialog.Builder(StatusActivity.this);
                        alert.setMessage("Your order placed successfully");
                        alert.setTitle("Thank you !");
                        alert.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                StatusActivity.this.finish();

                            }
                        });
                        alert.show();

                        new SQLiteUtil().emptyCart(StatusActivity.this);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }        // onPostExecute displays the results of the AsyncTask.

    }

} 