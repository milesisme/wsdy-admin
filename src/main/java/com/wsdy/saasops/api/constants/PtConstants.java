package com.wsdy.saasops.api.constants;

public class PtConstants {
   //public static final String LUCKYJACKPOT_URL="http://tickers.playtech.com/jackpots/new_jackpotxml.php?info=4&type=sum&currency=CNY";
	//public static final String LUCKYJACKPOT_URL="http://tickers.playtech.com/jackpots/new_jackpotxml.php?info=4&currency=cny&casino=greatfortune88";
	//public static final String ENTITY_KEY_NAME="X_ENTITY_KEY";
	public static final String PLAY_CREATED="New player has been created";
    public static final String DEPOSIT_OK="deposit ok";
    public static final String WITHDRAW_OK="withdraw ok";
    public static final String TRANSACTION_SC="approved";//成功
    public static final String TRANSACTION_FAIL="missing";//失败
    public static final String LOGOUT="has been successfully sent";
    public static final String RESETFAILEDLOGIN="has been reset";

	public interface SslEntity
    {
        String KeyStore="PKCS12";
        String keyFilePath="/key/VBETCNYTLE.p12";
        String keyPwd="UK8eXSQAGFkBZlo3";
        String KeyManager="SunX509";
        String tls="TLS";
    }
    public interface mod{
	    String createMember="/player/create";//建立用户
        String deposit="player/deposit";//存款
        String withdraw="player/withdraw";//取款
        String logout="player/logout";//登出
        String online="player/online";//在线
        String info="player/info";//会员信息 余额
        String checktransaction="player/checktransaction";//查询转账状态
        String resetfailedlogin="player/resetfailedlogin";//登录失败次数清零
        String[] frozen = new String[]{"player/update/playername", "1"}; // 锁用户
        String unFrozen = "player/unfreeze/playername";
    }
    public interface  JsonKey
    {
        String ENTITY_KEY_NAME="X_ENTITY_KEY";
        String JAVASCRIPT_URL="scriptUrl";
    }
    public interface Online
    {
        Integer yes=1;//在线
        Integer no=0;//离线
    }
    public interface RouteParam
    {
    	//String GAME_URL="http://gapi.evebcomp.com/pt/game/routing?param=";
    	String nolobby="4";
    	String language="zh-cn";
    }
}
