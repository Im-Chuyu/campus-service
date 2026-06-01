package com.twilight.campus.controller;

import com.twilight.campus.dto.MessageQueryDTO;
import com.twilight.campus.dto.MessageSendDTO;
import com.twilight.campus.pojo.Message;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 发送消息（管理员）
     */
    @PostMapping("/send")
    public Result<?> send(@RequestBody @Valid MessageSendDTO dto) {
        messageService.send(dto);
        return Result.success("消息发送成功", null);
    }

    /**
     * 我的消息列表
     */
    @GetMapping("/myList")
    public Result<List<Message>> myList() {
        return Result.success(messageService.myList());
    }

    /**
     * 未读消息数
     */
    @GetMapping("/unreadCount")
    public Result<Integer> unreadCount() {
        return Result.success(messageService.unreadCount());
    }

    /**
     * 标记已读
     */
    @PutMapping("/markRead/{id}")
    public Result<?> markRead(@PathVariable Long id) {
        messageService.markRead(id);
        return Result.success("已标记已读", null);
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteById(@PathVariable Long id) {
        messageService.deleteById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 条件查询消息（管理员）
     */
    @GetMapping("/list")
    public Result<List<Message>> list(MessageQueryDTO query) {
        return Result.success(messageService.list(query));
    }
}
