package controllers;

import models.User;
import models.UserDAOImpl;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import static play.mvc.Controller.flash;

/**
 * An Authenticator that allows to check the current user is an Alumni
 */
public class AlumniSecured extends Security.Authenticator {

    /**
     * This method is automatically called when used as an annotation. It checks the session to see if the email stored there belongs to a user
     * and if that user is an Alumni.
     * @param ctx The context of this session (default is session cookie)
     * @return The email of the User if they are authenticated, otherwise null.
     */
    @Override
    public String getUsername(Http.Context ctx) {
        String email = ctx.session().get("email");
        UserDAOImpl dao = new UserDAOImpl();
        User u = dao.getUserByEmail(email);
        if (u != null && u.getDiscriminator().equals("alumni")) {
            return email;
        } else {
            return null;
        }
    }

    /**
     * If the user is not authorised then they get redirected to the main page with an error flash.
     * @param ctx The context of this session (default is session cookie)
     * @return A redirect Result back to the main page.
     */
    @Override
    public Result onUnauthorized(Http.Context ctx) {
        flash("error","You are unauthorised to access this page, please login as an authorised user");
        return redirect("/");
    }
}
