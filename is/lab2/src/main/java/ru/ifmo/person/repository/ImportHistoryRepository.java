package ru.ifmo.person.repository;

import ru.ifmo.person.model.ImportHistory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

@ApplicationScoped
public class ImportHistoryRepository {

    @Inject
    private SessionFactory sessionFactory;
    
    private Session getSession() {
        return sessionFactory.openSession();
    }

    public List<ImportHistory> findAll() {
        try (Session session = getSession()) {
            return session.createQuery("FROM ImportHistory ORDER BY createdAt DESC", ImportHistory.class)
                    .list();
        }
    }

    public List<ImportHistory> findByUsername(String username) {
        try (Session session = getSession()) {
            return session.createQuery("FROM ImportHistory WHERE username = :username ORDER BY createdAt DESC", ImportHistory.class)
                    .setParameter("username", username)
                    .list();
        }
    }

    public ImportHistory findById(Long id) {
        try (Session session = getSession()) {
            return session.get(ImportHistory.class, id);
        }
    }

    public ImportHistory save(ImportHistory importHistory) {
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            session.persist(importHistory);
            session.flush();
            session.refresh(importHistory);
            tx.commit();
            return importHistory;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public long count() {
        try (Session session = getSession()) {
            return session.createQuery("SELECT COUNT(ih) FROM ImportHistory ih", Long.class)
                    .uniqueResult();
        }
    }

    public long countByUsername(String username) {
        try (Session session = getSession()) {
            return session.createQuery("SELECT COUNT(ih) FROM ImportHistory ih WHERE ih.username = :username", Long.class)
                    .setParameter("username", username)
                    .uniqueResult();
        }
    }
}
