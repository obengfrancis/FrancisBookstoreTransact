package business.category;

import business.book.Book;

import java.util.List;

public interface CategoryDao {

    public List<Category> findAll();

    public Category findByCategoryId(long categoryId);

    public Category findByName(String categoryName);

    public List<Book> findRandomByCategoryName(String categoryName, int limit);
}
