package models;

import helpers.AppException;
import helpers.HashHelper;
import views.forms.UserForm;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by biko on 05/02/15.
 */

@Entity
@DiscriminatorValue("student")
public class Student extends User{

    /**
     * Constructor for the Student class
     * @param name the student name will be set to
     * @param password the student password will be set to
     * @param email the student email will be set to
     * @param school the student school will be set to
     */
    public Student(String name, String password, String email, School school){
        super(name, password, email, school);
    }

    /**
     * makes an instance of a user
     * @param data the data from the form that will be associated with the new student
     * @return the new student will be returned
     */
    public static Student makeInstance(UserForm data) {
        School s = (new SchoolDAO()).byName(data.school.getName());
        String password = "";
        try {
            password = HashHelper.createPassword(data.password);
        } catch (AppException e) {
            //TODO: do something useful here maybe?
        }
        Student student = new Student(data.name, password, data.email, s);
        return student;
    }

    /**
     * finder that is used to search the database
     */
    public static Finder<Long,Student> find = new Finder<>(Long.class,Student.class);

}
