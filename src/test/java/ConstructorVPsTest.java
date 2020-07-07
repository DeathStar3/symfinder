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

public class ConstructorVPsTest extends Neo4jTest {

    @Test
    public void OneClassNoConstructorOverload() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS);
            Node shapeConstructor = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            graph.linkTwoNodes(shapeClass, shapeConstructor, RelationType.METHOD);
            graph.setConstructorVPs();
            assertEquals(0, graph.getNbConstructorVPs());
        });
    }

    @Test
    public void OneClassOneConstructorOverload() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS);
            Node shapeConstructor1 = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            Node shapeConstructor2 = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            graph.linkTwoNodes(shapeClass, shapeConstructor1, RelationType.METHOD);
            graph.linkTwoNodes(shapeClass, shapeConstructor2, RelationType.METHOD);
            graph.setConstructorVPs();
            assertEquals(1, graph.getNbConstructorVPs());
        });
    }

    @Test
    public void OneClassTwoConstructorOverloads() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS);
            Node shapeConstructor1 = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            Node shapeConstructor2 = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            Node shapeConstructor3 = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            graph.linkTwoNodes(shapeClass, shapeConstructor1, RelationType.METHOD);
            graph.linkTwoNodes(shapeClass, shapeConstructor2, RelationType.METHOD);
            graph.linkTwoNodes(shapeClass, shapeConstructor3, RelationType.METHOD);
            graph.setConstructorVPs();
            assertEquals(1, graph.getNbConstructorVPs());
        });
    }

    @Test
    public void TwoClassesNoConstructorOverload() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS);
            Node shapeConstructor1 = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            Node polygonClass = graph.createNode("Polygon", EntityType.CLASS);
            Node polygonConstructor = graph.createNode("Polygon", EntityType.CONSTRUCTOR);
            graph.linkTwoNodes(shapeClass, shapeConstructor1, RelationType.METHOD);
            graph.linkTwoNodes(polygonClass, polygonConstructor, RelationType.METHOD);
            graph.setConstructorVPs();
            assertEquals(0, graph.getNbConstructorVPs());
        });
    }

    @Test
    public void TwoClassesOneConstructorOverload() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS);
            Node shapeConstructor1 = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            Node shapeConstructor2 = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            Node polygonClass = graph.createNode("Polygon", EntityType.CLASS);
            Node polygonConstructor = graph.createNode("Polygon", EntityType.CONSTRUCTOR);
            graph.linkTwoNodes(shapeClass, shapeConstructor1, RelationType.METHOD);
            graph.linkTwoNodes(shapeClass, shapeConstructor2, RelationType.METHOD);
            graph.linkTwoNodes(polygonClass, polygonConstructor, RelationType.METHOD);
            graph.setConstructorVPs();
            assertEquals(1, graph.getNbConstructorVPs());
        });
    }

    @Test
    public void TwoClassesTwoConstructorOverloads() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS);
            Node shapeConstructor1 = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            Node shapeConstructor2 = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            Node polygonClass = graph.createNode("Polygon", EntityType.CLASS);
            Node polygonConstructor1 = graph.createNode("Polygon", EntityType.CONSTRUCTOR);
            Node polygonConstructor2 = graph.createNode("Polygon", EntityType.CONSTRUCTOR);
            graph.linkTwoNodes(shapeClass, shapeConstructor1, RelationType.METHOD);
            graph.linkTwoNodes(shapeClass, shapeConstructor2, RelationType.METHOD);
            graph.linkTwoNodes(polygonClass, polygonConstructor1, RelationType.METHOD);
            graph.linkTwoNodes(polygonClass, polygonConstructor2, RelationType.METHOD);
            graph.setConstructorVPs();
            assertEquals(2, graph.getNbConstructorVPs());
        });
    }

}