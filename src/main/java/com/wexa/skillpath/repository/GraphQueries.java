package com.wexa.skillpath.repository;

public final class GraphQueries {
    private GraphQueries() {}

    public static final String LIST_LEARNERS = """
            MATCH (l:Learner)
            RETURN l.id AS id, l.name AS name
            ORDER BY l.name
            """;

    public static final String LIST_ROLES = """
            MATCH (r:Role)
            RETURN r.id AS id, r.name AS name
            ORDER BY r.name
            """;

    public static final String RECOMMENDATIONS = """
            MATCH (l:Learner {id: $learnerId})-[:HAS_SKILL]->(have:Skill)
            MATCH (role:Role {id: $roleId})-[:REQUIRES]->(needed:Skill)
            WHERE NOT (l)-[:HAS_SKILL]->(needed)
            OPTIONAL MATCH (course:Course)-[:TEACHES]->(needed)
            WITH needed,
                 collect(DISTINCT course.name)[0..3] AS courses
            OPTIONAL MATCH path=(needed)-[:PREREQUISITE*1..3]->(prereq:Skill)
            WITH needed, courses, path
            ORDER BY length(path) ASC
            WITH needed, courses, collect([node IN nodes(path) | node.name])[0] AS pathNames
            RETURN needed.id AS skillId,
                   needed.name AS skillName,
                   CASE WHEN needed.priority = 'HIGH' THEN 'High' ELSE 'Recommended' END AS priority,
                   courses,
                   CASE WHEN pathNames IS NOT NULL AND size(pathNames) > 0 THEN pathNames ELSE [needed.name] END AS prerequisitePath
            ORDER BY CASE WHEN needed.priority = 'HIGH' THEN 0 ELSE 1 END, needed.name
            """;

    public static final String ROLE_SKILL_COVERAGE = """
            MATCH (l:Learner {id: $learnerId})
            MATCH (r:Role {id: $roleId})
            CALL {
                WITH r
                MATCH (r)-[:REQUIRES]->(required:Skill)
                RETURN count(required) AS totalSkills
            }
            CALL {
                WITH l, r
                MATCH (r)-[:REQUIRES]->(required:Skill)
                WHERE (l)-[:HAS_SKILL]->(required)
                RETURN count(required) AS matchedSkills
            }
            RETURN totalSkills, matchedSkills
            """;

    public static final String LEARNING_PATH = """
            MATCH (l:Learner {id: $learnerId})-[:HAS_SKILL]->(start:Skill)
            MATCH (r:Role {id: $roleId})-[:REQUIRES]->(target:Skill)
            WHERE NOT (l)-[:HAS_SKILL]->(target)
            MATCH path = shortestPath((start)-[:PREREQUISITE*1..5]->(target))
            RETURN start.name AS startSkill,
                   [n IN nodes(path) | n.name] AS path
            ORDER BY length(path), start.name
            LIMIT 5
            """;

}
