package views.forms;

import models.Category;

import java.util.List;

/**
 * Holds the data when doing Category selection forms
 */
public class CategorySelectionForm {
    /**
     * The list of Categories that have been selected in this form.
     */
    public List<Category> categories;

    /**
     * Constructor for CategorySelectionForm
     * @param categories The List of Categories that have been selected in this form.
     */
    public CategorySelectionForm(List<Category> categories) {
        this.categories = categories;
    }

    public CategorySelectionForm() {}
}
