package com.example.boardpjt.controller;

import com.example.boardpjt.model.entity.UserAccount;
import com.example.boardpjt.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follow")
public class FollowApiController {
    private final FollowService followService;

    // POST /api/follow/{userId}
    @PostMapping("/{userId}")
    public void follow(@PathVariable Long userId,
                                 Authentication authentication) {
        followService.followUser(authentication.getName(), userId);
    }

    // DELETE /api/follow/{userId}
    @DeleteMapping("/{userId}")
    public void unfollow(@PathVariable Long userId,
                       Authentication authentication) {
        followService.unfollowUser(authentication.getName(), userId);
    }

    @GetMapping("/{userId}/followingCount")
    public int followingCount(@PathVariable Long userId) {
        return followService.getFollowingCount(userId);
    }

    @GetMapping("/{userId}/followerCount")
    public int followerCount(@PathVariable Long userId) {
        return followService.getFollowerCount(userId);
    }

    @GetMapping("/{userId}/followers")
    public List<Map<String, Object>> getFollowers(@PathVariable Long userId) {
        Set<UserAccount> followers = followService.getFollowers(userId);
        return followers.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    return userMap;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{userId}/following")
    public List<Map<String, Object>> getFollowing(@PathVariable Long userId) {
        Set<UserAccount> following = followService.getFollowing(userId);
        return following.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    return userMap;
                })
                .collect(Collectors.toList());
    }
}
