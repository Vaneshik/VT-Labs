package ru.ifmo.person.repository;

import ru.ifmo.person.enumeration.Country;
import ru.ifmo.person.model.Person;
import ru.ifmo.person.model.Location;
import ru.ifmo.person.enumeration.Color;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

@ApplicationScoped
public class PersonRepository {

    @Inject
    private SessionFactory sessionFactory;
    
    private Session getSession() {
        return sessionFactory.openSession();
    }

    public List<Person> findAll() {
        try (Session session = getSession()) {
            return session.createQuery("FROM Person p LEFT JOIN FETCH p.location ORDER BY p.id", Person.class)
                    .list();
        }
    }

    public List<Person> findAll(int offset, int limit, String sortBy) {
        try (Session session = getSession()) {
            String orderClause = "p.id";
            if (sortBy != null && !sortBy.isEmpty()) {
                switch (sortBy.toLowerCase()) {
                    case "name":
                        orderClause = "p.name";
                        break;
                    case "creationdate":
                        orderClause = "p.creationDate DESC";
                        break;
                    case "height":
                        orderClause = "p.height";
                        break;
                    case "weight":
                        orderClause = "p.weight";
                        break;
                    default:
                        orderClause = "p.id";
                }
            }
            String hql = "FROM Person p LEFT JOIN FETCH p.location ORDER BY " + orderClause;
            Query<Person> query = session.createQuery(hql, Person.class);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            return query.list();
        }
    }

    public long count() {
        try (Session session = getSession()) {
            return session.createQuery("SELECT COUNT(p) FROM Person p", Long.class)
                    .uniqueResult();
        }
    }

    public List<Person> findFiltered(int offset, int limit, String name, Country nationality, String sortBy) {
        try (Session session = getSession()) {
            StringBuilder hql = new StringBuilder("FROM Person p LEFT JOIN FETCH p.location WHERE 1=1");
            
            if (name != null && !name.isEmpty()) {
                hql.append(" AND p.name LIKE :name");
            }
            if (nationality != null) {
                hql.append(" AND p.nationality = :nationality");
            }
            
            String orderClause = "p.id";
            if (sortBy != null && !sortBy.isEmpty()) {
                switch (sortBy.toLowerCase()) {
                    case "name":
                        orderClause = "p.name";
                        break;
                    case "creationdate":
                        orderClause = "p.creationDate DESC";
                        break;
                    case "height":
                        orderClause = "p.height";
                        break;
                    case "weight":
                        orderClause = "p.weight";
                        break;
                    default:
                        orderClause = "p.id";
                }
            }
            hql.append(" ORDER BY ").append(orderClause);
            
            Query<Person> query = session.createQuery(hql.toString(), Person.class);
            
            if (name != null && !name.isEmpty()) {
                query.setParameter("name", "%" + name + "%");
            }
            if (nationality != null) {
                query.setParameter("nationality", nationality);
            }
            
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            return query.list();
        }
    }

    public long countFiltered(String name, Country nationality) {
        try (Session session = getSession()) {
            StringBuilder hql = new StringBuilder("SELECT COUNT(p) FROM Person p WHERE 1=1");
            
            if (name != null && !name.isEmpty()) {
                hql.append(" AND p.name LIKE :name");
            }
            if (nationality != null) {
                hql.append(" AND p.nationality = :nationality");
            }
            
            Query<Long> query = session.createQuery(hql.toString(), Long.class);
            
            if (name != null && !name.isEmpty()) {
                query.setParameter("name", "%" + name + "%");
            }
            if (nationality != null) {
                query.setParameter("nationality", nationality);
            }
            
            return query.uniqueResult();
        }
    }

