package models.forms;

import models.Location;

import java.util.Objects;

/**
 * Форма для создания объекта класса {@link Location}.
 */
public class LocationForm extends Form<Location> {

    /**
     * Формирует объект класса {@link Location}.
     *
     * @return Объект класса {@link Location}
     */
    @Override
    public Location build() {
        return new Location(
                askLong("координата x", " (целое число, поле не может быть пустым)", Objects::nonNull),
                askDouble("координата y", " (десятичная дробь, поле не может быть пустым)", Objects::nonNull),
                askString("название локации", " (строка, поле не может быть пустым)", s -> !s.isEmpty())
        );
    }
}
