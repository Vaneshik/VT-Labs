package ru.ifmo.person.repository;

import ru.ifmo.person.model.Location;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

@ApplicationScoped
public class LocationRepository {

    @Inject
    private SessionFactory sessionFactory;
    
    private Session getSession() {
        return sessionFactory.openSession();
    }

    public List<Location> findAll() {
        try (Session session = getSession()) {
            return session.createQuery("FROM Location l ORDER BY l.id", Location.class)
                    .list();
        }
    }

    public List<Location> findDistinctLocations() {
        try (Session session = getSession()) {
            return session.createQuery(
                "SELECT l FROM Location l WHERE l.id IN " +
                "(SELECT MIN(l2.id) FROM Location l2 GROUP BY l2.name, l2.x, l2.y, l2.z) " +
                "ORDER BY l.name", 
                Location.class
            ).list();
        }
    }

    public Location findById(Integer id) {
        try (Session session = getSession()) {
            return session.get(Location.class, id);
        }
    }

    public void save(Location location) {
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            session.persist(location);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Location update(Location location) {
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            Location updated = (Location) session.merge(location);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void delete(Integer id) {
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            Location location = session.get(Location.class, id);
            if (location != null) {
                session.remove(location);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
