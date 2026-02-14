package com.github.kyanbrix.restapi;


import java.util.List;

public class GuildResource {

    public static class Role {
        public String id;
        public String name;
        public String color;
    }

    public static class Emoji {
        public String id;
        public String name;
    }

    public String id;
    public String name;
    public String owner_id;
    public List<Role> roles;
    public List<Emoji> emojis;
}
