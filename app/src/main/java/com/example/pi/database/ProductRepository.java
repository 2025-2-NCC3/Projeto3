package com.example.pi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.pi.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    private DatabaseHelper dbHelper;

    public ProductRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Adicionar um produto
    public long addProduct(Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("id", product.getId());
        values.put("name", product.getName());
        values.put("description", product.getDescription());
        values.put("price", product.getPrice());
        values.put("stock", product.getStock());
        values.put("category", product.getCategory());
        values.put("image_url", product.getImageUrl());

        long result = db.insert("products", null, values);
        db.close();
        return result;
    }

    // Buscar produto por ID
    public Product getProductById(String productId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Product product = null;

        Cursor cursor = db.query("products", null, "id = ?",
                new String[]{productId}, null, null, null);

        if (cursor.moveToFirst()) {
            product = cursorToProduct(cursor);
        }

        cursor.close();
        db.close();
        return product;
    }

    // Buscar todos os produtos
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("products", null, null, null, null, null, "name");

        if (cursor.moveToFirst()) {
            do {
                products.add(cursorToProduct(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return products;
    }

    // Buscar produtos por categoria
    public List<Product> getProductsByCategory(String category) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("products", null, "category = ?",
                new String[]{category}, null, null, "name");

        if (cursor.moveToFirst()) {
            do {
                products.add(cursorToProduct(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return products;
    }

    // Atualizar produto
    public int updateProduct(Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", product.getName());
        values.put("description", product.getDescription());
        values.put("price", product.getPrice());
        values.put("stock", product.getStock());
        values.put("category", product.getCategory());
        values.put("image_url", product.getImageUrl());

        int result = db.update("products", values, "id = ?", new String[]{product.getId()});
        db.close();
        return result;
    }

    // Atualizar estoque do produto
    public int updateProductStock(String productId, int newStock) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("stock", newStock);

        int result = db.update("products", values, "id = ?", new String[]{productId});
        db.close();
        return result;
    }

    // Deletar produto
    public int deleteProduct(String productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete("products", "id = ?", new String[]{productId});
        db.close();
        return result;
    }

    // Verificar se há estoque suficiente
    public boolean hasSufficientStock(String productId, int requestedQuantity) {
        Product product = getProductById(productId);
        return product != null && product.hasSufficientStock(requestedQuantity);
    }

    // Converter Cursor para Product
    private Product cursorToProduct(Cursor cursor) {
        Product product = new Product();
        product.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        product.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
        product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
        product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
        product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
        return product;
    }
}