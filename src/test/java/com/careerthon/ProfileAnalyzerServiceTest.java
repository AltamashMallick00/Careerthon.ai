package com.careerthon;

import com.careerthon.model.ProfileReview;
import com.careerthon.repository.ProfileReviewRepository;
import com.careerthon.service.ProfileAnalyzerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ProfileAnalyzerServiceTest {

    private ProfileReviewRepository reviewRepository;
    private ProfileAnalyzerService analyzerService;
    private Map<Long, ProfileReview> db = new HashMap<>();

    @BeforeEach
    void setUp() {
        db.clear();
        reviewRepository = new ProfileReviewRepository() {
            @Override
            public Optional<ProfileReview> findById(Long id) {
                return Optional.ofNullable(db.get(id));
            }

            @Override
            public <S extends ProfileReview> S save(S entity) {
                if (entity.getId() == null) {
                    entity.setId((long) (db.size() + 1));
                }
                db.put(entity.getId(), entity);
                return entity;
            }

            @Override public List<ProfileReview> findByStatusOrderByCreatedAtDesc(ProfileReview.ReviewStatus status) { return Collections.emptyList(); }
            @Override public List<ProfileReview> findAllByOrderByCreatedAtDesc() { return new ArrayList<>(db.values()); }
            @Override public List<ProfileReview> findByLinkedinUrlOrderByCreatedAtDesc(String linkedinUrl) { return Collections.emptyList(); }
            @Override public Page<ProfileReview> findAllByOrderByCreatedAtDesc(Pageable pageable) { return Page.empty(); }
            @Override public Page<ProfileReview> findByUserNameContainingIgnoreCaseOrLinkedinUrlContainingIgnoreCaseOrEmailAddressContainingIgnoreCase(String name, String url, String email, Pageable pageable) { return Page.empty(); }
            @Override public double findAverageOverallScore() { return 70.0; }
            @Override public void flush() {}
            @Override public <S extends ProfileReview> S saveAndFlush(S entity) { return save(entity); }
            @Override public <S extends ProfileReview> List<S> saveAllAndFlush(Iterable<S> entities) { return Collections.emptyList(); }
            @Override public void deleteAllInBatch(Iterable<ProfileReview> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
            @Override public void deleteAllInBatch() {}
            @Override public ProfileReview getOne(Long aLong) { return db.get(aLong); }
            @Override public ProfileReview getById(Long aLong) { return db.get(aLong); }
            @Override public ProfileReview getReferenceById(Long aLong) { return db.get(aLong); }
            @Override public <S extends ProfileReview> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
            @Override public <S extends ProfileReview> List<S> findAll(Example<S> example) { return Collections.emptyList(); }
            @Override public <S extends ProfileReview> List<S> findAll(Example<S> example, Sort sort) { return Collections.emptyList(); }
            @Override public <S extends ProfileReview> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
            @Override public <S extends ProfileReview> long count(Example<S> example) { return 0; }
            @Override public <S extends ProfileReview> boolean exists(Example<S> example) { return false; }
            @Override public <S extends ProfileReview, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public <S extends ProfileReview> List<S> saveAll(Iterable<S> entities) { return Collections.emptyList(); }
            @Override public boolean existsById(Long aLong) { return db.containsKey(aLong); }
            @Override public List<ProfileReview> findAll() { return new ArrayList<>(db.values()); }
            @Override public List<ProfileReview> findAllById(Iterable<Long> longs) { return Collections.emptyList(); }
            @Override public long count() { return db.size(); }
            @Override public void deleteById(Long aLong) { db.remove(aLong); }
            @Override public void delete(ProfileReview entity) { db.remove(entity.getId()); }
            @Override public void deleteAllById(Iterable<? extends Long> longs) {}
            @Override public void deleteAll(Iterable<? extends ProfileReview> entities) {}
            @Override public void deleteAll() { db.clear(); }
            @Override public List<ProfileReview> findAll(Sort sort) { return Collections.emptyList(); }
            @Override public Page<ProfileReview> findAll(Pageable pageable) { return Page.empty(); }
        };

        analyzerService = new ProfileAnalyzerService(reviewRepository, null);
    }

    @Test
    void testUniversalArchitectAndSeniorProfileScoring() {
        ProfileReview review = analyzerService.createReview("https://www.linkedin.com/in/alex-cloud-architect/", "alex@test.com");
        ProfileReview result = analyzerService.analyzeProfile(review.getId());

        assertNotNull(result);
        assertEquals("Alex Cloud Architect", result.getUserName());
        assertTrue(result.getOverallScore() >= 88, "Architect score should be >= 88, was: " + result.getOverallScore());
        assertEquals("Excellent", result.getScoreLabel());
        assertTrue(result.getSuggestedRoles().contains("Senior Full Stack Engineer") || result.getSuggestedRoles().contains("Cloud Solutions Architect"));
    }

    @Test
    void testUniversalCustomDeveloperProfileScoring() {
        ProfileReview review = analyzerService.createReview("https://www.linkedin.com/in/sarah-fullstack-dev/", "sarah@test.com");
        ProfileReview result = analyzerService.analyzeProfile(review.getId());

        assertNotNull(result);
        assertEquals("Sarah Fullstack Dev", result.getUserName());
        assertTrue(result.getOverallScore() >= 75 && result.getOverallScore() <= 90, "Custom dev score should be 75-90, was: " + result.getOverallScore());
    }

    @Test
    void testUniversalBeginnerUncustomizedProfileScoring() {
        ProfileReview review = analyzerService.createReview("https://www.linkedin.com/in/md-afroz-hassan-3ab131297/", "afroz@test.com");
        ProfileReview result = analyzerService.analyzeProfile(review.getId());

        assertNotNull(result);
        assertEquals("Md Afroz Hassan", result.getUserName());
        assertEquals("Emerging Professional", result.getUserTitle());
        assertTrue(result.getOverallScore() >= 45 && result.getOverallScore() <= 55, "Uncustomized new profile score should be 45-55, was: " + result.getOverallScore());
        assertTrue(result.getActionableInsights().contains("Customize your LinkedIn URL"));
    }

    @Test
    void testUniversalStandardProfileScoring() {
        ProfileReview review = analyzerService.createReview("https://www.linkedin.com/in/john-doe/", "john@test.com");
        ProfileReview result = analyzerService.analyzeProfile(review.getId());

        assertNotNull(result);
        assertEquals("John Doe", result.getUserName());
        assertTrue(result.getOverallScore() >= 65 && result.getOverallScore() <= 78, "Standard profile score should be 65-78, was: " + result.getOverallScore());
    }
}
