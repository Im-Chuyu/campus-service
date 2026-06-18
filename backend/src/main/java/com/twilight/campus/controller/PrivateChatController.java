package com.twilight.campus.controller;

import com.twilight.campus.dto.PrivateMessageSendDTO;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.PrivateChatService;
import com.twilight.campus.vo.PrivateChatPeerVO;
import com.twilight.campus.vo.PrivateChatSessionVO;
import com.twilight.campus.vo.PrivateMessageVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/private-chat")
public class PrivateChatController {

    @Autowired
    private PrivateChatService privateChatService;

    @GetMapping("/peer/{userId}")
    public Result<PrivateChatPeerVO> peer(@PathVariable Long userId) {
        return Result.success(privateChatService.getPeer(userId));
    }

    @GetMapping("/sessions")
    public Result<List<PrivateChatSessionVO>> sessions() {
        return Result.success(privateChatService.sessions());
    }

    @GetMapping("/history/{userId}")
    public Result<List<PrivateMessageVO>> history(@PathVariable Long userId) {
        return Result.success(privateChatService.history(userId));
    }

    @GetMapping("/sync/{userId}")
    public Result<List<PrivateMessageVO>> sync(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "0") Long afterId) {
        return Result.success(privateChatService.sync(userId, afterId));
    }

    @GetMapping("/unreadCount")
    public Result<Integer> unreadCount() {
        return Result.success(privateChatService.unreadCount());
    }

    @PutMapping("/markAllRead")
    public Result<?> markAllRead() {
        privateChatService.markAllRead();
        return Result.success("已全部标记为已读", null);
    }

    @PutMapping("/markRead/{userId}")
    public Result<?> markRead(@PathVariable Long userId) {
        privateChatService.markRead(userId);
        return Result.success("已标记当前会话为已读", null);
    }

    @PostMapping("/send")
    public Result<?> send(@RequestBody @Valid PrivateMessageSendDTO dto) {
        privateChatService.send(dto);
        return Result.success("发送成功", null);
    }

    @PostMapping("/block/{userId}")
    public Result<?> block(@PathVariable Long userId) {
        privateChatService.block(userId);
        return Result.success("已屏蔽", null);
    }

    @DeleteMapping("/block/{userId}")
    public Result<?> unblock(@PathVariable Long userId) {
        privateChatService.unblock(userId);
        return Result.success("已取消屏蔽", null);
    }
}
