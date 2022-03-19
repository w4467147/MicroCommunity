/*
 * Copyright 2017-2020 吴学文 and java110 team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.java110.community.cmd.inspectionItemTitle;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.AbstractServiceCmdListener;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.inspectionItemTitle.InspectionItemTitleDto;
import com.java110.dto.inspectionItemTitleValue.InspectionItemTitleValueDto;
import com.java110.intf.community.IInspectionItemTitleV1InnerServiceSMO;
import com.java110.intf.community.IInspectionItemTitleValueV1InnerServiceSMO;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.vo.ResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;


/**
 * 类表述：查询
 * 服务编码：inspectionItemTitle.listInspectionItemTitle
 * 请求路劲：/app/inspectionItemTitle.ListInspectionItemTitle
 * add by 吴学文 at 2022-03-15 11:07:38 mail: 928255095@qq.com
 * open source address: https://gitee.com/wuxw7/MicroCommunity
 * 官网：http://www.homecommunity.cn
 * 温馨提示：如果您对此文件进行修改 请不要删除原有作者及注释信息，请补充您的 修改的原因以及联系邮箱如下
 * // modify by 张三 at 2021-09-12 第10行在某种场景下存在某种bug 需要修复，注释10至20行 加入 20行至30行
 */
@Java110Cmd(serviceCode = "inspectionItemTitle.listInspectionItemTitle")
public class ListInspectionItemTitleCmd extends AbstractServiceCmdListener {

    private static Logger logger = LoggerFactory.getLogger(ListInspectionItemTitleCmd.class);
    @Autowired
    private IInspectionItemTitleV1InnerServiceSMO inspectionItemTitleV1InnerServiceSMOImpl;

    @Autowired
    private IInspectionItemTitleValueV1InnerServiceSMO inspectionItemTitleValueV1InnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) {
        super.validatePageInfo(reqJson);
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {

        InspectionItemTitleDto inspectionItemTitleDto = BeanConvertUtil.covertBean(reqJson, InspectionItemTitleDto.class);

        int count = inspectionItemTitleV1InnerServiceSMOImpl.queryInspectionItemTitlesCount(inspectionItemTitleDto);

        List<InspectionItemTitleDto> inspectionItemTitleDtos = null;

        if (count > 0) {
            inspectionItemTitleDtos = inspectionItemTitleV1InnerServiceSMOImpl.queryInspectionItemTitles(inspectionItemTitleDto);
            refreshTitileValues(inspectionItemTitleDtos);
        } else {
            inspectionItemTitleDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) reqJson.getInteger("row")), count, inspectionItemTitleDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        cmdDataFlowContext.setResponseEntity(responseEntity);
    }

    private void refreshTitileValues(List<InspectionItemTitleDto> inspectionItemTitleDtos) {

        if (inspectionItemTitleDtos == null || inspectionItemTitleDtos.size() < 1) {
            return;
        }

        List<String> titleIds = new ArrayList<>();
        for (InspectionItemTitleDto inspectionItemTitleDto : inspectionItemTitleDtos) {
            titleIds.add(inspectionItemTitleDto.getTitleId());
        }

        InspectionItemTitleValueDto inspectionItemTitleValueDto = new InspectionItemTitleValueDto();
        inspectionItemTitleValueDto.setTitleIds(titleIds.toArray(new String[titleIds.size()]));
        inspectionItemTitleValueDto.setCommunityId(inspectionItemTitleDtos.get(0).getCommunityId());
        List<InspectionItemTitleValueDto> inspectionItemTitleValueDtos
                = inspectionItemTitleValueV1InnerServiceSMOImpl.queryInspectionItemTitleValues(inspectionItemTitleValueDto);

        List<InspectionItemTitleValueDto> tmpInspectionItemTitleValueDtos = null;
        for (InspectionItemTitleDto tmpInspectionItemTitleDto : inspectionItemTitleDtos) {
            tmpInspectionItemTitleValueDtos = new ArrayList<>();
            for (InspectionItemTitleValueDto inspectionItemTitleValueDto1 : inspectionItemTitleValueDtos) {
                if (tmpInspectionItemTitleDto.getTitleId().equals(inspectionItemTitleValueDto1.getTitleId())) {
                    tmpInspectionItemTitleValueDtos.add(inspectionItemTitleValueDto1);
                }
            }
            tmpInspectionItemTitleDto.setInspectionItemTitleValueDtos(tmpInspectionItemTitleValueDtos);
        }


    }
}
