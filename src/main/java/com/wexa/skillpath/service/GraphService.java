package com.wexa.skillpath.service;

import com.wexa.skillpath.model.Option;
import com.wexa.skillpath.model.Recommendation;
import com.wexa.skillpath.repository.GraphRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GraphService {
    private final GraphRepository repository;

    public GraphService(GraphRepository repository) {
        this.repository = repository;
    }

    public List<Option> learners() { return repository.findLearners(); }
    public List<Option> roles() { return repository.findRoles(); }
    public List<Recommendation> recommendations(String learnerId, String roleId) {
        return repository.findRecommendations(learnerId, roleId);
    }
    public int[] coverage(String learnerId, String roleId) {
        return repository.findCoverage(learnerId, roleId);
    }
    public List<List<String>> learningPaths(String learnerId, String roleId) {
        return repository.findLearningPaths(learnerId, roleId);
    }
    public void seed() { repository.seed(); }
    public void verifyConnectivity() { repository.verifyConnectivity(); }
}
