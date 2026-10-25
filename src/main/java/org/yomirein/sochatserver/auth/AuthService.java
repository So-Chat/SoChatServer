package org.yomirein.sochatserver.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import org.yomirein.sochatserver.common.managers.ChallengeManager;
import org.yomirein.sochatserver.common.models.Challenge;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.persistance.api.repositories.UserRepository;
import org.yomirein.sochatserver.utils.JwtService;
import org.yomirein.sochatserver.utils.JwtType;
import org.yomirein.sochatserver.utils.KeyParser;

public class AuthService {

    private final ChallengeManager challengeManager;
    private final UserRepository userRepository;

    public AuthService(ChallengeManager challengeManager, UserRepository userRepository) {
        this.challengeManager = challengeManager;
        this.userRepository = userRepository;

        challengeManager.startCleanupThread();
    }

    public Challenge createChallenge(String username) {

        Optional<User> userCheck = userRepository.findByName(username);

        if (userCheck.isPresent()) {
            User user = userCheck.get();

            Challenge challenge = challengeManager.generateChallenge(user.getId());

            return challenge;
        }
        else{
            throw new RuntimeException("User is not valid");
        }
    }

    public String login(String username, String signature, String challenge) {

        Optional<User> userCheck = userRepository.findByName(username);

        if (userCheck.isPresent()) {

            User user = userCheck.get();

            if (!challengeManager.checkChallenge(challengeManager.getChallenges().get(user.getId()))){
                throw new RuntimeException("Challenge is not valid");
            }

            var decodedPublicKey = Base64.getDecoder().decode(KeyParser.convertPublicKeyToString(user.getEd25519PublicKey()));
            try {
                if (!challengeManager.verifyChallenge(
                        challenge.getBytes(StandardCharsets.UTF_8),
                        Base64.getDecoder().decode(signature), decodedPublicKey)) {
                    throw new RuntimeException("Challenge is not verified");
                }
                else{
                    return JwtService.generateToken(user.getUsername(), JwtType.AUTH, 60 * 24);
                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        else  {
            throw new RuntimeException("User is not valid");
        }

    }

    // TODO: MAKE CHECK AUTHENTICATION FOR AUTHENTICATED ONLINE USERS
    public void checkAuthentication(String token){
        Optional<User> user = userRepository.findByName(JwtService.extractUsername(token));

        if (user.isEmpty()){
            return;
        }

        if (JwtService.isTokenValid(token)){

        }
        else {

        }

    }

    public User register(String username, String ed25519PublicKey, String x25519PublicKey) {

        Optional<User> userCheck = userRepository.findByName(username);
        if (userCheck.isPresent()) {
            throw new RuntimeException("User already exists");
        }

        try {
            User user = new User(username,
                    KeyParser.stringToPublicKeyED25519(ed25519PublicKey),
                    KeyParser.stringToPublicKeyX25519(x25519PublicKey));
            return userRepository.saveUser(user);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
