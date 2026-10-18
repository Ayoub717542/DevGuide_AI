package com.example.DevTutor.repository;

import com.example.DevTutor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
