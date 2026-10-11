package com.example.model.tableperclass;
import javax.persistence.*;
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)

public abstract class Produit {
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String nom;
    protected Produit() { }
    protected Produit(String nom) { this.nom=nom; }
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom=nom; }
    public abstract String details();
}
