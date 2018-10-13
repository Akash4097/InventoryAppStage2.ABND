package com.example.akash.inventoryappstage1;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.akash.inventoryappstage1.data.InventoryContract;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.example.akash.inventoryappstage1.data.InventoryContract.ProductEntry;

public class InventoryCursorAdapter extends CursorRecyclerViewAdapter<InventoryCursorAdapter.ViewHolder> {
    private Context context;
    private Cursor cursor;

    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;
        this.cursor = cursor;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.productNameTextView)
        TextView productName;
        @BindView(R.id.productPriceTextView)
        TextView productPrice;
        @BindView(R.id.productQuantityTextView)
        TextView productQuantity;
        @BindView(R.id.saleButton)
        Button saleButton;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void onBindViewHolder(final InventoryCursorAdapter.ViewHolder viewHolder, final Cursor cursor) {
        String productName = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME));
        int productPrice = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_PRICE));
        int productQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_QUANTITY));

        viewHolder.productName.setText(productName);
        viewHolder.productPrice.setText(String.valueOf(productPrice));
        viewHolder.productQuantity.setText(String.valueOf(productQuantity));


        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailsActivity.class);
                int id = viewHolder.getAdapterPosition();
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, getItemId(id));
                intent.setData(currentProductUri);
                context.startActivity(intent);

            }
        });

        viewHolder.saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                cursor.moveToPosition(position);
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_QUANTITY));
                quantity--;

                if (quantity >= 0) {
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_QUANTITY, quantity);
                    Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, getItemId(position));
                    context.getContentResolver().update(uri, values, null, null);
                } else {
                    Toast.makeText(context, context.getString(R.string.quantity_less_than_zero_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @NonNull
    @Override
    public InventoryCursorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }
}
