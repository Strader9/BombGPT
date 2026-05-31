package com.campus.campus_life.mapper;

import com.campus.campus_life.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserMapper {
    @Select("SELECT * FROM user WHERE username=#{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM user WHERE email=#{email}")
    User findByEmail(String email);

    @Insert("INSERT INTO user(username,email,password,role) VALUES(#{username},#{email},#{password},#{role})")
    void insert(User user);

    @Update("UPDATE user SET password=#{password} WHERE email=#{email}")
    void updatePassword(String email, String password);
}