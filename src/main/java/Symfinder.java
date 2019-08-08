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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with symfinder.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2018-2019 Johann Mortara <johann.mortara@univ-cotedazur.fr>
 * Copyright 2018-2019 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
 * Copyright 2018-2019 Philippe Collet <philippe.collet@univ-cotedazur.fr>
 */

import configuration.Configuration;
import neo4j_types.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.neo4j.driver.v1.types.Node;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Inspired by https://www.programcreek.com/2014/01/how-to-resolve-bindings-when-using-eclipse-jdt-astparser/
 */
public class Symfinder {

    private static final Logger logger = LogManager.getLogger(Symfinder.class);

    private NeoGraph neoGraph;
    private String sourcePackage;
    private String graphOutputPath;

    private int nbCorrectedInheritanceLinks = 0;

    public Symfinder(String sourcePackage, String graphOutputPath) {
        this.sourcePackage = sourcePackage;
        this.graphOutputPath = graphOutputPath;
        this.neoGraph = new NeoGraph(Configuration.getNeo4JBoltAddress(),
                Configuration.getNeo4JUser(),
                Configuration.getNeo4JPassword());
    }

    public void run() throws IOException {
        String classpathPath;

        classpathPath = System.getenv("JAVA_HOME");
        if (classpathPath == null) { // default to linux openJDK 8 path
            classpathPath = "/usr/lib/jvm/java-8-openjdk";
        }

        List <File> files = Files.walk(Paths.get(sourcePackage))
                .filter(Files::isRegularFile)
                .filter(path -> ! isTestPath(path))
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(".java"))
                .collect(Collectors.toList());

        neoGraph.createClassesIndex();
        neoGraph.createInterfacesIndex();

        logger.log(Level.getLevel("MY_LEVEL"), "ClassesVisitor");
        visitPackage(classpathPath, files, new ClassesVisitor());
        logger.log(Level.getLevel("MY_LEVEL"), "GraphBuilderVisitor");
        visitPackage(classpathPath, files, new GraphBuilderVisitor());
        logger.log(Level.getLevel("MY_LEVEL"), "StrategyVisitor");
        visitPackage(classpathPath, files, new StrategyVisitor());
        logger.log(Level.getLevel("MY_LEVEL"), "FactoryVisitor");
        visitPackage(classpathPath, files, new FactoryVisitor());

