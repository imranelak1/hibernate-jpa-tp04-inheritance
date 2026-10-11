package com.example.service;

import com.example.util.Jpa;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/** CRUD commun et lecture polymorphique avec Criteria API. */
public class GenericCrudService<T> {
    private final EntityManagerFactory factory;
    private final Class<T> type;
    public GenericCrudService(EntityManagerFactory factory, Class<T> type) {
        this.factory = factory; this.type = type;
    }
    public T create(T entity) {
        return Jpa.transaction(factory, em -> { em.persist(entity); return entity; });
    }
    public T find(Long id) { return Jpa.transaction(factory, em -> em.find(type, id)); }
    public T update(T entity) { return Jpa.transaction(factory, em -> em.merge(entity)); }
    public void delete(Long id) {
        Jpa.transaction(factory, em -> {
            T entity = em.find(type, id);
            if (entity == null) throw new IllegalArgumentException("Entite absente: " + id);
            em.remove(entity); return null;
        });
    }
    public List<T> list() {
        return Jpa.transaction(factory, em -> {
            CriteriaQuery<T> query = em.getCriteriaBuilder().createQuery(type);
            Root<T> root = query.from(type);
            query.select(root).orderBy(em.getCriteriaBuilder().asc(root.get("id")));
            return em.createQuery(query).getResultList();
        });
    }
}
