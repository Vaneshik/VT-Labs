package models.forms;

import manager.ConsoleManager;
import manager.IdManager;
import models.Address;
import models.Coordinates;
import models.Organization;
import models.OrganizationType;

import java.util.Date;

/**
 * Форма для создания объекта класса {@link Organization}.
 */
public class OrganizationForm extends Form<Organization> {
    private final ConsoleManager console;

    public OrganizationForm(ConsoleManager console) {
        super(console);
        this.console = console;
    }

    /**
     * Формирует объект класса {@link Organization}.
     *
     * @return Объект класса {@link Organization}
     */
    @Override
    public Organization build() {
        return new Organization(
                IdManager.generateId(),
                askString("название организации", " (строка, поле не может быть пустым)", s -> !s.isEmpty()),
                askCoordinates(),
                new Date(),
                askDouble("годовой оборот", " (десятичная дробь, значение должно быть больше нуля)", x -> (x == null || x > 0)),
                askString("полное название организации", " (строка, поле не может быть пустым)", s -> !s.isEmpty()),
                askInteger("количество сотрудников", " (целое число, поле не может быть пустым, значение должно быть больше нуля)", x -> (x != null && x > 0)),
                askOrganizationType(),
                askAddress()
        );
    }

    private Coordinates askCoordinates() {
        return new CoordinatesForm(console).build();
    }

    private Address askAddress() {
        return new AddressForm(console).build();
    }

    private OrganizationType askOrganizationType() {
        return (OrganizationType) askEnum("тип организации", OrganizationType.values(), s -> true);
    }
}
