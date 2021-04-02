package com.github.evgenykuzin.core.db;

import com.github.evgenykuzin.core.entities.OzonProduct;
import org.hibernate.SessionFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class OzonProductDAO implements DAO <OzonProduct, Long> {
    private final Supplier<SessionFactory> sessionFactorySupplier;

    public OzonProductDAO(Supplier<SessionFactory> sessionFactorySupplier) {
        this.sessionFactorySupplier = sessionFactorySupplier;
    }

    public OzonProductDAO() {
        this.sessionFactorySupplier = HibernateUtil::getSessionFactory;
    }

    public void saveAll(@NotNull final Collection<OzonProduct> ozonProducts) {
        executeVoid(session -> ozonProducts.forEach(session::save));
    }

    public void save(OzonProduct ozonProduct) {
        saveAll(Collections.singleton(ozonProduct));
    }

    public OzonProduct get(Long ozonProductId) {
        return executeAndGet(session -> session
                        .get(OzonProduct.class, ozonProductId));
    }

    public List<OzonProduct> getAll() {
        return executeAndGet(session -> {
            var cq = session
                    .getCriteriaBuilder()
                    .createQuery(OzonProduct.class);
            var all = cq
                    .select(cq.from(OzonProduct.class));
            return session.createQuery(all).getResultList();
        });
    }

    public <V extends Serializable> List<OzonProduct> searchBy(String keyName, V keyValue) {
        return executeAndGet(session -> {
            var builder = session
                    .getCriteriaBuilder();
            var cq = builder
                    .createQuery(OzonProduct.class);
            var select = cq.select(cq.from(OzonProduct.class))
                    .where(builder.equal(cq.from(OzonProduct.class), keyValue));
            return new ArrayList<>();
        });
    }

    public List<OzonProduct> searchBy(Predicate<? extends OzonProduct> searchPredicate) {
        return executeAndGet(session -> {
            return new ArrayList<>();
        });
    }

    public void updateAll(Collection<OzonProduct> objects) {
        executeVoid(session -> objects.forEach(session::update));
    }

    public void update(OzonProduct ozonProduct) {
        updateAll(Collections.singleton(ozonProduct));
    }

    public void deleteAll(Collection<OzonProduct> objects) {
        executeVoid(session -> objects.forEach(session::delete));
    }

    public void delete(OzonProduct ozonProduct) {
        deleteAll(Collections.singleton(ozonProduct));

    }

    private void executeVoid(Command command) {
        this.executeVoid(sessionFactorySupplier.get(), command);
    }

    private  <T> T executeAndGet(Getter<T> getter) {
        return this.executeAndGet(sessionFactorySupplier.get(), getter);
    }

    public Iterator<OzonProduct> iterator() {
        return new OzonProductsIterator(this);
    }

    public static class OzonProductsIterator implements Iterator<OzonProduct> {
        private Long reachedCount;
        private Long nextId;
        private final OzonProductDAO ozonProductDAO;
        public OzonProductsIterator(OzonProductDAO ozonProductDAO) {
            this.ozonProductDAO = ozonProductDAO;
            reachedCount = ozonProductDAO.executeAndGet(session -> {
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<Long> query = builder.createQuery(Long.class);
                Root<OzonProduct> root = query.from(OzonProduct.class);
                query.select(builder.count(root.get("id")));
                return session.createQuery(query).getSingleResult();
            });
        }

        @Override
        public boolean hasNext() {
            return reachedCount != null && reachedCount > 0;
        }

        @Override
        public OzonProduct next() {
            if (nextId == null) {

            }
            return null;
        }
    }
}
