package models;

import helpers.AppException;
import helpers.HashHelper;
import views.forms.UserForm;

import javax.persistence.*;
import java.util.List;


/**
 * Created by biko on 05/02/15.
 */
@Entity
@DiscriminatorValue("alumni")
public class Alumni extends User {

    /**
     * Constructor for the Alumni
     * @param name of the Alumni being created
     * @param password of the Alumni being created
     * @param email of the Alumni being created
     * @param school of the Alumni being created
     */
    public Alumni(String name,String password,String email, School school, String university, String occupation, String industry, String employer){
        super(name,password,email, school, university, occupation, industry, employer);
    }

    /**
     * makes an Instance of an Alumni
     * @param data from the form, containing all the information we want the Alumni to have
     * @return the alumni created is returned
     */
    public static Alumni makeInstance(UserForm data) {
        School s = (new SchoolDAO()).byName(data.school.getName());
        String password = "";
        try {
            password = HashHelper.createPassword(data.password);
        } catch (AppException e) {
            //TODO: do something useful here maybe?
        }
        Alumni alumni = new Alumni(data.name, password, data.email, s, data.university, data.occupation, data.industry, data.employer);
        alumni.setProfile(data.profile);
        return alumni;
    }


    private String profile;
    @OneToMany(cascade=CascadeType.ALL)
    private List<Video> videos;

    /**
     * Finder for searching the database for Alumni
     */
    public static Finder<Long,Alumni> find = new Finder<>(Long.class,Alumni.class);

    /**
     * setter for Profile
     * @param profile the string of the profile of the Alumni
     */
    public void setProfile(String profile){
        this.profile=profile;
    }

    /**
     * getter for the Profile
     * @return the profile of the Alumni
     */
    public String getProfile(){
        return profile;
    }
}
