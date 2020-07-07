package visitors.java;

import neo4j_types.EntityAttribute;
import neo4j_types.EntityType;
import neo4j_types.RelationType;
import neograph.NeoGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.neo4j.driver.types.Node;

import java.util.Optional;

/**
 * Parses all classes and creates the inheritance relations.
 * This step cannot be done in the ClassesVisitor, as due to problems with name resolving in Eclipse JDT,
 * we have to do this manually by finding the corresponding nodes in the database.
 * Hence, all nodes must have been parsed at least once.
 */
public class GraphBuilderVisitor extends ImportsVisitor {

    private static final Logger logger = LogManager.getLogger(GraphBuilderVisitor.class);

    private static int nbCorrectedInheritanceLinks = 0;

    public GraphBuilderVisitor(NeoGraph neoGraph) {
        super(neoGraph);
    }

    @Override
    public boolean visit(TypeDeclaration type) {
        if (super.visit(type)) {
            ITypeBinding classBinding = type.resolveBinding();
            String thisClassName = classBinding.getQualifiedName();
            logger.debug("Class: " + thisClassName);
            Optional <Node> thisNode = classBinding.isInterface() ? neoGraph.getInterfaceNode(thisClassName) : neoGraph.getClassNode(thisClassName);
            if (thisNode.isPresent()) {
                // Link to superclass if exists
                ITypeBinding superclassType = classBinding.getSuperclass();
                if (superclassType != null) {
                    createImportedClassNode(classBinding.getErasure().getQualifiedName(), thisNode.get(), superclassType, EntityType.CLASS, RelationType.EXTENDS, "SUPERCLASS");
                }

                // Link to implemented interfaces if exist
                for (ITypeBinding o : classBinding.getInterfaces()) {
                    createImportedClassNode(classBinding.getErasure().getQualifiedName(), thisNode.get(), o, EntityType.INTERFACE, RelationType.IMPLEMENTS, "INTERFACE");
                }
            }
            return true;
        }
        return false; // TODO: 4/18/19 functional tests : only inner classes are ignored
    }

    // TODO: 4/1/19 functional tests : imports from different packages
    private void createImportedClassNode(String thisClassName, Node thisNode, ITypeBinding importedClassType, EntityType entityType, RelationType relationType, String name) {
        Optional <String> myImportedClass = getClassFullName(importedClassType);
        String qualifiedName = getClassBaseName(importedClassType.getQualifiedName());
        if (myImportedClass.isPresent() && ! myImportedClass.get().equals(qualifiedName)) {
            nbCorrectedInheritanceLinks++;
            logger.debug(String.format("DIFFERENT %s FULL NAMES FOUND FOR CLASS %s: \n" +
                    "JDT qualified name: %s\n" +
                    "Manually resolved name: %s\n" +
                    "Getting manually resolved name.", name, getClassBaseName(thisClassName), qualifiedName, myImportedClass.get()));
        }
        Node superclassNode = neoGraph.getOrCreateNode(myImportedClass.orElse(qualifiedName), entityType, new EntityAttribute[]{EntityAttribute.OUT_OF_SCOPE}, new EntityAttribute[]{});
        neoGraph.linkTwoNodes(superclassNode, thisNode, relationType);
    }


    public static int getNbCorrectedInheritanceLinks() {
        return nbCorrectedInheritanceLinks;
    }
}
