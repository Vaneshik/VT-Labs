package network;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
public class Response implements Serializable {
    private String message;
    private String collectionToStr;
    private boolean userAuthentication;

    public Response(String message, String collection) {
        this.message = message;
        this.collectionToStr = collection;
    }

    public Response(String message, boolean userAuthentication) {
        this.message = message;
        this.userAuthentication = userAuthentication;
    }
}
