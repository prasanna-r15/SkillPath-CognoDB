// The executable Java seed is in GraphRepository.seed().
// This file documents the graph shape and can be used as a reference when inspecting CognoDB.
// All application queries use parameters; no user input is concatenated into Cypher.

// Example node shape:
// (:Learner {id, name})
// (:Skill {id, name, priority})
// (:Course {id, name, level})
// (:Role {id, name})

// Example relationships:
// (Learner)-[:HAS_SKILL]->(Skill)
// (Role)-[:REQUIRES]->(Skill)
// (Course)-[:TEACHES]->(Skill)
// (Skill)-[:PREREQUISITE]->(Skill)
