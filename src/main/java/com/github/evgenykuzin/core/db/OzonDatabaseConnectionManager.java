package com.github.evgenykuzin.core.db;

import com.github.evgenykuzin.core.data_managers.DataManagerFactory;
import com.github.evgenykuzin.core.entities.OzonProduct;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;
import java.util.stream.Collectors;

public class OzonDatabaseConnectionManager {
//    public static Configuration cfg = new Configuration()
//            .addAnnotatedClass(com.github.evgenykuzin.core.entities.OzonProduct.class)
//            .setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver")
//            .setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect")
//            .setProperty("hibernate.connection.url", "jdbc:mysql://a0524484.xsph.ru:3306/a0524484_nu-seller?serverTimezone=UTC")
//            .setProperty("hibernate.connection.username", "a0524484_nu-seller")
//            .setProperty("hibernate.connection.password", "emkuaridte")
//            .setProperty("hibernate.order_updates", "true")
//            .setProperty("hibernate.show_sql", "true");

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

                settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");

                settings.put(Environment.HBM2DDL_AUTO, "create-drop");

                configuration.setProperties(settings);

                configuration.addAnnotatedClass(OzonProduct.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties()).build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }

    public static void save(OzonProduct ozonProduct) {
        var ozonProductDAO = new OzonProductDAO();
        ozonProductDAO.save(ozonProduct);
    }

    public static OzonProduct get(Long ozonProductId) {
        var ozonProductDAO = new OzonProductDAO();
        return ozonProductDAO.get(ozonProductId);
    }

    public static void main(String[] args) {
        new OzonProductDAO().saveAll(DataManagerFactory.getOzonWebCsvManager()
                .parseProducts()
                .stream()
                .map(product -> (OzonProduct) product)
                .collect(Collectors.toList())
        );
    }
}
