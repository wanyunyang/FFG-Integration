package controllers;

import play.mvc.*;
import play.*;
import play.mvc.Http.*;
import models.*;
import static play.mvc.Controller.*;

/**
 * Created by biko on 18/02/15.
 */
public class Secured extends Security.Authenticator {

    @Override
    public String getUsername(Context ctx){
        return ctx.session().get("email");
    }

    @Override
    public Result onUnauthorized(Context ctx){
        flash("error","You are unauthorised to access this page, please login as an authorised user");
        return redirect("/login");
    }

}
