package business.cart;

import business.book.BookForm;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A shopping cart item arrives in an order form from the client.
 * This class holds the de-serialized JSON data for cart items.
 *
 * (We ignore any extra elements that the client sends
 *  that this class does not require, for example "_type").
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShoppingCartItem {

	private int quantity;

	@JsonProperty("book")
	private BookForm bookForm;

	public ShoppingCartItem() {
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BookForm getBookForm() {
		return bookForm;
	}

	public void setBookForm(BookForm bookForm) {
		this.bookForm = bookForm;
	}
	@JsonIgnore
	public long getBookId() {
		return bookForm.getBookId();
	}

	public int getPrice() {
		return bookForm.getPrice();
	}
	public long getCategoryId() {return bookForm.getCategoryId();
	}
}
