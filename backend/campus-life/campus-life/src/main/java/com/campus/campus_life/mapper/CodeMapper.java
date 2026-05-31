package com.campus.campus_life.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;

public interface CodeMapper {
    @Insert("INSERT INTO verification_code(email,code,expire_time) VALUES(#{email},#{code},#{expire})")
    void insert(String email, String code, LocalDateTime expire);

    @Select("SELECT COUNT(*) FROM verification_code WHERE email=#{email} AND code=#{code} AND expire_time>NOW()")
    int check(String email, String code);
}