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
import org.junit.jupiter.api.Test;
import org.neo4j.driver.types.Node;

import static org.junit.Assert.assertEquals;

public class ClassLevelVPsTest extends Neo4jTest {

    @Test
    public void OneInterface() {
        runTest(graph -> {
            graph.createNode("Shape", EntityType.INTERFACE);
            graph.detectVPsAndVariants();
            assertEquals(1, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void OneAbstractClass() {
        runTest(graph -> {
            graph.createNode("Shape", EntityAttribute.ABSTRACT, EntityType.CLASS);
            graph.detectVPsAndVariants();
            assertEquals(1, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void OneExtendedClass() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS);
            Node circleClass = graph.createNode("Circle", EntityType.CLASS);
            graph.linkTwoNodes(shapeClass, circleClass, RelationType.EXTENDS);
            graph.detectVPsAndVariants();
            assertEquals(1, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void OneClass() {
        runTest(graph -> {
            graph.createNode("Shape", EntityType.CLASS);
            graph.detectVPsAndVariants();
            assertEquals(0, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void NoClass() {
        runTest(graph -> {
            assertEquals(0, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void OneAbstractClassOneInterface() {
        runTest(graph -> {
            graph.createNode("Serializable", EntityType.INTERFACE);
            graph.createNode("Shape", EntityAttribute.ABSTRACT, EntityType.CLASS);
            graph.detectVPsAndVariants();
            assertEquals(2, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void OneExtendedAbstractClass() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityAttribute.ABSTRACT, EntityType.CLASS);
            Node circleClass = graph.createNode("Circle", EntityType.CLASS);
            graph.linkTwoNodes(shapeClass, circleClass, RelationType.EXTENDS);
            graph.detectVPsAndVariants();
            assertEquals(1, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void OneExtendedInterface() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.INTERFACE);
            Node circleClass = graph.createNode("Circle", EntityType.CLASS);
            graph.linkTwoNodes(shapeClass, circleClass, RelationType.IMPLEMENTS);
            graph.detectVPsAndVariants();
            assertEquals(1, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void TwoLevelsClassVP() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityAttribute.ABSTRACT, EntityType.CLASS);
            Node circleClass = graph.createNode("Circle", EntityType.CLASS);
            Node smallCircleClass = graph.createNode("SmallCircle", EntityType.CLASS);
            graph.linkTwoNodes(shapeClass, circleClass, RelationType.EXTENDS);
            graph.linkTwoNodes(circleClass, smallCircleClass, RelationType.EXTENDS);
            graph.detectVPsAndVariants();
            assertEquals(2, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void NotCountingOutOfScopeVPs() {
        runTest(graph -> {
            graph.createNode("Figure", EntityType.INTERFACE);
            graph.createNode("Shape", EntityAttribute.ABSTRACT, EntityType.CLASS);
            graph.createNode("Circle", EntityType.CLASS);
            graph.createNode("Object", EntityAttribute.ABSTRACT, EntityType.CLASS, EntityAttribute.OUT_OF_SCOPE);
            graph.detectVPsAndVariants();
            assertEquals(2, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void ClassInheritedMustBeCountedOnce() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS);
            Node circleClass = graph.createNode("Circle", EntityType.CLASS);
            Node rectangleClass = graph.createNode("Rectangle", EntityType.CLASS, EntityAttribute.OUT_OF_SCOPE);
            graph.linkTwoNodes(shapeClass, circleClass, RelationType.EXTENDS);
            graph.linkTwoNodes(shapeClass, rectangleClass, RelationType.EXTENDS);
            graph.detectVPsAndVariants();
            assertEquals(1, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void DesignPatternShallBeCounted() {
        runTest(graph -> {
            graph.createNode("Shape", EntityType.CLASS, DesignPatternType.STRATEGY);
            graph.detectVPsAndVariants();
            assertEquals(1, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void DesignPatternShallBeCountedOnce() {
        runTest(graph -> {
            graph.createNode("Shape", EntityType.INTERFACE, DesignPatternType.STRATEGY);
            graph.detectVPsAndVariants();
            assertEquals(1, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void NodeWithMultipleDesignPatternsShallBeCountedOnce() {
        runTest(graph -> {
            graph.createNode("Shape", EntityType.CLASS, DesignPatternType.STRATEGY, DesignPatternType.FACTORY);
            graph.detectVPsAndVariants();
            assertEquals(1, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void InterfaceWithDesignPatternShallBeCountedOnce() {
        runTest(graph -> {
            graph.createNode("Shape", EntityType.INTERFACE, DesignPatternType.DECORATOR);
            graph.detectVPsAndVariants();
            assertEquals(1, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void AbstractClassWithDesignPatternShallBeCountedOnce() {
        runTest(graph -> {
            graph.createNode("Shape", EntityType.CLASS, EntityAttribute.ABSTRACT, DesignPatternType.DECORATOR);
            graph.detectVPsAndVariants();
            assertEquals(1, graph.getNbClassLevelVPs());
        });
    }

    @Test
    public void ExtendedClassWithDesignPatternShallBeCountedOnce() {
        runTest(graph -> {
            Node shapeNode = graph.createNode("Shape", EntityType.CLASS, DesignPatternType.DECORATOR);
            Node rectangleNode = graph.createNode("Rectangle", EntityType.CLASS);
            graph.linkTwoNodes(shapeNode, rectangleNode, RelationType.EXTENDS);
            graph.detectVPsAndVariants();
            assertEquals(1, graph.getNbClassLevelVPs());
        });
    }
}