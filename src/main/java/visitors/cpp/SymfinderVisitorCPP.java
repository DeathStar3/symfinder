package visitors.cpp;

import neograph.NeoGraph;
import org.antlr.v4.generatedsources.cpp14.CPP14BaseVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SymfinderVisitorCPP extends CPP14BaseVisitor<Boolean> {
    private static final Logger logger = LogManager.getLogger(SymfinderVisitorCPP.class);
    protected NeoGraph neoGraph;

    public SymfinderVisitorCPP(NeoGraph neoGraph) {
        this.neoGraph = neoGraph;
    }

    protected static String getClassBaseName(String className) {
        return className.split("<")[0];
    }

    @Override
    public Boolean visit(ParseTree tree) {
        return super.visit(tree);
    }
}
