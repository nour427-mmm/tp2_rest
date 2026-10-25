# tp2-rest

TP2 REST : Service web « Calculatrice » avec JAX-RS et Tomcat

# Compte-Rendu : TP2 - Service web REST avec JAX-RS

## Contexte Universitaire

- Établissement : Faculté des Sciences de Sfax (FSS)
- Matière : Architecture MicroServices
- Filière : Master Professionnel (MPSRCC1)
- Année Universitaire : 2026-2027
- Enseignantes : Corinne Amel ZAYANI, Amal ABID
- Étudiant(e) : **Nour Elhak Ben Mefteh**

---

## Objectifs du TP

Concevoir une API REST (URI et verbes HTTP), la réaliser avec JAX-RS (Jersey) dans un projet Maven `.war`, la déployer dans Tomcat, échanger du JSON, puis la tester avec le navigateur, `curl` et Postman en interprétant les codes de statut.

## Environnement

- JDK 17+
- Maven (intégré à IntelliJ)
- Apache Tomcat 
- IntelliJ IDEA
- Postman et curl



---

## Structure du projet

```
tp2
├── pom.xml
└── src/main/java/org/example/tp2
    ├── Calculatrice.java       (logique métier)
    ├── HelloApplication.java   (activation de JAX-RS avec @ApplicationPath("/api"))
    ├── CalculRest.java         (ressource /calcul)
    ├── Resultat.java           (objet renvoyé en JSON)
    ├── Operation.java          (objet de l'historique)
    └── OperationRest.java      (ressource /operations)
```

<img width="515" height="947" alt="Capture d&#39;écran 2026-10-25 162813" src="https://github.com/user-attachments/assets/7943b3dd-30ef-4367-972d-c77b294781cf" />


## Partie A : Conception du service

### Questions de conception

**1. Pourquoi les calculs directs utilisent-ils le verbe GET et non POST ?**

Un calcul direct ne fait que **lire** un résultat : il ne crée ni ne modifie aucune ressource sur le serveur. GET est le verbe prévu pour la lecture, il est *sûr* (sans effet de bord) et *idempotent*, et l'URL peut être mise en favori ou testée directement dans un navigateur. POST est réservé à la création d'une ressource.

**2. Dans l'URI `/calcul/somme?a=2&b=3`, comment s'appelle la partie située après le point d'interrogation ?**

C'est la **chaîne de requête** (*query string*) : elle contient les **paramètres de requête** (*query parameters*) `a=2` et `b=3`, séparés par `&`.

**3. Quelle différence y a-t-il entre `/operations` et `/operations/3` ?**

`/operations` désigne la **collection** de toutes les opérations, alors que `/operations/3` désigne **une seule ressource** de cette collection : l'opération d'identifiant 3.

**4. Quel code de statut HTTP le serveur doit-il renvoyer si le client demande `/operations/99` et que cette opération n'existe pas ?**

Le code **404 (Not Found)**.

---

## Partie B : Création du projet Maven

Création d'un projet **Jakarta EE** (template *REST service*) avec Maven, empaquetage `war`, et configuration du `pom.xml` avec Jersey pour JAX-RS.

Dépendances principales : `jersey-container-servlet`, `jersey-media-json-jackson` (conversion JSON), `jersey-cdi2-se` et `weld-se-core` (injection).

---

## Partie C : Premier service REST : la somme

Trois classes sont écrites : la logique métier `Calculatrice`, l'activation de JAX-RS `HelloApplication` et la ressource `CalculRest`. Le projet est ensuite construit en `.war` puis déployé dans Tomcat.

### Code source

`Calculatrice` contient la logique métier (somme, soustraction, produit, division) sans aucune dépendance REST. `HelloApplication`, annotée `@ApplicationPath("/api")`, active JAX-RS.

**`CalculRest.java`** (exemple : `somme`)

```java
@Path("/calcul")
public class CalculRest {

    @GET
    @Path("/somme")
    @Produces(MediaType.TEXT_PLAIN)
    public Response somme(@QueryParam("a") int a, @QueryParam("b") int b) {
        int res = calculService.somme(a, b);
        return Response.ok(res).build();
    }
}
```

### C.6 Comprendre l'URL du service

`http://localhost:8080/tp2_war_exploded/api/calcul/somme?a=2&b=3` = serveur Tomcat + contexte de l'application + `@ApplicationPath("/api")` + `@Path("/calcul")` + `@Path("/somme")` + paramètres lus par `@QueryParam`.

