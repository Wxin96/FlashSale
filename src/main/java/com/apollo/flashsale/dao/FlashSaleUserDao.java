package com.apollo.flashsale.dao;

import com.apollo.flashsale.domain.FlashSaleUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FlashSaleUserDao {

    @Select("select * from flashsale_user where id = #{id}")
    FlashSaleUser getById(@Param("id") long id);


    @Update("update flashsale_user set password = #{password} where id = #{id}")
    void updatePassword(FlashSaleUser user);

}
