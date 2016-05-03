package views.forms;

import models.Category;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the data when doing Video forms
 */
public class VideoForm {
    /**
     * The title of the Video
     */
    public String title = "";
    /**
     * The description of the Video
     */
    public String description = "";
    /**
     * The categories associated with this Video
     */
    public List<Category> categories = new ArrayList<Category>();
    /**
     * String representing the publicaccess boolean of the Video. "Yes" is true and "No" is false.
     */
    public String publicaccess = "";


    /**
     * Constructor for VideoForm
     */
    public VideoForm() {}

    /**
     * Constructor for VideoForm
     * @param title The title of the Video we are creating/editing with this form
     * @param description The description of the Video we are creating/editing with this form
     * @param categories The List of Categories associated with the Video we are creating/editing with this form
     * @param publicaccess The publicaccess field of the Video we are creating/editing with this form
     */
    public VideoForm(String title, String description, List<Category> categories, boolean publicaccess) {
        this.title = title;
        this.description = description;
        for(Category c : categories) {
            this.categories.add(c);
        }
        if (publicaccess) {
            this.publicaccess = "Yes";
        } else {
            this.publicaccess = "No";
        }
    }

    /**
     * Validate function. This is called when we bind a form from the request. It checks the form fields for any errors.
     * If there is an error then it is added to the errors list which can be used to help the User know what they
     * did incorrectly. Null checks on all the String fields and a check on the publicaccess field to ensure it is either
     * "Yes" or "No".
     * @return A List of ValidationErrors if there were errors in the form, otherwise null.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<ValidationError>();

        if (title == null || title.length() == 0) {
            errors.add(new ValidationError("title", "No video title was given"));
        }
        if (description == null || description.length() == 0) {
            errors.add(new ValidationError("description", "No video description was given"));
        }

        if (publicaccess == null || publicaccess.equals("")) {
            errors.add(new ValidationError("publicaccess", "No public access type was given"));
        } else if (!publicaccess.equals("Yes")
                && !publicaccess.equals("No")) {
            errors.add(new ValidationError("publicaccess", "Invalid public access type given"));
        }

        if (errors.size() > 0) {
            return errors;
        }

        return null;
    }
}
