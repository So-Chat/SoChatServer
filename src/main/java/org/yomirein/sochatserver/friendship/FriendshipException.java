package org.yomirein.sochatserver.friendship;

public class FriendshipException extends RuntimeException {

    private final FriendshipErrorCode code;

    public FriendshipException(FriendshipErrorCode code) {
        this.code = code;
    }

    public FriendshipErrorCode getCode() {
        return code;
    }
}



