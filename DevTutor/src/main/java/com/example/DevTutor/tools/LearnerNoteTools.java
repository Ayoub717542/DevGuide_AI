package com.example.DevTutor.tools;

import com.example.DevTutor.entity.LearnerNote;
import com.example.DevTutor.repository.LearnerNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LearnerNoteTools {

    private final LearnerNoteRepository learnerNoteRepository;

    @Tool(description = """
            Save a short note about how the user learns, so you remember it in future conversations.
            Only save new and important facts, for example a topic they struggle with,
            a topic they already know, or how they like explanations.
            Do not save personal or private information.
            Do not save a note you already have.
            """)
    public Map<String, Object> saveLearnerNote(
            @ToolParam(description = "One short sentence, under 200 characters")
            String note,

            ToolContext toolContext
    ) {
        LearnerNote learnerNote = new LearnerNote();
        learnerNote.setNote(note.length() > 255 ? note.substring(0, 255) : note);
        learnerNote.setUserEmail((String) toolContext.getContext().get("userEmail"));
        learnerNoteRepository.save(learnerNote);

        return Map.of("success", true);
    }
}
