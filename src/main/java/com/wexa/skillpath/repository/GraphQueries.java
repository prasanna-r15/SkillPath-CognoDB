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
            WITH needed, courses,
                 [node IN nodes(path) | node.name] AS pathNames
            RETURN needed.id AS skillId,
                   needed.name AS skillName,
                   CASE WHEN needed.priority = 'HIGH' THEN 'High' ELSE 'Recommended' END AS priority,
                   courses,
                   CASE WHEN size(pathNames) > 0 THEN pathNames ELSE [needed.name] END AS prerequisitePath
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

    public static final String SEED = """
            // Learners
            UNWIND $learners AS learner
            MERGE (l:Learner {id: learner.id})
            SET l.name = learner.name

            // Skills
            WITH 1 AS keepAlive
            UNWIND $skills AS skill
            MERGE (s:Skill {id: skill.id})
            SET s.name = skill.name, s.priority = skill.priority

            // Courses
            WITH 1 AS keepAlive
            UNWIND $courses AS course
            MERGE (c:Course {id: course.id})
            SET c.name = course.name, c.level = course.level

            // Roles
            WITH 1 AS keepAlive
            UNWIND $roles AS role
            MERGE (r:Role {id: role.id})
            SET r.name = role.name

            // Learner -> Skill
            WITH 1 AS keepAlive
            UNWIND $learnerSkills AS item
            MATCH (l:Learner {id: item.learnerId}), (s:Skill {id: item.skillId})
            MERGE (l)-[:HAS_SKILL]->(s)

            // Role -> Skill
            WITH 1 AS keepAlive
            UNWIND $roleSkills AS item
            MATCH (r:Role {id: item.roleId}), (s:Skill {id: item.skillId})
            MERGE (r)-[:REQUIRES]->(s)

            // Course -> Skill
            WITH 1 AS keepAlive
            UNWIND $courseSkills AS item
            MATCH (c:Course {id: item.courseId}), (s:Skill {id: item.skillId})
            MERGE (c)-[:TEACHES]->(s)

            // Skill -> prerequisite Skill
            WITH 1 AS keepAlive
            UNWIND $prerequisites AS item
            MATCH (from:Skill {id: item.fromId}), (to:Skill {id: item.toId})
            MERGE (from)-[:PREREQUISITE]->(to)
            RETURN count(*) AS rows
            """;
}
