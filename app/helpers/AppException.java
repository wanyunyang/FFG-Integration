package helpers;

import java.security.NoSuchAlgorithmException;

/**
 * Used for errors in the HashHelper Class. Refer to the parent class NoSuchAlgorithmException for documentation
 *
 *
 */
public class AppException extends NoSuchAlgorithmException {
    public AppException(){
        super();
    }

    public AppException(String message){
        super(message);
    }

    public AppException(String message, Throwable cause){
        super(message,cause);
    }

    public AppException(Throwable cause){
        super(cause);
    }


}
