package com.devops.springmongo.repository;

import com.devops.springmongo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    List<User> findByActiveTrue();

    @Query("{ 'firstName' : { $regex: ?0, $options: 'i' } }")
    Page<User> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    @Query("{ $or: [ { 'firstName' : { $regex: ?0, $options: 'i' } }, " +
           "{ 'lastName' : { $regex: ?0, $options: 'i' } }, " +
           "{ 'username' : { $regex: ?0, $options: 'i' } }, " +
           "{ 'email' : { $regex: ?0, $options: 'i' } } ] }")
    Page<User> searchUsers(String keyword, Pageable pageable);

    long countByActiveTrue();
}