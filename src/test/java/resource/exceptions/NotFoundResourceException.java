package resource.exceptions;

public class NotFoundResourceException extends Exception{
    public NotFoundResourceException(){
        super("Resource not found");
    }
}
