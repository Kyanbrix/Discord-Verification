package com.github.kyanbrix.restapi;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscordUserModel
{
    private String id;
    private String username;
    private String discriminator;
    private String email;
    private String avatar;

}
