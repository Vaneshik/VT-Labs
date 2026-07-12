package models;

import lombok.*;
import network.User;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

/**
 * Класс Организации.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Organization implements Comparable<Organization>, Serializable {
    private long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private Date creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Double annualTurnover; //Значение поля должно быть больше 0
    private String fullName; //Строка не может быть пустой, Поле не может быть null
    private Integer employeesCount; //Поле не может быть null, Значение поля должно быть больше 0
    private OrganizationType type; //Поле может быть null
    private Address postalAddress; //Поле не может быть null
    private String creator;

    @Override
    public int compareTo(Organization other) {
        return Comparator.comparingInt(Organization::getEmployeesCount)
                .thenComparingLong(Organization::getId)
                .compare(this, other);
    }

}