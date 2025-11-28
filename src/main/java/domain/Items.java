package domain;
public class Items implements libraryItems {

    private String author;
    private String name;
    private libraryType type;
    private int quantity;
    private String isbn;

    public Items(String author, String name, libraryType type, int quantity, String isbn) {
        this.author = author;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.isbn = isbn;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public libraryType getType() {
        return type;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public String getISBN() {
        return isbn;
    }

    @Override
    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setType(libraryType type) {
        this.type = type;
    }

    @Override
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public void setISBN(String isbn) {
        this.isbn = isbn;
    }
}
