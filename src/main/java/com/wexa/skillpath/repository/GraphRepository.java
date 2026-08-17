package com.wexa.skillpath.repository;

import com.wexa.skillpath.model.Option;
import com.wexa.skillpath.model.Recommendation;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Values;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.QueryConfig;
import org.neo4j.driver.Driver;
import org.neo4j.driver.QueryConfig;
import org.neo4j.driver.Record;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Values;

@Repository
public class GraphRepository {
    private final Driver driver;
    private final String database;

    public GraphRepository(Driver driver, @Value("${cognodb.database:neo4j}") String database) {
        this.driver = driver;
        this.database = database;
    }

    public List<Option> findLearners() {
        return driver.executableQuery(GraphQueries.LIST_LEARNERS)
                .withConfig(org.neo4j.driver.QueryConfig.builder().withDatabase(database).build())
                .execute().records().stream()
                .map(r -> new Option(r.get("id").asString(), r.get("name").asString()))
                .toList();
    }

    public List<Option> findRoles() {
        return driver.executableQuery(GraphQueries.LIST_ROLES)
                .withConfig(org.neo4j.driver.QueryConfig.builder().withDatabase(database).build())
                .execute().records().stream()
                .map(r -> new Option(r.get("id").asString(), r.get("name").asString()))
                .toList();
    }

    public List<Recommendation> findRecommendations(String learnerId, String roleId) {
        Map<String, Object> params = Map.of("learnerId", learnerId, "roleId", roleId);
        return driver.executableQuery(GraphQueries.RECOMMENDATIONS)
                .withParameters(params)
                .withConfig(org.neo4j.driver.QueryConfig.builder().withDatabase(database).build())
                .execute().records().stream()
                .map(r -> new Recommendation(
                        r.get("skillId").asString(),
                        r.get("skillName").asString(),
                        r.get("priority").asString(),
                        r.get("courses").asList(v -> v.asString()),
                        r.get("prerequisitePath").asList(v -> v.asString())))
                .toList();
    }

    public int[] findCoverage(String learnerId, String roleId) {
        Map<String, Object> params = Map.of("learnerId", learnerId, "roleId", roleId);
        Record record = driver.executableQuery(GraphQueries.ROLE_SKILL_COVERAGE)
                .withParameters(params)
                .withConfig(org.neo4j.driver.QueryConfig.builder().withDatabase(database).build())
                .execute().records().stream().findFirst().orElseThrow();
        return new int[]{record.get("totalSkills").asInt(), record.get("matchedSkills").asInt()};
    }

    public List<List<String>> findLearningPaths(String learnerId, String roleId) {
        Map<String, Object> params = Map.of("learnerId", learnerId, "roleId", roleId);
        return driver.executableQuery(GraphQueries.LEARNING_PATH)
                .withParameters(params)
                .withConfig(org.neo4j.driver.QueryConfig.builder().withDatabase(database).build())
                .execute().records().stream()
                .map(r -> r.get("path").asList(v -> v.asString()))
                .toList();
    }

        public void seed() {

                seedLearners();
                seedSkills();
                seedCourses();
                seedRoles();
                seedLearnerSkills();
                seedRoleSkills();
                seedCourseSkills();
                seedPrerequisites();
        }

        public void verifyConnectivity() {
                driver.verifyConnectivity();
        }

        public void testConnection() {
                driver.executableQuery("""
                RETURN 1 AS result
                """)
                .withConfig(
                        QueryConfig.builder()
                                .withDatabase(database)
                                .build()
                )
                .execute();
        }

        private void seedLearners() {

        Map<String, Object> params = Map.of(
                "learners", List.of(
                        Map.of("id", "L001", "name", "Asha"),
                        Map.of("id", "L002", "name", "Rahul")
                )
        );

        execute("""
                UNWIND $learners AS learner
                MERGE (l:Learner {id: learner.id})
                SET l.name = learner.name
                """, params);
        }

