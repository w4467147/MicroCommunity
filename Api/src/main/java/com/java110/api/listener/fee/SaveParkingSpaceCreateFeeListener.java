package com.java110.api.listener.fee;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.smo.fee.IFeeConfigInnerServiceSMO;
import com.java110.core.smo.parkingSpace.IParkingSpaceInnerServiceSMO;
import com.java110.dto.fee.FeeConfigDto;
import com.java110.dto.parking.ParkingSpaceDto;
import com.java110.entity.center.AppService;
import com.java110.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * @ClassName SaveParkingSpaceCreateFeeListener
 * @Description TODO
 * @Author wuxw
 * @Date 2020/1/31 15:57
 * @Version 1.0
 * add by wuxw 2020/1/31
 **/
@Java110Listener("saveParkingSpaceCreateFeeListener")
public class SaveParkingSpaceCreateFeeListener extends AbstractServiceApiListener {
    private static Logger logger = LoggerFactory.getLogger(SaveParkingSpaceCreateFeeListener.class);

    private static final int DEFAULT_ADD_FEE_COUNT = 200;

    @Autowired
    private IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl;

    @Autowired
    private IFeeConfigInnerServiceSMO feeConfigInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeConstant.SERVICE_CODE_SAVE_PARKING_SPEC_CREATE_FEE;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        // super.validatePageInfo(pd);
        Assert.hasKeyAndValue(reqJson, "communityId", "???????????????ID");
        Assert.hasKeyAndValue(reqJson, "locationTypeCd", "?????????????????????");
        Assert.hasKeyAndValue(reqJson, "locationObjId", "?????????????????????");
        Assert.hasKeyAndValue(reqJson, "configId", "?????????????????????");
        Assert.hasKeyAndValue(reqJson, "billType", "?????????????????????");
        Assert.hasKeyAndValue(reqJson, "storeId", "???????????????ID");
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {
        logger.debug("ServiceDataFlowEvent : {}", event);
        List<ParkingSpaceDto> parkingSpaceDtos = null;
        FeeConfigDto feeConfigDto = new FeeConfigDto();
        feeConfigDto.setCommunityId(reqJson.getString("communityId"));
        feeConfigDto.setConfigId(reqJson.getString("configId"));
        List<FeeConfigDto> feeConfigDtos = feeConfigInnerServiceSMOImpl.queryFeeConfigs(feeConfigDto);
        Assert.listOnlyOne(feeConfigDtos, "???????????????ID????????????????????????" + reqJson.getString("configId"));
        reqJson.put("feeTypeCd", feeConfigDtos.get(0).getFeeTypeCd());
        reqJson.put("feeFlag", feeConfigDtos.get(0).getFeeFlag());
        //??????????????????
        ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
        parkingSpaceDto.setState(reqJson.containsKey("parkingSpaceState") ? reqJson.getString("parkingSpaceState") : "");
        if (reqJson.containsKey("parkingSpaceState") && "SH".equals(reqJson.getString("parkingSpaceState"))) {
            parkingSpaceDto.setState("");
            parkingSpaceDto.setStates(new String[]{"S", "H"});
        }
        if ("1000".equals(reqJson.getString("locationTypeCd"))) {//??????

            parkingSpaceDto.setCommunityId(reqJson.getString("communityId"));
            parkingSpaceDtos = parkingSpaceInnerServiceSMOImpl.queryParkingSpaces(parkingSpaceDto);

        } else if ("2000".equals(reqJson.getString("locationTypeCd"))) {//?????????
            //ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
            parkingSpaceDto.setCommunityId(reqJson.getString("communityId"));
            parkingSpaceDto.setPaId(reqJson.getString("locationObjId"));
            parkingSpaceDtos = parkingSpaceInnerServiceSMOImpl.queryParkingSpaces(parkingSpaceDto);

        } else if ("3000".equals(reqJson.getString("locationTypeCd"))) {//?????????
            //ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
            parkingSpaceDto.setCommunityId(reqJson.getString("communityId"));
            parkingSpaceDto.setPsId(reqJson.getString("locationObjId"));
            parkingSpaceDtos = parkingSpaceInnerServiceSMOImpl.queryParkingSpaces(parkingSpaceDto);
        } else {
            throw new IllegalArgumentException("??????????????????");
        }

        if (parkingSpaceDtos == null || parkingSpaceDtos.size() < 1) {
            throw new IllegalArgumentException("??????????????????????????????");
        }

        dealParkingSpaceFee(parkingSpaceDtos, context, reqJson, event);
    }

