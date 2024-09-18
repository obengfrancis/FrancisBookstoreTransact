package business.order;

import api.ApiException;
import business.BookstoreDbException;
import business.JdbcUtils;
import business.book.Book;
import business.book.BookDao;
import business.cart.ShoppingCart;
import business.cart.ShoppingCartItem;
import business.customer.Customer;
import business.customer.CustomerDao;
import business.customer.CustomerForm;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class DefaultOrderService implements OrderService {

	private BookDao bookDao;

	private LineItemDao lineItemDao;

	private CustomerDao customerDao;

	public void setLineItemDao(LineItemDao lineItemDao) {
		this.lineItemDao = lineItemDao;
	}

	public void setCustoomerDao(CustomerDao custoomerDao) {
		this.customerDao = custoomerDao;
	}

	public void setOrderDao(OrderDao orderDao) {
		this.orderDao = orderDao;
	}

	private OrderDao orderDao;

	public void setBookDao(BookDao bookDao) {
		this.bookDao = bookDao;
	}

	@Override
	public OrderDetails getOrderDetails(long orderId) {
		Order order = orderDao.findByOrderId(orderId);
		Customer customer = customerDao.findByCustomerId(order.getCustomerId());
		List<LineItem> lineItems = lineItemDao.findByOrderId(orderId);
		List<Book> books = lineItems
				.stream()
				.map(lineItem -> bookDao.findByBookId(lineItem.getBookId()))
				.collect(Collectors.toList());
		return new OrderDetails(order, customer, lineItems, books);
	}

	@Override
    public long placeOrder(CustomerForm customerForm, ShoppingCart cart) {

		validateCustomer(customerForm);
		validateCart(cart);

		try (Connection connection = JdbcUtils.getConnection()) {
			Date date = getDate(
					customerForm.getCcExpiryMonth(),
					customerForm.getCcExpiryYear());
			return performPlaceOrderTransaction(
					customerForm.getName(),
					customerForm.getAddress(),
					customerForm.getPhone(),
					customerForm.getEmail(),
					customerForm.getCcNumber(),
					date, cart, connection);
		} catch (SQLException e) {
			throw new BookstoreDbException("Error during close connection for customer order", e);
		}
	}

	private Date getDate(String monthString, String yearString) {
		try {
			int month = Integer.parseInt(monthString);
			int year = Integer.parseInt(yearString);

			YearMonth expirationDate = YearMonth.of(year, month);
			if (expirationDate.isBefore(YearMonth.now())) {
				throw new ApiException.ValidationFailure("ccExpiryMonth", "Credit card has expired");
			}

			return Date.valueOf(expirationDate.atEndOfMonth());
		} catch (NumberFormatException | DateTimeException e) {
			throw new ApiException.ValidationFailure("ccExpiryMonth", "Invalid credit card expiration date");
		}
	}



	private long performPlaceOrderTransaction(
			String name, String address, String phone,
			String email, String ccNumber, Date date,
			ShoppingCart cart, Connection connection) {
		try {
			connection.setAutoCommit(false);
			long customerId = customerDao.create(
					connection, name, address, phone, email,
					ccNumber, date);
			long customerOrderId = orderDao.create(
					connection,
					cart.getComputedSubtotal() + cart.getSurcharge(),
					generateConfirmationNumber(), customerId);
			for (ShoppingCartItem item : cart.getItems()) {
				lineItemDao.create(connection, customerOrderId,
						item.getBookId(), item.getQuantity());
			}
			connection.commit();
			return customerOrderId;
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new BookstoreDbException("Failed to roll back transaction", e1);
			}
			return 0;
		}
	}

	private int generateConfirmationNumber() {
		int min = 100000000;
		int max = 999999999;
		Random random = new Random();
		int confirmationNumber = random.nextInt(max - min + 1) + min;
		return confirmationNumber;

	}


	private void validateCustomer(CustomerForm customerForm) {

    	String name = customerForm.getName();

		if (name == null || name.equals("") || name.length() < 4 || name.length() > 45) {
			throw new ApiException.ValidationFailure( "name", "Invalid name field");
		}

		String address = customerForm.getAddress();
		if (address == null || address.equals("") || address.length() < 4 || address.length() > 45  ){
			throw new ApiException.ValidationFailure("address", "Invalid address field");
		}

		String phone = customerForm.getPhone();
		if (phone == null || phone.equals("")) {
			throw new ApiException.ValidationFailure("Missing phone field");
		} else {
			String digits = phone.replaceAll("\\D", "");
			if (digits.length() != 10) {
				throw new ApiException.ValidationFailure("Invalid phone field");
			}
		}

		String email = customerForm.getEmail();
		if (email == null || email.isEmpty() || email.equals("") || !email.contains("@") || email.endsWith(".")) {
			throw new ApiException.ValidationFailure("email", "Invalid email field");
		}

		String ccNumber = customerForm.getCcNumber();
		String cleanedCcNumber = ccNumber.replaceAll("[\\s-]+", "");

		if (ccNumber == null || cleanedCcNumber.equals("") || cleanedCcNumber.length() < 14 || cleanedCcNumber.length() > 16) {
			throw new ApiException.ValidationFailure("ccNumber", "Invalid credit card number");
		}


		if (expiryDateIsInvalid(customerForm.getCcExpiryMonth(), customerForm.getCcExpiryYear())) {
			throw new ApiException.ValidationFailure("Please enter a valid expiration date.");

		}
	}

	private boolean expiryDateIsInvalid(String ccExpiryMonth, String ccExpiryYear) {
		try {
			int month = Integer.parseInt(ccExpiryMonth);
			int year = Integer.parseInt(ccExpiryYear);

			YearMonth ccExpiryDate = YearMonth.of(year, month);

			return YearMonth.now().isAfter(ccExpiryDate);
		} catch (NumberFormatException | NullPointerException | DateTimeException ex) {
			return true;
		}
	}

	private void validateCart(ShoppingCart cart) {

		if (cart.getItems().size() <= 0) {
			throw new ApiException.ValidationFailure("Cart is empty.");
		}

		cart.getItems().forEach(item-> {
			if (item.getQuantity() < 0 || item.getQuantity() > 99) {
				throw new ApiException.ValidationFailure("Invalid quantity");
			}
			Book databaseBook = bookDao.findByBookId(item.getBookId());

			if (databaseBook == null) {
				throw new ApiException.ValidationFailure("Invalid book");
			}
			if (item.getPrice() != databaseBook.getPrice()) {
				throw new ApiException.ValidationFailure("Price mismatch for book with ID " + item.getBookId());
			}

			if (item.getCategoryId() != databaseBook.getCategoryId()) {
				throw new ApiException.ValidationFailure("Invalid category for selected book");
			}
		});

	}

}
