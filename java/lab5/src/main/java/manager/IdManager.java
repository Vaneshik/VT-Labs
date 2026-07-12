package manager;

/**
 * Класс для генерации id.
 */
public class IdManager {
    private static long id = 1;
    private static CollectionManager collectionManager;

    private IdManager() {
    }

    /**
     * Установить CollectionManager.
     *
     * @param collectionManager CollectionManager
     */
    public static void setCollectionManager(CollectionManager collectionManager) {
        IdManager.collectionManager = collectionManager;
    }


    /**
     * Генерация id.
     *
     * @return id
     */
    public static long generateId() {
        if (collectionManager == null) {
            throw new NullPointerException("CollectionManager не инициализирован");
        }
        while (collectionManager.getById(id) != null) {
            id++;
        }
        return id;
    }
}
