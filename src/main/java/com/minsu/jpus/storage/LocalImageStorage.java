package com.minsu.jpus.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LocalImageStorage {

  private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
  private static final long MAX_PIXELS = 20_000_000L;

  private final Path root;

  public LocalImageStorage(
      @Value("${app.upload-dir:./uploads}") String uploadDir
  ) throws IOException {
    this.root = Path.of(uploadDir).toAbsolutePath().normalize();

    // 저장 폴더가 없으면 생성
    Files.createDirectories(root);
  }

  public record StoredImage(
      String storageKey,
      String contentType
  ) {

  }

  public StoredImage save(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "빈 이미지는 저장할 수 없습니다."
      );
    }

    if (file.getSize() > MAX_FILE_SIZE) {
      throw new ResponseStatusException(
          HttpStatus.PAYLOAD_TOO_LARGE,
          "이미지는 한 장당 5MB 이하여야 합니다."
      );
    }

    String contentType = detectContentType(file);
    String suffix = contentType.equals("image/png")
        ? ".png"
        : ".jpg";

    Path target = null;

    try {
      // 원본 파일명 대신 서버가 만든 고유한 이름 사용
      target = Files.createTempFile(root, "image-", suffix);

      try (InputStream input = file.getInputStream()) {
        Files.copy(
            input,
            target,
            StandardCopyOption.REPLACE_EXISTING
        );
      }

      return new StoredImage(
          target.getFileName().toString(),
          contentType
      );
    } catch (IOException e) {
      // 저장 도중 실패했다면 불완전한 파일 정리
      if (target != null) {
        try {
          Files.deleteIfExists(target);
        } catch (IOException cleanupError) {
          e.addSuppressed(cleanupError);
        }
      }

      throw new UncheckedIOException("이미지 저장 실패", e);
    }
  }

  private String detectContentType(MultipartFile file) {
    try (
        InputStream input = file.getInputStream();
        ImageInputStream imageInput =
            ImageIO.createImageInputStream(input)
    ) {
      if (imageInput == null) {
        throw invalidImage();
      }

      Iterator<ImageReader> readers =
          ImageIO.getImageReaders(imageInput);

      if (!readers.hasNext()) {
        throw invalidImage();
      }

      ImageReader reader = readers.next();

      try {
        reader.setInput(imageInput);

        String contentType = switch (
            reader.getFormatName().toLowerCase(Locale.ROOT)
            ) {
          case "jpeg", "jpg" -> "image/jpeg";
          case "png" -> "image/png";
          default -> throw invalidImage();
        };

        long width = reader.getWidth(0);
        long height = reader.getHeight(0);

        if (width <= 0 || height <= 0
            || width * height > MAX_PIXELS) {
          throw invalidImage();
        }

        // 실제 이미지 내용까지 읽을 수 있는지 확인
        reader.read(0);

        return contentType;
      } finally {
        reader.dispose();
      }
    } catch (IOException e) {
      throw invalidImage();
    }
  }

  public Resource load(String storageKey) {
    Path path = resolve(storageKey);

    if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "이미지가 없습니다."
      );
    }

    return new FileSystemResource(path);
  }

  public void delete(String storageKey) {
    try {
      Files.deleteIfExists(resolve(storageKey));
    } catch (IOException e) {
      throw new UncheckedIOException("이미지 삭제 실패", e);
    }
  }

  private Path resolve(String storageKey) {
    if (storageKey == null || storageKey.isBlank()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    Path path = root.resolve(storageKey).normalize();

    // 저장 폴더 밖의 경로 접근 방지
    if (!path.startsWith(root) || path.equals(root)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    return path;
  }

  private ResponseStatusException invalidImage() {
    return new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "2000만 픽셀 이하의 JPG·PNG 이미지만 가능합니다."
    );
  }
}