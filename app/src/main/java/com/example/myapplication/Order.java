package.com.example.myapplication;
import java.util.Date;

public class Order {
    private String orderId;
    private String userName;
    private Date orderDate;
    private double totalAmount;


    public void addItem(OrderItem item) {
        items.add(item);
        totalAmount += item.getSubtotal();
    }


}