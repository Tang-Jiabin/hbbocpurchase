package com.zykj.purchase.repository;

import com.zykj.purchase.pojo.RePay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RePayRepository extends JpaRepository<RePay,Integer> {
}
