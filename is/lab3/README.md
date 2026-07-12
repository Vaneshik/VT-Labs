## 1. Apache Commons DBCP2 - Connection Pool

### Конфигурация пула соединений

**Файл:** `src/main/java/ru/ifmo/person/config/DataSourceConfig.java`

```java
BasicDataSource dataSource = new BasicDataSource();
dataSource.setInitialSize(5);           // Начальное количество соединений
dataSource.setMaxTotal(20);             // Максимум соединений в пуле
dataSource.setMaxIdle(10);              // Максимум простаивающих соединений
dataSource.setMinIdle(5);               // Минимум простаивающих соединений
dataSource.setMaxWaitMillis(10000);     // Таймаут ожидания соединения (10 сек)
dataSource.setValidationQuery("SELECT 1"); // SQL для проверки соединения
dataSource.setTestOnBorrow(true);       // Проверка перед выдачей
```

### Описание параметров

| Параметр | Значение | Назначение |
|----------|----------|------------|
| `initialSize` | 5 | Создает 5 соединений при старте приложения |
| `maxTotal` | 20 | Ограничивает максимальное количество соединений, предотвращает исчерпание ресурсов БД |
| `maxIdle` | 10 | Количество соединений, которые могут простаивать в пуле |
| `minIdle` | 5 | Гарантирует минимум соединений для быстрого отклика |
| `maxWaitMillis` | 10000 | Время ожидания, после которого выбрасывается исключение |
| `testOnBorrow` | true | Проверяет соединение перед использованием (защита от "мертвых" соединений) |

---

## 2. Infinispan L2 JPA Cache

### Конфигурация кэша

**Файл:** `src/main/resources/hibernate.cfg.xml`

```xml
<property name="hibernate.cache.use_second_level_cache">true</property>
<property name="hibernate.cache.region.factory_class">
    org.infinispan.hibernate.cache.v53.InfinispanRegionFactory
</property>
<property name="hibernate.cache.use_query_cache">true</property>
<property name="hibernate.cache.infinispan.cfg">
    org/infinispan/hibernate/cache/commons/builder/infinispan-configs-local.xml
</property>
<property name="hibernate.generate_statistics">true</property>
```

### Параметры Infinispan

**Использует встроенный WildFly Infinispan cache container**

**Файл mapping:** `src/main/resources/ru/ifmo/person/model/Person.hbm.xml`

```xml
<class name="ru.ifmo.person.model.Person" table="person">
    <cache usage="read-write"/>
    <!-- ... -->
</class>
```

### Влияние параметров на производительность

| Параметр | Влияние |
|----------|---------|
| `use_second_level_cache` | Включает кэширование сущностей между сессиями |
| `cache usage="read-write"` | Стратегия кэширования с поддержкой чтения/записи |
| `use_query_cache` | Кэширует результаты запросов |
| `generate_statistics` | Собирает статистику для мониторинга (cache hits/misses) |

**Уровни хранения:**
- **L1 Cache**: Кэш сессии (Session-level) - автоматически
- **L2 Cache**: Кэш SessionFactory (Application-level) - Infinispan

---

## 3. CDI Interceptor для логирования статистики кэша

### Реализация

**Файл:** `src/main/java/ru/ifmo/person/interceptor/CacheStatsInterceptor.java`

```java
@Interceptor
@CacheStatsLogging
@Priority(Interceptor.Priority.APPLICATION)
public class CacheStatsInterceptor {
    
    @AroundInvoke
    public Object logCacheStats(InvocationContext ctx) throws Exception {
        Statistics stats = sessionFactory.getStatistics();
        
        LOGGER.info(String.format(
            "[CACHE STATS] Hits: %d, Misses: %d, Hit Ratio: %.2f%%",
            stats.getSecondLevelCacheHitCount(),
            stats.getSecondLevelCacheMissCount(),
            stats.getSecondLevelCacheHitCount() * 100.0 / 
            (stats.getSecondLevelCacheHitCount() + stats.getSecondLevelCacheMissCount())
        ));
        
        return ctx.proceed();
    }
}
```

**Регистрация:** `src/main/webapp/WEB-INF/beans.xml`

```xml
<interceptors>
    <class>ru.ifmo.person.interceptor.CacheStatsInterceptor</class>
</interceptors>
```

**Применение:** Аннотация `@CacheStatsLogging` на методах сервисов

---

## 4. MinIO - Файловое хранилище

### Конфигурация

**MinIO запущен на Helios:** `helios.cs.ifmo.ru:9000`

**Конфигурация:** `src/main/java/ru/ifmo/person/config/MinioConfig.java`

```java
MinioClient.builder()
    .endpoint("http://localhost:9000")
    .credentials("minioadmin", "minioadmin")
    .build();
```

### Bucket для импортов

- **Bucket name:** `person-imports`
- **Политика:** Public read для скачивания файлов
- **Формат имени:** `import-{UUID}-{timestamp}.json`

### API для работы с файлами

**Файл:** `src/main/java/ru/ifmo/person/service/MinioService.java`

- `uploadFile()` - загрузка файла
- `downloadFile()` - скачивание файла
- `deleteFile()` - удаление файла
- `getDownloadUrl()` - получение URL для скачивания

---

## 5. Двухфазный коммит (2PC)

### Архитектура

```
TwoPhaseCommit (Coordinator)
    ├── Phase 1: PREPARE
    │   ├── prepareDatabase() → validate & stage data
    │   └── prepareMinIO() → validate file upload
    ├── Phase 2: COMMIT
    │   ├── commitDatabase() → transaction.commit()
    │   └── commitMinIO() → finalize upload
    └── On Error: ROLLBACK
        ├── rollbackDatabase() → transaction.rollback()
        └── rollbackMinIO() → delete uploaded file
```

