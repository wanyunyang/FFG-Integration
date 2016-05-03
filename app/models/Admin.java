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
 * Created by biko on 05/02/15.
 */

/*
 * NOTE: If you're adding a function to Admin that
 * should also be accessible to SuperAdmins, remember
 * to copy it over to SuperAdmin as SuperAdmin no
 * longer inherits from Admin.
 */

@Entity
@DiscriminatorValue("admin")
public class Admin extends User {

    /**
     * Contructor of the Admin
     * @param name of the Admin will be set to this
     * @param password of the admin will be set to this
     * @param email of the admin will be set to this
     * @param school of the admin will be set to this
     */
    public Admin(String name,String password,String email, School school){
        super(name,password,email, school);
    }

    /**
     * makes an instance of an Admin
     * @param data from the form, containing all the information we want the Admin to have
     * @return the admin created is returned
     */
    public static Admin makeInstance(UserForm data) {
        School s = (new SchoolDAO()).byName(data.school.getName());
        String password = "";
        try {
            password = HashHelper.createPassword(data.password);
        } catch (AppException e) {
            //TODO: do something useful here maybe?
        }
        Admin admin = new Admin(data.name, password, data.email, s);
        return admin;
    }

    /**
     * Finder for searching the database for Admins
     */
    public static Finder<Long,Admin> find = new Finder<>(Long.class,Admin.class);

    /**
     *
     * @return a list of all the video's that are unapproved
     */
    public List<Video> getUnapprovedVideos(){
        ExpressionList<Video> expList = Video.find.where().eq("approved",0);
        List<Video> videos = expList.findList();

        return videos;
    }

    /**
     *
     * @return a list of approved video's
     */
    public List<Video> getApprovedVideos(){
        ExpressionList<Video> expList = Video.find.where().eq("approved",1);
        List<Video> videos = expList.findList();

        return videos;
    }

    /**
     *
     * @return a list of all video's
     */
    public List<Video> getAllVideos(){
        List<Video> videos = Ebean.find(Video.class).select("*").orderBy("Video.approved, approved asc").findList();
        return videos;
    }

    /**
     *
     * @return a list of students associated with the Admin's school
     */
    public List<Student> getStudents() {
        School school = this.getSchool();
        List<Student> students = Student.find.where().eq("school",school).findList();
        return students;
    }

    /**
     *
     * @return a list of the alumni associated with the admin's school
     */
    public List<Alumni> getAlumni() {
        School school = this.getSchool();
        List<Alumni> alumni = Alumni.find.where().eq("school",school).findList();
        return alumni;
    }
}
