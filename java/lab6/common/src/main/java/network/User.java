package network;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class User implements Serializable {
    private String username;
    private String password;
}
