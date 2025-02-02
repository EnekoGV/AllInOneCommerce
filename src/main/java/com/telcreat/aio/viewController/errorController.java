package com.telcreat.aio.viewController;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
@SessionAttributes({"searchForm", "categories", "cartItemNumber"})
public class errorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/error-404";
            }
            else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error/error-500";
            }
            else if(statusCode == HttpStatus.BAD_REQUEST.value()){
                return "error/error-400";
            }
            else if(statusCode == HttpStatus.FORBIDDEN.value()){
                return "error/error-403";
            }
        }
        return "error/error-404";
    }

    @Override
    public String getErrorPath() {
        return null;
    }
}
