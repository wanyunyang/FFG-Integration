package views.forms;

import models.School;
import models.SchoolDAO;
import models.User;
import models.UserDAOImpl;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the data when doing User forms
 */
public class UserForm
{
    /**
     * The name of the User
     */
    public String name = "";
    /**
     * The password of the User
     */
    public String password = "";
    /**
     * The email of the User
     */
    public String email = "";
    /**
     * The School the User is associated with
     */
    public School school = null;
    /**
     * The User ID of the User
     */
    public Long id = 0l;
    /**
     * The type of User of the User
     */
    public String discriminator = "student";

    public String university = "";

    public String occupation = "";

    public String industry = "";

    public String employer = "";
    /**
     * The profile of the User. (this is only ever not null for Alumnis)
     */
    public String profile = null;

    /**
     * Constructor for UserForm
     */
    public UserForm()
    {
    }

    /**
     * Constructor for UserForm
     *
     * @param school The School of the User being edited/created by this form
     */
    public UserForm(School school)
    {
        this.school = school;
    }

    public UserForm(String name, String password, String email, School school, String discriminator, String profile, Long id, String university, String occupation, String industry, String employer)
    {
        this.name = name;
        this.password = password;
        this.email = email;
        this.school = school;
        this.discriminator = discriminator;
        this.profile = profile;
        this.id = id;
        this.occupation = occupation;
        this.industry = industry;
        this.employer = employer;
        this.university = university;
    }

    /**
     * Constructor for UserForm
     *
     * @param name          The name of the User being edited/created by this form
     * @param password      The password of the User being edited/created by this form
     * @param email         The email of the User being edited/created by this form
     * @param school        The school of the User being edited/created by this form
     * @param discriminator The type of the User being edited/created by this form
     * @param profile       The profile of the User being edited/created by this form
     * @param id            The User ID of the User being edited/created by this form
     */
    public UserForm(String name, String password, String email, School school, String discriminator, String profile, Long id)
    {
        this(name, password, email, school, discriminator, profile, id, null, null, null, null);
    }

    /**
     * Validate function. This is called when we bind a form from the request. It checks the form fields for any errors.
     * If there is an error then it is added to the errors list which can be used to help the User know what they
     * did incorrectly. Password is not checked for null/empty String here because leaving password field empty is used
     * to indicate either "create random password" or "leave password the same". Email checks that the email is not null/empty,
     * that the email is in a valid format and that there isn't already a User in the database with that email address (provided that
     * the email that conflicts isn't the Users current email). School is checked from the database too.
     *
     * @return A List of ValidationErrors if there were errors in the form, otherwise null.
     */
    public List<ValidationError> validate()
    {
        List<ValidationError> errors = new ArrayList<>();
        UserDAOImpl udao = new UserDAOImpl();

        if (name == null || name.length() == 0)
        {
            errors.add(new ValidationError("name", "No name was given"));
        }

        if (password != null && !password.equals("") && password.length() < 8)
        {
            errors.add(new ValidationError("password", "Please enter a longer password so you account is more secure!"));
        }

        //found a nice email regex credit: http://www.regular-expressions.info/email.html.
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(
                "\\b[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\b");

        User user = udao.getUser(id);

        if (email == null || email.length() == 0)
        {
            errors.add(new ValidationError("email", "No email was given"));
        } else if (!regex.matcher(email).matches())
        {
            errors.add(new ValidationError("email", "Invalid email was given"));
        } else if (user != null && !user.getEmail().equals(email) && udao.getUserByEmail(email) != null)
        {
            errors.add(new ValidationError("email", "A user already exists with that email address"));
        } else if (user == null && udao.getUserByEmail(email) != null)
        {
            errors.add(new ValidationError("email", "A user already exists with that email address"));
        }

        if (discriminator == null || discriminator.equals(""))
        {
            errors.add(new ValidationError("discriminator", "No user type was given"));
        } else if (!discriminator.equals("student") && !discriminator.equals("alumni") && !discriminator.equals("admin") && !discriminator
                .equals("superadmin"))
        {
            errors.add(new ValidationError("discriminator", "Invalid user type given"));
        }

        SchoolDAO sdao = new SchoolDAO();
        if (school != null)
        {
            if (sdao.byName(school.getName()) == null)
            {
                errors.add(new ValidationError("school", "Invalid school provided"));
            }
        }

        if (discriminator != null && discriminator.length() != 0 && discriminator.equals("student"))
        {
            addErrorIfEmpty(occupation, "occupation", errors);
            addErrorIfEmpty(university, "university", errors);
            addErrorIfEmpty(employer, "employed", errors);
            addErrorIfEmpty(industry, "industry", errors);
        }

        if (errors.size() > 0)
        {
            return errors;
        }

        return null;
    }

    private void addErrorIfEmpty(String field, String fieldName, List<ValidationError> errors)
    {
        if (field == null || field.length() == 0)
        {
            errors.add(new ValidationError(fieldName, "No " + fieldName + " was given"));
        }
    }
}
