package com.zykj.purchase.repository;

import com.zykj.purchase.pojo.DrawCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrawCodeRepository extends JpaRepository<DrawCode,Integer>, JpaSpecificationExecutor {


    List<DrawCode> findAllByStatus(Pageable pr , int status);
    List<DrawCode> findAllByStatus( int status);

    DrawCode findByCodeAndStatus(String usedCode, int i);

    int countByStatus(int status);
}
