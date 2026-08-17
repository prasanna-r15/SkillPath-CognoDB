// 1. Multi-hop: find a prerequisite chain from an existing skill to a missing target skill.
MATCH (l:Learner {id: $learnerId})-[:HAS_SKILL]->(start:Skill)
MATCH (r:Role {id: $roleId})-[:REQUIRES]->(target:Skill)
WHERE NOT (l)-[:HAS_SKILL]->(target)
MATCH path = shortestPath((start)-[:PREREQUISITE*1..5]->(target))
RETURN [n IN nodes(path) | n.name] AS learningPath
ORDER BY length(path)
LIMIT 5;

// 2. Graph-native recommendation: role requirements that the learner does not have,
//    then traverse Course -> TEACHES -> Skill to find learning options.
MATCH (l:Learner {id: $learnerId})-[:HAS_SKILL]->(have:Skill)
MATCH (role:Role {id: $roleId})-[:REQUIRES]->(needed:Skill)
WHERE NOT (l)-[:HAS_SKILL]->(needed)
OPTIONAL MATCH (course:Course)-[:TEACHES]->(needed)
RETURN needed.name AS missingSkill,
       collect(course.name)[0..3] AS recommendedCourses
ORDER BY needed.name;

// 3. Relationship exploration: what skills does a role require and which courses teach them?
MATCH (r:Role {id: $roleId})-[:REQUIRES]->(s:Skill)<-[:TEACHES]-(c:Course)
RETURN s.name AS skill, collect(c.name) AS courses
ORDER BY skill;
