package models.forms;

import models.Address;
import models.Coordinates;
import models.Organization;
import models.OrganizationType;

import java.util.Date;
import java.util.Random;

/**
 * Форма для создания объекта класса {@link Organization}.
 */
public class OrganizationForm extends Form<Organization> {
    private final String username;

    public OrganizationForm(String username) {
        this.username = username;
    }

    /**
     * Формирует объект класса {@link Organization}.
     *
     * @return Объект класса {@link Organization}
     */
    @Override
    public Organization build() {
        return new Organization(
                -1,
                askString("название организации", " (строка, поле не может быть пустым)", s -> !s.isEmpty()),
                askCoordinates(),
                new Date(),
                askDouble("годовой оборот", " (десятичная дробь, значение должно быть больше нуля)", x -> (x == null || x > 0)),
                askString("полное название организации", " (строка, поле не может быть пустым)", s -> !s.isEmpty()),
                askInteger("количество сотрудников", " (целое число, поле не может быть пустым, значение должно быть больше нуля)", x -> (x != null && x > 0)),
                askOrganizationType(),
                askAddress(),
                this.username
        );
    }

    private Coordinates askCoordinates() {
        return new CoordinatesForm().build();
    }

    private Address askAddress() {
        return new AddressForm().build();
    }

    private OrganizationType askOrganizationType() {
        return (OrganizationType) askEnum("тип организации", OrganizationType.values(), s -> true);
    }
}
