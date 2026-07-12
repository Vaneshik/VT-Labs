package command;

import manager.CollectionManager;
import models.Organization;
import models.forms.AddressForm;
import network.Response;
import network.User;

/**
 * Команда "filter_less_than_postal_address".
 * Описание команды: вывести элементы, значение поля postalAddress которых меньше заданного.
 */
public class FilterLessThanPostalAddressCommand implements CommandInterface {
    CollectionManager manager;

    public FilterLessThanPostalAddressCommand(CollectionManager manager) {
        this.manager = manager;
    }

    /**
     * Выполнение команды.
     *
     * @param args               аргументы
     * @param organizationObject
     */
    @Override
    public Response execute(User user, String[] args, Organization organizationObject) {
        if (args.length != 0) {
            return new Response("Команда не принимает аргументы!", " ");
        }

        var response = manager.filterLessThanPostalAddress(new AddressForm().build());
        if (response.isEmpty()) {
            return new Response("Элементов не найдено", " ");
        } else {
            StringBuilder responseBuilder = new StringBuilder();
            responseBuilder.append("Элементы, значение поля postalAddress которых меньше заданного:").append("\n");
            response.stream().map(responseBuilder::append).forEach(s -> responseBuilder.append("\n"));
            return new Response(responseBuilder.toString(), " ");
        }
    }

    @Override
    public String toString() {
        return ": вывести элементы, значение поля postalAddress которых меньше заданного";
    }
}
