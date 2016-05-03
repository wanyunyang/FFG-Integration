package views.forms;

import models.School;
import play.data.validation.ValidationError;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;

public class SchoolRegForm extends UserForm {

    public SchoolRegForm(){
        //this.discriminator = "admin";
        this.university = "n/a";
        this.occupation = "n/a";
        this.profile = "n/a";
        this.employer = "n/a";
        this.industry = "n/a";
    }

    /**
     * Validate function. This is called when we bind a form from the request. It checks the form fields for any errors.
     * If there is an error then it is added to the errors list which can be used to help the User know what they
     * did incorrectly
     * @return A List of ValidationErrors if there were errors in the form, otherwise null.
     */
    @Override
    public List<ValidationError> validate() {
        List<ValidationError> perrors = super.validate();
        List<ValidationError> errors = new ArrayList<ValidationError>();
        if(perrors != null) errors.addAll(perrors);

        if(password == null || password.equals(""))
            errors.add(new ValidationError("password", "No password provided."));

        if(school == null)
            errors.add(new ValidationError("school", "No school provided."));

        /*if(address == null)
            errors.add(new ValidationError("address", "No address provided."));

        if(postcode == null)
            errors.add(new ValidationError("postcode", "No school postcode provided."));

        if(schoolURN == null)
            errors.add(new ValidationError("schoolURN", "No school URN provided."));

        if(profile == null || profile.equals(""))
            errors.add(new ValidationError("profile", "No profile information provided."));*/

        if(!errors.isEmpty()) return errors;
        return null;
    }
}
