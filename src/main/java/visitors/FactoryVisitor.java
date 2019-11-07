package visitors;

import neo4j_types.DesignPatternType;
import neo4j_types.EntityAttribute;
import neo4j_types.EntityType;
import neograph.NeoGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.neo4j.driver.v1.types.Node;

/**
 * Detects factory patterns.
 * We detect as a factory pattern:
 * - a class who possesses a method which returns an object whose type is a subtype of the method return type
 * - a class whose name contains "Factory"
 */
public class FactoryVisitor extends SymfinderVisitor {

    private static final Logger logger = LogManager.getLogger(FactoryVisitor.class);

    public FactoryVisitor(NeoGraph neoGraph) {
        super(neoGraph);
    }

    @Override
    public boolean visit(TypeDeclaration type) {
        if (super.visit(type)) {
            String qualifiedName = type.resolveBinding().getQualifiedName();
            if (qualifiedName.contains("Factory")) {
                neoGraph.addLabelToNode(neoGraph.getOrCreateNode(qualifiedName, type.resolveBinding().isInterface() ? EntityType.INTERFACE : EntityType.CLASS), DesignPatternType.FACTORY.toString());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean visit(ReturnStatement node) {
        String typeOfReturnedObject;
        if (node.getExpression() != null &&
                node.getExpression().resolveTypeBinding() != null &&
                ! node.getExpression().resolveTypeBinding().isNested() &&
                (typeOfReturnedObject = node.getExpression().resolveTypeBinding().getQualifiedName()) != null &&
                ! typeOfReturnedObject.equals("null")) {
            MethodDeclaration methodDeclaration = (MethodDeclaration) getParentOfNodeWithType(node, ASTNode.METHOD_DECLARATION);
            if (methodDeclaration != null && ! methodDeclaration.isConstructor() && methodDeclaration.getReturnType2().resolveBinding() != null && methodDeclaration.resolveBinding() != null) {
                logger.debug(methodDeclaration.getName().getIdentifier());
                // Check for constructor because of java.sourceui/src/org/netbeans/api/java/source/ui/ElementJavadoc.java:391 in netbeans-incubator
                // TODO: 3/22/19 find why getReturnType2 returns null in core/src/main/java/org/apache/cxf/bus/managers/BindingFactoryManagerImpl.java
                // TODO: 4/18/19 find why resolveBinding returns null in AWT 9+181, KeyboardFocusManager.java:2439, return SNFH_FAILURE
                String parsedClassType = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName();
                String methodReturnType = methodDeclaration.getReturnType2().resolveBinding().getQualifiedName();
                logger.debug("typeOfReturnedObject : " + typeOfReturnedObject);
                logger.debug("methodReturnType : " + methodReturnType);
                // TODO: 4/30/19 if does not exist already, add label to filter on visualization
                Node methodReturnTypeNode = neoGraph.getOrCreateNode(methodReturnType, methodDeclaration.getReturnType2().resolveBinding().isInterface() ? EntityType.INTERFACE : EntityType.CLASS, new EntityAttribute[]{EntityAttribute.OUT_OF_SCOPE}, new EntityAttribute[]{});
                Node parsedClassNode = neoGraph.getOrCreateNode(parsedClassType, methodDeclaration.resolveBinding().getDeclaringClass().isInterface() ? EntityType.INTERFACE : EntityType.CLASS, new EntityAttribute[]{EntityAttribute.OUT_OF_SCOPE}, new EntityAttribute[]{});
                Node returnedObjectTypeNode = neoGraph.getOrCreateNode(typeOfReturnedObject, EntityType.CLASS);
                // TODO: 3/27/19 functional test case with method returning Object → not direct link
                if (neoGraph.relatedTo(methodReturnTypeNode, returnedObjectTypeNode) && neoGraph.getNbVariants(methodReturnTypeNode) >= 2) {
                    neoGraph.addLabelToNode(parsedClassNode, DesignPatternType.FACTORY.toString());
                }
            }
        }
        return false;
    }

    private ASTNode getParentOfNodeWithType(ASTNode node, int astNodeType) {
        ASTNode parentNode = node.getParent();
        // If parentNode == null, it means that we went up through all parents without finding
        // a node of the corresponding type.
        while (parentNode != null && parentNode.getNodeType() != astNodeType) {
            parentNode = parentNode.getParent();
        }
        return parentNode;
    }
}

