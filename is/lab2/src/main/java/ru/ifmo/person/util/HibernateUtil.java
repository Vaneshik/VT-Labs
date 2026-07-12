package ru.ifmo.person.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@ApplicationScoped
public class HibernateUtil {

    private static SessionFactory sessionFactory;

    static {
        SessionFactory sf = null;
        try {
            Configuration configuration = new Configuration();
            
            configuration.configure("hibernate.cfg.xml");
            
            configuration.addResource("ru/ifmo/person/model/Person.hbm.xml");
            
            configuration.addResource("ru/ifmo/person/model/Location.hbm.xml");
            
            configuration.addResource("ru/ifmo/person/model/ImportHistory.hbm.xml");
            
            sf = configuration.buildSessionFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        sessionFactory = sf;
    }

    @Produces
    @ApplicationScoped
    public SessionFactory createSessionFactory() {
        if (sessionFactory == null) {
            throw new IllegalStateException("SessionFactory was not initialized properly. Check server logs for errors.");
        }
        return sessionFactory;
    }

    @Produces
    public Session createSession(SessionFactory sf) {
        return sf.openSession();
    }

    public void closeSession(@Disposes Session session) {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}

