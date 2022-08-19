package com.wsdy.saasops.api.utils;

import com.wsdy.saasops.api.constants.ApiConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * jwt工具类
 */
@ConfigurationProperties(prefix = "wsdy.jwt")
@Component
@Getter
@Setter
@Slf4j
public class JwtUtils {

    private String secret;
    private long expire;
    private String header;
    private String secretFindPwd;
    private long expireFindPwd;

    /**
     * 生成代理jwt token,跟会员一样的密钥，需要时可以改
     */
    public String agentGenerateToken(String accountId) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + expire * 1000);
        return Jwts.builder().setHeaderParam("typ", "JWT").setSubject(String.valueOf(accountId))
                .setIssuedAt(nowDate).setExpiration(expireDate).signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    /**
     * 生成jwt token
     */
    public String generateToken(long userId, String userName) {
        Date nowDate = new Date();
        // 过期时间
        Date expireDate = new Date(nowDate.getTime() + expire * 1000);

        return Jwts.builder().setHeaderParam("typ", "JWT").setSubject(userId + ApiConstants.USER_TOKEN_SPLIT + userName)
                .setIssuedAt(nowDate).setExpiration(expireDate).signWith(SignatureAlgorithm.HS512, secret).compact();
    }


    /**
     * 生成jwt token
     */
    public String generateToken(long userId, String userName, long expireTime) {
        Date nowDate = new Date();
        // 过期时间
        Date expireDate = new Date(nowDate.getTime() + expireTime * 1000);

        return Jwts.builder().setHeaderParam("typ", "JWT").setSubject(userId + ApiConstants.USER_TOKEN_SPLIT + userName)
                .setIssuedAt(nowDate).setExpiration(expireDate).signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public Claims getClaimByToken(String token) {
        try {
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            log.info("validate is token error " + token);
            return null;
        }
    }

    /**
     * token是否过期
     *
     * @return true：过期
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    /**
     * 生成jwt token
     */
    public String generatefindPwdToken(String userName) {
        Date nowDate = new Date();
        // 过期时间
        Date expireDate = new Date(nowDate.getTime() + expireFindPwd * 1000);

        return Jwts.builder().setHeaderParam("typ", "JWT").setSubject(userName).setIssuedAt(nowDate)
                .setExpiration(expireDate).signWith(SignatureAlgorithm.HS512, secretFindPwd).compact();
    }

    public String generatefindPwdToken(String userName, String code) {
        Date nowDate = new Date();
        // 过期时间
        Date expireDate = new Date(nowDate.getTime() + expireFindPwd * 1000);

        return Jwts.builder().setHeaderParam("typ", "JWT").setSubject(code + ApiConstants.USER_TOKEN_SPLIT + userName)
                .setIssuedAt(nowDate).setExpiration(expireDate).signWith(SignatureAlgorithm.HS512, secretFindPwd)
                .compact();
    }
    
    /**
     * 	找回密码 代理生成token 
     * 
     * @param accountId
     * @return
     */
    public String agentGeneratefindPwdToken(String accountId) {
    	Date nowDate = new Date();
    	Date expireDate = new Date(nowDate.getTime() + expire * 1000);
    	return Jwts.builder().setHeaderParam("typ", "JWT").setSubject(accountId)
    			.setIssuedAt(nowDate).setExpiration(expireDate).signWith(SignatureAlgorithm.HS512, secretFindPwd).compact();
    }
    
    /**
     * 	找回密码 代理生成token
     * 
     * @param agyId
     * @param code
     * @return
     */
    public String agentGeneratefindPwdToken(String agyId, String code) {
    	Date nowDate = new Date();
    	Date expireDate = new Date(nowDate.getTime() + expire * 1000);
    	return Jwts.builder().setHeaderParam("typ", "JWT").setSubject(code + ApiConstants.USER_TOKEN_SPLIT + agyId)
    			.setIssuedAt(nowDate).setExpiration(expireDate).signWith(SignatureAlgorithm.HS512, secretFindPwd).compact();
    }


    public Claims getClaimByfindPwdToken(String token) {
        try {
            return Jwts.parser().setSigningKey(secretFindPwd).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            log.info("validate is token error ");
            return null;
        }
    }
}
