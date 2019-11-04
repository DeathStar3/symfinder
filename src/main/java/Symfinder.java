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

import configuration.Configuration;
import neograph.NeoGraph;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import visitors.ClassesVisitor;
import visitors.FactoryVisitor;
import visitors.GraphBuilderVisitor;
import visitors.StrategyTemplateDecoratorVisitor;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    public Symfinder(String sourcePackage, String graphOutputPath) {
        this.sourcePackage = sourcePackage;
        this.graphOutputPath = graphOutputPath;
        this.neoGraph = new
                NeoGraph(Configuration.getNeo4JBoltAddress(),
                Configuration.getNeo4JUser(),
                Configuration.getNeo4JPassword());
    }

    public void run() throws IOException {
        logger.log(Level.getLevel("MY_LEVEL"), "Symfinder version: " + System.getenv("SYMFINDER_VERSION"));
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
        visitPackage(classpathPath, files, new ClassesVisitor(neoGraph));
        logger.log(Level.getLevel("MY_LEVEL"), "GraphBuilderVisitor");
        visitPackage(classpathPath, files, new GraphBuilderVisitor(neoGraph));
        logger.log(Level.getLevel("MY_LEVEL"), "StrategyTemplateVisitor");
        visitPackage(classpathPath, files, new StrategyTemplateDecoratorVisitor(neoGraph));
        logger.log(Level.getLevel("MY_LEVEL"), "FactoryVisitor");
        visitPackage(classpathPath, files, new FactoryVisitor(neoGraph));

        neoGraph.detectVPsAndVariants();
        logger.log(Level.getLevel("MY_LEVEL"), "Number of VPs: " + neoGraph.getTotalNbVPs());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of methods VPs: " + neoGraph.getNbMethodVPs());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of constructors VPs: " + neoGraph.getNbConstructorVPs());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of method level VPs: " + neoGraph.getNbMethodLevelVPs());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of class level VPs: " + neoGraph.getNbClassLevelVPs());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of variants: " + neoGraph.getTotalNbVariants());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of methods variants: " + neoGraph.getNbMethodVariants());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of constructors variants: " + neoGraph.getNbConstructorVariants());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of method level variants: " + neoGraph.getNbMethodLevelVariants());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of class level variants: " + neoGraph.getNbClassLevelVariants());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of nodes: " + neoGraph.getNbNodes());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of relationships: " + neoGraph.getNbRelationships());
        logger.log(Level.getLevel("MY_LEVEL"), "Number of corrected inheritance relationships: " + GraphBuilderVisitor.getNbCorrectedInheritanceLinks() + "/" + neoGraph.getNbInheritanceRelationships());
        neoGraph.writeVPGraphFile(graphOutputPath);
        neoGraph.writeStatisticsFile(graphOutputPath.replace(".json", "-stats.json"));
        logger.debug(neoGraph.generateStatisticsJson());
        neoGraph.closeDriver();
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

    private boolean isTestPath(Path path) {
        for (int i = 0 ; i < path.getNameCount() ; i++) {
            if (path.getName(i).toString().equals("test")) {
                return true;
            }
        }
        return false;
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

}

