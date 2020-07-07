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
import org.antlr.v4.generatedsources.cpp14.CPP14Lexer;
import org.antlr.v4.generatedsources.cpp14.CPP14Parser;
import org.antlr.v4.generatedsources.cpp14.CPP14_macroLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import visitors.cpp.ClassesVisitorCPP;
import visitors.java.ClassesVisitor;
import visitors.java.FactoryVisitor;
import visitors.java.GraphBuilderVisitor;
import visitors.java.StrategyTemplateDecoratorVisitor;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Inspired by https://www.programcreek.com/2014/01/how-to-resolve-bindings-when-using-eclipse-jdt-astparser/
 */
public class Symfinder {

    private static final Logger logger = LogManager.getLogger(Symfinder.class);
    private static final String PRE_COMPILED_DIR = "preCompiled";
    private static final Level LEVEL = Level.getLevel("MY_LEVEL");

    private NeoGraph neoGraph;
    private String sourcePackage;
    private String precompiledPackage;
    private String graphOutputPath;
    private String language;
    private String macrosFile;

    public Symfinder(String sourcePackage, String graphOutputPath, String language) {
        this.sourcePackage = sourcePackage;
        this.precompiledPackage = sourcePackage + "/" + PRE_COMPILED_DIR;
        this.graphOutputPath = graphOutputPath;
        this.language = language;
        this.macrosFile = sourcePackage + "/macros.symfinder";
        this.neoGraph = new NeoGraph(Configuration.getNeo4JBoltAddress(),
                Configuration.getNeo4JUser(),
                Configuration.getNeo4JPassword());
    }

    public void run() throws IOException, InterruptedException {
        long symfinderStartTime = System.currentTimeMillis();
        boolean java = language.equals("java");
        logger.printf(LEVEL, "Symfinder version: %s", System.getenv("SYMFINDER_VERSION"));
        logger.printf(LEVEL, "Souce package: %s", this.sourcePackage);

        List<File> files;
        if (java) {
            files = getJavaSourcesFiles(sourcePackage);
        } else {
            files = getCPPSourcesFiles(sourcePackage);
        }

        neoGraph.createClassesIndex();
        neoGraph.createInterfacesIndex();

        if (java) {
            String classpathPath = System.getenv("JAVA_HOME");
            if (classpathPath == null) { // default to linux openJDK 11 path
                classpathPath = "/usr/lib/jvm/java-11-openjdk";
            }

            logger.log(LEVEL, "ClassesVisitor");
            visitPackage(classpathPath, files, new ClassesVisitor(neoGraph));
            logger.log(LEVEL, "GraphBuilderVisitor");
            visitPackage(classpathPath, files, new GraphBuilderVisitor(neoGraph));
            logger.log(LEVEL, "StrategyTemplateVisitor");
            visitPackage(classpathPath, files, new StrategyTemplateDecoratorVisitor(neoGraph));
            logger.log(LEVEL, "FactoryVisitor");
            visitPackage(classpathPath, files, new FactoryVisitor(neoGraph));
        } else {
            logger.log(LEVEL, "===== Lexer for Macros =====");
            detectMacros(files);
            logger.log(LEVEL, "===== Preprocessing using g++ =====");
            precompileCPP(files);
            files = getCPPSourcesFiles(precompiledPackage);

            logger.log(LEVEL, "===== Lexer for CPP14 =====");
            logger.log(LEVEL, "ClassesVisitor");
            analyseCPP(files);
            cleanUpCpp();
            neoGraph.detectCPPStrategyPatterns();
            neoGraph.detectCPPDecoratorPatterns();
        }

        neoGraph.detectVPsAndVariants();
        logger.log(LEVEL, "Number of VPs: {}", neoGraph.getTotalNbVPs());
        logger.log(LEVEL, "Number of methods VPs: {}", neoGraph.getNbMethodVPs());
        logger.log(LEVEL, "Number of constructors VPs: {}", neoGraph.getNbConstructorVPs());
        logger.log(LEVEL, "Number of method level VPs: {}", neoGraph.getNbMethodLevelVPs());
        logger.log(LEVEL, "Number of class level VPs: {}", neoGraph.getNbClassLevelVPs());
        logger.log(LEVEL, "Number of variants: {}", neoGraph.getTotalNbVariants());
        logger.log(LEVEL, "Number of methods variants: {}", neoGraph.getNbMethodVariants());
        logger.log(LEVEL, "Number of constructors variants: {}", neoGraph.getNbConstructorVariants());
        logger.log(LEVEL, "Number of method level variants: {}", neoGraph.getNbMethodLevelVariants());
        logger.log(LEVEL, "Number of class level variants: {}", neoGraph.getNbClassLevelVariants());
        logger.log(LEVEL, "Number of nodes: {}", neoGraph.getNbNodes());
        logger.log(LEVEL, "Number of relationships: {}", neoGraph.getNbRelationships());
        if (java) {
            logger.log(LEVEL, "Number of corrected inheritance relationships: {}/{}", GraphBuilderVisitor.getNbCorrectedInheritanceLinks(), neoGraph.getNbInheritanceRelationships());
        }
        neoGraph.writeGraphFile(graphOutputPath);
        neoGraph.writeStatisticsFile(graphOutputPath.replace(".json", "-stats.json"));
        logger.debug(neoGraph.generateStatisticsJson());
        neoGraph.closeDriver();
        long symfinderExecutionTime = System.currentTimeMillis() - symfinderStartTime;
        logger.printf(Level.getLevel("MY_LEVEL"), "Total execution time: %s", formatExecutionTime(symfinderExecutionTime));
    }

