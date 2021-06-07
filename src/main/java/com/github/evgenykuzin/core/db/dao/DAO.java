package com.github.evgenykuzin.core.db.dao;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface DAO<C, I extends Serializable> {
    void saveAll(Collection<C> objects);

    void save(C object);

    C get(I id);

    List<C> getAll();

    List<C> searchBy(SearchEntry... searchEntries);

    void updateAll(Collection<C> objects);

    void update(C object);

    void deleteAll(Collection<C> objects);

    void delete(C object);

    void saveOrUpdate(C object);

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

    @lombok.Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    class SearchEntry {
        private final String keyName;
        private final String keyValue;
    }
}
