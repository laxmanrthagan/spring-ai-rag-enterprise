package com.ragagentic.ai.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GraphRagAgent {

    private final Neo4jClient neo4jClient;


    public List<String> fetchRelations(String query) {

        log.info("Executing Graph RAG extraction for query: {}", query);

        try {

            String cypher = """
                    MATCH (e)-[r]->(n)
                    WHERE toLower(e.name) CONTAINS toLower($query)
                       OR toLower(n.name) CONTAINS toLower($query)
                    RETURN 
                        e.name + ' -[' + type(r) + ']-> ' + n.name AS relation
                    LIMIT 10
                    """;
            return neo4jClient.query(cypher)
                    .bind(query)
                    .to("query")
                    .fetch()
                    .all()
                    .stream()
                    .map(row -> row.get("relation").toString())
                    .toList();
        } catch (Exception e) {

            log.error("Graph RAG traversal failed", e);
            return Collections.emptyList();
        }
    }
}