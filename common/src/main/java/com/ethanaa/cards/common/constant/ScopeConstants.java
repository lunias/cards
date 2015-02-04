package com.ethanaa.cards.common.constant;

public final class ScopeConstants {

	private ScopeConstants() {		
	}

	public static final String READ = "read";
	public static final String WRITE = "write";
	public static final String DELETE = "delete";
	
	public static final String OAUTH_PREFIX = "oauth.";		
	public static final String OAUTH_READ = OAUTH_PREFIX + READ;	
	public static final String OAUTH_WRITE = OAUTH_PREFIX + WRITE;
	public static final String OAUTH_DELETE = OAUTH_PREFIX + DELETE;
	
	public static final String WEB_PREFIX = "web.";
	public static final String WEB_READ = WEB_PREFIX + READ;
	public static final String WEB_WRITE = WEB_PREFIX + WRITE;
	public static final String WEB_DELETE = WEB_PREFIX + DELETE;
	
	public static final String GAME_PREFIX = "game.";
	public static final String GAME_READ = GAME_PREFIX + READ;
	public static final String GAME_WRITE = GAME_PREFIX + WRITE;
	public static final String GAME_DELETE = GAME_PREFIX + DELETE;
	
	
}
