package com.example.akash.inventoryappstage1;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.akash.inventoryappstage1.data.InventoryContract;
import com.example.akash.inventoryappstage1.data.InventoryDBHelper;
import com.example.akash.inventoryappstage1.data.InventoryContract.ProductEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    InventoryDBHelper dbHelper;
    @BindView(R.id.fab)
    FloatingActionButton inventoryFab;
    @BindView(R.id.text_view_products)
    TextView displayProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        inventoryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        dbHelper = new InventoryDBHelper(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the product database.
     */
    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.

        // Create and/or open a database to read from it
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //create projection variable
        String project[] = {ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        Cursor cursor = db.query(ProductEntry.TABLE_NAME,
                project, null,
                null, null,
                null, null);
        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // product table in the database).
            displayProducts.setText("The products table contains " + cursor.getCount() + " products.\n\n");
            displayProducts.append(ProductEntry._ID + " - " +
                    ProductEntry.COLUMN_PRODUCT_NAME + " - " +
                    ProductEntry.COLUMN_PRICE + " - " +
                    ProductEntry.COLUMN_QUANTITY + " - " +
                    ProductEntry.COLUMN_SUPPLIER_NAME + " - " +
                    ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
            int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int productPriceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int productQuantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int productSupplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int productSupplierPhoneNumberColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String productName = cursor.getString(productNameColumnIndex);
                int productPrice = cursor.getInt(productPriceColumnIndex);
                int productQuantity = cursor.getInt(productQuantityColumnIndex);
                String supplierName = cursor.getString(productSupplierNameColumnIndex);
                String supplierNumber = cursor.getString(productSupplierPhoneNumberColumnIndex);

                // Display the values from each column of the current row in the cursor in the TextView
                displayProducts.append(("\n" + currentID + " - " +
                        productName + " - " +
                        productPrice + " - " +
                        productQuantity + " - " +
                        supplierName + " - " +
                        supplierNumber));
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }


    private void insertProduct() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Asus Zenfone Max pro");
        values.put(ProductEntry.COLUMN_PRICE, 10999);
        values.put(ProductEntry.COLUMN_QUANTITY, 10);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, "Anothny Gonjales");
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, "91-987654321");

        long id = db.insert(ProductEntry.TABLE_NAME, null, values);
        Log.i("database Id ", String.valueOf(id));

    }

    /**
     * this method used to delete all the products from the product database table
     */
    private void deleteAllProduct() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(InventoryContract.ProductEntry.TABLE_NAME, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertProduct();
                displayDatabaseInfo();
                return true;
            case R.id.action_delete_all_products:
                deleteAllProduct();
                displayDatabaseInfo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
