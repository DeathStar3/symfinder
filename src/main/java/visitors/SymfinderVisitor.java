package visitors;

import neograph.NeoGraph;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.*;

import java.util.Arrays;

/**
 * This class is inherited by all visitors and ensures that some parts of the code are ignored:
 * - enums
 * - test classes
 * - private nested classes
 * - anonymous classes
 */
public class SymfinderVisitor extends ASTVisitor {

    private static final Logger logger = LogManager.getLogger(SymfinderVisitor.class);
    protected NeoGraph neoGraph;
    protected boolean visitedType = false;

    public SymfinderVisitor(NeoGraph neoGraph) {
        this.neoGraph = neoGraph;
    }

    @Override
    public boolean visit(TypeDeclaration type) {
        ITypeBinding classBinding = type.resolveBinding();
        logger.printf(Level.INFO, "Visitor: %s - Class: %s", this.getClass().getTypeName(), classBinding.getQualifiedName());
        visitedType = ! isTestClass(classBinding) && ! (classBinding.isNested() && Modifier.isPrivate(classBinding.getModifiers())) && ! classBinding.isEnum() && ! classBinding.isAnonymous();
        return visitedType;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration classDeclarationStatement) {
        return false;
    }

    protected boolean isTestClass(ITypeBinding classBinding) {
        return Arrays.asList(classBinding.getPackage().getNameComponents()).contains("test");
    }

    protected static String getClassBaseName(String className){
        return className.split("<")[0];
    }
}
