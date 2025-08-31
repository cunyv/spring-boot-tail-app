package top.allhere.logmonitoring.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class LogController {

    /**
     * 获取文件的最后几行
     * @param filePath 文件路径
     * @param lines 行数
     * @return 文件的最后几行
     */
    @GetMapping("/tail")
    public ResponseEntity<String> tailFile(@RequestParam String filePath, @RequestParam(defaultValue = "10") int lines) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return ResponseEntity.badRequest().body("File not found: " + filePath);
            }

            // 读取文件的最后N行
            List<String> fileLines = Files.readAllLines(path);
            int totalLines = fileLines.size();
            int startLine = Math.max(0, totalLines - lines);
            
            StringBuilder result = new StringBuilder();
            for (int i = startLine; i < totalLines; i++) {
                result.append(fileLines.get(i)).append("\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error reading file: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     * @param filePath 文件路径
     * @return 文件内容
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }
            
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(path));
            String filename = path.getFileName().toString();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(Files.size(path))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}