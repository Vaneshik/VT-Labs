package exceptions;

public class IllegalMoominsCount extends RuntimeException {
    @Override
    public String toString() {
        return "В комнате не может быть больше трех муми-троллей!!!";
    }
}
