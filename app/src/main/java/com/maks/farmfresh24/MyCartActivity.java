package com.maks.farmfresh24;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.maks.farmfresh24.adapter.CartAdapter;
import com.maks.farmfresh24.dbutils.SQLiteUtil;
import com.maks.farmfresh24.model.CartList;
import com.maks.farmfresh24.model.ShoppingCart;
import com.maks.farmfresh24.utils.AppPreferences;
import com.maks.farmfresh24.utils.TypefaceSpan;
import com.maks.farmfresh24.utils.Utils;

import java.util.ArrayList;

public class MyCartActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Toolbar toolbar;
    private TextView txtTotal, txtRs;
    private Button btnCheckout;
    private float totalAmt, mrp, qty, grandTotal = 0;
    ArrayList<ShoppingCart> list;
    String strGrandTotal = "0";
    SQLiteUtil dbUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cart);
        initToolbar();
        initView();
//        setFonts();
        dbUtil = new SQLiteUtil();
        Log.e("TAG", "Inside My Cart ");
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();

    }

    public void loadData() {

        list = dbUtil.getData(MyCartActivity.this);
        if (list.size() == 0) {
            btnCheckout.setText("Start Shopping");
            grandTotal = 0;
            txtRs.setText(String.format("Rs. %.2f", grandTotal));
        } else {
            grandTotal = 0;
            for (int i = 0; i < list.size(); i++) {
                try {
                    mrp = Integer.parseInt(Utils.discountPrice(list.get(i).getProduct().getMrp(), list.get(i).getProduct().getPer_discount()));

                    totalAmt = mrp * Integer.parseInt(list.get(i).getQuantity());
                    grandTotal = grandTotal + totalAmt;

                    String item = list.get(i).getProduct().getShort_desc().toString();
                    Log.e("TAG", "My Cart:::Item: " + item);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        adapter = new CartAdapter(list, MyCartActivity.this);
        recyclerView.setAdapter(adapter);

        txtRs.setText(String.format("Rs. %.2f", grandTotal));

    }

    public void onItemClick(View v, int position) {
        ShoppingCart sh = list.get(position);
        if (v.getId() == R.id.btnDel) {
            dbUtil.deleteCartItem(sh.getId(), this);
        }
        if (v.getId() == R.id.btnMinus) {
            int q = Integer.parseInt(sh.getQuantity());

            if (q > 1) {

                dbUtil.deleteCartItem(sh.getId(), this);
                sh.setQuantity("" + (q - 1));
                dbUtil.insert(sh, this);

            }
        }

        if (v.getId() == R.id.btnPlus) {
            int q = Integer.parseInt(sh.getQuantity());

            dbUtil.deleteCartItem(sh.getId(), this);
            sh.setQuantity("" + (q + 1));
            dbUtil.insert(sh, this);
        }


        loadData();

    }

    private void setListener() {
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnCheckout.getText().equals("Start Shopping")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {

                    if (new AppPreferences(MyCartActivity.this).isLogin()) {
                        Intent intent = new Intent(getApplicationContext(), PlaceOrderActivity.class);
                        intent.putExtra("amount", txtRs.getText().toString().substring(4));
                        CartList.getInstance().setArrayListCart(list);
                        startActivity(intent);
                    } else {
                        Intent intent4 = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent4);
                    }

                }
            }
        });
    }

    private void setFonts() {
        txtTotal.setTypeface(Utils.setLatoFontBold(this));
        txtTotal.setTypeface(Utils.setLatoFontBold(this));
        txtRs.setTypeface(Utils.setLatoFontBold(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            SpannableString s = new SpannableString("My shopping cart ");
            s.setSpan(new TypefaceSpan(this, "Jacquard.ttf"), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            toolbar.setTitle(s);
            setSupportActionBar(toolbar);
        }

    }

    private void initView() {

        txtTotal = (TextView) findViewById(R.id.txtTotal);
        txtRs = (TextView) findViewById(R.id.txtRs);
        btnCheckout = (Button) findViewById(R.id.btnCheckOut);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

    }
}
