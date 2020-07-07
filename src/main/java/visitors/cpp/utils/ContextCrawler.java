package visitors.cpp.utils;

import neo4j_types.EntityAttribute;
import neo4j_types.EntityType;
import neo4j_types.RelationType;
import neograph.NeoGraph;
import org.antlr.v4.generatedsources.cpp14.CPP14Parser;
import org.antlr.v4.runtime.RuleContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContextCrawler {

    private static final Logger logger = LogManager.getLogger(ContextCrawler.class);

    public void linkBaseClass(CPP14Parser.ClassheadContext ctx, Node thisNode, EntityType nodeType, EntityAttribute[] nodeAttributes, NeoGraph neoGraph) {
        if (thisNode != null && ctx.baseclause() != null) {
            linkBaseClassList(ctx.baseclause().basespecifierlist(), thisNode, nodeType, nodeAttributes, neoGraph);
        }
    }

    /**
     * Recursive method to add all the base classes as multiple inheritance is possible in C++
     * @param ctx : Base specifier list context
     * @param thisNode : Current database node
     * @param nodeType : node EntityType
     * @param nodeAttributes : Attributes of the analysed class
     * @param neoGraph : current Neo4j database
     */
    public void linkBaseClassList(CPP14Parser.BasespecifierlistContext ctx, Node thisNode, EntityType nodeType, EntityAttribute[] nodeAttributes, NeoGraph neoGraph) {
        if (ctx == null) {
            return;
        }

        try {
            String baseClass = ctx.basespecifier().basetypespecifier().getText();
            boolean baseClassIsTemplate = baseClass.split("<").length > 1;

            Optional<Node> superclassNodeOpt = neoGraph.getClassNode(baseClass);
            Node superclassNode;
            if (!superclassNodeOpt.isPresent()) {
                logger.printf(Level.INFO, "Base class %s not found, creating node..", baseClass);
                if (baseClassIsTemplate) {
                    superclassNode = neoGraph.createNode(baseClass, nodeType, EntityAttribute.CPP_TEMPLATE_INSTANTIATION);
                } else {
                    superclassNode = neoGraph.createNode(baseClass, nodeType, nodeAttributes);
                }
            } else {
                superclassNode = superclassNodeOpt.get();
            }
            if (!neoGraph.relatedTo(superclassNode, thisNode)) {
                neoGraph.linkTwoNodes(superclassNode, thisNode, RelationType.EXTENDS);
            }
            if (baseClassIsTemplate) {
                String templateName = baseClass.split("<")[0];
                Optional<Node> templateClassNodeOpt = neoGraph.getClassNode(templateName);
                Node templateClassNode;
                if (!templateClassNodeOpt.isPresent()) {
                    logger.printf(Level.INFO, "Template class %s not found, creating node..", templateName);
                    templateClassNode = neoGraph.createNode(templateName, nodeType, EntityAttribute.CPP_TEMPLATE);
                } else {
                    templateClassNode = templateClassNodeOpt.get();
                }
                if (!neoGraph.relatedTo(templateClassNode, superclassNode)) {
                    neoGraph.linkTwoNodes(templateClassNode, superclassNode, RelationType.EXTENDS);
                }
            }
        } catch (NullPointerException ignored) {

        }

        linkBaseClassList(ctx.basespecifierlist(), thisNode, nodeType, nodeAttributes, neoGraph);
    }

    /**
     * Get class name with its namespace to be able to differenciate it from classes with the same name in another package
     * @param ctx : Class head context
     * @return : full class name as a String
     */
    public String getQualifiedClassName(CPP14Parser.ClassheadContext ctx) {
        String currentNamespace = getCurrentNamespace(ctx);
        if (!currentNamespace.equals("")) currentNamespace += "::";
        try {
            return String.format("%s%s", currentNamespace, ctx.classheadname().getText());
        } catch (NullPointerException ignored) {
        }
        return null;
    }

    public String getCurrentNamespace(RuleContext ctx) {
        if (ctx == null) return "";

        try {
            if (ctx instanceof CPP14Parser.NamespacedefinitionContext) {
                String parent = getCurrentNamespace(ctx.parent);
                String namespace = ((CPP14Parser.NamespacedefinitionContext) ctx).namednamespacedefinition()
                        .originalnamespacedefinition()
                        .Identifier()
                        .getText();
                if (!parent.equals(""))
                    namespace = String.format("%s::%s", parent, namespace);
                return namespace;
            }
        } catch (NullPointerException ignored) {
            return "";
        }
        return getCurrentNamespace(ctx.parent);
    }

    /**
     * Method to determine an entity type which will either be null or abstract
     */
    public EntityAttribute methodDeclarationEntityType(CPP14Parser.MemberdeclarationContext ctx) {
        if (ctx == null)
            return null;
        try {
            CPP14Parser.MemberdeclaratorlistContext memberDeclaratorList = ctx.memberdeclaratorlist();

            if (ctx.memberdeclaratorlist() != null) {
                while (memberDeclaratorList.memberdeclaratorlist() != null) {
                    memberDeclaratorList = memberDeclaratorList.memberdeclaratorlist();
                }
                CPP14Parser.MemberdeclaratorContext memberDeclarator = memberDeclaratorList.memberdeclarator();

                if (memberDeclarator.braceorequalinitializer() == null) {
                    return null;
                }
                if (memberDeclarator.braceorequalinitializer().initializerclause() != null) {
                    return EntityAttribute.ABSTRACT;
                }
                return null;
            }

        } catch (NullPointerException ignored) {

        }

        return null;
    }

    /**
     * Finds member type from a member declaration
     * @param ctx : Member declaration context
     * @return String
     */
    public String getMemberType(CPP14Parser.MemberdeclarationContext ctx) {
        return getMemberType(ctx.declspecifierseq());
    }

    /**
     * Intermediate method to down into the members list until we find an actual specifier
     * @param declspecifierseq : Declaration specifier sequence context
     * @return String
     */
    public String getMemberType(CPP14Parser.DeclspecifierseqContext declspecifierseq) {
        if (declspecifierseq == null) {
            return null;
        }
        if (declspecifierseq.declspecifierseq() == null) {
            return getMemberType(declspecifierseq.declspecifier());
        }
        return getMemberType(declspecifierseq.declspecifierseq());
    }

    /**
     * Returns the member type as a string
     * @param declspecifier : Declaration specifier context
     * @return String
     */
    public String getMemberType(CPP14Parser.DeclspecifierContext declspecifier) {
        try {
            return declspecifier.typespecifier().getText();
        } catch (NullPointerException ignored) {
        }
        return null;
    }

    /**
     * Goes through the member list to find the abstract methods' names
     *
     * @param memberdeclaratorlist : Member declarator list context
     * @return String List
     */
    public List<String> getAbstractMethodNames(CPP14Parser.MemberdeclaratorlistContext memberdeclaratorlist) {
        List<CPP14Parser.MemberdeclaratorContext> declarators = getAllMemberDeclarator(memberdeclaratorlist);
        List<String> methodNames = new ArrayList<>();
        for (CPP14Parser.MemberdeclaratorContext declarator : declarators) {
            try {
                methodNames.add(declarator
                        .declarator()
                        .ptrdeclarator()
                        .noptrdeclarator()
                        .noptrdeclarator()
                        .declaratorid()
                        .idexpression()
                        .unqualifiedid()
                        .Identifier()
                        .getText());
            } catch (NullPointerException ignored) {
            }
        }
        return methodNames;
    }

    /**
     * Register a method in the database
     *
     * @param parentClassName {@link String}
     * @param methodName      {@link String}
     * @param methodType      {@link EntityType} The type tag to add to the database, either Constructor or Method
     * @param neoGraph        {@link NeoGraph} Current database
     */
    public void registerMethod(String parentClassName, String methodName, EntityType methodType, NeoGraph neoGraph) {
        registerMethod(parentClassName, methodName, methodType, null, neoGraph);
    }

    /**
     * Register a method in the database
     *
     * @param parentClassName {@link String}
     * @param methodName      {@link String}
     * @param methodType      {@link EntityType} The type tag to add to the database, either Constructor or Method
     * @param entityAttribute {@link EntityAttribute} Optional entity attribute to add, can be null, or EntityAttribute.Abstract
     * @param neoGraph        {@link NeoGraph} Current database
     */
    public void registerMethod(String parentClassName, String methodName, EntityType methodType, EntityAttribute entityAttribute, NeoGraph neoGraph) {
        Node methodNode = entityAttribute == null ? neoGraph.createNode(methodName, methodType) : neoGraph.createNode(methodName, methodType, entityAttribute);
        Node parentClassNode = neoGraph.getOrCreateNode(parentClassName, EntityType.CLASS);
        neoGraph.linkTwoNodes(parentClassNode, methodNode, RelationType.METHOD);
    }

    public boolean isDestructor(CPP14Parser.FunctiondefinitionContext ctx) {
        try {

            return ctx.declarator().ptrdeclarator().noptrdeclarator().noptrdeclarator().declaratorid().idexpression().unqualifiedid().Tilde() != null;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Utility function returning a function name from it's function definition context
     *
     * @param ctx {@link CPP14Parser.FunctiondefinitionContext}
     * @return String
     */
    public String getFunctionName(CPP14Parser.FunctiondefinitionContext ctx) {
        try {
            return ctx.declarator().ptrdeclarator().noptrdeclarator().noptrdeclarator().declaratorid().idexpression().unqualifiedid().Identifier().getSymbol().getText();
        } catch (NullPointerException npe) {

        }
        try {
            if (ctx.declarator().ptrdeclarator().noptrdeclarator().noptrdeclarator().declaratorid().idexpression().unqualifiedid().Tilde() != null) {
                return ctx.declarator().ptrdeclarator().noptrdeclarator().noptrdeclarator().declaratorid().idexpression().unqualifiedid().Tilde().getText()
                        + ctx.declarator().ptrdeclarator().noptrdeclarator().noptrdeclarator().declaratorid().idexpression().unqualifiedid().classname().getText();
            }
        } catch (NullPointerException e) {

        }
        return null;
    }

    /**
     * Return a parent class name of a parser context if it finds one , null otherwise,
     * Uses instanceOf to detect the correct context
     *
     * @param ctx {@link RuleContext}
     * @return String or Null
     */
    public String getParentClassName(RuleContext ctx) {
        if (ctx == null)
            return null;
        if (ctx instanceof CPP14Parser.ClassspecifierContext)
            try {
                return ((CPP14Parser.ClassspecifierContext) ctx).classhead().classheadname().classname().Identifier().toString();
            } catch (NullPointerException npe) {
                return null;
            }
        return getParentClassName(ctx.parent);
    }

    /**
     * Return the qualified parent class name of a parser context if it finds one , null otherwise,
     * Uses instanceOf to detect the correct context
     *
     * @param ctx {@link RuleContext}
     * @return String or Null
     */
    public String getQualifiedParentClassName(RuleContext ctx) {
        if (ctx == null)
            return null;
        if (ctx instanceof CPP14Parser.ClassspecifierContext)
            return getQualifiedClassName(((CPP14Parser.ClassspecifierContext) ctx).classhead());

        return getQualifiedParentClassName(ctx.parent);
    }

    /**
     * return a list of the member declarators in a member declarator list
     */
    public List<CPP14Parser.MemberdeclaratorContext> getAllMemberDeclarator(CPP14Parser.MemberdeclaratorlistContext ctx) {
        ArrayList<CPP14Parser.MemberdeclaratorContext> declarators = new ArrayList<>();
        getAllMemberDeclarator(ctx, declarators);
        return declarators;
    }

    public void getAllMemberDeclarator(CPP14Parser.MemberdeclaratorlistContext ctx, List<CPP14Parser.MemberdeclaratorContext> declarators) {
        if (ctx == null) {
            return;
        }
        try {
            declarators.add(ctx.memberdeclarator());
        } catch (NullPointerException ignored) {
        }
        getAllMemberDeclarator(ctx.memberdeclaratorlist(), declarators);
    }

    public void getAllAttributes(CPP14Parser.MemberdeclaratorlistContext ctx, List<String> members) {
        List<CPP14Parser.MemberdeclaratorContext> declarator = getAllMemberDeclarator(ctx);
        for (CPP14Parser.MemberdeclaratorContext memberdeclaratorContext : declarator) {
            try {
                String name = getAttributeName(memberdeclaratorContext.declarator());
                if (name != null)
                    members.add(name);
            } catch (NullPointerException ignored) {
            }

        }
    }

    /**
     * Return an attribute name from a declarator context
     */
    public String getAttributeName(CPP14Parser.DeclaratorContext ctx) {
        if (ctx.ptrdeclarator() == null)
            return null;
        return getAttributeName(ctx.ptrdeclarator());
    }

    public String getAttributeName(CPP14Parser.PtrdeclaratorContext ctx) {
        if (ctx == null)
            return null;
        if (ctx.noptrdeclarator() != null)
            return ctx.noptrdeclarator().declaratorid().getText();
        return getAttributeName(ctx.ptrdeclarator());
    }
}
