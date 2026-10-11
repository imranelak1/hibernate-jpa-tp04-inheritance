package com.example;

import com.example.model.singletable.*;
import com.example.model.joined.*;
import com.example.model.tableperclass.*;
import com.example.service.GenericCrudService;
import com.example.util.Jpa;
import javax.persistence.*;

public class App {
    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("hibernate-inheritance");
        try {
            var vehicles = new GenericCrudService<>(factory, Vehicule.class);
            var employees = new GenericCrudService<>(factory, Employe.class);
            var products = new GenericCrudService<>(factory, Produit.class);
            Voiture voiture = new Voiture("Toyota", 4);
            Moto moto = new Moto("Harley", true);
            Developpeur dev = new Developpeur("Alice", "Java");
            Manager manager = new Manager("Bob", 3);
            Livre livre = new Livre("Clean Code", "Robert C. Martin");
            Electronique electronique = new Electronique("Smartphone", 24);
            Jpa.transaction(factory, em -> {
                em.persist(voiture); em.persist(moto); em.persist(dev);
                em.persist(manager); em.persist(livre); em.persist(electronique);
                return null;
            });
            System.out.println("=== TP04 - Trois strategies d'heritage ===");
            System.out.println("--- SINGLE_TABLE: Vehicule ---");
            vehicles.list().forEach(v -> System.out.println(v.getClass().getSimpleName()+": "+v.getMarque()+"; "+v.details()));
            System.out.println("--- JOINED: Employe ---");
            employees.list().forEach(e -> System.out.println(e.getClass().getSimpleName()+": "+e.getNom()+"; "+e.details()));
            System.out.println("--- TABLE_PER_CLASS: Produit ---");
            products.list().forEach(p -> System.out.println(p.getClass().getSimpleName()+": "+p.getNom()+"; "+p.details()));
            Jpa.transaction(factory, em -> {
                System.out.println("Stockage SINGLE_TABLE: Vehicule="+count(em,"Vehicule"));
                System.out.println("Stockage JOINED: Employe="+count(em,"Employe")+", Developpeur="+count(em,"Developpeur")+", Manager="+count(em,"Manager"));
                System.out.println("Stockage TABLE_PER_CLASS: Livre="+count(em,"Livre")+", Electronique="+count(em,"Electronique"));
                return null;
            });
            voiture.setMarque("Toyota Hybrid"); vehicles.update(voiture);
            manager.setNombreEquipes(4); employees.update(manager);
            electronique.setGarantie(36); products.update(electronique);
            System.out.println("UPDATE recharge: "+vehicles.find(voiture.getId()).getMarque()+"; "+employees.find(manager.getId()).details()+"; "+products.find(electronique.getId()).details());
            vehicles.delete(moto.getId()); employees.delete(dev.getId()); products.delete(livre.getId());
            System.out.println("DELETE verifie: Vehicule="+vehicles.list().size()+", Employe="+employees.list().size()+", Produit="+products.list().size());
            System.out.println("RESULTAT: CRUD complet et lectures polymorphiques valides pour les 3 strategies.");
        } finally { factory.close(); }
    }
    private static Number count(EntityManager em, String table) {
        return (Number) em.createNativeQuery("select count(*) from "+table).getSingleResult();
    }
}
