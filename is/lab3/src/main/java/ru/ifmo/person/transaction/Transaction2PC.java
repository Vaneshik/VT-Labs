package ru.ifmo.person.transaction;

import io.minio.MinioClient;
import org.hibernate.Session;

public interface Transaction2PC<T> {
    
    PrepareResult prepareDatabase(Session session) throws Exception;
    
    PrepareResult prepareMinIO(MinioClient client) throws Exception;
    
    T commitDatabase(Session session) throws Exception;
    
    void commitMinIO(MinioClient client) throws Exception;
    
    void rollbackDatabase(Session session);
    
    void rollbackMinIO(MinioClient client);
    
    String getTransactionId();
}
