package domain;

public interface libraryItems {

    String getAuthor();
    String getName();
    libraryType getType();
    int getQuantity();
    String getISBN();

    void setAuthor(String author);
    void setName(String name);
    void setType(libraryType type);
    void setQuantity(int quantity);
    void setISBN(String isbn);
}
