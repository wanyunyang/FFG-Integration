package models;

/**
 * Created by biko on 03/02/15.
 */

import java.util.List;


public interface UserDAO {

    public List getAllUsers();
    public User getUser(Long id);
    public void deleteUser(Long ID);


}
