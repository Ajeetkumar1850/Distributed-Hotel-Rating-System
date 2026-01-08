package com.micro.user.repositories;

import com.micro.user.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    public Optional<User> findByEmail(String email);

    public Optional<User> findByPhone(String phone);
}
