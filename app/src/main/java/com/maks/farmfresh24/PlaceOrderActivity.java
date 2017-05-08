package com.maks.farmfresh24;

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

import com.maks.farmfresh24.ccavenuepayment.WebViewActivity;
import com.maks.farmfresh24.ccavenuepayment.dto.CardTypeDTO;
import com.maks.farmfresh24.ccavenuepayment.dto.EMIOptionDTO;
import com.maks.farmfresh24.ccavenuepayment.dto.PaymentOptionDTO;
import com.maks.farmfresh24.ccavenuepayment.utility.AvenuesParams;
import com.maks.farmfresh24.ccavenuepayment.utility.ServiceHandler;
import com.maks.farmfresh24.ccavenuepayment.utility.ServiceUtility;
import com.maks.farmfresh24.dbutils.SQLiteUtil;
import com.maks.farmfresh24.model.Address;
import com.maks.farmfresh24.model.CartList;
import com.maks.farmfresh24.model.DiscountItem;
import com.maks.farmfresh24.model.ShoppingCart;
import com.maks.farmfresh24.utils.AppPreferences;
import com.maks.farmfresh24.utils.Constants;
import com.maks.farmfresh24.utils.TypefaceSpan;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlaceOrderActivity extends AppCompatActivity {

    public static final String TAG = PlaceOrderActivity.class.getSimpleName();

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
    Map<String, ArrayList<CardTypeDTO>> cardsList = new LinkedHashMap<String, ArrayList<CardTypeDTO>>();
    ArrayList<PaymentOptionDTO> payOptionList = new ArrayList<PaymentOptionDTO>();
    ArrayList<EMIOptionDTO> emiOptionList = new ArrayList<EMIOptionDTO>();

    private ProgressDialog pDialog;

    String selectedPaymentOption;
    CardTypeDTO selectedCardType;
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

    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    /*private double getAmount() {
        Double amount = 10.0;
        if (isDouble(txtAmt.getText().toString())) {
            amount = Double.parseDouble(this.amount);
            return amount;
        } else {
            Toast.makeText(getApplicationContext(), "Paying Default Amount ₹10", Toast.LENGTH_LONG).show();
            return amount;
        }
    }*/

    /*public void makePayment(View view) {

        String phone = "8882434664";
        String productName = "product_name";
        String firstName = "piyush";
        String txnId = "0nf7" + System.currentTimeMillis();
        String email = "piyush.jain@payu.in";
        String sUrl = "https://test.payumoney.com/mobileapp/payumoney/success.php";
        String fUrl = "https://test.payumoney.com/mobileapp/payumoney/failure.php";
        String udf1 = "";
        String udf2 = "";
        String udf3 = "";
        String udf4 = "";
        String udf5 = "";
        boolean isDebug = true;
        String key = "mJX31QtA";
        String merchantId = "5743923";
    }

    private void showDialogMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(TAG);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }
*/
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if (data != null) {

                *//**
                 * Here, data.getStringExtra("payu_response") ---> Implicit response sent by PayU
                 * data.getStringExtra("result") ---> Response received from merchant's Surl/Furl
                 *
                 * PayU sends the same response to merchant server and in app. In response check the value of key "status"
                 * for identifying status of transaction. There are two possible status like, success or failure
                 * *//*

                try {
                    JSONObject response = new JSONObject(data.getStringExtra("payu_response"));
                    if (response.optString("status").equalsIgnoreCase("success")) {
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                        placeOrder("Online");
                    } else {
                        Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
                    }
                    *//*new android.app.AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setMessage("Your Payment has been " + response.optString("status"))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();

                                }
                            }).show();*//*
                } catch (Exception e) {
                    e.printStackTrace();
                }
