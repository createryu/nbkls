package com.yuqiliu.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.yuqiliu.annotation.LoginRequired;
import com.yuqiliu.entity.Comment;
import com.yuqiliu.entity.DiscussPost;
import com.yuqiliu.entity.Page;
import com.yuqiliu.entity.User;
import com.yuqiliu.service.*;
import com.yuqiliu.util.CommunityConstant;
import com.yuqiliu.util.CommunityUtil;
import com.yuqiliu.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yuqiliu
 * @create 2020-05-19  1:02
 */

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController implements CommunityConstant {

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${quniu.bucket.header.url}")
    private String headerBucketUrl;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;


    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(Model model)
    {
        // 上传文件名称
        String fileName = CommunityUtil.generateUUID();
        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody",CommunityUtil.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey,secretKey);
        String uploadToken = auth.uploadToken(headerBucketName,fileName,3600,policy);

        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);

        return "/site/setting";
    }

    // 更新头像路径
    @PostMapping("/header/url")
    @ResponseBody
    public String updateHeaderUrl(String fileName)
    {
        if (StringUtils.isBlank(fileName))
        {
            return CommunityUtil.getJSONString(1,"文件名不能为空!");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(),url);

        return CommunityUtil.getJSONString(0);
    }

    // 废弃
//    @LoginRequired
//    @PostMapping("/upload")
//    public String uploadHeader(MultipartFile headerImage, Model model)
//    {
//        if (headerImage == null)
//        {
//            model.addAttribute("error","您还没有选择图片!");
//            return "/site/setting";
//        }
//
//        String fileName = headerImage.getOriginalFilename();
//        String suffix = fileName.substring(fileName.lastIndexOf("."));
//        if (StringUtils.isBlank(suffix))
//        {
//            model.addAttribute("error","文件的格式不正确!");
//            return "/site/setting";
//        }
//
//        // 生成随机文件名
//        fileName = CommunityUtil.generateUUID() + suffix;
//        // 确定文件的存储路径
//        File dest = new File(uploadPath);
//        if (!dest.exists()){
//            dest.mkdirs();
//        }
//        try {
//            // 存储文件
//            headerImage.transferTo(new File(dest +"/"+ fileName));
//        } catch (IOException e) {
//            log.error("上传文件失败: " + e.getMessage());
//            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
//        }
//
//        // 更新当前用户的头像的路径（web访问路径）
//        // http://localhost:8080/community/user/header/xxx.png
//        User user = hostHolder.getUser();
//        String headerUrl = domain + contextPath + "/user/header/" + fileName;
//        userService.updateHeader(user.getId(),headerUrl);
//
//        return "redirect:/index";
//    }

    // 废弃
//    @GetMapping("/header/{fileName}")
//    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response)
//    {
//        // 图片在服务器上的存放路径
//        fileName = uploadPath + "/" +fileName;
//        // 文件后缀
//        String suffix = fileName.substring(fileName.lastIndexOf("."));
//        // 相应图片
//        response.setContentType("image/" + suffix);
//        try (
//                FileInputStream fis = new FileInputStream(fileName);
//                OutputStream os = response.getOutputStream();
//        )
//        {
//            byte[] buffer = new byte[1024];
//            int b = 0;
//            while ((b = fis.read(buffer)) != -1) {
//                os.write(buffer, 0, b);
//            }
//        } catch (IOException e) {
//            log.error("读取头像失败: " + e.getMessage());
//        }
//    }


    @LoginRequired
    @GetMapping("/updatepassword")
    public String updatePassword(String oldpassword,String newpassword,Model model)
    {
        User user = hostHolder.getUser();
        if (StringUtils.isBlank(oldpassword))
        {
            model.addAttribute("oldpasswordMsg","您还没有输入原密码!");
            return "/site/setting";
        }
        if (StringUtils.isBlank(newpassword))
        {
            model.addAttribute("newpasswordMsg","您还没有输入新密码!");
            return "/site/setting";
        }
        // 原始密码输入不对不行
        if (!user.getPassword().equals(CommunityUtil.md5(oldpassword + user.getSalt())))
        {
            model.addAttribute("oldpasswordMsg","您输入的原密码不正确!");
            return "/site/setting";
        }

        userService.updatePassword(user.getId(),CommunityUtil.md5(newpassword + user.getSalt()));

        return "redirect:/index";
    }


    // 个人主页
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId,Model model)
    {
        User user = userService.findUserById(userId);
        if (user == null)
        {
            throw new RuntimeException("该用户不存在!");
        }

        // 用户
        model.addAttribute("user",user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);


        return "/site/profile";
    }


    // 我的帖子
    @GetMapping("/myPost/{userId}")
    public String getMyDiscussPostPage(@PathVariable("userId") int userId, Model model, Page page)
    {
        page.setPath("/user/myPost/" + userId);
        page.setRows(discussPostService.findDiscussPostRows(userId));
        List<DiscussPost> lists = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(), 0);
        List<Map<String,Object>> discussPosts=new ArrayList<>();
        User user = userService.findUserById(userId);

        if (lists != null)
        {
            for (DiscussPost post:lists)
            {
                Map<String,Object> map=new HashMap<>();
                map.put("post",post);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }

        model.addAttribute("user",user);
        model.addAttribute("discussPosts",discussPosts);

        return "/site/my-post";
    }

    // 我的回复
    @GetMapping("/myReply/{userId}")
    public String getMyReplyPage(@PathVariable("userId") int userId, Model model, Page page)
    {
        page.setPath("/user/myReply/" + userId);
        page.setRows(commentService.selectReplyRows(userId));
        List<Comment> lists = commentService.getMyReply(userId,page.getOffset(),page.getLimit());
        List<Map<String,Object>> replys=new ArrayList<>();
        User user = userService.findUserById(userId);

        if (lists != null)
        {
            for (Comment comment : lists)
            {
                Map<String,Object> map=new HashMap<>();
                DiscussPost discussPost = discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("reply",comment);
                map.put("post",discussPost);

                replys.add(map);
            }
        }

        model.addAttribute("user",user);
        model.addAttribute("replys",replys);
        return "/site/my-reply";
    }


}
