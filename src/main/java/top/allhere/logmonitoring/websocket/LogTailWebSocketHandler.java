package top.allhere.logmonitoring.websocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogTailWebSocketHandler extends TextWebSocketHandler {
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FileTailer> tailers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String filePath = message.getPayload();
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            session.sendMessage(new TextMessage("ERROR: File not found: " + filePath));
            return;
        }

        // 如果已经有一个tailer在运行，先停止它
        if (tailers.containsKey(session.getId())) {
            tailers.get(session.getId()).stop();
        }

        // 创建新的tailer
        FileTailer tailer = new FileTailer(path.toFile(), session);
        tailers.put(session.getId(), tailer);
        
        // 开始tail文件
        scheduler.scheduleWithFixedDelay(tailer, 0, 1, TimeUnit.SECONDS);
        
        session.sendMessage(new TextMessage("STARTED: Tailing file " + filePath));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        
        // 停止并移除tailer
        FileTailer tailer = tailers.remove(session.getId());
        if (tailer != null) {
            tailer.stop();
        }
    }

    private static class FileTailer implements Runnable {
        private final java.io.File file;
        private final WebSocketSession session;
        private long lastPosition = 0;
        private volatile boolean running = true;

        public FileTailer(java.io.File file, WebSocketSession session) {
            this.file = file;
            this.session = session;
        }

        @Override
        public void run() {
            if (!running) return;
            
            try {
                long length = file.length();
                if (length < lastPosition) {
                    // 文件被截断了
                    lastPosition = 0;
                }
                
                if (length > lastPosition) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        // 跳过之前已经读取的内容
                        reader.skip(lastPosition);
                        
                        String line;
                        while ((line = reader.readLine()) != null && running) {
                            if (session.isOpen()) {
                                session.sendMessage(new TextMessage(line + "\n"));
                            }
                        }
                        
                        lastPosition = reader.ready() ? file.length() : lastPosition + reader.toString().length();
                        lastPosition = length;
                    }
                }
            } catch (Exception e) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage("ERROR: " + e.getMessage()));
                    }
                } catch (IOException ioException) {
                    // 忽略发送错误
                }
                running = false;
            }
        }

        public void stop() {
            running = false;
        }
    }
}