package com.example;

import com.example.model.singletable.*;
import com.example.model.joined.*;
import com.example.model.tableperclass.*;
import com.example.service.GenericCrudService;
import com.example.util.Jpa;
import javax.persistence.*;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class InheritanceTest {
    private EntityManagerFactory factory;
    @BeforeEach void open() {
        factory = Persistence.createEntityManagerFactory("hibernate-inheritance", Map.of(
            "javax.persistence.jdbc.url", "jdbc:h2:mem:inheritance_"+UUID.randomUUID(),
            "hibernate.show_sql", "false"));
    }
    @AfterEach void close() { factory.close(); }
    private long count(String table) {
        return Jpa.transaction(factory, em -> ((Number) em.createNativeQuery("select count(*) from "+table).getSingleResult()).longValue());
    }
    @Test void singleTableCrudAndDiscriminator() {
        var service = new GenericCrudService<>(factory, Vehicule.class);
        Voiture car = new Voiture("Toyota",4); Moto moto = new Moto("Harley",true);
        service.create(car); service.create(moto);
        assertInstanceOf(Voiture.class, service.find(car.getId()));
        assertTrue(((Moto)service.find(moto.getId())).getSidecar());
        assertEquals(2, count("Vehicule"));
        Jpa.transaction(factory, em -> {
            assertEquals("VOITURE", em.createNativeQuery("select vehicule_type from Vehicule where id=:id").setParameter("id",car.getId()).getSingleResult());
            assertEquals("MOTO", em.createNativeQuery("select vehicule_type from Vehicule where id=:id").setParameter("id",moto.getId()).getSingleResult());
            assertNull(em.createNativeQuery("select sidecar from Vehicule where id=:id").setParameter("id",car.getId()).getSingleResult());
            return null;
        });
        car.setNombrePortes(5); service.update(car);
        assertEquals(5, ((Voiture)service.find(car.getId())).getNombrePortes());
        service.delete(moto.getId()); assertNull(service.find(moto.getId())); assertEquals(1,count("Vehicule"));
    }
    @Test void joinedCrudAndParentForeignKeyRows() {
        var service = new GenericCrudService<>(factory, Employe.class);
        Developpeur dev = new Developpeur("Alice","Java"); Manager boss = new Manager("Bob",3);
        service.create(dev); service.create(boss);
        assertEquals(2,count("Employe")); assertEquals(1,count("Developpeur")); assertEquals(1,count("Manager"));
        assertEquals("Java",((Developpeur)service.find(dev.getId())).getLangage());
        boss.setNombreEquipes(4); service.update(boss);
        assertEquals(4,((Manager)service.find(boss.getId())).getNombreEquipes());
        service.delete(dev.getId());
        assertEquals(1,count("Employe")); assertEquals(0,count("Developpeur")); assertNull(service.find(dev.getId()));
    }
    @Test void tablePerClassCrudAndGloballyUniqueIds() {
        var service = new GenericCrudService<>(factory, Produit.class);
        Livre book = new Livre("Clean Code","Robert C. Martin"); Electronique phone = new Electronique("Smartphone",24);
        service.create(book); service.create(phone);
        assertNotEquals(book.getId(),phone.getId());
        assertEquals(1,count("Livre")); assertEquals(1,count("Electronique"));
        assertEquals("Robert C. Martin",((Livre)service.find(book.getId())).getAuteur());
        phone.setGarantie(36); service.update(phone);
        assertEquals(36,((Electronique)service.find(phone.getId())).getGarantie());
        service.delete(book.getId()); assertNull(service.find(book.getId())); assertEquals(0,count("Livre"));
        Jpa.transaction(factory, em -> {
            assertEquals(0,((Number) em.createNativeQuery("select count(*) from INFORMATION_SCHEMA.TABLES where TABLE_NAME='PRODUIT'").getSingleResult()).intValue());
            return null;
        });
    }
    @Test void criteriaQueriesReturnConcreteSubtypesForAllHierarchies() {
        Jpa.transaction(factory, em -> {
            em.persist(new Voiture("Toyota",4)); em.persist(new Moto("Harley",true));
            em.persist(new Developpeur("Alice","Java")); em.persist(new Manager("Bob",3));
            em.persist(new Livre("Clean Code","Robert C. Martin")); em.persist(new Electronique("Smartphone",24));
            return null;
        });
        var vehicles = new GenericCrudService<>(factory,Vehicule.class).list();
        var employees = new GenericCrudService<>(factory,Employe.class).list();
        var products = new GenericCrudService<>(factory,Produit.class).list();
        assertEquals(List.of(Voiture.class,Moto.class),vehicles.stream().map(Object::getClass).toList());
        assertEquals(List.of(Developpeur.class,Manager.class),employees.stream().map(Object::getClass).toList());
        assertEquals(List.of(Livre.class,Electronique.class),products.stream().map(Object::getClass).toList());
    }
    @Test void deletingMissingEntityRollsBackAndLeavesExistingData() {
        var service = new GenericCrudService<>(factory,Vehicule.class);
        service.create(new Moto("Harley",true));
        assertThrows(IllegalArgumentException.class, () -> service.delete(999L));
        assertEquals(1,service.list().size());
    }
}
