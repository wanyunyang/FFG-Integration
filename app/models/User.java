package models;



/**
 * Created by biko on 03/02/15.
 */
import javax.persistence.*;

import helpers.*;
import play.db.ebean.*;
import views.forms.UserForm;
import play.libs.Crypto;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name="discriminator")
public abstract class User extends Model {
    @Id
    private Long id;
    private String name;
    private String password;
    private String email;
    private boolean approved = false;

    @ManyToOne
    private School school;

    public String university;

    public String occupation;

    public String industry;

    public String employer;


    public User(String name, String password, String email, School school){
        this(name, password, email, school, null, null, null, null);
    }

    /**
     * Constructor for Userclass
     * @param name of the user being created
     * @param password of the user being created
     * @param email of the user being created
     * @param school of the user being created
     */
    public User(String name, String password, String email, School school, String university, String occupation, String industry, String employer){
        this.name=name;
        this.email=email;
        this.password=password;
        this.school = school;
        this.employer = employer;
        this.industry = industry;
        this.occupation = occupation;
        this.university = university;
    }

    /**
     * Used for matching an email and password with a user
     * @param email email of the user being authenticated
     * @param password password of the user being authenticated
     * @return User that matched the username and password, null if none match
     */
    public static User authenticate(String email, String password){
        User user = find.where().eq("email",email).findUnique();
        if(user == null) return null;
        boolean auth = HashHelper.checkPassword(password,user.password);
        if (auth) {
            return user;
        } else {
            return null;
        }
    }

    /**
     * used to test if the user has admin rights (ie discriminator = "admin" or "superadmin")
     * @return true if the user is an admin or superadmin, false otherwise
     */
    public boolean hasAdminRights() {
        String discr = this.getDiscriminator();
        return discr.equals("admin") || discr.equals("superadmin");
    }

    /**
     * hacky fix so new db structure is compatible with old code
     * @return the discriminator of the class
     */
    @Transient
    public String getDiscriminator(){
        DiscriminatorValue val = this.getClass().getAnnotation( DiscriminatorValue.class );

        if (val == null) {
            return null; //TODO: because it "can" return null we should probably add some checks for it even though it should NEVER be null
        } else {
            return val.value();
        }
    }

    /**
     * used to search the database for Users
     */
    public static Finder<Long,User> find = new Finder<>(Long.class,User.class);

    /**
     *getter for id
     * @return id
     */
    public Long getId(){
        return this.id;
    }

    /**
     * setter for id
     * @param id you want it to be set to
     */
    public void setId(Long id) { this.id = id; }

    /**
     * shouldn't really be used as now the passwords are encrypted, but kept for compatability with old code
     * @return password of a user
     */
    public String getPassword(){
        return password;
    }

    /**
     * sets (and encrypts) a regular string password
     * @param password the password you want it to be set to
     * @return true if the password is set, false otherwise - so this should be checked to be certain the password is set
     */
    public boolean setPassword(String password){
        try{
            this.password = HashHelper.createPassword(password);
            return true;
        }catch (AppException e){
            return false;
        }
    }

    /**
     * sets the password without the Hash if it was needed
     * @param password password that the user's will be set to
     */
    public void setPasswordNoHash(String password) {
        this.password = password;
    }

    /**
     * getter for Name of user
     * @return name of the user
     */
    public String getName() {
        return name;
    }

    /**
     * setter for name of user
     * @param name that the user's will be set to
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter for email of user
     * @return email of the user
     */
    public String getEmail(){
        return email;
    }

    /**
     * setter for email of user
     * @param email that the user's will be set to
     */
    public void setEmail(String email){
        this.email=email;
    }

    /**
     * getter for school of the user
     * @return school of the user
     */
    public School getSchool(){
        return this.school;
    }

    /**
     * setter for school of the user
     * @param school the user's will be set to
     */
    public void setSchool(School school){
        this.school = school;
    }

    /**
     * don't use this, use setApproved instead. Not sure if it is semantically correct
     */
    public void approved(){
        this.approved = false;
    }

    /**
     * getter for the approved state (true/false)
     * @return the approved state of the user
     */
    public boolean getApproved() { return approved; }

    /**
     * setter for the approved state of the user
     * @param a approved of the user is set to a
     */
    public void setApproved(boolean a) { this.approved = a; }

    /**
     * getter for the Alumni profile
     * @return the profile of the alumni - if not an alumni then it returns null instead
     */
    public String getAlumniProfile() {
        if (this.getDiscriminator().equals("alumni")) {
            Alumni a = Alumni.find.byId(this.getId());
            return a.getProfile();
        } else {
            return null;
        }
    }

    /**
     * Setter for the Alumni profile
     * @param s the profile of the alumni is set to s
     */
    public void setAlumniProfile(String s) {
        if (this.getDiscriminator().equals("alumni")) {
            ((Alumni) this).setProfile(s);
        }
    }

    public String getUniversity()
    {
        return university;
    }

    public void setUniversity(String university)
    {
        this.university = university;
    }

    public String getOccupation()
    {
        return occupation;
    }

    public void setOccupation(String occupation)
    {
        this.occupation = occupation;
    }

    public String getIndustry()
    {
        return industry;
    }

    public void setIndustry(String industry)
    {
        this.industry = industry;
    }

    public String getEmployer()
    {
        return employer;
    }

    public void setEmployer(String employer)
    {
        this.employer = employer;
    }

}
