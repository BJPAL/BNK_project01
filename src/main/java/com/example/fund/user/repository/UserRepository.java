package com.example.fund.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fund.user.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

}
