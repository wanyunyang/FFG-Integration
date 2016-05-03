package views.forms;

import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the data when doing login forms
 */
public class LoginForm {
    /**
     * The email of the User trying to login
     */
    public String email;
    /**
     * The password of the User trying to login
     */
    public String password;

    public LoginForm() {}

    /**
     * Validate function. This is called when we bind a form from the request. It checks the form fields for any errors.
     * If there is an error then it is added to the errors list which can be used to help the User know what they
     * did incorrectly
     * @return A List of ValidationErrors if there were errors in the form, otherwise null.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<ValidationError>();

        if (email == null || email.length() == 0) {
            errors.add(new ValidationError("login", "No login was given"));
        }
        if (password == null || password.length() == 0) {
            errors.add(new ValidationError("password", "No password was given"));
        }

        if (errors.size() > 0) {
            return errors;
        }

        return null;
    }
}
