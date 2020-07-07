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
import org.neo4j.driver.types.Node;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CPPClassAbstract extends Neo4jTest {

    @Test
    public void setCPPLabelAbstractClass() {
        runTest(graph -> {
            Node shape = graph.createNode("Shape",EntityType.CLASS);
            Node area = graph.createNode("area",EntityType.METHOD,EntityAttribute.ABSTRACT);
            graph.linkTwoNodes(shape,area,RelationType.METHOD);
            graph.detectCPPClassAbstract(shape);
            assertTrue(graph.getNode("Shape").get().hasLabel(EntityAttribute.ABSTRACT.toString()));
        });
    }

    @Test
    public void setCPPLabelAbstractClassFalsePositive() {
        runTest(graph -> {
            Node shape = graph.createNode("Shape",EntityType.CLASS);
            Node area = graph.createNode("area",EntityType.METHOD);
            graph.linkTwoNodes(shape,area,RelationType.METHOD);
            graph.detectCPPClassAbstract(shape);
            assertFalse(graph.getNode("Shape").get().hasLabel(EntityAttribute.ABSTRACT.toString()));
        });
    }
}
