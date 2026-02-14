package com.github.kyanbrix.restapi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MemberResource {

    public static class Clan {
        public String identity_guild_id;
        public boolean identity_enabled;
        public String tag;
        public String badge;
    }

    public static class PrimaryGuild{
        public String identity_guild_id;
        public boolean identity_enabled;
        public String tag;
        public String badge;
    }

    public static class User {
        public String id;
        public String username;
        public String avatar;
        public String discriminator;
        public int public_flags;
        public int flags;
        public Object banner;
        public int accent_color;
        public String global_name;
        public Object avatar_decoration_data;
        public Object collectibles;
        public Object display_name_styles;
        public String banner_color;
        public Clan clan;
        public PrimaryGuild primary_guild;


    }

    public Object avatar;
    public Object banner;
    public Object communication_disabled_until;
    public int flags;
    public Date joined_at;
    public Object nick;
    public boolean pending;
    public Object premium_since;
    public ArrayList<String> roles;
    public Object unusual_dm_activity_until;
    public Object collectibles;
    public Object display_name_styles;
    public User user;
    public boolean mute;
    public boolean deaf;
}
