// вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)
public class InfoCommand implements Command{
    @Override
    public void execute() {
        System.out.println("InfoCommand executed");
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String name() {
        return "InfoCommand";
    }
}
