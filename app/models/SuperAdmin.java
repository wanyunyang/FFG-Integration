package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import helpers.AppException;
import helpers.HashHelper;
import views.forms.UserForm;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;

/**
 * Refer to Admin class for documentation - There is no difference between the two at this level
 */
@Entity
@DiscriminatorValue("superadmin")
public class SuperAdmin extends User {

    public SuperAdmin(String name, String password, String email, School school){
        super(name, password, email, school);
    }

    public static SuperAdmin makeInstance(UserForm data) {
        School s = (new SchoolDAO()).byName(data.school.getName());
        String password = "";
        try {
            password = HashHelper.createPassword(data.password);
        } catch (AppException e) {
            //TODO: do something useful here maybe?
        }
        SuperAdmin admin = new SuperAdmin(data.name, password, data.email, s);
        return admin;
    }

    public void setActiveSchool(School school){
        super.setSchool(school);
    }

    public List<School> getSchools() {
        SchoolDAO dao = new SchoolDAO();
        return dao.getAllSchool();
    }

    public List<Video> getUnapprovedVideos(){
        ExpressionList<Video> expList = Video.find.where().eq("approved",0);
        List<Video> videos = expList.findList();

        return videos;
    }

    public List<Video> getApprovedVideos(){
        ExpressionList<Video> expList = Video.find.where().eq("approved",1);
        List<Video> videos = expList.findList();

        return videos;
    }

    public List<Video> getAllVideos(){
        List<Video> videos = Ebean.find(Video.class).select("*").orderBy("Video.approved, approved asc").findList();
        return videos;
    }

    public List<Student> getStudents() {
        School school = this.getSchool();
        List<Student> students = Student.find.where().eq("school",school).findList();
        return students;
    }

    public List<Alumni> getAlumni() {
        School school = this.getSchool();
        List<Alumni> alumni = Alumni.find.where().eq("school",school).findList();
        return alumni;
    }
}
