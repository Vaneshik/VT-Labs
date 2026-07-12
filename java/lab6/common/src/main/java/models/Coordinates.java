package models;

import lombok.*;

import java.io.Serializable;

/**
 * Класс Координат.
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Coordinates implements Serializable {
    private Double x; //Максимальное значение поля: 30, Поле не может быть null
    private Float y;
}
