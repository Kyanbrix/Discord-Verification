package com.github.kyanbrix.restapi.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quote")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Quotes {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String quote;



}
