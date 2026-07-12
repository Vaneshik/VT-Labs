package manager;

import models.Address;
import models.Coordinates;
import models.Location;
import models.Organization;

/**
 * Класс для валидации объектов.
 */
public class ValidationManager {

    private ValidationManager() {
    }

    /**
     * Проверяет валидность организации.
     *
     * @param o               объект организации
     * @param collectionManager менеджер коллекции
     * @return true, если объект валиден, иначе false
     */
    public static boolean isValidOrganization(Organization o, CollectionManager collectionManager) {
        return o.getId() > 0 &&
                collectionManager.getById(o.getId()) == null &&

                o.getName() != null &&
                !o.getName().isEmpty() &&

                o.getCoordinates() != null &&
                isValidCoordinates(o.getCoordinates()) &&

                o.getCreationDate() != null &&

                o.getAnnualTurnover() > 0 &&

                o.getFullName() != null &&
                !o.getFullName().isEmpty() &&

                o.getEmployeesCount() != null &&
                o.getEmployeesCount() > 0 &&

                o.getType() != null &&

                o.getPostalAddress() != null &&
                isValidAddress(o.getPostalAddress());

    }

    /**
     * Проверяет валидность адреса.
     *
     * @param o объект адреса
     * @return true, если объект валиден, иначе false
     */
    public static boolean isValidAddress(Address o) {
        return o.getZipCode() != null &&
                o.getTown() != null &&
                isValidLocation(o.getTown());
    }

    /**
     * Проверяет валидность локации.
     *
     * @param o объект локации
     * @return true, если объект валиден, иначе false
     */
    public static boolean isValidLocation(Location o) {
        return o.getX() != null &&
                o.getY() != null &&
                o.getName() != null &&
                !o.getName().isEmpty();
    }

    /**
     * Проверяет валидность координат.
     *
     * @param o объект координат
     * @return true, если объект валиден, иначе false
     */
    public static boolean isValidCoordinates(Coordinates o) {
        return o.getX() != null;
    }
}
