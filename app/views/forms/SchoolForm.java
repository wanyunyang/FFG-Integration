package views.forms;

import models.SchoolDAO;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the data when doing School forms
 */
public class SchoolForm {
    /**
     * The name of the School
     */
    public String name = "";

    /**
     * Constructor for SchoolForm
     */
    public SchoolForm() {}

    /**
     * Constructor for SchoolForm. Sets the name of the school to name.
     * @param name The name of the School being created/edited by this form.
     */
    public SchoolForm(String name) {
        this.name = name;
    }

    /**
     * Validate function. This is called when we bind a form from the request. It checks the form fields for any errors.
     * If there is an error then it is added to the errors list which can be used to help the User know what they
     * did incorrectly. The School is checked against the database to ensure that two Schools can't have the same name.
     * @return A List of ValidationErrors if there were errors in the form, otherwise null.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<ValidationError>();

        SchoolDAO sdao = new SchoolDAO();

        if (name == null || name.length() == 0) {
            errors.add(new ValidationError("name", "No name was given"));
        } if (sdao.byName(name) != null) {
            errors.add(new ValidationError("name","A school with that name already exists"));
        }

        if (errors.size() > 0) {
            return errors;
        }

        return null;
    }
}
