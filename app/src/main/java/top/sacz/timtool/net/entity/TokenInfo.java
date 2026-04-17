package top.sacz.timtool.net.entity;


import kotlinx.serialization.Serializable;

@Serializable
public class TokenInfo {

    public String tokenName;
    public String tokenValue;
    public Boolean isLogin;
    public Object loginId;
    public String loginType;
    public long tokenTimeout;
    public long sessionTimeout;
    public long tokenSessionTimeout;
    public long tokenActiveTimeout;
    public String loginDevice;
    public String tag;
}
