package ai_service.controller;

import ai_service.dto.ChatRequestDto;
import ai_service.service.FileStorageService;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@AllArgsConstructor
public class AIController {
    private final FileStorageService fileStorageService;
    private final ChatModel chatModel;




    @PostMapping("/process")
    public ResponseEntity<Map<String,String>> processReport(@RequestParam("File") MultipartFile file){
        try{
            String content = fileStorageService.extractText(file);
            return ResponseEntity.ok(Map.of("content", content));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String,String>> chatWithAI(@RequestBody ChatRequestDto requestDto){
        try{
            String context = requestDto.context();
            int maxChars = 18000;
            if (context != null && context.length() > maxChars) {
                context = context.substring(0, maxChars) + "\n\n[... context truncated ...]";
            }

            String systemMessageText = """
            You are Taka, the virtual assistant for KIZUNA INDUSTRIAL MANAGEMENT SYSTEM.
            Answer ONLY in English. Be professional, clear, and concise.

            RESPONSE STRUCTURE (use Markdown):
            1. **Summary** — one short line when the answer has multiple sections.
            2. ## Section headings — one topic per section (e.g. Findings, Analysis, Risks).
            3. Bullet lists — for steps, issues, or recommendations.
            4. Markdown tables — for metrics, comparisons, or inventory-style data.
            5. **Recommended actions** — optional closing bullets when advice applies.

            RULES:
            - Do not use bracket tags, roleplay jargon, or fake system codes (e.g. [SYSTEM], CMD >>).
            - Do not mix languages.
            - If context is empty or insufficient, state what data you need.
            - Keep paragraphs short (2–4 sentences max).

            REPORT CONTEXT:
            """ + (context != null ? context : "No report attached.");

            SystemMessage systemMessage = new SystemMessage(systemMessageText);
            UserMessage userMessage = new UserMessage(requestDto.question());

            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

            String response = chatModel.call(prompt).getResult().getOutput().getText();

            return ResponseEntity.ok(Map.of("answer", response));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

}
