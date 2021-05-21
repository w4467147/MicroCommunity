package com.java110.dto.fee;

import com.java110.dto.PageDto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @ClassName FloorDto
 * @Description 费用属性数据层封装
 * @Author wuxw
 * @Date 2019/4/24 8:52
 * @Version 1.0
 * add by wuxw 2019/4/24
 **/
public class FeeAttrDto extends PageDto implements Serializable {

    public static final String SPEC_CD_REPAIR = "390001"; // 报修单ID

    public static final String SPEC_CD_IMPORT_FEE_NAME = "390002";//导入费用名称

    public static final String SPEC_CD_SHARE_DEGREES = "390003";//公摊用量
    public static final String SPEC_CD_TOTAL_DEGREES = "390004";//公摊总用量
    public static final String SPEC_CD_SHARE_FORMULA = "390005";//公摊公式
    public static final String SPEC_CD_PROXY_CONSUMPTION = "390006";//用量
    public static final String SPEC_CD_ONCE_FEE_DEADLINE_TIME = "390010";// 截止时间
    public static final String SPEC_CD_OWNER_ID = "390007";//业主ID
    public static final String SPEC_CD_OWNER_NAME = "390008";//业主名称
    public static final String SPEC_CD_OWNER_LINK = "390009";//业主联系方式

    private String attrId;
    private String specCd;
    private String specCdName;
    private String communityId;
    private String feeId;
    private String value;

    public static String getFeeAttrValue(FeeDto feeDto, String specCd) {
        List<FeeAttrDto> feeAttrDtos = feeDto.getFeeAttrDtos();
        FeeAttrDto feeAttrDto = getFeeAttr(feeAttrDtos, specCd);
        if (feeAttrDto == null) {
            return "";
        }
        return feeAttrDto.getValue();
    }

    public static FeeAttrDto getFeeAttr(FeeDto feeDto, String specCd) {
        List<FeeAttrDto> feeAttrDtos = feeDto.getFeeAttrDtos();
        return getFeeAttr(feeAttrDtos, specCd);
    }

    public static FeeAttrDto getFeeAttr(List<FeeAttrDto> feeAttrDtos, String specCd) {
        if (feeAttrDtos == null || feeAttrDtos.size() < 1) {
            return null;
        }
        for (FeeAttrDto feeAttrDto : feeAttrDtos) {
            if (specCd.equals(feeAttrDto.getSpecCd())) {
                return feeAttrDto;
            }
        }
        return null;
    }


    private Date createTime;

    private String statusCd = "0";


    public String getAttrId() {
        return attrId;
    }

    public void setAttrId(String attrId) {
        this.attrId = attrId;
    }

    public String getSpecCd() {
        return specCd;
    }

    public void setSpecCd(String specCd) {
        this.specCd = specCd;
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    public String getFeeId() {
        return feeId;
    }

    public void setFeeId(String feeId) {
        this.feeId = feeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public String getSpecCdName() {
        return specCdName;
    }

    public void setSpecCdName(String specCdName) {
        this.specCdName = specCdName;
    }


}
