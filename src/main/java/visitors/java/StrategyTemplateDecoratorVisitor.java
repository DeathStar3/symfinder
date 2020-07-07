package visitors.java;

import neo4j_types.DesignPatternType;
import neo4j_types.EntityAttribute;
import neo4j_types.EntityType;
import neograph.NeoGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.*;
import org.neo4j.driver.types.Node;

import java.util.Optional;

/**
 * Detects strategy, template and decorator patterns.
 * We detect as a strategy pattern:
 * - a class who possesses at least two variants and is used as a field in another class
 * - a class whose name contains "Strategy"
 * We detect as a template pattern:
 * - an abstract class which possesses at least one subclass and contains a concrete method calling an abstract method of this same class
 * - a class whose name contains "Template"
 * We detect as a decorator pattern:
 * - a class which possesses at least one subclass and a field corresponding to a class having at least two subclasses
 * - a class whose name contains "Decorator"
 */
// TODO name contains template + update doc
public class StrategyTemplateDecoratorVisitor extends ImportsVisitor {

    private static final Logger logger = LogManager.getLogger(StrategyTemplateDecoratorVisitor.class);

    private ITypeBinding fieldDeclaringClassBinding;

    public StrategyTemplateDecoratorVisitor(NeoGraph neoGraph) {
        super(neoGraph);
    }

    @Override
    public boolean visit(FieldDeclaration field) {
        logger.debug(field);
        ITypeBinding fieldTypeBinding = field.getType().resolveBinding();
        if (field.getParent() instanceof TypeDeclaration && fieldTypeBinding != null) { // prevents the case where the field is an enum, which does not bring variability
            fieldDeclaringClassBinding = ((TypeDeclaration) field.getParent()).resolveBinding();
            Optional <String> classFullName = getClassFullName(fieldTypeBinding);
            if (classFullName.isPresent()) {
                Optional <Node> typeNode = neoGraph.getNode(classFullName.get());
                typeNode.ifPresent(node -> {
                    if (fieldTypeBinding.getName().contains("Strategy") || neoGraph.getNbVariants(node) >= 2) {
                        neoGraph.addLabelToNode(node, DesignPatternType.STRATEGY.toString());
                    }
                    if (fieldTypeBinding.getName().contains("Decorator")) {
                        neoGraph.addLabelToNode(node, DesignPatternType.DECORATOR.toString());
                    }
                    checkAbstractDecorator(fieldDeclaringClassBinding, fieldTypeBinding);
                });
            }
        }
        return false;
    }

    private void checkAbstractDecorator(ITypeBinding currentClassBinding, ITypeBinding fieldClassBinding) {
        Optional <String> currentClassFullName = getClassFullName(currentClassBinding);
        Optional <String> fieldClassFullName = getClassFullName(fieldClassBinding);
        if (currentClassFullName.isPresent() && fieldClassFullName.isPresent()) {
            Node currentClassNode = neoGraph.getNode(currentClassFullName.get()).get();
            Node fieldClassNode = neoGraph.getNode(fieldClassFullName.get()).get();
            String currentClassName = currentClassBinding.getErasure().getQualifiedName();
            boolean isClassInheritingFieldClass = neoGraph.getSuperclassNode(currentClassName).map(node -> node.equals(fieldClassNode)).orElse(false);
            boolean isClassImplementingFieldClass = neoGraph.getImplementedInterfacesNodes(currentClassName).stream().anyMatch(node -> node.equals(fieldClassNode));
//            if(fieldClassBinding.getErasure().getQualifiedName().contains("Decorator") ||
//                    ((isClassInheritingFieldClass || isClassImplementingFieldClass) && neoGraph.getNbVariants(fieldClassNode) >= 2 && neoGraph.getNbVariants(currentClassNode) >= 1)) {
            if (((isClassInheritingFieldClass || isClassImplementingFieldClass) && neoGraph.getNbVariants(fieldClassNode) >= 2 && neoGraph.getNbVariants(currentClassNode) >= 1)) {
                neoGraph.addLabelToNode(currentClassNode, DesignPatternType.DECORATOR.toString());
            }
        }

    }

    /**
     * This method is used to detect template patterns.
     * We do not explicitly check that the class is abstract as she must be abstract to define an abstract method.
     * We do not explicitly check the fact that the calling method is concrete; an abstract method cannot call another method as it does not have a body.
     */
    @Override
    public boolean visit(MethodInvocation node) {
        IMethodBinding methodBinding = node.resolveMethodBinding();
        if (methodBinding != null) { // TODO: 4/10/19 check why null in JavaGeom, math.geom3d.Box3D, p1.getX()
            ITypeBinding declaringClass = methodBinding.getDeclaringClass();
            Node declaringClassNode = neoGraph.getOrCreateNode(declaringClass.getQualifiedName(), declaringClass.isInterface() ? EntityType.INTERFACE : EntityType.CLASS, new EntityAttribute[]{EntityAttribute.OUT_OF_SCOPE}, new EntityAttribute[]{});
            if (neoGraph.getNbVariants(declaringClassNode) > 0 && (declaringClass.getName().contains("Template") || (declaringClass.equals(this.thisClassBinding) && Modifier.isAbstract(methodBinding.getModifiers())))) {
                neoGraph.addLabelToNode(declaringClassNode, DesignPatternType.TEMPLATE.toString());
            }
        }
        return false;
    }

}

