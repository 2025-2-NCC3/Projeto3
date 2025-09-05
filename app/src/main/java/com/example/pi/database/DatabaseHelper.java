package com.example.pi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pi.models.Order;
import com.example.pi.models.OrderItem;
import com.example.pi.models.OrderStatus;
import com.example.pi.models.Product;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CantinaApp.db";
    private static final int DATABASE_VERSION = 2;

    // Tabelas
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_ORDER_ITEMS = "order_items";

    // Colunas comuns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_PRICE = "price";

    // Colunas específicas para produtos
    private static final String COLUMN_STOCK = "stock";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_IMAGE_URL = "image_url";

    // Colunas específicas para pedidos
    private static final String COLUMN_ORDER_ID = "order_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_ORDER_DATE = "order_date";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_TOTAL_AMOUNT = "total_amount";
    private static final String COLUMN_UNIQUE_CODE = "unique_code";

    // Colunas específicas para itens do pedido
    private static final String COLUMN_ITEM_ID = "item_id";
    private static final String COLUMN_PRODUCT_ID = "product_id";
    private static final String COLUMN_PRODUCT_NAME = "product_name";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_UNIT_PRICE = "unit_price";
    private static final String COLUMN_SUBTOTAL = "subtotal";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Criar tabela de produtos
        String createProductsTable = "CREATE TABLE " + TABLE_PRODUCTS + "(" +
                COLUMN_ID + " TEXT PRIMARY KEY," +
                COLUMN_NAME + " TEXT NOT NULL," +
                COLUMN_DESCRIPTION + " TEXT," +
                COLUMN_PRICE + " REAL NOT NULL," +
                COLUMN_STOCK + " INTEGER DEFAULT 0," +
                COLUMN_CATEGORY + " TEXT," +
                COLUMN_IMAGE_URL + " TEXT)";

        // Criar tabela de pedidos
        String createOrdersTable = "CREATE TABLE " + TABLE_ORDERS + "(" +
                COLUMN_ORDER_ID + " TEXT PRIMARY KEY," +
                COLUMN_USER_ID + " TEXT," +
                COLUMN_USER_NAME + " TEXT," +
                COLUMN_ORDER_DATE + " INTEGER," +
                COLUMN_STATUS + " TEXT," +
                COLUMN_TOTAL_AMOUNT + " REAL," +
                COLUMN_UNIQUE_CODE + " TEXT)";

        // Criar tabela de itens do pedido
        String createOrderItemsTable = "CREATE TABLE " + TABLE_ORDER_ITEMS + "(" +
                COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_ORDER_ID + " TEXT," +
                COLUMN_PRODUCT_ID + " TEXT," +
                COLUMN_PRODUCT_NAME + " TEXT," +
                COLUMN_QUANTITY + " INTEGER," +
                COLUMN_UNIT_PRICE + " REAL," +
                COLUMN_SUBTOTAL + " REAL," +
                "FOREIGN KEY(" + COLUMN_ORDER_ID + ") REFERENCES " +
                TABLE_ORDERS + "(" + COLUMN_ORDER_ID + "))";

        db.execSQL(createProductsTable);
        db.execSQL(createOrdersTable);
        db.execSQL(createOrderItemsTable);

        // Inserir alguns produtos de exemplo
        insertSampleProducts(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop das tabelas existentes
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);

        // Recriar as tabelas
        onCreate(db);
    }

    // ========== MÉTODOS PARA PRODUTOS ==========

    public long addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ID, product.getId());
        values.put(COLUMN_NAME, product.getName());
        values.put(COLUMN_DESCRIPTION, product.getDescription());
        values.put(COLUMN_PRICE, product.getPrice());
        values.put(COLUMN_STOCK, product.getStock());
        values.put(COLUMN_CATEGORY, product.getCategory());
        values.put(COLUMN_IMAGE_URL, product.getImageUrl());

        long result = db.insert(TABLE_PRODUCTS, null, values);
        db.close();
        return result;
    }

    public Product getProductById(String productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Product product = null;

        Cursor cursor = db.query(TABLE_PRODUCTS, null,
                COLUMN_ID + " = ?", new String[]{productId},
                null, null, null);

        if (cursor.moveToFirst()) {
            product = cursorToProduct(cursor);
        }

        cursor.close();
        db.close();
        return product;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, COLUMN_NAME);

        if (cursor.moveToFirst()) {
            do {
                products.add(cursorToProduct(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return products;
    }

    public List<Product> getProductsByCategory(String category) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PRODUCTS, null,
                COLUMN_CATEGORY + " = ?", new String[]{category},
                null, null, COLUMN_NAME);

        if (cursor.moveToFirst()) {
            do {
                products.add(cursorToProduct(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return products;
    }

    public int updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, product.getName());
        values.put(COLUMN_DESCRIPTION, product.getDescription());
        values.put(COLUMN_PRICE, product.getPrice());
        values.put(COLUMN_STOCK, product.getStock());
        values.put(COLUMN_CATEGORY, product.getCategory());
        values.put(COLUMN_IMAGE_URL, product.getImageUrl());

        int result = db.update(TABLE_PRODUCTS, values,
                COLUMN_ID + " = ?", new String[]{product.getId()});
        db.close();
        return result;
    }

    public int updateProductStock(String productId, int newStock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_STOCK, newStock);

        int result = db.update(TABLE_PRODUCTS, values,
                COLUMN_ID + " = ?", new String[]{productId});
        db.close();
        return result;
    }

    public int deleteProduct(String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PRODUCTS,
                COLUMN_ID + " = ?", new String[]{productId});
        db.close();
        return result;
    }

    public boolean hasSufficientStock(String productId, int requestedQuantity) {
        Product product = getProductById(productId);
        return product != null && product.getStock() >= requestedQuantity;
    }

    // ========== MÉTODOS PARA PEDIDOS ==========

    public long addOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ORDER_ID, order.getOrderId());
        values.put(COLUMN_USER_ID, order.getUserId());
        values.put(COLUMN_USER_NAME, order.getUserName());
        values.put(COLUMN_ORDER_DATE, order.getOrderDate().getTime());
        values.put(COLUMN_STATUS, order.getStatus().name());
        values.put(COLUMN_TOTAL_AMOUNT, order.getTotalAmount());
        values.put(COLUMN_UNIQUE_CODE, order.getUniqueCode());

        long result = db.insert(TABLE_ORDERS, null, values);

        // Adicionar itens do pedido
        if (result != -1 && order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                addOrderItem(order.getOrderId(), item);
            }
        }

        db.close();
        return result;
    }

    private long addOrderItem(String orderId, OrderItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ORDER_ID, orderId);
        values.put(COLUMN_PRODUCT_ID, item.getProductId());
        values.put(COLUMN_PRODUCT_NAME, item.getProductName());
        values.put(COLUMN_QUANTITY, item.getQuantity());
        values.put(COLUMN_UNIT_PRICE, item.getUnitPrice());
        values.put(COLUMN_SUBTOTAL, item.getSubtotal());

        long result = db.insert(TABLE_ORDER_ITEMS, null, values);
        db.close();
        return result;
    }

    public Order getOrderById(String orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Order order = null;

        Cursor cursor = db.query(TABLE_ORDERS, null,
                COLUMN_ORDER_ID + " = ?", new String[]{orderId},
                null, null, null);

        if (cursor.moveToFirst()) {
            order = cursorToOrder(cursor);
            // Buscar itens do pedido
            if (order != null) {
                order.setItems(getOrderItems(orderId));
            }
        }

        cursor.close();
        db.close();
        return order;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ORDERS, null, null, null, null, null, COLUMN_ORDER_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Order order = cursorToOrder(cursor);
                // Buscar itens do pedido
                if (order != null) {
                    order.setItems(getOrderItems(order.getOrderId()));
                }
                orders.add(order);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return orders;
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ORDERS, null,
                COLUMN_STATUS + " = ?", new String[]{status.name()},
                null, null, COLUMN_ORDER_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Order order = cursorToOrder(cursor);
                // Buscar itens do pedido
                if (order != null) {
                    order.setItems(getOrderItems(order.getOrderId()));
                }
                orders.add(order);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return orders;
    }

    public List<Order> getOrdersByUser(String userId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ORDERS, null,
                COLUMN_USER_ID + " = ?", new String[]{userId},
                null, null, COLUMN_ORDER_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Order order = cursorToOrder(cursor);
                // Buscar itens do pedido
                if (order != null) {
                    order.setItems(getOrderItems(order.getOrderId()));
                }
                orders.add(order);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return orders;
    }

    public int updateOrderStatus(String orderId, OrderStatus status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_STATUS, status.name());

        return db.update(TABLE_ORDERS, values,
                COLUMN_ORDER_ID + " = ?", new String[]{orderId});
    }

    public int deleteOrder(String orderId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Primeiro deletar os itens do pedido
        db.delete(TABLE_ORDER_ITEMS, COLUMN_ORDER_ID + " = ?", new String[]{orderId});

        // Depois deletar o pedido
        int result = db.delete(TABLE_ORDERS, COLUMN_ORDER_ID + " = ?", new String[]{orderId});
        db.close();
        return result;
    }

    private List<OrderItem> getOrderItems(String orderId) {
        List<OrderItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ORDER_ITEMS, null,
                COLUMN_ORDER_ID + " = ?", new String[]{orderId},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                OrderItem item = new OrderItem();
                item.setProductId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)));
                item.setProductName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)));
                item.setUnitPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_UNIT_PRICE)));

                items.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return items;
    }

    // ========== MÉTODOS AUXILIARES ==========

    private Product cursorToProduct(Cursor cursor) {
        Product product = new Product();
        product.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
        product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)));
        product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STOCK)));
        product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
        product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)));
        return product;
    }

    private Order cursorToOrder(Cursor cursor) {
        Order order = new Order();
        order.setOrderId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID)));
        order.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
        order.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
        order.setOrderDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ORDER_DATE))));
        order.setStatus(OrderStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))));
        order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_AMOUNT)));
        order.setUniqueCode(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_CODE)));
        return order;
    }

    // ========== MÉTODO PARA INSERIR PRODUTOS DE EXEMPLO ==========

    private void insertSampleProducts(SQLiteDatabase db) {
        // Produtos de exemplo
        String[][] sampleProducts = {
                {"1", "X-Burger", "Hambúrguer com queijo e alface", "15.90", "50", "Lanches", ""},
                {"2", "X-Salada", "Hambúrguer com queijo, alface e tomate", "18.90", "40", "Lanches", ""},
                {"3", "X-Bacon", "Hambúrguer com queijo, bacon e alface", "22.90", "35", "Lanches", ""},
                {"4", "Refrigerante Lata", "Lata 350ml", "6.50", "100", "Bebidas", ""},
                {"5", "Suco Natural", "Suco de laranja ou limão 500ml", "8.90", "60", "Bebidas", ""},
                {"6", "Água Mineral", "Garrafa 500ml", "4.50", "80", "Bebidas", ""},
                {"7", "Batata Frita", "Porção média com ketchup", "12.90", "30", "Acompanhamentos", ""},
                {"8", "Porção de Nuggets", "10 unidades com molho", "16.90", "25", "Acompanhamentos", ""},
                {"9", "Sorvete", "Casquinha de chocolate ou baunilha", "7.90", "45", "Sobremesas", ""},
                {"10", "Brownie", "Brownie de chocolate com nuts", "9.90", "20", "Sobremesas", ""}
        };

        for (String[] productData : sampleProducts) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, productData[0]);
            values.put(COLUMN_NAME, productData[1]);
            values.put(COLUMN_DESCRIPTION, productData[2]);
            values.put(COLUMN_PRICE, Double.parseDouble(productData[3]));
            values.put(COLUMN_STOCK, Integer.parseInt(productData[4]));
            values.put(COLUMN_CATEGORY, productData[5]);
            values.put(COLUMN_IMAGE_URL, productData[6]);

            db.insert(TABLE_PRODUCTS, null, values);
        }
    }

    // ========== MÉTODOS PARA LIMPEZA E RESET ==========

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ORDER_ITEMS, null, null);
        db.delete(TABLE_ORDERS, null, null);
        db.delete(TABLE_PRODUCTS, null, null);
        db.close();

        // Reinserir produtos de exemplo
        insertSampleProducts(getWritableDatabase());
    }

    public int getOrdersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public int getProductsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }
}