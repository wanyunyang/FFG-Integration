package controllers;

import helpers.AdminHelpers;
import models.*;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.forms.UserForm;
import views.forms.VideoForm;
import views.html.alumni_videos;
import views.html.edit_alumni_video;
import views.html.edit_self;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller which contains the functions needed for users to edit their own details and for alumni to edit their own videos
 */
public class UserController extends Controller {

    /**
     * Creates a UserForm filled with data from the current User. If the User is a SuperAdmin then auth is set to true, which will allow them to edit their own School.
     * @return An ok Result that renders the edit_self html (which goes to /edit)
     */
    public static Result getUserDetails() {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();

        UserForm data = new UserForm(user.getName(),"",user.getEmail(),user.getSchool(),user.getDiscriminator(),user.getAlumniProfile(),user.getId());

        Form<UserForm> formdata = Form.form(UserForm.class).fill(data);

        boolean auth = false;
        if (user.getDiscriminator().equals("superadmin")) {
            auth = true;
        }

        Map<String, Boolean> schoolMap = AdminHelpers.ConstructSchoolMap(data.school.getName(), auth);

        return ok(edit_self.render(user, formdata, schoolMap, auth));
    }

    /**
     * A UserForm is created from the request and if it has errors then a badRequest Result is returned. Otherwise the data is saved to the database.
     * @return Either a badRequest Result if the UserForm has errors a redirect Result back to the main users page.
     */
    public static Result postUserDetails() {
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
        Map<String, Boolean> schoolMap = AdminHelpers.ConstructSchoolMap(userSchoolName,auth);

        if (data.hasErrors()) {
            flash("error", "Please correct errors below.");
            return badRequest(edit_self.render(user, data,schoolMap,auth));
        }
        else {
            UserForm formData = data.get();
            user.setName(formData.name);
            user.setEmail(formData.email);
            if (!formData.password.equals("")) {
                user.setPassword(formData.password);
            }
            if (user.getDiscriminator().equals("superadmin")) {
                SchoolDAO sdao = new SchoolDAO();
                user.setSchool(sdao.byName(formData.school.getName()));
            }
            user.setAlumniProfile(formData.profile);
            user.update();

        }
        return redirect("/");
    }

    /**
     * Backend function that codes the alumni video page. It displays a list of all the videos the alumni has uploaded, whether they have been approved and has options to
     * edit or delete the video.
     * @return An ok Result that renders the video page.
     */
    @Security.Authenticated(AlumniSecured.class)
    public static Result videos() {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        VideoDAO vdao = new VideoDAO();
        List<Video> v = vdao.getVideosByUser(user);
        return ok(alumni_videos.render(user, v));
    }

    /**
     * If the Alumni doesn't have the permission to edit the Video corresponding to id (i.e the User IDs don't match) then they are redirected to the main page. If the Video doesn't exist they are also redirected.
     * Otherwise, a new VideoForm is created with values retrieved from the database.
     * @param id the ID of the Video being edited
     * @return an ok Result that renders the edit_video html (which goes to the /myvideos/manage/id) if the User has permission to edit the Video exists otherwise a redirect Result to the main video page.
     */
    @Security.Authenticated(AlumniSecured.class)
    public static Result getVideo(Long id) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();

        VideoDAO dao = new VideoDAO();
        Video video = dao.getVideo(id);
        if (video == null) {
            flash("error","Video does not exist.");
            redirect("/myvideos");
        }
        if(!user.getId().equals(video.getUser().getId())) {
            flash("error","You do not have permission to edit that video");
            redirect("/myvideos");
        }
        VideoForm data = new VideoForm(video.getTitle(),video.getDescription(),video.getCategories(),video.getPublicAccess());
        Form<VideoForm> formdata = Form.form(VideoForm.class).fill(data);

        Map<String, Boolean> catMap = AdminHelpers.ConstructCategoryMap(video.getCategories());
        Map<String, Boolean> publicMap = new HashMap<>();
        if (!video.getPublicAccess()) {
            publicMap.put("No",true);
            publicMap.put("Yes",false);
        } else {
            publicMap.put("Yes",true);
            publicMap.put("No",false);
        }
        return ok(edit_alumni_video.render(user, formdata, id, catMap, publicMap));
    }

    /**
     * A VideoForm is created from the request. If the User doesn't have sufficient permissions (i.e it is not their Video) or if the Video does not exist then they are redirected to the main video page.
     * Otheriwse the VideoForm is checked for errors. If it has errors then a badRequest is returned. Otherwise, the Video in the database with an ID id is updated to have the same information found in the VideoForm and a redirect to the main video page is returned.
     * @param id the ID of the Video being edited
     * @return A redirect Result to the myvideo page if the User doesn't have permission to edit or if the Video doesn't exist, the Video didn't exist or if the edit was successful or a badRequest Result if the form had errors.
     */
    @Security.Authenticated(AlumniSecured.class)
    public static Result postVideo(Long id) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        Form<VideoForm> data = Form.form(VideoForm.class).bindFromRequest();
        VideoDAO dao = new VideoDAO();
        Video video = dao.getVideo(id);
        if (video == null) {
            flash("error","Video does not exist.");
            redirect("/myvideos");
        }
        if(!user.getId().equals(video.getUser().getId())) {
            flash("error","You do not have permission to edit that video");
            redirect("/myvideos");
        }

        if (data.hasErrors()) {
            Map<String, Boolean> catMap = AdminHelpers.ConstructCategoryMap(video.getCategories());
            Map<String, Boolean> publicMap = new HashMap<>();
            if (!video.getPublicAccess()) {
                publicMap.put("No",true);
                publicMap.put("Yes",false);
            } else {
                publicMap.put("Yes",true);
                publicMap.put("No",false);
            }
            flash("error", "Please correct errors below.");
            return badRequest(edit_alumni_video.render(user, data, id, catMap,publicMap));
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
            return redirect("/myvideos");
        }
    }

    /**
     * Removes the Video selected from the database. The Video is retrieved using a dynamic form to retrieve the id.
     * @return A redirect Result back to the myvideo page
     */
    @Security.Authenticated(AlumniSecured.class)
    public static Result deleteVideo() {
        DynamicForm requestData = Form.form().bindFromRequest();
        Long id = Long.parseLong(requestData.get("id"));
        VideoDAO dao = new VideoDAO();
        dao.deleteVideo(id);

        flash("success", "Video deleted!");

        return redirect("/myvideos");
    }
}