        neoGraph.setMethodsOverloads();
        neoGraph.setConstructorsOverloads();
        neoGraph.setNbVariantsProperty();
        neoGraph.setVPLabels();
        neoGraph.writeVPGraphFile(graphOutputPath);
        logger.log(Level.getLevel("MY_LEVEL"), "Number of methods VPs: " + neoGraph.getTotalNbOverloadedMethods());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of constructors VPs: " + neoGraph.getTotalNbOverloadedConstructors());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of method level VPs: " + neoGraph.getNbMethodLevelVPs());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of class level VPs: " + neoGraph.getNbClassLevelVPs());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of VPs: " + neoGraph.getTotalNbVPs());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of methods variants: " + neoGraph.getNbMethodVariants());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of constructors variants: " + neoGraph.getNbConstructorVariants());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of method level variants: " + neoGraph.getNbMethodLevelVariants());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of class level variants: " + neoGraph.getNbClassLevelVariants());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of variants: " + neoGraph.getTotalNbVariants());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of nodes: " + neoGraph.getNbNodes());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of relationships: " + neoGraph.getNbRelationships());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of corrected inheritance relationships: " + nbCorrectedInheritanceLinks + "/" + neoGraph.getNbInheritanceRelationships());
        neoGraph.writeStatisticsFile(graphOutputPath.replace(".json", "-stats.json"));
        logger.debug(neoGraph.generateStatisticsJson());
        neoGraph.deleteGraph();
        neoGraph.closeDriver();
    }

    private boolean isTestPath(Path path) {
        for (int i = 0 ; i < path.getNameCount() ; i++) {
            if (path.getName(i).toString().equals("test")) {
                return true;
            }
        }
        return false;
    }

    private void visitPackage(String classpathPath, List <File> files, ASTVisitor visitor) throws IOException {
        long startTime = System.currentTimeMillis();
        for (File file : files) {
            String fileContent = getFileLines(file);

            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setResolveBindings(true);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);

            parser.setBindingsRecovery(true);

            parser.setCompilerOptions(JavaCore.getOptions());

            parser.setUnitName(file.getCanonicalPath());

            parser.setEnvironment(new String[]{classpathPath}, new String[]{""}, new String[]{"UTF-8"}, true);
            parser.setSource(fileContent.toCharArray());

            Map <String, String> options = JavaCore.getOptions();
            options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
            parser.setCompilerOptions(options);

            CompilationUnit cu = (CompilationUnit) parser.createAST(null);
            cu.accept(visitor);
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        logger.printf(Level.getLevel("MY_LEVEL"), "%s execution time: %s", visitor.getClass().getTypeName(), formatExecutionTime(elapsedTime));
    }

    private String getFileLines(File file) {
        for (Charset charset : Charset.availableCharsets().values()) {
            String lines = getFileLinesWithEncoding(file, charset);
            if (lines != null) {
                return lines;
            }
        }
        return null;
    }

    private String getFileLinesWithEncoding(File file, Charset charset) {
        try (Stream <String> lines = Files.lines(file.toPath(), charset)) {
            return lines.collect(Collectors.joining("\n"));
        } catch (UncheckedIOException e) {
            logger.debug(charset.displayName() + ": wrong encoding");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String formatExecutionTime(long execTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        Date resultdate = new Date(execTime);
        return sdf.format(resultdate);
    }

    /**
     * This class ensures is inherited by all visitors and ensures that some parts of the code are ignored:
     * - enums
     * - test classes
     * - private nested classes
     * - anonymous classes
     */
    private class SymfinderVisitor extends ASTVisitor {

        @Override
        public boolean visit(TypeDeclaration type) {
            ITypeBinding classBinding = type.resolveBinding();
            logger.printf(Level.INFO, "Visitor: %s - Class: %s", this.getClass().getTypeName(), classBinding.getQualifiedName());
            return ! isTestClass(classBinding) && ! (classBinding.isNested() && Modifier.isPrivate(classBinding.getModifiers())) && ! classBinding.isEnum() && ! classBinding.isAnonymous();
        }

        @Override
        public boolean visit(AnonymousClassDeclaration classDeclarationStatement) {
            return false;
        }

    }

    /**
     * Parses all classes and the methods they contain, and adds them to the database.
     */
    private class ClassesVisitor extends SymfinderVisitor {

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

    /**
     * Parses all classes and creates the inheritance relations.
     * This step cannot be done in the ClassesVisitor, as due to problems with name resolving in Eclipse JDT,
     * we have to do this manually by finding the corresponding nodes in the database.
     * Hence, all nodes must have been parsed at least once.
     */
    private class GraphBuilderVisitor extends SymfinderVisitor {

        List <ImportDeclaration> imports = new ArrayList <>();

        @Override
        public boolean visit(ImportDeclaration node) {
            if (! node.isStatic()) {
                imports.add(node);
            }
            return true;
        }

        @Override
        public boolean visit(TypeDeclaration type) {
            if (super.visit(type)) {
                ITypeBinding classBinding = type.resolveBinding();
                String thisClassName = classBinding.getQualifiedName();
                Optional <Node> thisNode = classBinding.isInterface() ? neoGraph.getInterfaceNode(thisClassName) : neoGraph.getClassNode(thisClassName);
                if (thisNode.isPresent()) {
                    // Link to superclass if exists
                    ITypeBinding superclassType = classBinding.getSuperclass();
                    if (superclassType != null) {
                        createImportedClassNode(thisClassName.split("<")[0], thisNode.get(), superclassType, EntityType.CLASS, RelationType.EXTENDS, "SUPERCLASS");
                    }

                    // Link to implemented interfaces if exist
                    for (ITypeBinding o : classBinding.getInterfaces()) {
                        createImportedClassNode(thisClassName.split("<")[0], thisNode.get(), o, EntityType.INTERFACE, RelationType.IMPLEMENTS, "INTERFACE");
                    }
                }
                return true;
            }
            return false; // TODO: 4/18/19 functional tests : only inner classes are ignored
        }

        // TODO: 4/1/19 functional tests : imports from different packages
        private void createImportedClassNode(String thisClassName, Node thisNode, ITypeBinding importedClassType, EntityType entityType, RelationType relationType, String name) {
            Optional <String> myImportedClass = getClassFullName(importedClassType.getName().split("<")[0]);
            String qualifiedName = importedClassType.getQualifiedName().split("<")[0];
            if (myImportedClass.isPresent() && ! myImportedClass.get().equals(qualifiedName)) {
                nbCorrectedInheritanceLinks++;
                logger.debug(String.format("DIFFERENT %s FULL NAMES FOUND FOR CLASS %s: \n" +
                        "JDT qualified name: %s\n" +
                        "Manually resolved name: %s\n" +
                        "Getting manually resolved name.", name, thisClassName.split("<")[0], qualifiedName, myImportedClass.get()));
            }
            Node superclassNode = neoGraph.getOrCreateNode(myImportedClass.orElse(qualifiedName), entityType, new EntityAttribute[]{EntityAttribute.OUT_OF_SCOPE}, new EntityAttribute[]{});
            neoGraph.linkTwoNodes(superclassNode, thisNode, relationType);
        }

        /**
         * Iterates on imports to find the real full class name (class name with package).
         * There are two kinds of imports:
         * - imports of classes:   a.b.TheClass  (1)
         * - imports of packages:  a.b.*         (2)
         * The determination is done in two steps:
         * - Iterate over (1). If a correspondence is found, return it.
         * - Iterate over (2) and for each one check in the database if the package a class with this class name.
         * WARNING: all classes must have been parsed at least once before executing this method.
         * Otherwise, the class we are looking to may not exist in the database.
         *
         * @param className
         *
         * @return
         */
        private Optional <String> getClassFullName(String className) {
            Optional <ImportDeclaration> first = imports.stream()
                    .filter(importDeclaration -> importDeclaration.getName().getFullyQualifiedName().endsWith(className))
                    .findFirst();
            if (first.isPresent()) {
                return Optional.of(first.get().getName().getFullyQualifiedName());
            }
            Optional <Optional <Node>> first1 = imports.stream()
                    .filter(ImportDeclaration::isOnDemand)
                    .map(importDeclaration -> neoGraph.getNodeWithNameInPackage(className, importDeclaration.getName().getFullyQualifiedName()))
                    .filter(Optional::isPresent)
                    .findFirst();
            return first1.map(node -> node.get().get("name").asString()); // Optional.empty -> out of scope class
        }

        @Override
        public void endVisit(TypeDeclaration node) {
            imports.clear();
        }

    }

    /**
     * Detects strategy patterns.
     * We detect as a strategy pattern:
     * - a class who possesses at least two variants and is used as a field in another class
     * - a class whose name contains "Strategy"
    */
    private class StrategyVisitor extends SymfinderVisitor {

        @Override
        public boolean visit(FieldDeclaration field) {
            logger.debug(field);
            ITypeBinding binding = field.getType().resolveBinding();
            if (binding != null) { // TODO: 12/6/18 log this
                Node typeNode = neoGraph.getOrCreateNode(binding.getQualifiedName(), binding.isInterface() ? EntityType.INTERFACE : EntityType.CLASS, new EntityAttribute[]{EntityAttribute.OUT_OF_SCOPE}, new EntityAttribute[]{});
                if (binding.getName().contains("Strategy") || neoGraph.getNbVariants(typeNode) >= 2) {
                    neoGraph.addLabelToNode(typeNode, DesignPatternType.STRATEGY.toString());
                }
            }
            return false;
        }

    }

    /**
     * Detects factory patterns.
     * We detect as a factory pattern:
     * - a class who possesses a method which returns an object whose type is a subtype of the method return type
     * - a class whose name contains "Factory"
     */
    private class FactoryVisitor extends SymfinderVisitor {

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

    }

	/**
	 * We consider as a test class a class contained in a package containing "test".
	 * This condition is sufficient if we consider projects built using Maven.
	 * Adding a condition on the class' name may remove from the analysis important classes (in JUnit for example)
	 */
    private boolean isTestClass(ITypeBinding classBinding) {
        return Arrays.asList(classBinding.getPackage().getNameComponents()).contains("test");
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

