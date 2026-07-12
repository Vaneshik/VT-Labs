package network;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@ToString
@Getter
public class Request implements Serializable {
    private User user;
    private boolean registerRequired;
    private String commandName;
    private String[] commandStrArg;
    private Serializable commandObjArg;

    public Request(User user, String commandName, String[] commandStrArg, Serializable commandObjArg) {
        this.user = user;
        this.commandName = commandName;
        this.commandStrArg = commandStrArg;
        this.commandObjArg = commandObjArg;
    }

    public Request(User user, String commandName, String[] commandStrArg) {
        this(user, commandName, commandStrArg, null);
    }

    public Request(User user, boolean registerRequired) {
        this.user = user;
        this.registerRequired = registerRequired;
    }
}
