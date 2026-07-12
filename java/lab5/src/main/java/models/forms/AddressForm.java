package models.forms;

import manager.ConsoleManager;
import models.Address;
import models.Location;

/**
 * Форма для создания объекта класса {@link Address}.
 */
public class AddressForm extends Form<Address> {
    private final ConsoleManager console;

    public AddressForm(ConsoleManager console) {
        super(console);
        this.console = console;
    }

    /**
     * Формирует объект класса {@link Address}.
     *
     * @return Объект класса {@link Address}
     */
    @Override
    public Address build() {
        return new Address(
                askString("почтовый индекс", " (строка, поле не может быть пустым)", s -> !s.isEmpty()),
                askLocation()
        );
    }

    private Location askLocation() {
        return new LocationForm(console).build();
    }
}
