package com.example.DevTutor.repository;

import com.example.DevTutor.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Message,Long> {

}
