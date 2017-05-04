package com.rebindtech.delivery.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rebindtech.delivery.MainActivity;
import com.rebindtech.delivery.R;
import com.rebindtech.delivery.model.OrderPojo;

import java.util.List;

/**
 * Created by Deva on 08/03/2017.
 */

public class MyOrderListAdapter extends RecyclerView.Adapter<MyOrderListAdapter.ViewHolder> {

    // private CategoryActivity context;
    Context context;
    OnItemClickListener mItemClickListener;
    //List of Category
    List<OrderPojo> Category;
    Activity activity;

    public MyOrderListAdapter(List<OrderPojo> Category, Context context) {
        super();
        this.context = context;
        //Getting all the Category
        this.Category = Category;
        activity = (Activity) context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_my_orders, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final OrderPojo category = Category.get(position);

        holder.txtOrderDate.setText("Date: " + category.getDate());
        holder.txtOrderPrice.setText("Rs. " + category.getAmount());
        holder.txtOrderId.setText("Order ID: " + category.getOId());
        /*holder.layoutMyOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("OrderList", (Serializable) category.getDetails());
                intent.putExtra("TotalAmount", category.getAmount());
                context.startActivity(intent);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return Category.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        //public ImageView imageView;
        public TextView txtOrderDate, txtOrderId, txtOrderPrice;
        public LinearLayout layoutMyOrder;
        public EditText edtStatus;
        public Button buttonStatusUpdate;

        public ViewHolder(View itemView) {
            super(itemView);
            layoutMyOrder = (LinearLayout) itemView.findViewById(R.id.layoutMyOrder);
            txtOrderDate = (TextView) itemView.findViewById(R.id.txtMyOrderDate);
            txtOrderId = (TextView) itemView.findViewById(R.id.txtMyOrderOrderId);
            txtOrderPrice = (TextView) itemView.findViewById(R.id.txtMyOrderRupee);
            edtStatus = (EditText) itemView.findViewById(R.id.editTextStatus);
            buttonStatusUpdate = (Button) itemView.findViewById(R.id.buttonUpdate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, MainActivity.class);
                }
            });
            buttonStatusUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}