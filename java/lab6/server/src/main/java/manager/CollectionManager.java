package manager;

import lombok.Getter;
import lombok.Setter;
import models.Address;
import models.Organization;
import network.User;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    private final ReadWriteLock locker = new ReentrantReadWriteLock();

    /**
     * Добавить элемент в коллекцию.
     *
     * @param o элемент
     */
    public void add(Organization o, boolean flag) {
        locker.writeLock().lock();
        try {
            if (flag) {
                Long id = DBProvider.addOrganization(o);
                if (id != -1) {
                    o.setId(id);
                    collection.add(o);
                } else {
                    System.out.println("Error adding organization to the database.");
                }
            } else {
                collection.add(o);
            }
        } finally {
            locker.writeLock().unlock();
        }
    }


    /**
     * Обновить элемент коллекции по id.
     *
     * @param id id элемента
     * @param o  новый элемент
     */
    public void update(long id, Organization o) {
        locker.writeLock().lock();
        try {
            if (DBProvider.updateOrganization(id, o)) {
                collection.stream().filter(org -> org.getId() == id).forEach(org -> {
                    org.setName(o.getName());
                    org.setCoordinates(o.getCoordinates());
                    org.setCreationDate(o.getCreationDate());
                    org.setAnnualTurnover(o.getAnnualTurnover());
                    org.setFullName(o.getFullName());
                    org.setEmployeesCount(o.getEmployeesCount());
                    org.setType(o.getType());
                    org.setPostalAddress(o.getPostalAddress());
                });
                System.out.println("Organization updated!");
            } else {
                System.out.println("Error updating organization.");
            }
        } finally {
            locker.writeLock().unlock();
        }
    }

    /**
     * Удалить элемент по id.
     *
     * @param id id элемента
     */
    public void removeById(long id) {
        locker.writeLock().lock();
        try {
            if (DBProvider.removeOrganizationById(id)) {
                collection.removeIf(o -> o.getId() == id);
            }
        } finally {
            locker.writeLock().unlock();
        }
    }

    /**
     * Очистить коллекцию.
     */
    public void clear(User user) {
        locker.writeLock().lock();
        try {
            if (DBProvider.clearOrganizations(user)) {
                collection.clear();
                DBProvider.load(this);
            }
            initializationTime = new Date();
        } finally {
            locker.writeLock().unlock();
        }
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
        locker.writeLock().lock();
        if (collection.isEmpty() || collection.first().compareTo(o) > 0) {
            if (DBProvider.addOrganization(o) != -1) {
                collection.add(o);
            }
        }
        locker.writeLock().unlock();
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

    // TODO: removeGreater
    public void removeGreater(Organization o) {
        locker.writeLock().lock();
        try {
            // Remove from database
            DBProvider.removeOrganizationsGreaterThan(o);

            // Remove from collection
            collection.removeIf(organization -> organization.compareTo(o) > 0 && Objects.equals(organization.getCreator(), o.getCreator()));
        } finally {
            locker.writeLock().unlock();
        }
    }

    /**
     * Удалить элементы, значение поля annualTurnover которых меньше заданного.
     *
     * @param o объект для сравнения
     */
    // TODO: removeLower
    public void removeLower(Organization o) {
        locker.writeLock().lock();
        try {
            // Remove from database
            DBProvider.removeOrganizationsLessThan(o);

            // Remove from collection
            collection.removeIf(organization -> organization.compareTo(o) < 0 && Objects.equals(organization.getCreator(), o.getCreator()));
        } finally {
            locker.writeLock().unlock();
        }
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
