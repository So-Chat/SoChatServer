package org.yomirein.sochatserver.friendship;

import lombok.RequiredArgsConstructor;
import org.postgresql.util.PSQLException;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.common.repos.TrustKeysRepository;
import org.yomirein.sochatserver.users.UserRepository;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final TrustKeysRepository trustKeysRepository;

    public Friendship sendRequest(Long fromUserId, Long toUserId, String fingerprint) {
        try {
            User from = userRepository.findById(fromUserId).orElseThrow(() -> new RuntimeException("User not found"));
            User to = userRepository.findById(toUserId).orElseThrow(() -> new RuntimeException("User not found"));

            Optional<Friendship> existing = friendshipRepository.findByUserAndFriend(from, to);
            if (existing.isPresent()) return existing.get();

            Friendship f = new Friendship();
            f.setUser(from);
            f.setFriend(to);
            f.setStatus(FriendshipStatus.PENDING);

            trustKeysRepository.addKeyToUser(toUserId, fromUserId, fingerprint);

            return friendshipRepository.saveOrUpdate(f);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }



    public boolean declineRequest(Long requestId) {
        try {
            Optional<Friendship> opt = friendshipRepository.findById(requestId);
            if (opt.isEmpty()) {
                throw new RuntimeException("Friendship request not found: " + requestId);
            }
            Friendship f = opt.get();

            trustKeysRepository.removeKeyFromUser(f.getFriend().getId(), f.getUser().getId());

            return friendshipRepository.deleteById(f.getId());
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean removeFriendship(Long requestId) {
        try {
            Optional<Friendship> opt = friendshipRepository.findById(requestId);
            if (opt.isEmpty()) {
                throw new RuntimeException("Friendship request not found: " + requestId);
            }
            Friendship f = opt.get();

            trustKeysRepository.removeKeyFromUser(f.getFriend().getId(), f.getUser().getId());
            trustKeysRepository.removeKeyFromUser(f.getUser().getId(), f.getFriend().getId());

            return friendshipRepository.deleteById(f.getId());
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public Friendship acceptRequest(Long requestId, String fingerprint){
        try{
            Optional<Friendship> opt = friendshipRepository.findById(requestId);

            if (opt.isEmpty()) {
                throw new RuntimeException("Friendship request not found: " + requestId);
            }

            Friendship f = opt.get();
            f.setStatus(FriendshipStatus.ACCEPTED);

            trustKeysRepository.addKeyToUser(f.getUser().getId(), f.getFriend().getId(), fingerprint);

            return friendshipRepository.saveOrUpdate(f);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public Friendship block(long userId, long blockedId){
        try{
            User from = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("user not found"));
            User to = userRepository.findById(blockedId).orElseThrow(() -> new RuntimeException("user not found"));

            Friendship f = new Friendship();
            f.setUser(from);
            f.setFriend(to);
            f.setStatus(FriendshipStatus.BLOCKED);

            trustKeysRepository.removeKeyFromUser(f.getUser().getId(), f.getFriend().getId());
            trustKeysRepository.removeKeyFromUser(f.getFriend().getId(), f.getUser().getId());

            return friendshipRepository.saveOrUpdate(f);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Friendship> listByUser(Long userId) {
        User u = userRepository.findById(userId).orElseThrow();
        return friendshipRepository.findByUserOrFriend(u, u);
    }

    private String extractSqlState(Throwable e) {

        while (e != null) {

            if (e instanceof PSQLException psql)
                return psql.getSQLState();

            e = e.getCause();
        }

        return null;
    }

    private FriendshipException mapDbError(Throwable e){
        String state = extractSqlState(e);

        if (state == null)
            return new FriendshipException(FriendshipErrorCode.DB_ERROR);

        return switch (state){
            case "23505" -> new FriendshipException(FriendshipErrorCode.ALREADY_FRIENDS);
            default -> new FriendshipException(FriendshipErrorCode.DB_ERROR);
        };
    }

}
