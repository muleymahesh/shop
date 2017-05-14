package com.maks.farmfresh24;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.maks.farmfresh24.ccavenuepayment.Utility.AvenuesParams;
import com.maks.farmfresh24.ccavenuepayment.Utility.ServiceUtility;
import com.maks.farmfresh24.ccavenuepayment.WebViewActivity;
import com.maks.farmfresh24.dbutils.SQLiteUtil;
import com.maks.farmfresh24.model.Address;
import com.maks.farmfresh24.model.CartList;
import com.maks.farmfresh24.model.DiscountItem;
import com.maks.farmfresh24.model.ShoppingCart;
import com.maks.farmfresh24.utils.AppPreferences;
import com.maks.farmfresh24.utils.Constants;
import com.maks.farmfresh24.utils.TypefaceSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlaceOrderActivity extends AppCompatActivity {

    public static final String TAG = PlaceOrderActivity.class.getSimpleName();
    private static final int PAYMENT_RECEIVED = 1;

    private Toolbar toolbar;
    private TextView txtDate;
    private TextView txtAmt;
    private TextView txtDeliveryCharges, txtAmountToPay;
    Button orderBtn, btnAddr;
    DiscountItem discountItem;
    Spinner spnTimeSlot;
    //    ,spnPaymentType;
    ArrayList<ShoppingCart> list;
    String amount, selected_date;
    private boolean isOnline = false;
    float discountedAmount = 0;

    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;

    private SimpleDateFormat dateFormatter;
    private RadioGroup radioGroupPaymentMode;
    public static String req;


    ArrayList<Address> addresses = new ArrayList<>();

    /* Payment gateway*/
    String merchantId = "129307";
    String accessCode = "AVCT70ED45CC50TCCC";
    String redirectUrl = "http://farmfresh24.in/farmfresh24/admin/ccavenue/ccavResponseHandler.php";
    String cancelUrl = "http://farmfresh24.in/farmfresh24/admin/ccavenue/ccavResponseHandler.php";
    String rsaKeyUrl = "http://farmfresh24.in/farmfresh24/admin/ccavenue/GetRSA.php";

    private ProgressDialog pDialog;

    String selectedPaymentOption;
    AppPreferences pref;
    StringBuilder discountedApplied = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);
        initToolbar();
        initView();
        pref = new AppPreferences(PlaceOrderActivity.this);
        getData();

        setFonts();

        setListeners();

        /*//TODO Must write this code if integrating One Tap payments
        OnetapCallback.setOneTapCallback(PlaceOrderActivity.this);
        //TODO Must write below code in your activity to set up initial context for PayU
        Payu.setInstance(this);
        // lets tell the people what version of sdk we are using
        PayUSdkDetails payUSdkDetails = new PayUSdkDetails();*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        //getData();
        addresses.clear();
        addresses.addAll(new SQLiteUtil().getAddressList(this));
        ListView addrlist = (ListView) findViewById(R.id.listView);
        MyAdapter adapter = new MyAdapter(this, addresses);
        addrlist.setAdapter(adapter);
    }

    private void getData() {
        list = CartList.getInstance().getArrayListCart();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        amount = bundle.getString("amount");
        amount = amount.substring(0, amount.length() - 3);

        txtAmt.setText(amount);
        discountedAmount = Integer.parseInt(amount);
        getDiscounts();
        // discountedAmount = Integer.parseInt(amount);
        /*addresses.clear();
        addresses.addAll(new SQLiteUtil().getAddressList(this));
        ListView addrlist = (ListView) findViewById(R.id.listView);
        MyAdapter adapter = new MyAdapter(this, addresses);
        addrlist.setAdapter(adapter);*/
    }


    class MyAdapter extends ArrayAdapter<Address> {

        ArrayList<Address> arr;

        public MyAdapter(Context context, ArrayList<Address> arr) {
            super(context, R.layout.addr_item);
            this.arr = arr;
        }

        @Override
        public int getCount() {
            return arr.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.addr_item, null);

            }
            Address address = arr.get(position);
            String add = address.getFname() + " " + address.getLname() + ",\n" + address.getAddr() + ", " + address.getArea() + ", " + ",\n" + address.getPhone();

            TextView txtaddr = (TextView) convertView.findViewById(R.id.textView3);
            txtaddr.setText(add);
            //txtaddr.setTypeface(Utils.setLatoFontBold(MyOrdersActivity.this));
            return convertView;
        }
    }

    private void setFonts() {
        //  orderBtn.setTypeface(Utils.setLatoFontBold(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        orderBtn = (Button) findViewById(R.id.orderBtn);
        spnTimeSlot = (Spinner) findViewById(R.id.spnTimeslot);
//        spnPaymentType =(Spinner)findViewById(R.id.spnPaymentType);

/*        spnTimeSlot.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
                new String[]{"9 - 11 AM","11 - 1 PM","1 - 3 PM","3 - 5 PM","5 - 7 PM"}));*/
        spnTimeSlot.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Before 9:00 AM"}));

        btnAddr = (Button) findViewById(R.id.btnAddr);
        txtDate = (TextView) findViewById(R.id.txtDate);
        txtAmt = (TextView) findViewById(R.id.amt);
        txtDeliveryCharges = (TextView) findViewById(R.id.txtDeliveryCharges);
        txtAmountToPay = (TextView) findViewById(R.id.txtAmountToPay);
        radioGroupPaymentMode = (RadioGroup) findViewById(R.id.radioGroupMode);
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == txtDate) {
                    fromDatePickerDialog.show();
                }
            }
        });
        btnAddr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PlaceOrderActivity.this, AddressActivity.class));
            }
        });

        dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        setDateTimeField();
        ((RadioButton) findViewById(R.id.radioCOD)).setChecked(true);
        radioGroupPaymentMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                discountedApplied = new StringBuilder();
                if (i == R.id.radioOnline) {
                    isOnline = true;
                    findViewById(R.id.txtDiscount).setVisibility(View.VISIBLE);
                    if (discountItem != null) {
                        //discountedAmount = (int) (Integer.parseInt(amount) * (5.0f / 100.0f));
                        discountedAmount = (Integer.parseInt(amount)) - Float.valueOf(discountItem.getOnline_Transction_Discount());
                        ((TextView) findViewById(R.id.txtDiscount)).setText("5% Discount amount to pay: " + discountItem.getOnline_Transction_Discount());
                        discountedApplied.append("Online 5% Discount amount: " + discountItem.getOnline_Transction_Discount() + "\n");
                    }
                } else if (i == R.id.radioCOD) {
                    isOnline = false;
                    //findViewById(R.id.txtDiscount).setVisibility(View.GONE);
                    discountedAmount = Integer.parseInt(amount);
                }
                setDefaultData();
            }
        });
    }

    private void setDefaultData() {
        txtAmt.setText(amount);
        /*int firstOrderDiscount = 0;
        Log.d("Amount", pref.getFirstOrderAmount());
        if (pref.getOrderCount() == 1) {
            firstOrderDiscount = Integer.parseInt(pref.getFirstOrderAmount()) / 2;
            if (firstOrderDiscount >= 250) {
                amount = String.valueOf((Integer.parseInt(amount)) - 250);
            } else {
                amount = String.valueOf((Integer.parseInt(amount)) - firstOrderDiscount);
            }
            if (Integer.parseInt(amount) < 0) {
                amount = "0";
            }
        }*/
        if (!discountItem.getSecond_Order_Discount().equalsIgnoreCase("0")) {
            discountedAmount = discountedAmount - Integer.parseInt(discountItem.getSecond_Order_Discount());
            discountedApplied.append("Second Order Discount amount: " + discountItem.getSecond_Order_Discount() + "\n");
        }
        if (!discountItem.getMonthly_5000_Amount_Discount().equalsIgnoreCase("0")) {
            discountedAmount = discountedAmount - Integer.parseInt(discountItem.getMonthly_5000_Amount_Discount());
            discountedApplied.append("Monthly 5000 Discount amount: " + discountItem.getMonthly_5000_Amount_Discount() + "\n");
        }
        if (!discountItem.getMonthly_10000_Amount_Discount().equalsIgnoreCase("0")) {
            discountedAmount = discountedAmount - Integer.parseInt(discountItem.getMonthly_10000_Amount_Discount());
            discountedApplied.append("Monthly 10000 Discount amount: " + discountItem.getMonthly_10000_Amount_Discount() + "\n");
        }
        if (!discountItem.getMonthly_15000_Amount_Discount().equalsIgnoreCase("0")) {
            discountedAmount = discountedAmount - Integer.parseInt(discountItem.getMonthly_15000_Amount_Discount());
            discountedApplied.append("Monthly 15000 Discount amount: " + discountItem.getMonthly_15000_Amount_Discount() + "\n");
        }

        if (discountedAmount > 250) {
            txtDeliveryCharges.setText("You are eligible for free delivery");
            //discountedAmount = Integer.parseInt(dataObject.getAmount_Payable());
        } else {
            txtDeliveryCharges.setText("Delivery charges is Rs.30 for order below Rs.250");
            discountedAmount = discountedAmount + Integer.parseInt(discountItem.getDelivery_Charges());
        }
        //amount = String.valueOf(discountedAmount);
        txtAmountToPay.setText("Total Amount: Rs." + discountedAmount);
        //    txtAmt.setText(amount);
        ((TextView) findViewById(R.id.txtDiscount)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.txtDiscount)).setText(discountedApplied);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            SpannableString s = new SpannableString("Delivery address");
            s.setSpan(new TypefaceSpan(this, "Jacquard.ttf"), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


            toolbar.setTitle(s);
            setSupportActionBar(toolbar);
        }
    }

    private void setDateTimeField() {

        final Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                Calendar cal = Calendar.getInstance();

                if (newDate.get(Calendar.DAY_OF_YEAR) > (cal.get(Calendar.DAY_OF_YEAR) + 10)) {
                    Toast.makeText(PlaceOrderActivity.this, "Please select delivery date within next ten days.", Toast.LENGTH_SHORT).show();
                    txtDate.setText("");
                } else if (newDate.after(newCalendar) && newDate.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)) {
                    Toast.makeText(PlaceOrderActivity.this, "Please select future date for delivery.", Toast.LENGTH_SHORT).show();
                    txtDate.setText("");

                } else if (newDate.after(newCalendar) && newDate.get(Calendar.DAY_OF_YEAR) > cal.get(Calendar.DAY_OF_YEAR)) {
                    txtDate.setText(dateFormatter.format(newDate.getTime()));

                    spnTimeSlot.setAdapter(new ArrayAdapter<String>(PlaceOrderActivity.this, android.R.layout.simple_spinner_dropdown_item,
                            new String[]{"Before 9:00 AM"}));

                }
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(PlaceOrderActivity.this);
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
                        new SQLiteUtil().emptyCart(PlaceOrderActivity.this);
