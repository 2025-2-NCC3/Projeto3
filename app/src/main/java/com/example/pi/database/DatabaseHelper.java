package com.example.pi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pi.models.Order;
import com.example.pi.models.OrderItem;
import com.example.pi.models.OrderStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CantinaApp.db";
    private static final int DATABASE_VERSION = 2;

    // Tabelas
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String TABLE_PRODUCTS = "products";

    // Colunas para tabela de pedidos
    private static final String COLUMN_ORDER_ID = "order_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_ORDER_DATE = "order_date";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_TOTAL_AMOUNT = "total_amount";
    private static final String COLUMN_UNIQUE_CODE = "unique_code";

    // Colunas para tabela de itens do pedido
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

        db.execSQL(createOrdersTable);
        db.execSQL(createOrderItemsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        onCreate(db);
    }

    // Métodos para operações com pedidos
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

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ORDERS, null, null, null, null, null, COLUMN_ORDER_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setOrderId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID)));
                order.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
                order.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
                order.setOrderDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ORDER_DATE))));
                order.setStatus(OrderStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))));
                order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_AMOUNT)));
                order.setUniqueCode(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_CODE)));

                // Buscar itens do pedido
                order.setItems(getOrderItems(order.getOrderId()));

                orders.add(order);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return orders;
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
}