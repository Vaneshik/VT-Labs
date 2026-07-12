package manager;

import lombok.Getter;
import lombok.Setter;
import models.Address;
import models.Organization;

import java.util.Date;
import java.util.List;
import java.util.TreeSet;

/**
 * Класс для управления коллекцией.
 */
@Getter
@Setter
public class CollectionManager {
    /**
     * Время инициализации коллекции.
     */
    public static Date initializationTime = new Date();

    /**
     * Коллекция.
     */
    private TreeSet<Organization> collection = new TreeSet<Organization>();

    /**
     * Добавить элемент в коллекцию.
     *
     * @param o элемент
     */
    public void add(Organization o) {
        collection.add(o);
    }

    /**
     * Обновить элемент коллекции по id.
     *
     * @param id id элемента
     * @param o  новый элемент
     */
    public void update(long id, Organization o) {
        collection.stream().filter(organization -> organization.getId() == id).forEach(organization -> {
            organization.setName(o.getName());
            organization.setCoordinates(o.getCoordinates());
            organization.setCreationDate(o.getCreationDate());
            organization.setAnnualTurnover(o.getAnnualTurnover());
            organization.setFullName(o.getFullName());
            organization.setEmployeesCount(o.getEmployeesCount());
            organization.setType(o.getType());
            organization.setPostalAddress(o.getPostalAddress());
        });
    }

    /**
     * Удалить элемент по id.
     *
     * @param id id элемента
     */
    public void removeById(long id) {
        collection.removeIf(o -> o.getId() == id);
    }

    /**
     * Очистить коллекцию.
     */
    public void clear() {
        initializationTime = new Date();
        collection.clear();
    }

    /**
     * Вернуть первый(минимальный) элемент коллекции.
     *
     * @return первый элемент коллекции
     */
    public String minByFullName() {
        return collection.stream().map(Organization::getFullName).min(String::compareTo).orElse(null);
    }

    /**
     * Добавить элемент в коллекцию, если он меньше минимального.
     *
     * @param o элемент
     */
    public void addIfMin(Organization o) {
        if (collection.isEmpty() || collection.first().compareTo(o) > 0) {
            collection.add(o);
        }
    }

    /**
     * Вернуть список элементов, значение поля name которых содержит заданную подстроку.
     *
     * @param name подстрока
     */
    public List<Organization> filterContainsName(String name) {
        return collection.stream().filter(o -> o.getName().contains(name)).toList();
    }

    /**
     * Вернуть список элементов, значение поля postalAddress которых меньше заданного.
     *
     * @param postalAddress индекс
     */
    public List<Organization> filterLessThanPostalAddress(Address postalAddress) {
        return collection.stream().filter(o -> o.getPostalAddress().compareTo(postalAddress) < 0).toList();
    }

    /**
     * Удалить элементы, которые больше заданного.
     *
     * @param o объект для сравнения
     */
    public void removeGreater(Organization o) {
        collection.removeIf(organization -> organization.compareTo(o) > 0);
    }

    /**
     * Удалить элементы, значение поля annualTurnover которых меньше заданного.
     *
     * @param o объект для сравнения
     */
    public void removeLower(Organization o) {
        collection.removeIf(organization -> organization.compareTo(o) < 0);
    }

    /**
     * Вернуть элемент по id.
     *
     * @param id id элемента
     */
    public Organization getById(long id) {
        return collection.stream().filter(o -> o.getId() == id).findFirst().orElse(null);
    }
}
