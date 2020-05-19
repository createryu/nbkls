package com.yuqiliu.controller;

import com.yuqiliu.annotation.LoginRequired;
import com.yuqiliu.dao.UserMapper;
import com.yuqiliu.entity.User;
import com.yuqiliu.service.UserService;
import com.yuqiliu.util.CommunityUtil;
import com.yuqiliu.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @author yuqiliu
 * @create 2020-05-19  1:02
 */

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;


    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage()
    {
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model)
    {
        if (headerImage == null)
        {
            model.addAttribute("error","您还没有选择图片!");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix))
        {
            model.addAttribute("error","文件的格式不正确!");
            return "/site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件的存储路径
        File dest = new File(uploadPath);
        if (!dest.exists()){
            dest.mkdirs();
        }
        try {
            // 存储文件
            headerImage.transferTo(new File(dest +"/"+ fileName));
        } catch (IOException e) {
            log.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        // 更新当前用户的头像的路径（web访问路径）
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response)
    {
        // 图片在服务器上的存放路径
        fileName = uploadPath + "/" +fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 相应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        )
        {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            log.error("读取头像失败: " + e.getMessage());
        }
    }


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

}
