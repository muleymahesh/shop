
package com.maks.farmfresh24.adapter;

/**
 * Created by maks on 7/2/16.
 */

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maks.farmfresh24.MyOrdersActivity;
import com.maks.farmfresh24.R;
import com.maks.farmfresh24.model.OrderPojo;
import com.maks.farmfresh24.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Belal on 11/9/2015.
 */
public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.ViewHolder> {

   // private CategoryActivity context;
   private MyOrdersActivity context;
    OnItemClickListener mItemClickListener;
    //List of Category
    List<OrderPojo> Category;
    Activity activity;

    public MyOrderAdapter(List<OrderPojo> Category, MyOrdersActivity context){
        super();
        this.context = context;
        //Getting all the Category
        this.Category = Category;
        activity = (Activity)context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_myorder_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        OrderPojo category =  Category.get(position);

    try {

        if(category.getDetails()!=null && !category.getDetails().isEmpty()) {
            Picasso.with(context).load(Constants.PRODUCT_IMG_PATH + category.getDetails().get(0).getImgUrl()).resize(400, 220).centerCrop().into(holder.imageView);
            holder.txtName.setText(category.getDetails().get(0).getProductName());
            holder.txtPrice.setText("Rs. "+category.getDetails().get(0).getMrp());
        }

    }catch(Exception e){}
        holder.txtStatus.setText(category.getOrder_status());
        holder.txtID.setText( category.getOId()+" Date: "+category.getDate());
    }

    @Override
    public int getItemCount() {
        return Category.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView imageView;
        public TextView txtName, txtStatus, txtPrice, txtID;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            imageView = (ImageView) itemView.findViewById(R.id.imgProduct);
            txtName = (TextView) itemView.findViewById(R.id.txtName);
//            txtName.setTypeface(Utils.setLatoFontBold(activity));

            txtStatus = (TextView) itemView.findViewById(R.id.txtStatus);
//            txtStatus.setTypeface(Utils.setLatoFontBold(activity));
            txtPrice = (TextView) itemView.findViewById(R.id.txtPrice);
//            txtPrice.setTypeface(Utils.setLatoFontBold(activity));
            txtID = (TextView) itemView.findViewById(R.id.txtID);
//            txtID.setTypeface(Utils.setLatoFontBold(activity));
        }

        @Override
        public void onClick(View v) {
            context.onItemClick(v,getPosition());
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
