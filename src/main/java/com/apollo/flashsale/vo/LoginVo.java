package com.apollo.flashsale.vo;

import com.apollo.flashsale.validator.IsMobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class LoginVo {

    @NotNull
    @IsMobile(required = false)
    private String mobile;

    @NotNull
    @Length(min = 32)
    private String password;


}
