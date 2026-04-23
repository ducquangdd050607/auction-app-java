package com.auctionapp.auctionappjava.client.session;

import com.auctionapp.auctionappjava.common.dto.AuthUserDto;
import java.util.Optional;
import java.util.UUID;

public class ClientSession {
    private AuthUserDto currentUser;
    public synchronized void setCurrentUser(AuthUserDto user){ this.currentUser=user; }
    public synchronized Optional<AuthUserDto> getCurrentUser(){ return Optional.ofNullable(currentUser); }
    public synchronized UUID getUserId(){ return currentUser==null?null:currentUser.id(); }
    public synchronized void clear(){ currentUser=null; }
    public synchronized boolean isLoggedIn(){ return currentUser!=null; }
}
