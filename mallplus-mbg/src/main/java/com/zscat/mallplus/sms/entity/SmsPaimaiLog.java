package com.zscat.mallplus.sms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author zscat
 * @since 2019-10-15
 */
@Data
@TableName("sms_paimai_log")
public class SmsPaimaiLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("paimai_id")
    private Long paimaiId;

    @TableField("member_id")
    private Long memberId;

    private BigDecimal price;

    private String pic;

    private String note;

    @TableField("create_time")
    private Date createTime;

    @TableField("goods_id")
    private Long goodsId;



}
