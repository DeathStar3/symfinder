package visitors.java;

import neo4j_types.EntityAttribute;
import neo4j_types.EntityType;
import neo4j_types.RelationType;
import neograph.NeoGraph;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.neo4j.driver.types.Node;

/**
 * Parses all classes and the methods they contain, and adds them to the database.
 */
public class ClassesVisitor extends SymfinderVisitor {

    private static final Logger logger = LogManager.getLogger(ClassesVisitor.class);

    public ClassesVisitor(NeoGraph neoGraph) {
        super(neoGraph);
    }

    @Override
    public boolean visit(TypeDeclaration type) {
        if (super.visit(type)) {
            EntityType nodeType;
            EntityAttribute[] nodeAttributes;
            // If the class is abstract
            if (Modifier.isAbstract(type.getModifiers())) {
                nodeType = EntityType.CLASS;
                nodeAttributes = new EntityAttribute[]{EntityAttribute.ABSTRACT};
                // If the type is an interface
            } else if (type.isInterface()) {
                nodeType = EntityType.INTERFACE;
                nodeAttributes = new EntityAttribute[]{};
                // The type is a class
            } else {
                nodeType = EntityType.CLASS;
                nodeAttributes = new EntityAttribute[]{};
            }
            neoGraph.createNode(type.resolveBinding().getQualifiedName(), nodeType, nodeAttributes);
            return true;
        }
        return false;
    }

    @Override
    public boolean visit(MethodDeclaration method) {
        // Ignoring methods in anonymous classes
        ITypeBinding declaringClass;
        if (! (method.resolveBinding() == null)) {
            declaringClass = method.resolveBinding().getDeclaringClass();
            String methodName = method.getName().getIdentifier();
            String parentClassName = declaringClass.getQualifiedName();
            logger.printf(Level.DEBUG, "Method: %s, parent: %s", methodName, parentClassName);
            EntityType methodType = method.isConstructor() ? EntityType.CONSTRUCTOR : EntityType.METHOD;
            Node methodNode = Modifier.isAbstract(method.getModifiers()) ? neoGraph.createNode(methodName, methodType, EntityAttribute.ABSTRACT) : neoGraph.createNode(methodName, methodType);
            Node parentClassNode = neoGraph.getOrCreateNode(parentClassName, declaringClass.isInterface() ? EntityType.INTERFACE : EntityType.CLASS);
            neoGraph.linkTwoNodes(parentClassNode, methodNode, RelationType.METHOD);
        }
        return false;
    }
}

