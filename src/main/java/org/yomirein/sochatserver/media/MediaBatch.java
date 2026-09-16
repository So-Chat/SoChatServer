package org.yomirein.sochatserver.media;

import java.nio.file.Path;
import java.util.List;

public record MediaBatch(
    List<String> ids,
    Path lastPath
) {}
