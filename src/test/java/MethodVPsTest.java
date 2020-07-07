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

import neo4j_types.EntityType;
import neo4j_types.RelationType;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.types.Node;

import static org.junit.Assert.assertEquals;

public class MethodVPsTest extends Neo4jTest {

    @Test
    public void OneClassNoMethodOverload(){
        runTest(graph -> {
            Node nodeClass = graph.createNode("Shape", EntityType.CLASS);
            Node nodeMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node nodeMethod2 = graph.createNode("display", EntityType.METHOD);
            RelationType relationType = RelationType.METHOD;
            graph.linkTwoNodes(nodeClass, nodeMethod1, relationType);
            graph.linkTwoNodes(nodeClass, nodeMethod2, relationType);
            graph.setMethodVPs();
            assertEquals(0, graph.getNbMethodVPs());
        });
    }

    @Test
    public void TwoClassesNoMethodOverload(){
        runTest(graph -> {
            Node shapeNode = graph.createNode("Shape", EntityType.CLASS);
            Node shapeMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node shapeMethod2 = graph.createNode("display", EntityType.METHOD);
            Node polygonNode = graph.createNode("Polygon", EntityType.CLASS);
            Node polygonMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node polygonMethod2 = graph.createNode("display", EntityType.METHOD);
            graph.linkTwoNodes(shapeNode, shapeMethod1, RelationType.METHOD);
            graph.linkTwoNodes(shapeNode, shapeMethod2, RelationType.METHOD);
            graph.linkTwoNodes(polygonNode, polygonMethod1, RelationType.METHOD);
            graph.linkTwoNodes(polygonNode, polygonMethod2, RelationType.METHOD);
            graph.setMethodVPs();
            assertEquals(0, graph.getNbMethodVPs());
        });
    }

    @Test
    public void OneClassOneMethodOverload(){
        runTest(graph -> {
            Node nodeClass = graph.createNode("Shape", EntityType.CLASS);
            Node nodeMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node nodeMethod2 = graph.createNode("draw", EntityType.METHOD);
            RelationType relationType = RelationType.METHOD;
            graph.linkTwoNodes(nodeClass, nodeMethod1, relationType);
            graph.linkTwoNodes(nodeClass, nodeMethod2, relationType);
            graph.setMethodVPs();
            assertEquals(1, graph.getNbMethodVPs());
        });
    }

    @Test
    public void TwoClassesOneMethodOverload(){
        runTest(graph -> {
            Node shapeNode = graph.createNode("Shape", EntityType.CLASS);
            Node shapeMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node shapeMethod2 = graph.createNode("draw", EntityType.METHOD);
            Node polygonNode = graph.createNode("Polygon", EntityType.CLASS);
            Node polygonMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node polygonMethod2 = graph.createNode("display", EntityType.METHOD);
            graph.linkTwoNodes(shapeNode, shapeMethod1, RelationType.METHOD);
            graph.linkTwoNodes(shapeNode, shapeMethod2, RelationType.METHOD);
            graph.linkTwoNodes(polygonNode, polygonMethod1, RelationType.METHOD);
            graph.linkTwoNodes(polygonNode, polygonMethod2, RelationType.METHOD);
            graph.setMethodVPs();
            assertEquals(1, graph.getNbMethodVPs());
        });
    }

    @Test
    public void TwoClassesTwoMethodOverloads(){
        runTest(graph -> {
            Node shapeNode = graph.createNode("Shape", EntityType.CLASS);
            Node shapeMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node shapeMethod2 = graph.createNode("draw", EntityType.METHOD);
            Node polygonNode = graph.createNode("Polygon", EntityType.CLASS);
            Node polygonMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node polygonMethod2 = graph.createNode("draw", EntityType.METHOD);
            graph.linkTwoNodes(shapeNode, shapeMethod1, RelationType.METHOD);
            graph.linkTwoNodes(shapeNode, shapeMethod2, RelationType.METHOD);
            graph.linkTwoNodes(polygonNode, polygonMethod1, RelationType.METHOD);
            graph.linkTwoNodes(polygonNode, polygonMethod2, RelationType.METHOD);
            graph.setMethodVPs();
            assertEquals(2, graph.getNbMethodVPs());
        });
    }

}