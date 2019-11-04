# Cypher queries used in _symfinder_

This document references in the source code the Cypher queries used in _symfinder_ to detect symmetry implementations.

## Counting variation points

The total number of variation points (_vp_-s) is obtained by summing class level _vp_-s and method level _vp_-s.

### Getting the number of class level _vp_-s

Class level _vp_-s correspond to:
- interfaces
- abstract classes
- concrete extended classes

The number of interfaces is obtained by counting the number of nodes possessing the `INTERFACE` label.  
The number of abstract classes is obtained by counting the number of class nodes possessing the `ABSTRACT` label.  
The number of extended classes is obtained by counting the number of concrete class nodes (to avoid double counting with the abstract classes) possessing at least `EXTENDS` relationship.

```java
public int getNbClassLevelVPs() {
    int nbInterfaces = submitRequest("MATCH (n:INTERFACE) RETURN COUNT (n)").list().get(0).get(0).asInt();
    int nbAbstractClasses = submitRequest("MATCH (n:CLASS:ABSTRACT) RETURN COUNT (n)").list().get(0).get(0).asInt();
    int nbExtendedClasses = submitRequest("MATCH (n:CLASS)-[r:EXTENDS]->() WHERE NOT n:ABSTRACT RETURN COUNT (n)").list().get(0).get(0).asInt(); // we exclude abstracts as they are already counted
    return nbInterfaces + nbAbstractClasses + nbExtendedClasses;
}
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/454b0aba4c50bd8c0523132568d77fe229c5d671/src/main/java/neograph.NeoGraph.java#L427">source</a>


### Getting the number of method level _vp_-s

Method level _vp_-s correspond to:
- overriden methods
- overriden constructors

The number of overriden methods for a class node is determined by first counting the number of different method names in the method nodes linked to this node, and storing it in the `methods` property.

```java
public void setMethodsOverloads() {
    submitRequest("MATCH (c:CLASS)-->(a:METHOD) MATCH (c:CLASS)-->(b:METHOD)\n" +
            "WHERE a.name = b.name AND ID(a) <> ID(b)\n" +
            "WITH count(DISTINCT a.name) AS cnt, c\n" +
            "SET c.methods = cnt");
    submitRequest("MATCH (c:CLASS)\n" +
            "WHERE NOT EXISTS(c.methods)\n" +
            "SET c.methods = 0");
}
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/454b0aba4c50bd8c0523132568d77fe229c5d671/src/main/java/neograph.NeoGraph.java#L216">source</a>



Then, we get the sum of the values of this property for each node to get the total number of overriden methods.

```java
public int getTotalNbOverloadedMethods() {
    return submitRequest("MATCH (c:CLASS) RETURN (SUM(c.methods))")
            .list().get(0).get(0).asInt();
}
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/454b0aba4c50bd8c0523132568d77fe229c5d671/src/main/java/neograph.NeoGraph.java#L389">source</a>

The number of overriden constructors for a class node is determined by first counting the number of constructor nodes linked to this node, and storing it in the `constructors` property.

```java
public void setConstructorsOverloads() {
    submitRequest("MATCH (c:CLASS)-->(a:CONSTRUCTOR)\n" +
            "WITH count(a.name) AS cnt, c\n" +
            "SET c.constructors = cnt -1");
    submitRequest("MATCH (c:CLASS)\n" +
            "WHERE NOT EXISTS(c.constructors)\n" +
            "SET c.constructors = 0");
}
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/454b0aba4c50bd8c0523132568d77fe229c5d671/src/main/java/neograph.NeoGraph.java#L230">source</a>


Then, we get the sum of the values of this property for each node to get the total number of overriden constructors.

```java
public int getTotalNbOverloadedConstructors() {
    return submitRequest("MATCH (c:CLASS) RETURN (SUM(c.constructors))")
            .list().get(0).get(0).asInt();
}
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/454b0aba4c50bd8c0523132568d77fe229c5d671/src/main/java/neograph.NeoGraph.java#L379">source</a>


By adding these two values, we obtain the number of method level _vp_-s.


## Counting variants

The total number of variants is obtained by summing class level variants and method level variants.

### Getting the number of class level variants

The number of class level variants corresponds to the number of concrete classes without a subclass and extending a class or implementing an interface.

```java
public int getNbClassLevelVariants() {
    return submitRequest("MATCH (c:CLASS) WHERE (NOT c:ABSTRACT) AND ()-[:EXTENDS|:IMPLEMENTS]->(c) AND (NOT (c)-[:EXTENDS]->()) RETURN (COUNT(c))")
            .list().get(0).get(0).asInt();
}
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/454b0aba4c50bd8c0523132568d77fe229c5d671/src/main/java/neograph.NeoGraph.java#L333">source</a>


### Getting the number of method level variants

Method level variants correspond to:
- overrides of methods
- overrides of constructors

The number of overrides of methods is determined by counting for each class node the number of method nodes linked to it and having the same name.

```java
public int getNbMethodVariants() {
    return submitRequest("MATCH (c:CLASS)-->(a:METHOD) MATCH (c:CLASS)-->(b:METHOD)\n" +
            "WHERE a.name = b.name AND ID(a) <> ID(b)\n" +
            "return count(DISTINCT a)")
            .list().get(0).get(0).asInt();
}
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/454b0aba4c50bd8c0523132568d77fe229c5d671/src/main/java/neograph.NeoGraph.java#L354">source</a>

The number of overrides of constructors is determined by counting for each class node the number of constructor nodes linked to it and having the same name.

```java
public int getNbConstructorVariants() {
    return submitRequest("MATCH (c:CLASS)-->(a:CONSTRUCTOR) MATCH (c:CLASS)-->(b:CONSTRUCTOR)\n" +
            "WHERE a.name = b.name AND ID(a) <> ID(b)\n" +
            "return count(DISTINCT a)")
            .list().get(0).get(0).asInt();
}
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/454b0aba4c50bd8c0523132568d77fe229c5d671/src/main/java/neograph.NeoGraph.java#L367">source</a>

By adding these two values, we obtain the number of method level variants.
