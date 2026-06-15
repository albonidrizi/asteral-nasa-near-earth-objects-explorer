package com.nasa.asteral.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import com.nasa.asteral.AbstractIntegrationTest;
import com.nasa.asteral.model.db.FavoriteAsteroid;
import com.nasa.asteral.model.db.MyUser;
import com.nasa.asteral.service.FavoriteAsteroidService;

class FavoriteAsteroidRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private FavoriteAsteroidRepository favoriteRepository;
    @Autowired
    private MyUserRepository userRepository;
    @Autowired
    private FavoriteAsteroidService favoriteService;

    @Test
    void databaseEnforcesPerUserFavoriteUniquenessAndIsolation() {
        userRepository.save(user("alice"));
        userRepository.save(user("bob"));
        favoriteRepository.saveAndFlush(favorite("alice", "123"));
        favoriteRepository.saveAndFlush(favorite("bob", "123"));

        assertEquals(1, favoriteRepository.findByUsername("alice").size());
        assertEquals(1, favoriteRepository.findByUsername("bob").size());
        assertThrows(DataIntegrityViolationException.class,
                () -> favoriteRepository.saveAndFlush(favorite("alice", "123")));
    }

    @Test
    void concurrentDuplicateFavoriteAddsResultInOneRow() throws Exception {
        userRepository.save(user("concurrent-user"));
        int workers = 8;
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(workers)) {
            List<Future<Void>> results = new ArrayList<>();
            for (int index = 0; index < workers; index++) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    favoriteService.addAsteroidToFavorite("same-asteroid", "concurrent-user");
                    return null;
                }));
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            executor.shutdown();
            assertTrue(executor.awaitTermination(20, TimeUnit.SECONDS));
            for (Future<Void> result : results) {
                result.get();
            }
        }

        assertEquals(1, favoriteRepository.findByUsername("concurrent-user").size());
    }

    private MyUser user(String username) {
        MyUser user = new MyUser();
        user.setUsername(username);
        user.setPassword("not-a-real-password-hash");
        user.setRole("USER");
        return user;
    }

    private FavoriteAsteroid favorite(String username, String referenceId) {
        return FavoriteAsteroid.builder().username(username).asteroidReferenceId(referenceId).build();
    }
}