### C.7 Tests

**Navigateur** : `http://localhost:8080/tp2_war_exploded/api/calcul/somme?a=2&b=3` → affiche `5`.

<img width="951" height="416" alt="Capture d&#39;écran 2026-10-25 163802" src="https://github.com/user-attachments/assets/72a1dbda-f2f2-47f4-8ad5-ce23c269b4bc" />

**Postman** : requête `GET` sur la même URL.

<img width="1435" height="696" alt="Capture d&#39;écran 2026-10-25 164333" src="https://github.com/user-attachments/assets/0b91e344-916a-45cc-8164-8ccc63a485c6" />


### Question C.1 : Remplacer GET par POST dans Postman. Quel code de statut obtient-on ? Expliquez.

<img width="1437" height="801" alt="Capture d&#39;écran 2026-10-25 164434" src="https://github.com/user-attachments/assets/88c71be3-b150-4dce-8a04-d252ae2bea8d" />


On obtient **405 (Method Not Allowed)**. L'URI `/calcul/somme` existe bien, mais la méthode Java correspondante n'est annotée qu'avec `@GET` : aucune méthode n'est associée au verbe POST pour cette URI, donc le serveur refuse le verbe.

---

## Partie D : Compléter la calculatrice

### D.1 Soustraction et produit

Ajout de `soustraction` et `produit` sur le même modèle que `somme`. Résultats attendus : `soustraction?a=10&b=4` → 6 et `produit?a=6&b=7` → 42.

<img width="956" height="396" alt="Capture d&#39;écran 2026-10-25 165330" src="https://github.com/user-attachments/assets/229bc63f-5f7d-4231-8ea4-b86307c5a905" />


<img width="957" height="407" alt="Capture d&#39;écran 2026-10-25 165414" src="https://github.com/user-attachments/assets/1409bc08-2192-47a2-8788-63cac1f78a11" />


### D.2 Division et gestion d'erreur

Diviser par zéro est une erreur du **client** : le service répond **400 (Bad Request)** et non une erreur interne 500.

```java
@GET
@Path("/division")
@Produces(MediaType.TEXT_PLAIN)
public Response division(@QueryParam("a") int a, @QueryParam("b") int b) {
    if (b == 0) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Erreur : Division par zero impossible")
                .build();
    }
    return Response.ok(calculService.division(a, b)).build();
}
```

Résultats : `division?a=7&b=2` → 200 et `3.5` ; `division?a=7&b=0` → 400 et message d'erreur.

<img width="960" height="402" alt="Capture d&#39;écran 2026-10-25 165841" src="https://github.com/user-attachments/assets/eb091c40-6a1f-477d-9bd1-7d2e0978603e" />

<img width="957" height="408" alt="Capture d&#39;écran 2026-10-25 165923" src="https://github.com/user-attachments/assets/324a92d7-fc02-40f5-b21e-4520652b573e" />


### D.3 Observations

<img width="957" height="287" alt="Capture d&#39;écran 2026-10-25 170212" src="https://github.com/user-attachments/assets/760bc102-d532-4d52-98b3-5647701b1765" />


<img width="958" height="393" alt="Capture d&#39;écran 2026-10-25 170307" src="https://github.com/user-attachments/assets/daad76a0-33fa-4234-92c3-9cb19028ec77" />


**Question D.1 : Quelle valeur JAX-RS donne-t-il à un paramètre entier absent ?**

JAX-RS donne la **valeur par défaut du type Java**, soit `0` pour un `int`. Ainsi `somme?a=2` (b absent) calcule `2 + 0` et renvoie `2` avec un statut 200.

**Question D.2 : Quel statut obtient-on lorsque le paramètre n'est pas un entier ? La méthode `somme` a-t-elle été exécutée ?**

On obtient **404 (Not Found)** : JAX-RS ne parvient pas à convertir `"abc"` en `int` et considère que la ressource demandée n'existe pas. La méthode `somme` **n'est pas exécutée** : l'erreur se produit avant l'appel, pendant la conversion du paramètre.

### D.4 Paramètres dans le chemin : `@PathParam`

```java
@GET
@Path("/produit/{a}/{b}")
@Produces(MediaType.TEXT_PLAIN)
public Response produitChemin(@PathParam("a") int a, @PathParam("b") int b) {
    return Response.ok(calculService.produit(a, b)).build();
}
```

