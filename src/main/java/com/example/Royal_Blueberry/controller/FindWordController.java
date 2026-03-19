package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.WordDetailDto;
import com.example.Royal_Blueberry.entity.EmbedWordVector;
import com.example.Royal_Blueberry.service.EmbedWordService;
import com.example.Royal_Blueberry.service.FindWordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/searching")
@Slf4j
public class FindWordController {
    private final FindWordService findWordService ;
    @GetMapping("get-detail/{word}")
    public ResponseEntity<WordDetailDto> findWord(@PathVariable("word") String word)
    {
        log.info("have a request form client");
        WordDetailDto dto = findWordService.findWord(word.trim());
        return ResponseEntity.ok(dto);
    }
}
