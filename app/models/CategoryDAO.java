package models;

import java.util.List;

/**
 * Data access object for Question objects. It helps to perform database operations on the Question table in the database.
 */
public class CategoryDAO {

    /**
     * Constructor for CategoryDAO
     */
    public CategoryDAO() {}

    /**
     * Searches the database for the Category where the ID field is the same as id.
     * @param id The ID of the Category we want to retrieve.
     * @return The Category where id matches ID or null if no matches were found.
     */
    public Category getCategory(Long id) {
        return Category.find.byId(id);
    }

    /**
     * Searches by ID to find the Category and then deletes this Category from the database.
     * @param id The ID of the Category we want to delete.
     */
    public void deleteCategory(Long id) {
        Category.find.byId(id).delete();
    }

    /**
     * Gives a List of all of the Category objects in the database.
     * @return A List of all Category objects in the database.
     */
    public List<Category> getAllCategories() {
        return Category.find.all();
    }
}