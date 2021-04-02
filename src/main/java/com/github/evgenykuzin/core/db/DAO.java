package com.github.evgenykuzin.core.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public interface DAO<C, I extends Serializable> {
    void saveAll(Collection<C> objects);

    void save(C object);

    C get(I id);

    List<C> getAll();

    <V extends Serializable> List<C> searchBy(String keyName, V keyValue);

    List<C> searchBy(Predicate<? extends C> searchPredicate);

    void updateAll(Collection<C> objects);

    void update(C object);

    void deleteAll(Collection<C> objects);

    void delete(C object);

    default void executeVoid(SessionFactory sessionFactory, Command command) {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            command.execute(session);
            session.getTransaction().commit();
        }
    }

    default <T> T executeAndGet(SessionFactory sessionFactory, Getter<T> getter) {
        T t;
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            t = getter.get(session);
            session.getTransaction().commit();
        }
        return t;
    }

    interface Command {
        void execute(Session session);
    }

    interface Getter <T> {
        T get(Session session);
    }
}
