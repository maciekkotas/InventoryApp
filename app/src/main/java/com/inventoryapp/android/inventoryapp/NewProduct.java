package com.inventoryapp.android.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.inventoryapp.android.inventoryapp.data.ProductContract.ProductEntry;

import java.io.InputStream;

/**
 * Created by macie on 05.07.2017.
 */

public class NewProduct extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri currentProductUri;
    private static final int PRODUCT_LOADER = 0;
    private EditText nameEditText;
    private EditText infoEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private ImageView addImage;
    private boolean productHasChanged = false;
    private int quantity;
    private static final int RESULT_LOAD_IMG = 0;
    private Uri uri;


    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            productHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_product);

        addImage = (ImageView) findViewById(R.id.add_image);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMG);

            }
        });

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        if (currentProductUri == null) {
            setTitle(R.string.title_new);
        } else {
            setTitle(R.string.title_update);
            getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }

        nameEditText = (EditText) findViewById(R.id.name_edit_text);
        infoEditText = (EditText) findViewById(R.id.info_edit_text);
        priceEditText = (EditText) findViewById(R.id.price_edit_text);
        quantityEditText = (EditText) findViewById(R.id.quantity_edit_text);

        nameEditText.setOnTouchListener(touchListener);
        infoEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        ImageView add = (ImageView) findViewById(R.id.addButton);
        ImageView reduce = (ImageView) findViewById(R.id.reduce);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantityEditText.getText().toString().isEmpty()) {
                    quantity = 0;
                    quantity++;
                } else {
                    quantity = Integer.parseInt(quantityEditText.getText().toString()) + 1;
                }
                quantityEditText.setText("" + quantity);
            }
        });
        reduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantityEditText.getText().toString().isEmpty()) {
                    Toast.makeText(NewProduct.this, R.string.empty_stock, Toast.LENGTH_LONG).show();
                    quantity = 0;
                } else {
                    quantity = Integer.parseInt(quantityEditText.getText().toString()) - 1;
                    if (quantity < 0) {
                        quantity = 0;
                        Toast.makeText(NewProduct.this, R.string.empty_stock, Toast.LENGTH_LONG).show();
                    }
                }
                quantityEditText.setText("" + quantity);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabTwo);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String price = priceEditText.getText().toString();
                String quantity = quantityEditText.getText().toString();
                String info = infoEditText.getText().toString();

                if (name.isEmpty() || price.isEmpty() || quantity.isEmpty() || uri == null || info.isEmpty()) {
                    if (name.isEmpty()) {
                        nameEditText.setError(getString(R.string.error_name));
                    }
                    if (price.isEmpty()) {
                        priceEditText.setError(getString(R.string.error_price));
                    }
                    if (quantity.isEmpty()) {
                        quantityEditText.setError(getString(R.string.error_quantity));
                    }
                    if (info.isEmpty()){
                        infoEditText.setError(getString(R.string.error_info));
                    }
                    if (uri == null) {
                        Toast.makeText(NewProduct.this, R.string.error_image,
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    saveProduct();
                    finish();
                }
            }
        });
    }

    private void saveProduct() {

        String nameString = nameEditText.getText().toString().trim();
        String infoString = infoEditText.getText().toString().trim();
        String quantity = quantityEditText.getText().toString().trim();
        double price = Double.parseDouble(priceEditText.getText().toString().trim());
        String mImageUri = uri.toString();

        if (currentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(infoString) &&
                price == 0 && TextUtils.isEmpty(quantity)) {
            return;
        }

        final ContentValues values = new ContentValues();

        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_INFO, infoString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, mImageUri);

        if (currentProductUri == null) {

            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, R.string.product_saved_error,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.product_saved,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.product_saved_error,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.product_saved,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_product_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.make_order:
                String name = nameEditText.getText().toString();
                String quantity = quantityEditText.getText().toString();
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setData(Uri.parse("mailto:"));
                i.putExtra(Intent.EXTRA_SUBJECT, R.string.order_option + ": " + name);
                i.putExtra(Intent.EXTRA_TEXT, R.string.order_option + ": " + name
                        + "\n" + R.string.quantity + ": " + quantity);
                startActivity(i);
                return true;
            case R.id.delete_product:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:

                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(NewProduct.this);
                    return true;
                } else {
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    NavUtils.navigateUpFromSameTask(NewProduct.this);
                                }
                            };

                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_INFO,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_IMAGE
        };

        return new CursorLoader(this,
                currentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int infoColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_INFO);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            String productName = cursor.getString(nameColumnIndex);
            String productInfo = cursor.getString(infoColumnIndex);
            String productPrice = cursor.getString(priceColumnIndex);
            int productQuantity = cursor.getInt(quantityColumnIndex);
            final String imageUri = cursor.getString(imageColumnIndex);

            nameEditText.setText(productName);
            infoEditText.setText(productInfo);
            priceEditText.setText(productPrice);
            quantityEditText.setText(Integer.toString(productQuantity));
            ViewTreeObserver viewTreeObserver = addImage.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    uri = Uri.parse(imageUri);
                    addImage.setImageBitmap(getImageBitMap(uri));


                }
            });


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText("");
        infoEditText.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_back);
        builder.setPositiveButton(R.string.dialog_back_positive, discardButtonClickListener);
        builder.setNegativeButton(R.string.dialog_back_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_delete);
        builder.setPositiveButton(R.string.dialog_delete_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.dialog_delete_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (currentProductUri != null) {

            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.dialog_delete_error,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.dialog_delete_no_error,
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                uri = data.getData();
                Log.i("picture", "Uri: " + uri.toString());
                addImage.setImageBitmap(getImageBitMap(uri));
            }
        }
    }

    public Bitmap getImageBitMap(Uri uri) {
        int width = addImage.getWidth();
        int height = addImage.getHeight();
        InputStream input;

        try {
            input = this.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            int photoW = options.outWidth;
            int photoH = options.outHeight;
            input.close();

            width = (int) Math.ceil(photoW / (float) width);
            height = (int) Math.ceil(photoH / (float) height);
            if (height > 1 || width > 1) {
                if (height > width) {
                    options.inSampleSize = height;
                } else {
                    options.inSampleSize = width;
                }
            }

            options.inJustDecodeBounds = false;
            input = this.getContentResolver().openInputStream(uri);
            Bitmap bMap = BitmapFactory.decodeStream(input, null, options);
            input.close();
            return bMap;

        } catch (Exception e) {
            Log.e("Error", e.toString());
            return null;
        }
    }
}


