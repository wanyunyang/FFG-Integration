package views.forms;

import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the data when doing Category forms
 */
public class CategoryForm {
    /**
     * The name of the Category
     */
    public String name = "";

    /**
     * Constructor for CategoryForm
     */
    public CategoryForm() {}

    /**
     * Constructor for CategoryForm. Sets the name field.
     * @param name The name of the Category being created/edited by this form.
     */
    public CategoryForm(String name) {
        this.name = name;
    }

    /**
     * Validate function. This is called when we bind a form from the request. It checks the form fields for any errors.
     * If there is an error then it is added to the errors list which can be used to help the User know what they
     * did incorrectly
     * @return A List of ValidationErrors if there were errors in the form, otherwise null.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<ValidationError>();

        if (name == null || name.length() == 0) {
            errors.add(new ValidationError("name", "No name was given"));
        }

        if (errors.size() > 0) {
            return errors;
        }

        return null;
    }
}
