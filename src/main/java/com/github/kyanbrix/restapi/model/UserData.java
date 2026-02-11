package com.github.kyanbrix.restapi.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;


@Getter
@Table(name = "user_data")
@Entity
public class UserData {


    @Id
    @Column(name = "user_id")
    private String id;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_avatar")
    private String avatar;


    public UserData(){}

    public UserData(String id, String userName, String avatar) {
        this.id = id;
        this.userName = userName;
        this.avatar = avatar;
    }
}