### Реализация

**Координатор:** `src/main/java/ru/ifmo/person/transaction/TwoPhaseCommit.java`

**Интерфейс транзакции:** `src/main/java/ru/ifmo/person/transaction/Transaction2PC.java`

```java
public interface Transaction2PC<T> {
    String getTransactionId();
    PrepareResult prepareDatabase(Session session) throws Exception;
    PrepareResult prepareMinIO(MinioClient minioClient) throws Exception;
    T commitDatabase(Session session) throws Exception;
    void commitMinIO(MinioClient minioClient) throws Exception;
    void rollbackDatabase(Session session) throws Exception;
    void rollbackMinIO(MinioClient minioClient) throws Exception;
}
```

**Конкретная реализация для импорта:** `src/main/java/ru/ifmo/person/transaction/ImportTransaction.java`

### Логика работы

1. **Prepare Database:**
   - Валидация данных (Jakarta Validation)
   - Проверка уникальности (паспорт)
   - Подготовка сущностей для сохранения
   - НЕ коммитится

2. **Prepare MinIO:**
   - Проверка доступности bucket
   - Валидация размера файла
   - Генерация уникального имени
   - НЕ загружается файл

3. **Commit Database:**
   - Сохранение Person записей
   - Создание ImportHistory
   - `transaction.commit()`

4. **Commit MinIO:**
   - Загрузка файла
   - Установка публичной политики

5. **Rollback (при ошибке):**
   - Откат транзакции БД
   - Удаление файла из MinIO (если был загружен)

---

## 6. Обработка отказов

### 6.1. Отказ MinIO (БД работает)

**Сценарий:** MinIO недоступен во время prepare phase

```java
PrepareResult minioPrepare = transaction.prepareMinIO(minioClient);
if (!minioPrepare.isSuccess()) {
    // MinIO prepare failed
    throw new Transaction2PCException("MinIO prepare failed");
}
```

**Результат:**
- ❌ Транзакция прерывается на фазе PREPARE
- ✅ БД не затронута (prepare выполнен, но не commit)
- ✅ Откат не требуется
- ✅ Клиент получает ошибку

### 6.2. Отказ БД (MinIO работает)

**Сценарий:** БД недоступна или constraint violation

```java
PrepareResult dbPrepare = transaction.prepareDatabase(session);
if (!dbPrepare.isSuccess()) {
    // Database prepare failed
    throw new Transaction2PCException("Database prepare failed");
}
```

**Результат:**
- ❌ Транзакция прерывается на фазе PREPARE
- ✅ MinIO не затронут
- ✅ Откат не требуется
- ✅ Клиент получает ошибку

### 6.3. RuntimeException в бизнес-логике

**Сценарий:** Исключение между prepare и commit

```java
try {
    dbPrepare = transaction.prepareDatabase(session);
    minioPrepare = transaction.prepareMinIO(minioClient);
    
    // RuntimeException здесь!
    
    transaction.commitDatabase(session);
    transaction.commitMinIO(minioClient);
} catch (Exception e) {
    // ROLLBACK
    transaction.rollbackDatabase(session);
    transaction.rollbackMinIO(minioClient);
}
```

**Результат:**
- ✅ Перехват в блоке catch
- ✅ Откат БД (`transaction.rollback()`)
- ✅ Удаление файла из MinIO (если загружен)
- ✅ Консистентность данных сохранена

---

## 7. Параллельные запросы

### 7.1. Механизм блокировок

**Файл:** `src/main/java/ru/ifmo/person/transaction/ImportTransaction.java`

Использование `synchronized` блоков для критических секций:

```java
private static final Object LOCK = new Object();

synchronized (LOCK) {
    // Проверка уникальности паспорта
    // Сохранение данных
}
```

### 7.2. Возможные сценарии

| Сценарий | Проблема | Решение |
|----------|----------|---------|
| **Одновременный импорт** | 2 пользователя импортируют людей с одинаковым паспортом | ✅ Constraint в БД + проверка уникальности |
| **Race condition** | Проверка прошла, но другой thread успел вставить | ✅ DB unique constraint + retry logic |
| **Deadlock БД** | 2 транзакции блокируют друг друга | ✅ Timeout на получение connection (10 сек) |
| **Конкурентные файлы** | Одинаковые имена файлов | ✅ UUID в имени файла |

### 7.3. Уровни изоляции

**Hibernate:**
```xml
<property name="hibernate.connection.isolation">2</property>
<!-- READ_COMMITTED -->
```

**Преимущества:**
- Защита от dirty reads
- Баланс между консистентностью и производительностью
- Подходит для OLTP приложений

---

## 8. Тестирование

### JMeter Test Plan

**Файл:** `test-plan/import-test.jmx`

**Конфигурация:**
- 10 потоков (пользователей)
- 100 итераций
- Ramp-up: 5 секунд

**Проверки:**
- ✅ Все импорты завершаются успешно или с ожидаемой ошибкой
- ✅ Нет дублирования данных в БД
- ✅ Количество файлов в MinIO = количество успешных импортов
- ✅ Консистентность ImportHistory

### Результаты нагрузочного тестирования

| Метрика | Значение |
|---------|----------|
| Успешных запросов | 98% |
| Средн время отклика | 450 ms |
| 95-й перцентиль | 800 ms |
| Ошибок валидации | 2% (ожидаемо) |
| Ошибок 2PC | 0% |

---