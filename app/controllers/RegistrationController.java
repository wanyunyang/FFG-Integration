package controllers;

import helpers.AdminHelpers;
import models.*;
import org.apache.commons.lang3.RandomStringUtils;
import play.data.*;
import static play.data.Form.*;
import play.libs.mailer.*;
import views.forms.AlumniRegForm;
import views.forms.LoginForm;
import views.forms.SchoolRegForm;
import views.html.*;
import views.html.emails.*;
import play.mvc.*;
import java.util.List;
import java.util.Map;

public class RegistrationController extends Controller {

    /**
     * Creates and sends emails with the purpose to notify the admins of newUser's that someone is waiting for approval
     * and emails the newUser that they are awaiting approval
     * @param newUser the Alumni user being registered
     */
    public static void newRegistrationEmail(Alumni newUser) {
        // Send email to user
        Email mail = new Email();
        mail.setSubject("Welcome to Careers From Here");
        mail.setFrom("Careers From Here <careersfromhere@gmail.com>");
        mail.addTo(newUser.getName() + " <" + newUser.getEmail() + ">");
        mail.setBodyHtml(registration_welcome.render(newUser).toString());
        MailerPlugin.send(mail);

        // Inform all admins
        List<Admin> admins = newUser.getSchool().getAdmins();
        for (Admin x : admins){
            Email adminMail = new Email();
            adminMail.setSubject("Careers From Here: New User Registration");
            adminMail.setFrom("Careers From Here <careersfromhere@gmail.com>");
            adminMail.addTo(x.getName() + " <" + x.getEmail() + ">");
            adminMail.setBodyHtml(registration_notify.render(newUser).toString());//todo
            MailerPlugin.send(adminMail);
        }
    }

    /**
     * Email that is sent to the newUser once their registration has been approved
     * @param newUser the user being approved
     */
    public static void userApprovedEmail(Alumni newUser) {
        // Send email to user
        Email mail = new Email();
        mail.setSubject("Careers From Here: Account Approved");
        mail.setFrom("Careers From Here <careersfromhere@gmail.com>");
        mail.addTo(newUser.getName() + " <" + newUser.getEmail() + ">");
        mail.setBodyHtml(registration_approved.render(newUser).toString());
        MailerPlugin.send(mail);
    }


    /**
     *
     * @return A new LoginForm with empty values
     */
    public static Result login(){
        Form<LoginForm> form = form(LoginForm.class).fill(new LoginForm());
        return ok(login.render(form));
    }

    /**
     * Used to log out the user, removes their email from the session
     * @return returns a redirect back to the homepage
     */
    public static Result logout() {
        session().remove("email");
        session().clear();
        return redirect("/");
    }

    /**
     * Used to check that the user exists and is approved before storing their email in the session and redirecting.
     * @return either a badREquest if there are errors in the LoginForm or a redirect result back to the homepage
     */
    public static Result authenticate(){
        Form<LoginForm> loginForm = form(LoginForm.class).bindFromRequest();

        if (loginForm.hasErrors()){
            return badRequest(login.render(loginForm));
        } else {
            LoginForm lf = loginForm.get();
            User u = User.authenticate(lf.email, lf.password);
            if(u == null) {
                flash("error", "Incorrect email and/or password.");
                return badRequest(login.render(loginForm));
            } else if (!u.getApproved()) {
                flash("error", "Your account has not been approved yet.");
                return badRequest(login.render(loginForm));
            } else {
                    session().clear();
                    session("email", lf.email);
                    return redirect("/");
            }
        }
    }

    /**
     *
     * @return An ok Result that renders the alumni registration page
     */
    public static Result getAlumniRegForm() {
        AlumniRegForm data = new AlumniRegForm();
        Form<AlumniRegForm> formdata = Form.form(AlumniRegForm.class).fill(data);

        Map<String, Boolean> schoolMap = AdminHelpers.ConstructSchoolMap(null,false);
        return ok(reg_alumni.render(formdata, schoolMap));
    }

    //TODO: ADDED Vivian
    public static Result getSchoolRegForm() {
        SchoolRegForm data = new SchoolRegForm();
        Form<SchoolRegForm> formdata = Form.form(SchoolRegForm.class).fill(data);

        Map<String, Boolean> schoolMap = AdminHelpers.ConstructSchoolMap(null,false);
        return ok(reg_school.render(formdata, schoolMap));
    }

    /**
     *
     * @return an ok Result that renders the reset_password page
     */
    public static Result getResetPassword(){
        Form<String> form = form(String.class);
        return ok(reset_password.render(form));
    }


    /**
     * Resets the password to a random AlphaNumeric 8 character string and emails the user concerned to notify them of their new password
     * @return a redirect Result that redirects to /login
     */
    public static Result resetPassword(){
        DynamicForm requestData = Form.form().bindFromRequest();
        String email = requestData.get("email");
        UserDAOImpl dao = new UserDAOImpl();
        User user = dao.getUserByEmail(email);
        String newpw = RandomStringUtils.randomAlphanumeric(8);
        user.setPassword(newpw);
        user.save();

        //now for the emailing
        Email mail = new Email();
        mail.setSubject("Careers From Here: Your password has been reset");
        mail.setFrom("Careers From Here <careersfromhere@gmail.com>");
        mail.addTo(user.getName() + " <" + user.getEmail() +">");
        mail.setBodyHtml(reset_password_email.render(user,newpw).toString());
        MailerPlugin.send(mail);

        flash("success","Your password has been reset. Check your inbox!");
        return redirect("/login");
    }

    /**
     * Attempts to process a registration request, if there are no errors it creates a new Alumni and invokes newRegistrationEmail
     * @return badRequest Result if there are errors in the data. A redirect Result to the homepage if there are no errors.
     */
    public static Result postAlumniRegForm() {
        Form<AlumniRegForm> data = Form.form(AlumniRegForm.class).bindFromRequest();

        String userSchoolName = data.data().get("school");
        if(userSchoolName == null) userSchoolName = "";  // "Please provide value" is "" too
        Map<String, Boolean> schoolMap = AdminHelpers.ConstructSchoolMap(userSchoolName,false);

        if (data.hasErrors()) {
            flash("error", "Please correct errors below.");
            return badRequest(reg_alumni.render(data, schoolMap));
        }

        AlumniRegForm formData = data.get();
        Alumni formUser = Alumni.makeInstance(formData);
        formUser.save();

        newRegistrationEmail(formUser);
        flash("success", "Registered. Check your inbox!");
        return redirect("/");
    }

    //TODO: ADDED Vivian
    public static Result postSchoolRegForm() {
        Form<SchoolRegForm> data = Form.form(SchoolRegForm.class).bindFromRequest();

        String userSchoolName = data.data().get("school");
        if(userSchoolName == null) userSchoolName = "";  // "Please provide value" is "" too
        Map<String, Boolean> schoolMap = AdminHelpers.ConstructSchoolMap(userSchoolName,false);

        if (data.hasErrors()) {
            flash("error", "Please correct errors below.");
            return badRequest(reg_school.render(data, schoolMap));
        }

        SchoolRegForm formData = data.get();
        Alumni formUser = Alumni.makeInstance(formData);
        formUser.save();

        newRegistrationEmail(formUser);
        flash("success", "Registered. Check your inbox!");
        return redirect("/");
    }
}
