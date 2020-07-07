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

public class MethodVariantsTest extends Neo4jTest {

    @Test
    public void OneClassNoMethodVariant() {
        runTest(graph -> {
            Node rectangleClass = graph.createNode("Rectangle", EntityType.CLASS);
            Node drawMethod = graph.createNode("draw", EntityType.METHOD);
            graph.linkTwoNodes(rectangleClass, drawMethod, RelationType.METHOD);
            graph.setMethodVariants();
            assertEquals(0, graph.getNbMethodVariants());
        });
    }

    @Test
    public void OneClassTwoMethodVariants() {
        runTest(graph -> {
            Node rectangleClass = graph.createNode("Rectangle", EntityType.CLASS);
            Node drawMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node drawMethod2 = graph.createNode("draw", EntityType.METHOD);
            graph.linkTwoNodes(rectangleClass, drawMethod1, RelationType.METHOD);
            graph.linkTwoNodes(rectangleClass, drawMethod2, RelationType.METHOD);
            graph.setMethodVariants();
            assertEquals(2, graph.getNbMethodVariants());
        });
    }

    @Test
    public void OneClassThreeMethodVariants() {
        runTest(graph -> {
            Node rectangleClass = graph.createNode("Rectangle", EntityType.CLASS);
            Node drawMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node drawMethod2 = graph.createNode("draw", EntityType.METHOD);
            Node drawMethod3 = graph.createNode("draw", EntityType.METHOD);
            graph.linkTwoNodes(rectangleClass, drawMethod1, RelationType.METHOD);
            graph.linkTwoNodes(rectangleClass, drawMethod2, RelationType.METHOD);
            graph.linkTwoNodes(rectangleClass, drawMethod3, RelationType.METHOD);
            graph.setMethodVariants();
            assertEquals(3, graph.getNbMethodVariants());
        });
    }

    @Test
    public void OneClassOneOverloadedMethod() {
        runTest(graph -> {
            Node rectangleClass = graph.createNode("Rectangle", EntityType.CLASS);
            Node drawMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node displayMethod1 = graph.createNode("display", EntityType.METHOD);
            Node displayMethod2 = graph.createNode("display", EntityType.METHOD);
            graph.linkTwoNodes(rectangleClass, drawMethod1, RelationType.METHOD);
            graph.linkTwoNodes(rectangleClass, displayMethod1, RelationType.METHOD);
            graph.linkTwoNodes(rectangleClass, displayMethod2, RelationType.METHOD);
            graph.setMethodVariants();
            assertEquals(2, graph.getNbMethodVariants());
        });
    }

    @Test
    public void OneClassTwoOverloadedMethodsAndNoVariantMethod() {
        runTest(graph -> {
            Node rectangleClass = graph.createNode("Rectangle", EntityType.CLASS);
            Node drawMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node displayMethod1 = graph.createNode("display", EntityType.METHOD);
            Node displayMethod2 = graph.createNode("display", EntityType.METHOD);
            Node showMethod1 = graph.createNode("show", EntityType.METHOD);
            Node showMethod2 = graph.createNode("show", EntityType.METHOD);
            Node showMethod3 = graph.createNode("show", EntityType.METHOD);
            graph.linkTwoNodes(rectangleClass, drawMethod1, RelationType.METHOD);
            graph.linkTwoNodes(rectangleClass, displayMethod1, RelationType.METHOD);
            graph.linkTwoNodes(rectangleClass, displayMethod2, RelationType.METHOD);
            graph.linkTwoNodes(rectangleClass, showMethod1, RelationType.METHOD);
            graph.linkTwoNodes(rectangleClass, showMethod2, RelationType.METHOD);
            graph.linkTwoNodes(rectangleClass, showMethod3, RelationType.METHOD);
            graph.setMethodVariants();
            assertEquals(5, graph.getNbMethodVariants());
        });
    }


    @Test
    public void TwoClassesNoMethodVariant() {
        runTest(graph -> {
            Node rectangleClass = graph.createNode("Rectangle", EntityType.CLASS);
            Node circleClass = graph.createNode("Circle", EntityType.CLASS);
            Node drawRectangleMethod = graph.createNode("draw", EntityType.METHOD);
            Node drawCircleMethod = graph.createNode("draw", EntityType.METHOD);
            graph.linkTwoNodes(rectangleClass, drawRectangleMethod, RelationType.METHOD);
            graph.linkTwoNodes(circleClass, drawCircleMethod, RelationType.METHOD);
            graph.setMethodVariants();
            assertEquals(0, graph.getNbMethodVariants());
        });
    }

    @Test
    public void TwoClassesTwoMethodVariants() {
        runTest(graph -> {
            Node rectangleClass = graph.createNode("Rectangle", EntityType.CLASS);
            Node circleClass = graph.createNode("Circle", EntityType.CLASS);
            Node drawRectangleMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node drawRectangleMethod2 = graph.createNode("draw", EntityType.METHOD);
            Node drawCircleMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node drawCircleMethod2 = graph.createNode("draw", EntityType.METHOD);
            graph.linkTwoNodes(rectangleClass, drawRectangleMethod1, RelationType.METHOD);
            graph.linkTwoNodes(rectangleClass, drawRectangleMethod2, RelationType.METHOD);
            graph.linkTwoNodes(circleClass, drawCircleMethod1, RelationType.METHOD);
            graph.linkTwoNodes(circleClass, drawCircleMethod2, RelationType.METHOD);
            graph.setMethodVariants();
            assertEquals(4, graph.getNbMethodVariants());
        });
    }


    @Test
    public void TwoClassesTwoMethodVariantsWithNoVariantMethod() {
        runTest(graph -> {
            Node rectangleClass = graph.createNode("Rectangle", EntityType.CLASS);
            Node circleClass = graph.createNode("Circle", EntityType.CLASS);
            Node displayRectangleMethod = graph.createNode("display", EntityType.METHOD);
            Node drawRectangleMethod1 = graph.createNode("draw", EntityType.METHOD);
            Node drawRectangleMethod2 = graph.createNode("draw", EntityType.METHOD);
            Node displayCircleMethod = graph.createNode("display", EntityType.METHOD);
            Node visualizeCircleMethod1 = graph.createNode("visualize", EntityType.METHOD);
            Node visualizeCircleMethod2 = graph.createNode("visualize", EntityType.METHOD);
            graph.linkTwoNodes(rectangleClass, displayRectangleMethod, RelationType.METHOD);
            graph.linkTwoNodes(rectangleClass, drawRectangleMethod1, RelationType.METHOD);
            graph.linkTwoNodes(rectangleClass, drawRectangleMethod2, RelationType.METHOD);
            graph.linkTwoNodes(circleClass, displayCircleMethod, RelationType.METHOD);
            graph.linkTwoNodes(circleClass, visualizeCircleMethod1, RelationType.METHOD);
            graph.linkTwoNodes(circleClass, visualizeCircleMethod2, RelationType.METHOD);
            graph.setMethodVariants();
            assertEquals(4, graph.getNbMethodVariants());
        });
    }
}
