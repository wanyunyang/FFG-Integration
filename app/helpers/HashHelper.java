package helpers;

import org.mindrot.jbcrypt.BCrypt;

/**
 *  Singleton Class used for encrypting the password of Users
 */
public class HashHelper {
    private static HashHelper instance = null;
    protected HashHelper(){
        //Exists only to defeat instantiation
    }

    /**
     * Used for getting the current instance of HashHelper. If there is no current instance it creates a new one and returns that
     * @return instance
     */
    public static HashHelper getInstance(){
        if(instance == null){
            instance = new HashHelper();
        }
        return instance;
    }

    /**
     * function for encrypting a regular string password.
     * @param pass the password that will be encrypted
     * @return The hash of the password (refer to org.mindrot.jbcrypt.BCrypt for further docs)
     * @throws AppException if there is a null password given as an argument
     */
    public static String createPassword(String pass) throws AppException {
        if (pass == null) {
            throw new AppException("empty.password");
        }
        return BCrypt.hashpw(pass, BCrypt.gensalt());
    }


    /**
     * function for checking the a string matches an encryptedPassword
     * @param candidate string for the password being checked
     * @param encryptedPassword encrypted password stored in the database
     * @return true if the passwords match otherwise false
     */
    public static boolean checkPassword(String candidate, String encryptedPassword){
        if(candidate == null){
            return false;
        }
        if (encryptedPassword == null){
            return false;
        }
        return BCrypt.checkpw(candidate,encryptedPassword);
    }
}
