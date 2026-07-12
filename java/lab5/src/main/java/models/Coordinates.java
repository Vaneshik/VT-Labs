package models;

import lombok.*;

/**
 * Класс Координат.
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Coordinates {
    private Double x; //Максимальное значение поля: 30, Поле не может быть null
    private Float y;
}
