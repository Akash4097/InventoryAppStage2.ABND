package com.example.akash.inventoryappstage1;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.akash.inventoryappstage1.data.InventoryContract;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.detailsProductNameTextView)
    TextView productName;
    @BindView(R.id.detailsProductPriceTextView)
    TextView productPrice;
    @BindView(R.id.detailsProductQuantityTextView)
    TextView productQuantity;
    @BindView(R.id.detailsSupplierNameTextView)
    TextView supplierName;
    @BindView(R.id.detailsSupplierPhoneNumberTextView)
    TextView supplierPhoneNumber;
    @BindView(R.id.decreaseQuantityButton)
    Button decreaseQuantityButton;
    @BindView(R.id.increaseQuantityButton)
    Button increaseQuantityButton;
    @BindView(R.id.orderButton)
    Button orderButton;

    /**
     * instance variable that hold the supplier phone number of the product
     */
    String prSupplierPhoneNumber;
    /**
     * instance variable that hold the quantity of the product
     */
    private int quantity;
    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri != null) {
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        } else {
            throw new IllegalArgumentException(getString(R.string.uri_null_exception));
        }

        decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                quantity--;
                if (quantity >= 0) {
                    values.put(InventoryContract.ProductEntry.COLUMN_QUANTITY, quantity);
                    getContentResolver().update(mCurrentProductUri, values, null, null);
                } else {
                    Toast.makeText(DetailsActivity.this, R.string.quantity_less_than_zero_error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                quantity++;
                values.put(InventoryContract.ProductEntry.COLUMN_QUANTITY, quantity);
                getContentResolver().update(mCurrentProductUri, values, null, null);
            }
        });

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + prSupplierPhoneNumber));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_menu_option:
                Intent intent = new Intent(this, EditorActivity.class);
                intent.setData(mCurrentProductUri);
                startActivity(intent);
                finish();
                return true;
            case R.id.delete_menu_option:
                showDeleteConfirmationDialog();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String projection[] = {InventoryContract.ProductEntry._ID,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.ProductEntry.COLUMN_PRICE,
                InventoryContract.ProductEntry.COLUMN_QUANTITY,
                InventoryContract.ProductEntry.COLUMN_SUPPLIER_NAME,
                InventoryContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER};
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int productNameIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int productPriceIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRICE);
            int productQuantityIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_QUANTITY);
            int supplierNameIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneNumberIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String prName = data.getString(productNameIndex);
            int prPrice = data.getInt(productPriceIndex);
            quantity = data.getInt(productQuantityIndex);
            String prSupplierName = data.getString(supplierNameIndex);
            prSupplierPhoneNumber = data.getString(supplierPhoneNumberIndex);

            // Update the views on the screen with the values from the database
            productName.setText(prName);
            productPrice.setText(String.valueOf(prPrice));
            productQuantity.setText(String.valueOf(quantity));
            supplierName.setText(prSupplierName);
            supplierPhoneNumber.setText(prSupplierPhoneNumber);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        productName.setText("");
        productPrice.setText(String.valueOf(0));
        productQuantity.setText(String.valueOf(0));
        supplierName.setText("");
        supplierPhoneNumber.setText("");
    }
}
