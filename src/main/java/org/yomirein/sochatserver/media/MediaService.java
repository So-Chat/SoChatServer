package org.yomirein.sochatserver.media;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.yomirein.sochatserver.chats.ChatService;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.users.UserService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.yomirein.sochatserver.utils.MessageSender.sendHttp;

@AllArgsConstructor
public class MediaService {

    private final Path root = Paths.get("uploads").toAbsolutePath().normalize();
    private final MediaRepository mediaRepository;

    private final ChatService chatService;
    private final UserService userService;

    public Media getMediaFile(String uri) throws MediaException {
        if (!uri.startsWith("/media/")) {
            throw new MediaException(HttpResponseStatus.NOT_FOUND, "Not found");
        }

        String mediaId = Paths.get(uri).getFileName().toString();
        System.out.println(mediaId);
        Media media = mediaRepository.findById(mediaId).orElseThrow(() -> new MediaException(HttpResponseStatus.NOT_FOUND, "Not found"));

        String dir1 = media.getMediaId().substring(0, 2);
        String dir2 = media.getMediaId().substring(2, 4);

        String extension = "";
        int dotIndex = media.getFileName().lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = media.getFileName().substring(dotIndex);
        }

        Path requested = root.resolve(dir1, dir2, media.getMediaId() + extension).normalize();

        File file = requested.toFile();
        if (!file.exists() || !file.isFile()) {
            throw new MediaException(HttpResponseStatus.NOT_FOUND, "File not found");
        }
        media.setFile(file);

        return media;
    }


    //
    public String saveUploadedFile(String token, FileUpload fileUpload) throws MediaException, IOException {
        User user = userService.getUserByToken(token);
        String fileId = UUID.randomUUID().toString();

        String originalName = fileUpload.getFilename();
        String extension = FilenameUtils.getExtension(originalName);

        String newFileName = fileId + "." + extension;

        Media media = mediaRepository.save(
                fileId,
                user.getId(),
                fileUpload.getContentType(),
                originalName,
                fileUpload.length()
        );

        String dir1 = fileId.substring(0, 2);
        String dir2 = fileId.substring(2, 4);

        // media/7a/3b/7a3b...
        Path folder = root.resolve(dir1, dir2);

        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }

        File dest = folder.resolve(newFileName).toFile();

        if (!fileUpload.renameTo(dest)) {
            Files.copy(fileUpload.getFile().toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return media.getMediaId();
    }

    public void deleteMedia(String mediaId) throws MediaException {
        Optional<Media> mediaOptional = mediaRepository.findById(mediaId);

        if (mediaOptional.isEmpty()) { throw new MediaException(HttpResponseStatus.NOT_FOUND, "Not found"); }
        mediaRepository.deleteById(mediaId);

        String dir1 = mediaId.substring(0, 2);
        String dir2 = mediaId.substring(2, 4);

        // media/7a/3b/7a3b...
        Path folder = root.resolve(dir1, dir2, mediaId + "." + FilenameUtils.getExtension(mediaOptional.get().getFileName()));
        folder.toFile().delete();
    }

    public List<Media> getAllMediaFromMessage(long messageId) {
        return mediaRepository.findAttachedMessage(messageId);
    }

    public void attachMessage(String mediaId, long messageId){
        mediaRepository.update(mediaId, messageId, null, null, null);
    }

}
