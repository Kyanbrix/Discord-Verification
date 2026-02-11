package com.github.kyanbrix.restapi.repository;

import com.github.kyanbrix.restapi.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TokenRepository extends JpaRepository<UserToken, Long> {


    UserToken findByUserId(String userID);


    boolean existsByUserId(String userId);


    @Query(nativeQuery = true, value = "SELECT * FROM user_token WHERE user_id = :userId")
    List<UserToken> findTokenByUserId(@Param("userId") String userId);







}
