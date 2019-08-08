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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with symfinder.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2018-2019 Johann Mortara <johann.mortara@univ-cotedazur.fr>
 * Copyright 2018-2019 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
 * Copyright 2018-2019 Philippe Collet <philippe.collet@univ-cotedazur.fr>
 */

import neo4j_types.EntityAttribute;
import neo4j_types.EntityType;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.*;

public class GetOrCreateNodeTest extends Neo4JTest {

    @Test
    public void getOrCreateNodeCreationNoProperty() {
        runTest(graph -> {
            String nodeName = "n";
            EntityType nodeType = EntityType.CLASS;
            org.neo4j.driver.v1.types.Node node = graph.getOrCreateNode(nodeName, nodeType);
            try (Transaction tx = graphDatabaseService.beginTx()) {
                ResourceIterable <Node> allNodes = graphDatabaseService.getAllNodes();
                assertEquals(1, allNodes.stream().count());
                assertEquals(node.get("name").asString(), nodeName);
                assertTrue(node.hasLabel(nodeType.toString()));
                tx.success();
            }
        });
    }

    @Test
    public void getOrCreateNodeCreatingPropertyExistingNode() {
        runTest(graph -> {
            String nodeName = "n";
            EntityType nodeType = EntityType.CLASS;
            EntityAttribute attribute1 = EntityAttribute.VP;
            EntityAttribute attribute2 = EntityAttribute.ABSTRACT;
            org.neo4j.driver.v1.types.Node node1 = graph.getOrCreateNode(nodeName, nodeType, new EntityAttribute[]{attribute1}, new EntityAttribute[]{});
            org.neo4j.driver.v1.types.Node node2 = graph.getOrCreateNode(nodeName, nodeType, new EntityAttribute[]{attribute2}, new EntityAttribute[]{});
            try (Transaction tx = graphDatabaseService.beginTx()) {
                ResourceIterable <Node> allNodes = graphDatabaseService.getAllNodes();
                assertEquals(1, allNodes.stream().count());
                assertEquals(node1.get("name").asString(), nodeName);
                assertTrue(node1.hasLabel(nodeType.toString()));
                assertTrue(node1.hasLabel(attribute1.toString()));
                assertEquals(node1, node2);
                assertTrue(node2.hasLabel(attribute1.toString()));
                assertFalse(node2.hasLabel(attribute2.toString()));
                tx.success();
            }
        });
    }

    @Test
    public void getOrCreateNodeCreatingPropertyNonExistingNode() {
        runTest(graph -> {
            String nodeName = "n";
            EntityType nodeType = EntityType.CLASS;
            EntityAttribute attribute = EntityAttribute.VP;
            org.neo4j.driver.v1.types.Node node = graph.getOrCreateNode(nodeName, nodeType, new EntityAttribute[]{attribute}, new EntityAttribute[]{});
            try (Transaction tx = graphDatabaseService.beginTx()) {
                ResourceIterable <Node> allNodes = graphDatabaseService.getAllNodes();
                assertEquals(1, allNodes.stream().count());
                assertEquals(node.get("name").asString(), nodeName);
                assertTrue(node.hasLabel(nodeType.toString()));
                assertTrue(node.hasLabel(attribute.toString()));
                tx.success();
            }
        });
    }

