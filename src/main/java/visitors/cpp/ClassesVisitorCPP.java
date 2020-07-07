package visitors.cpp;

import neo4j_types.EntityAttribute;
import neo4j_types.EntityType;
import neo4j_types.RelationType;
import neograph.NeoGraph;
import org.antlr.v4.generatedsources.cpp14.CPP14Parser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.types.Node;
import visitors.cpp.utils.ContextCrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Parses all classes and the methods they contain, and adds them to the database for CPP.
 */
public class ClassesVisitorCPP extends SymfinderVisitorCPP {
    private static final Logger logger = LogManager.getLogger(ClassesVisitorCPP.class);
    private ContextCrawler contextCrawler;

    public ClassesVisitorCPP(NeoGraph neoGraph) {
        super(neoGraph);
        this.contextCrawler = new ContextCrawler();
    }

    /**
     * Visit the signature of a class and detects if there is inheritance
     * Detect also template class and/or specialization
     *
     * @param ctx Context of ClassHead
     */
    @Override
    public Boolean visitClasshead(CPP14Parser.ClassheadContext ctx) {
        Node thisNode = null;
        Node templateNode;
        EntityType nodeType = EntityType.CLASS;
        EntityAttribute[] nodeAttributes = new EntityAttribute[]{};
        String currentClass = "";
        if (ctx.classheadname() != null) {
            currentClass = this.contextCrawler.getQualifiedClassName(ctx);
            logger.printf(Level.INFO, "Entering Class: %s ", currentClass);
            boolean isClassSpecialization = currentClass.split("<").length > 1;

            if (isClassSpecialization) {
                String templateClass = currentClass.split("<")[0];

                templateNode = getOrCreateNodeClass(templateClass, EntityAttribute.CPP_TEMPLATE, "Found Template Class: %s");
                thisNode = getOrCreateNodeClass(currentClass, EntityAttribute.CPP_TEMPLATE_SPECIALIZATION, "Found Template Class specialization: %s ");

                if (!neoGraph.relatedTo(templateNode, thisNode)) {
                    neoGraph.linkTwoNodes(templateNode, thisNode, RelationType.EXTENDS);
                }
            } else {
                thisNode = getOrCreateNodeClass(currentClass, null, "Found Class: %s ");
            }
        }

        this.contextCrawler.linkBaseClass(ctx, thisNode, nodeType, nodeAttributes, neoGraph);
        Boolean result = super.visitClasshead(ctx);
        if (!currentClass.isEmpty()) {
            logger.printf(Level.INFO, "Exiting Class: %s", currentClass);
        }
        return result;
    }

    /**
     * Get the node of given parameter or create the node in neoGraph
     *
     * @param nodeName  Name of the node to get/create
     * @param attribute EntityAttribute for the node
     * @param log       Message to log
     * @return Node
     */
    private Node getOrCreateNodeClass(String nodeName, EntityAttribute attribute, String log) {
        Optional<Node> nodeOptional = neoGraph.getClassNode(nodeName);
        if (!nodeOptional.isPresent()) {
            logger.printf(Level.INFO, log, nodeName);
            if (attribute == null) {
                return neoGraph.createNode(nodeName, EntityType.CLASS);
            }
            return neoGraph.createNode(nodeName, EntityType.CLASS, attribute);
        }
        return nodeOptional.get();
    }

    /**
     * Detect if a class is abstract.
     * We need here to parse the entire class before in order to have the members and methods in neo4j
     *
     * @param ctx Context of Class
     */
    @Override
    public Boolean visitClassspecifier(CPP14Parser.ClassspecifierContext ctx) {
        Boolean result = super.visitClassspecifier(ctx);
        Optional<Node> node = neoGraph.getClassNode(this.contextCrawler.getQualifiedClassName(ctx.classhead()));
        node.ifPresent(neoGraph::detectCPPClassAbstract);
        return result;
    }