Test : `GET /calcul/produit/6/7` → `42`.

> 📸 **CAPTURE 19** : Test `GET /calcul/produit/6/7` (résultat 42)

**Question D.3 : Quelle différence y a-t-il entre `@QueryParam` et `@PathParam` ? Laquelle utilise-t-on pour lire l'identifiant dans `/operations/{id}` ?**

- `@QueryParam` lit un paramètre placé **après le `?`** (`?a=2&b=3`). Il sert surtout pour des options, des filtres ou des valeurs facultatives.
- `@PathParam` lit une valeur placée **dans le chemin** de l'URI (`/produit/6/7`). Elle fait partie de l'identité de la ressource et est obligatoire.

Pour l'identifiant dans `/operations/{id}`, on utilise **`@PathParam("id")`**.

---

## Partie E : Échanger des données en JSON

### E.1 La classe `Resultat`

POJO (`operation`, `a`, `b`, `resultat`) avec un constructeur public sans argument et des getters/setters, nécessaires à la conversion JSON.

### E.2 Une méthode qui produit du JSON

```java
@GET
@Path("/somme/detail")
@Produces(MediaType.APPLICATION_JSON)
public Response sommeDetail(@QueryParam("a") int a, @QueryParam("b") int b) {
    Resultat res = new Resultat("somme", a, b, calculService.somme(a, b));
    return Response.ok(res).build();
}
```

