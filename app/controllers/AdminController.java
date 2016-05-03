package controllers;

import helpers.AdminHelpers;
import models.*;
import org.apache.commons.lang3.RandomStringUtils;
import play.data.Form;
import play.data.DynamicForm;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.*;
import views.forms.*;
import views.html.admin.*;
import views.html.emails.*;

import java.text.SimpleDateFormat;
import java.util.*;
/**
 * Controller for all of the admin specific functions found in the admin panel. The entire class is secured using a @Security.Authenticated(AdminSecured.class) annotation,
 * which means only Admin or SuperAdmin Users can use the functions in this class.
 */
@Security.Authenticated(AdminSecured.class)
public class AdminController extends Controller {
    public static SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    /**
     * Helper function to throw an error and a redirect if the User doesn't have permission
     * @param redirUrl Relative URL to redirect to if a User doesn't have permission
     * @return A redirect Result to the specified URL
     */
    private static Result insufficientPermissions(String redirUrl) {
        flash("error", "You don't have sufficient permissions to perform requested action.");
        return redirect(redirUrl);
    }

    /**
     * Helper function to throw an error and a redirect if the database does not have the object requested.
     * @param redirUrl Relative URL to redirect to if an object doesn't exist
     * @return A redirect Result to the specified URL
     */
    private static Result doesNotExist(String redirUrl) {
        flash("error","The object with that ID does not exist");
        return redirect(redirUrl);
    }

    /**
     * Backend function that codes the admin panel index page
     * @return An ok Result that renders the index page. This page contains links to the other admin pages.
     */
    public static Result index() {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        return ok(dashboard.render(user));
    }

