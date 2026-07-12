package models;

import lombok.*;

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
public class Address implements Comparable<Address> {
    private String zipCode; //Поле не может быть null
    private Location town; //Поле может быть null

    @Override
    public int compareTo(Address o) {
        return zipCode.compareToIgnoreCase(o.zipCode);
    }
}
