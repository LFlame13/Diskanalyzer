package com.disk.diskanalyzer;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class Analyzer {
    private HashMap<String, Long> sizes;

    public Map<String, Long> calculateDirectorySize(Path path) {
        try {
            sizes = new HashMap<>();
            Files.walkFileTree(
                    path,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            // Пропускаем системные директории
                            if (isSystemDirectory(file)) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }

                            long size = Files.size(file);
                            updateDirSize(file, size);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            // Логируем ошибку
                            System.err.println("Failed to visit file: " + file + " - " + exc.getMessage());
                            return FileVisitResult.CONTINUE; // Пропускаем файл и продолжаем
                        }
                    }
            );
            return sizes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateDirSize(Path path, Long size) {
        String key = path.toString();
        sizes.put(key, size + sizes.getOrDefault(key, 0L));

        Path parent = path.getParent();
        if (parent != null) {
            updateDirSize(parent, size);
        }
    }

    private boolean isSystemDirectory(Path path) {
        String name = path.toString();
        return name.contains("$RECYCLE.BIN") || name.contains("System Volume Information") || name.contains("WindowsApps");
    }
}
