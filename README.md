# TP 4 — Héritage avec Hibernate : stratégies de mapping

Solution du laboratoire **147** du cours [Hibernate & JPA](https://mliaedu.toubkalit.com/student/courses/28). Le projet compare les trois stratégies demandées avec les entités et les packages de l'énoncé, puis vérifie création, lecture, modification et suppression dans une base H2.

## Exécuter

Prérequis : **JDK 17** et **Maven 3.9+**, avec un accès à Maven Central lors du premier lancement.

```sh
mvn clean verify
mvn exec:java
```

La classe principale est `com.example.App`. Les dépendances prescrites sont conservées : `javax.persistence-api:2.2`, `hibernate-core:5.4.30.Final` et `h2:1.4.200`. Les tests utilisent JUnit 5. Aucune application serveur ni installation de base de données n'est nécessaire.

## Couverture de l'énoncé

| Exigence | Réalisation |
|---|---|
| Projet Maven, API JPA standard et H2 | `pom.xml`, code `javax.persistence`, base en mémoire |
| Unité `hibernate-inheritance` et déclaration des 9 classes | `src/main/resources/META-INF/persistence.xml` |
| `SINGLE_TABLE` | `com.example.model.singletable.Vehicule`, `Voiture`, `Moto` ; colonne `vehicule_type`, valeurs `VOITURE`/`MOTO` |
| `JOINED` | `com.example.model.joined.Employe`, `Developpeur`, `Manager` ; tables liées par identifiant |
| `TABLE_PER_CLASS` | `com.example.model.tableperclass.Produit`, `Livre`, `Electronique` ; identifiants `AUTO` et tables concrètes distinctes |
| Données demandées | Toyota/Harley, Alice/Bob, Clean Code/Smartphone et attributs propres aux sous-classes |
| Lectures polymorphiques et analyse SQL | Requêtes Criteria sur les classes parentes ; SQL affiché avec jointures et `UNION ALL` |
| CRUD complet | `GenericCrudService` et `App` : persistance, relecture dans un nouveau contexte, mise à jour et suppression |
| Bonnes pratiques et prolongements proposés | Index discriminant, méthode abstraite `details()`, service CRUD générique et lecture Criteria polymorphique |

`Jpa.transaction` ferme chaque `EntityManager` et annule la transaction après une exception. `App` ferme également la factory. Le schéma `create-drop` rend les démonstrations reproductibles et disparaît à la fermeture.

## Résultats réellement obtenus

La démonstration recharge les six objets depuis la base et imprime leurs types concrets. Elle observe **2 lignes** dans `Vehicule`, **2 + 1 + 1** lignes dans `Employe`/`Developpeur`/`Manager`, et **1 + 1** ligne dans `Livre`/`Electronique`. Les mises à jour rechargées donnent `Toyota Hybrid`, `equipes=4`, `garantie=36 mois`. Après trois suppressions, chaque hiérarchie contient **1 objet**.

**5 tests réussis, 0 échec, 0 erreur, 0 ignoré.** Ils contrôlent les discriminants, les colonnes nulles spécifiques, les lignes parentes/enfants et leur suppression, les identifiants distincts de `TABLE_PER_CLASS`, l'absence de table abstraite `Produit`, les sous-types retournés par Criteria et le rollback d'une suppression invalide.

Les [logs de démonstration](evidence/demo-output.txt), les [logs de tests](evidence/test-output.txt) et les [rapports JUnit](evidence/junit/) permettent de vérifier ces résultats. Les images suivantes sont des captures dans un navigateur de rapports construits à partir des sorties enregistrées ; elles ne sont pas des captures directes du terminal.

![Résultats de la démonstration](screenshots/results.png)

![Résultats des tests](screenshots/tests.png)

Le [rapport des résultats](evidence/report.html) peut être ouvert directement dans un navigateur.

## Comparaison

| Stratégie | Stockage et SQL observés | Compromis |
|---|---|---|
| `SINGLE_TABLE` | Une table, discriminant, colonnes propres aux sous-classes parfois nulles | Lecture simple ; table plus large |
| `JOINED` | Une table par classe, jointures pour la lecture polymorphique | Schéma normalisé ; davantage de jointures |
| `TABLE_PER_CLASS` | Une table par classe concrète, colonnes héritées répétées, union pour la lecture polymorphique | Tables indépendantes ; requête polymorphique plus complexe |

Le générateur `AUTO` partagé évite les identifiants en collision dans `TABLE_PER_CLASS`. L'essai avec PostgreSQL/MySQL évoqué en conclusion est une piste supplémentaire ; cette livraison utilise la base H2 demandée.

Référence technique consultée : [guide officiel Hibernate 5.4 — héritage](https://docs.hibernate.org/orm/5.4/userguide/html_single/#entity-inheritance).

## Régénérer le rapport et les captures

Les images sont des captures navigateur de résultats réellement exécutés, conservés dans `evidence/`. Les tests proviennent des XML Surefire, également archivés dans `evidence/junit/`. Les empreintes SHA-256 des sources figurent dans `evidence/checksums.json`.

Avec Python 3 et Node.js disponibles, depuis la racine du dépôt :

```sh
mvn clean verify > evidence/test-output.txt 2>&1
mvn exec:java > evidence/demo-output.txt 2>&1
python scripts/generate_report.py
npm install --no-save playwright
npx playwright install chromium
node scripts/capture-results.cjs
```

Le rapport se consulte aussi directement dans `evidence/report.html`. Les captures générées sont `screenshots/results.png` et `screenshots/tests.png`.