    /**
     * Backend function that codes the admin panel users page. The users page has a list of Users for a School with options to approve, edit or delete the Users
     * and the page also has buttons that allow to add Users in single or in bulk. Will display all Users for a School if the User is a SuperAdmin
     * or all the non-SuperAdmin Users for a School if the User is an Admin. We also pass a "boolean" int that allows us to filter by unapproved Users only
     * or to view all Users.
     * @param unapprovedonly 1 if we want to view unapproved Users only, otherwise it is 0.
     * @return An ok Result that renders the users page.
     */
    public static Result users(int unapprovedonly) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        School s = user.getSchool();
        if (user.getDiscriminator().equals("superadmin")) {
            if (unapprovedonly == 1) {
                return ok(users.render(user, udao.getUnapprovedSchoolUsers(s)));
            } else {
                return ok(users.render(user, udao.getSchoolUsers(s)));
            }
        } else {
            if (unapprovedonly == 1) {
                return ok(users.render(user, udao.getUnapprovedSchoolUsersNoSA(s)));
            } else {
                return ok(users.render(user, udao.getSchoolUsersNoSA(s)));
            }
        }
    }

    /**
     * Backend function that codes the admin panel video page. The video page has a list of all Videos for a School with options to approve, edit or delete Videos.
     * We also pass a "boolean" int that allows us to filter by unapproved Users only or to view all Users.
     * @param unapprovedonly 1 if we want to view unapproved Videos only, otherwise it is 0.
     * @return An ok Result that renders the video page.
     */
    public static Result videos(int unapprovedonly) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        VideoDAO dao = new VideoDAO();
        School s = user.getSchool();
        if (unapprovedonly == 1) {
            return ok(videos.render(user, dao.getUnapprovedVideosBySchool(s)));
        } else {
            return ok(videos.render(user, dao.getAllVideosBySchool(s)));
        }
    }

    /**
     * Backend function that codes the admin panel question page. The question page has a list of all active Questions for a School with options to approve, edit or delete individual Questions.
     * Questions can also be reordered and new Questions can be added from this page.
     * @return An ok Result that renders the question page.
     */
    public static Result questions() {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        School s = user.getSchool();
        QuestionDAO dao = new QuestionDAO();
        List<Question> qs = dao.getActiveQuestions(s); //this gets all the active Questions for a School
        return ok(questions.render(user, qs));
    }

    /**
     * Backend function that codes the admin panel school page. This function is annotated with @Security.Authenticated(SuperAdminSecured.class) which means this function can only be used by SuperAdmin Users.
     * The school page has a list of all Schools with options to edit or delete individual Schools. New Schools can also be created, which when created are populated with Questions found in the 'Default' School.
     * @return An ok Result that renders the school page.
     */
    @Security.Authenticated(SuperAdminSecured.class)
    public static Result schools() {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        SchoolDAO dao = new SchoolDAO();
        List<School> ss = dao.getAllSchool();
        return ok(schools.render(user, ss));
    }

    /**
     * Backend function that codes the admin panel category page. This function is annotated with @Security.Authenticated(SuperAdminSecured.class) which means this function can only be used by SuperAdmin Users.
     * The category page has a list of all categories with options to edit or delete individual categories. New categories can also be created.
     * @return An ok Result that renders the category page.
     */
    @Security.Authenticated(SuperAdminSecured.class)
    public static Result categories() {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        CategoryDAO dao = new CategoryDAO();
        List<Category> cs = dao.getAllCategories();
        return ok(categories.render(user, cs));
    }

    /**
     * Calls getUser with null as the id, meaning a new User is being created.
     * @return An ok Result that renders the edit_user html (which goes to the admin/users/manage/new/) with an empty UserForm with the School set as the same as the User logged in.
     */
    public static Result getNewUser() {
        return getUser(null);
    }

    /**
     * Either creates a new UserForm with empty values except for School, which is the same as the current User, or creates a UserForm filled with data from the User in the database with the matching id.
     * The logged in User (Admin/SuperAdmin) must have permission to edit the User they are trying to edit, which means they must have the same School as the User they are trying to edit, otherwise they
     * they are redirected. The User must also exist, and if it doesn't then they are redirected.
     * @param id the ID of the User that is being edited or null if a User is being created.
     * @return Either an ok Result that renders the edit_user html (which goes to the admin/users/manage/id/ or the admin/users/manage/new/) with a UserForm filled with the current data of the User being edited.
     * or a redirect to the main users page if they don't have permission or the User doesn't exist.
     */
    public static Result getUser(Long id) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        School s = user.getSchool();
        UserForm data;
        if (id == null) {
            data = new UserForm(user.getSchool()); // Suggest same School as Admin by default
        } else {
            UserDAOImpl dao = new UserDAOImpl();
            User u = dao.getUser(id);
            if (u == null) {
                return doesNotExist("/admin/users");
            }
            if (s.getId() != u.getSchool().getId()) {
                return insufficientPermissions("/admin/users");
            }
            data = new UserForm(u.getName(),"",u.getEmail(),u.getSchool(),u.getDiscriminator(),u.getAlumniProfile(),u.getId());
        }
        Form<UserForm> formdata = Form.form(UserForm.class).fill(data);

        Map<String, Boolean> schoolMap = AdminHelpers.ConstructSchoolMap(data.school.getName(),false);
        Map<String, Boolean> discrMap = AdminHelpers.ConstructDiscriminatorMap(data.discriminator, user.getDiscriminator());

        boolean auth = false;
        if (user.getDiscriminator().equals("superadmin")) {
            auth = true;
        }
        return ok(edit_user.render(user, formdata, id, schoolMap, discrMap, auth));
    }

    /**
     * Calls postUser with null as the id, meaning a new User is created.
     * @return Either a badRequest Result if the UserForm has errors or a redirect Result back to the main users page.
     */
    public static Result postNewUser() {
        return postUser(null);
    }

    /**
     * A UserForm is created from the request and if it has errors then a badRequest Result is returned. If ID is null then a User is created based upon the data found in
     * the form (with a password randomly generated if password was left blank) and the User is emailed their login details. If ID is not null then the User is found in the database by ID.
     * If the logged in User does not have permission to edit the User then they are redirected back to the users page without the data being saved to the database. If the User doesn't exist then they are redirected also.
     * Otherwise the data is saved to the database.
     * @param id the ID of the User that is being edited or null if a User is being created.
     * @return Either a badRequest Result if the UserForm has errors a redirect Result back to the main users page.
     */
    public static Result postUser(Long id) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        Form<UserForm> data = Form.form(UserForm.class).bindFromRequest();

        boolean auth = false;
        if (user.getDiscriminator().equals("superadmin")) {
            auth = true;
        }

        String userSchoolName = data.data().get("school");
        if(userSchoolName == null) userSchoolName = "";  // "Please provide value" is "" too
        if(userSchoolName.equals("")) userSchoolName = user.getSchool().getName();
        Map<String, Boolean> schoolMap = AdminHelpers.ConstructSchoolMap(userSchoolName,false);
        String temp = data.data().get("discriminator");
        if(temp == null){
            temp = "student";
        }
        Map<String, Boolean> discrMap =
                AdminHelpers.ConstructDiscriminatorMap(
                        temp,user.getDiscriminator());

        if (data.hasErrors()) {
            flash("error", "Please correct errors below.");
            return badRequest(edit_user.render(user, data, id, schoolMap, discrMap,auth));
        }
        else {
            UserForm formData = data.get();
            User formUser = null;
            if (formData.school == null) {
                formData.school = user.getSchool(); //this is to allow for non-superadmins to create stuff as they always submit null Schools
            }
            if (id == null) { //aka if we're making a new User, actually make a new one
                switch(formData.discriminator) {
                    case "alumni":
                        formUser = Alumni.makeInstance(formData);
                        break;
                    case "admin":
                        formUser = Admin.makeInstance(formData);
                        break;
                    case "superadmin": //only superadmins can possibly select this option
                        formUser = SuperAdmin.makeInstance(formData);
                        break;
                    default: //"student"
                        formUser = Student.makeInstance(formData);
                        break;
                }
                if (formData.password == null || formData.password.equals("")) {
                    String pass = RandomStringUtils.randomAlphanumeric(8);
                    formUser.setPassword(pass);
                }
                formUser.setApproved(true);
                formUser.save();

                //Notify the User
                Email mail = new Email();
                mail.setSubject("Careers From Here: Account Invitation");
                mail.setFrom("Careers From Here <careersfromhere@gmail.com>");
                mail.addTo(formUser.getEmail() + " <" + formUser.getEmail() + ">");
                mail.setBodyHtml(registration_invite.render(formUser,formData.password).toString());
                MailerPlugin.send(mail);
            }
            else { //if we have an id (aka we're editing) we want to edit the details of the User in the database already
                UserDAOImpl dao = new UserDAOImpl();
                SchoolDAO sdao = new SchoolDAO();
                formUser = dao.getUser(id);
                if (formUser == null) {
                    return doesNotExist("/admin/users");
                }
                if (user.getSchool().getId() != formUser.getSchool().getId()) {
                    return insufficientPermissions("/admin/users");
                }
                formUser.setName(formData.name);
                formUser.setEmail(formData.email);
                if (!formData.password.equals("")) {
                    formUser.setPassword(formData.password);
                }
                formUser.setSchool(sdao.byName(formData.school.getName()));
                formUser.setAlumniProfile(formData.profile);
                formUser.update();
            }
        }
        return redirect("/admin/users");
    }

    /**
     * Removes the User selected from the database. The User is retrieved using a dynamic form to retrieve the id. It also detects the page we were directed from from the header.
     * If we were directed here from the "unapproved Users only" page then we redirect back there.
     * @return A redirect Result back to the users page (unapproved only or all users)
     */
    public static Result deleteUser() {
        DynamicForm requestData = Form.form().bindFromRequest();
        Long id = Long.parseLong(requestData.get("id"));
        UserDAOImpl dao = new UserDAOImpl();
        dao.deleteUser(id);

        flash("User deleted!");

        String refererUrl = request().getHeader("referer");
        String host = request().host();
        if (refererUrl.equals("http://" + host + "/admin/users?unapprovedonly=1")){
            return redirect("/admin/users?unapprovedonly=1");
        } else {
            return redirect("/admin/users");
        }
    }

    /**
     * Sets the approved boolean field for a User to true. The User is retrieved using a dynamic form to retrieve the id. It also detects the page we were directed from from the header.
     * If we were directed here from the "unapproved Users only" page then we redirect back there.
     * @return A redirect Result back to the users page (unapproved only or all users)
     */
    public static Result approveUser() {
        DynamicForm requestData = Form.form().bindFromRequest();
        Long id = Long.parseLong(requestData.get("id"));

        UserDAOImpl dao = new UserDAOImpl();
        dao.approveUser(id);

        flash("success", "User approved!");

        String refererUrl = request().getHeader("referer");
        String host = request().host();
        if (refererUrl.equals("http://" + host + "/admin/users?unapprovedonly=1")){
            return redirect("/admin/users?unapprovedonly=1");
        } else {
            return redirect("/admin/users");
        }    }

    /**
     * Creates an empty BulkRegisterForm with the School set to the same as the User by default.
     * @return An ok Result that renders the bulk_register html (which goes to the admin/users/bulkregister) with an empty BulkRegisterForm with School set to the same as the logged in User.
     */
    public static Result getBulkRegister() {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        School s = user.getSchool();

        boolean auth = false;
        if (user.getDiscriminator().equals("superadmin")) {
            auth = true;
        }

        BulkRegisterForm data = new BulkRegisterForm(user.getSchool()); // Suggest same School as Admin by default

        Form<BulkRegisterForm> formdata = Form.form(BulkRegisterForm.class).fill(data);

        Map<String, Boolean> schoolMap = AdminHelpers.ConstructSchoolMap(data.school.getName(),false);
        Map<String, Boolean> discrMap = AdminHelpers.ConstructDiscriminatorMap(data.discriminator, user.getDiscriminator());

        return ok(bulk_register.render(user, formdata, schoolMap,discrMap,auth));
    }

    /**
     * Retrieves a BulkRegisterForm from the request and parses the data within it. If the form has errors then a badRequest is returned. Otherwise, the data field is parsed so that it is split into an array of
     * Strings based on new line characters. Each String in this list is compared so that it is not empty/null, matches an email regex and that a User in the database doesn't already have the email dictated in the
     * String. If the String doesn't cause an error then a new User is created with a default name and a random password and the User is emailed their login details. The successful account creations are counted
     * and returned in a flash so the User knows how many accounts were successfully created. If there were Strings that caused errors then a badRequest Result is returned and a new BulkRegisterForm is created with
     * the same fields as the current BulkRegisterForm but with the data field now only filled with the concatenated Strings (with new line characters seperating them) that caused errors. If there were no errors
     * then a redirect Result takes them to the main users page.
     * @return Either a badRequest Result if the form had errors or if certain emails entered were invalid or a redirect back to the main users page.
     */
    public static Result postBulkRegister() {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        Form<BulkRegisterForm> data = Form.form(BulkRegisterForm.class).bindFromRequest();

        boolean auth = false;
        if (user.getDiscriminator().equals("superadmin")) {
            auth = true;
        }

        String userSchoolName = data.data().get("school");
        if(userSchoolName == null) userSchoolName = "";  // "Please provide value" is "" too
        if(userSchoolName.equals("")) userSchoolName = user.getSchool().getName();
        Map<String, Boolean> schoolMap = AdminHelpers.ConstructSchoolMap(userSchoolName,false);
        String temp = data.data().get("discriminator");
        if(temp == null){
            temp = "student";
        }
        Map<String, Boolean> discrMap = AdminHelpers.ConstructDiscriminatorMap(temp, user.getDiscriminator());

        if (data.hasErrors()) {
            flash("error", "Please correct errors below.");
            return badRequest(bulk_register.render(user, data, schoolMap,discrMap,auth));
        }
        else {
            BulkRegisterForm formData = data.get();
            if (formData.school == null) {
                formData.school = user.getSchool(); //this is to allow for non-superadmins to create stuff as they always submit null Schools
            }
            String emails = formData.data;
            String[] emailss = emails.split("\r\n|\r|\n");
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile("\\b[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\b");
            SchoolDAO sdao = new SchoolDAO();

            List<String> errors = new ArrayList<>();
            int success = 0;
            for(int i = 0; i < emailss.length; i++) {
                String s = emailss[i];
                //Create new User with random password
                String pass = RandomStringUtils.randomAlphanumeric(8);
                User formUser = null;
                if (s == null || s.length() == 0) {
                    //do nothing - it's not the end of the world if they leave a blank line in!
                } else if (!regex.matcher(s).matches()) {
                    errors.add(s);
                } else if (udao.getUserByEmail(s) != null) {
                    errors.add(s);
                } else {
                    switch(formData.discriminator) {
                        case "alumni":
                            formUser = new Alumni("Default Alumni",pass,s,sdao.byName(formData.school.getName()), "", "", "", "");
                            break;
                        case "admin":
                            formUser = new Admin("Default Admin",pass,s,sdao.byName(formData.school.getName()));
                            break;
                        case "superadmin": //only superadmins can possibly select this option
                            formUser = new SuperAdmin("Default SuperAdmin",pass,s,sdao.byName(formData.school.getName()));
                            break;
                        default: //"student"
                            formUser = new Student("Default Student",pass,s,sdao.byName(formData.school.getName()));
                            break;
                    }
                    formUser.setPassword(pass);
                    formUser.save();
                    success++;

                    //Notify the User
                    Email mail = new Email();
                    mail.setSubject("Careers From Here: Account Invitation");
                    mail.setFrom("Careers From Here <careersfromhere@gmail.com>");
                    mail.addTo(formUser.getName() + " <" + formUser.getEmail() + ">");
                    mail.setBodyHtml(registration_invite.render(formUser,pass).toString());
                    MailerPlugin.send(mail);
                }
            }
            if (success > 0) {
                if (success == 1) {
                    flash("success", success + " account was created!");
                } else {
                    flash("success", success + " accounts were created!");

                }
            }
            if (errors.size() != 0) {
                flash("error", "These email addresses are either invalid email addresses or they are already associated with accounts. Please check they are correct.");
                String ret = "";
                for (String s : errors) {
                    ret = ret + s + "\n";
                }
                formData.data = ret;
                data =  Form.form(BulkRegisterForm.class).fill(formData);
                return badRequest(bulk_register.render(user, data, schoolMap,discrMap,auth));
            }
        }
        return redirect("/admin/users");
    }

    /**
     * If the logged in User doesn't have the permission to edit the Video corresponding to id (i.e the Schools do not match) then they are redirected to the main users page. If the Video doesn't exist they are also redirected.
     * Otherwise, a new VideoForm is created with values retrieved from the database from the User with the ID of id.
     * @param id the ID of the Video being edited
     * @return an ok Result that renders the edit_video html (which goes to the admin/video/manage/id) if the User has permission to edit the Video exists otherwise a redirect Result to the main video page.
     */
    public static Result getVideo(Long id) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();

        VideoDAO dao = new VideoDAO();
        Video video = dao.getVideo(id);
        if (video == null) {
            return doesNotExist("/admin/videos");
        }
        if(user.getSchool().getId() != video.getUser().getSchool().getId()) {
            return insufficientPermissions("/admin/videos");
        }

        VideoForm data = new VideoForm(video.getTitle(),video.getDescription(),video.getCategories(),video.getPublicAccess());
        Form<VideoForm> formdata = Form.form(VideoForm.class).fill(data);

        Map<String, Boolean> catMap = AdminHelpers.ConstructCategoryMap(video.getCategories());
        Map<String, Boolean> publicMap = new HashMap<>();
        if (video.getPublicAccess() == false) {
            publicMap.put("No",true);
            publicMap.put("Yes",false);
        } else {
            publicMap.put("Yes",true);
            publicMap.put("No",false);
        }
        return ok(edit_video.render(user, formdata, id, catMap, publicMap));
    }

    /**
     * A VideoForm is created from the request. If the User doesn't have sufficient permissions then they are redirected to the main video page. Otheriwse the VideoForm is checked for errors. If it has errors then
     * a badRequest is returned. Otherwise, the Video in the database with an ID id is updated to have the same information found in the VideoForm and a redirect to the main video page is returned.
     * @param id the ID of the Video being edited
     * @return A redirect Result to the main video page if the User doesn't have permission to edit, the Video didn't exist or if the edit was successful or a badRequest Result if the form had errors.
     */
    public static Result postVideo(Long id) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        Form<VideoForm> data = Form.form(VideoForm.class).bindFromRequest();
        VideoDAO dao = new VideoDAO();
        Video video = dao.getVideo(id);
        if (video == null) {
            return doesNotExist("/admin/videos");
        }
        if(user.getSchool().getId() != video.getUser().getSchool().getId()) {
            return insufficientPermissions("/admin/videos");
        }

        if (data.hasErrors()) {
            Map<String, Boolean> catMap = AdminHelpers.ConstructCategoryMap(video.getCategories());
            Map<String, Boolean> publicMap = new HashMap<>();
            if (video.getPublicAccess() == false) {
                publicMap.put("No",true);
                publicMap.put("Yes",false);
            } else {
                publicMap.put("Yes",true);
                publicMap.put("No",false);
            }
            flash("error", "Please correct errors below.");
            return badRequest(edit_video.render(user, data, id, catMap,publicMap));
        }
        else { //don't need to check for null id because we don't create Videos here
            CategoryDAO cdao = new CategoryDAO(); // Has fully initialised Category objects

            VideoForm formData = data.get();
            video.setTitle(formData.title);
            video.setDescription(formData.description);
            video.categories.clear();
            if(formData.publicaccess.equals("Yes")) {
                video.setPublicAccess(true);
            } else {
                video.setPublicAccess(false);
            }

            for (Category c : cdao.getAllCategories()) {
                if (AdminHelpers.CategoryContains(formData.categories, c)) {
                    video.addCategory(c);
                }
            }
            video.update();
            return redirect("/admin/videos");
        }
    }

    /**
     * Removes the Video selected from the database. The Video is retrieved using a dynamic form to retrieve the id. It also detects the page we were directed from from the header.
     * If we were directed here from the "unapproved Videos only" page then we redirect back there.
     * @return A redirect Result back to the video page (unapproved only or all)
     */
    public static Result deleteVideo() {
        DynamicForm requestData = Form.form().bindFromRequest();
        Long id = Long.parseLong(requestData.get("id"));
        VideoDAO dao = new VideoDAO();
        dao.deleteVideo(id);

        flash("success", "Video deleted!");

        String refererUrl = request().getHeader("referer");
        String host = request().host();
        if (refererUrl.equals("http://" + host + "/admin/videos?unapprovedonly=1")){
            return redirect("/admin/videos?unapprovedonly=1");
        } else {
            return redirect("/admin/videos");
        }
    }

    /**
     * Sets the approved boolean field for a User to true. The User is retrieved using a dynamic form to retrieve the id. It also detects the page we were directed from from the header.
     * If we were directed here from the "unapproved Videos only" page then we redirect back there.
     * @return A redirect Result back to the users page (unapproved only or all)
     */
    public static Result approveVideo() {
        DynamicForm requestData = Form.form().bindFromRequest();
        Long id = Long.parseLong(requestData.get("id"));
        VideoDAO dao = new VideoDAO();
        Video v = dao.getVideo(id);
        v.setApproved(true);
        v.update();
        VideoProcessor processor = new VideoProcessor();
        VideoUploader youtubeUploader = new VideoUploader();
        List<VideoClip> videoClips = v.getVideoClips();
        for (VideoClip vc: videoClips)
        {
            String filePath = vc.getVideoPath().substring(0, vc.getVideoPath().lastIndexOf("/")+1);
            String newFileName = vc.getVideoPath().substring(vc.getVideoPath().lastIndexOf("/")+1, vc.getVideoPath().lastIndexOf("."));
            Date date = new Date();
            String outputPath = newFileName + "-" + df.format(date) + ".flv";
            String mergedOutputPath = processor.processVideo(vc.getVideoPath(), vc.getAudioPath(), outputPath);
            String youtubePath = null;
            if (mergedOutputPath != null)
            {
                vc.setOutputPath(mergedOutputPath);
                youtubePath = youtubeUploader.upload(mergedOutputPath);
            }
            if (youtubePath != null)
            {
                vc.setYoutubeID(youtubePath);
                vc.update();
            }
        }

        flash("success", "Video approved!");

        String refererUrl = request().getHeader("referer");
        String host = request().host();
        if (refererUrl.equals("http://" + host + "/admin/videos?unapprovedonly=1")){
            return redirect("/admin/videos?unapprovedonly=1");
        } else {
            return redirect("/admin/videos");
        }
    }

    /**
     * Calls getQuestion with a null ID.
     * @return Either a redirect if the User had insufficient permissions or an ok Result that renders the edit_question html (which is the URL /admin/questions/manage/new)
     */
    public static Result getNewQuestion() { return getQuestion(null);}

    /**
     * Creates a QuestionForm based on the id. If the id is null then an empty QuestionForm is created, otherwise it is filled with data from the Question with the matching id in the database. If the User does not
     * have permission to edit the Question then they are redirected to the main questions page. If the Question doesn't exist they are also redirected. Otherwise an ok Result is returned.
     * @param id the ID of the Question being edited or null if a Question is being created.
     * @return A redirect Result to the main question page if the User had insufficient permissions or the Question doesn't exist or an ok Result that renders the edit_question html (which is the URL /admin/questions/manage/new or /admin/questions/manage/id)
     */
    public static Result getQuestion(Long id) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        QuestionForm data;

        if (id == null) {
            data = new QuestionForm();
        }
        else {
            QuestionDAO dao = new QuestionDAO();
            Question q = dao.getQuestion(id);
            if (q == null) {
                return doesNotExist("/admin/questions");
            }
            if(user.getSchool().getId() != q.getSchool().getId()) {
                return insufficientPermissions("/admin/questions");
            }
            data = new QuestionForm(q.getText(),q.getDuration());
        }
        Form<QuestionForm> formdata = Form.form(QuestionForm.class).fill(data);
        return ok(edit_question.render(user, formdata, id));
    }

    /**
     * Creates a DynamicForm from the request and retrieves the order String from it. Based in this String an array is created, split by commas. This array is then checked for validity against
     * the size of the active Question list, each element is checked to be an integer and it is checked so that is it a valid permutation of numbers from  1 to the size of the active Question list.
     * If an error is found then a redirect to the main question page is returned. Otherwise the Questions in database are updated with the new order and a redirect to the main questions page is returned.
     * @return A redirect Result to the main question page.
     */
    public static Result reorderQuestions() {
        DynamicForm requestData = Form.form().bindFromRequest();
        String data = requestData.get("order");

        UserDAOImpl udao = new UserDAOImpl();
        Http.Request r = request();
        QuestionDAO dao = new QuestionDAO();

        User user = udao.getUserFromContext();
        School s = user.getSchool();

        List<Question> qs = dao.getActiveQuestions(s);
        String[] orderStr = data.split(",");
        int[] order = new int[qs.size()];
        try {
            // Ensure same length
            if(orderStr.length != qs.size())
                throw new Exception("Wrong reorder size submitted.");

            // Ensure every element is a valid integer
            int[] orderTmp = new int[qs.size()];
            for(int i = 0; i < order.length; ++i) {
                order[i] =  orderTmp[i] = Integer.parseInt(orderStr[i]);
            }

            // Ensure it's a valid permutation from 1 to qs.size()
            // By sorting newOrderTemp
            Arrays.sort(orderTmp);
            for(int i = 0; i < orderTmp.length; ++i) {
                if(orderTmp[i] != i+1)
                    throw new Exception("Invalid ordering provided.");
            }
        } catch(Exception e) {
            flash("error", e.getMessage());
            return redirect("/admin/questions");
        }

        // Apply reordering
        for(int i = 0; i < qs.size(); ++i) {
            int oldOrder = order[i];
            int newOrder = i+1;
            Question q = qs.get(oldOrder-1);
            q.setOrder(newOrder);
            q.update();
        }
        return redirect("/admin/questions");
    }

    /**
     * Calls postQuestion with a null id.
     * @return A redirect Result to the main questions page if the User has insufficient permissions or the operation is successful or a badRequest Result if the form has errors.
     */
    public static Result postNewQuestion() {return postQuestion(null);}

    /**
     * Retrieves a QuestionForm from the request. If this form has errors then a badRequest is returned. Otherwise, if id is null then a new Question is created from the form data. If the id is not null then
     * the User is checked for permission. If the School of the Question does not match the School of the User then redirect back to tha main questions page. If the Question doesn't exist then also redirect.
     * Otherwise, update the Question in the database with the data from the form.
     * @param id the ID of the Question being edited or null if a Question is being created
     * @return A redirect Result to the main questions page if the User has insufficient permissions, the Question doesn't exist or the operation is successful or a badRequest Result if the form has errors.
     */
    public static Result postQuestion(Long id) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        Form<QuestionForm> data = Form.form(QuestionForm.class).bindFromRequest();

        if (data.hasErrors()) {
            flash("error", "Please correct errors below.");
            return badRequest(edit_question.render(user, data, id));
        }
        else {
            QuestionForm formData = data.get();
            Question q;
            QuestionDAO dao = new QuestionDAO();
            if (id == null) { //if we are creating a new Question
                q = Question.makeInstance(formData, user.getSchool());
                dao.newQuestion(q); //this ensures we get the ordering correct
            } else {
                q = dao.getQuestion(id);
                if (q == null) {
                    return doesNotExist("/admin/questions");
                }
                if(user.getSchool().getId() != q.getSchool().getId()) {
                    return insufficientPermissions("/admin/questions");
                }
                q.setText(formData.text);
                q.setDuration(formData.duration);
                q.update();
            }
            return redirect("/admin/questions");
        }
    }

    /**
     * Retrieves an id from the request using a DynamicForm and calls deleteQuestion on the id. This sets the active field on the Question to 0 rather than deleting the Question to ensure backwards compatibility with
     * old Videos.
     * @return A redirect Result to the main question page.
     */
    public static Result deleteQuestion() {
        DynamicForm requestData = Form.form().bindFromRequest();
        Long id = Long.parseLong(requestData.get("id"));
        QuestionDAO dao = new QuestionDAO();
        dao.deleteQuestion(id);

        return redirect("/admin/questions");
    }

    /**
     * Calls getSchool with a null id parameter. This method is annotated with @Security.Authenticated(SuperAdminSecured.class) which means it can only be called by SuperAdmin Users.
     * @return An ok Result which renders the edit school html (which is /admin/schools/manage/new)
     */
    @Security.Authenticated(SuperAdminSecured.class)
    public static Result getNewSchool() { return getSchool(null);}

    /**
     * Creates a SchoolForm based on the id parameter. If id is null then an empty SchoolForm is created, otherwise a SchoolForm is created and filled with data from the School in the database with the ID id. If
     * the School doesn't exist then the logged in User is redirected to the main schools page.
     * This method is annotated with @Security.Authenticated(SuperAdminSecured.class) which means it can only be called by SuperAdmin Users.
     * @param id the ID of the School we are editing or null if we are creating a School.
     * @return An ok Result which renders the edit school html (which is either /admin/schools/manage/id or /admin/schools/manage/new) or a redirect if the School doesn't exist.
     */
    @Security.Authenticated(SuperAdminSecured.class)
    public static Result getSchool(Long id) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        SchoolForm data;

        if (id == null) {
            data = new SchoolForm();
        }
        else {
            SchoolDAO dao = new SchoolDAO();
            School sch = dao.getSchool(id);
            if (sch == null) {
                return doesNotExist("/admin/schools");
            }
            data = new SchoolForm(sch.getName());
        }
        Form<SchoolForm> formdata = Form.form(SchoolForm.class).fill(data);
        return ok(edit_school.render(user, formdata, id));
    }

    /**
     * Calls postSchool with a null id parameter. This method is annotated with @Security.Authenticated(SuperAdminSecured.class) which means it can only be called by SuperAdmin Users.
     * @return A badRequest Result if the form has errors or a redirect Result back to the main school page.
     */
    @Security.Authenticated(SuperAdminSecured.class)
    public static Result postNewSchool() {return postSchool(null);}

    /**
     * Retrieves a School form from the request. If this form has errors then it returns a badRequest Result. Otherwise, either a new School is created with the data from the form (if id is null) or
     * the School with the matching ID in the database is updated with the data in the form. If School doesn't exist then they are redirected before any saves can be made.
     * This method is annotated with @Security.Authenticated(SuperAdminSecured.class) which means it can only be called by SuperAdmin Users.
     * @param id the ID of the School being edited or null if a School is being created.
     * @return A badRequest Result if the form has errors or a redirect Result back to the main school page.
     */
    @Security.Authenticated(SuperAdminSecured.class)
    public static Result postSchool(Long id) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        Form<SchoolForm> data = Form.form(SchoolForm.class).bindFromRequest();

        if (data.hasErrors()) {
            flash("error", "Please correct errors below.");
            return badRequest(edit_school.render(user, data, id));
        }
        else {
            SchoolForm formData = data.get();
            School sch;
            if (id == null) {
                sch = School.makeInstance(formData);
                sch.save();

            } else {
                SchoolDAO dao = new SchoolDAO();
                sch = dao.getSchool(id);
                if (sch == null) {
                    return doesNotExist("/admin/schools");
                }
                sch.setName(formData.name);
                sch.update();
            }
            return redirect("/admin/schools");
        }
    }

    /**
     * Deletes a School using an ID retrieved from a DynamicForm. This method is annotated with @Security.Authenticated(SuperAdminSecured.class) which means it can only be called by SuperAdmin Users.
     * @return A redirect Result back to the main school page
     */
    @Security.Authenticated(SuperAdminSecured.class)
    public static Result deleteSchool() {
        DynamicForm requestData = Form.form().bindFromRequest();
        Long id = Long.parseLong(requestData.get("id"));
        SchoolDAO dao = new SchoolDAO();
        dao.deleteSchool(id);

        return redirect("/admin/schools");
    }

    /**
     * Calls getCategory with a null id parameter. This method is annotated with @Security.Authenticated(SuperAdminSecured.class) which means it can only be called by SuperAdmin Users.
     * @return An ok Result that renders the edit category html (which is at /admin/categories/manage/new)
     */
    @Security.Authenticated(SuperAdminSecured.class)
    public static Result getNewCategory() { return getCategory(null);}

    /**
     * Creates either a new empty CategoryForm if id is null or a new CategoryForm filled with the data retrieved from the database Category with the ID matching the parameter id. If the Category doesn't exist then
     * they are redirected to the main categories page. This method is annotated with @Security.Authenticated(SuperAdminSecured.class) which means it can only be called by SuperAdmin Users.
     * @param id the ID of the Category being edited or null if a Category is being created.
     * @return An ok Result that renders the edit category html (which is at /admin/categories/manage/id or /admin/categories/manage/new) or a redirect to the main categories page.
     */
    @Security.Authenticated(SuperAdminSecured.class)
    public static Result getCategory(Long id) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        CategoryForm data;

        if (id == null) {
            data = new CategoryForm();
        }
        else {
            CategoryDAO dao = new CategoryDAO();
            Category cat = dao.getCategory(id);
            if (cat == null) {
                return doesNotExist("/admin/categories");
            }
            data = new CategoryForm(cat.getName());
        }
        Form<CategoryForm> formdata = Form.form(CategoryForm.class).fill(data);
        return ok(edit_category.render(user, formdata, id));
    }

    /**
     * Calls postCategory with a null id parameter. This method is annotated with @Security.Authenticated(SuperAdminSecured.class) which means it can only be called by SuperAdmin Users.
     * @return
     */
    @Security.Authenticated(SuperAdminSecured.class)
    public static Result postNewCategory() {return postCategory(null);}

    /**
     * Retrieves a CategoryForm from the request. If this form has errors then a badRequest Result is returned. Otherwise, based on the id parameter, either a new Category is created from the data contained in
     * the CategoryForm (if id is null) or an existing Category in the database is updated with the data contained in the CategoryForm (if id is not null). If the Category doesn't exist then a redirect is returned
     * before any saves can be made. This method is annotated with @Security.Authenticated(SuperAdminSecured.class) which means it can only be called by SuperAdmin Users.
     * @param id the ID of the Category being edited or null if a Category is being created.
     * @return A redirect Result back to the main category page if it was successful or a badRequest Result if the form had errors.
     */
    @Security.Authenticated(SuperAdminSecured.class)
    public static Result postCategory(Long id) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        Form<CategoryForm> data = Form.form(CategoryForm.class).bindFromRequest();

        if (data.hasErrors()) {
            flash("error", "Please correct errors below.");
            return badRequest(edit_category.render(user, data, id));
        }
        else {
            CategoryForm formData = data.get();
            Category cat;
            if (id == null) {
                cat = Category.makeInstance(formData);
                cat.save();

            } else {
                CategoryDAO dao = new CategoryDAO();
                cat = dao.getCategory(id);
                if (cat == null) {
                    return doesNotExist("/admin/categories");
                }
                cat.setName(formData.name);
                cat.update();
            }
            return redirect("/admin/categories");
        }
    }

    /**
     * Deletes a Category using an ID retrieved from a DynamicForm. This method is annotated with @Security.Authenticated(SuperAdminSecured.class) which means it can only be called by SuperAdmin Users.
     * @return A redirect Result back to the main category page.
     */
    @Security.Authenticated(SuperAdminSecured.class)
    public static Result deleteCategory() {
        DynamicForm requestData = Form.form().bindFromRequest();
        Long id = Long.parseLong(requestData.get("id"));
        CategoryDAO dao = new CategoryDAO();
        dao.deleteCategory(id);

        return redirect("/admin/categories");
    }

}