*//*
                new android.app.AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("Payu's Data : " + data.getStringExtra("payu_response") + "\n\n\n Merchant's Data: " + data.getStringExtra("result"))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }).show();*//*

            } else {
                Toast.makeText(this, getString(R.string.could_not_receive_data), Toast.LENGTH_LONG).show();
            }
        }
    }*/


    private void getDiscounts() {
        String p_id = "";
        for (int i = 0; i < list.size(); i++) {
            p_id += list.get(i).getProduct_id() + ",";
        }
        String request;
        request = "{\"method\":\"get_discount_from_server\"" +
                ",\"email\":\"" + new AppPreferences(PlaceOrderActivity.this).getEmail() + "\"" +
                ",\"totalamount\":\"" + amount + "\"" +
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

    public void callPaymentActivity() {
        //generating order number
        AppPreferences pref = new AppPreferences(PlaceOrderActivity.this);
        Integer randomNum = ServiceUtility.randInt(0, 9999999);
        String vOrderId = ServiceUtility.chkNull(randomNum).toString().trim();
        String vRsaKeyUrl = ServiceUtility.chkNull("http://farmfresh24.in/farmfresh24/admin/ccavenue/GetRSA.php").toString().trim();
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(AvenuesParams.ORDER_ID, ServiceUtility.chkNull(randomNum).toString().trim());
        intent.putExtra(AvenuesParams.ACCESS_CODE, ServiceUtility.chkNull("AVCT70ED45CC50TCCC").toString().trim());
        intent.putExtra(AvenuesParams.MERCHANT_ID, ServiceUtility.chkNull("129307").toString().trim());
        intent.putExtra(AvenuesParams.BILLING_NAME, ServiceUtility.chkNull(pref.getFname()).toString().trim());
        intent.putExtra(AvenuesParams.BILLING_ADDRESS, ServiceUtility.chkNull(addresses.get(0).getArea() + " " + addresses.get(0).getAddr()).toString().trim());
        intent.putExtra(AvenuesParams.BILLING_COUNTRY, ServiceUtility.chkNull("India").toString().trim());
        intent.putExtra(AvenuesParams.BILLING_STATE, ServiceUtility.chkNull(addresses.get(0).getAddr()).toString().trim());
        intent.putExtra(AvenuesParams.BILLING_CITY, ServiceUtility.chkNull(addresses.get(0).getArea()).toString().trim());
        intent.putExtra(AvenuesParams.BILLING_ZIP, ServiceUtility.chkNull(addresses.get(0).getZipcode()).toString().trim());
        intent.putExtra(AvenuesParams.BILLING_TEL, ServiceUtility.chkNull(addresses.get(0).getPhone()).toString().trim());
        intent.putExtra(AvenuesParams.BILLING_EMAIL, ServiceUtility.chkNull(addresses.get(0).getEmail()).toString().trim());
        intent.putExtra(AvenuesParams.DELIVERY_NAME, ServiceUtility.chkNull(pref.getFname()).toString().trim());
        intent.putExtra(AvenuesParams.DELIVERY_ADDRESS, ServiceUtility.chkNull(addresses.get(0).getArea() + " " + addresses.get(0).getAddr()).toString().trim());
        intent.putExtra(AvenuesParams.DELIVERY_COUNTRY, ServiceUtility.chkNull("India").toString().trim());
        intent.putExtra(AvenuesParams.DELIVERY_STATE, ServiceUtility.chkNull(addresses.get(0).getArea()).toString().trim());
        intent.putExtra(AvenuesParams.DELIVERY_CITY, ServiceUtility.chkNull(addresses.get(0).getLandmark()).toString().trim());
        intent.putExtra(AvenuesParams.DELIVERY_ZIP, ServiceUtility.chkNull(addresses.get(0).getZipcode()).toString().trim());
        intent.putExtra(AvenuesParams.DELIVERY_TEL, ServiceUtility.chkNull(addresses.get(0).getPhone()).toString().trim());

        /*String cardCVV = ServiceUtility.chkNull(cardCvv.getText()).toString().trim();
        if (((LinearLayout) findViewById(R.id.vCardCVVCont)).getVisibility() == 0 && vCardCVV.getVisibility() == 0) {
            cardCVV = ServiceUtility.chkNull(vCardCVV.getText()).toString().trim();
        }*/
        //intent.putExtra(AvenuesParams.CVV, cardCVV);
        intent.putExtra(AvenuesParams.REDIRECT_URL, ServiceUtility.chkNull("http://farmfresh24.in/farmfresh24/admin/ccavenue/ccavResponseHandler.php").toString().trim());
        intent.putExtra(AvenuesParams.CANCEL_URL, ServiceUtility.chkNull("http://farmfresh24.in/farmfresh24/admin/ccavenue/ccavResponseHandler.php").toString().trim());
        intent.putExtra(AvenuesParams.RSA_KEY_URL, ServiceUtility.chkNull("http://farmfresh24.in/farmfresh24/admin/ccavenue/GetRSA.php").toString().trim());
        intent.putExtra(AvenuesParams.PAYMENT_OPTION, selectedPaymentOption);
        /*intent.putExtra(AvenuesParams.CARD_NUMBER, ServiceUtility.chkNull(cardNumber.getText()).toString().trim());
        intent.putExtra(AvenuesParams.EXPIRY_YEAR, ServiceUtility.chkNull(expiryYear.getText()).toString().trim());
        intent.putExtra(AvenuesParams.EXPIRY_MONTH, ServiceUtility.chkNull(expiryMonth.getText()).toString().trim());
        intent.putExtra(AvenuesParams.ISSUING_BANK, ServiceUtility.chkNull(issuingBank.getText()).toString().trim());*/
        startActivity(intent);
    }

    private JSONObject jsonRespObj;
    private Map<String, String> paymentOptions = new LinkedHashMap<String, String>();

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(PlaceOrderActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            List<NameValuePair> vParams = new ArrayList<NameValuePair>();
            vParams.add(new BasicNameValuePair(AvenuesParams.COMMAND, "getJsonDataVault"));
            vParams.add(new BasicNameValuePair(AvenuesParams.ACCESS_CODE, "AVCT70ED45CC50TCCC".toString().trim()));
            vParams.add(new BasicNameValuePair(AvenuesParams.CURRENCY, "INR".toString().trim()));
            vParams.add(new BasicNameValuePair(AvenuesParams.AMOUNT, String.valueOf(discountedAmount).toString().trim()));
            vParams.add(new BasicNameValuePair(AvenuesParams.CUSTOMER_IDENTIFIER, pref.getFname().toString().trim()));

            String vJsonStr = sh.makeServiceCall(com.maks.farmfresh24.ccavenuepayment.utility.Constants.JSON_URL, ServiceHandler.POST, vParams);

            Log.d("Response: ", "> " + vJsonStr);

            if (vJsonStr != null && !vJsonStr.equals("")) {
                try {
                    jsonRespObj = new JSONObject(vJsonStr);
                    if (jsonRespObj != null) {
                        if (jsonRespObj.getString("payOptions") != null) {
                            JSONArray vPayOptsArr = new JSONArray(jsonRespObj.getString("payOptions"));
                            for (int i = 0; i < vPayOptsArr.length(); i++) {
                                JSONObject vPaymentOption = vPayOptsArr.getJSONObject(i);
                                if (vPaymentOption.getString("payOpt").equals("OPTIVRS")) continue;
                                payOptionList.add(new PaymentOptionDTO(vPaymentOption.getString("payOpt"), vPaymentOption.getString("payOptDesc").toString()));//Add payment option only if it includes any card
                                paymentOptions.put(vPaymentOption.getString("payOpt"), vPaymentOption.getString("payOptDesc"));
                                try {
                                    JSONArray vCardArr = new JSONArray(vPaymentOption.getString("cardsList"));
                                    if (vCardArr.length() > 0) {
                                        cardsList.put(vPaymentOption.getString("payOpt"), new ArrayList<CardTypeDTO>()); //Add a new Arraylist
                                        for (int j = 0; j < vCardArr.length(); j++) {
                                            JSONObject card = vCardArr.getJSONObject(j);
                                            try {
                                                CardTypeDTO cardTypeDTO = new CardTypeDTO();
                                                cardTypeDTO.setCardName(card.getString("cardName"));
                                                cardTypeDTO.setCardType(card.getString("cardType"));
                                                cardTypeDTO.setPayOptType(card.getString("payOptType"));
                                                cardTypeDTO.setDataAcceptedAt(card.getString("dataAcceptedAt"));
                                                cardTypeDTO.setStatus(card.getString("status"));

                                                cardsList.get(vPaymentOption.getString("payOpt")).add(cardTypeDTO);
                                            } catch (Exception e) {
                                                Log.e("ServiceHandler", "Error parsing cardType", e);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("ServiceHandler", "Error parsing payment option", e);
                                }
                            }
                        }
                        if ((jsonRespObj.getString("EmiBanks") != null && jsonRespObj.getString("EmiBanks").length() > 0) &&
                                (jsonRespObj.getString("EmiPlans") != null && jsonRespObj.getString("EmiPlans").length() > 0)) {
                            paymentOptions.put("OPTEMI", "Credit Card EMI");
                            payOptionList.add(new PaymentOptionDTO("OPTEMI", "Credit Card EMI"));
                        }
                    }
                } catch (JSONException e) {
                    Log.e("ServiceHandler", "Error fetching data from server", e);
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            selectedPaymentOption = "OPTDBCRD";
        }
    }
}
