package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;

import java.util.List;

/**
 * Data access object for School objects. It helps to perform database operations on the School table in the database.
 */
public class SchoolDAO {

    /**
     * Constructs the SchoolDAO.
     */
    public SchoolDAO() {}

    /**
     * Searches the database for the School which the paramter id matches the database ID.
     * @param id The ID of the school we want to retrieve.
     * @return The School object we wanted to retrieve or null if no match could be made.
     */
    public School getSchool(Long id) {
        return School.find.byId(id);
    }

    /**
     * Gives a List of all of the School objects in the database.
     * @return A List of all School objects in the database.
     */
    public List<School> getAllSchool() {
        List<School> result = School.find.all();
        return result;
    }

    /**
     * Searches by ID to find the School and then deletes this School from the database. Deleting a School will also delete all Questions and Users associated with that School.
     * @param id The ID of the School we want to delete.
     */
    public void deleteSchool(Long id) {
        SqlUpdate delete = Ebean.createSqlUpdate("DELETE FROM school WHERE school_id = :id");
        delete.setParameter("id",id);
        delete.execute();
    }

    /**
     * Since we have the assumption that School names are unique, we can search the database by name to find School objects. Searches the database for the School object where name matches the parameter name.
     * If two Schools with the same name are found it will throw a PersistenceException. However, this should not occur as checks are placed upon School creation forms to ensure name uniqueness.
     * @param name The name of the School we want to retrieve.
     * @return The School we wanted to retrieve or null if no School is found.
     * @throws javax.persistence.PersistenceException
     */
    public School byName(String name) {
        return School.find.where().eq("name", name).findUnique();
    }

}