    @Test
    public void getOrCreateNodeMatchingPropertyExistingNode() {
        runTest(graph -> {
            String nodeName = "n";
            EntityType nodeType = EntityType.CLASS;
            EntityAttribute attribute1 = EntityAttribute.VP;
            EntityAttribute attribute2 = EntityAttribute.ABSTRACT;
            org.neo4j.driver.v1.types.Node node1 = graph.getOrCreateNode(nodeName, nodeType, new EntityAttribute[]{attribute1}, new EntityAttribute[]{});
            org.neo4j.driver.v1.types.Node node2 = graph.getOrCreateNode(nodeName, nodeType, new EntityAttribute[]{}, new EntityAttribute[]{attribute2});
            try (Transaction tx = graphDatabaseService.beginTx()) {
                ResourceIterable <Node> allNodes = graphDatabaseService.getAllNodes();
                assertEquals(1, allNodes.stream().count());
                assertEquals(node1.get("name").asString(), nodeName);
                assertTrue(node1.hasLabel(nodeType.toString()));
                assertTrue(node1.hasLabel(attribute1.toString()));
                assertEquals(node1, node2);
                assertTrue(node2.hasLabel(attribute1.toString()));
                assertTrue(node2.hasLabel(attribute2.toString()));
                tx.success();
            }
        });
    }
    @Test
    public void getOrCreateNodeMatchingPropertyNonExistingNode() {
        runTest(graph -> {
            String nodeName = "n";
            EntityType nodeType = EntityType.CLASS;
            EntityAttribute attribute = EntityAttribute.VP;
            org.neo4j.driver.v1.types.Node node = graph.getOrCreateNode(nodeName, nodeType, new EntityAttribute[]{}, new EntityAttribute[]{attribute});
            try (Transaction tx = graphDatabaseService.beginTx()) {
                ResourceIterable <Node> allNodes = graphDatabaseService.getAllNodes();
                assertEquals(1, allNodes.stream().count());
                assertEquals(node.get("name").asString(), nodeName);
                assertTrue(node.hasLabel(nodeType.toString()));
                assertFalse(node.hasLabel(attribute.toString()));
                tx.success();
            }
        });
    }

    @Test
    public void getOrCreateNodeGetting() {
        runTest(graph -> {
            String nodeName = "n";
            EntityType nodeType1 = EntityType.CLASS;
            EntityType nodeType2 = EntityType.INTERFACE;
            graph.getOrCreateNode(nodeName, nodeType1);
            org.neo4j.driver.v1.types.Node node = graph.getOrCreateNode(nodeName, nodeType2);
            try (Transaction tx = graphDatabaseService.beginTx()) {
                ResourceIterable <Node> allNodes = graphDatabaseService.getAllNodes();
                assertEquals(2, allNodes.stream().count());
                assertEquals(node.get("name").asString(), nodeName);
                assertTrue(node.hasLabel(nodeType2.toString()));
                tx.success();
            }
        });
    }

    @Test
    public void getOrCreateNodeMatchAndCreateExistingNode() {
        runTest(graph -> {
            String nodeName = "n";
            EntityType nodeType1 = EntityType.CLASS;
            EntityAttribute nodeType2 = EntityAttribute.ABSTRACT;
            graph.getOrCreateNode(nodeName, nodeType1);
            org.neo4j.driver.v1.types.Node node = graph.getOrCreateNode(nodeName, nodeType1, new EntityAttribute[]{nodeType2});
            try (Transaction tx = graphDatabaseService.beginTx()) {
                ResourceIterable <Node> allNodes = graphDatabaseService.getAllNodes();
                assertEquals(1, allNodes.stream().count());
                assertEquals(node.get("name").asString(), nodeName);
                assertTrue(node.hasLabel(nodeType1.toString()));
                assertTrue(node.hasLabel(nodeType2.toString()));
                tx.success();
            }
        });
    }

    @Test
    public void getOrCreateNodeMatchAndCreateNonExistingNode() {
        runTest(graph -> {
            String nodeName = "n";
            EntityType nodeType1 = EntityType.CLASS;
            EntityAttribute nodeType2 = EntityAttribute.ABSTRACT;
            org.neo4j.driver.v1.types.Node node = graph.getOrCreateNode(nodeName, nodeType1, new EntityAttribute[]{nodeType2});
            try (Transaction tx = graphDatabaseService.beginTx()) {
                ResourceIterable <Node> allNodes = graphDatabaseService.getAllNodes();
                assertEquals(1, allNodes.stream().count());
                assertEquals(node.get("name").asString(), nodeName);
                assertTrue(node.hasLabel(nodeType1.toString()));
                assertTrue(node.hasLabel(nodeType2.toString()));
                tx.success();
            }
        });
    }

}
