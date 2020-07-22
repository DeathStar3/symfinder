import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Map;
import java.util.stream.Stream;


public class GetChildrenProcedure {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;

    @Procedure(value = "symfinder.count")
    @Description("Counts for the node of given ID the number of its children having a given label. " +
            "Returns a map with the number of occurrences for nodes having the same name.")
    public Stream <Output> count(@Name("nodeId") long nodeId, @Name("label") String label) {
        final Output output = db.executeTransactionally(String.format("MATCH (c) WHERE ID(c) = $idNode " +
                "OPTIONAL MATCH (c)-->(m1:%s) OPTIONAL MATCH (c)-->(m2:%s) " +
                "WHERE m1.name = m2.name AND ID(m1) <> ID(m2) " +
                "WITH CASE WHEN m1.name IS NOT NULL THEN {name: m1.name, number: count(DISTINCT m1)} ELSE NULL END as counter " +
                "RETURN collect(counter)", label, label), Map.of("idNode", nodeId), Output::new);
        return Stream.of(output);
    }

    public class Output {
        public Object result;

        public Output(Result result) {
            result.stream()
                    .findFirst().flatMap(stringObjectMap -> stringObjectMap.entrySet().stream()
                    .findFirst()).ifPresent(stringObjectEntry -> this.result = stringObjectEntry.getValue());
        }

        @Override
        public String toString() {
            return "Output{" +
                    "result=" + result +
                    '}';
        }
    }
}
