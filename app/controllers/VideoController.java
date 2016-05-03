package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import helpers.AdminHelpers;
import models.*;
import play.api.Routes;
import play.data.Form;
import play.mvc.*;
import views.forms.CategorySelectionForm;
import views.html.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static play.data.Form.form;


public class VideoController extends Controller {

    public static Result index() {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();

        VideoDAO dao = new VideoDAO();
        List<Video> accessibleVideos = new ArrayList<Video>();
        if (user == null) {
            accessibleVideos = dao.getAllPublicVideos();
        }
        else {
            accessibleVideos = dao.getVideosBySchool(user.getSchool());
        }

        CategoryDAO cdao = new CategoryDAO();
        List<Category> allCats = cdao.getAllCategories();

        Map<String, Boolean> catIdNameMap = new HashMap<>();
        for(Category c : allCats)  {
            catIdNameMap.put(c.getName(), false);
        }

        Form<CategorySelectionForm> catForm = form(CategorySelectionForm.class)
                .fill(new CategorySelectionForm(allCats));

        return ok(index.render(accessibleVideos, catForm, catIdNameMap, user));
    }

    public static Result categorySelect()
    {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();

        CategoryDAO cdao = new CategoryDAO();
        List<Category> allCats = cdao.getAllCategories();

        CategorySelectionForm form = Form.form(CategorySelectionForm.class).bindFromRequest().get();

        Map<String, Boolean> catIdNameMap = new HashMap<>();
        List<Category> selectedCategories = new ArrayList<Category>();
        for (Category c : allCats) {
            // TODO: add to a separate list for filtering
            // form.categories contains malformed Category objects -- only name is present, no ids, etc.
            boolean cont = (form.categories == null) ? false : AdminHelpers.CategoryContains(form.categories, c);
            if(cont) selectedCategories.add(c);
            catIdNameMap.put(c.getName(), cont);
        }

        Form<CategorySelectionForm> catForm = form(CategorySelectionForm.class)
                .fill(new CategorySelectionForm(allCats));

        VideoDAO dao = new VideoDAO();
        List<Video> accessibleVideos = new ArrayList<>();

        if (user == null) {
            accessibleVideos = dao.getAllPublicVideosByCategories(selectedCategories);
        }
        else {
            accessibleVideos = dao.getVideosBySchoolAndCategories(user.getSchool(), selectedCategories);
        }


        return ok(index.render(accessibleVideos, catForm, catIdNameMap, user));
    }

    //TODO: change this to view the correct video from database
    public static Result view(Long id) {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();
        VideoDAO vdao = new VideoDAO();
        Video v = vdao.getVideo(id);
        if (v == null) {
            flash("error","The video with the requested ID does not exist");
            return redirect("/");
        }
        if (!v.getPublicAccess() && v.getUser().getSchool().getId() != user.getSchool().getId()) {
            flash("error", "You don't have sufficient permissions to view requested video.");
            return redirect("/");
        }

        return ok(view.render(v, user));
    }

    // TODO: check if integrated correctly
    @Security.Authenticated(AlumniSecured.class)
    public static Result record() {
        UserDAOImpl udao = new UserDAOImpl();
        User user = udao.getUserFromContext();

        QuestionDAO qdao = new QuestionDAO();
        List<Question> questions = qdao.getActiveQuestions(user.getSchool());

        ArrayList<String> questionsText = new ArrayList<>();
        for (int i = 0; i < questions.size(); ++i)
            questionsText.add(i, questions.get(i).getText());

        ArrayList<Double> questionsDurations = new ArrayList<>();
        for (int i = 0; i < questions.size(); ++i)
            questionsDurations.add(i, questions.get(i).getDuration());

        ObjectMapper obj = new ObjectMapper();
        String JSONQuestionsText = null;
        String JSONQuestionsDurations = null;
        try {
            JSONQuestionsText = obj.writeValueAsString(questionsText);
            JSONQuestionsDurations = obj.writeValueAsString(questionsDurations);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return ok(record.render(questions, JSONQuestionsText, JSONQuestionsDurations, user));
    }

    public static Result getVideoAt(String path) {
        return ok(new File(path));
    }

}
