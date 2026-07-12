package models;

import lombok.*;

import java.io.Serializable;

/**
 * Класс Локации.
 */
@AllArgsConstructor
@Setter
@Getter
@ToString
@NoArgsConstructor
public class Location  implements Serializable {
    private Long x; //Поле не может быть null
    private Double y; //Поле не может быть null
    private String name; //Строка не может быть пустой, Поле может быть null

}