        private void seedSkills() {

    Map<String, Object> params = Map.of(
            "skills", List.of(
                    Map.of("id", "S001", "name", "Java", "priority", "HIGH"),
                    Map.of("id", "S002", "name", "Spring Boot", "priority", "HIGH"),
                    Map.of("id", "S003", "name", "REST APIs", "priority", "HIGH"),
                    Map.of("id", "S004", "name", "SQL", "priority", "HIGH"),
                    Map.of("id", "S005", "name", "Docker", "priority", "MEDIUM"),
                    Map.of("id", "S006", "name", "Kafka", "priority", "MEDIUM"),
                    Map.of("id", "S007", "name", "Microservices", "priority", "HIGH"),
                    Map.of("id", "S008", "name", "AWS", "priority", "MEDIUM"),
                    Map.of("id", "S009", "name", "Kubernetes", "priority", "MEDIUM")
            )
    );

    execute("""
            UNWIND $skills AS skill
            MERGE (s:Skill {id: skill.id})
            SET s.name = skill.name,
                s.priority = skill.priority
            """, params);
}

private void seedCourses() {

    Map<String, Object> params = Map.of(
            "courses", List.of(
                    Map.of("id", "C001", "name", "Java Backend Foundations", "level", "Beginner"),
                    Map.of("id", "C002", "name", "Spring Boot in Practice", "level", "Intermediate"),
                    Map.of("id", "C003", "name", "Designing REST APIs", "level", "Intermediate"),
                    Map.of("id", "C004", "name", "SQL for Backend Engineers", "level", "Intermediate"),
                    Map.of("id", "C005", "name", "Docker for Developers", "level", "Intermediate"),
                    Map.of("id", "C006", "name", "Apache Kafka Essentials", "level", "Intermediate"),
                    Map.of("id", "C007", "name", "Microservices Architecture", "level", "Advanced"),
                    Map.of("id", "C008", "name", "AWS Developer Essentials", "level", "Intermediate"),
                    Map.of("id", "C009", "name", "Kubernetes Fundamentals", "level", "Advanced")
            )
    );

    execute("""
            UNWIND $courses AS course
            MERGE (c:Course {id: course.id})
            SET c.name = course.name,
                c.level = course.level
            """, params);
}

private void seedRoles() {

    Map<String, Object> params = Map.of(
            "roles", List.of(
                    Map.of("id", "R001", "name", "Java Backend Engineer"),
                    Map.of("id", "R002", "name", "Cloud Microservices Engineer")
            )
    );

    execute("""
            UNWIND $roles AS role
            MERGE (r:Role {id: role.id})
            SET r.name = role.name
            """, params);
}

private void seedLearnerSkills() {

    Map<String, Object> params = Map.of(
            "learnerSkills", List.of(
                    Map.of("learnerId", "L001", "skillId", "S001"),
                    Map.of("learnerId", "L001", "skillId", "S004"),
                    Map.of("learnerId", "L002", "skillId", "S001"),
                    Map.of("learnerId", "L002", "skillId", "S002"),
                    Map.of("learnerId", "L002", "skillId", "S003"),
                    Map.of("learnerId", "L002", "skillId", "S005")
            )
    );

    execute("""
            UNWIND $learnerSkills AS item
            MATCH (l:Learner {id: item.learnerId})
            MATCH (s:Skill {id: item.skillId})
            MERGE (l)-[:HAS_SKILL]->(s)
            """, params);
}

private void seedRoleSkills() {

    Map<String, Object> params = Map.of(
            "roleSkills", List.of(
                    Map.of("roleId", "R001", "skillId", "S001"),
                    Map.of("roleId", "R001", "skillId", "S002"),
                    Map.of("roleId", "R001", "skillId", "S003"),
                    Map.of("roleId", "R001", "skillId", "S004"),
                    Map.of("roleId", "R001", "skillId", "S007"),

                    Map.of("roleId", "R002", "skillId", "S001"),
                    Map.of("roleId", "R002", "skillId", "S002"),
                    Map.of("roleId", "R002", "skillId", "S005"),
                    Map.of("roleId", "R002", "skillId", "S006"),
                    Map.of("roleId", "R002", "skillId", "S007"),
                    Map.of("roleId", "R002", "skillId", "S008"),
                    Map.of("roleId", "R002", "skillId", "S009")
            )
    );

    execute("""
            UNWIND $roleSkills AS item
            MATCH (r:Role {id: item.roleId})
            MATCH (s:Skill {id: item.skillId})
            MERGE (r)-[:REQUIRES]->(s)
            """, params);
}

private void seedCourseSkills() {

    Map<String, Object> params = Map.of(
            "courseSkills", List.of(
                    Map.of("courseId", "C001", "skillId", "S001"),
                    Map.of("courseId", "C002", "skillId", "S002"),
                    Map.of("courseId", "C003", "skillId", "S003"),
                    Map.of("courseId", "C004", "skillId", "S004"),
                    Map.of("courseId", "C005", "skillId", "S005"),
                    Map.of("courseId", "C006", "skillId", "S006"),
                    Map.of("courseId", "C007", "skillId", "S007"),
                    Map.of("courseId", "C008", "skillId", "S008"),
                    Map.of("courseId", "C009", "skillId", "S009")
            )
    );

    execute("""
            UNWIND $courseSkills AS item
            MATCH (c:Course {id: item.courseId})
            MATCH (s:Skill {id: item.skillId})
            MERGE (c)-[:TEACHES]->(s)
            """, params);
}

private void seedPrerequisites() {

    Map<String, Object> params = Map.of(
            "prerequisites", List.of(
                    Map.of("fromId", "S001", "toId", "S002"),
                    Map.of("fromId", "S002", "toId", "S003"),
                    Map.of("fromId", "S002", "toId", "S007"),
                    Map.of("fromId", "S005", "toId", "S007"),
                    Map.of("fromId", "S007", "toId", "S006"),
                    Map.of("fromId", "S007", "toId", "S008"),
                    Map.of("fromId", "S007", "toId", "S009")
            )
    );

    execute("""
            UNWIND $prerequisites AS item
            MATCH (from:Skill {id: item.fromId})
            MATCH (to:Skill {id: item.toId})
            MERGE (from)-[:PREREQUISITE]->(to)
            """, params);
}

private void execute(String query, Map<String, Object> params) {

    driver.executableQuery(query)
            .withParameters(params)
            .withConfig(
                    QueryConfig.builder()
                            .withDatabase(database)
                            .build()
            )
            .execute();
}
}