    private void cleanUpCpp() {
        File precompiledDirectory = new File(precompiledPackage);
        deleteFiles(precompiledDirectory);
        precompiledDirectory.delete();
        new File(macrosFile).delete();
    }

    private void deleteFiles(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                deleteFiles(file);
            }
            if (!file.delete()) {
                logger.log(LEVEL, "Error deleting file {}", file.getPath());
            }
        }
    }

    private List<File> getJavaSourcesFiles(String rootPackage) throws IOException {
        return Files.walk(Paths.get(rootPackage))
                .filter(path -> !isTestPath(path))
                .map(Path::toFile)
                .filter(File::isFile)
                .filter(this::endsWithJavaExtension)
                .collect(Collectors.toList());
    }

    private List<File> getCPPSourcesFiles(String rootPackage) throws IOException {
        return Files.walk(Paths.get(rootPackage))
                .map(Path::toFile)
                .filter(File::isFile)
                .filter(this::endsWithCppExtension)
                .filter(this::excludeTestsInPath)
                .collect(Collectors.toList());
    }

    private boolean endsWithJavaExtension(File file) {
        return file.getName().endsWith(".java");
    }

    private boolean endsWithCppExtension(File file) {
        return file.getName().endsWith(".cpp") ||
                file.getName().endsWith(".cc") ||
                file.getName().endsWith(".h") ||
                file.getName().endsWith(".hh") ||
                file.getName().endsWith(".hpp");
    }

    private boolean excludeTestsInPath(File file) {
        return !file.getName().toLowerCase().contains("test") &&
                !file.getName().contains("test") &&
                !file.getPath().replaceFirst(".*" + sourcePackage, "").contains("test");
    }

    /**
     * Will get the macros from the given files, to write them to our macros.symfinder file to prepare the preprocessing
     *
     * @param files Cpp files possibly containing macros
     * @throws IOException
     */
    private void detectMacros(List<File> files) throws IOException {
        writeMacrosToFile(storeMacros(files));
    }

    /**
     * Detects the macros from the given files, using regular expressions and a simplified ANTLR 4 grammar
     *
     * @param files Cpp files possibly containing macros
     * @return String list containing all the detected macros
     */
    private List<String> storeMacros(List<File> files) {
        List<String> macros = new ArrayList<>();
        for (File file : files) {
            String fileContent = getFileLines(file);

            CPP14_macroLexer lexer = new CPP14_macroLexer(CharStreams.fromString(fileContent));
            Vocabulary vocabulary = lexer.getVocabulary();
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            for (Token token : tokens.getTokens()) {
                if (vocabulary.getSymbolicName(token.getType()).equals("Macro") ||
                        vocabulary.getSymbolicName(token.getType()).equals("MultiLineMacro")) {
                    String macro = token.getText();
                    Pattern pattern = Pattern.compile("^#[ ]*define.*");
                    Matcher matcher = pattern.matcher(macro);
                    if (matcher.find()) {
                        macro = macro.replace("\\ ", "");
                        macros.add(macro);
                    }
                }
            }
        }
        return macros;
    }

    private void writeMacrosToFile(List<String> macros) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(macrosFile))) {
            for (String macro : macros) {
                writer.newLine();
                writer.write(macro);
                writer.newLine();
            }
        }
        logger.log(LEVEL, "Number of macros detected : {}", macros.size());
    }

    /**
     * Uses a standard CPP14 ANTLR 4 grammar, after we preprocessed the files to create the CPP AST, then uses our visitor to visit this AST to analyse its variability
     *
     * @param files Cpp files to analyse
     */
    private void analyseCPP(List<File> files) {
        int count = 1;
        int total = files.size();
        List<String> npe_files = new ArrayList<>();
        for (File file : files) {

            try {
                logger.log(LEVEL, "Opening: {}", file.getName());
                logger.log(LEVEL, "ClassesVisitor: {}, {}/{} {}%", file.getName(), count, total, (int) ((((float) count) / total) * 100));

                String fileContent = getFileLines(file);

                CPP14Lexer lexer = new CPP14Lexer(CharStreams.fromString(fileContent));
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                CPP14Parser parser = new CPP14Parser(tokens);
                ParseTree tree = parser.translationunit();

                ClassesVisitorCPP visitor = new ClassesVisitorCPP(neoGraph);
                logger.log(LEVEL, "ClassesVisitor: Visiting {}", file.getName());

                visitor.visit(tree);
            } catch (NullPointerException e) {
                npe_files.add(file.getPath());
                e.printStackTrace();
                logger.error("ClassesVisitor Error: {}", file.getName(), e);
            }
            logger.log(LEVEL, "Closing: {}", file.getName());
            count += 1;
        }
    }

    /**
     * Apply the cpp command to run the preprocessor on each file. grep is used to delete the undesired preprocessor directives such as include, if, else, elif, endif and error.
     * If Symfinder is running on Windows, it will require a Ubuntu WSL to run the command.
     * The macros are written in a macros.symfinder file at the root of the analysed project. Moreover, the preprocessed result of the project is in a preCompiled folder at the root too.
     *
     * @param files Cpp files
     */
    private void precompileCPP(List<File> files) throws IOException, InterruptedException {
        String command = "cat %s | grep -v \"#[ \\t]*include.*\"| grep -v -e \"#[ \\t]*if.*\" -e \"#[ \\t]*endif.*\" -e \"#[ \\t]*else.*\"  -e \"#[ \\t]*elif.*\" -e \"#[ \\t]*error\" | cpp -imacros " + macrosFile + " -o %s -w";
        if (System.getProperty("os.name").contains("Windows")) {
            command = "ubuntu run \"" + command.replace("\"", "\\\"") + "\"";
        }
        Runtime rt = Runtime.getRuntime();
        int total = files.size();
        int count = 0;
        //new File(precompiledPackage).mkdirs();
        for (File file : files) {
            count++;

            File outputFile = Paths.get(precompiledPackage).resolve(Paths.get(sourcePackage)
                    .relativize(Paths.get(file.getPath()))).toFile();
            outputFile.getParentFile().mkdirs();
            logger.log(LEVEL, "Precompiling CPP : {} , {}/{} {}%", file.getName(), count, total, (int) ((((float) count) / total) * 100));

            String actualCommand = String.format(command, file.getPath().replace("\\", "/"), outputFile.getPath().replace("\\", "/"));
            String[] cmd = {"/bin/sh", "-c", actualCommand};
            Process pr = rt.exec(cmd);
            InputStream er = pr.getErrorStream();
            InputStream ir = pr.getInputStream();
            if (er.available() > 0) {
                Scanner sc = new Scanner(er);
                while (sc.hasNext()) {
                    logger.log(LEVEL, "Error Preprocessing {} : {}", file.getName(), sc.nextLine());
                }
            }
            if (ir.available() > 0) {
                Scanner sc = new Scanner(ir);
                while (sc.hasNext()) {
                    logger.log(LEVEL, "Error Preprocessing {} : {}", file.getName(), sc.nextLine());
                }
            }
            int returnCode = pr.waitFor();
            if (returnCode != 0) {
                logger.log(LEVEL, "Error during process of {} (code {})", file.getName(), returnCode);
                logger.log(LEVEL, "Error during command {}", actualCommand);
            }
        }
    }

    private void visitPackage(String classpathPath, List<File> files, ASTVisitor visitor) throws IOException {
        long startTime = System.currentTimeMillis();
        for (File file : files) {
            String fileContent = getFileLines(file);

            ASTParser parser = ASTParser.newParser(AST.JLS13);
            parser.setResolveBindings(true);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);

            parser.setBindingsRecovery(true);

            parser.setCompilerOptions(JavaCore.getOptions());

            parser.setUnitName(file.getCanonicalPath());

            parser.setEnvironment(new String[]{classpathPath}, new String[]{""}, new String[]{"UTF-8"}, true);
            parser.setSource(fileContent.toCharArray());

            Map <String, String> options = JavaCore.getOptions();
            options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_13);
            parser.setCompilerOptions(options);

            CompilationUnit cu = (CompilationUnit) parser.createAST(null);
            cu.accept(visitor);
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        logger.printf(LEVEL, "%s execution time: %s", visitor.getClass().getTypeName(), formatExecutionTime(elapsedTime));
    }

    private boolean isTestPath(Path path) {
        for (int i = 0 ; i < path.getNameCount() ; i++) {
            int finalI = i;
            if (List.of("test", "tests").stream().anyMatch(s -> path.getName(finalI).toString().equals(s))) {
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

    static String formatExecutionTime(long execTime) {
        long ms = execTime % 1000;
        long seconds = (execTime - ms) / 1000;
        long s = seconds % 60;
        long minutes = (seconds - s) / 60;
        long m = minutes % 60;
        long hours = (minutes - m) / 60;
        return String.format("%02d:%02d:%02d.%03d", hours, m, s, ms);
    }

}