    /**
     * Detects template class
     *
     * @param ctx Context of Template Class Declaration
     */
    @Override
    public Boolean visitTemplatedeclaration(CPP14Parser.TemplatedeclarationContext ctx) {
        try {
            CPP14Parser.TypespecifierContext typeContext = ctx.declaration()
                    .blockdeclaration()
                    .simpledeclaration()
                    .declspecifierseq()
                    .declspecifier()
                    .typespecifier();

            if (typeContext.classspecifier() != null) {
                String templateClassName = this.contextCrawler.getQualifiedClassName(typeContext.classspecifier().classhead());
                getOrCreateNodeClass(templateClassName, EntityAttribute.CPP_TEMPLATE, "Found Template Class %s");
            } else {
                // TODO: 02/12/2019 Template method specialization
                if (typeContext.trailingtypespecifier() != null) {
                    String result = typeContext.trailingtypespecifier().simpletypespecifier().getText();
                }
            }
        } catch (NullPointerException ignored) {
            logger.debug("No template class name found");
        }

        return super.visitTemplatedeclaration(ctx);
    }

    /**
     * Detects different members (attributes, methods and constructors) of a class
     *
     * @param ctx Context of member declaration
     */
    @Override
    public Boolean visitMemberdeclaration(CPP14Parser.MemberdeclarationContext ctx) {

        List<String> members = new ArrayList<>();
        if (ctx.memberdeclaratorlist() != null) {
            this.contextCrawler.getAllAttributes(ctx.memberdeclaratorlist(), members);
        }

        if (!members.isEmpty()) {
            for (String member : members) {

                String memberType = this.contextCrawler.getMemberType(ctx);
                if (memberType != null) {
                    Node attributeNode = neoGraph.createNode(member, EntityType.ATTRIBUTE);
                    neoGraph.setNodeAttribute(attributeNode, "type", memberType);
                    Optional<Node> classNode = neoGraph.getClassNode(this.contextCrawler.getQualifiedParentClassName(ctx));
                    classNode.ifPresent(node -> neoGraph.linkTwoNodes(node, attributeNode, RelationType.ATTRIBUTE));
                }
            }
        }

        try {
            if (ctx.declspecifierseq().declspecifier().functionspecifier() != null
                    && ctx.declspecifierseq().declspecifier().functionspecifier().Virtual() != null) {
                List<String> declarators = this.contextCrawler.getAbstractMethodNames(ctx.memberdeclaratorlist());
                for (String methodName : declarators) {
                    String parentClassName = this.contextCrawler.getQualifiedParentClassName(ctx);
                    EntityType methodType = Objects.equals(methodName, parentClassName) ? EntityType.CONSTRUCTOR : EntityType.METHOD;
                    EntityAttribute entityAttribute = this.contextCrawler.methodDeclarationEntityType(ctx);
                    if (entityAttribute != null) {
                        this.contextCrawler.registerMethod(parentClassName, methodName, methodType, entityAttribute, neoGraph);
                    } else {
                        this.contextCrawler.registerMethod(parentClassName, methodName, methodType, neoGraph);
                    }
                }

            }
        } catch (NullPointerException ignored) {
        }

        return super.visitMemberdeclaration(ctx);
    }

    /**
     * Detects methods of a class using the definition of function
     *
     * @param ctx Context of Function Definition
     */
    @Override
    public Boolean visitFunctiondefinition(CPP14Parser.FunctiondefinitionContext ctx) {
        String parentClassName = this.contextCrawler.getParentClassName(ctx);
        if (parentClassName != null && !this.contextCrawler.isDestructor(ctx)) {
            String methodName = this.contextCrawler.getFunctionName(ctx);
            String qualifiedParentClassName = this.contextCrawler.getQualifiedParentClassName(ctx);
            EntityType methodType = Objects.equals(methodName, parentClassName) ? EntityType.CONSTRUCTOR : EntityType.METHOD;
            this.contextCrawler.registerMethod(qualifiedParentClassName, methodName, methodType, neoGraph);
        }
        return super.visitFunctiondefinition(ctx);
    }

}