    private void dealParkingSpaceFee(List<ParkingSpaceDto> parkingSpaceDtos, DataFlowContext context, JSONObject reqJson, ServiceDataFlowEvent event) {

        AppService service = event.getAppService();


        HttpHeaders header = new HttpHeaders();
        context.getRequestCurrentHeaders().put(CommonConstant.HTTP_ORDER_TYPE_CD, "D");
        JSONArray businesses = new JSONArray();
        JSONObject paramInObj = null;
        ResponseEntity<String> responseEntity = null;
        int failParkingSpaces = 0;
        //??????????????????
        for (int parkingSpaceIndex = 0; parkingSpaceIndex < parkingSpaceDtos.size(); parkingSpaceIndex++) {

            businesses.add(addFee(parkingSpaceDtos.get(parkingSpaceIndex), reqJson, context));

            if (parkingSpaceIndex % DEFAULT_ADD_FEE_COUNT == 0 && parkingSpaceIndex != 0) {
                paramInObj = super.restToCenterProtocol(businesses, context.getRequestCurrentHeaders());
                //??? rest header ?????????????????????????????????
                super.freshHttpHeader(header, context.getRequestCurrentHeaders());

                responseEntity = this.callService(context, service.getServiceCode(), paramInObj);

                if (responseEntity.getStatusCode() != HttpStatus.OK) {
                    failParkingSpaces += businesses.size();
                }

                businesses = new JSONArray();
            }
        }
        if (businesses != null && businesses.size() > 0) {

            paramInObj = super.restToCenterProtocol(businesses, context.getRequestCurrentHeaders());
            //??? rest header ?????????????????????????????????
            super.freshHttpHeader(header, context.getRequestCurrentHeaders());
            responseEntity = this.callService(context, service.getServiceCode(), paramInObj);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                failParkingSpaces += businesses.size();
            }
        }

        JSONObject paramOut = new JSONObject();
        paramOut.put("totalParkingSpace", parkingSpaceDtos.size());
        paramOut.put("successParkingSpace", parkingSpaceDtos.size() - failParkingSpaces);
        paramOut.put("errorParkingSpace", failParkingSpaces);

        responseEntity = new ResponseEntity<>(paramOut.toJSONString(), HttpStatus.OK);

        context.setResponseEntity(responseEntity);
    }

    /**
     * ??????????????????
     *
     * @param paramInJson     ???????????????????????????
     * @param dataFlowContext ???????????????
     * @return ?????????????????????????????????
     */
    private JSONObject addFee(ParkingSpaceDto parkingSpaceDto, JSONObject paramInJson, DataFlowContext dataFlowContext) {


        JSONObject business = JSONObject.parseObject("{\"datas\":{}}");
        business.put(CommonConstant.HTTP_BUSINESS_TYPE_CD, BusinessTypeConstant.BUSINESS_TYPE_SAVE_FEE_INFO);
        business.put(CommonConstant.HTTP_SEQ, DEFAULT_SEQ + 1);
        business.put(CommonConstant.HTTP_INVOKE_MODEL, CommonConstant.HTTP_INVOKE_MODEL_S);
        JSONObject businessUnit = new JSONObject();
        businessUnit.put("feeId", "-1");
        businessUnit.put("configId", paramInJson.getString("configId"));
        businessUnit.put("feeTypeCd", paramInJson.getString("feeTypeCd"));
        businessUnit.put("incomeObjId", paramInJson.getString("storeId"));
        businessUnit.put("amount", "-1.00");
        businessUnit.put("startTime", DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
        businessUnit.put("endTime", DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
        businessUnit.put("communityId", paramInJson.getString("communityId"));
        businessUnit.put("payerObjId", parkingSpaceDto.getPsId());
        businessUnit.put("payerObjType", "6666");
        businessUnit.put("feeFlag", paramInJson.getString("feeFlag"));
        businessUnit.put("state", "2008001");
        businessUnit.put("userId", dataFlowContext.getRequestCurrentHeaders().get(CommonConstant.HTTP_USER_ID));
        business.getJSONObject(CommonConstant.HTTP_BUSINESS_DATAS).put("businessFee", businessUnit);

        return business;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    public IParkingSpaceInnerServiceSMO getParkingSpaceInnerServiceSMOImpl() {
        return parkingSpaceInnerServiceSMOImpl;
    }

    public void setParkingSpaceInnerServiceSMOImpl(IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl) {
        this.parkingSpaceInnerServiceSMOImpl = parkingSpaceInnerServiceSMOImpl;
    }
}
