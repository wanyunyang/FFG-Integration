package views.forms;

import models.School;
import models.SchoolDAO;
import models.UserDAOImpl;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the data when doing bulk registation functions
 */
public class BulkRegisterForm {
    /**
     * The School we will be adding the users to
     */
    public School school = null;
    /**
     * The list of emails (seperated by new line literals) of the Users we want to register
     */
    public String data = "";
    /**
     * The type of user we want to create in bulk
     */
    public String discriminator = "student";

    /**
     * Constructor for BulkRegisterForm
     */
    public BulkRegisterForm() {}

    /**
     * Contructor for BulkRegisterForm.
     * @param school The School we want to bulk register Users for
     */
    public BulkRegisterForm(School school) {
        this.school = school;
    }

    /**
     * Validate function. This is called when we bind a form from the request. It checks the form fields for any errors.
     * If there is an error then it is added to the errors list which can be used to help the User know what they
     * did incorrectly
     * @return A List of ValidationErrors if there were errors in the form, otherwise null.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        SchoolDAO sdao = new SchoolDAO();
        if (school != null) {
            if(sdao.byName(school.getName()) == null) {
                errors.add(new ValidationError("school", "Invalid school provided"));
            }
        }

        if (data == null || data.equals("")) {
            errors.add(new ValidationError("data","No email addresses given"));
        }

        if (discriminator == null || discriminator.equals("")) {
            errors.add(new ValidationError("discriminator", "No user type was given"));
        } else if (!discriminator.equals("student")
                && !discriminator.equals("alumni")
                && !discriminator.equals("admin")
                && !discriminator.equals("superadmin")) {
            errors.add(new ValidationError("discriminator", "Invalid user type given"));
        }

        if (errors.size() > 0) {
            return errors;
        }

        return null;
    }
}