    public Person findById(Long id) {
        try (Session session = getSession()) {
            return session.createQuery("FROM Person p LEFT JOIN FETCH p.location WHERE p.id = :id", Person.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }

    public List<Person> findByName(String name) {
        try (Session session = getSession()) {
            return session.createQuery("FROM Person p LEFT JOIN FETCH p.location WHERE p.name = :name", Person.class)
                    .setParameter("name", name)
                    .list();
        }
    }

    public List<Person> findByNationality(Country nationality) {
        try (Session session = getSession()) {
            return session.createQuery("FROM Person p LEFT JOIN FETCH p.location WHERE p.nationality = :nationality", Person.class)
                    .setParameter("nationality", nationality)
                    .list();
        }
    }

    public void save(Person person) {
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            session.persist(person);
            session.flush();
            session.refresh(person);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void save(Person person, Integer locationId) {
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            if (locationId != null) {
                Location location = session.get(Location.class, locationId);
                person.setLocation(location);
            }
            session.persist(person);
            session.flush();
            session.refresh(person);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Person update(Person person) {
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            Person updated = (Person) session.merge(person);
            session.flush();
            session.refresh(updated);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void update(Person person, Integer locationId) {
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            if (locationId != null) {
                Location location = session.get(Location.class, locationId);
                person.setLocation(location);
            } else {
                person.setLocation(null);
            }
            session.merge(person);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void delete(Long id) {
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            Person person = session.get(Person.class, id);
            if (person == null) {
                tx.rollback();
                throw new IllegalArgumentException("Person not found");
            }
            session.remove(person);
            tx.commit();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void deleteByNationality(Country nationality) {
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            session.createQuery("DELETE FROM Person p WHERE p.nationality = :nationality")
                    .setParameter("nationality", nationality)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Double getAverageHeight() {
        try (Session session = getSession()) {
            Object result = session.createQuery("SELECT AVG(p.height) FROM Person p WHERE p.height IS NOT NULL")
                    .uniqueResult();
            if (result == null) {
                return 0.0;
            }
            if (result instanceof Double) {
                return (Double) result;
            }
            return ((Number) result).doubleValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public List<Country> getUniqueNationalities() {
        try (Session session = getSession()) {
            return session.createQuery("SELECT DISTINCT p.nationality FROM Person p WHERE p.nationality IS NOT NULL ORDER BY p.nationality", Country.class)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public Double getHairColorPercentage(Color hairColor) {
        try (Session session = getSession()) {
            Long total = session.createQuery("SELECT COUNT(p) FROM Person p", Long.class)
                    .uniqueResult();
            
            if (total == 0) {
                return 0.0;
            }
            
            Long colorCount = session.createQuery("SELECT COUNT(p) FROM Person p WHERE p.hairColor = :color", Long.class)
                    .setParameter("color", hairColor)
                    .uniqueResult();
            
            return (colorCount.doubleValue() / total.doubleValue()) * 100.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public Long countByHairColorAndLocation(Color hairColor, Integer locationId) {
        try (Session session = getSession()) {
            if (locationId == null) {
                return session.createQuery("SELECT COUNT(p) FROM Person p WHERE p.hairColor = :color AND p.location IS NULL", Long.class)
                        .setParameter("color", hairColor)
                        .uniqueResult();
            } else {
                return session.createQuery("SELECT COUNT(p) FROM Person p WHERE p.hairColor = :color AND p.location.id = :locationId", Long.class)
                        .setParameter("color", hairColor)
                        .setParameter("locationId", locationId)
                        .uniqueResult();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public Person findByPassportID(String passportID) {
        try (Session session = getSession()) {
            return session.createQuery("FROM Person p WHERE p.passportID = :passportID", Person.class)
                    .setParameter("passportID", passportID)
                    .uniqueResult();
        }
    }

    public Person findByNameAndCoordinates(String name, double x, int y) {
        try (Session session = getSession()) {
            return session.createQuery("FROM Person p WHERE p.name = :name AND p.coordinates.x = :x AND p.coordinates.y = :y", Person.class)
                    .setParameter("name", name)
                    .setParameter("x", x)
                    .setParameter("y", y)
                    .uniqueResult();
        }
    }

    public void saveAll(List<Person> persons) {
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            for (Person person : persons) {
                session.persist(person);
            }
            session.flush();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Batch save failed: " + e.getMessage(), e);
        }
    }

    public void saveAllInTransaction(List<Person> persons, Session session) {
        for (Person person : persons) {
            session.persist(person);
        }
        session.flush();
    }
}
