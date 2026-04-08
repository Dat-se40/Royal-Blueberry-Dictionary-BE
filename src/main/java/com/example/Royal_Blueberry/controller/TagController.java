package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.entity.Tag;
import com.example.Royal_Blueberry.entity.WordTagRelation;
import com.example.Royal_Blueberry.service.impl.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    // Lấy userId từ JWT context
    private String getUserId(Principal principal) {
        return principal.getName(); // Hoặc logic extract ID từ JWT của bạn
    }

    // 1. Lấy danh sách tag của user
    @GetMapping
    public ResponseEntity<List<Tag>> getMyTags(Principal principal) {
        return ResponseEntity.ok(tagService.getAllTagsByUser(getUserId(principal)));
    }

    // 2. Tạo hoặc cập nhật tag
    @PostMapping
    public ResponseEntity<Tag> saveTag(@RequestBody Tag tag, Principal principal) {
        return ResponseEntity.ok(tagService.createOrUpdateTag(tag, getUserId(principal)));
    }

    // 3. Xóa tag
    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable String tagId, Principal principal) {
        tagService.deleteTag(tagId, getUserId(principal));
        return ResponseEntity.noContent().build();
    }

    // 4. Gắn tag cho 1 từ
    @PostMapping("/link")
    public ResponseEntity<WordTagRelation> linkWordToTag(@RequestBody WordTagRelation relation, Principal principal) {
        return ResponseEntity.ok(tagService.linkWordToTag(relation, getUserId(principal)));
    }

    // 5. Bỏ tag khỏi 1 từ
    @DeleteMapping("/unlink")
    public ResponseEntity<Void> unlinkWordFromTag(
            @RequestParam String tagId,
            @RequestParam String word,
            @RequestParam Integer meaningIndex,
            Principal principal) {
        tagService.unlinkWordFromTag(tagId, word, meaningIndex, getUserId(principal));
        return ResponseEntity.noContent().build();
    }

    // 6. Xem một từ đang có những tag nào
    @GetMapping("/word/{word}")
    public ResponseEntity<List<WordTagRelation>> getTagsOfWord(@PathVariable String word, Principal principal) {
        return ResponseEntity.ok(tagService.getRelationsByWord(word, getUserId(principal)));
    }
}