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

public class MethodLevelVPsTest extends Neo4jTest {

    @Test
    public void NoMethodOverloadNoConstructorOverload() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS);
            Node shapeConstructor = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            Node displayMethod = graph.createNode("display", EntityType.METHOD);
            RelationType relationType = RelationType.METHOD;
            graph.linkTwoNodes(shapeClass, shapeConstructor, relationType);
            graph.linkTwoNodes(shapeClass, displayMethod, relationType);
            graph.setMethodVPs();
            graph.setConstructorVPs();
            assertEquals(0, graph.getNbMethodVPs());
            assertEquals(0, graph.getNbConstructorVPs());
            assertEquals(0, graph.getNbMethodLevelVPs());
        });
    }

    @Test
    public void OneMethodOverloadNoConstructorOverload() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS);
            Node shapeConstructor = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            Node displayMethod1 = graph.createNode("display", EntityType.METHOD);
            Node displayMethod2 = graph.createNode("display", EntityType.METHOD);
            RelationType relationType = RelationType.METHOD;
            graph.linkTwoNodes(shapeClass, shapeConstructor, relationType);
            graph.linkTwoNodes(shapeClass, displayMethod1, relationType);
            graph.linkTwoNodes(shapeClass, displayMethod2, relationType);
            graph.setMethodVPs();
            graph.setConstructorVPs();
            assertEquals(1, graph.getNbMethodVPs());
            assertEquals(0, graph.getNbConstructorVPs());
            assertEquals(1, graph.getNbMethodLevelVPs());
        });
    }

    @Test
    public void NoMethodOverloadOneConstructorOverload() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS);
            Node shapeConstructor1 = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            Node shapeConstructor2 = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            Node displayMethod = graph.createNode("display", EntityType.METHOD);
            RelationType relationType = RelationType.METHOD;
            graph.linkTwoNodes(shapeClass, shapeConstructor1, relationType);
            graph.linkTwoNodes(shapeClass, shapeConstructor2, relationType);
            graph.linkTwoNodes(shapeClass, displayMethod, relationType);
            graph.setMethodVPs();
            graph.setConstructorVPs();
            assertEquals(0, graph.getNbMethodVPs());
            assertEquals(1, graph.getNbConstructorVPs());
            assertEquals(1, graph.getNbMethodLevelVPs());
        });
    }

    @Test
    public void OneMethodOverloadOneConstructorOverload() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS);
            Node shapeConstructor1 = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            Node shapeConstructor2 = graph.createNode("Shape", EntityType.CONSTRUCTOR);
            Node displayMethod1 = graph.createNode("display", EntityType.METHOD);
            Node displayMethod2 = graph.createNode("display", EntityType.METHOD);
            RelationType relationType = RelationType.METHOD;
            graph.linkTwoNodes(shapeClass, shapeConstructor1, relationType);
            graph.linkTwoNodes(shapeClass, shapeConstructor2, relationType);
            graph.linkTwoNodes(shapeClass, displayMethod1, relationType);
            graph.linkTwoNodes(shapeClass, displayMethod2, relationType);
            graph.setMethodVPs();
            graph.setConstructorVPs();
            assertEquals(1, graph.getNbMethodVPs());
            assertEquals(1, graph.getNbConstructorVPs());
            assertEquals(2, graph.getNbMethodLevelVPs());
        });
    }

}