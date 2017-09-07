package com.inventoryapp.android.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.inventoryapp.android.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by macie on 06.07.2017.
 */

public class ProductAdapter extends CursorAdapter {

    public ProductAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_product, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        final TextView name = (TextView) view.findViewById(R.id.name_view);
        final TextView info = (TextView) view.findViewById(R.id.info_view);
        final TextView price = (TextView) view.findViewById(R.id.price_view);
        final TextView quantity = (TextView) view.findViewById(R.id.quantity_view);

        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int infoColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_INFO);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);


        final String productName = cursor.getString(nameColumnIndex);
        final String productInfo = cursor.getString(infoColumnIndex);
        final String productPrice = cursor.getString(priceColumnIndex);
        final String productQuantity = cursor.getString(quantityColumnIndex);
        final String id = cursor.getString(idColumnIndex);
        final Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, Long.parseLong(id));

        name.setText(productName);
        info.setText(productInfo);
        price.setText(productPrice);
        quantity.setText(productQuantity);

        ImageView sellOne = (ImageView) view.findViewById(R.id.sell);
        sellOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int q = Integer.parseInt(quantity.getText().toString());
                q--;
                if (q >= 0) {
                    double priceDouble = Double.parseDouble(price.getText().toString().trim());
                    ContentResolver resolver = context.getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceDouble);
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, q);
                    resolver.update(uri, values, null, null);
                }
                if (q <= 0) {
                    q = 0;
                    Toast.makeText(context,
                            R.string.empty_stock, Toast.LENGTH_SHORT).show();
                }
                quantity.setText(String.valueOf(q));
            }
        });

    }
}
