package com.example.userservice.repository;

import com.example.userservice.domin.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

}
