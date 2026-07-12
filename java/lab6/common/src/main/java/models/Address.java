package models;

import lombok.*;

import java.io.Serializable;

/**
 * Класс Адреса.
 *
 */
@Setter
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Address implements Comparable<Address>, Serializable {
    private String zipCode; //Поле не может быть null
    private Location town; //Поле может быть null

    @Override
    public int compareTo(Address o) {
        return zipCode.compareToIgnoreCase(o.zipCode);
    }
}
