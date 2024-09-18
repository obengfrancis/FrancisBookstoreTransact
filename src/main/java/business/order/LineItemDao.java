package business.order;

import java.sql.Connection;
import java.util.List;

public interface LineItemDao {

    public void create(Connection connection, long bookId, long orderId, int quantity);

    public List<LineItem> findByOrderId(long orderId);
}
