# Cypher queries used in _symfinder_

This document references in the source code the Cypher queries used in _symfinder_ to detect symmetry implementations.

## Queries for symmetry in subtyping

Two main queries are used to identify symmetry in subtyping. The first one identifies the potential _vp_-s and the second one their variants.

### Identifying potential _vp_-s

The _vp_-s in subtyping correspond to interfaces, abstract classes, and concrete extended classes. Hence, these _vp_-s are identified by querying if a given node holds label `INTERFACE`, or `CLASS` and `ABSTRACT`, or corresponds to the unchanged part of a design pattern, or has class level variants, meaning that a concrete class is a _vp_ only if it has subclasses.

```java
// Identifying vp-s realized by subtyping
MATCH (c)
WHERE (NOT c:OUT_OF_SCOPE)
AND (c:INTERFACE OR (c:CLASS AND c:ABSTRACT)
OR (n:STRATEGY OR n:FACTORY OR n:DECORATOR OR n:TEMPLATE)
OR (EXISTS(c.classVariants) AND c.classVariants > 0))
SET c:VP
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/72bb0e3f76af69afe156d3f8da415e7b72a74a7a/src/main/java/neograph/NeoGraph.java#L282">source</a>


### Identifying potential variants

The variants of a _vp_ realized by subtyping correspond to subclasses. Hence, they are identified by querying if a given node is related to another node that holds label `CLASS` or `INTERFACE`, which is identified as a _vp_, and which relation holds label `EXTENDS` or `IMPLEMENTS`.

```java
// Identifying variants of a vp realized by subtyping
MATCH (sc:VP)-[:EXTENDS|IMPLEMENTS]->(c)
WHERE c:CLASS OR c:INTERFACE
SET c:VARIANT
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/72bb0e3f76af69afe156d3f8da415e7b72a74a7a/src/main/java/neograph/NeoGraph.java#L293">source</a>

## Queries for symmetry in overloading

Four queries are used to identify symmetry in overloading. They identify the total number of potential _vp_-s and variants realized by overloading in each class node.


### Identifying potential vp-s

The _vp_-s realized by overloading correspond to overloaded methods and constructors. First, for each class node is counted the number of overloaded methods using the name of method nodes that are linked to that class node. Then, the total number of overloaded methods for that class node is stored in its `methodVPs` attribute.

```java
// Identifying the number of vp-s realized by method overloading
MATCH (c:CLASS)-->(a:METHOD) MATCH (c:CLASS)-->(b:METHOD)
WHERE a.name = b.name AND ID(a) <> ID(b)
WITH COUNT(DISTINCT a.name) AS cnt, c
SET c.methodVPs = cnt

// When there is no vp, set the node attribute to zero
MATCH (c:CLASS)
WHERE NOT EXISTS(c.methodVPs)
SET c.methodVPs = 0
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/72bb0e3f76af69afe156d3f8da415e7b72a74a7a/src/main/java/neograph/NeoGraph.java#L206">source</a>


Secondly, the number of _vp_-s realized by constructor overloading is obtained in a similar way.

```java
// Identifying the number of vp-s realized by constructor overloading. If there is more than one constructor, this means that it is overloaded, hence the value is 1.
MATCH (c:CLASS)-->(a:CONSTRUCTOR)
WITH COUNT(a.name) AS cnt, c
SET c.constructorVPs = CASE WHEN cnt > 1 THEN 1 ELSE 0 END

// When there is no vp, set the node attribute to zero
MATCH (c:CLASS)
WHERE NOT EXISTS(c.constructorVPs)
SET c.constructorVPs = 0
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/72bb0e3f76af69afe156d3f8da415e7b72a74a7a/src/main/java/neograph/NeoGraph.java#L244">source</a>


Then, a node class has _vp_-s realized by overloading if it has at least one overloaded method or constructor.

```java
// Total number of identified vp-s realized by overloading
MATCH (c)
WHERE (NOT c:OUT_OF_SCOPE) AND (c.methodVPs > 0 OR c.constructorVPs > 0)
SET c:METHOD_LEVEL_VP
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/72bb0e3f76af69afe156d3f8da415e7b72a74a7a/src/main/java/neograph/NeoGraph.java#L288">source</a>


### Identifying potential variants

The variants realized by overloading correspond to the overloaded methods and constructors. Hence, for each overloaded method in a class node is counted how many times it is overloaded and then the resulted number of all overloaded methods for that class node is stored in its `methodVariants` attribute.

```java
// Identifying the number of variants realized by method overloading
MATCH (c:CLASS)-->(a:METHOD) MATCH (c:CLASS)-->(b:METHOD)
WHERE a.name = b.name AND ID(a) <> ID(b)
WITH count(DISTINCT a) AS cnt, c
SET c.methodVariants = cnt

// When there is no variant, set the node attribute to zero
MATCH (c:CLASS)
WHERE NOT EXISTS(c.methodVariants)
SET c.methodVariants = 0
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/72bb0e3f76af69afe156d3f8da415e7b72a74a7a/src/main/java/neograph/NeoGraph.java#L229">source</a>


Then, the number of variants realized by constructor overloading is obtained in a similar way.

```java
// Identifying the number of variants realized by constructor overloading. If there is no overload constructor, that is, there is 0 or 1, then the attribute is set to 0.
MATCH (c:CLASS)-->(a:CONSTRUCTOR)
WITH COUNT(a.name) AS cnt, c
MATCH (c:CLASS) SET c.constructorVariants = CASE
WHERE NOT EXISTS(c.methodVPs) WHEN cnt > 1 THEN cnt ELSE 0 END

// When there is no variant, set the node attribute to zero
MATCH (c:CLASS)
WHERE NOT EXISTS(c.constructorVariants)
SET c.constructorVariants = 0
```
<a href="https://github.com/DeathStar3/symfinder-internal/blob/72bb0e3f76af69afe156d3f8da415e7b72a74a7a/src/main/java/neograph/NeoGraph.java#L257">source</a>