Test : `curl -i ".../calcul/somme/detail?a=2&b=3"`. Résultat : statut `200`, `Content-Type: application/json`, corps `{"operation":"somme","a":2,"b":3,"resultat":5.0}` (l'ordre des champs peut varier selon la bibliothèque JSON).

> 📸 **CAPTURE 20** : `curl -i` sur `somme/detail?a=2&b=3` (200, application/json, corps JSON)

**Question E.1 : Qui a effectué la conversion en JSON ? D'où viennent les noms `a`, `b`, `operation` et `resultat` ?**

La conversion est faite automatiquement par **Jersey**, grâce au module JSON (`jersey-media-json-jackson`) : c'est un *MessageBodyWriter* qui sérialise l'objet Java en JSON à cause de `@Produces(APPLICATION_JSON)`. Les noms des champs viennent des **accesseurs de la classe `Resultat`** : `getA()` donne `a`, `getOperation()` donne `operation`, etc. C'est pourquoi le constructeur vide et les getters/setters sont obligatoires.

### E.3 L'en-tête `Accept`

Tests avec `curl -i -H "Accept: application/json"` puis `-H "Accept: application/xml"` sur `somme/detail`.

> 📸 **CAPTURE 21** : `curl -i` avec `Accept: application/json` (statut 200)

> 📸 **CAPTURE 22** : `curl -i` avec `Accept: application/xml` (statut 406)

**Question E.2 : Pourquoi la seconde requête est-elle refusée ?**

Le client demande du XML (`Accept: application/xml`), mais la méthode ne sait produire que du JSON (`@Produces(MediaType.APPLICATION_JSON)`). Aucun format ne satisfait le client, donc le serveur répond **406 (Not Acceptable)**.

### E.4 Exercice : `divisionDetail`

```java
@GET
@Path("/division/detail")
@Produces(MediaType.APPLICATION_JSON)
public Response divisionDetail(@QueryParam("a") int a, @QueryParam("b") int b) {
    if (b == 0) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN)
                .entity("Erreur : Division par zero impossible")
                .build();
    }
    Resultat res = new Resultat("division", a, b, calculService.division(a, b));
    return Response.ok(res).build();
}
```

> 📸 **CAPTURE 23** : `curl -i` sur `division/detail?a=7&b=2` (200 et JSON)

> 📸 **CAPTURE 24** : `curl -i` sur `division/detail?a=7&b=0` (400)

---

## Partie F : La ressource « operations » (POST, GET, PUT, DELETE)

Les opérations sont conservées **en mémoire** (sans base de données) : elles sont perdues à chaque redémarrage ou redéploiement de Tomcat et la numérotation repart de 1.

### F.1 et F.2 Les classes `Operation` et `OperationRest`

`Operation` est un POJO (`id`, `operateur`, `a`, `b`, `resultat`). `OperationRest` stocke les opérations dans une `ConcurrentHashMap` **static** (une instance de la classe est créée par requête) et expose les 5 méthodes `POST`, `GET` (liste), `GET /{id}`, `PUT /{id}` et `DELETE /{id}`. Méthode principale, la création :

```java
@POST
public Response creer(Operation op, @Context UriInfo uriInfo) {
    if (!calculer(op)) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"erreur\":\"Operateur invalide ou division par zero\"}").build();
    }
    int id = COMPTEUR.incrementAndGet();
    op.setId(id);
    OPERATIONS.put(id, op);
    URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
    return Response.created(location).entity(op).build();   // 201 + en-tête Location
}
```

### F.3 Tests : création et lecture

| N° | Requête | Corps envoyé | Statut obtenu | Réponse |
|---|---|---|---|---|
| 1 | `POST /operations` | `{"operateur":"somme","a":8,"b":2}` | 201 | Opération `id` 1, `resultat` 10.0, en-tête `Location` finissant par `/operations/1` |
| 2 | `POST /operations` | `{"operateur":"division","a":7,"b":2}` | 201 | Opération `id` 2, `resultat` 3.5 |
| 3 | `GET /operations` | aucun | 200 | Tableau JSON des deux opérations |
| 4 | `GET /operations/2` | aucun | 200 | L'opération d'identifiant 2 |
| 5 | `GET /operations/99` | aucun | 404 | Corps vide |
| 6 | `POST /operations` | `{"operateur":"division","a":7,"b":0}` | 400 | `{"erreur":"Operateur invalide ou division par zero"}` |
| 7 | `POST /operations` | `{"operateur":"racine","a":9,"b":0}` | 400 | `{"erreur":"Operateur invalide ou division par zero"}` |

> 📸 **CAPTURE 25** : Test 1 : POST somme 8+2 (statut 201, corps JSON et en-tête Location)

> 📸 **CAPTURE 26** : Test 2 : POST division 7/2 (statut 201, résultat 3.5)

> 📸 **CAPTURE 27** : Test 3 : GET /operations (statut 200, tableau des deux opérations)

> 📸 **CAPTURE 28** : Test 4 : GET /operations/2 (statut 200)

> 📸 **CAPTURE 29** : Test 5 : GET /operations/99 (statut 404, corps vide)

> 📸 **CAPTURE 30** : Test 6 : POST division 7/0 (statut 400 et message d'erreur)

> 📸 **CAPTURE 31** : Test 7 : POST opérateur « racine » (statut 400 et message d'erreur)

### F.4 Modifier une opération : PUT

Statuts : 404 (id inexistant), 400 (calcul impossible), 200 (modification réussie).

Test principal : `PUT /operations/1` avec `{"operateur":"produit","a":8,"b":2}` → 200, `resultat` 16.0.

> 📸 **CAPTURE 32** : PUT sur un id inexistant, par exemple `/operations/99` (statut 404)

> 📸 **CAPTURE 33** : PUT sur `/operations/1` avec un calcul impossible (statut 400)

> 📸 **CAPTURE 34** : PUT `/operations/1` avec `{"operateur":"produit","a":8,"b":2}` (statut 200, résultat 16.0)

> 📸 **CAPTURE 35** : GET `/operations/1` après la modification (résultat 16.0 confirmé)

### F.5 Supprimer une opération : DELETE

Tests dans l'ordre : `DELETE /operations/1` → 204, puis `GET /operations/1` → 404, puis `DELETE /operations/1` → 404.

> 📸 **CAPTURE 36** : Premier DELETE `/operations/1` (statut 204)

> 📸 **CAPTURE 37** : GET `/operations/1` après suppression (statut 404)

> 📸 **CAPTURE 38** : Second DELETE `/operations/1` (statut 404)

---

## Questions de synthèse

### 1. Tableau de l'étape A.1 avec les codes de statut HTTP

| Verbe | URI | Rôle | Codes de statut possibles |
|---|---|---|---|
| GET | `/calcul/somme?a=2&b=3` | a + b | 200 (404 si un paramètre n'est pas un entier) |
| GET | `/calcul/soustraction?a=2&b=3` | a – b | 200 (404 si paramètre non entier) |
| GET | `/calcul/produit?a=2&b=3` | a × b | 200 (404 si paramètre non entier) |
| GET | `/calcul/division?a=6&b=3` | a ÷ b | 200, 400 (b = 0), 404 (paramètre non entier) |
| POST | `/operations` | Crée une opération | 201, 400 (opérateur inconnu ou division par 0) |
| GET | `/operations` | Liste des opérations | 200 |
| GET | `/operations/{id}` | Lit l'opération id | 200, 404 |
| PUT | `/operations/{id}` | Modifie l'opération id | 200, 400, 404 |
| DELETE | `/operations/{id}` | Supprime l'opération id | 204, 404 |

Dans tous les cas, un verbe non prévu pour une URI donne **405** et un format non supporté (`Accept`) donne **406**.

### 2. Annotations JAX-RS utilisées et leur rôle

| Annotation | Rôle |
|---|---|
| `@ApplicationPath` | Active JAX-RS et définit le préfixe commun de toutes les URI (`/api`) |
| `@Path` | Associe une classe ou une méthode à une portion d'URI (accepte des paramètres `{id}`) |
| `@GET` | La méthode répond aux requêtes HTTP GET (lecture) |
| `@POST` | La méthode répond aux requêtes POST (création) |
| `@PUT` | La méthode répond aux requêtes PUT (modification/remplacement) |
| `@DELETE` | La méthode répond aux requêtes DELETE (suppression) |
| `@Produces` | Indique le format de la réponse (`text/plain`, `application/json`) |
| `@Consumes` | Indique le format attendu dans le corps de la requête (JSON) |
| `@QueryParam` | Lit un paramètre de requête placé après `?` |
| `@PathParam` | Lit une valeur située dans le chemin de l'URI |
| `@Context` | Injecte des informations du contexte HTTP (ex. `UriInfo`) |

### 3. Différence entre JAX-RS et Jersey

**JAX-RS** (Jakarta RESTful Web Services) est une **spécification** : elle définit l'API et les annotations (`@Path`, `@GET`...), mais ne contient pas de moteur. **Jersey** est une **implémentation** de cette spécification (c'est l'implémentation de référence) : c'est lui qui, à l'exécution, lit les annotations, reçoit les requêtes, appelle nos méthodes et convertit les objets en JSON. D'autres implémentations existent (RESTEasy, Apache CXF).

### 4. Rôle de Tomcat et pourquoi un `.war` et non un `.jar` exécutable

**Tomcat** est un **conteneur de servlets** (serveur web d'applications Java) : il écoute sur le port 8080, reçoit les requêtes HTTP, les transmet à l'application déployée (ici via la servlet de Jersey) et renvoie la réponse. Notre projet ne contient **pas de serveur intégré** : il fournit seulement le code de l'application. Le **`.war`** est le format standard d'une application web destinée à être **déployée dans un conteneur existant**, alors qu'un `.jar` exécutable doit embarquer son propre serveur et se lancer seul (cas de Spring Boot).

### 5. Stateless et historique des opérations

Le principe **stateless** interdit au serveur de mémoriser l'**état du client** (session, contexte de conversation) entre deux requêtes : chaque requête doit contenir toute l'information nécessaire pour être traitée. La liste des opérations n'est pas un état de client mais l'**état des ressources** (les données de l'application), commun à tous les clients et accessible par une URI. Une requête comme `GET /operations/2` donne le même résultat quel que soit le client et ne dépend d'aucun échange précédent avec lui. Stocker les données de l'application est donc normal (ici en mémoire, normalement dans une base de données).

### 6. Répéter deux fois POST `/operations` puis deux fois PUT `/operations/1`

- **POST envoyé deux fois** : crée **deux ressources distinctes** (id 1 puis id 2, deux réponses 201). Le résultat change à chaque envoi : POST **n'est pas idempotent**.
- **PUT envoyé deux fois** : remplace la **même** ressource par le même contenu. Après la première ou la deuxième exécution, l'état du serveur est identique (même id, même résultat) : PUT **est idempotent**.

---

## Conclusion

Ce TP a permis de concevoir et de réaliser une API REST complète avec JAX-RS (Jersey) déployée sur Tomcat : calculs directs en GET (texte brut ou JSON), gestion des erreurs avec les codes de statut appropriés (400, 404, 405, 406), et une ressource `operations` qui implémente les quatre verbes HTTP (POST, GET, PUT, DELETE). Les tests avec le navigateur, `curl` et Postman ont permis de vérifier les codes de statut et le format des réponses.
