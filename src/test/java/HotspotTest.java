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
 * Copyright 2018-2020 Johann Mortara <johann.mortara@univ-cotedazur.fr>
 * Copyright 2018-2020 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
 * Copyright 2018-2020 Philippe Collet <philippe.collet@univ-cotedazur.fr>
 */

import neo4j_types.EntityAttribute;
import neo4j_types.EntityType;
import neo4j_types.RelationType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.types.Node;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HotspotTest extends Neo4jTest {

    @Test
    public void subtypingOneVariantThresholdTwo() {
        runTest(graph -> {
            Node shapeNode = graph.createNode("Shape", EntityType.CLASS);
            Node rectangleNode = graph.createNode("Rectangle", EntityType.CLASS);
            graph.linkTwoNodes(shapeNode, rectangleNode, RelationType.EXTENDS);
            graph.detectVPsAndVariants();
            graph.detectSingularHotspotsInSubtyping(2);
            graph.setHotspotLabels();
            assertTrue(graph.getNode("Shape").get().hasLabel(EntityAttribute.VP.toString()));
            assertFalse(graph.getNode("Shape").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
            assertTrue(graph.getNode("Rectangle").get().hasLabel(EntityAttribute.VARIANT.toString()));
            assertFalse(graph.getNode("Rectangle").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
            assertFalse(graph.getNode("Rectangle").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
        });
    }

    @Test
    public void subtypingTwoVariantsThresholdTwo() {
        runTest(graph -> {
            Node shapeNode = graph.createNode("Shape", EntityType.CLASS);
            Node rectangleNode = graph.createNode("Rectangle", EntityType.CLASS);
            Node circleNode = graph.createNode("Circle", EntityType.CLASS);
            graph.linkTwoNodes(shapeNode, rectangleNode, RelationType.EXTENDS);
            graph.linkTwoNodes(shapeNode, circleNode, RelationType.EXTENDS);
            graph.detectVPsAndVariants();
            graph.detectSingularHotspotsInSubtyping(2);
            graph.setHotspotLabels();
            assertTrue(graph.getNode("Shape").get().hasLabel(EntityAttribute.VP.toString()));
            assertTrue(graph.getNode("Rectangle").get().hasLabel(EntityAttribute.VARIANT.toString()));
            assertTrue(graph.getNode("Circle").get().hasLabel(EntityAttribute.VARIANT.toString()));
            assertTrue(graph.getNode("Shape").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
            assertTrue(graph.getNode("Rectangle").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
            assertTrue(graph.getNode("Circle").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
        });
    }

    @Test
    public void subtypingTwoVariantsThresholdThree() {
        runTest(graph -> {
            Node shapeNode = graph.createNode("Shape", EntityType.CLASS);
            Node rectangleNode = graph.createNode("Rectangle", EntityType.CLASS);
            Node circleNode = graph.createNode("Circle", EntityType.CLASS);
            graph.linkTwoNodes(shapeNode, rectangleNode, RelationType.EXTENDS);
            graph.linkTwoNodes(shapeNode, circleNode, RelationType.EXTENDS);
            graph.detectVPsAndVariants();
            graph.detectSingularHotspotsInSubtyping(3);
            graph.setHotspotLabels();
            assertTrue(graph.getNode("Shape").get().hasLabel(EntityAttribute.VP.toString()));
            assertTrue(graph.getNode("Rectangle").get().hasLabel(EntityAttribute.VARIANT.toString()));
            assertTrue(graph.getNode("Circle").get().hasLabel(EntityAttribute.VARIANT.toString()));
            assertFalse(graph.getNode("Shape").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
            assertFalse(graph.getNode("Rectangle").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
            assertFalse(graph.getNode("Circle").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
        });
    }

    @Test
    public void subtypingThreeVariantsThresholdThree() {
        runTest(graph -> {
            Node shapeNode = graph.createNode("Shape", EntityType.CLASS);
            Node rectangleNode = graph.createNode("Rectangle", EntityType.CLASS);
            Node circleNode = graph.createNode("Circle", EntityType.CLASS);
            Node triangleNode = graph.createNode("Triangle", EntityType.CLASS);
            graph.linkTwoNodes(shapeNode, rectangleNode, RelationType.EXTENDS);
            graph.linkTwoNodes(shapeNode, circleNode, RelationType.EXTENDS);
            graph.linkTwoNodes(shapeNode, triangleNode, RelationType.EXTENDS);
            graph.detectVPsAndVariants();
            graph.detectSingularHotspotsInSubtyping(3);
            graph.setHotspotLabels();
            assertTrue(graph.getNode("Shape").get().hasLabel(EntityAttribute.VP.toString()));
            assertTrue(graph.getNode("Rectangle").get().hasLabel(EntityAttribute.VARIANT.toString()));
            assertTrue(graph.getNode("Circle").get().hasLabel(EntityAttribute.VARIANT.toString()));
            assertTrue(graph.getNode("Triangle").get().hasLabel(EntityAttribute.VARIANT.toString()));
            assertTrue(graph.getNode("Shape").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
            assertTrue(graph.getNode("Rectangle").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
            assertTrue(graph.getNode("Circle").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
            assertTrue(graph.getNode("Triangle").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
        });
    }

    @Test
    public void methodOverloadingTwoVariantsThresholdThree() {
        runTest(graph -> {
            Node shapeNode = graph.createNode("Shape", EntityType.CLASS);
            graph.setNodeAttribute(shapeNode, "methodVariants", 2);
            graph.setNodeAttribute(shapeNode, "constructorVariants", 0);
            graph.detectSingularHotspotsInOverloading(3);
            graph.setHotspotLabels();
            assertFalse(graph.getNode("Shape").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
        });
    }

    @Test
    public void methodOverloadingNoVariant() {
        runTest(graph -> {
            graph.createNode("Shape", EntityType.CLASS);
            graph.detectSingularHotspotsInOverloading(3);
            graph.setHotspotLabels();
            assertFalse(graph.getNode("Shape").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
        });
    }

    @Test
    public void methodOverloadingThreeVariantsThresholdThree() {
        runTest(graph -> {
            Node shapeNode = graph.createNode("Shape", EntityType.CLASS);
            graph.setNodeAttribute(shapeNode, "methodVariants", 3);
            graph.setNodeAttribute(shapeNode, "constructorVariants", 0);
            graph.detectSingularHotspotsInOverloading(3);
            graph.setHotspotLabels();
            assertTrue(graph.getNode("Shape").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
        });
    }

    @Test
    public void constructorOverloadingOneVPThresholdThree() {
        runTest(graph -> {
            Node shapeNode = graph.createNode("Shape", EntityType.CLASS);
            graph.setNodeAttribute(shapeNode, "methodVariants", 0);
            graph.setNodeAttribute(shapeNode, "constructorVariants", 1);
            graph.detectSingularHotspotsInOverloading(3);
            graph.setHotspotLabels();
            assertFalse(graph.getNode("Shape").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
        });
    }

    @Test
    public void constructorOverloadingThreeVariantsThresholdThree() {
        runTest(graph -> {
            Node shapeNode = graph.createNode("Shape", EntityType.CLASS);
            graph.setNodeAttribute(shapeNode, "methodVariants", 0);
            graph.setNodeAttribute(shapeNode, "constructorVariants", 3);
            graph.detectSingularHotspotsInOverloading(3);
            graph.setHotspotLabels();
            assertTrue(graph.getNode("Shape").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
        });
    }

    @Test
    public void mixedOverloadingTwoVPsThresholdThree() {
        runTest(graph -> {
            Node shapeNode = graph.createNode("Shape", EntityType.CLASS);
            graph.setNodeAttribute(shapeNode, "constructorVariants", 1);
            graph.setNodeAttribute(shapeNode, "methodVariants", 1);
            graph.detectSingularHotspotsInOverloading(3);
            graph.setHotspotLabels();
            assertFalse(graph.getNode("Shape").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
        });
    }

    @Test
    public void mixedOverloadingThreeVPsThresholdThree() {
        runTest(graph -> {
            Node shapeNode = graph.createNode("Shape", EntityType.CLASS);
            graph.setNodeAttribute(shapeNode, "constructorVariants", 2);
            graph.setNodeAttribute(shapeNode, "methodVariants", 1);
            graph.detectSingularHotspotsInOverloading(3);
            graph.setHotspotLabels();
            assertTrue(graph.getNode("Shape").get().hasLabel(EntityAttribute.HOTSPOT.toString()));
        });
    }

}
