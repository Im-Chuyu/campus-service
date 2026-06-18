package com.twilight.campus.controller;

import com.twilight.campus.dto.MessageQueryDTO;
import com.twilight.campus.dto.MessageSendDTO;
import com.twilight.campus.dto.UserFeedbackDTO;
import com.twilight.campus.pojo.Message;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.MessageService;
import com.twilight.campus.vo.PageResultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public Result<?> send(@RequestBody @Valid MessageSendDTO dto) {
        messageService.send(dto);
        return Result.success("消息发送成功", null);
    }

    @PostMapping("/feedback")
    public Result<?> feedback(@RequestBody @Valid UserFeedbackDTO dto) {
        messageService.submitFeedback(dto);
        return Result.success("反馈已提交", null);
    }

    @GetMapping("/myList")
    public Result<List<Message>> myList() {
        return Result.success(messageService.myList());
    }

    @GetMapping("/unreadCount")
    public Result<Integer> unreadCount() {
        return Result.success(messageService.unreadCount());
    }

    @PutMapping("/markAllRead")
    public Result<?> markAllRead() {
        int updated = messageService.markAllRead();
        return Result.success("已全部标记为已读", updated);
    }

    @PutMapping("/markRead/{id}")
    public Result<?> markRead(@PathVariable Long id) {
        messageService.markRead(id);
        return Result.success("已标记为已读", null);
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteById(@PathVariable Long id) {
        messageService.deleteById(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/list")
    public Result<List<Message>> list(MessageQueryDTO query) {
        return Result.success(messageService.list(query));
    }

    @GetMapping("/page")
    public Result<PageResultVO<Message>> page(MessageQueryDTO query) {
        return Result.success(messageService.page(query));
    }
}
