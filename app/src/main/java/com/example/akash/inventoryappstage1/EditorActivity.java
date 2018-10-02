package com.example.akash.inventoryappstage1;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.akash.inventoryappstage1.data.InventoryContract;
import com.example.akash.inventoryappstage1.data.InventoryDBHelper;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Allows user to create a new product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity {


    /**
     * EditText field to enter the products's name
     */
    @BindView(R.id.product_name_edit_text)
    EditText productName;

    /**
     * EditText field to enter the products's price
     */
    @BindView(R.id.product_price_edit_text)
    EditText productPrice;

    /**
     * EditText field to enter the products's quantity
     */
    @BindView(R.id.product_quantity_edit_text)
    EditText productQuantity;

    /**
     * EditText field to enter the products's supplier name
     */
    @BindView(R.id.product_supplier_name_edit_text)
    EditText supplierName;

    /**
     * EditText field to enter the products's supplier's phone number
     */
    @BindView(R.id.product_supplier_phone_number_edit_text)
    EditText supplierPhoneNumber;
    InventoryDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        dbHelper = new InventoryDBHelper(this);
    }

    /***
     * take input from EditText and save into the product database, here pr stands for product
     */
    private void insertProduct() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String prName = productName.getText().toString().trim();
        int prPrice = Integer.parseInt(productPrice.getText().toString().trim());
        int prQuantity = Integer.parseInt(productQuantity.getText().toString().trim());
        String prSupplierName = supplierName.getText().toString().trim();
        String prSupplierPhoneNumber = supplierPhoneNumber.getText().toString().trim();

        // Gets the database in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME, prName);
        values.put(InventoryContract.ProductEntry.COLUMN_PRICE, prPrice);
        values.put(InventoryContract.ProductEntry.COLUMN_QUANTITY, prQuantity);
        values.put(InventoryContract.ProductEntry.COLUMN_SUPPLIER_NAME, prSupplierName);
        values.put(InventoryContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, prSupplierPhoneNumber);

        // Insert a new row for product in the database, returning the ID of that new row.
        long id = db.insert(InventoryContract.ProductEntry.TABLE_NAME, null, values);

        if (id != -1) {
            // If the row ID is other than -1 then the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, "product saved with row id " + id, Toast.LENGTH_SHORT);
        } else {
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, "Error with saving product", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                //save product to the database
                insertProduct();
                //finish the activity
                finish();
                return true;
            case R.id.action_delete:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
