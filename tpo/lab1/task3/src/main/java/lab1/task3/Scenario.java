package lab1.task3;

import lab1.task3.command.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Scenario {

    private final List<Command> commands = new ArrayList<>();

    public void add(Command command) {
        commands.add(Objects.requireNonNull(command, "command"));
    }

    public void run() {
        for (Command cmd : commands) {
            cmd.execute();
        }
    }

    public int size() {
        return commands.size();
    }
}
