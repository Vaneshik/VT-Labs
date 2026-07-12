package models.forms;

import models.Coordinates;

/**
 * Форма для создания объекта класса {@link Coordinates}.
 */
public class CoordinatesForm extends Form<Coordinates> {
    /**
     * Формирует объект класса {@link Coordinates}.
     *
     * @return Объект класса {@link Coordinates}
     */
    @Override
    public Coordinates build() {
        return new Coordinates(
                askDouble("координата x", " (десятичная дробь, поле не может быть пустым, максимальное значение = 30)", x -> (x != null && x <= 30)),
                askFloat("координата y", " (десятичная дробь)", x -> true)
        );
    }
}
