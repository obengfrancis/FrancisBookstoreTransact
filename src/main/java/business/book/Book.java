package business.book;

public class Book {

	private final long bookId;
	private final String title;
	private final String author;
	private final int price;
	private final boolean isPublic;
	private final long categoryId;

	public Book(long bookId, String title, String author, int price, boolean isPublic, long categoryId) {
		this.bookId = bookId;
		this.title = title;
		this.author = author;
		this.price = price;
		this.isPublic = isPublic;
		this.categoryId = categoryId;
	}

	public long getBookId() {
		return bookId;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}

	public int getPrice() {
		return price;
	}

	public boolean getIsPublic() {
		return isPublic;
	}

	public long getCategoryId() {
		return categoryId;
	}
}
