/*
 * This file is part of symfinder.
 *
 * symfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * symfinder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with symfinder. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2018-2019 Johann Mortara <johann.mortara@univ-cotedazur.fr>
 * Copyright 2018-2019 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
 * Copyright 2018-2019 Philippe Collet <philippe.collet@univ-cotedazur.fr>
 */

import neo4j_types.DesignPatternType;
import neo4j_types.EntityAttribute;
import neo4j_types.EntityType;
import neo4j_types.RelationType;
import neograph.NeoGraph;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class NeoGraphTest extends Neo4jTest {

    @Test
    public void createNodeOneLabel() {
        runTest(graph -> {
            EntityType nodeType = EntityType.CLASS;
            String nodeName = "n";
            graph.createNode(nodeName, nodeType);
            try (Transaction tx = graphDatabaseService.beginTx()) {
                ResourceIterable <Node> allNodes = tx.getAllNodes();
                assertEquals(1, allNodes.stream().count());
                Optional <Node> optionalNode = allNodes.stream().findFirst();
                assertTrue(optionalNode.isPresent());
                assertTrue(optionalNode.get().hasLabel(Label.label(nodeType.toString())));
                assertEquals(optionalNode.get().getProperty("name"), nodeName);
                tx.commit();
            }
        });
    }

    @Test
    public void createNodeTwoLabels() {
        runTest(graph -> {
            EntityType nodeType1 = EntityType.CLASS;
            EntityAttribute nodeType2 = EntityAttribute.ABSTRACT;
            String nodeName = "n";
            graph.createNode(nodeName, nodeType1, nodeType2);
            try (Transaction tx = graphDatabaseService.beginTx()) {
                ResourceIterable <Node> allNodes = tx.getAllNodes();
                assertEquals(1, allNodes.stream().count());
                Optional <Node> optionalNode = allNodes.stream().findFirst();
                assertTrue(optionalNode.isPresent());
                assertTrue(optionalNode.get().hasLabel(Label.label(nodeType1.toString())));
                assertTrue(optionalNode.get().hasLabel(Label.label(nodeType2.toString())));
                assertEquals(optionalNode.get().getProperty("name"), nodeName);
                tx.commit();
            }
        });
    }

    @Test
    public void getNode() {
        runTest(graph -> {
            EntityType nodeType = EntityType.CLASS;
            String nodeName = "n";
            graph.createNode(nodeName, nodeType);
            org.neo4j.driver.types.Node node = graph.getOrCreateNode(nodeName, nodeType);
            assertEquals(node.get("name").asString(), nodeName);
        });
    }

    @Test
    public void linkTwoNodes() {
        runTest(graph -> {
            org.neo4j.driver.types.Node node1 = graph.createNode("n1", EntityType.CLASS);
            org.neo4j.driver.types.Node node2 = graph.createNode("n2", EntityType.METHOD);
            RelationType relationType = RelationType.METHOD;
            graph.linkTwoNodes(node1, node2, relationType);
            try (Transaction tx = graphDatabaseService.beginTx()) {
                ResourceIterable <Node> allNodes = tx.getAllNodes();
                assertEquals(2, allNodes.stream().count());
                ResourceIterable <Relationship> allRelationships = tx.getAllRelationships();
                assertEquals(1, allRelationships.stream().count());
                Optional <Relationship> optionalRelationship = allRelationships.stream().findFirst();
                assertTrue(optionalRelationship.isPresent());
                assertTrue(optionalRelationship.get().isType(RelationshipType.withName(relationType.toString())));
                tx.commit();
            }
        });
    }

    @Test
    public void setMethodsOverloadsNoOverload() {
        runTest(graph -> {
            org.neo4j.driver.types.Node classNode = graph.createNode("Rectangle", EntityType.CLASS);
            org.neo4j.driver.types.Node drawNode1 = graph.createNode("draw", EntityType.METHOD);
            org.neo4j.driver.types.Node areaNode = graph.createNode("area", EntityType.METHOD);
            RelationType relationType = RelationType.METHOD;
            graph.linkTwoNodes(classNode, drawNode1, relationType);
            graph.linkTwoNodes(classNode, areaNode, relationType);
            graph.setMethodVPs();
            try (Transaction tx = graphDatabaseService.beginTx()) {
                List <Node> allNodes = tx.getAllNodes().stream()
                        .filter(node -> node.hasLabel(Label.label(EntityType.CLASS.toString())))
                        .collect(Collectors.toList());
                assertEquals(1, allNodes.size());
                Node node = allNodes.get(0);
                assertTrue(node.getAllProperties().containsKey("methodVPs"));
                assertEquals(0L, node.getProperty("methodVPs"));
                tx.commit();
            }
        });
    }

    @Test
    public void setMethodsOverloadsOneOverloadTwoVariants() {
        runTest(graph -> {
            org.neo4j.driver.types.Node classNode = graph.createNode("Rectangle", EntityType.CLASS);
            org.neo4j.driver.types.Node drawNode1 = graph.createNode("draw", EntityType.METHOD);
            org.neo4j.driver.types.Node drawNode2 = graph.createNode("draw", EntityType.METHOD);
            org.neo4j.driver.types.Node areaNode = graph.createNode("area", EntityType.METHOD);
            RelationType relationType = RelationType.METHOD;
            graph.linkTwoNodes(classNode, drawNode1, relationType);
            graph.linkTwoNodes(classNode, drawNode2, relationType);
            graph.linkTwoNodes(classNode, areaNode, relationType);
            graph.setMethodVPs();
            try (Transaction tx = graphDatabaseService.beginTx()) {
                List <Node> allNodes = tx.getAllNodes().stream()
                        .filter(node -> node.hasLabel(Label.label(EntityType.CLASS.toString())))
                        .collect(Collectors.toList());
                assertEquals(1, allNodes.size());
                Node node = allNodes.get(0);
                assertTrue(node.getAllProperties().containsKey("methodVPs"));
                assertEquals(1L, node.getProperty("methodVPs"));
                tx.commit();
            }
        });
    }

    @Test
    public void setMethodsOverloadsOneOverloadThreeVariants() {
        runTest(graph -> {
            org.neo4j.driver.types.Node classNode = graph.createNode("Rectangle", EntityType.CLASS);
            org.neo4j.driver.types.Node drawNode1 = graph.createNode("draw", EntityType.METHOD);
            org.neo4j.driver.types.Node drawNode2 = graph.createNode("draw", EntityType.METHOD);
            org.neo4j.driver.types.Node drawNode3 = graph.createNode("draw", EntityType.METHOD);
            org.neo4j.driver.types.Node areaNode = graph.createNode("area", EntityType.METHOD);
            RelationType relationType = RelationType.METHOD;
            graph.linkTwoNodes(classNode, drawNode1, relationType);
            graph.linkTwoNodes(classNode, drawNode2, relationType);
            graph.linkTwoNodes(classNode, drawNode3, relationType);
            graph.linkTwoNodes(classNode, areaNode, relationType);
            graph.setMethodVPs();
            try (Transaction tx = graphDatabaseService.beginTx()) {
                List <Node> allNodes = tx.getAllNodes().stream()
                        .filter(node -> node.hasLabel(Label.label(EntityType.CLASS.toString())))
                        .collect(Collectors.toList());
                assertEquals(1, allNodes.size());
                Node node = allNodes.get(0);
                assertTrue(node.getAllProperties().containsKey("methodVPs"));
                assertEquals(1L, node.getProperty("methodVPs"));
                tx.commit();
            }
        });
    }

    @Test
    public void setMethodsOverloadsTwoOverloads() {
        runTest(graph -> {
            org.neo4j.driver.types.Node classNode = graph.createNode("Rectangle", EntityType.CLASS);
            org.neo4j.driver.types.Node drawNode1 = graph.createNode("draw", EntityType.METHOD);
            org.neo4j.driver.types.Node drawNode2 = graph.createNode("draw", EntityType.METHOD);
            org.neo4j.driver.types.Node areaNode1 = graph.createNode("area", EntityType.METHOD);
            org.neo4j.driver.types.Node areaNode2 = graph.createNode("area", EntityType.METHOD);
            RelationType relationType = RelationType.METHOD;
            graph.linkTwoNodes(classNode, drawNode1, relationType);
            graph.linkTwoNodes(classNode, drawNode2, relationType);
            graph.linkTwoNodes(classNode, areaNode1, relationType);
            graph.linkTwoNodes(classNode, areaNode2, relationType);
            graph.setMethodVPs();
            try (Transaction tx = graphDatabaseService.beginTx()) {
                List <Node> allNodes = tx.getAllNodes().stream()
                        .filter(node -> node.hasLabel(Label.label(EntityType.CLASS.toString())))
                        .collect(Collectors.toList());
                assertEquals(1, allNodes.size());
                Node node = allNodes.get(0);
                assertTrue(node.getAllProperties().containsKey("methodVPs"));
                assertEquals(2L, node.getProperty("methodVPs"));
                tx.commit();
            }
        });
    }

    @Test
    public void setConstructorsOverloadsNoOverload() {
        runTest(graph -> {
            org.neo4j.driver.types.Node classNode = graph.createNode("Rectangle", EntityType.CLASS);
            org.neo4j.driver.types.Node constructorNode = graph.createNode("Rectangle", EntityType.CONSTRUCTOR);
            org.neo4j.driver.types.Node methodNode = graph.createNode("draw", EntityType.METHOD);
            RelationType relationType = RelationType.METHOD;
            graph.linkTwoNodes(classNode, constructorNode, relationType);
            graph.linkTwoNodes(classNode, methodNode, relationType);
            graph.setConstructorVPs();
            try (Transaction tx = graphDatabaseService.beginTx()) {
                List <Node> allNodes = tx.getAllNodes().stream()
                        .filter(node -> node.hasLabel(Label.label(EntityType.CLASS.toString())))
                        .collect(Collectors.toList());
                assertEquals(1, allNodes.size());
                Node node = allNodes.get(0);
                assertTrue(node.getAllProperties().containsKey("constructorVPs"));
                assertEquals(0L, node.getProperty("constructorVPs"));
                tx.commit();
            }
        });
    }

    @Test
    public void setConstructorsOverloadsOneOverload() {
        runTest(graph -> {
            org.neo4j.driver.types.Node classNode = graph.createNode("Rectangle", EntityType.CLASS);
            org.neo4j.driver.types.Node constructorNode1 = graph.createNode("Rectangle", EntityType.CONSTRUCTOR);
            org.neo4j.driver.types.Node constructorNode2 = graph.createNode("Rectangle", EntityType.CONSTRUCTOR);
            org.neo4j.driver.types.Node methodNode = graph.createNode("draw", EntityType.METHOD);
            RelationType relationType = RelationType.METHOD;
            graph.linkTwoNodes(classNode, constructorNode1, relationType);
            graph.linkTwoNodes(classNode, constructorNode2, relationType);
            graph.linkTwoNodes(classNode, methodNode, relationType);
            graph.setConstructorVPs();
            try (Transaction tx = graphDatabaseService.beginTx()) {
                List <Node> allNodes = tx.getAllNodes().stream()
                        .filter(node -> node.hasLabel(Label.label(EntityType.CLASS.toString())))
                        .collect(Collectors.toList());
                assertEquals(1, allNodes.size());
                Node node = allNodes.get(0);
                assertTrue(node.getAllProperties().containsKey("constructorVPs"));
                assertEquals(1L, node.getProperty("constructorVPs"));
                tx.commit();
            }
        });
    }

    @Test
    public void setConstructorsOverloadsTwoOverloads() {
        runTest(graph -> {
            org.neo4j.driver.types.Node classNode = graph.createNode("Rectangle", EntityType.CLASS);
            org.neo4j.driver.types.Node constructorNode1 = graph.createNode("Rectangle", EntityType.CONSTRUCTOR);
            org.neo4j.driver.types.Node constructorNode2 = graph.createNode("Rectangle", EntityType.CONSTRUCTOR);
            org.neo4j.driver.types.Node constructorNode3 = graph.createNode("Rectangle", EntityType.CONSTRUCTOR);
            org.neo4j.driver.types.Node methodNode = graph.createNode("draw", EntityType.METHOD);
            RelationType relationType = RelationType.METHOD;
            graph.linkTwoNodes(classNode, constructorNode1, relationType);
            graph.linkTwoNodes(classNode, constructorNode2, relationType);
            graph.linkTwoNodes(classNode, constructorNode3, relationType);
            graph.linkTwoNodes(classNode, methodNode, relationType);
            graph.setConstructorVPs();
            try (Transaction tx = graphDatabaseService.beginTx()) {
                List <Node> allNodes = tx.getAllNodes().stream()
                        .filter(node -> node.hasLabel(Label.label(EntityType.CLASS.toString())))
                        .collect(Collectors.toList());
                assertEquals(1, allNodes.size());
                Node node = allNodes.get(0);
                assertTrue(node.getAllProperties().containsKey("constructorVPs"));
                assertEquals(1L, node.getProperty("constructorVPs"));
                tx.commit();
            }
        });
    }

    @Test
    public void setNbVariantsPropertyTest() {
        runTest(graph -> {
            org.neo4j.driver.types.Node nodeClass1 = graph.createNode("class", EntityType.CLASS);
            org.neo4j.driver.types.Node nodeSubclass1 = graph.createNode("subclass1", EntityType.CLASS);
            org.neo4j.driver.types.Node nodeSubclass2 = graph.createNode("subclass2", EntityType.CLASS);
            org.neo4j.driver.types.Node nodeMethod = graph.createNode("method", EntityType.METHOD);
            graph.linkTwoNodes(nodeClass1, nodeSubclass1, RelationType.EXTENDS);
            graph.linkTwoNodes(nodeClass1, nodeSubclass2, RelationType.EXTENDS);
            graph.linkTwoNodes(nodeClass1, nodeMethod, RelationType.METHOD);
            graph.setNbVariantsProperty();
            try (Transaction tx = graphDatabaseService.beginTx()) {
                List <Node> allClassNodes = tx.getAllNodes().stream()
                        .filter(node -> node.hasLabel(Label.label(EntityType.CLASS.toString())))
                        .collect(Collectors.toList());
                assertEquals(3, allClassNodes.size());
                allClassNodes.stream().filter(node -> node.getProperty("name").equals("class"))
                        .findFirst()
                        .ifPresent(node -> assertEquals(2L, node.getProperty("classVariants")));
                allClassNodes.stream().filter(node -> node.getProperty("name").equals("subclass1"))
                        .findFirst()
                        .ifPresent(node -> assertEquals(0L, node.getProperty("classVariants")));
                tx.commit();
            }
        });
    }

    @Test
    public void deleteGraph() {
        runTest(graph -> {
            graph.deleteGraph();
            try (Transaction tx = graphDatabaseService.beginTx()) {
                ResourceIterable <Node> allNodes = tx.getAllNodes();
                assertEquals(0, allNodes.stream().count());
                tx.commit();
            }
        });
    }

    @Test
    public void addLabelToNode() {
        runTest(graph -> {
            org.neo4j.driver.types.Node node = graph.createNode("class", EntityType.CLASS);
            graph.addLabelToNode(node, DesignPatternType.STRATEGY.toString());
            try (Transaction tx = graphDatabaseService.beginTx()) {
                Node nodeFromGraph = tx.getNodeById(node.id());
                assertTrue(nodeFromGraph.hasLabel(Label.label(EntityType.CLASS.toString())));
                assertTrue(nodeFromGraph.hasLabel(Label.label(DesignPatternType.STRATEGY.toString())));
                tx.commit();
            }
        });
    }

    @Test
    public void getNbVariantsClass() {
        runTest(graph -> {
            org.neo4j.driver.types.Node shapeNode = graph.createNode("Shape", EntityType.CLASS);
            org.neo4j.driver.types.Node rectangleNode = graph.createNode("Rectangle", EntityType.CLASS);
            org.neo4j.driver.types.Node circleNode = graph.createNode("Circle", EntityType.CLASS);
            RelationType relationType = RelationType.EXTENDS;
            graph.linkTwoNodes(shapeNode, rectangleNode, relationType);
            graph.linkTwoNodes(shapeNode, circleNode, relationType);
            assertEquals(2, graph.getNbVariants(shapeNode));
            assertEquals(0, graph.getNbVariants(rectangleNode));
            assertEquals(0, graph.getNbVariants(circleNode));
        });
    }

    @Test
    public void getNbVariantsInterface() {
        runTest(graph -> {
            org.neo4j.driver.types.Node shapeNode = graph.createNode("Shape", EntityType.INTERFACE);
            org.neo4j.driver.types.Node rectangleNode = graph.createNode("Rectangle", EntityType.CLASS);
            org.neo4j.driver.types.Node circleNode = graph.createNode("Circle", EntityType.CLASS);
            RelationType relationType = RelationType.IMPLEMENTS;
            graph.linkTwoNodes(shapeNode, rectangleNode, relationType);
            graph.linkTwoNodes(shapeNode, circleNode, relationType);
            assertEquals(2, graph.getNbVariants(shapeNode));
            assertEquals(0, graph.getNbVariants(rectangleNode));
            assertEquals(0, graph.getNbVariants(circleNode));
        });
    }

    @Test
    public void relatedToRelationExists() {
        runTest(graph -> {
            org.neo4j.driver.types.Node shapeNode = graph.createNode("Shape", EntityType.CLASS, EntityAttribute.ABSTRACT);
            org.neo4j.driver.types.Node rectangleNode = graph.createNode("Rectangle", EntityType.CLASS);
            org.neo4j.driver.types.Node circleNode = graph.createNode("Circle", EntityType.CLASS);
            graph.linkTwoNodes(shapeNode, rectangleNode, RelationType.EXTENDS);
            assertTrue(graph.relatedTo(shapeNode, rectangleNode));
            assertFalse(graph.relatedTo(rectangleNode, shapeNode));
            assertFalse(graph.relatedTo(shapeNode, circleNode));
            assertFalse(graph.relatedTo(rectangleNode, circleNode));
        });
    }

    @Test
    public void relatedToTwoLevelRelation() {
        runTest(graph -> {
            org.neo4j.driver.types.Node shapeNode = graph.createNode("Shape", EntityType.CLASS, EntityAttribute.ABSTRACT);
            org.neo4j.driver.types.Node rectangleNode = graph.createNode("Rectangle", EntityType.CLASS);
            org.neo4j.driver.types.Node squareNode = graph.createNode("Square", EntityType.CLASS);
            graph.linkTwoNodes(shapeNode, rectangleNode, RelationType.EXTENDS);
            graph.linkTwoNodes(rectangleNode, squareNode, RelationType.EXTENDS);
            assertTrue(graph.relatedTo(shapeNode, rectangleNode));
            assertTrue(graph.relatedTo(rectangleNode, squareNode));
            assertFalse(graph.relatedTo(shapeNode, squareNode));
        });
    }

    @Test
    public void getClauseForNodesMatchingLabelsTest() {
        assertEquals("n:STRATEGY", NeoGraph.getClauseForNodesMatchingLabels("n", DesignPatternType.STRATEGY));
        assertEquals("cl:STRATEGY", NeoGraph.getClauseForNodesMatchingLabels("cl", DesignPatternType.STRATEGY));
        assertEquals("n:STRATEGY OR n:FACTORY", NeoGraph.getClauseForNodesMatchingLabels("n", DesignPatternType.STRATEGY, DesignPatternType.FACTORY));
        assertEquals("n:FACTORY OR n:STRATEGY", NeoGraph.getClauseForNodesMatchingLabels("n", DesignPatternType.FACTORY, DesignPatternType.STRATEGY));
    }

    @Test
    public void getSuperclassNodeWithExistingSuperclass() {
        runTest(graph -> {
            org.neo4j.driver.types.Node shapeNode = graph.createNode("Shape", EntityType.CLASS, EntityAttribute.ABSTRACT);
            org.neo4j.driver.types.Node rectangleNode = graph.createNode("Rectangle", EntityType.CLASS);
            graph.linkTwoNodes(shapeNode, rectangleNode, RelationType.EXTENDS);
            assertTrue(graph.getSuperclassNode("Rectangle").isPresent());
            assertEquals(shapeNode, graph.getSuperclassNode("Rectangle").get());
        });
    }

    @Test
    public void getSuperclassNodeWithNonExistingSuperclass() {
        runTest(graph -> {
            graph.createNode("Rectangle", EntityType.CLASS);
            assertFalse(graph.getSuperclassNode("Rectangle").isPresent());
        });
    }

    @Test
    public void getInheritedInterfaceNodeWithExistingSuperclass() {
        runTest(graph -> {
            org.neo4j.driver.types.Node shapeNode = graph.createNode("Shape", EntityType.INTERFACE);
            org.neo4j.driver.types.Node rectangleNode = graph.createNode("Rectangle", EntityType.CLASS);
            graph.linkTwoNodes(shapeNode, rectangleNode, RelationType.IMPLEMENTS);
            assertFalse(graph.getImplementedInterfacesNodes("Rectangle").isEmpty());
            assertTrue(graph.getImplementedInterfacesNodes("Rectangle").contains(shapeNode));
        });
    }

    @Test
    public void getInheritedInterfaceNodeWithNonExistingSuperclass() {
        runTest(graph -> {
            graph.createNode("Rectangle", EntityType.CLASS);
            assertTrue(graph.getImplementedInterfacesNodes("Rectangle").isEmpty());
        });
    }

    @Test
    public void getTwoInheritedInterfaces() {
        runTest(graph -> {
            org.neo4j.driver.types.Node shapeNode = graph.createNode("Shape", EntityType.INTERFACE);
            org.neo4j.driver.types.Node objectNode = graph.createNode("Object", EntityType.INTERFACE);
            org.neo4j.driver.types.Node rectangleNode = graph.createNode("Rectangle", EntityType.CLASS);
            graph.linkTwoNodes(shapeNode, rectangleNode, RelationType.IMPLEMENTS);
            graph.linkTwoNodes(objectNode, rectangleNode, RelationType.IMPLEMENTS);
            assertFalse(graph.getImplementedInterfacesNodes("Rectangle").isEmpty());
            assertTrue(graph.getImplementedInterfacesNodes("Rectangle").contains(objectNode));
            assertTrue(graph.getImplementedInterfacesNodes("Rectangle").contains(shapeNode));
        });
    }
}