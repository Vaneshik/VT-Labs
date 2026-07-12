package models.forms;

import models.Address;
import models.Location;

/**
 * Форма для создания объекта класса {@link Address}.
 */
public class AddressForm extends Form<Address> {

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
        return new LocationForm().build();
    }
}
