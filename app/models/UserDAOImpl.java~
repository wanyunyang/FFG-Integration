package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.SqlUpdate;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by biko on 03/02/15.
 */
public class UserDAOImpl implements UserDAO {

    public UserDAOImpl() {}

    /**
     *
     * @return a list of all users
     */
    public List<User> getAllUsers(){
        List<User> users = User.find.all();
        return users;
    }

    /**
     *
     * @param school the school you wish to get the users for
     * @return a list of all users associated with the school
     */
    public List<User> getSchoolUsers(School school) {
        ExpressionList<User> expList = User.find.where().eq("school",school);
        List<User> users = expList.findList();
        return users;
    }

    /**
     * same as getSchoolUsers but now no SuperAdmin's will be present
     * @param school the school you wish to get the users for
     * @return a list of all users associated with the school without Super Admin's
     */
    public List<User> getSchoolUsersNoSA(School school) {
        ExpressionList<User> expList = User.find.where().eq("school",school).ne("discriminator","superadmin");
        List<User> users = expList.findList();
        return users;
    }

    /**
     *
     * @param school the school you want to get the unapproved users from
     * @return a list of all the users that are unapproved associated with the school
     */
    public List<User> getUnapprovedSchoolUsers(School school) {
        ExpressionList<User> expList = User.find.where().eq("school",school).eq("approved",false);
        List<User> users = expList.findList();
        return users;
    }

    /**
     * same as getUnapprovedSchoolUsers but now without Super Admins
     * @param school the school you want to get the unapproved users from
     * @return a list of all the unapproved users associated with the school not including Super Admins
     */
    public List<User> getUnapprovedSchoolUsersNoSA(School school) {
        ExpressionList<User> expList = User.find.where().eq("school",school).ne("discriminator","superadmin").eq("approved",false);
        List<User> users = expList.findList();
        return users;
    }

    /**
     * gets a user associated with the ID
     * @param ID the ID of the user you want to get
     * @return the user associated with the id - null if none do
     */
    public User getUser(Long ID){
        User user = User.find.byId(ID);
        if (user == null) {
            return null;
        } else if (user.getDiscriminator().equals("student")) {
            Student s = new Student(user.getName(),user.getPassword(),user.getEmail(),user.getSchool());
            s.setId(user.getId());
            s.setApproved(user.getApproved());
            return s;
        } else if (user.getDiscriminator().equals("alumni")) {
            Alumni a = new Alumni(user.getName(),user.getPassword(),user.getEmail(),user.getSchool());
            a.setId(user.getId());
            a.setApproved(user.getApproved());
            return a;
        } else if (user.getDiscriminator().equals("admin")) {
            Admin ad = new Admin(user.getName(),user.getPassword(),user.getEmail(),user.getSchool());
            ad.setId(user.getId());
            ad.setApproved(user.getApproved());
            return ad;
        } else if (user.getDiscriminator().equals("superadmin")) {
            SuperAdmin sa = new SuperAdmin(user.getName(),user.getPassword(),user.getEmail(),user.getSchool());
            sa.setId(user.getId());
            sa.setApproved(user.getApproved());
            return sa;
        }
        else return user;
    }

    /**
     *
     * @param email of the user you want to get
     * @return the user associated with email
     */
    public User getUserByEmail(String email) {
        User u = User.find.where().eq("email",email).findUnique();
        return u;
    }

    /**
     * gets the User from the HTTP context
     * @return the user associated with email in the session
     */
    public User getUserFromContext() {
        String email = Http.Context.current().session().get("email");
        User user = null;
        if (email != null) {
            user = getUserByEmail(email);
        }
        return user;
    }

    /**
     * approves the user with ID
     * @param ID of the user
     */
    public void approveUser(Long ID) {
        User user = getUser(ID);
        user.setApproved(true);
        user.update();
    }

    /**
     * deletes the user with ID
     * @param ID of the user you want to delete
     */
    public void deleteUser(Long ID){
        SqlUpdate delete = Ebean.createSqlUpdate("DELETE FROM user WHERE id = :id");
        delete.setParameter("id",ID);
        delete.execute();
    }

}
