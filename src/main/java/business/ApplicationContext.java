
package business;

import business.book.BookDao;
import business.book.BookDaoJdbc;
import business.category.CategoryDao;
import business.category.CategoryDaoJdbc;
import business.customer.CustomerDao;
import business.customer.CustomerDaoJdbc;
import business.order.*;

public class ApplicationContext {

    private  CategoryDao categoryDao;
    private BookDao bookDao;

    private OrderService orderService;

    private OrderDao orderDao;

    private LineItemDao lineItemDao;

    private CustomerDao customerDao;

    public static ApplicationContext INSTANCE = new ApplicationContext();

    private ApplicationContext() {

        categoryDao = new CategoryDaoJdbc();
        bookDao = new BookDaoJdbc();
       orderService = new DefaultOrderService();

        orderDao = new OrderDaoJdbc();
        lineItemDao = new LineItemDaoJdbc();
        customerDao = new CustomerDaoJdbc();
        ((DefaultOrderService)orderService).setBookDao(bookDao);
        ((DefaultOrderService)orderService).setOrderDao(orderDao);
        ((DefaultOrderService)orderService).setCustoomerDao(customerDao);
        ((DefaultOrderService)orderService).setLineItemDao(lineItemDao);
    }

    public CategoryDao getCategoryDao() {
        return categoryDao;
    }

    public BookDao getBookDao() { return bookDao; }

    public OrderService getOrderService() { return orderService; }

}
