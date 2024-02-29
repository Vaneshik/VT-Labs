public interface Command {
    void execute();
    void cancel();
    String name();
}
