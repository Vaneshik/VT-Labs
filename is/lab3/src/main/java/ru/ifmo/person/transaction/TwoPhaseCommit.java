package ru.ifmo.person.transaction;

import io.minio.MinioClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import ru.ifmo.person.config.MinioConfig;

import java.util.logging.Logger;

@ApplicationScoped
public class TwoPhaseCommit {

    private static final Logger LOGGER = Logger.getLogger(TwoPhaseCommit.class.getName());

    @Inject
    private SessionFactory sessionFactory;

    @Inject
    private MinioConfig minioConfig;

    public enum Phase {
        PREPARE, COMMIT, ROLLBACK
    }

    public <T> T execute(Transaction2PC<T> transaction) throws Transaction2PCException {
        String txId = transaction.getTransactionId();
        Session session = null;
        Transaction dbTransaction = null;
        
        Phase currentPhase = Phase.PREPARE;
        boolean dbPrepared = false;
        boolean minioPrepared = false;
        
        // Get MinioClient from config instead of injecting
        MinioClient minioClient = minioConfig.createMinioClient();
        
        try {
            LOGGER.info(String.format("[2PC:%s] Starting two-phase commit transaction", txId));
            
            session = sessionFactory.openSession();
            dbTransaction = session.beginTransaction();
            
            LOGGER.info(String.format("[2PC:%s] Phase 1: PREPARE - Database", txId));
            PrepareResult dbPrepare = transaction.prepareDatabase(session);
            if (!dbPrepare.isSuccess()) {
                LOGGER.severe(String.format("[2PC:%s] Database prepare failed: %s", txId, dbPrepare.getMessage()));
                throw new Transaction2PCException("Database prepare failed: " + dbPrepare.getMessage(), dbPrepare.getException());
            }
            dbPrepared = true;
            LOGGER.info(String.format("[2PC:%s] Database prepared successfully", txId));
            
            LOGGER.info(String.format("[2PC:%s] Phase 1: PREPARE - MinIO", txId));
            PrepareResult minioPrepare = transaction.prepareMinIO(minioClient);
            if (!minioPrepare.isSuccess()) {
                LOGGER.severe(String.format("[2PC:%s] MinIO prepare failed: %s", txId, minioPrepare.getMessage()));
                throw new Transaction2PCException("MinIO prepare failed: " + minioPrepare.getMessage(), minioPrepare.getException());
            }
            minioPrepared = true;
            LOGGER.info(String.format("[2PC:%s] MinIO prepared successfully", txId));
            
            currentPhase = Phase.COMMIT;
            LOGGER.info(String.format("[2PC:%s] Phase 2: COMMIT - Database", txId));
            T result = transaction.commitDatabase(session);
            dbTransaction.commit();
            LOGGER.info(String.format("[2PC:%s] Database committed successfully", txId));
            
            LOGGER.info(String.format("[2PC:%s] Phase 2: COMMIT - MinIO", txId));
            transaction.commitMinIO(minioClient);
            LOGGER.info(String.format("[2PC:%s] MinIO committed successfully", txId));
            
            LOGGER.info(String.format("[2PC:%s] Two-phase commit completed successfully", txId));
            return result;
            
        } catch (Exception e) {
            currentPhase = Phase.ROLLBACK;
            LOGGER.severe(String.format("[2PC:%s] Transaction failed, initiating rollback. Error: %s", txId, e.getMessage()));
            
            if (dbTransaction != null && dbTransaction.isActive()) {
                try {
                    LOGGER.info(String.format("[2PC:%s] Rolling back database transaction", txId));
                    transaction.rollbackDatabase(session);
                    dbTransaction.rollback();
                    LOGGER.info(String.format("[2PC:%s] Database rolled back successfully", txId));
                } catch (Exception rollbackEx) {
                    LOGGER.severe(String.format("[2PC:%s] Database rollback failed: %s", txId, rollbackEx.getMessage()));
                }
            }
            
            if (minioPrepared) {
                try {
                    LOGGER.info(String.format("[2PC:%s] Rolling back MinIO operations", txId));
                    transaction.rollbackMinIO(minioClient);
                    LOGGER.info(String.format("[2PC:%s] MinIO rolled back successfully", txId));
                } catch (Exception rollbackEx) {
                    LOGGER.severe(String.format("[2PC:%s] MinIO rollback failed: %s", txId, rollbackEx.getMessage()));
                }
            }
            
            if (e instanceof Transaction2PCException) {
                throw (Transaction2PCException) e;
            }
            throw new Transaction2PCException("Two-phase commit failed: " + e.getMessage(), e);
            
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
