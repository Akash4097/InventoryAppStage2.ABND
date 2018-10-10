package com.example.akash.inventoryappstage1;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.akash.inventoryappstage1.data.InventoryContract;
import com.example.akash.inventoryappstage1.data.InventoryDBHelper;
import com.example.akash.inventoryappstage1.data.InventoryContract.ProductEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    InventoryDBHelper dbHelper;
    @BindView(R.id.product_list)
    RecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton inventoryFab;
    @BindView(R.id.empty_view)
    RelativeLayout emptyView;

    private static final int PRODUCT_LOADER = 0;
    private InventoryCursorAdapter cursorAdapter;

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

//        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        cursorAdapter = new InventoryCursorAdapter(this,null);
        recyclerView.setAdapter(cursorAdapter);
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        dbHelper = new InventoryDBHelper(this);
    }

    private void insertProduct() {
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Asus Zenfone Max pro");
        values.put(ProductEntry.COLUMN_PRICE, 10999);
        values.put(ProductEntry.COLUMN_QUANTITY, 10);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, "Anothny Gonjales");
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, "91-987654321");

        Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
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
                return true;
            case R.id.action_delete_all_products:
                deleteAllProduct();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        //create projection variable
        String projection[] = {ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER};
        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // update {@link InventoryCursorAdapter} with this new cursor containing update product data
        cursorAdapter.cursorAdapter.swapCursor(data);
        if (cursorAdapter.cursorAdapter.getCursor().getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {
         //Callback is called when the data needs to be deleted
        cursorAdapter.cursorAdapter.swapCursor(null);
    }
}
