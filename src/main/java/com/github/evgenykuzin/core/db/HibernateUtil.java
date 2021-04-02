package com.github.evgenykuzin.core.db;

import com.github.evgenykuzin.core.entities.OzonProduct;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();

                // Hibernate settings equivalent to hibernate.cfg.xml's properties
                Properties settings = new Properties();
                settings.put(Environment.DRIVER, "com.mysql.jdbc.Driver");
                settings.put(Environment.URL, "jdbc:mysql://a0524484.xsph.ru:3306/a0524484_nu-seller?serverTimezone=UTC");
                settings.put(Environment.USER, "a0524484_nu-seller");
                settings.put(Environment.PASS, "emkuaridte");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
                settings.put(Environment.SHOW_SQL, "true");
                //settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
                //settings.put(Environment.HBM2DDL_AUTO, "create-drop");

                configuration
                        .addPackage("com.github.evgenykuzin.core.entities");

                configuration
                        .setProperties(settings);

                configuration
                        .addAnnotatedClass(OzonProduct.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties()).build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }

    public static SessionFactory getTestSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();

                // Hibernate settings equivalent to hibernate.cfg.xml's properties
                Properties settings = new Properties();
                settings.put(Environment.DRIVER, "com.mysql.jdbc.Driver");
                settings.put(Environment.URL, "jdbc:mysql://a0524484.xsph.ru:3306/a0524484_nu-seller?serverTimezone=UTC");
                settings.put(Environment.USER, "a0524484_nu-seller");
                settings.put(Environment.PASS, "emkuaridte");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
                //settings.put(Environment.SHOW_SQL, "true");
                //settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
                //settings.put(Environment.HBM2DDL_AUTO, "create-drop");

                configuration
                        .addPackage("com.github.evgenykuzin.core.entities");

                configuration
                        .setProperties(settings);

                configuration
                        .addAnnotatedClass(OzonProduct.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties()).build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }

    private HibernateUtil() {
    }

    public static void close() {
        sessionFactory.close();
    }
}