//                        if (json.optInt("orderCount") > 1) {
//                            pref.setFirstOrderStatus(false);
//                        } else {
//                            pref.setFirstOrderStatus(true);
//                        }

                        pref.setOrderCount(json.optInt("orderCount"));
                        int baseamount = Integer.parseInt(txtAmt.getText().toString());
                        pref.setFirstOrderAmount("" + (baseamount));
                        AlertDialog.Builder alert = new AlertDialog.Builder(PlaceOrderActivity.this);
                        alert.setMessage("Your order placed successfully");
                        alert.setTitle("Thank you !");
                        alert.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PlaceOrderActivity.this.finish();

                            }
                        });
                        alert.show();

                        new SQLiteUtil().emptyCart(PlaceOrderActivity.this);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }        // onPostExecute displays the results of the AsyncTask.

    }

    public static String POST(String data) {


//        return HttpUtils.requestWebService(Constants.WS_URL, "POST", data);

        Response response = null;
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, data);
        Request request = new Request.Builder()
                .url(Constants.WS_URL)
                .post(body)
                .build();

        try {
            response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

    private void setListeners() {
        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (addresses == null || addresses.isEmpty()) {
                    Toast.makeText(PlaceOrderActivity.this, "Enter delivery address.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (txtDate.getText().toString().isEmpty()) {
                    txtDate.setError("Required field");
                    txtDate.requestFocus();
                    return;
                }

                if (spnTimeSlot.getSelectedItem().toString().equals("9 - 11 AM")) {

                }
//                makePayment(v);
                if (isOnline) {
                    //navigateToBaseActivity();
                    callPaymentActivity();
                } else {
                    placeOrder("Cash on delivery");
                }
            }
        });
    }

    private void callPaymentActivity() {
        Integer randomNum = ServiceUtility.randInt(0, 9999999);
        String orderId = randomNum.toString();
        String vAccessCode = ServiceUtility.chkNull(accessCode).toString().trim();
        String vMerchantId = ServiceUtility.chkNull(merchantId).toString().trim();
        String vCurrency = ServiceUtility.chkNull("INR").toString().trim();
        String vAmount = ServiceUtility.chkNull(discountedAmount).toString().trim();
        if (!vAccessCode.equals("") && !vMerchantId.equals("") && !vCurrency.equals("") && !vAmount.equals("")) {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(AvenuesParams.ACCESS_CODE, ServiceUtility.chkNull(accessCode).toString().trim());
            intent.putExtra(AvenuesParams.MERCHANT_ID, ServiceUtility.chkNull(merchantId).toString().trim());
            intent.putExtra(AvenuesParams.ORDER_ID, ServiceUtility.chkNull(orderId).toString().trim());
            intent.putExtra(AvenuesParams.CURRENCY, ServiceUtility.chkNull("INR").toString().trim());
            intent.putExtra(AvenuesParams.AMOUNT, ServiceUtility.chkNull(discountedAmount).toString().trim());

            intent.putExtra(AvenuesParams.REDIRECT_URL, ServiceUtility.chkNull(redirectUrl).toString().trim());
            intent.putExtra(AvenuesParams.CANCEL_URL, ServiceUtility.chkNull(cancelUrl).toString().trim());
            intent.putExtra(AvenuesParams.RSA_KEY_URL, ServiceUtility.chkNull(rsaKeyUrl).toString().trim());
            startActivityForResult(intent, PAYMENT_RECEIVED);
        } else {
            showToast("All parameters are mandatory.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_RECEIVED) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("transStatus");
                if (result.equalsIgnoreCase("Success")) {
                    placeOrder("Online");
                } else {
                    Toast.makeText(this, "Tansactiona has been failed. Please try again", Toast.LENGTH_SHORT).show();
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void placeOrder(String OrderMode) {
        String data1 = "\"[";
        String p_id = "", qty = "";
        String price = "";
        for (int i = 0; i < list.size(); i++) {
            p_id += list.get(i).getProduct_id() + ",";
            qty += list.get(i).getQuantity() + ",";
            price = list.get(i).getProduct().getMrp() + ",";
        }

        data1 += "]\"";
        req = "{\"method\":\"add_oder\"" +
                ",\"first_name\":\"" + addresses.get(0).getFname() + "\"" +
                ",\"last_name\":\"" + addresses.get(0).getLname() + "\"," +
                "\"gender\":\"Male\"" +
                ",\"email\":\"" + new AppPreferences(PlaceOrderActivity.this).getEmail() + "\"" +
                ",\"amount\":\"" + discountedAmount +
                "\",\"shipping_type\":\"" + OrderMode + "\"" +
                ",\"street\":\"" + addresses.get(0).getArea() + "\"" +
                ",\"city\":\"" + addresses.get(0).getAddr() + "\"" +
                ",\"state\":\"" + addresses.get(0).getLandmark() + "\"" +
                ",\"country\":\"India\"" +
                ",\"zipcode\":\"" + addresses.get(0).getZipcode() +
                "\",\"phone\":\"" + addresses.get(0).getPhone() + "\"" +
                ",\"order_detail\":\"Delivery Date " + txtDate.getText().toString() + "" +
                ", between " + spnTimeSlot.getSelectedItem().toString() + "\"" +
                ",\"user_id\":\"23\"" +
                ",\"p_id\":\"" + p_id + "\"" +
                ",\"qty\":\"" + qty + "\"," +
                "\"price\":\"" + price + "\"" +
                ",\"Second_Order_Discount\":\"" + discountItem.getSecond_Order_Discount() + "\"" +
                ",\"Monthly_5000_Amount_Discount\":\"" + discountItem.getMonthly_5000_Amount_Discount() + "\"" +
                ",\"Monthly_10000_Amount_Discount\":\"" + discountItem.getMonthly_10000_Amount_Discount() + "\"" +
                ",\"Monthly_15000_Amount_Discount\":\"" + discountItem.getMonthly_15000_Amount_Discount() + "\"" +
                ",\"Online_Transction_Discount\":\"" + discountItem.getOnline_Transction_Discount() + "\"" +
                ",\"Delivery_Charges\":\"" + discountItem.getDelivery_Charges() + "\"" +
                ",\"wallet\":\"" + new AppPreferences(PlaceOrderActivity.this).getWalletAmount() + "\"" +
                "}";

        Log.e("request", req);

        new HttpAsyncTask().execute(Constants.WS_URL, req);

    }

    private void getDiscounts() {
        String p_id = "";
        for (int i = 0; i < list.size(); i++) {
            p_id += list.get(i).getProduct_id() + ",";
        }
        String request;
        request = "{\"method\":\"get_discount_from_server\"" +
                ",\"email\":\"" + new AppPreferences(PlaceOrderActivity.this).getEmail() + "\"" +
                ",\"amount\":\"" + amount + "\"" +
                "}";
        new ProductTask().execute(Constants.WS_URL, request);
    }

    class ProductTask extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(PlaceOrderActivity.this);
            pd.setMessage("Loading...");
            pd.show();
            pd.setCancelable(false);
        }

        @Override
        protected String doInBackground(String... ulr) {
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            Log.e("request", ulr[1]);
            Log.e("request", ulr[0]);
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
                try {
                    Log.e("response", s);
                    JSONArray jsonArray = new JSONObject(s).optJSONArray("data");
                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject dataObject = jsonArray.getJSONObject(i);
                            discountItem = new DiscountItem();
                            discountItem.setAmount_Payable(dataObject.optString("Amount_Payable"));
                            discountItem.setDelivery_Charges(dataObject.optString("Delivery_Charges"));
                            discountItem.setMonthly_5000_Amount_Discount(dataObject.optString("Monthly_5000_Amount_Discount"));
                            discountItem.setMonthly_10000_Amount_Discount(dataObject.optString("Monthly_10000_Amount_Discount"));
                            discountItem.setMonthly_15000_Amount_Discount(dataObject.optString("Monthly_15000_Amount_Discount"));
                            discountItem.setOnline_Transction_Discount(dataObject.optString("Online_Transction_Discount"));
                            discountItem.setSecond_Order_Discount(dataObject.optString("Second_Order_Discount"));
                            setDefaultData();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
