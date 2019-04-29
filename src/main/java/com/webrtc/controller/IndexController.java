package com.webrtc.controller;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;


/**
 * <p>
 * IndexController
 *
 * @author 刘志强
 * @created Create Time: 2019/4/25
 */

@RestController
@RequestMapping("/web")
public class IndexController {

    /**
     * 直播页面
     * @param modelMap
     * @return
     */
    @GetMapping("webBroadcast")
    public ModelAndView webBroadcast(ModelMap modelMap){
        return new ModelAndView("/broadcast", modelMap);
    }


    /**
     * 观看页面
     * @param modelMap
     * @return
     */
    @GetMapping("webWatch")
    public ModelAndView webWatch(ModelMap modelMap){
        return new ModelAndView("/watch", modelMap);
    }

}