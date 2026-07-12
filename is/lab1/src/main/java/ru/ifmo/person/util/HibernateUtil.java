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
            // System.out.println("=== Initializing Hibernate SessionFactory ===");
            Configuration configuration = new Configuration();
            
            // System.out.println("Loading hibernate.cfg.xml...");
            configuration.configure("hibernate.cfg.xml");
            
            // System.out.println("Adding Person mapping...");
            configuration.addResource("ru/ifmo/person/model/Person.hbm.xml");
            
            // System.out.println("Adding Location mapping...");
            configuration.addResource("ru/ifmo/person/model/Location.hbm.xml");
            
            // System.out.println("Building SessionFactory...");
            sf = configuration.buildSessionFactory();
            // System.out.println("=== SessionFactory created successfully ===");
        } catch (Exception e) {
            // System.err.println("=== FATAL: SessionFactory creation failed ===");
            e.printStackTrace();
            // System.err.println("=== Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "null"));
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

