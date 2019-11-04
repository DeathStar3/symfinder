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

import neo4j_types.EntityAttribute;
import neo4j_types.EntityType;
import neo4j_types.RelationType;
import org.junit.Test;
import org.neo4j.driver.v1.types.Node;

import static org.junit.Assert.assertEquals;

public class ClassLevelVariantsTest extends Neo4jTest {

    @Test
    public void NoSubclass() {
        runTest(graph -> {
            graph.createNode("Shape", EntityType.CLASS, EntityAttribute.ABSTRACT);
            graph.setVPLabels();
            graph.setVariantsLabels();
            assertEquals(0, graph.getNbClassLevelVariants());
        });
    }

    @Test
    public void OneConcreteClass() {
        runTest(graph -> {
            graph.createNode("Shape", EntityType.CLASS);
            graph.detectVPsAndVariants();
            assertEquals(0, graph.getNbClassLevelVariants());
        });
    }

    @Test
    public void OneSubclass() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS, EntityAttribute.ABSTRACT);
            Node circleClass = graph.createNode("Circle", EntityType.CLASS);
            graph.linkTwoNodes(shapeClass, circleClass, RelationType.EXTENDS);
            graph.detectVPsAndVariants();
            assertEquals(1, graph.getNbClassLevelVariants());
        });
    }

    @Test
    public void ThreeSubclasses() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS, EntityAttribute.ABSTRACT);
            Node circleClass = graph.createNode("Circle", EntityType.CLASS);
            Node rectangleClass = graph.createNode("Rectangle", EntityType.CLASS);
            Node triangleClass = graph.createNode("Triangle", EntityType.CLASS);
            graph.linkTwoNodes(shapeClass, circleClass, RelationType.EXTENDS);
            graph.linkTwoNodes(shapeClass, rectangleClass, RelationType.EXTENDS);
            graph.linkTwoNodes(shapeClass, triangleClass, RelationType.EXTENDS);
            graph.detectVPsAndVariants();
            assertEquals(3, graph.getNbClassLevelVariants());
        });
    }

    // TODO: 3/25/19 determine if we should detect a variant or not
    @Test
    public void OneAbstractSubclass() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("Shape", EntityType.CLASS, EntityAttribute.ABSTRACT);
            Node polygonClass = graph.createNode("Polygon", EntityType.CLASS, EntityAttribute.ABSTRACT);
            graph.linkTwoNodes(shapeClass, polygonClass, RelationType.EXTENDS);
            graph.detectVPsAndVariants();
            assertEquals(1, graph.getNbClassLevelVariants());
        });
    }

    @Test
    public void OutOfScopeSuperclass() {
        runTest(graph -> {
            Node objectClass = graph.createNode("Object", EntityType.CLASS, EntityAttribute.ABSTRACT, EntityAttribute.OUT_OF_SCOPE);
            Node polygonClass = graph.createNode("Polygon", EntityType.CLASS);
            graph.linkTwoNodes(objectClass, polygonClass, RelationType.EXTENDS);
            graph.detectVPsAndVariants();
            assertEquals(0, graph.getNbClassLevelVariants());
        });
    }
}
