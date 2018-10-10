package com.example.akash.inventoryappstage1;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.akash.inventoryappstage1.data.InventoryContract;

public class InventoryCursorAdapter extends RecyclerView.Adapter<InventoryCursorAdapter.ViewHolder>{

    CursorAdapter cursorAdapter;
    Context context;

    public InventoryCursorAdapter(Context context, Cursor cursor){
        this.context = context;
        cursorAdapter = new CursorAdapter(context,cursor){

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {

                TextView name = view.findViewById(R.id.productNameTextView);
                TextView price = view.findViewById(R.id.productPriceTextView);
                TextView quantity = view.findViewById(R.id.productQuantityTextView);

                String productName = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME));
                int productPrice = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_PRICE));
                int productQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_QUANTITY));
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.ProductEntry._ID));

                quantity.setText(String.valueOf(productQuantity));
                name.setText( String.valueOf(id)+ ":" + productName);
                price.setText(String.valueOf(productPrice));
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemCount() {
        return cursorAdapter.getCount();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Passing the binding operation to cursor loader
        cursorAdapter.getCursor().moveToPosition(position); //EDITED: added this line as suggested in the comments below, thanks :)
        cursorAdapter.bindView(holder.itemView, context, cursorAdapter.getCursor());

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Passing the inflater job to the cursor-adapter
        View v = cursorAdapter.newView(context, cursorAdapter.getCursor(), parent);
        return new ViewHolder(v);
    }
}
